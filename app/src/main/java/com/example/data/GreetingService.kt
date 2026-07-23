package com.example.data

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

class GreetingService {
    private val _greetingFlow = MutableStateFlow(getGreetingForCurrentTime())
    val greetingFlow: StateFlow<String> = _greetingFlow.asStateFlow()

    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        startAutoRefresh()
    }

    fun refreshGreeting() {
        _greetingFlow.value = getGreetingForCurrentTime()
    }

    private fun startAutoRefresh() {
        job?.cancel()
        job = scope.launch {
            while (isActive) {
                delay(60000) // 1 minute
                _greetingFlow.value = getGreetingForCurrentTime()
            }
        }
    }

    fun getGreetingForCurrentTime(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good Morning ☀️"
            in 12..16 -> "Good Afternoon 🌤️"
            in 17..20 -> "Good Evening 🌇"
            else -> "Good Night 🌙"
        }
    }

    fun onDestroy() {
        job?.cancel()
    }
}
