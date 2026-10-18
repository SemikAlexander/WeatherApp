package com.example.weatherapp.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/** Базовый маркер Intent'а (пользовательское действие/событие) в MVI. */
interface IaIntent

/** Базовый маркер однократного побочного эффекта (навигация, тост, перезапуск). */
interface IaEffect

/**
 * Базовый MVI-ViewModel.
 *
 * Контракт:
 *  - state    — единственный источник истины экрана (Immutable, [StateFlow]);
 *  - effects  — однократные события, не являющиеся состоянием ([SharedFlow], replay=0);
 *  - reducer  — чистая функция (oldState, intent) -> newState; вызывается на главном потоке;
 *  - handle   — обработка побочных эффектов интента (сеть и т.п.) во [viewModelScope].
 *
 * Все интенты проходят через [dispatch] — единая точка входа, что даёт
 * предсказуемость и трассируемость потока данных.
 */
abstract class MviViewModel<I : IaIntent, S : Any, E : IaEffect> : ViewModel() {

    private val _state: MutableStateFlow<S> by lazy { MutableStateFlow(initialState()) }
    val state: StateFlow<S> get() = _state.asStateFlow()

    /**
     * extraBufferCapacity + DROP_OLDEST: эффект не теряется, если подписчик
     * ещё не активен, и dispatch никогда не суспендируется.
     */
    private val _effects = MutableSharedFlow<E>(
        replay = 0,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val effects: SharedFlow<E> = _effects.asSharedFlow()

    abstract fun initialState(): S

    /** Чистое преобразование состояния. Без side effects — тестопригодно. */
    abstract fun reduce(state: S, intent: I): S

    /** Побочные эффекты интента (запросы в репозиторий и пр.). Вызывается после reduce. */
    open suspend fun handle(intent: I) {}

    protected fun setState(newState: S) {
        _state.value = newState
    }

    protected fun emitEffect(effect: E) {
        _effects.tryEmit(effect)
    }

    /**
     * Единая точка входа для всех пользовательских действий.
     *
     * ВАЖНО (баг, из-за которого не работал SwipeRefresh): reduce выполнялся
     * внутри coroutine-dispatcher'а Main — т.е. АСИНХРОННО относительно кадра
     * Compose. Жест свайпа фиксировался в state до того, как isLoading успевал
     * стать true, и Accompanist SwipeRefresh мгновенно сбрасывал спиннер —
     * визуально «рефреш не происходит». Поэтому reduce применяется
     * СИНХРОННО на текущем потоке (UI-потоке), а суспендируемый handle —
     * отдельно в viewModelScope.
     */
    fun dispatch(intent: I) {
        _state.value = reduce(_state.value, intent)
        viewModelScope.launch { handle(intent) }
    }

    /**
     * Хелпер: подписка на поток домена с трансформацией в интент экрана.
     * Возвращает [Job] подписки — вызывающий код может явно отменить её;
     * если результат не нужен, пишите `asIntent(...) // = Job`.
     */
    protected fun <T> Flow<T>.asIntent(transform: (T) -> I): Job =
        onEach { dispatch(transform(it)) }.launchIn(viewModelScope)
}
