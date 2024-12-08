package net.jeikobu.mqttstatus

import mqtt.packets.Qos
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class ConfigTest {
    @Test
    fun `correctly grabs and parses config`() {
        // given
        val configPath = "src/test/resources/config.yml"
        val expectedConfig = Config(
            mqttHost = "hocallost",
            mqttPort = 3881,
            topic = "john/paul/the/second",
            onValue = "off",
            offValue = "on",
            retainWill = false,
            qos = Qos.AT_MOST_ONCE,
            interval = 10.seconds
        )

        // when
        val config = ConfigHandler(configPath).getConfig()

        // then
        assertEquals(expected = expectedConfig, actual = config)
    }

    @Test
    fun `correctly grabs defaults when file not found`() {
        // given
        val configPath = "this/path/does/not/exist.yml"
        val expectedConfig = Config()

        // when
        val config = ConfigHandler(configPath).getConfig()

        // then
        assertEquals(expected = expectedConfig, actual = config)
    }
}