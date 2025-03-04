/**
 *    Vesternet VES-ZB-SWI-015 2 Channel Switch
 *
 */
metadata {
    definition (name: 'Vesternet VES-ZB-SWI-015 2 Channel Switch', namespace: 'Vesternet', author: 'Vesternet', importUrl: 'https://raw.githubusercontent.com/vesternet/hubitat-zigbee-device-drivers/main/Vesternet%20VES-ZB-SWI-015%202%20Channel%20Switch.groovy', singleThreaded: true) {
        capability 'Switch'
        capability 'Actuator'
        capability 'PowerMeter'
        capability 'EnergyMeter'
        capability 'VoltageMeasurement'
        capability 'CurrentMeter'
        capability 'Sensor'
        capability 'Configuration'
        capability 'Refresh'

        command 'addGroup', [[name:'Group ID*', type: 'NUMBER', description: 'Enter the Group ID (1-65527) that you wish to add to this device.'], [name:'Source Endpoint ID*', type: 'ENUM', constraints: ['01', '02'], description: 'Enter the Source Endpoint ID from this device that you wish to add the Group to.']]
        command 'removeGroup', [[name:'Group ID*', type: 'NUMBER', description: 'Enter the Group ID (1-65527) that you wish to remove from this device.'], [name:'Source Endpoint ID*', type: 'ENUM', constraints: ['01', '02'], description: 'Enter the Source Endpoint ID from this device that you wish to remove the Group from.']]
        command 'readGroups', [[name:'Source Endpoint ID*', type: 'ENUM', constraints: ['01', '02'], description: 'Enter the Source Endpoint ID from this device that you wish to read Groups from.']]
        command 'clearGroups', [[name:'Source Endpoint ID*', type: 'ENUM', constraints: ['01', '02'], description: 'Enter the Source Endpoint ID from this device that you wish to clear Groups from.']]

        command 'storeScene', [[name:'Group ID*', type: 'NUMBER', description: 'Enter the Group ID (0-65527) that you wish to store a Scene in on this device.'], [name:'Scene ID*', type: 'NUMBER', description: 'Enter the Scene ID (1-255) that you wish to add to the Group on this device.  The current state of the device will be stored to this Scene ID.'], [name:'Source Endpoint ID*', type: 'ENUM', constraints: ['01', '02'], description: 'Enter the Source Endpoint ID from this device that you wish to store the Scene in.']]
        command 'removeScene', [[name:'Group ID*', type: 'NUMBER', description: 'Enter the Group ID (0-65527) that you wish to remove a Scene from on this device.'], [name:'Scene ID*', type: 'NUMBER', description: 'Enter the Scene ID (1-255) that you wish to remove from the Group on this device.'], [name:'Source Endpoint ID*', type: 'ENUM', constraints: ['01', '02'], description: 'Enter the Source Endpoint ID from this device that you wish to remove the Scene from.']]
        command 'recallScene', [[name:'Group ID*', type: 'NUMBER', description: 'Enter the Group ID (0-65527) that you wish to recall a Scene from on this device.'], [name:'Scene ID*', type: 'NUMBER', description: 'Enter the Scene ID (1-255) that you wish to recall from the Group on this device.'], [name:'Source Endpoint ID*', type: 'ENUM', constraints: ['01', '02'], description: 'Enter the Source Endpoint ID from this device that you wish to recall the Scene from.']]
        command 'readScenes', [[name:'Group ID*', type: 'NUMBER', description: 'Enter the Group ID (0-65527) that you wish to read Scenes from on this device.'], [name:'Source Endpoint ID*', type: 'ENUM', constraints: ['01', '02'], description: 'Enter the Source Endpoint ID from this device that you wish to read Scenes from.']]
        command 'clearScenes', [[name:'Group ID*', type: 'NUMBER', description: 'Enter the Group ID (0-65527) that you wish to clear Scenes from on this device.'], [name:'Source Endpoint ID*', type: 'ENUM', constraints: ['01', '02'], description: 'Enter the Source Endpoint ID from this device that you wish to clear Scenes from.']]

        fingerprint profileId: '0104', endpointId:'01', inClusters: '0000,0003,0004,0005,0006', manufacturer: 'Sunricher', model: 'ON/OFF(2CH)', deviceJoinName: 'Vesternet VES-ZB-SWI-015 2 Channel Switch'
    }
    preferences {
        input name: 'sceneTransitionTime', type: 'enum', title: 'Scene Transition Time (ms)', options: [0:'0s', 500:'0.5s', 1000:'1s', 1500:'1.5s', 2000:'2s', 3000:'3s', 4000:'4s', 5000:'5s'], defaultValue: 1000
        input name: 'powerReportEnable', type: 'bool', title: 'Enable Power, Voltage & Current Reporting', defaultValue: false
        input name: 'powerReportChange', type: 'enum', title: 'Power Change (W)', options: [1:'1W', 2:'2W', 3:'3W', 4:'4W', 5:'5W', 6:'6W', 7:'7W', 8:'8W', 9:'9W', 10:'10W', 15:'15W', 20:'20W'], defaultValue: 5
        input name: 'powerReportTime', type: 'enum', title: 'Power Time (s)', options: [10:'10s', 20:'20s', 30:'30s', 40:'40s', 50:'50s', 60:'60s', 90:'90s', 120:'120s', 240:'240s', 300:'300s', 600:'600s', 1200:'1200s'], defaultValue: 600
        input name: 'voltageReportChange', type: 'enum', title: 'Voltage Change (V)', options: [1:'1V', 2:'2V', 3:'3V', 4:'4V', 5:'5V', 6:'6V', 7:'7V', 8:'8V', 9:'9V', 10:'10V', 15:'15V', 20:'20V'], defaultValue: 5
        input name: 'voltageReportTime', type: 'enum', title: 'Voltage Time (s)', options: [10:'10s', 20:'20s', 30:'30s', 40:'40s', 50:'50s', 60:'60s', 90:'90s', 120:'120s', 240:'240s', 300:'300s', 600:'600s', 1200:'1200s'], defaultValue: 600
        input name: 'currentReportChange', type: 'enum', title: 'Current Change (A)', options: [0.1:'0.1A', 0.2:'0.2A', 0.3:'0.3A', 0.4:'0.4A', 0.5:'0.5A', 0.6:'0.6A', 0.7:'0.7A', 0.8:'0.8A', 0.9:'0.9A', 1:'1A', 1.5:'1.5A', 2:'2A'], defaultValue: 0.5
        input name: 'currentReportTime', type: 'enum', title: 'Current Time (s)', options: [10:'10s', 20:'20s', 30:'30s', 40:'40s', 50:'50s', 60:'60s', 90:'90s', 120:'120s', 240:'240s', 300:'300s', 600:'600s', 1200:'1200s'], defaultValue: 600
        input name: 'energyReportEnable', type: 'bool', title: 'Enable Energy Reporting', defaultValue: false
        input name: 'energyReportChange', type: 'enum', title: 'Energy Change (kWh)', options: [0.1:'0.1kWh', 0.2:'0.2kWh', 0.3:'0.3kWh', 0.4:'0.4kWh', 0.5:'0.5kWh', 0.6:'0.6kWh', 0.7:'0.7kWh', 0.8:'0.8kWh', 0.9:'0.9kWh', 1:'1kWh'], defaultValue: 0.5
        input name: 'energyReportTime', type: 'enum', title: 'Energy Time (s)', options: [10:'10s', 20:'20s', 30:'30s', 40:'40s', 50:'50s', 60:'60s', 90:'90s', 120:'120s', 240:'240s', 300:'300s', 600:'600s', 1200:'1200s'], defaultValue: 600
        input name: 'logEnable', type: 'bool', title: 'Enable Debug Logging', defaultValue: true
        input name: 'txtEnable', type: 'bool', title: 'Enable descriptionText Logging', defaultValue: true
    }
}

