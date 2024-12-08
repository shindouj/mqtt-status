package net.jeikobu.mqttstatus

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addFileSource
import kotlinx.coroutines.*
import org.tinylog.kotlin.Logger
import java.io.File

fun main(): Unit = runBlocking {
    val scheduler = Scheduler(this)
    val app = MqttStatusApp(scheduler, getConfig())
    app.run()
}

private fun getConfig(): Config {
    val homeDirectory = System.getProperty("user.home")
    val configPath = "$homeDirectory${File.separator}.mqttstatus${File.separator}config.yml"

    Logger.info("Configuration path = $configPath")

    return ConfigLoaderBuilder
        .default()
        .addFileSource(configPath)
        .build()
        .loadConfigOrThrow<Config>()
}

