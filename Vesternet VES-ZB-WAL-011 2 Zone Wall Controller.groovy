/**
 *    Vesternet VES-ZB-WAL-011 2 Zone Wall Controller
 */
metadata {
    definition(name: 'Vesternet VES-ZB-WAL-011 2 Zone Wall Controller', namespace: 'Vesternet', author: 'Vesternet', importUrl: 'https://raw.githubusercontent.com/vesternet/hubitat-zigbee-device-drivers/main/Vesternet%20VES-ZB-WAL-011%202%20Zone%20Wall%20Controller.groovy') {
        capability 'PushableButton'
        capability 'HoldableButton'
        capability 'ReleasableButton'
        capability 'Battery'
        capability 'Sensor'
        capability 'Configuration'

        command 'createGroupBinding', [[name: 'Cluster*', type: 'ENUM', constraints: ['On/Off Cluster (0006)', 'Level Cluster (0008)'], description: 'Select Cluster to bind on this device.'], [name:'Group ID*', type: 'NUMBER', description: 'Enter the Group ID that you wish to bind to (1-65527).'], [name:'Source Endpoint ID*', type: 'ENUM', constraints: ['01', '02'], description: 'Enter the Source Endpoint ID from this device that you wish to bind to.']]
        command 'removeGroupBinding', [[name: 'Cluster*', type: 'ENUM', constraints: ['On/Off Cluster (0006)', 'Level Cluster (0008)'], description: 'Select Cluster to unbind on this device.'], [name:'Group ID*', type: 'NUMBER', description: 'Enter the Group ID that you wish to unbind from (1-65527).'], [name:'Source Endpoint ID*', type: 'ENUM', constraints: ['01', '02'], description: 'Enter the Source Endpoint ID from this device that you wish to unbind from.']]
        command 'createDeviceBinding', [[name: 'Cluster*', type: 'ENUM', constraints: ['On/Off Cluster (0006)', 'Level Cluster (0008)'], description: 'Select Cluster to bind on this device.'], [name:'Device IEEE Address*', type: 'STRING', description: 'Enter the Device IEEE Address that you wish to bind to.'], [name:'Destination Endpoint ID*', type: 'NUMBER', description: 'Enter the Destination Endpoint ID that you wish to bind to (0-255).'], [name:'Source Endpoint ID*', type: 'ENUM', constraints: ['01', '02'], description: 'Enter the Source Endpoint ID from this device that you wish to bind to.']]
        command 'removeDeviceBinding', [[name: 'Cluster*', type: 'ENUM', constraints: ['On/Off Cluster (0006)', 'Level Cluster (0008)'], description: 'Select Cluster to unbind on this device.'], [name:'Device IEEE Address*', type: 'STRING', description: 'Enter the Device IEEE Address that you wish to unbind from.'], [name:'Destination Endpoint ID*', type: 'NUMBER', description: 'Enter the Destination Endpoint ID that you wish to unbind from (0-255).'], [name:'Source Endpoint ID*', type: 'ENUM', constraints: ['01', '02'], description: 'Enter the Source Endpoint ID from this device that you wish to unbind from.']]
        command 'readBindings'
        command 'clearBindings'
        
        fingerprint profileId: '0104', endpointId: '01', inClusters: '0000,0001,0003,0B05', outClusters: '0003,0004,0005,0006,0008,0019,0300,1000', manufacturer: 'Sunricher', model: 'ZG2833K4_EU06', deviceJoinName: 'Vesternet VES-ZB-WAL-011 2 Zone Wall Controller'
    }
    preferences {
        input name: 'batteryPercentageReportTime', type: 'enum', title: 'Battery Percentage Time (hours)', options: ['3600':'1h', '5400':'1.5h', '7200':'2h', '10800':'3h', '21600':'6h', '28800':'8h', '43200':'12h', '64800':'18h', '86400':'24h', '172800':'48h', '259200':'72h'], defaultValue: '28800'
        input name: 'createChildDevices', type: 'bool', title: 'Create child devices for Switch / SwitchLevel capability Events', defaultValue: false
        input name: 'logEnable', type: 'bool', title: 'Enable Debug Logging', defaultValue: true
        input name: 'txtEnable', type: 'bool', title: 'Enable descriptionText Logging', defaultValue: true
    }
}

