package com.example.meerkatservice

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.activity.result.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocationTrackingViewModel : ViewModel() {
    private val _isServiceBound = MutableStateFlow(false)
    val isServiceBound: StateFlow<Boolean> = _isServiceBound.asStateFlow()

    private var myService: LocationTrackingService? = null
    val serviceCounter: StateFlow<Int?> = myService?.currentCounter ?: MutableStateFlow(null)

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            logger.trace("onServiceConnected name={} service={}", name, service)
            val binder = service as LocationTrackingService.LocalBinder
            myService = binder.getService()
            _isServiceBound.value = true
            // サービスからデータを監視する場合はここで開始
            viewModelScope.launch {
                myService?.currentCounter?.collect { counter ->
                    // 必要に応じてデータをViewModelの別のStateFlowに更新
                    logger.debug("Service Data Updated: $counter")
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            logger.trace("onServiceDisconnected name={}", name)
            myService = null
            _isServiceBound.value = false
        }
    }

    fun bindToService(context: Context) {
        logger.trace("bindToService")
        if (!_isServiceBound.value) {
            Intent(context, LocationTrackingService::class.java).also { intent ->
                context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    fun unbindFromService(context: Context) {
        logger.trace("unbindFromService")
        if (_isServiceBound.value) {
            context.unbindService(serviceConnection)
            myService = null
            _isServiceBound.value = false
        }
    }

    fun callServiceMethod() {
        logger.trace("callServiceMethod")
//        myService?.doSomethingInService()
    }

    override fun onCleared() {
        // ViewModelが破棄されるときにアンバインドすることが重要
        // ただし、Activity/Fragmentのライフサイクルで管理する方が安全な場合もある
        // context.unbindService(serviceConnection) // ここで context を安全に取得する方法に注意
        super.onCleared()
    }
}
