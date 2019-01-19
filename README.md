# dslink-java-v2-smartthings

* Version: 1.0.0
* Java - version 1.6 and up.
* [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)


## Overview

If you are not familiar with DSA and links, an overview can be found at
[here](http://iot-dsa.org/get-started/how-dsa-works).

This link was built using the DSLink Java SDK which can be found
[here](https://github.com/iot-dsa-v2/sdk-dslink-java-v2).

## Usage
1. Install DSA and this DSLink, preferably on the same network as your SmartThings hub. A build zip of this DSLink is included in the buildzips folder of this repo.
2. In the DSLink, add a hub node, choosing a port to listen on for messages from your SmartThings hub.
3. Install the [Device Handler][dt] in the [Device Handler IDE][ide-dt] using "Create via code"
4. Add the "DSA Device" device in the [My Devices IDE][ide-mydev]. Enter DSA Device (or whatever) for the name. Select "DSA Bridge" for the type. The other values are up to you.
4. Configure the "DSA Device" in the [My Devices IDE][ide-mydev] with the IP Address of the machine running DSA and the Port you selected earlier (Note: This needs to be an address that the _SmartThings Hub_ can access. So if the hub and DSA are on the same local network, a local ip can be used)
5. Install the [Smart App][app] on the [Smart App IDE][ide-app] using "Create via code"
6. Configure the Smart App (via the Native App; on Android, this must be the Classic version) with the devices you want to share and the Device Handler you just installed as the bridge
7. See that the Hub node from step 2 gets populated with information about the devices you're sharing


## Acknowledgements

SmartThings MQTT Bridge

The smartapp and device handler groovy code was shamelessly copied from [stjohnjohnson's SmartThings MQTT Bridge](https://github.com/stjohnjohnson/smartthings-mqtt-bridge/blob/master/README.md) and then modified to send data to DSA rather than MQTT.


SDK-DSLINK-JAVA

This software contains unmodified binary redistributions of 
[sdk-dslink-java-v2](https://github.com/iot-dsa-v2/sdk-dslink-java-v2), which is licensed 
and available under the Apache License 2.0. An original copy of the license agreement can be found 
at https://github.com/iot-dsa-v2/sdk-dslink-java-v2/blob/master/LICENSE

 [dt]: https://github.com/iot-dsa-v2/dslink-java-v2-smartthings/blob/master/src/devicetypes/dsa-bridge.groovy
 [app]: https://github.com/iot-dsa-v2/dslink-java-v2-smartthings/blob/master/src/smartapps/dsa-bridge.groovy
 [ide-dt]: https://graph.api.smartthings.com/ide/devices
 [ide-mydev]: https://graph.api.smartthings.com/device/list
 [ide-app]: https://graph.api.smartthings.com/ide/apps