def getModelNumberOfButtons() {
    logDebug('getModelNumberOfButtons called')
    ['ZG2833K4_EU06' : 4]
}

def installed() {
    device.updateSetting('logEnable', [value: 'true', type: 'bool'])
    device.updateSetting('txtEnable', [value: 'true', type: 'bool'])
    logDebug('installed called')
    def numberOfButtons = modelNumberOfButtons[device.getDataValue('model')]
    logDebug("numberOfButtons: ${numberOfButtons}")
    sendEvent(getEvent(name: 'numberOfButtons', value: numberOfButtons, displayed: false))
    for(def buttonNumber : 1..numberOfButtons) {
        sendEvent(buttonAction('pushed', buttonNumber, 'digital'))
    }
    device.updateSetting('createChildDevices', [value: 'false', type: 'bool'])
    device.updateSetting('batteryPercentageReportTime', [value: '28800', type: 'enum'])
    runIn(1800, logsOff)
}

def updated() {
    logDebug('updated called')
    log.warn("debug logging is: ${logEnable == true}")
    log.warn("descriptionText logging is: ${txtEnable == true}")
    log.warn("battery time is: ${batteryPercentageReportTime.toBigDecimal() / 3600} hours")
    log.warn("create child devices is: ${createChildDevices == true}")
    state.clear()
    unschedule()
    if (logEnable) {
        runIn(1800, logsOff)
    }
}

def configure() {
    logDebug('configure called')
    logDebug('battery powered device requires manual wakeup to accept configuration commands')
    logDebug("battery percentage time is: ${batteryPercentageReportTime.toBigDecimal()}s")
    def batterytime = batteryPercentageReportTime.toInteger()
    def cmds = [ "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0006 {${device.zigbeeId}} {}", 'delay 200',
                "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0008 {${device.zigbeeId}} {}", 'delay 200',
                "zdo bind 0x${device.deviceNetworkId} 0x02 0x01 0x0006 {${device.zigbeeId}} {}", 'delay 200',
                "zdo bind 0x${device.deviceNetworkId} 0x02 0x01 0x0008 {${device.zigbeeId}} {}", 'delay 200',
                "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0001 {${device.zigbeeId}} {}", 'delay 200',
                "he cr 0x${device.deviceNetworkId} 0x01 0x0001 0x0021 0x20 3600 ${batterytime} {${intTo16bitUnsignedHex(2)}}", 'delay 200',
                "he rattr 0x${device.deviceNetworkId} 0x01 0x0001 0x0021 {}" ]
    logDebug("sending ${cmds}")
    return cmds
}

def readBindings(startIndex = 0) {
    logDebug("readBindings called startIndex: ${startIndex}")
    def cmds = [ "he raw 0x${device.deviceNetworkId} 0x0 0x0 0x0033 {00 ${zigbee.convertToHexString(startIndex, 1)}} {0x0000}" ]
    log.warn('battery powered device, wake it up manually to allow bindings to be read!')
    logDebug("sending ${cmds}")
    sendHubCommand(new hubitat.device.HubMultiAction(cmds, hubitat.device.Protocol.ZIGBEE))
}

def clearBindings() {
    logDebug('clearBindings')
    logDebug("group bindings: ${state['GroupBindings']}")
    logDebug("device bindings: ${state['DeviceBindings']}")
    if (state['GroupBindings'] != [:]) {
        state['GroupBindings'].each { cluster, groups ->
            logDebug("removing group bindings for cluster: ${cluster}")
            logDebug("groups: ${groups}")
            groups.each { group ->
                logDebug("removing group: ${group.group}")
                removeGroupBinding(cluster, group.group, group.sourceEndpoint)
            }
        }
    }
    if (state['DeviceBindings'] != [:]) {
        state['DeviceBindings'].each { cluster, devices ->
            logDebug("removing device bindings for cluster: ${cluster}")
            logDebug("devices: ${devices}")
            devices.each { device ->
                logDebug("removing device: ${device.device}")
                removeDeviceBinding(cluster, device.device, device.destinationEndpoint, device.sourceEndpoint)
            }
        }
    }
    logDebug("group bindings: ${state['GroupBindings']}")
    logDebug("device bindings: ${state['DeviceBindings']}")
    state['GroupBindings'] = [:]
    state['DeviceBindings'] = [:]
}

