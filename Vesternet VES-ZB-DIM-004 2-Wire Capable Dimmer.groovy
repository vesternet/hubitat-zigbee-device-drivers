/**
 *	Vesternet VES-ZB-DIM-004 2-Wire Capable Dimmer
 * 
 */
metadata {
	definition (name: "Vesternet VES-ZB-DIM-004 2-Wire Capable Dimmer", namespace: "Vesternet", author: "Vesternet", importUrl: "https://raw.githubusercontent.com/vesternet/hubitat-zigbee-device-drivers/main/Vesternet%20VES-ZB-DIM-004%202-Wire%20Capable%20Dimmer.groovy") {
		capability "Switch"
		capability "SwitchLevel"
		capability "ChangeLevel"		
        capability "Actuator"
        capability "PowerMeter"
		capability "EnergyMeter"
		capability "VoltageMeasurement"		
        capability "CurrentMeter"
		capability "Sensor"
		capability "Configuration"
        capability "Refresh"

		fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0008,0702,0B04,0B05,1000", outClusters: "0019", manufacturer: "Sunricher", model: "Micro Smart Dimmer", deviceJoinName: "Vesternet VES-ZB-DIM-004 2-Wire Capable Dimmer"
        fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0008,0702,0B04,0B05,1000", outClusters: "0019", manufacturer: "Sunricher", model: "HK-SL-DIM-A", deviceJoinName: "Vesternet VES-ZB-DIM-004 2-Wire Capable Dimmer"
	}

	preferences {
        input name: "levelTransitionTime", type: "enum", title: "Transition Time (ms)", options: [0:"0ms",500:"0.5s",1000:"1s",1500:"1.5s",2000:"2s",3000:"3s",4000:"4s",5000:"5s"], defaultValue: 1000
        input name: "levelChangeTime", type: "enum", title: "Level Change Time (s)", options: [5:"5s",10:"10s",15:"15s",20:"20s",25:"25s",30:"30s",35:"35s",40:"40s",45:"45s",50:"50s",55:"55s",60:"60s"], defaultValue: 30
        input name: "powerReportEnable", type: "bool", title: "Enable Power, Voltage & Current Reporting", defaultValue: false       
        input name: "powerReportChange", type: "enum", title: "Power Change (W)", options: [1:"1W",2:"2W",3:"3W",4:"4W",5:"5W",6:"6W",7:"7W",8:"8W",9:"9W",10:"10W",15:"15W",20:"20W"], defaultValue: 5
        input name: "powerReportTime", type: "enum", title: "Power Time (s)", options: [10:"10s",20:"20s",30:"30s",40:"40s",50:"50s",60:"60s",90:"90s",120:"120s",240:"240s",300:"300s",600:"600s",1200:"1200s"], defaultValue: 600         
        input name: "voltageReportChange", type: "enum", title: "Voltage Change (V)", options: [1:"1V",2:"2V",3:"3V",4:"4V",5:"5V",6:"6V",7:"7V",8:"8V",9:"9V",10:"10V",15:"15V",20:"20V"], defaultValue: 5   
        input name: "voltageReportTime", type: "enum", title: "Voltage Time (s)", options: [10:"10s",20:"20s",30:"30s",40:"40s",50:"50s",60:"60s",90:"90s",120:"120s",240:"240s",300:"300s",600:"600s",1200:"1200s"], defaultValue: 600         
        input name: "currentReportChange", type: "enum", title: "Current Change (A)", options: [0.1:"0.1A",0.2:"0.2A",0.3:"0.3A",0.4:"0.4A",0.5:"0.5A",0.6:"0.6A",0.7:"0.7A",0.8:"0.8A",0.9:"0.9A",1:"1A",1.5:"1.5A",2:"2A"], defaultValue: 0.5
        input name: "currentReportTime", type: "enum", title: "Current Time (s)", options: [10:"10s",20:"20s",30:"30s",40:"40s",50:"50s",60:"60s",90:"90s",120:"120s",240:"240s",300:"300s",600:"600s",1200:"1200s"], defaultValue: 600         
        input name: "energyReportEnable", type: "bool", title: "Enable Energy Reporting", defaultValue: false          
        input name: "energyReportChange", type: "enum", title: "Energy Change (kWh)", options: [0.1:"0.1kWh",0.2:"0.2kWh",0.3:"0.3kWh",0.4:"0.4kWh",0.5:"0.5kWh",0.6:"0.6kWh",0.7:"0.7kWh",0.8:"0.8kWh",0.9:"0.9kWh",1:"1kWh"], defaultValue: 0.5       
		input name: "energyReportTime", type: "enum", title: "Energy Time (s)", options: [10:"10s",20:"20s",30:"30s",40:"40s",50:"50s",60:"60s",90:"90s",120:"120s",240:"240s",300:"300s",600:"600s",1200:"1200s"], defaultValue: 600         
        input name: "logEnable", type: "bool", title: "Enable Debug Logging", defaultValue: true
        input name: "txtEnable", type: "bool", title: "Enable descriptionText Logging", defaultValue: true
	}
}

