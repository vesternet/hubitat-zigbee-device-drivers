/**
 *	Vesternet VES-ZB-REM-013 4 Channel Remote Control
 * 
 */
metadata {
	definition (name: "Vesternet VES-ZB-REM-013 4 Channel Remote Control", namespace: "Vesternet", author: "Vesternet", importUrl: "https://raw.githubusercontent.com/vesternet/hubitat-zigbee-device-drivers/main/Vesternet%20VES-ZB-REM-013%204%20Channel%20Remote%20Control.groovy") {
		capability "PushableButton"
        capability "HoldableButton"
        capability "ReleasableButton"
		capability "Battery"
        capability "Sensor"        
		capability "Configuration"
        
		fingerprint profileId: "0104", endpointId: "01", inClusters: "0000,0001,0003,0B05", outClusters: "0003,0004,0005,0006,0008,0019,0300,1000", manufacturer: "Sunricher", model: "ZGRC-KEY-013", deviceJoinName: "Vesternet VES-ZB-REM-013 4 Channel Remote Control" 
        fingerprint profileId: "0104", endpointId: "01", inClusters: "0000,0001,0003,0B05,1000", outClusters: "0003,0004,0005,0006,0008,0019,0300,1000", manufacturer: "Sunricher", model: "ZGRC-KEY-013", deviceJoinName: "Vesternet VES-ZB-REM-013 4 Channel Remote Control"    
	}
	preferences {
        input name: "logEnable", type: "bool", title: "Enable Debug Logging", defaultValue: true
        input name: "txtEnable", type: "bool", title: "Enable descriptionText Logging", defaultValue: true
	}
}

def getModelNumberOfButtons() {
    logDebug("getModelNumberOfButtons called")
    ["ZGRC-KEY-013" : 10]
}