def createGroupBinding(cluster, group, sourceEndpoint = 1) {
    logDebug("createGroupBinding called cluster: ${cluster} group: ${group} sourceEndpoint: ${sourceEndpoint}")
    manageBinding('group', 'create', sourceEndpoint, cluster, group)
}

def removeGroupBinding(cluster, group, sourceEndpoint = 1) {
    logDebug("removeGroupBinding called cluster: ${cluster} group: ${group} sourceEndpoint: ${sourceEndpoint}")
    manageBinding('group', 'remove', sourceEndpoint, cluster, group)
}

def createDeviceBinding(cluster, device, destinationEndpoint, sourceEndpoint = 1) {
    logDebug("createDeviceBinding called cluster: ${cluster} device: ${device} destinationEndpoint: ${destinationEndpoint} sourceEndpoint: ${sourceEndpoint}")
    manageBinding('device', 'create', sourceEndpoint, cluster, device, destinationEndpoint)
}

def removeDeviceBinding(cluster, device, destinationEndpoint, sourceEndpoint = 1) {
    logDebug("removeDeviceBinding called cluster: ${cluster} device: ${device} destinationEndpoint: ${destinationEndpoint} sourceEndpoint: ${sourceEndpoint}")
    manageBinding('device', 'remove', sourceEndpoint, cluster, device, destinationEndpoint)
}

def manageBinding(type, action, sourceEndpoint, cluster, address, destinationEndpoint=0) {
    logDebug("manageBinding called type: ${type} action: ${action} sourceEndpoint: ${sourceEndpoint} cluster: ${cluster} address: ${address} destinationEndpoint: ${destinationEndpoint}")
    def bindType = action ? action == 'create' ? 'bind' : action == 'remove' ? 'unbind' : 'bind' : 'bind'
    logDebug("bindType: ${bindType}")
    def clusterID = cluster == 'On/Off Cluster (0006)' ? '0006' : cluster == 'Level Cluster (0008)' ? '0008' : '0006'
    logDebug("clusterID: ${clusterID}")
    def cmds = []
    if (type == 'group') {
        def groupID = address ? address.toInteger() : 0
        if (groupID > 65527 || groupID < 1) {
            logDebug('groupID out of bounds, resetting')
            groupID = groupID > 65527 ? 65527 : groupID < 1 ? 1 : groupID
        }
        logDebug("groupID: ${groupID}")
        def sourceEndpointID = sourceEndpoint ? sourceEndpoint.toInteger() : 0
        if (sourceEndpointID > 255 || sourceEndpointID < 0) {
            logDebug('sourceEndpointID out of bounds, resetting')
            sourceEndpointID = sourceEndpointID > 255 ? 255 : sourceEndpointID < 0 ? 0 : sourceEndpointID
        }
        logDebug("sourceEndpointID: ${sourceEndpointID}")
        cmds += "zdo ${bindType} 0x${device.deviceNetworkId} 0x${zigbee.convertToHexString(sourceEndpointID, 1)} 0x01 0x${clusterID} {${device.zigbeeId}} {${zigbee.convertToHexString(groupID, 4)}}"
    }
    else if (type == 'device') {
        def pattern = /^[0-9A-Fa-f]{16}$/
        if (address =~ pattern) {
            def destinationEndpointID = destinationEndpoint ? destinationEndpoint.toInteger() : 0
            if (destinationEndpointID > 255 || destinationEndpointID < 0) {
                logDebug('destinationEndpointID out of bounds, resetting')
                destinationEndpointID = destinationEndpointID > 255 ? 255 : destinationEndpointID < 0 ? 0 : destinationEndpointID
            }
            logDebug("destinationEndpointID: ${destinationEndpointID}")
            def sourceEndpointID = sourceEndpoint ? sourceEndpoint.toInteger() : 0
            if (sourceEndpointID > 255 || sourceEndpointID < 0) {
                logDebug('sourceEndpointID out of bounds, resetting')
                sourceEndpointID = sourceEndpointID > 255 ? 255 : sourceEndpointID < 0 ? 0 : sourceEndpointID
            }
            logDebug("sourceEndpointID: ${sourceEndpointID}")
            cmds += "zdo ${bindType} 0x${device.deviceNetworkId} 0x${zigbee.convertToHexString(sourceEndpointID, 1)} 0x${zigbee.convertToHexString(destinationEndpointID, 1)} 0x${clusterID} {${device.zigbeeId}} {${address}}"
        }
        else {
            logDebug('invalid IEEE address!')
        }
    }
    log.warn('battery powered device, wake it up manually to allow group bindings to be set!')
    logDebug("sending ${cmds}")
    sendHubCommand(new hubitat.device.HubMultiAction(cmds, hubitat.device.Protocol.ZIGBEE))
    readBindings()
}

