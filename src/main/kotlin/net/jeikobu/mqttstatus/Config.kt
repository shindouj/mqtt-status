package net.jeikobu.mqttstatus

import mqtt.packets.Qos
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class Config(
    val mqttHost: String = "localhost",
    val mqttPort: Int = 1883,
    val topic: String = "pc/is_powered_on",
    val onValue: String = "true",
    val offValue: String = "false",
    val retainWill: Boolean = true,
    val qos: Qos = Qos.AT_LEAST_ONCE,
    val interval: Duration = 30.seconds,
)