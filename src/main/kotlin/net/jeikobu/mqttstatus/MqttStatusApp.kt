package net.jeikobu.mqttstatus

import MQTTClient
import mqtt.MQTTVersion
import mqtt.Subscription
import mqtt.packets.Qos
import mqtt.packets.mqttv5.SubscriptionOptions
import org.tinylog.kotlin.Logger
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalUnsignedTypes::class)
class MqttStatusApp(private val scheduler: Scheduler, private val config: Config, private val interval: Duration = 30.seconds) {
    fun run() {
        Logger.trace { "Configuration file = $config" }

        val onValue = config.onValue.encodeToByteArray().toUByteArray()
        val offValue = config.offValue.encodeToByteArray().toUByteArray()

        val client = getClient(config, offValue)

        client.subscribe(listOf(Subscription(config.topic, SubscriptionOptions(Qos.EXACTLY_ONCE))))
        repeat(times = 2) { client.step() }

        publish(client, config, onValue)
        scheduler.schedule(interval = interval, task = { publish(client, config, onValue) })
    }

    private fun getClient(config: Config, offValue: UByteArray): MQTTClient = MQTTClient(
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

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun publish(client: MQTTClient, config: Config, payload: UByteArray) {
        client.publish(false, config.qos, config.topic, payload);
        repeat(times = 2) { client.step() }
    }
}