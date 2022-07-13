/**
 *	Vesternet VES-ZB-MOT-019 Motor Controller
 * 
 */
metadata {
	definition (name: "Vesternet VES-ZB-MOT-019 Motor Controller", namespace: "Vesternet", author: "Vesternet", importUrl: "https://raw.githubusercontent.com/vesternet/hubitat-zigbee-device-drivers/main/Vesternet%20VES-ZB-MOT-019%20Motor%20Controller.groovy", singleThreaded: true) {
		capability "Actuator"
        capability "WindowBlind"
		capability "WindowShade"
		capability "Configuration"
        capability "Refresh"
		
        fingerprint profileId: "0104", endpointId:"01", inClusters: "0000,0003,0004,0005,0006,0008,0102,0B05,1000", outClusters: "0019", manufacturer: "Sunricher", model: "HK-ZCC-A", deviceJoinName: "Vesternet VES-ZB-MOT-019 Motor Controller"                      
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
    def cmds = zigbee.configureReporting(0x0102, 0x0008, DataType.UINT8, 0, 0x0E10, 1) + zigbee.configureReporting(0x0102, 0x0009, DataType.UINT8, 0, 0x0E10, 1)
	logDebug("sending ${cmds}")
	return cmds
}

def refresh() {
	logDebug("refresh called")    
	def cmds = zigbee.readAttribute(0x0102, 0x0000) + zigbee.readAttribute(0x0102, 0x0007) + zigbee.readAttribute(0x0102, 0x0008) + zigbee.readAttribute(0x0102, 0x0009)
    logDebug("sending ${cmds}")
	return cmds
}

def open() {
    logDebug("open called")
    def cmds = zigbee.command(0x0102, 0x0)
	logDebug("sending ${cmds}")
    state["action"] = "digitalopen"    
    sendEvent(getEvent([name: "windowShade", value: "opening", type: "digital", descriptionText: "${device.displayName} is opening"]))
	return cmds    
}

def close() {
    logDebug("close called")
    def cmds = zigbee.command(0x0102, 0x1)
	logDebug("sending ${cmds}")
    state["action"] = "digitalclose"
    sendEvent(getEvent([name: "windowShade", value: "closing", type: "digital", descriptionText: "${device.displayName} is closing"]))
	return cmds
}

def setPosition(position) {
    logDebug("setPosition called")
    logDebug("got position: ${position}")
    def cmds = zigbee.command(0x0102, 0x5, intTo8bitUnsignedHex(position.toInteger()))
	logDebug("sending ${cmds}")
    state["action"] = "digitalsetposition"
    def currentValue =  device.currentValue("position") ?: "unknown"
    if (position < currentValue) {
        sendEvent(getEvent([name: "windowShade", value: "closing", type: "digital", descriptionText: "${device.displayName} is closing"]))
    }  
    else if (position > currentValue) {
        sendEvent(getEvent([name: "windowShade", value: "opening", type: "digital", descriptionText: "${device.displayName} is opening"]))
    }  
	return cmds
}

def startPositionChange(direction) {
    logDebug("startPositionChange called")
    logDebug("got direction: ${direction}")
    def upDown = direction == "down" ? 1 : 0    
    logDebug("upDown: ${upDown}")
    def cmds = zigbee.command(0x0102, intTo8bitUnsignedHex(upDown.toInteger()))
    logDebug("sending ${cmds}")
    state["action"] = "digitalstartpositionchange"
    def currentValue =  device.currentValue("position") ?: "unknown"
    if (direction == "down") {
        sendEvent(getEvent([name: "windowShade", value: "closing", type: "digital", descriptionText: "${device.displayName} is closing"]))
    }  
    else if (direction == "up") {
        sendEvent(getEvent([name: "windowShade", value: "opening", type: "digital", descriptionText: "${device.displayName} is opening"]))
    }  
	return cmds
}

def stopPositionChange() {
    logDebug("stopPositionChange called")
    def cmds = zigbee.command(0x0102, 0x2)
	logDebug("sending ${cmds}")
    state["action"] = "digitalstop"
    sendEvent(getEvent([name: "windowShade", value: "stopping", type: "digital", descriptionText: "${device.displayName} is stopping"]))
	return cmds
}

def setTiltLevel(tilt) {
    logDebug("setTiltLevel called")
    logDebug("got tilt: ${tilt}")
    def cmds = zigbee.command(0x0102, 0x8, intTo8bitUnsignedHex(tilt.toInteger()))
	logDebug("sending ${cmds}")
    state["action"] = "digitalsettilt"
    sendEvent(getEvent([name: "windowBlind", value: "tilting", type: "digital", descriptionText: "${device.displayName} is tilting"]))    
	return cmds
}

void parse(String description) {
	logDebug("parse called")
	logDebug("got description: ${description}")	
    def descriptionMap = zigbee.parseDescriptionAsMap(description)
    def events = getEvents(descriptionMap)	
	if (events) {	
        logDebug("parse returning events: ${events}")
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
        def endpoint = descriptionMap.sourceEndpoint ?: descriptionMap.endpoint ?: "unknown"
        logDebug("got endpoint: ${endpoint}")
        if (endpoint == "01") {
            if (descriptionMap.cluster == "0102" || descriptionMap.clusterId == "0102" || descriptionMap.clusterInt == 258) {
                if (descriptionMap.command == "0A" || descriptionMap.command == "01") {   
                    if (descriptionMap.attrId == "000" || descriptionMap.attrInt == 0) {
                        logDebug("window covering (0000) window covering type report")
                        def levelValue = zigbee.convertHexToInt(descriptionMap.value)
                        logDebug("window covering type report is ${levelValue}")	                        
                        def windowCoveringTypes = [ 0: "Rollershade", 1: "Rollershade - 2 Motor", 2: "Rollershade - Exterior", 3: "Rollershade - Exterior - 2 Motor", 4: "Drapery", 5: "Awning", 6: "Shutter", 7: "Tilt Blind - Tilt Only", 8: "Tilt Blind - Lift and Tilt", 9: "Projector Screen" ]
                        logDebug("window covering type is ${windowCoveringTypes[levelValue]}")	   
                        state["windowCoveringType"] = windowCoveringTypes[levelValue] ?: "unknown"                        
                    }
                    else if (descriptionMap.attrId == "0008" || descriptionMap.attrInt == 8) {
                        logDebug("window covering (0008) current position lift percentage report")
                        def levelValue = zigbee.convertHexToInt(descriptionMap.value)
                        logDebug("current position lift percentage report is ${levelValue}")	                        
                        def descriptionText = "${device.displayName} position was set to ${levelValue}%"
                        def currentValue =  device.currentValue("position") ?: "unknown"
                        if (levelValue == currentValue) {
                            descriptionText = "${device.displayName} position is ${levelValue}%"
                        }  
                        def type = "physical"
                        def action = state["action"] ?: "standby"
                        if (action == "digitalsetposition") {
                            logDebug("action is ${action}")
                            type = "digital"
                            state["action"] = "standby"
                            logDebug("action set to standby")
                        }
                        logText(descriptionText)
                        events.add(getEvent([name: "position", value: levelValue, unit: "%", type: type, descriptionText: descriptionText]))
                        if (levelValue == 0) {
                            events.add(getEvent([name: "windowShade", value: "closed", type: type, descriptionText: "${device.displayName} is closed"]))
                        }
                        else if (levelValue == 100) {
                            events.add(getEvent([name: "windowShade", value: "open", type: type, descriptionText: "${device.displayName} is open"]))
                        }
                        else {
                            events.add(getEvent([name: "windowShade", value: "partially open", type: type, descriptionText: "${device.displayName} is partially open"]))
                        }
                    }
                    else if (descriptionMap.attrId == "0009" || descriptionMap.attrInt == 9) {
                        logDebug("window covering (0009) current position tilt percentage report")
                        def levelValue = zigbee.convertHexToInt(descriptionMap.value)
                        logDebug("current position tilt percentage report is ${levelValue}")	                        
                        def descriptionText = "${device.displayName} tilt was set to ${levelValue}%"
                        def currentValue =  device.currentValue("level") ?: "unknown"
                        if (levelValue == currentValue) {
                            descriptionText = "${device.displayName} tilt is ${levelValue}%"
                        }  
                        def type = "physical"
                        def action = state["action"] ?: "standby"
                        if (action == "digitalsettilt") {
                            logDebug("action is ${action}")
                            type = "digital"
                            state["action"] = "standby"
                            logDebug("action set to standby")
                        }
                        logText(descriptionText)
                        events.add(getEvent([name: "tilt", value: levelValue, unit: "%", type: type, descriptionText: descriptionText]))
                    }
                    else {
                        logDebug("window covering (0102) attribute skipped")
                    }
                }
                else {
                    logDebug("window covering (0102) command skipped")
                }
            }
            else {
                logDebug("skipped")
            }
        }
        else {
            logDebug("skipped")
        }
        if (descriptionMap.additionalAttrs) {
            logDebug("got additionalAttrs: ${descriptionMap.additionalAttrs}")
            descriptionMap.additionalAttrs.each { 
                it.sourceEndpoint = descriptionMap.endpoint
                it.endpoint = descriptionMap.endpoint
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

def intTo16bitUnsignedHex(value) {
    def hexStr = zigbee.convertToHexString(value.toInteger(),4)
    return new String(hexStr.substring(2, 4) + hexStr.substring(0, 2))
}

def intTo8bitUnsignedHex(value) {
    return zigbee.convertToHexString(value.toInteger(), 2)
}