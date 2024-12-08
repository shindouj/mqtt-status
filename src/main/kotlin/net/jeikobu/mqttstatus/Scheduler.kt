package net.jeikobu.mqttstatus

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import kotlin.time.Duration
import kotlin.time.toJavaDuration

class Scheduler(private val scope: CoroutineScope) {
    fun schedule(interval: Duration, task: suspend () -> Unit): Job {
        return scope.launch {
            while (isActive) {
                task()
                delay(interval.toJavaDuration())
            }
        }
    }
}