def getParentEndpointID() {
    return 11 //change to 1 for older firmware versions
}

def installed() {
    device.updateSetting('logEnable', [value: 'true', type: 'bool'])
    device.updateSetting('txtEnable', [value: 'true', type: 'bool'])
    logDebug('installed called')
    device.updateSetting('sceneTransitionTime', [value: '1000', type: 'enum'])
    device.updateSetting('powerReportEnable', [value: 'false', type: 'bool'])
    device.updateSetting('energyReportEnable', [value: 'false', type: 'bool'])
    runIn(1800, logsOff)
}

def updated() {
    logDebug('updated called')
    log.warn("debug logging is: ${logEnable == true}")
    log.warn("descriptionText logging is: ${txtEnable == true}")
    log.warn("sceneTransition time is: ${sceneTransitionTime}ms")
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
    if (logEnable) runIn(1800, logsOff)
}

def configure() {
    logDebug('configure called')
    def cmds = zigbee.onOffConfig('1', 0, 3600) + zigbee.onOffConfig('2', 0, 3600)
    if (powerReportEnable) {
        logDebug("power change is: ${powerReportChange}W") //10 = 1W
        logDebug("power time is: ${powerReportTime}s")
        def powerchange = (powerReportChange.toBigDecimal() / 1 * 10).toInteger()
        logDebug("powerchange: ${powerchange}")
        if (powerchange == 0) {
            logDebug('powerchange is ZERO, protecting against report flooding!')
            powerchange = 1000
        }
        def powertime = powerReportTime.toInteger()        
        cmds += zigbee.readAttribute(0x0B04, 0x0604, ['destEndpoint': parentEndpointID]) + zigbee.readAttribute(0x0B04, 0x0605, ['destEndpoint': parentEndpointID]) + zigbee.configureReporting(0x0B04, 0x050B, DataType.INT16, 5, powertime, powerchange, ['destEndpoint': parentEndpointID])
        logDebug("voltage change is: ${voltageReportChange}V") //10 = 1V
        logDebug("voltage time is: ${voltageReportTime}s")
        def voltagechange = (voltageReportChange.toBigDecimal() / 1 * 10).toInteger()
        logDebug("voltagechange: ${voltagechange}")
        if (voltagechange == 0) {
            logDebug('voltagechange is ZERO, protecting against report flooding!')
            voltagechange = 1000
        }
        def voltagetime = voltageReportTime.toInteger()
        cmds += zigbee.readAttribute(0x0B04, 0x0600, ['destEndpoint': parentEndpointID]) + zigbee.readAttribute(0x0B04, 0x0601, ['destEndpoint': parentEndpointID]) + zigbee.configureReporting(0x0B04, 0x0505, DataType.UINT16, 5, voltagetime, voltagechange, ['destEndpoint': parentEndpointID])
        logDebug("current change is: ${currentReportChange}A") //1000 = 1A
        logDebug("current time is: ${currentReportTime}s")
        def currentchange = (currentReportChange.toBigDecimal() / 1 * 1000).toInteger()
        logDebug("currentchange: ${currentchange}")
        if (currentchange == 0) {
            logDebug('currentchange is ZERO, protecting against report flooding!')
            currentchange = 1000
        }
        def currenttime = currentReportTime.toInteger()
        cmds += zigbee.readAttribute(0x0B04, 0x0602, ['destEndpoint': parentEndpointID]) + zigbee.readAttribute(0x0B04, 0x0603, ['destEndpoint': parentEndpointID]) + zigbee.configureReporting(0x0B04, 0x0508, DataType.UINT16, 5, currenttime, currentchange, ['destEndpoint': parentEndpointID])
    }
    else {
        cmds += zigbee.configureReporting(0x0B04, 0x050B, DataType.INT16, 0, 0xFFFF, 1, ['destEndpoint': parentEndpointID]) + zigbee.configureReporting(0x0B04, 0x0505, DataType.UINT16, 0, 0xFFFF, 1, ['destEndpoint': parentEndpointID]) + zigbee.configureReporting(0x0B04, 0x0508, DataType.UINT16, 0, 0xFFFF, 1, ['destEndpoint': parentEndpointID])
    }
    if (energyReportEnable) {
        logDebug("energy change is: ${energyReportChange}kWh") //3600000 = 1kWh
        logDebug("energy time is: ${energyReportTime}s")
        def energychange = (energyReportChange.toBigDecimal() / 1 * 3600000).toInteger()
        logDebug("energychange: ${energychange}")
        if (energychange == 0) {
            logDebug('energychange is ZERO, protecting against report flooding!')
            energychange = 1000
        }
        def energytime = energyReportTime.toInteger()
        cmds += zigbee.readAttribute(0x0702, 0x0300, ['destEndpoint': parentEndpointID]) + zigbee.readAttribute(0x0702, 0x0301, ['destEndpoint': parentEndpointID]) + zigbee.readAttribute(0x0702, 0x0302, ['destEndpoint': parentEndpointID]) + zigbee.readAttribute(0x0702, 0x0303, ['destEndpoint': parentEndpointID]) + zigbee.readAttribute(0x0702, 0x0304, ['destEndpoint': parentEndpointID]) + zigbee.configureReporting(0x0702, 0x0000, DataType.UINT48, 5, energytime, energychange, ['destEndpoint': parentEndpointID])
    }
    else {
        cmds += zigbee.configureReporting(0x0702, 0x0000, DataType.UINT48, 0, 0xFFFF, 1, ['destEndpoint': parentEndpointID])
    }
    cmds += refresh()
    logDebug("sending ${cmds}")
    return cmds
}