def installed() {
	device.updateSetting("logEnable", [value: "true", type: "bool"])
    device.updateSetting("txtEnable", [value: "true", type: "bool"])
    logDebug("installed called")
    device.updateSetting("levelTransitionTime", [value: "1000", type: "enum"])
    device.updateSetting("levelChangeTime", [value: "30", type: "enum"])
    device.updateSetting("powerReportEnable", [value: "false", type: "bool"])
    device.updateSetting("energyReportEnable", [value: "false", type: "bool"])
	runIn(1800,logsOff)
}

def updated() {
	logDebug("updated called")
	log.warn("debug logging is: ${logEnable == true}")
	log.warn("descriptionText logging is: ${txtEnable == true}")
    log.warn("levelTransition time is: ${levelTransitionTime}ms")
    log.warn("level change time is: ${levelChangeTime}s")
    log.warn("power reporting is: ${powerReportEnable == true}")
    if (powerReportEnable) {
        log.warn("power change is: ${powerReportChange}W") 
        log.warn("power time is: ${powerReportTime}s") 
        log.warn("voltage change is: ${voltageReportChange}V") 
        log.warn("voltage time is: ${voltageReportTime}s") 
        log.warn("current change is: ${currentReportChange}A") 
        log.warn("current time is: ${currentReportTime}s") 
    }
    log.warn("energy reporting is: ${energyReportEnable == true}")      
    if (energyReportEnable) {        
        log.warn("energy change is: ${energyReportChange}kWh") 
        log.warn("energy time is: ${energyReportTime}s") 
    }
    state.clear()
	unschedule()
	if (logEnable) runIn(1800,logsOff)
}

def configure() {
	logDebug("configure called")
    def cmds = zigbee.onOffConfig() + zigbee.levelConfig()
    if (powerReportEnable) {
        logDebug("power change is: ${powerReportChange}W") //10 = 1W
        logDebug("power time is: ${powerReportTime}s")       
        def powerchange = (powerReportChange.toBigDecimal() / 1 * 10).toInteger()
        logDebug("powerchange: ${powerchange}") 
        if (powerchange == 0) {
            logDebug("powerchange is ZERO, protecting against report flooding!") 
            powerchange = 1000
        }
        def powertime = powerReportTime.toInteger()        
        cmds += zigbee.readAttribute(0x0B04, 0x0604) + zigbee.readAttribute(0x0B04, 0x0605) + zigbee.configureReporting(0x0B04, 0x050B, DataType.INT16, 5, powertime, powerchange)    
        logDebug("voltage change is: ${voltageReportChange}V") //10 = 1V
        logDebug("voltage time is: ${voltageReportTime}s")       
        def voltagechange = (voltageReportChange.toBigDecimal() / 1 * 10).toInteger()
        logDebug("voltagechange: ${voltagechange}") 
        if (voltagechange == 0) {
            logDebug("voltagechange is ZERO, protecting against report flooding!") 
            voltagechange = 1000
        }
        def voltagetime = voltageReportTime.toInteger()
        cmds += zigbee.readAttribute(0x0B04, 0x0600) + zigbee.readAttribute(0x0B04, 0x0601) + zigbee.configureReporting(0x0B04, 0x0505, DataType.UINT16, 5, voltagetime, voltagechange)
        logDebug("current change is: ${currentReportChange}A") //1000 = 1A
        logDebug("current time is: ${currentReportTime}s")       
        def currentchange = (currentReportChange.toBigDecimal() / 1 * 1000).toInteger()
        logDebug("currentchange: ${currentchange}") 
        if (currentchange == 0) {
            logDebug("currentchange is ZERO, protecting against report flooding!") 
            currentchange = 1000
        }
        def currenttime = currentReportTime.toInteger()
        cmds += zigbee.readAttribute(0x0B04, 0x0602) + zigbee.readAttribute(0x0B04, 0x0603) + zigbee.configureReporting(0x0B04, 0x0508, DataType.UINT16, 5, currenttime, currentchange) 
    } 
    else {
        cmds += zigbee.configureReporting(0x0B04, 0x050B, DataType.INT16, 0, 0xFFFF, 0) + zigbee.configureReporting(0x0B04, 0x0505, DataType.UINT16, 0, 0xFFFF, 0) + zigbee.configureReporting(0x0B04, 0x0508, DataType.UINT16, 0, 0xFFFF, 0)
    }
    if (energyReportEnable) {
        logDebug("energy change is: ${energyReportChange}kWh") //3600000 = 1kWh
        logDebug("energy time is: ${energyReportTime}s") 
        def energychange = (energyReportChange.toBigDecimal() / 1 * 3600000).toInteger()    
        logDebug("energychange: ${energychange}") 
        if (energychange == 0) {
            logDebug("energychange is ZERO, protecting against report flooding!") 
            energychange = 1000
        }
        def energytime = energyReportTime.toInteger()
        cmds += zigbee.readAttribute(0x0702, 0x0300) + zigbee.readAttribute(0x0702, 0x0301) + zigbee.readAttribute(0x0702, 0x0302) + zigbee.readAttribute(0x0702, 0x0303) + zigbee.readAttribute(0x0702, 0x0304) + zigbee.configureReporting(0x0702, 0x0000, DataType.UINT48, 5, energytime, energychange)        
    }
    else {
        cmds += zigbee.configureReporting(0x0702, 0x0000, DataType.UINT48, 0, 0xFFFF, 0)
    }
	logDebug("sending ${cmds}")
	return cmds
}