def storeBinding(type, sourceEndpoint, cluster, address, destinationEndpoint=0) {
    logDebug("storeBinding called type: ${type}  sourceEndpoint: ${sourceEndpoint} cluster: ${cluster} address: ${address} destinationEndpoint: ${destinationEndpoint}")
    if (type == 'group' || (type == 'device' && address != location.hub.zigbeeEui)) {
        def bindingType = type == 'group' ? 'Group' : 'Device'
        def bindings = state[bindingType + 'BindingsTemporary'] ? state[bindingType + 'BindingsTemporary'] : [:]
        logDebug("current bindings: ${bindings}")
        def clusterBindings = bindings[cluster] ? bindings[cluster] : []
        logDebug("current cluster bindings: ${clusterBindings}")
        def binding = type == 'group' ? ['sourceEndpoint': sourceEndpoint, 'group': address] : ['sourceEndpoint': sourceEndpoint, 'device': address, 'destinationEndpoint': destinationEndpoint]
        clusterBindings.add(binding)
        logDebug("new cluster bindings: ${clusterBindings}")
        state[bindingType + 'BindingsTemporary'][cluster] = clusterBindings
        logDebug("new bindings: ${bindings}")
    }
}

def storeBindings(reset = false) {
    logDebug("storeBindings called reset: ${reset}")
    if (reset) {
        logDebug('reset stored bindings')
        state['DeviceBindings'] = [:]
        state['DeviceBindingsTemporary'] = [:]
        state['GroupBindings'] = [:]
        state['GroupBindingsTemporary'] = [:]
    }
    else {
        logDebug('store completed bindings')
        logDebug("temporary group bindings: ${state['GroupBindingsTemporary']}")
        logDebug("temporary device bindings: ${state['DeviceBindingsTemporary']}")
        if (state['GroupBindingsTemporary'] != [:]) {
            state['GroupBindingsTemporary'].each { clusterID, groups ->
                logDebug("adding clusterID: ${clusterID}")
                def cluster = clusterID == '0006' ? 'On/Off Cluster (0006)' : clusterID == '0008' ? 'Level Cluster (0008)' : 'unknown'
                if (cluster != 'unknown') {
                    state['GroupBindings'][cluster] = []
                    logDebug("groups: ${groups}")
                    groups.each { group ->
                        logDebug("adding group: $group}")
                        state['GroupBindings'][cluster].add(['sourceEndpoint': zigbee.convertHexToInt(group.sourceEndpoint), 'group': zigbee.convertHexToInt(group.group)])
                    }
                }
            }
        }
        if (state['DeviceBindingsTemporary'] != [:]) {
            state['DeviceBindingsTemporary'].each { clusterID, devices ->
                logDebug("adding clusterID: ${clusterID}")
                def cluster = clusterID == '0006' ? 'On/Off Cluster (0006)' : clusterID == '0008' ? 'Level Cluster (0008)' : 'unknown'
                if (cluster != 'unknown') {
                    state['DeviceBindings'][cluster] = []
                    logDebug("devices: ${devices}")
                    devices.each { device ->
                        logDebug("adding device: ${device}")
                        state['DeviceBindings'][cluster].add(['sourceEndpoint': zigbee.convertHexToInt(device.sourceEndpoint), 'device': device.device, 'destinationEndpoint': zigbee.convertHexToInt(device.destinationEndpoint)])
                    }
                }
            }
        }
        logDebug("group bindings: ${state['GroupBindings']}")
        logDebug("device bindings: ${state['DeviceBindings']}")
        state['GroupBindingsTemporary'] = [:]
        state['DeviceBindingsTemporary'] = [:]
    }
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
    if (!(descriptionMap.profileId) || (descriptionMap.profileId && descriptionMap.profileId == '0104') || (descriptionMap.profileId && descriptionMap.profileId == '0000')) {
        if (descriptionMap.profileId && descriptionMap.profileId == '0000') {
            logDebug('got ZDP message')
            if (descriptionMap.cluster == '8033' || descriptionMap.clusterId == '8033' || descriptionMap.clusterInt == 32819) {
                logDebug('got binding table response')
                if (descriptionMap.data[1] && descriptionMap.data[1] == '00') {
                    if (descriptionMap.data[2]) {
                        def numberOfBindings = zigbee.convertHexToInt(descriptionMap.data[2])
                        logDebug("number of bindings: ${numberOfBindings}")
                        def startIndex = zigbee.convertHexToInt(descriptionMap.data[3])
                        logDebug("start index: ${startIndex}")
                        if (startIndex == 0) {
                            storeBindings(true)
                        }
                        def numberOfBindingsInThisList = zigbee.convertHexToInt(descriptionMap.data[4])
                        logDebug("number of bindings in this list: ${numberOfBindingsInThisList}")
                        def x = 5
                        for (def i = 0; i < numberOfBindingsInThisList; i++) {
                            logDebug("binding ${startIndex + i + 1} index ${startIndex + i}")
                            def sourceIeeeAddress = descriptionMap.data[x..x + 7].reverse().join('')
                            logDebug("source ieee address: ${sourceIeeeAddress}")
                            x+= 8
                            def sourceEndpoint = descriptionMap.data[x]
                            logDebug("source endpoint: ${sourceEndpoint}")
                            x++
                            def cluster = descriptionMap.data[x..x + 1 ].reverse().join('')
                            logDebug("cluster: ${cluster}")
                            x+= 2
                            def addressMode = descriptionMap.data[x]
                            logDebug("addressMode: ${addressMode}")
                            x++
                            if (addressMode == '01') {
                                def destinationAddress = descriptionMap.data[x..x + 1].reverse().join('')
                                logDebug("destination address: ${destinationAddress}")
                                x+= 2
                                storeBinding('group', sourceEndpoint, cluster, destinationAddress)
                            }
                            else if (addressMode == '03') {
                                def destinationIeeeAddress = descriptionMap.data[x..x + 7].reverse().join('')
                                logDebug("destination ieee address: ${destinationIeeeAddress}")
                                x+= 8
                                def destinationEndpoint = descriptionMap.data[x]
                                logDebug("destination endpoint: ${destinationEndpoint}")
                                x++
                                storeBinding('device', sourceEndpoint, cluster, destinationIeeeAddress, destinationEndpoint)
                            }
                        }
                        if (numberOfBindings != numberOfBindingsInThisList + startIndex) {
                            logDebug('binding table incomplete, requesting more')
                            readBindings(numberOfBindingsInThisList + startIndex)
                        }
                        else {
                            storeBindings()
                        }
                    }
                    else {
                        logDebug('no size found in binding table data')
                    }
                }
                else {
                    logDebug('no status found in binding table data')
                }
            }
        }
        else if (descriptionMap.cluster == '0006' || descriptionMap.clusterId == '0006' || descriptionMap.clusterInt == 6) {
            def switchCommand = "${descriptionMap.command} unknown"
            switch (descriptionMap.command) {
                case '00':
                    switchCommand = 'off'
                    break
                case '01':
                    switchCommand = 'on'
                    break
            }
            logDebug("switch (0006) command: ${switchCommand}")
            if (descriptionMap.command == '00' || descriptionMap.command == '01') {
                switchCommand = zigbee.convertHexToInt(descriptionMap.command)
                def buttonNumber = switchCommand == 1 ? (endpoint * 2) - 1 : (endpoint * 2)
                logDebug("button number is ${buttonNumber}")
                events.add(buttonAction('pushed', buttonNumber, 'physical'))
            }
            else {
                logDebug('switch (0006) command skipped')
            }
        }
        else if (descriptionMap.cluster == '0008' || descriptionMap.clusterId == '0008' || descriptionMap.clusterInt == 8) {
            def levelCommand = "${descriptionMap.command} unknown"
            switch (descriptionMap.command) {
                case '01':
                    levelCommand = 'move'
                    break
                case '05':
                    levelCommand = 'move with on/off'
                    break
                case '03':
                    levelCommand = 'stop'
                    break
                case '07':
                    levelCommand = 'stop with on/off'
                    break
            }
            logDebug("level (0008) command: ${levelCommand}")
            if (descriptionMap.command == '01' || descriptionMap.command == '05') {
                def levelDirectionData = descriptionMap.data[0];
                if (levelDirectionData == '00' || levelDirectionData == '01') {
                    def levelDirection = "${levelDirectionData} unknown"
                    switch (levelDirectionData) {
                        case '00':
                            levelDirection = 'up'
                            break
                        case '01':
                            levelDirection = 'down'
                            break
                    }
                    logDebug("level (0008) direction: ${levelDirection}")
                    levelDirection = zigbee.convertHexToInt(levelDirectionData)
                    def buttonNumber = levelDirection == 0 ?  (endpoint * 2) - 1 : (endpoint * 2)
                    logDebug("button number is ${buttonNumber}")
                    logDebug('button event is held')
                    events.add(buttonAction('held', buttonNumber, 'physical'))
                }
                else {
                    logDebug("level (0008) direction: ${levelDirectionData} unknown")
                }
            }
            else if (descriptionMap.command == '03' || descriptionMap.command == '07') {
                def buttonNumber = device.currentValue('held', true)
                if (buttonNumber) {
                    logDebug("button number was ${buttonNumber}")
                    logDebug('button event is released')
                    events.add(buttonAction('released', buttonNumber, 'physical'))
                }
                else {
                    logDebug('could not determine buttonNumber')
                }
            }
            else {
                logDebug('level (0008) command skipped')
            }
        }
        else if (descriptionMap.cluster == '0001' || descriptionMap.clusterId == '0001' || descriptionMap.clusterInt == 1) {
            if (descriptionMap.command == '0A' || descriptionMap.command == '01') {
                if (descriptionMap.attrId == '0021' || descriptionMap.attrInt == 33) {
                    logDebug('power configuration (0001) battery report')
                    def batteryValue = zigbee.convertHexToInt(descriptionMap.value)
                    if (batteryValue > 100) {
                        logDebug('battery value is more than 100, dividing by 2')
                        batteryValue = (batteryValue / 2).toInteger();
                    }
                    logDebug("battery percentage report is ${batteryValue}")
                    def descriptionText = "${device.displayName} is ${batteryValue}%"
                    logText(descriptionText)
                    events.add([name: 'battery', value: batteryValue, unit: '%', descriptionText: descriptionText])
                }
                else {
                    logDebug('power configuration (0001) attribute skipped')
                }
            }
            else {
                logDebug('power configuration (0001) command skipped')
            }
        }
        else {
            logDebug('skipped')
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
    logDebug('push called')
    sendEvent(buttonAction('pushed', button, 'digital'))
}

def hold(button){
    logDebug('hold called')
    sendEvent(buttonAction('held', button, 'digital'))
}

def release(button){
    logDebug('release called')
    sendEvent(buttonAction('released', button, 'digital'))
}

def buttonAction(action, button, type) {
    logDebug("buttonAction called button: ${button} action: ${action} type: ${type}")
    buttonAsLight(button, action)
    def descriptionText = "${device.displayName} button ${button} is ${action}"
    logText(descriptionText)
    return getEvent([name: action, value: button, descriptionText: descriptionText, isStateChange: true, type: type])
}

def buttonAsLight(button, action) {
    logDebug("buttonAsLight called button: ${button} action: ${action}")
    def epNumber = ((button.toInteger() / 2) + 0.5).toInteger()
    if (createChildDevices) {
        logDebug('creating child device events')
        logDebug("button: ${button} epNumber: ${epNumber}")
        if (action == 'pushed') {
            logDebug('sending Switch event to child device')
            String onOffState = 'on'
            if (button.toInteger() % 2 == 0) {
                onOffState = 'off'
            }
            sendEventToChildDevice("EP${epNumber}", 'switch', onOffState, "was turned ${onOffState}")
        }
        else if (action == 'held') {
            logDebug('starting SwitchLevel event processing for child device')
            String direction = 'up'
            if (button.toInteger() % 2 == 0) {
                direction = 'down'
            }
            def currentValue = getChildDeviceCurrentValue("EP${epNumber}", 'level') ?: 0
            if (direction == 'down' && currentValue == 0) {
                logDebug('down command but device is at 0%, skipping!')
            }
            else if (direction == 'up' && currentValue == 100) {
                logDebug('up command but device is at 100%, skipping!')
            }
            else {
                sendEventToChildDevice("EP${epNumber}", 'startLevelChange', direction, "was set to ${direction}")
            }
        }
        else if (action == 'released') {
            logDebug('stopping SwitchLevel event processing for child device')
            sendEventToChildDevice("EP${epNumber}", 'stopLevelChange', 'stop', 'was set to stop')
        }
    }
    else {
        def childDevice = this.getChildDevice("${device.id}-EP${epNumber}")
        if (childDevice) {
            logDebug("deleting child device ${device.id}-EP${epNumber}")
            this.deleteChildDevice("${device.id}-EP${epNumber}")
        }
    }
}

def sendEventToChildDevice(address, event, attributeValue, childDescriptionText) {
    logDebug("sendEventToChildDevice called address: ${address} event: ${event} attributeValue: ${attributeValue} descriptionText: ${childDescriptionText}")
    def childDevice = this.getChildDevice("${device.id}-${address}")
    if (childDevice == null) {
        logDebug("creating child device for address: ${address}")
        this.addChildDevice('Vesternet', 'Vesternet Zigbee Wall Controllers & Remote Controls Child Switch', "${device.id}-${address}", [name: "${device.displayName} ${address}", label: "${device.displayName} ${address}", isComponent: true])
        childDevice = this.getChildDevice("${device.id}-${address}")
    }
    if (childDevice) {
        logDebug("got child device name: ${childDevice.name} displayName: ${childDevice.displayName}, sending event")
        descriptionText = "${childDevice.displayName} ${childDescriptionText}"
        def childEvent = [name: event, value: attributeValue, descriptionText: descriptionText, type: 'physical']
        childDevice.parse([getEvent(childEvent)])
    }
    else {
        log.warn('could not find child device, skipping event!')
    }
}

def getChildDeviceCurrentValue(address, attribute) {
    logDebug("getChildDeviceCurrentValue called address: ${address} attribute: ${attribute}")
    def currentValue = 'unknown'
    def childDevice = this.getChildDevice("${device.id}-${address}")
    if (childDevice == null) {
        logDebug("creating child device for address: ${address}")
        this.addChildDevice('Vesternet', 'Vesternet Zigbee Wall Controllers & Remote Controls Child Switch', "${device.id}-${address}", [name: "${device.displayName} ${address}", label: "${device.displayName} ${address}", isComponent: true])
        childDevice = this.getChildDevice("${device.id}-${address}")
    }
    if (childDevice) {
        logDebug("got child device name: ${childDevice.name} displayName: ${childDevice.displayName}, getting state")
        currentValue = childDevice.currentValue(attribute) ?: 'unknown'
        logDebug("got currentValue: ${currentValue}")
    }
    else {
        log.warn('could not find child device!')
    }
    return currentValue
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
    def hexStr = zigbee.convertToHexString(value.toInteger(),4)
    return new String(hexStr.substring(2, 4) + hexStr.substring(0, 2))
}