def refresh() {
    logDebug('refresh called')
    def cmds = zigbee.readAttribute(0x0006, 0x0000, ['destEndpoint': 0x01])
    cmds += zigbee.readAttribute(0x0006, 0x0000, ['destEndpoint': 0x02])
    if (powerReportEnable) {
        cmds += zigbee.readAttribute(0x0B04, 0x050b, ['destEndpoint': parentEndpointID]) + zigbee.readAttribute(0x0B04, 0x0505, ['destEndpoint': parentEndpointID]) + zigbee.readAttribute(0x0B04, 0x0508, ['destEndpoint': parentEndpointID])
    }
    if (energyReportEnable) {
        cmds += zigbee.readAttribute(0x0702, 0x0000, ['destEndpoint': parentEndpointID])
    }
    logDebug("sending ${cmds}")
    return cmds
}

def readGroups(endpoint = null) {
    logDebug("readGroups called endpoint: ${endpoint}")
    def deviceEndpoint = endpoint
    if (deviceEndpoint == null) {
        deviceEndpoint = device.endpointId
    }
    def cmds = [ "he cmd 0x${device.deviceNetworkId} 0x${deviceEndpoint} 0x0004 0x02 {0}" ]
    logDebug("sending ${cmds}")
    sendHubCommand(new hubitat.device.HubMultiAction(cmds, hubitat.device.Protocol.ZIGBEE))
}

def clearGroups(endpoint = null) {
    logDebug("clearGroups called endpoint: ${endpoint}")
    def deviceEndpoint = endpoint
    if (deviceEndpoint == null) {
        deviceEndpoint = device.endpointId
    }
    def cmds = [ "he cmd 0x${device.deviceNetworkId} 0x${deviceEndpoint} 0x0004 0x04 {}" ]
    logDebug("sending ${cmds}")
    sendHubCommand(new hubitat.device.HubMultiAction(cmds, hubitat.device.Protocol.ZIGBEE))
    readGroups(endpoint)
}

def addGroup(group, endpoint = null) {
    logDebug("addGroup called endpoint: ${endpoint} group: ${group}")
    manageGroup('add', endpoint, group)
}

def removeGroup(group, endpoint = null) {
    logDebug("removeGroup called endpoint: ${endpoint} group: ${group}")
    manageGroup('remove', endpoint, group)
}

