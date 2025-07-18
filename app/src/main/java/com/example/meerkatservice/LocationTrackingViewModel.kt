package com.example.meerkatservice

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocationTrackingViewModel : ViewModel() {
    private val _isServiceBound = MutableStateFlow(false)
    val isServiceBound: StateFlow<Boolean> = _isServiceBound.asStateFlow()

    private var binder: LocationTrackingService.LocalBinder? = null
    val serviceCounter: MutableStateFlow<Int?> = MutableStateFlow<Int?>(null)
    val currentLocation: MutableStateFlow<Location?> = MutableStateFlow<Location?>(null)
    val locationError: MutableStateFlow<String?> = MutableStateFlow<String?>(null)

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            logger.trace("onServiceConnected name={} service={}", name, service)
            val localBinder = service as LocationTrackingService.LocalBinder
            binder = localBinder
            _isServiceBound.value = true
            // サービスからデータを監視する場合はここで開始
            viewModelScope.launch {
                runCatching {
                    localBinder.getService().currentLocation.collect { location ->
                        logger.debug("Service Location Data Updated: {}", location)
                        currentLocation.value = location
                    }
                }.onFailure {
                    logger.error("currentLocation", it)
                }
            }
            viewModelScope.launch {
                runCatching {
                    localBinder.getService().locationError.collect { error ->
                        logger.debug("Service Location Error Updated: {}", error)
                        locationError.value = error
                    }
                }.onFailure {
                    logger.error("locationError", it)
                }
            }
            viewModelScope.launch {
                runCatching {
                    localBinder.getService().currentCounter.collect { counter ->
                        // 必要に応じてデータをViewModelの別のStateFlowに更新
                        logger.debug("Service Data Updated: $counter")
                        serviceCounter.value = counter
                    }
                }.onFailure {
                    logger.error("currentCounter", it)
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            logger.trace("onServiceDisconnected name={}", name)
            binder = null
            _isServiceBound.value = false
        }
    }

    fun bindToService(context: Context) {
        logger.trace("bindToService")
        if (!_isServiceBound.value) {
            Intent(context, LocationTrackingService::class.java).also { intent ->
                context.startForegroundService(intent)
                context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    fun unbindFromService(context: Context) {
        logger.trace("unbindFromService")
        if (_isServiceBound.value) {
            context.unbindService(serviceConnection)
            binder = null
            _isServiceBound.value = false
        }
    }

    fun stopService(context: Context) {
        logger.trace("stopService")
        Intent(context, LocationTrackingService::class.java).also { intent ->
            context.stopService(intent)
        }
    }

    fun callServiceMethod() {
        logger.trace("callServiceMethod")
        binder?.getService()?.incrementCounter()
    }

    override fun onCleared() {
        // ViewModelが破棄されるときにアンバインドすることが重要
        // ただし、Activity/Fragmentのライフサイクルで管理する方が安全な場合もある
        // context.unbindService(serviceConnection) // ここで context を安全に取得する方法に注意
        logger.trace("onCleared")
        super.onCleared()
    }
}
