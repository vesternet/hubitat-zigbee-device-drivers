/**
 *    Vesternet VES-ZB-SWI-005 2-Wire Capable Switch
 */
metadata {
    definition(name: 'Vesternet VES-ZB-SWI-005 2-Wire Capable Switch', namespace: 'Vesternet', author: 'Vesternet', importUrl: 'https://raw.githubusercontent.com/vesternet/hubitat-zigbee-device-drivers/main/Vesternet%20VES-ZB-SWI-005%202-Wire%20Capable%20Switch.groovy') {
        capability 'Switch'
        capability 'Actuator'
        capability 'Configuration'
        capability 'Refresh'

        command 'addGroup', [[name:'Group ID*', type: 'NUMBER', description: 'Enter the Group ID (1-65527) that you wish to add to this device.']]
        command 'removeGroup', [[name:'Group ID*', type: 'NUMBER', description: 'Enter the Group ID (1-65527) that you wish to remove from this device.']]
        command 'readGroups'
        command 'clearGroups'

        command 'storeScene', [[name:'Group ID*', type: 'NUMBER', description: 'Enter the Group ID (0-65527) that you wish to store a Scene in on this device.'], [name:'Scene ID*', type: 'NUMBER', description: 'Enter the Scene ID (1-255) that you wish to add to the Group on this device.  The current state of the device will be stored to this Scene ID.']]
        command 'removeScene', [[name:'Group ID*', type: 'NUMBER', description: 'Enter the Group ID (0-65527) that you wish to remove a Scene from on this device.'], [name:'Scene ID*', type: 'NUMBER', description: 'Enter the Scene ID (1-255) that you wish to remove from the Group on this device.']]
        command 'recallScene', [[name:'Group ID*', type: 'NUMBER', description: 'Enter the Group ID (0-65527) that you wish to recall a Scene from on this device.'], [name:'Scene ID*', type: 'NUMBER', description: 'Enter the Scene ID (1-255) that you wish to recall from the Group on this device.']]
        command 'readScenes', [[name:'Group ID*', type: 'NUMBER', description: 'Enter the Group ID (0-65527) that you wish to read Scenes from on this device.']]
        command 'clearScenes', [[name:'Group ID*', type: 'NUMBER', description: 'Enter the Group ID (0-65527) that you wish to clear Scenes from on this device.']]

        fingerprint profileId: '0104', endpointId: '01', inClusters: '0000,0003,0004,0005,0006,0B05,1000', outClusters: '0019', manufacturer: 'Sunricher', model: 'Micro Smart OnOff', deviceJoinName: 'Vesternet VES-ZB-SWI-005 2-Wire Capable Switch'
        fingerprint profileId: '0104', endpointId: '01', inClusters: '0000,0003,0004,0005,0006,0B05,1000', outClusters: '0019', manufacturer: 'Sunricher', model: 'HK-SL-RELAY-A', deviceJoinName: 'Vesternet VES-ZB-SWI-005 2-Wire Capable Switch'
    }
    preferences {
        input name: 'sceneTransitionTime', type: 'enum', title: 'Scene Transition Time (ms)', options: [0:'0s', 500:'0.5s', 1000:'1s', 1500:'1.5s', 2000:'2s', 3000:'3s', 4000:'4s', 5000:'5s'], defaultValue: 1000
        input name: 'powerFailState', type: 'enum', title: 'Load State After Power Failure', options: [ 'off': 'Off', 'on': 'On', 'opposite': 'Opposite', 'previous': 'Previous' ], defaultValue: 'previous'
        input name: 'logEnable', type: 'bool', title: 'Enable Debug Logging', defaultValue: true
        input name: 'txtEnable', type: 'bool', title: 'Enable descriptionText Logging', defaultValue: true
    }
}

def installed() {
    device.updateSetting('logEnable', [value: 'true', type: 'bool'])
    device.updateSetting('txtEnable', [value: 'true', type: 'bool'])
    logDebug('installed called')
    device.updateSetting('sceneTransitionTime', [value: '1000', type: 'enum'])
    device.updateSetting('powerFailState', [value: 'previous', type: 'enum'])
    runIn(1800, logsOff)
}

def updated() {
    logDebug('updated called')
    log.warn("debug logging is: ${logEnable == true}")
    log.warn("descriptionText logging is: ${txtEnable == true}")
    log.warn("sceneTransition time is: ${sceneTransitionTime}ms")
    log.warn("power fail state is: ${powerFailState ?: 'previous'}")
    state.clear()
    unschedule()
    if (logEnable) {
        runIn(1800, logsOff)
    }
}

def configure() {
    logDebug('configure called')
    def cmds = zigbee.onOffConfig()
    def startUpOnOffOptions = ['on': 0x01, 'off': 0x0, 'opposite': 0x02, 'previous': 0x03]
    def startUpOnOff = powerFailState ? startUpOnOffOptions[powerFailState] : 0x03
    logDebug("power fail state is: ${powerFailState ?: 'previous'}")
    logDebug("startUpOnOff is: ${startUpOnOff}")
    cmds += zigbee.writeAttribute(0x0006, 0x4003, DataType.ENUM8, startUpOnOff)
    logDebug("sending ${cmds}")
    return cmds
}

def refresh() {
    logDebug('refresh called')
    def cmds = zigbee.onOffRefresh()
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
    def cmds = ["he cmd 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0006 1 {}"]
    logDebug("sending ${cmds}")
    state['action'] = 'digitalon'
    return cmds
}

def off() {
    logDebug('off called')
    def cmds = ["he cmd 0x${device.deviceNetworkId} 0x${device.endpointId} 0x0006 0 {}"]
    logDebug("sending ${cmds}")
    state['action'] = 'digitaloff'
    return cmds
}

void parse(String description) {
    logDebug('parse called')
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
                    def onOffValue = zigbee.convertHexToInt(descriptionMap.value)
                    if (onOffValue == 1 || onOffValue == 0) {
                        logDebug("on off report is ${onOffValue}")
                        def onOffState = onOffValue == 0 ? 'off' : 'on'
                        def descriptionText = "${device.displayName} was turned ${onOffState}"
                        def currentValue = device.currentValue('switch') ?: 'unknown'
                        if (onOffState == currentValue) {
                            descriptionText = "${device.displayName} is ${onOffState}"
                        }
                        def type = 'physical'
                        def action = state['action'] ?: 'standby'
                        if (action == 'digitalon' || action == 'digitaloff') {
                            logDebug("action is ${action}")
                            type = 'digital'
                            state['action'] = 'standby'
                            logDebug('action set to standby')
                        }
                        logText(descriptionText)
                        events.add(getEvent([name: 'switch', value: onOffState, type: type, descriptionText: descriptionText]))
                    }
                    else {
                        logDebug("skipping onOffValue: ${onOffValue}")
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