def manageGroup(action, endpoint = null, group) {
    logDebug("manageGroup called action: ${action} endpoint: ${endpoint} group: ${group}")
    def deviceEndpoint = endpoint
    if (deviceEndpoint == null) {
        deviceEndpoint = device.endpointId
    }
    def groupID = group ? group.toInteger() : 0
    if (groupID > 65527 || groupID < 1) {
        logDebug('groupID out of bounds, resetting')
        groupID = groupID > 65527 ? 65527 : groupID < 1 ? 1 : groupID
    }
    logDebug("groupID: ${groupID}")
    def cmds = []
    if (action == 'add') {
        cmds += [ "he cmd 0x${device.deviceNetworkId} 0x${deviceEndpoint} 0x0004 0x0 {${intTo16bitUnsignedHex(groupID)} 0}" ]
    }
    else if (action == 'remove') {
        cmds += [ "he cmd 0x${device.deviceNetworkId} 0x${deviceEndpoint} 0x0004 0x3 {${intTo16bitUnsignedHex(groupID)}}" ]
    }
    logDebug("sending ${cmds}")
    sendHubCommand(new hubitat.device.HubMultiAction(cmds, hubitat.device.Protocol.ZIGBEE))
    readGroups(endpoint)
}

def storeGroups(endpoint, group, reset = false) {
    logDebug("storeGroups called endpoint: ${endpoint} group: ${group} reset: ${reset}")
    if (state['Groups'] == null) {
        state['Groups'] = [:]
    }
    if (reset) {
        resetGroups(endpoint)
    }
    state['Groups']["endpoint ${endpoint}"].add("group ${group}")
    logDebug("new groups: ${state['Groups']}")
}

def resetGroups(endpoint) {
    logDebug("resetGroups called endpoint: ${endpoint}")
    logDebug("current groups: ${state['Groups']}")
    state['Groups']["endpoint ${endpoint}"] = []
    logDebug("reset groups: ${state['Groups']}")
}

def readScenes(group, endpoint = null) {
    logDebug("readScenes called endpoint: ${endpoint} group: ${group}")
    def deviceEndpoint = endpoint
    if (deviceEndpoint == null) {
        deviceEndpoint = device.endpointId
    }
    def groupID = group ? group.toInteger() : 0
    if (groupID > 65527 || groupID < 0) {
        logDebug('groupID out of bounds, resetting')
        groupID = groupID > 65527 ? 65527 : groupID < 0 ? 0 : groupID
    }
    logDebug("groupID: ${groupID}")
    def cmds = [ "he cmd 0x${device.deviceNetworkId} 0x${deviceEndpoint} 0x0005 0x06 {${intTo16bitUnsignedHex(groupID)}}" ]
    logDebug("sending ${cmds}")
    sendHubCommand(new hubitat.device.HubMultiAction(cmds, hubitat.device.Protocol.ZIGBEE))
}

def clearScenes(group, endpoint = null) {
    logDebug("clearScenes called endpoint: ${endpoint} group: ${group}")
    def deviceEndpoint = endpoint
    if (deviceEndpoint == null) {
        deviceEndpoint = device.endpointId
    }
    def groupID = group ? group.toInteger() : 0
    if (groupID > 65527 || groupID < 0) {
        logDebug('groupID out of bounds, resetting')
        groupID = groupID > 65527 ? 65527 : groupID < 0 ? 1 : groupID
    }
    logDebug("groupID: ${groupID}")
    def cmds = [ "he cmd 0x${device.deviceNetworkId} 0x${deviceEndpoint} 0x0005 0x03 {${intTo16bitUnsignedHex(groupID)}}" ]
    logDebug("sending ${cmds}")
    sendHubCommand(new hubitat.device.HubMultiAction(cmds, hubitat.device.Protocol.ZIGBEE))
    readScenes(group, endpoint)
}

def storeScene(group, scene, endpoint = null) {
    logDebug("storeScene called endpoint: ${endpoint} group: ${group} scene: ${scene}")
    manageScene('store', endpoint, group, scene)
}

def removeScene(group, scene, endpoint = null) {
    logDebug("removeScene called endpoint: ${endpoint} group: ${group} scene: ${scene}")
    manageScene('remove', endpoint, group, scene)
}

def recallScene(group, scene, endpoint = null) {
    logDebug("recallScene called endpoint: ${endpoint} group: ${group} scene: ${scene}")
    manageScene('recall', endpoint, group, scene)
}

