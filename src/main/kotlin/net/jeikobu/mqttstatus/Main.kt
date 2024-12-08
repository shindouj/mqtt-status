package net.jeikobu.mqttstatus

import MQTTClient
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addFileSource
import kotlinx.coroutines.*
import kotlinx.coroutines.time.delay
import mqtt.MQTTVersion
import mqtt.Subscription
import mqtt.packets.Qos
import mqtt.packets.mqttv5.SubscriptionOptions
import org.tinylog.kotlin.Logger
import java.io.File
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.time.toJavaDuration

@OptIn(ExperimentalUnsignedTypes::class)
fun main(): Unit = runBlocking {
    val homeDirectory = System.getProperty("user.home")
    val configPath = "$homeDirectory${File.separator}.mqttstatus${File.separator}config.yml"

    Logger.info("Configuration path = $configPath")

    val config = ConfigLoaderBuilder
        .default()
        .addFileSource(configPath)
        .build()
        .loadConfigOrThrow<Config>()

    Logger.trace { "Configuration file = $config" }

    val onValue = config.onValue.encodeToByteArray().toUByteArray()
    val offValue = config.offValue.encodeToByteArray().toUByteArray()

    val client = MQTTClient(
        MQTTVersion.MQTT3_1_1,
        config.mqttHost,
        config.mqttPort,
        null,
        willTopic = config.topic,
        willQos = config.qos,
        willRetain = config.retainWill,
        willPayload = offValue,
    ) {
        val payload = it.payload?.toByteArray()?.decodeToString()
        Logger.info("${config.topic} = $payload")
    }

    client.subscribe(listOf(Subscription(config.topic, SubscriptionOptions(Qos.EXACTLY_ONCE))))
    repeat(times = 2) { client.step() }

    publish(client, config.topic, onValue)
    schedule(task = { publish(client, config.topic, onValue) })
}

@OptIn(ExperimentalUnsignedTypes::class)
fun publish(client: MQTTClient, topic: String, payload: UByteArray) {
    client.publish(false, Qos.EXACTLY_ONCE, topic, payload);
    repeat(times = 2) { client.step() }
}

fun CoroutineScope.schedule(interval: Long = 30, unit: DurationUnit = DurationUnit.SECONDS, task: suspend () -> Unit): Job {
    return launch {
        while (isActive) {
            task()
            delay(interval.toDuration(unit).toJavaDuration())
        }
    }
}