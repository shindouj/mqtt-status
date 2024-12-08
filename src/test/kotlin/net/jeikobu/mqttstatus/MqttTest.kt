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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.milliseconds

class MqttTest {

    private fun config(): Config = Config(
        topic = "john/paul/the/second",
        onValue = "ON_REPELS_MOSQUITOS",
        offValue = "OFF_ATTRACTS_MOSQUITOS",
        qos = Qos.AT_LEAST_ONCE,
        retainWill = true
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @ExperimentalUnsignedTypes
    @Test
    fun `happy path`() = runTest {
        // given
        val expectedConfig = config()
        var connectPacket : MQTT4Connect? = null
        var subscribePacket: MQTT4Subscribe? = null
        var publishPacket: MQTT4Publish? = null

        val broker = Broker(packetInterceptor = object : PacketInterceptor {
            override fun packetReceived(
                clientId: String,
                username: String?,
                password: UByteArray?,
                packet: MQTTPacket
            ) {
                println(packet)
                when (packet) {
                    is MQTT4Connect -> connectPacket = packet
                    is MQTT4Subscribe -> subscribePacket = packet
                    is MQTT4Publish -> publishPacket = packet
                }
            }

        })

        val scheduler = Scheduler(scope = backgroundScope)
        val app = MqttStatusApp(
            scheduler = scheduler,
            config = expectedConfig,
            interval = 1.milliseconds
        )

        // when
        app.run()
        repeat(3) { broker.step(); advanceTimeBy(1.milliseconds) }

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
                assertEquals(expected = expectedConfig.topic, actual = sub.topicFilter)
            }
        }

        publishPacket.let { packet ->
            assertNotNull(actual = packet)
            assertEquals(expected = expectedConfig.onValue, actual = packet.payload?.toByteArray()?.decodeToString())
            assertEquals(expected = expectedConfig.qos, actual = packet.qos)
            assertEquals(expected = expectedConfig.topic, actual = packet.topicName)
            assertFalse(actual = packet.retain)
        }
    }
}