def manageScene(action, endpoint = null, group, scene) {
    logDebug("manageScene called action: ${action} endpoint: ${endpoint} group: ${group} scene: ${scene}")
    def deviceEndpoint = endpoint
    if (deviceEndpoint == null) {
        deviceEndpoint = device.endpointId
    }
    def groupID = group ? group.toInteger() : 0
    if (groupID > 65527 || groupID < 0) {
        logDebug('groupID out of bounds, resetting')
        groupID = groupID > 65527 ? 65527 : groupID < 0 ? 0 : groupID
    }
    logDebug("groupID: ${groupID}")
    def sceneID = scene ? scene.toInteger() : 0
    if (sceneID > 255 || sceneID < 1) {
        logDebug('sceneID out of bounds, resetting')
        sceneID = sceneID > 255 ? 255 : sceneID < 1 ? 1 : sceneID
    }
    logDebug("sceneID: ${sceneID}")
    def cmds = []
    if (action == 'store') {
        cmds += [ "he cmd 0x${device.deviceNetworkId} 0x${deviceEndpoint} 0x0005 0x4 {${intTo16bitUnsignedHex(groupID)} ${intTo8bitUnsignedHex(sceneID)}}" ]
    }
    else if (action == 'remove') {
        cmds += [ "he cmd 0x${device.deviceNetworkId} 0x${deviceEndpoint} 0x0005 0x2 {${intTo16bitUnsignedHex(groupID)} ${intTo8bitUnsignedHex(sceneID)}}" ]
    }
    else if (action == 'recall') {
        logDebug("sceneTransitionTime: ${sceneTransitionTime ?: 1000}")
        def transitionTime = sceneTransitionTime ? sceneTransitionTime.toBigDecimal() : 1000
        transitionTime = (transitionTime / 1000 * 10).toInteger()
        logDebug("transitionTime: ${transitionTime}")
        cmds += [ "he cmd 0x${device.deviceNetworkId} 0x${deviceEndpoint} 0x0005 0x5 {${intTo16bitUnsignedHex(groupID)} ${intTo8bitUnsignedHex(sceneID)} ${intTo16bitUnsignedHex(transitionTime)}}" ]
    }
    logDebug("sending ${cmds}")
    sendHubCommand(new hubitat.device.HubMultiAction(cmds, hubitat.device.Protocol.ZIGBEE))
    if (action != 'recall') {
        readScenes(group, endpoint)
    }
}

def storeScenes(endpoint, group, scene, reset = false) {
    logDebug("storeScene called endpoint: ${endpoint} group: ${group} scene: ${scene} reset: ${reset}")
    if (state['Scenes'] == null) {
        state['Scenes'] = [:]
    }
    if (reset) {
        resetScenes(endpoint, group)
    }
    if (state['Scenes']["endpoint ${endpoint}"]["group ${group}"] == null) {
        state['Scenes']["endpoint ${endpoint}"]["group ${group}"] = []
    }
    state['Scenes']["endpoint ${endpoint}"]["group ${group}"].add("scene ${scene}")
    logDebug("new scenes: ${state['Scenes']}")
}

def resetScenes(endpoint, group) {
    logDebug("resetScenes called endpoint: ${endpoint} group: ${group}")
    if (state['Scenes'] == null) {
        state['Scenes'] = [:]
    }
    logDebug("current scenes: ${state['Scenes']}")
    if (state['Scenes']["endpoint ${endpoint}"] == null) {
        state['Scenes']["endpoint ${endpoint}"] = [:]
    }
    logDebug("current scenes: ${state['Scenes']["endpoint ${endpoint}"]}")
    if (state['Scenes']["endpoint ${endpoint}"].containsKey("group ${group}".toString())) {
        state['Scenes']["endpoint ${endpoint}"].remove("group ${group}".toString())
    }
    logDebug("new scenes: ${state['Scenes']}")
}

def on() {
    logDebug('on called')
    def cmds =  ["he cmd 0x${device.deviceNetworkId} 0x01 0x0006 0x01 {}", 'delay 200', "he cmd 0x${device.deviceNetworkId} 0x02 0x0006 0x01 {}"]
    logDebug("sending ${cmds}")
    state['action-EP01'] = 'digitalon'
    state['action-EP02'] = 'digitalon'
    return cmds
}

def off() {
    logDebug('off called')
    def cmds =  ["he cmd 0x${device.deviceNetworkId} 0x01 0x0006 0x00 {}", 'delay 200', "he cmd 0x${device.deviceNetworkId} 0x02 0x0006 0x00 {}"]
    logDebug("sending ${cmds}")
    state['action-EP01'] = 'digitaloff'
    state['action-EP02'] = 'digitaloff'
    return cmds
}

def componentOn(childDevice) {
    logDebug('componentOn called')
    logDebug("got childDevice: ${childDevice.displayName}")
    def endpoint = childDevice.deviceNetworkId.split('-EP')[1]
    def cmds = ["he cmd 0x${device.deviceNetworkId} 0x${endpoint} 0x0006 0x01 {}"]
    logDebug("sending ${cmds}")
    state["action-EP${endpoint}"] = 'digitalon'
    sendHubCommand(new hubitat.device.HubMultiAction(cmds, hubitat.device.Protocol.ZIGBEE))
}

def componentOff(childDevice) {
    logDebug('componentOff called')
    logDebug("got childDevice: ${childDevice.displayName}")
    def endpoint = childDevice.deviceNetworkId.split('-EP')[1]
    def cmds = ["he cmd 0x${device.deviceNetworkId} 0x${endpoint} 0x0006 0x00 {}"]
    logDebug("sending ${cmds}")
    state["action-EP${endpoint}"] = 'digitaloff'
    sendHubCommand(new hubitat.device.HubMultiAction(cmds, hubitat.device.Protocol.ZIGBEE))
}

