package net.jeikobu.mqttstatus

import kotlinx.coroutines.*
import org.tinylog.kotlin.Logger

fun main(): Unit = runBlocking {
    Logger.info { "Starting MqttStatus" }
    val scheduler = Scheduler(this)
    val app = MqttStatusApp(scheduler, ConfigHandler().getConfig())
    app.run()
}

