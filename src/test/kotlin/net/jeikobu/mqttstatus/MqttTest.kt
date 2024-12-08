package net.jeikobu.mqttstatus

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import mqtt.broker.Broker
import mqtt.broker.interfaces.PacketInterceptor
import mqtt.packets.MQTTPacket
import mqtt.packets.Qos
import mqtt.packets.mqttv4.MQTT4Connect
import mqtt.packets.mqttv4.MQTT4Publish
import mqtt.packets.mqttv4.MQTT4Subscribe
import java.net.ServerSocket
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.milliseconds

class MqttTest {

    private fun findFreePort() = ServerSocket(0).use { it.localPort }

    private fun config(): Config = Config(
        mqttPort = findFreePort(),
        topic = "john/paul/the/second",
        onValue = "ON_REPELS_MOSQUITOS",
        offValue = "OFF_ATTRACTS_MOSQUITOS",
        qos = Qos.AT_LEAST_ONCE,
        retainWill = true,
        interval = 1.milliseconds
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @ExperimentalUnsignedTypes
    @Test
    fun `connects to MQTT broker and sends correct data`() = runTest {
        // given
        val repeats = 3
        val expectedConfig = config()
        var connectPacket : MQTT4Connect? = null
        var subscribePacket: MQTT4Subscribe? = null
        val publishPackets: MutableList<MQTT4Publish> = mutableListOf()

        val broker = Broker(port = expectedConfig.mqttPort, packetInterceptor = object : PacketInterceptor {
            override fun packetReceived(
                clientId: String,
                username: String?,
                password: UByteArray?,
                packet: MQTTPacket
            ) {
                when (packet) {
                    is MQTT4Connect -> connectPacket = packet
                    is MQTT4Subscribe -> subscribePacket = packet
                    is MQTT4Publish -> {
                        publishPackets += packet
                    }
                }
            }

        })

        val scheduler = Scheduler(scope = backgroundScope)
        val app = MqttStatusApp(
            scheduler = scheduler,
            config = expectedConfig
        )

        // when
        app.run()
        repeat(repeats) { broker.step(); advanceTimeBy(1.milliseconds) }

        // then
        connectPacket.let { packet ->
            assertNotNull(actual = packet)
            assertNotNull(actual = packet.willTopic)
            assertEquals(expected = expectedConfig.topic, actual = packet.willTopic)
            assertNotNull(actual = packet.willPayload)
            assertEquals(expected = expectedConfig.offValue, actual = packet.willPayload?.toByteArray()?.decodeToString())
        }

        subscribePacket.let { packet ->
            assertNotNull(actual = packet)
            assertEquals(expected = 1, actual = packet.subscriptions.size)

            packet.subscriptions[0].let { sub ->
                assertNotNull(actual = sub)
                assertEquals(expected = expectedConfig.qos, actual = sub.options.qos)
                assertEquals(expected = expectedConfig.topic, actual = sub.topicFilter)
            }
        }

        assertEquals(expected = repeats, actual = publishPackets.size)
        publishPackets.forEach { packet ->
            assertNotNull(actual = packet)
            assertEquals(expected = expectedConfig.onValue, actual = packet.payload?.toByteArray()?.decodeToString())
            assertEquals(expected = expectedConfig.qos, actual = packet.qos)
            assertEquals(expected = expectedConfig.topic, actual = packet.topicName)
            assertFalse(actual = packet.retain)
        }
    }
}