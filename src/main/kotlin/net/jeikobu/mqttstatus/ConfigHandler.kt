package net.jeikobu.mqttstatus

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addFileSource
import org.tinylog.kotlin.Logger
import java.io.File

class ConfigHandler(
    private val configPath: String = buildString {
        append(System.getProperty("user.dir"))
        append(File.separator)
        append(".mqttstatus")
        append(File.separator)
        append("config.yml")
}) {
    fun getConfig(): Config {
        Logger.info("Configuration path = $configPath")

        return ConfigLoaderBuilder
            .default()
            .allowEmptyConfigFiles()
            .addFileSource(configPath, optional = true)
            .build()
            .loadConfigOrThrow<Config>()
    }
}