/**
 *	Vesternet VES-ZB-PIR-021 Motion Sensor
 * 
 */
metadata {
	definition (name: "Vesternet VES-ZB-PIR-021 Motion Sensor", namespace: "Vesternet", author: "Vesternet", importUrl: "https://raw.githubusercontent.com/vesternet/hubitat-zigbee-device-drivers/main/Vesternet%20VES-ZB-PIR-021%Motion%20Sensor.groovy") {
		capability "MotionSensor"    
        capability "TamperAlert"    
		capability "Battery"
        capability "Sensor"        
        capability "Refresh"
		capability "Configuration"
        
        fingerprint profileId: "0104", endpointId: "01", inClusters: "0000,0001,0003,0500,0020,0B05", outClusters: "0019", model: "MotionSensor-ZB3.0", manufacturer: "Shyugj"
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
    def cmds = [ "he wattr 0x${device.deviceNetworkId} 0x01 0x0500 0x0010 0xF0 {${location.hub.zigbeeEui}}", "delay 2000",
                "he raw 0x${device.deviceNetworkId} 1 0x01 0x0500 {01 23 00 00 00}", "delay 2000",
                "he rattr 0x${device.deviceNetworkId} 0x01 0x0500 0 {}", "delay 200",
                "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0001 {${device.zigbeeId}} {}", "delay 200",
                "he cr 0x${device.deviceNetworkId} 0x01 0x0001 0x0021 0x20 3600 21600 {0200}", "delay 200",
                "he raw 0x${device.deviceNetworkId} 1 0x01 0x0001 {10 00 08 00 2100}", "delay 200",
                "he rattr 0x${device.deviceNetworkId} 0x01 0x0001 0x0021 {}"
                ]
    logDebug("sending ${cmds}")
	return cmds
}

def refresh() {
	logDebug("refresh called")
    logDebug("battery powered device requires manual wakeup to accept refresh commands")
    def cmds = [ "he rattr 0x${device.deviceNetworkId} 0x01 0x0001 0x0021 {}", "delay 200",
                "he rattr 0x${device.deviceNetworkId} 0x01 0x0500 0x0000 {}" ]
    logDebug("sending ${cmds}")
	return cmds
}

void parse(String description) {
	logDebug("parse called")
	logDebug("got description: ${description}")	
    if (description.startsWith("enroll") || description.startsWith("zone")) {
        def events = getIASEvents(description)
        if (events) {	
            events.each {		    
                sendEvent(it)
            }
        }
        else {	
            logDebug("Unhandled event: ${description}")			        	
        }
    }
    else {
        def descriptionMap = zigbee.parseDescriptionAsMap(description)
        def events = getEvents(descriptionMap)	
        if (events) {	
            events.each {		    
                sendEvent(it)
            }
        }
        else {	
            logDebug("Unhandled event: ${descriptionMap}")			        	
        }
    }
	   
}

def getIASEvents(description) {
    logDebug("getIASEvents called")
    logDebug("got description: ${description}")
	def events = []    
    if (description.startsWith("zone status")) {
        def zoneStatus = zigbee.parseZoneStatus(description)
        if (zoneStatus != null) {
            logDebug("got zoneStatus")
            logDebug("alarm1: ${zoneStatus.alarm1}")
            logDebug("alarm2: ${zoneStatus.alarm2}")
            logDebug("tamper: ${zoneStatus.tamper}")
            logDebug("battery: ${zoneStatus.battery}")
            logDebug("supervisionReports: ${zoneStatus.supervisionReports}")
            logDebug("restoreReports: ${zoneStatus.restoreReports}")
            logDebug("trouble: ${zoneStatus.trouble}")
            logDebug("ac: ${zoneStatus.ac}")
            logDebug("test: ${zoneStatus.test}")
            logDebug("batteryDefect: ${zoneStatus.batteryDefect}")
            def alarmState = zoneStatus.alarm1 || zoneStatus.alarm2
            logDebug("alarm state is ${alarmState}")        
            def motionState = alarmState ? "active" : "inactive"
            def descriptionText = "${device.displayName} motion is ${motionState}"        
            logText(descriptionText)
            events.add([name: "motion", value: motionState, descriptionText: descriptionText])            
            def tamperState = zoneStatus.tamper ? "detected" : "clear"
            logDebug("tamper state is ${tamperState}")        
            descriptionText = "${device.displayName} tamper is ${tamperState}"        
            logText(descriptionText)
            events.add([name: "tamper", value: tamperState, descriptionText: descriptionText])
        }
        else {
            logDebug("could not parse zoneStatus")
        }
    } 
    else if (description.startsWith("enroll request")) {
        logDebug("got enrollRequest")
        def zoneStateValue = state.iasZoneState
        if (zoneStateValue != null && zoneStateValue == "enrolled") {
            logDebug("ias zone state is already enrolled, ignoring")
        }
        else if (zoneStateValue != null && zoneStateValue == "not enrolled") {
            logDebug("ias zone state is not enrolled")
            def cmds = [ "he wattr 0x${device.deviceNetworkId} 0x01 0x0500 0x0010 0xF0 {${location.hub.zigbeeEui}}", "delay 2000",
                        "he raw 0x${device.deviceNetworkId} 1 0x01 0x0500 {01 23 00 00 00}", "delay 2000",
                        "he rattr 0x${device.deviceNetworkId} 0x01 0x0500 0x0000 {}" ]
            logDebug("sending ${cmds}")
            sendHubCommand(new hubitat.device.HubMultiAction(cmds, hubitat.device.Protocol.ZIGBEE))
        }        
    }
	return events
}

def getEvents(descriptionMap) {
    logDebug("getEvents called")
    logDebug("got descriptionMap: ${descriptionMap}")
	def events = []    
    if (!(descriptionMap.profileId) || (descriptionMap.profileId && descriptionMap.profileId == "0104")) {
        if (descriptionMap.cluster == "0500" || descriptionMap.clusterId == "0500" || descriptionMap.clusterInt == 1280) {        
            if (descriptionMap.command == "0A" || descriptionMap.command == "01") {
                if (descriptionMap.attrId == "0000" || descriptionMap.attrInt == 0) {
                    logDebug("ias (0500) zone state report")
                    def zoneStateValue = descriptionMap.value
                    logDebug("ias (0500) zone state is ${zoneStateValue}")
                    if (zoneStateValue == "00") {
                        logDebug("zone state is not enrolled")
                        state.iasZoneState = "not enrolled"
                    }
                    else if (zoneStateValue == "01") {
                        logDebug("zone state is enrolled")
                        state.iasZoneState = "enrolled"
                    }
                    else {
                        logDebug("could not determine zone state")
                    }
                }
                else if (descriptionMap.attrId == "0002" || descriptionMap.attrInt == 2) {
                    logDebug("ias (0500) zone status report")                    
                }
                else {
                    logDebug("ias (0500) attribute ${descriptionMap.attrId} ${descriptionMap.attrInt} skipped")
                }
            }
            else {
                logDebug("ias (0500) command ${descriptionMap.command} skipped")
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