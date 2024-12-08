# mqtt-status

Congratulations! If you are here, you are most likely looking for a quick and simple solution
for the issue I also had: a barebones MQTT client sending messages when your computer is on. Well, this does just that.
Literally - *just* that. 

## Setup

The following is my setup on Linux. Windows *should* work too, but I haven't checked it, and the solution
suddenly stops being simple  since there are no instructions anymore.
This setup will run a user-level service - running an MQTT client as root written by an internet rando is a ***VERY***
stupid idea. I don't judge, but again, you have to figure that out on your own.

Prerequisites: JDK 21 installed.

1.  `git clone`
2.  `./gradlew jar`
3.  Copy `build/libs/*` to `~/.local/bin/`
4.  Create `~/.config/systemd/user` and write `mqtt_status.service`:
    ```
    [Unit]
    Description = Very simple MQTT client that updates power status every 30 seconds
    
    [Service]
    ExecStart = /usr/bin/java -jar %h/.local/bin/mqtt-status-1.0.jar
    
    [Install]
    WantedBy = plasma-workspace.target
    ```
    Your mileage may vary when it comes to `WantedBy` - check available targets with `systemctl --user list-units --type=target`.
5.  Create `~/.mqttstatus/config.yml`:
    ```yaml
    mqttHost: localhost
    mqttPort: 1883
    topic: pc/is_powered_on
    onValue: "true"
    offValue: "false"
    retainWill: true
    qos: AT_LEAST_ONCE # available values: AT_MOST_ONCE (0), AT_LEAST_ONCE (1), EXACTLY_ONCE (2)
    interval: 30s
    ```
    These are the defaults - in an unlikely scenario you like them all you can skip this step. 
    If not, modify to your liking. You can leave only the properties you want to change.
6.  `systemctl --user enable mqtt_status.service`

Pull requests and improvement ideas are welcome, although new features (and especially more data!) are unlikely to be merged.
This solution aims to be extremely simple.