def componentConfigure(childDevice) {
    logDebug('componentConfigure called')
    logDebug("got childDevice: ${childDevice.displayName}")
    def endpoint = hubitat.helper.HexUtils.hexStringToInt(childDevice.deviceNetworkId.split('-EP')[1])
    def powerFailState = childDevice.getSetting('powerFailState') ?: 'previous'
    logDebug("powerFailState is ${powerFailState}")
    def startUpOnOff = ['on': 0x01, 'off': 0x0, 'opposite': 0x02, 'previous': 0x03]
    logDebug("startUpOnOff: ${startUpOnOff[powerFailState ?: 'previous']}")
    def cmds = zigbee.writeAttribute(0x0006, 0x4003, DataType.ENUM8, startUpOnOff[powerFailState ?: 'previous'], ['destEndpoint': endpoint])
    logDebug("sending ${cmds}")
    sendHubCommand(new hubitat.device.HubMultiAction(cmds, hubitat.device.Protocol.ZIGBEE))
}

def componentRefresh(childDevice) {
    logDebug('componentRefresh called')
    logDebug("got childDevice: ${childDevice.displayName}")
    def endpoint = hubitat.helper.HexUtils.hexStringToInt(childDevice.deviceNetworkId.split('-EP')[1])
    def cmds = zigbee.readAttribute(0x0006, 0x0000, ['destEndpoint': endpoint])
    logDebug("sending ${cmds}")
    sendHubCommand(new hubitat.device.HubMultiAction(cmds, hubitat.device.Protocol.ZIGBEE))
}

