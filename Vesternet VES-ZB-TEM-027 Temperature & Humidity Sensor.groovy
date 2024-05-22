/**
 *	Vesternet VES-ZB-TEM-027 Temperature & Humidity Sensor
 * 
 */
metadata {
	definition (name: "Vesternet VES-ZB-TEM-027 Temperature & Humidity Sensor", namespace: "Vesternet", author: "Vesternet", importUrl: "https://raw.githubusercontent.com/vesternet/hubitat-zigbee-device-drivers/main/Vesternet%20VES-ZB-TEM-027%20Temperature%20%26%20Humidity%20Sensor.groovy") {
		capability "TemperatureMeasurement"
        capability "RelativeHumidityMeasurement"
		capability "Battery"
        capability "Sensor"        
        capability "Refresh"
		capability "Configuration"
        
		fingerprint profileId: "0104", endpointId: "01", inClusters: "0000,0001,0003,0402,0405,0020,0B05", outClusters: "0019", model: "TempAndHumSensor-ZB3.0", manufacturer: "Shyugj"        
	}
	preferences {
        input name: "logEnable", type: "bool", title: "Enable Debug Logging", defaultValue: true
        input name: "txtEnable", type: "bool", title: "Enable descriptionText Logging", defaultValue: true
	}
}

def installed() {
    device.updateSetting("logEnable", [value: "true", type: "bool"])
    device.updateSetting("txtEnable", [value: "true", type: "bool"])
    logDebug("installed called")	
	runIn(1800,logsOff)
}

def updated() {
	logDebug("updated called")
	log.warn("debug logging is: ${logEnable == true}")
    log.warn("descriptionText logging is: ${txtEnable == true}")
    state.clear()
	unschedule()
	if (logEnable) runIn(1800,logsOff)
}

def configure() {
	logDebug("configure called")
    logDebug("battery powered device requires manual wakeup to accept configuration commands")
    def cmds = [ "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0402 {${device.zigbeeId}} {}", "delay 200",
                "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0405 {${device.zigbeeId}} {}", "delay 200",
                "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0001 {${device.zigbeeId}} {}", "delay 200",
                "he cr 0x${device.deviceNetworkId} 0x01 0x0402 0x0000 0x29 60 1800 {6400}", "delay 200",
                "he cr 0x${device.deviceNetworkId} 0x01 0x0405 0x0000 0x21 60 1800 {6400}", "delay 200",
                "he cr 0x${device.deviceNetworkId} 0x01 0x0001 0x0021 0x20 3600 21600 {0200}", "delay 200",
                "he raw 0x${device.deviceNetworkId} 1 0x01 0x0402 {10 00 08 00 0000}", "delay 200",
                "he raw 0x${device.deviceNetworkId} 1 0x01 0x0405 {10 00 08 00 0000}", "delay 200",
                "he raw 0x${device.deviceNetworkId} 1 0x01 0x0001 {10 00 08 00 2100}", "delay 200",
                "he rattr 0x${device.deviceNetworkId} 0x01 0x0402 0 {}", "delay 200",
                "he rattr 0x${device.deviceNetworkId} 0x01 0x0405 0 {}", "delay 200",
                "he rattr 0x${device.deviceNetworkId} 0x01 0x0001 0x0021 {}"
                ]
    logDebug("sending ${cmds}")
	return cmds
}

def refresh() {
	logDebug("refresh called")
    logDebug("battery powered device requires manual wakeup to accept refresh commands")
    def cmds = [ "he rattr 0x${device.deviceNetworkId} 0x01 0x0402 0 {}", "delay 200",
                "he rattr 0x${device.deviceNetworkId} 0x01 0x0405 0 {}", "delay 200",
                "he rattr 0x${device.deviceNetworkId} 0x01 0x0001 0x0021 {}" ]
    logDebug("sending ${cmds}")
	return cmds
}

void parse(String description) {
	logDebug("parse called")
	logDebug("got description: ${description}")	
    def descriptionMap = zigbee.parseDescriptionAsMap(description)
    def events = getEvents(descriptionMap)	
	if (events) {	
        events.each {		    
            sendEvent(it)
        }
	}
	else {	
        logDebug("Unhandled command: ${descriptionMap}")			        	
	}   
}