def installed() {
    device.updateSetting("logEnable", [value: "true", type: "bool"])
    device.updateSetting("txtEnable", [value: "true", type: "bool"])
    logDebug("installed called")	
    def numberOfButtons = modelNumberOfButtons[device.getDataValue("model")]
    logDebug("numberOfButtons: ${numberOfButtons}")
    sendEvent(getEvent(name: "numberOfButtons", value: numberOfButtons, displayed: false))
    for(def buttonNumber : 1..numberOfButtons) {
        sendEvent(buttonAction("pushed", buttonNumber, "digital"))
    }
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
    def cmds = [ "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0006 {${device.zigbeeId}} {}", "delay 200",
                "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0008 {${device.zigbeeId}} {}", "delay 200",
                "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0005 {${device.zigbeeId}} {}", "delay 200",
                "zdo bind 0x${device.deviceNetworkId} 0x02 0x01 0x0006 {${device.zigbeeId}} {}", "delay 200",
                "zdo bind 0x${device.deviceNetworkId} 0x02 0x01 0x0008 {${device.zigbeeId}} {}", "delay 200",
                "zdo bind 0x${device.deviceNetworkId} 0x03 0x01 0x0006 {${device.zigbeeId}} {}", "delay 200",
                "zdo bind 0x${device.deviceNetworkId} 0x03 0x01 0x0008 {${device.zigbeeId}} {}", "delay 200",
                "zdo bind 0x${device.deviceNetworkId} 0x04 0x01 0x0006 {${device.zigbeeId}} {}", "delay 200",
                "zdo bind 0x${device.deviceNetworkId} 0x04 0x01 0x0008 {${device.zigbeeId}} {}", "delay 200",
                "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0001 {${device.zigbeeId}} {}", "delay 200",
                "he cr 0x${device.deviceNetworkId} 0x01 0x0001 0x21 0x20 21600 21600 {0x01}" ]
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
    def endpoint = descriptionMap.sourceEndpoint ? zigbee.convertHexToInt(descriptionMap.sourceEndpoint) : descriptionMap.endpoint ? zigbee.convertHexToInt(descriptionMap.endpoint) : device.endpointId.toInteger()
    if (!(descriptionMap.profileId) || (descriptionMap.profileId && descriptionMap.profileId == "0104")) {
        if (descriptionMap.cluster == "0006" || descriptionMap.clusterId == "0006" || descriptionMap.clusterInt == 6) {
            def switchCommand = "${descriptionMap.command} unknown"
            switch (descriptionMap.command) {
                case "00": 
                    switchCommand = "off"
                    break
                case "01": 
                    switchCommand = "on"
                    break            
            }
            logDebug("switch (0006) command: ${switchCommand}")
            if (descriptionMap.command == "00" || descriptionMap.command == "01") {
                switchCommand = zigbee.convertHexToInt(descriptionMap.command)            
                def buttonNumber = switchCommand == 1 ? (endpoint * 2) - 1 : (endpoint * 2) 
                logDebug("button number is ${buttonNumber}")
                events.add(buttonAction("pushed", buttonNumber, "physical"))            
            }
            else {
                logDebug("switch (0006) command skipped")
            }
        }
        else if (descriptionMap.cluster == "0008" || descriptionMap.clusterId == "0008" || descriptionMap.clusterInt == 8) {
            def levelCommand = "${descriptionMap.command} unknown"
            switch (descriptionMap.command) {
                case "05": 
                    levelCommand = "move with on/off"
                    break
                case "07": 
                    levelCommand = "stop"
                    break            
            }       
            logDebug("level (0008) command: ${levelCommand}") 
            if (descriptionMap.command == "05" || descriptionMap.command == "01") {
                def levelDirectionData = descriptionMap.data[0];
                if (levelDirectionData == "00" || levelDirectionData == "01") {
                    def levelDirection = "${levelDirectionData} unknown"
                    switch (levelDirectionData) {
                        case "00": 
                            levelDirection = "up"
                            break
                        case "01": 
                            levelDirection = "down"
                            break            
                    }        
                    logDebug("level (0008) direction: ${levelDirection}")
                    levelDirection = zigbee.convertHexToInt(levelDirectionData)            
                    def buttonNumber = levelDirection == 0 ?  (endpoint * 2) - 1 : (endpoint * 2)
                    logDebug("button number is ${buttonNumber}")
                    logDebug("button event is held")                
                    events.add(buttonAction("held", buttonNumber, "physical"))          
                }
                else {
                    logDebug("level (0008) direction: ${levelDirectionData} unknown")
                }
            }
            else if (descriptionMap.command == "07" || descriptionMap.command =="03") {
                def buttonNumber = device.currentValue("held", true)
                if (buttonNumber) {                
                    logDebug("button number was ${buttonNumber}")
                    logDebug("button event is released")            
                    events.add(buttonAction("released", buttonNumber, "physical"))
                }
                else {
                    logDebug("could not determine buttonNumber")
                }
            }
            else {
                logDebug("level (0008) command skipped")
            }
        }    
        else if (descriptionMap.cluster == "0005" || descriptionMap.clusterId == "0005" || descriptionMap.clusterInt == 5) {
            if (endpoint == 1) {
                def sceneCommand = "${descriptionMap.command} unknown"
                switch (descriptionMap.command) {
                    case "05": 
                        sceneCommand = "recall scene"
                        break
                }        
                if (descriptionMap.command == "05") {
                    def sceneData = descriptionMap.data[2];
                    if (sceneData == "01" || sceneData == "02") {                
                        logDebug("scene (0005) command: ${sceneCommand} scene: ${sceneData}")
                        sceneData = zigbee.convertHexToInt(sceneData)            
                        def buttonNumber = sceneData == 1 ? 9 : 10
                        logDebug("button number is ${buttonNumber}")
                        logDebug("button event is pushed")
                        events.add(buttonAction("pushed", buttonNumber, "physical"))                        
                    }
                    else {
                        logDebug("scene (0005) command: ${sceneCommand} scene: ${sceneData} unknown")
                    }
                }
                else {
                    logDebug("scene (0005) command skipped")
                }
            }
            else {
                logDebug("got unexpected endpoint in scene command")
            }
        }
        else if (descriptionMap.cluster == "0001" || descriptionMap.clusterId == "0001" || descriptionMap.clusterInt == 1) {        
            if (descriptionMap.command == "0A" || descriptionMap.command == "01") {
                if (descriptionMap.attrId == "0021" || descriptionMap.attrInt == 33) {
                    logDebug("power configuration (0001) battery report")
                    def batteryValue = zigbee.convertHexToInt(descriptionMap.value)
                    if (batteryValue > 100) {
                        logDebug("battery value is more than 100, dividing by 2")
                        batteryValue = (batteryValue / 2).toInteger();
                    }
                    logDebug("battery percentage report is ${batteryValue}")		
                    def descriptionText = "${device.displayName} is ${batteryValue}%"
                    logText(descriptionText)	                          
                    events.add([name: "battery", value: batteryValue, unit: "%", descriptionText: descriptionText])
                }
                else {
                    logDebug("power configuration (0001) attribute skipped")
                }
            }
            else {
                logDebug("power configuration (0001) command skipped")
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

def push(button){
    logDebug("push called")    
    sendEvent(buttonAction("pushed", button, "digital"))
}

def hold(button){
    logDebug("hold called")
    sendEvent(buttonAction("held", button, "digital"))
}

def release(button){
    logDebug("release called")
    sendEvent(buttonAction("released", button, "digital"))
}

def buttonAction(action, button, type) {
    logDebug("buttonAction called button: ${button} action: ${action} type: ${type}")  
    def descriptionText = "${device.displayName} button ${button} is ${action}"
    logText(descriptionText)
	return getEvent([name: action, value: button, descriptionText: descriptionText, isStateChange: true, type: type])
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