void parse(String description) {
    logDebug('parse called')
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
    logDebug('getEvents called')
    logDebug("got descriptionMap: ${descriptionMap}")
    def events = []
    def endpoint = descriptionMap.sourceEndpoint ? zigbee.convertHexToInt(descriptionMap.sourceEndpoint) : descriptionMap.endpoint ? zigbee.convertHexToInt(descriptionMap.endpoint) : device.endpointId.toInteger()
    logDebug("got endpoint: ${endpoint}")
    if (!(descriptionMap.profileId) || (descriptionMap.profileId && descriptionMap.profileId == '0104')) {
        if (descriptionMap.cluster == '0004' || descriptionMap.clusterId == '0004' || descriptionMap.clusterInt == 4) {
            if (descriptionMap.command == '00') {
                logDebug('group (0004) add group response (00)')
                def status = zigbee.convertHexToInt(descriptionMap.data[0])
                logDebug("status: ${status}")
                if (status == 0) {
                    def groupID = zigbee.convertHexToInt(descriptionMap.data[2..1].join())
                    logDebug("groupID: ${groupID}")
                }
                else {
                    logDebug('add group failed!')
                }
            }
            else if (descriptionMap.command == '02') {
                logDebug('group (0004) get group membership response (02)')
                def capacity = zigbee.convertHexToInt(descriptionMap.data[0])
                logDebug("capacity: ${capacity}")
                def count = zigbee.convertHexToInt(descriptionMap.data[1])
                logDebug("count: ${count}")
                if (count == 0) {
                    resetGroups(endpoint)
                }
                else {
                    for (def i = 0; i < count; i++) {
                        logDebug("count ${i + 1}")
                        def start = (i + 1) * 2
                        logDebug("start ${start}")
                        def group = descriptionMap.data[start + 1..start].join()
                        logDebug("group ${group}")
                        def groupID = zigbee.convertHexToInt(group)
                        logDebug("groupID: ${groupID}")
                        storeGroups(endpoint, groupID, i == 0)
                    }
                }
            }
            else if (descriptionMap.command == '03') {
                logDebug('group (0004) remove group response (03)')
                def status = zigbee.convertHexToInt(descriptionMap.data[0])
                logDebug("status: ${status}")
                if (status == 0) {
                    def groupID = zigbee.convertHexToInt(descriptionMap.data[2..1].join())
                    logDebug("groupID: ${groupID}")
                }
                else {
                    logDebug('remove group failed!')
                }
            }
            else {
                logDebug('group (0004) command skipped')
            }
        }
        else if (descriptionMap.cluster == '0005' || descriptionMap.clusterId == '0005' || descriptionMap.clusterInt == 5) {
            if (descriptionMap.command == '02') {
                logDebug('scene (0005) remove scene response (02)')
                def status = zigbee.convertHexToInt(descriptionMap.data[0])
                logDebug("status: ${status}")
                if (status == 0) {
                    def groupID = zigbee.convertHexToInt(descriptionMap.data[2..1].join())
                    logDebug("groupID: ${groupID}")
                    def sceneID = zigbee.convertHexToInt(descriptionMap.data[3])
                    logDebug("sceneID: ${sceneID}")
                }
                else {
                    logDebug('remove scene failed!')
                }
            }
            else if (descriptionMap.command == '03') {
                logDebug('scene (0005) remove all scenes response (03)')
                def status = zigbee.convertHexToInt(descriptionMap.data[0])
                logDebug("status: ${status}")
                if (status == 0) {
                    def groupID = zigbee.convertHexToInt(descriptionMap.data[2..1].join())
                    logDebug("groupID: ${groupID}")
                }
                else {
                    logDebug('remove all scenes failed!')
                }
            }
            else if (descriptionMap.command == '04') {
                logDebug('scene (0005) store scene response (04)')
                def status = zigbee.convertHexToInt(descriptionMap.data[0])
                logDebug("status: ${status}")
                if (status == 0) {
                    def groupID = zigbee.convertHexToInt(descriptionMap.data[2..1].join())
                    logDebug("groupID: ${groupID}")
                    def sceneID = zigbee.convertHexToInt(descriptionMap.data[3])
                    logDebug("sceneID: ${sceneID}")
                }
                else {
                    logDebug('store scene failed!')
                }
            }
            else if (descriptionMap.command == '06') {
                logDebug('scene (0005) get scene membership response (06)')
                def status = zigbee.convertHexToInt(descriptionMap.data[0])
                logDebug("status: ${status}")
                if (status == 0) {
                    def capacity = zigbee.convertHexToInt(descriptionMap.data[1])
                    logDebug("capacity: ${capacity}")
                    def groupID = zigbee.convertHexToInt(descriptionMap.data[3..2].join())
                    logDebug("groupID: ${groupID}")
                    def count = zigbee.convertHexToInt(descriptionMap.data[4])
                    logDebug("scene count: ${count}")
                    if (count == 0) {
                        resetScenes(endpoint, groupID)
                    }
                    else {
                        for (def i = 0; i < count; i++) {
                            logDebug("count ${i + 1}")
                            def start = 5 + i
                            logDebug("start ${start}")
                            def scene = descriptionMap.data[start]
                            logDebug("scene ${scene}")
                            def sceneID = zigbee.convertHexToInt(scene)
                            logDebug("sceneID: ${sceneID}")
                            storeScenes(endpoint, groupID, sceneID, i == 0)
                        }
                    }
                }
                else {
                    logDebug('get scene membership failed!')
                    def groupID = zigbee.convertHexToInt(descriptionMap.data[3..2].join())
                    logDebug("groupID: ${groupID}")
                    resetScenes(endpoint, groupID)
                }
            }
            else {
                logDebug('scene (0005) command skipped')
            }
        }
        else if (descriptionMap.cluster == '0006' || descriptionMap.clusterId == '0006' || descriptionMap.clusterInt == 6) {
            if (descriptionMap.command == '0A' || descriptionMap.command == '01') {
                if (descriptionMap.attrId == '0000' || descriptionMap.attrInt == 0) {
                    logDebug('on off (0006) on off report')
                    if (endpoint == 1 || endpoint == 2) {
                        def onOffValue = zigbee.convertHexToInt(descriptionMap.value)
                        if (onOffValue == 1 || onOffValue == 0) {
                            logDebug("on off report is ${onOffValue}")
                            def onOffState = onOffValue == 0 ? 'off' : 'on'
                            def descriptionText = "was turned ${onOffState}"
                            def currentChildValue = getChildDeviceCurrentValue("EP0${endpoint}")
                            if (onOffState == currentChildValue) {
                                descriptionText = "is ${onOffState}"
                            }
                            def type = 'physical'
                            def action = state["action-EP0${endpoint}"] ?: 'standby'
                            if (action == 'digitalon' || action == 'digitaloff') {
                                logDebug("action is ${action}")
                                type = 'digital'
                                state["action-EP0${endpoint}"] = 'standby'
                                logDebug('action set to standby')
                            }
                            sendEventToChildDevice("EP0${endpoint}", 'switch', onOffState, descriptionText, ['type': type])
                            def currentValue = device.currentValue('switch')
                            if (getChildDeviceCurrentValue("EP0${endpoint}") == onOffState) {
                                logDebug('both child devices states match')
                                descriptionText = "${device.displayName} was turned ${onOffState}"
                                if (onOffState == currentValue) {
                                    descriptionText = "${device.displayName} is ${onOffState}"
                                }
                                logText(descriptionText)
                                events.add(getEvent([name: 'switch', value: onOffState, descriptionText: descriptionText]))
                            }
                        }
                        else {
                            logDebug("skipping onOffValue: ${onOffValue}")
                        }
                    }
                    else {
                        logDebug('on off (0006) attribute on unexpected endpoint, skipped')
                    }
                }
                else {
                    logDebug('on off (0006) attribute skipped')
                }
            }
            else {
                logDebug('on off (0006) command skipped')
            }
        }
        else if (descriptionMap.cluster == '0702' || descriptionMap.clusterId == '0702' || descriptionMap.clusterInt == 1794) {
            if (descriptionMap.command == '0A' || descriptionMap.command == '01') {
                if (descriptionMap.attrId == '0000' || descriptionMap.attrInt == 0) {
                    logDebug('simple metering (0702) current summation delivered report')
                    if (endpoint == 11) {
                        def energyValue = zigbee.convertHexToInt(descriptionMap.value)
                        logDebug("energy report is ${energyValue}") //3600000 = 1kWh
                        energyValue = energyValue / 3600000
                        def descriptionText = "${device.displayName} is ${energyValue} kWh"
                        logText(descriptionText)
                        events.add(getEvent([name: 'energy', value: energyValue, unit: 'kWh', descriptionText: descriptionText]))
                    }
                    else {
                        logDebug('simple metering (0702) attribute on unexpected endpoint, skipped')
                    }
                }
                else {
                    logDebug('simple metering (0702) attribute skipped')
                }
            }
            else {
                logDebug('simple metering (0702) command skipped')
            }
        }
        else if (descriptionMap.cluster == '0B04' || descriptionMap.clusterId == '0B04' || descriptionMap.clusterInt == 2820) {
            if (descriptionMap.command == '0A' || descriptionMap.command == '01') {
                if (descriptionMap.attrId == '050B' || descriptionMap.attrInt == 1291) {
                    logDebug('electrical measurement (0B04) power report')
                    if (endpoint == 11) {
                        def powerValue = zigbee.convertHexToInt(descriptionMap.value)
                        logDebug("power report is ${powerValue}") //10 = 1W
                        powerValue = powerValue / 10
                        def descriptionText = "${device.displayName} is ${powerValue} W"
                        logText(descriptionText)
                        events.add(getEvent([name: 'power', value: powerValue, unit: 'W', descriptionText: descriptionText]))
                    }
                    else {
                        logDebug('electrical measurement (0B04) attribute on unexpected endpoint, skipped')
                    }
                }
                else if (descriptionMap.attrId == '0505' || descriptionMap.attrInt == 1285) {
                    logDebug('electrical measurement (0B04) voltage report')
                    if (endpoint == 11) {
                        def voltageValue = zigbee.convertHexToInt(descriptionMap.value)
                        logDebug("voltage report is ${voltageValue}") //10 = 1V
                        voltageValue = voltageValue / 10
                        def descriptionText = "${device.displayName} is ${voltageValue} V"
                        logText(descriptionText)
                        events.add(getEvent([name: 'voltage', value: voltageValue, unit: 'V', descriptionText: descriptionText]))
                    }
                    else {
                        logDebug('electrical measurement (0B04) attribute on unexpected endpoint, skipped')
                    }
                }
                else if (descriptionMap.attrId == '0508' || descriptionMap.attrInt == 1288) {
                    logDebug('electrical measurement (0B04) current report')
                    if (endpoint == 11) {
                        def currentValue = zigbee.convertHexToInt(descriptionMap.value)
                        logDebug("current report is ${currentValue}") //1000 = 1A
                        currentValue = currentValue / 1000
                        def descriptionText = "${device.displayName} is ${currentValue} A"
                        logText(descriptionText)
                        events.add(getEvent([name: 'amperage', value: currentValue, unit: 'A', descriptionText: descriptionText]))
                    }
                    else {
                        logDebug('electrical measurement (0B04) attribute on unexpected endpoint, skipped')
                    }
                }
                else {
                    logDebug('electrical measurement (0B04) attribute skipped')
                }
            }
            else {
                logDebug('electrical measurement (0B04) command skipped')
            }
        }
        else {
            logDebug('skipped')
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

def sendEventToChildDevice(address, event, attributeValue, childDescriptionText, options = [:]) {
    logDebug("sendEventToChildDevice called address: ${address} event: ${event} attributeValue: ${attributeValue} descriptionText: ${childDescriptionText} options: ${options}")
    def childDevice = this.getChildDevice("${device.id}-${address}")
    if (childDevice == null) {
        logDebug("creating child device for address: ${address}")
        this.addChildDevice('Vesternet', 'Vesternet VES-ZB-SWI-015 2 Channel Switch Child Switch', "${device.id}-${address}", [name: "${device.displayName} ${address}", label: "${device.displayName} ${address}", isComponent: true])
        childDevice = this.getChildDevice("${device.id}-${address}")
    }
    if (childDevice) {
        logDebug("got child device name: ${childDevice.name} displayName: ${childDevice.displayName}, sending event")
        descriptionText = "${childDevice.displayName} ${childDescriptionText}"
        def childEvent = [name: event, value: attributeValue, descriptionText: descriptionText]
        if (options.type) {
            childEvent.type = options.type
        }
        if (options.unit) {
            childEvent.unit = options.unit
        }
        childDevice.parse([getEvent(childEvent)])
    }
    else {
        log.warn('could not find child device, skipping event!')
    }
}

def getChildDeviceCurrentValue(address) {
    logDebug("getChildDeviceCurrentValue called address: ${address}")
    def currentValue = 'unknown'
    def childDevice = this.getChildDevice("${device.id}-${address}")
    if (childDevice == null) {
        logDebug("creating child device for address: ${address}")
        this.addChildDevice('Vesternet', 'Vesternet VES-ZB-SWI-015 2 Channel Switch Child Switch', "${device.id}-${address}", [name: "${device.displayName} ${address}", label: "${device.displayName} ${address}", isComponent: true])
        childDevice = this.getChildDevice("${device.id}-${address}")
    }
    if (childDevice) {
        logDebug("got child device name: ${childDevice.name} displayName: ${childDevice.displayName}, getting state")
        currentValue = childDevice.currentValue('switch') ?: 'unknown'
        logDebug("got currentValue: ${currentValue}")
        return currentValue
    }
    else {
        log.warn('could not find child device!')
    }
    return currentValue
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
    log.warn('debug logging disabled')
    device.updateSetting('logEnable', [value:'false', type: 'bool'])
}

def intTo16bitUnsignedHex(value) {
    def hexStr = zigbee.convertToHexString(value.toInteger(), 4)
    return new String(hexStr.substring(2, 4) + hexStr.substring(0, 2))
}

def intTo8bitUnsignedHex(value) {
    return zigbee.convertToHexString(value.toInteger(), 2)
}