def getEvents(descriptionMap) {
    logDebug("getEvents called")
    logDebug("got descriptionMap: ${descriptionMap}")
	def events = []    
    if (!(descriptionMap.profileId) || (descriptionMap.profileId && descriptionMap.profileId == "0104")) {
        if (descriptionMap.cluster == "0402" || descriptionMap.clusterId == "0402" || descriptionMap.clusterInt == 1026) {
            if (descriptionMap.command == "0A" || descriptionMap.command == "01") {
                if (descriptionMap.attrId == "0000" || descriptionMap.attrInt == 0) {
                    logDebug("temperature (0402) measured value")
                    def temperatureValue = zigbee.convertHexToInt(descriptionMap.value)                    
                    logDebug("temperature report is ${temperatureValue}")		
                    temperatureValue = temperatureValue / 100
                    def descriptionText = "${device.displayName} temperature is ${temperatureValue}°C"
                    logText(descriptionText)	                          
                    events.add([name: "temperature", value: temperatureValue, unit: "°C", descriptionText: descriptionText])
                }
                else {
                    logDebug("temperature (0402) attribute ${descriptionMap.attrId} ${descriptionMap.attrInt} skipped")
                }
            }
            else {
                logDebug("temperature (0402) command ${descriptionMap.command} skipped")
            }
        }
        else if (descriptionMap.cluster == "0405" || descriptionMap.clusterId == "0405" || descriptionMap.clusterInt == 1029) {
            if (descriptionMap.command == "0A" || descriptionMap.command == "01") {
                if (descriptionMap.attrId == "0000" || descriptionMap.attrInt == 0) {
                    logDebug("humidity (0405) measured value")
                    def humidityValue = zigbee.convertHexToInt(descriptionMap.value)                    
                    logDebug("humidity report is ${humidityValue}")		
                    humidityValue = humidityValue / 100
                    def descriptionText = "${device.displayName} humidity is ${humidityValue}%"
                    logText(descriptionText)	                          
                    events.add([name: "humidity", value: humidityValue, unit: "%", descriptionText: descriptionText])
                }
                else {
                    logDebug("humidity (0405) attribute ${descriptionMap.attrId} ${descriptionMap.attrInt} skipped")
                }
            }
            else {
                logDebug("humidity (0405) command ${descriptionMap.command} skipped")
            }
        }
        else if (descriptionMap.cluster == "0001" || descriptionMap.clusterId == "0001" || descriptionMap.clusterInt == 1) {        
            if (descriptionMap.command == "0A" || descriptionMap.command == "01") {
                if (descriptionMap.attrId == "0021" || descriptionMap.attrInt == 33) {
                    logDebug("power configuration (0001) battery percentage report")
                    def batteryValue = zigbee.convertHexToInt(descriptionMap.value)
                    if (batteryValue > 100) {
                        logDebug("battery value is more than 100, dividing by 2")
                        batteryValue = batteryValue / 2;
                    }
                    logDebug("battery percentage report is ${batteryValue}")		
                    def descriptionText = "${device.displayName} battery percent is ${batteryValue}%"
                    logText(descriptionText)	                          
                    events.add([name: "battery", value: batteryValue, unit: "%", descriptionText: descriptionText, isStateChange: true])
                }
                else if (descriptionMap.attrId == "0020" || descriptionMap.attrInt == 32) {
                    logDebug("power configuration (0001) battery voltage report")
                    def batteryValue = zigbee.convertHexToInt(descriptionMap.value)                    
                    logDebug("battery voltage report is ${batteryValue}")		
                    batteryValue = batteryValue * 100 / 1000
                    def descriptionText = "${device.displayName} battery voltage is ${batteryValue}V"
                    logText(descriptionText)
                }
                else {
                    logDebug("power configuration (0001) attribute ${descriptionMap.attrId} ${descriptionMap.attrInt} skipped")
                }
            }
            else {
                logDebug("power configuration (0001) command ${descriptionMap.command} skipped")
            }
        }  
        else {
            logDebug("skipped")
        }
        if (descriptionMap.additionalAttrs) {
            logDebug("got additionalAttrs: ${descriptionMap.additionalAttrs}")
            descriptionMap.additionalAttrs.each { 
                it.clusterInt = descriptionMap.clusterInt
                it.cluster = descriptionMap.cluster
                it.clusterId = descriptionMap.clusterId          
                it.command = descriptionMap.command  
                events.add(getEvents(it))
            }
        }
    }
	return events
}

def getEvent(event) {
    logDebug("getEvent called data: ${event}")
    return createEvent(event)
}

def logDebug(msg) {
	if (logEnable != false) {
		log.debug("${msg}")
	}
}

def logText(msg) {
	if (txtEnable != false) {
		log.info("${msg}")
	}
}

def logsOff() {
    log.warn("debug logging disabled")
    device.updateSetting("logEnable", [value:"false", type: "bool"])
}