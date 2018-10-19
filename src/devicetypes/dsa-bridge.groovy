/**
 *  DSA Bridge
 *
 *  Copyright 2018 Daniel Shapiro
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
 
metadata {
	definition (name: "DSA Bridge", namespace: "iot-dsa-v2", author: "Daniel Shapiro") {
		capability "Notification"
	}
    
    preferences {
        input("ip", "string",
            title: "Bridge DSLink IP Address",
            description: "Bridge DSLink IP Address",
            required: true,
            displayDuringSetup: true
        )
        input("port", "string",
            title: "Bridge DSLink Port",
            description: "Bridge DSLink Port",
            required: true,
            displayDuringSetup: true
        )
    }


	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		valueTile("basic", "device.ip", width: 3, height: 2) {
            state("basic", label:'OK')
        }
        main "basic"
	}
}

private String makeNetworkId(ipaddr, port) { 
     String hexIp = ipaddr.tokenize('.').collect { 
     String.format('%02X', it.toInteger()) 
     }.join() 
     String hexPort = String.format('%04X', port.toInteger()) 
     //log.debug "${hexIp}:${hexPort}"
     return "${hexIp}:${hexPort}" 
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
    
    String nid = makeNetworkId(ip, port)
    device.deviceNetworkId = "$nid"
    
	//def msg = parseLanMessage(description)
	def evt = createEvent(name: "message", value: description)
    log.debug evt
    return evt
}

// handle commands
def deviceNotification(message) {
	if (device.hub == null)
    {
        log.error "Hub is null, must set the hub in the device settings so we can get local hub IP and port"
        return
    }
    
    String nid = makeNetworkId(ip, port)
    device.deviceNetworkId = "$nid"
        
    //log.debug "Sending '${message}' to device"
    
    //message = '{"path":"/","method":"GET"}'

    def slurper = new JsonSlurper()
    def params = slurper.parseText(message)
    
    //if (parsed.path == '/subscribe') {
     //   parsed.body.callback = device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
    //}
    params.uri = "$ip:$port"
    
    //if (params.remove('method') == "GET") {
//    	try {
//            httpGet(params) { resp ->
//                resp.headers.each {
//                    log.debug "${it.name} : ${it.value}"
//                }
//                log.debug "response contentType: ${resp.contentType}"
//                log.debug "response data: ${resp.data}"
//                sendEvent(name: "message", value: resp.data.toString())
//            }
//        } catch (e) {
//            log.error "something went wrong: $e"
//        }
//    } else {
//    	try {
//            httpPutJson(params) { resp ->
//                resp.headers.each {
//                    log.debug "${it.name} : ${it.value}"
//                }
//                log.debug "response contentType: ${resp.contentType}"
//                log.debug "response data: ${resp.data}"
//                //sendEvent(name: "message", value: resp.data.toString())
//            }
//        } catch (e) {
//            log.error "something went wrong: $e"
//        }
//    }
    
    def headers = [:]
    headers.put("HOST", "$ip:$port")
    headers.put("Content-Type", "application/json")

    def hubAction = new physicalgraph.device.HubAction(
        [
            method: "PUT",
            path: params.path,
            headers: headers,
            body: params.body
        ],
        "$nid"
    )
    log.debug hubAction
    return hubAction
}