def refresh() {
	logDebug("refresh called")
	def cmds = zigbee.onOffRefresh() + zigbee.levelRefresh()
    if (powerReportEnable) {
        cmds += zigbee.readAttribute(0x0B04, 0x050b) + zigbee.readAttribute(0x0B04, 0x0505) + zigbee.readAttribute(0x0B04, 0x0508)
    } 
    if (energyReportEnable) {
        cmds += zigbee.readAttribute(0x0702, 0x0000) 
    }
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

def setLevel(value) {    
    logDebug("setLevel called value: ${value}")
    logDebug("levelTransitionTime: ${levelTransitionTime}")
    setLevel(value, levelTransitionTime.toBigDecimal() / 1000) 
}

def setLevel(value, rate) {
    logDebug("setLevel called value: ${value} rate: ${rate}")
    rate = rate.toBigDecimal()
    def scaledRate = (rate * 10).toInteger()
    logDebug("scaledRate: ${scaledRate}")    
    def scaledValue = (value.toInteger() * 2.55).toInteger()
    logDebug("scaledValue: ${scaledValue}")
    def cmds = ["he cmd 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0008 4 {0x${intTo8bitUnsignedHex(scaledValue)} 0x${intTo16bitUnsignedHex(scaledRate)}}"]
	logDebug("sending ${cmds}")
    state["action"] = "digitalsetlevel"
	return cmds
}

def startLevelChange(direction) {
    logDebug("startLevelChange called direction: ${direction}")
    def upDown = direction == "down" ? 1 : 0    
    logDebug("upDown: ${upDown} levelChangeTime: ${levelChangeTime}")
    def scaledRate = (100 / levelChangeTime.toBigDecimal()).toInteger()
    logDebug("scaledRate: ${scaledRate}")
    def currentValue = device.currentValue("switch") ?: "unknown"
    def cmds = []
    if (currentValue != "on") {
        cmds += ["he cmd 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0006 1 {}", "delay 1000"]
    }
    cmds += ["he cmd 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0008 1 {0x${intTo8bitUnsignedHex(upDown)} 0x${intTo16bitUnsignedHex(scaledRate)}}"]
    logDebug("sending ${cmds}")
    state["action"] = "digitalsetlevel"
	return cmds
}

def stopLevelChange() {
    logDebug("stopLevelChange called")
    def cmds = ["he cmd 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0008 3 {}"]
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
        else if (descriptionMap.cluster == "0008" || descriptionMap.clusterId == "0008" || descriptionMap.clusterInt == 8) {
            if (descriptionMap.command == "0A" || descriptionMap.command == "01") {   
                if (descriptionMap.attrId == "0000" || descriptionMap.attrInt == 0) {
                    logDebug("level control (0008) current level report")
                    def levelValue = zigbee.convertHexToInt(descriptionMap.value)
                    logDebug("current level report is ${levelValue}")	
                    levelValue = levelValue <= 254 ? levelValue >= 0 ? levelValue : 0 : 254                
                    levelValue = Math.round(levelValue / 2.55)
                    def descriptionText = "${device.displayName} was set to ${levelValue}%"
                    def currentValue =  device.currentValue("level") ?: "unknown"
                    if (levelValue == currentValue) {
                        descriptionText = "${device.displayName} is ${levelValue}%"
                    }  
                    def type = "physical"
                    def action = state["action"] ?: "standby"
                    if (action == "digitalsetlevel") {
                        logDebug("action is ${action}")
                        type = "digital"
                        state["action"] = "standby"
                        logDebug("action set to standby")
                    }
                    logText(descriptionText)
                    events.add(getEvent([name: "level", value: levelValue, unit: "%", type: type, descriptionText: descriptionText]))
                }
                else {
                    logDebug("level control (0008) attribute skipped")
                }
            }
            else {
                logDebug("level control (0008) command skipped")
            }
        }
        else if (descriptionMap.cluster == "0702" || descriptionMap.clusterId == "0702" || descriptionMap.clusterInt == 1794) {
            if (descriptionMap.command == "0A" || descriptionMap.command == "01") {                
                if (descriptionMap.attrId == "0000" || descriptionMap.attrInt == 0) {            
                    logDebug("simple metering (0702) current summation delivered report")
                    def energyValue = zigbee.convertHexToInt(descriptionMap.value)
                    logDebug("energy report is ${energyValue}") //3600000 = 1kWh
                    energyValue = energyValue / 3600000 
                    def descriptionText = "${device.displayName} is ${energyValue} kWh"
                    logText(descriptionText)
                    events.add(getEvent([name: "energy", value: energyValue, unit: "kWh", descriptionText: descriptionText])) 
                }        
                else {
                    logDebug("simple metering (0702) attribute skipped")
                }
            }
            else {
                logDebug("simple metering (0702) command skipped")
            }
        }
        else if (descriptionMap.cluster == "0B04" || descriptionMap.clusterId == "0B04" || descriptionMap.clusterInt == 2820) {
            if (descriptionMap.command == "0A" || descriptionMap.command == "01") { 
                if (descriptionMap.attrId == "050B" || descriptionMap.attrInt == 1291) {
                    logDebug("electrical measurement (0B04) power report")                     
                    def powerValue = zigbee.convertHexToInt(descriptionMap.value)
                    logDebug("power report is ${powerValue}") //10 = 1W
                    powerValue = powerValue / 10
                    def descriptionText = "${device.displayName} is ${powerValue} W"
                    logText(descriptionText)
                    events.add(getEvent([name: "power", value: powerValue, unit: "W", descriptionText: descriptionText]))
                }
                else if (descriptionMap.attrId == "0505" || descriptionMap.attrInt == 1285) {
                    logDebug("electrical measurement (0B04) voltage report")                     
                    def voltageValue = zigbee.convertHexToInt(descriptionMap.value)
                    logDebug("voltage report is ${voltageValue}") //10 = 1V
                    voltageValue = voltageValue / 10 
                    def descriptionText = "${device.displayName} is ${voltageValue} V"
                    logText(descriptionText)
                    events.add(getEvent([name: "voltage", value: voltageValue, unit: "V", descriptionText: descriptionText]))
                }
                else if (descriptionMap.attrId == "0508" || descriptionMap.attrInt == 1288) {
                    logDebug("electrical measurement (0B04) current report")                     
                    def currentValue = zigbee.convertHexToInt(descriptionMap.value)
                    logDebug("current report is ${currentValue}") //1000 = 1A
                    currentValue = currentValue / 1000 
                    def descriptionText = "${device.displayName} is ${currentValue} A"
                    logText(descriptionText)
                    events.add(getEvent([name: "current", value: currentValue, unit: "A", descriptionText: descriptionText]))
                }     
                else {
                    logDebug("electrical measurement (0B04) attribute skipped")
                } 
            }
            else {
                logDebug("electrical measurement (0B04) command skipped")
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

def intTo16bitUnsignedHex(value) {
    def hexStr = zigbee.convertToHexString(value.toInteger(),4)
    return new String(hexStr.substring(2, 4) + hexStr.substring(0, 2))
}

def intTo8bitUnsignedHex(value) {
    return zigbee.convertToHexString(value.toInteger(), 2)
}