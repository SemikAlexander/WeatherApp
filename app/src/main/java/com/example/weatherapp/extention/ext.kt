package com.example.weatherapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast

fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, message, duration).show()

fun Context.toast(messageId: Int, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, messageId, duration).show()

fun isActiveNetworkConnected(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkCapabilities =
        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    return networkCapabilities != null &&
        (
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            )
}
