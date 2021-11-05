/**
 *	Vesternet VES-ZB-SWI-005 2-Wire Capable Switch
 * 
 */
metadata {
	definition (name: "Vesternet VES-ZB-SWI-005 2-Wire Capable Switch", namespace: "Vesternet", author: "Vesternet", importUrl: "https://raw.githubusercontent.com/vesternet/hubitat-zigbee-device-drivers/main/Vesternet%20VES-ZB-SWI-005%202-Wire%20Capable%20Switch.groovy") {
		capability "Switch"		
		capability "Actuator"
		capability "Configuration"
        capability "Refresh"

		fingerprint profileId: "0104", endpointId: "01", inClusters: "0000,0003,0004,0005,0006,0B05,1000", outClusters: "0019", manufacturer: "Sunricher", model: "Micro Smart OnOff", deviceJoinName: "Vesternet VES-ZB-SWI-005 2-Wire Capable Switch"
        fingerprint profileId: "0104", endpointId: "01", inClusters: "0000,0003,0004,0005,0006,0B05,1000", outClusters: "0019", manufacturer: "Sunricher", model: "HK-SL-RELAY-A", deviceJoinName: "Vesternet VES-ZB-SWI-005 2-Wire Capable Switch"        
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
    def cmds = zigbee.onOffConfig()
	logDebug("sending ${cmds}")
	return cmds
}

def refresh() {
	logDebug("refresh called")
	def cmds = zigbee.onOffRefresh()
    logDebug("sending ${cmds}")
	return cmds
}

def on() {
	logDebug("on called")
	def cmds = ["he cmd 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0006 1 {}"]
	logDebug("sending ${cmds}")
    state["action"] = "digitalon"
	return cmds
}

def off() {
	logDebug("off called")
	def cmds = ["he cmd 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0006 0 {}"]
	logDebug("sending ${cmds}")
    state["action"] = "digitaloff"
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
        if (descriptionMap.cluster == "0006" || descriptionMap.clusterId == "0006" || descriptionMap.clusterInt == 6) {
            if (descriptionMap.command == "0A" || descriptionMap.command == "01") {   
                if (descriptionMap.attrId == "0000" || descriptionMap.attrInt == 0) {
                    logDebug("on off (0006) on off report")
                    def onOffValue = zigbee.convertHexToInt(descriptionMap.value)
                    if (onOffValue == 1 || onOffValue == 0) {
                        logDebug("on off report is ${onOffValue}")	
                        def onOffState = onOffValue == 0 ? "off" : "on" 
                        def descriptionText = "${device.displayName} was turned ${onOffState}"
                        def currentValue = device.currentValue("switch") ?: "unknown"
                        if (onOffState == currentValue) {
                            descriptionText = "${device.displayName} is ${onOffState}"
                        }                
                        def type = "physical"
                        def action = state["action"] ?: "standby"
                        if (action == "digitalon" || action == "digitaloff") {
                            logDebug("action is ${action}")
                            type = "digital"
                            state["action"] = "standby"
                            logDebug("action set to standby")
                        }
                        logText(descriptionText)	                          
                        events.add(getEvent([name: "switch", value: onOffState, type: type, descriptionText: descriptionText]))
                    }
                    else {
                        logDebug("skipping onOffValue: ${onOffValue}")
                    }
                }
                else {
                    logDebug("on off (0006) attribute skipped")
                }
            }
            else {
                logDebug("on off (0006) command skipped")
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