/**
 *    Vesternet VES-ZB-TEM-027 Temperature & Humidity Sensor
 *
 */
metadata {
    definition (name: 'Vesternet VES-ZB-TEM-027 Temperature & Humidity Sensor', namespace: 'Vesternet', author: 'Vesternet', importUrl: 'https://raw.githubusercontent.com/vesternet/hubitat-zigbee-device-drivers/main/Vesternet%20VES-ZB-TEM-027%20Temperature%20%26%20Humidity%20Sensor.groovy') {
        capability 'TemperatureMeasurement'
        capability 'RelativeHumidityMeasurement'
        capability 'Battery'
        capability 'Sensor'
        capability 'Refresh'
        capability 'Configuration'

        fingerprint profileId: '0104', endpointId: '01', inClusters: '0000,0001,0003,0402,0405,0020,0B05', outClusters: '0019', model: 'TempAndHumSensor-ZB3.0', manufacturer: 'Shyugj'
    }
    preferences {
        input name: 'temperatureReportChange', type: 'enum', title: 'Temperature Change (°C)', options: ['0.5': '0.5°C', '1':'1°C','1.5':'1.5°C','2':'2°C','2.5':'2.5°C','3':'3°C','3.5':'3.5°C','4':'4°C','4.5':'4.5°C','5':'5°C'], defaultValue: '0.5'
        input name: 'temperatureReportTime', type: 'enum', title: 'Temperature Time (minutes)', options: ['60':'1m','90':'1.5m','120':'2m','240':'4m','300':'5m','600':'10m','1200':'20m','1800':'30m','2400':'40m','3000':'50m','3600':'60m','5400':'90m','7200':'120m'], defaultValue: '1800'
        input name: 'humidityReportChange', type: 'enum', title: 'Humidity Change (%)', options: ['0.5': '0.5%', '1':'1%','1.5':'1.5%','2':'2%','2.5':'2.5%','3':'3%','3.5':'3.5%','4':'4%','4.5':'4.5%','5':'5%'], defaultValue: '1'
        input name: 'humidityReportTime', type: 'enum', title: 'Humidity Time (minutes)', options: ['60':'1m','90':'1.5m','120':'2m','240':'4m','300':'5m','600':'10m','1200':'20m','1800':'30m','2400':'40m','3000':'50m','3600':'60m','5400':'90m','7200':'120m'], defaultValue: '3600'
        input name: 'batteryPercentageReportTime', type: 'enum', title: 'Battery Percentage Time (hours)', options: ['3600':'1h','5400':'1.5h','7200':'2h','10800':'3h','21600':'6h','28800':'8h','43200':'12h','64800':'18h','86400':'24h','172800':'48h','259200':'72h'], defaultValue: '28800'
        input name: 'logEnable', type: 'bool', title: 'Enable Debug Logging', defaultValue: true
        input name: 'txtEnable', type: 'bool', title: 'Enable descriptionText Logging', defaultValue: true
    }
}

def installed() {
    device.updateSetting('logEnable', [value: 'true', type: 'bool'])
    device.updateSetting('txtEnable', [value: 'true', type: 'bool'])
    logDebug('installed called')
    device.updateSetting('temperatureReportChange', [value: '0.5', type: 'enum'])
    device.updateSetting('temperatureReportTime', [value: '1800', type: 'enum'])
    device.updateSetting('humidityReportChange', [value: '1', type: 'enum'])
    device.updateSetting('humidityReportTime', [value: '3600', type: 'enum'])
    device.updateSetting('batteryPercentageReportTime', [value: '28800', type: 'enum'])
    runIn(1800,logsOff)
}

def updated() {
    logDebug('updated called')
    log.warn("debug logging is: ${logEnable == true}")
    log.warn("descriptionText logging is: ${txtEnable == true}")
    log.warn("temperature change is: ${temperatureReportChange.toBigDecimal()}°C")
    log.warn("temperature time is: ${temperatureReportTime.toBigDecimal() / 60} minutes")
    log.warn("humidity change is: ${humidityReportChange.toBigDecimal()}%")
    log.warn("humidity time is: ${humidityReportTime.toBigDecimal() / 60} minutes")
    log.warn("battery time is: ${batteryPercentageReportTime.toBigDecimal() / 3600} hours")
    state.clear()
    unschedule()
    if (logEnable) runIn(1800,logsOff)
}

def configure() {
    logDebug('configure called')
    logDebug('battery powered device requires manual wakeup to accept configuration commands')
    logDebug("temperature change is: ${temperatureReportChange.toBigDecimal()}°C")
    logDebug("temperature time is: ${temperatureReportTime.toBigDecimal()}s")
    def temperaturechange = (temperatureReportChange.toBigDecimal() * 100).toInteger()
    logDebug("temperaturechange: ${temperaturechange}")
    if (temperaturechange == 0) {
        logDebug('temperaturechange is ZERO, protecting against report flooding!')
        temperaturechange = 1000
    }
    temperaturechange = intTo16bitUnsignedHex(temperaturechange)
    def temperaturetime = temperatureReportTime.toInteger()
    logDebug("humidity change is: ${humidityReportChange.toBigDecimal()}%")
    logDebug("humidity time is: ${humidityReportTime.toBigDecimal()}s")
    def humiditychange = (humidityReportChange.toBigDecimal() * 100).toInteger()
    logDebug("humiditychange: ${humiditychange}")
    if (humiditychange == 0) {
        logDebug('humiditychange is ZERO, protecting against report flooding!')
        humiditychange = 1000
    }
    humiditychange = intTo16bitUnsignedHex(humiditychange)
    def humiditytime = humidityReportTime.toInteger()
    logDebug("battery percentage time is: ${batteryPercentageReportTime.toBigDecimal()}s")
    def batterytime = batteryPercentageReportTime.toInteger()
    def cmds = [ "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0402 {${device.zigbeeId}} {}", 'delay 200',
                "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0405 {${device.zigbeeId}} {}", 'delay 200',
                "zdo bind 0x${device.deviceNetworkId} 0x01 0x01 0x0001 {${device.zigbeeId}} {}", 'delay 200',
                "he cr 0x${device.deviceNetworkId} 0x01 0x0402 0x0000 0x29 60 ${temperaturetime} {${temperaturechange}}", 'delay 200',
                "he cr 0x${device.deviceNetworkId} 0x01 0x0405 0x0000 0x21 60 ${humiditytime} {${humiditychange}}", 'delay 200',
                "he cr 0x${device.deviceNetworkId} 0x01 0x0001 0x0021 0x20 3600 ${batterytime} {${intTo16bitUnsignedHex(2)}}", 'delay 200',
                "he raw 0x${device.deviceNetworkId} 1 0x01 0x0402 {10 00 08 00 0000}", 'delay 200',
                "he raw 0x${device.deviceNetworkId} 1 0x01 0x0405 {10 00 08 00 0000}", 'delay 200',
                "he raw 0x${device.deviceNetworkId} 1 0x01 0x0001 {10 00 08 00 2100}", 'delay 200',
                "he rattr 0x${device.deviceNetworkId} 0x01 0x0402 0 {}", 'delay 200',
                "he rattr 0x${device.deviceNetworkId} 0x01 0x0405 0 {}", 'delay 200',
                "he rattr 0x${device.deviceNetworkId} 0x01 0x0001 0x0021 {}"
                ]
    logDebug("sending ${cmds}")
    return cmds
}

def refresh() {
    logDebug('refresh called')
    logDebug('battery powered device requires manual wakeup to accept refresh commands')
    def cmds = [ "he rattr 0x${device.deviceNetworkId} 0x01 0x0402 0 {}", 'delay 200',
                "he rattr 0x${device.deviceNetworkId} 0x01 0x0405 0 {}", 'delay 200',
                "he rattr 0x${device.deviceNetworkId} 0x01 0x0001 0x0021 {}" ]
    logDebug("sending ${cmds}")
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
    if (!(descriptionMap.profileId) || (descriptionMap.profileId && descriptionMap.profileId == '0104')) {
        if (descriptionMap.cluster == '0402' || descriptionMap.clusterId == '0402' || descriptionMap.clusterInt == 1026) {
            if (descriptionMap.command == '0A' || descriptionMap.command == '01') {
                if (descriptionMap.attrId == '0000' || descriptionMap.attrInt == 0) {
                    logDebug('temperature (0402) measured value')
                    def temperatureValue = zigbee.convertHexToInt(descriptionMap.value)
                    logDebug("temperature report is ${temperatureValue}")
                    temperatureValue = temperatureValue / 100
                    def descriptionText = "${device.displayName} temperature is ${temperatureValue}°C"
                    logText(descriptionText)
                    events.add([name: 'temperature', value: temperatureValue, unit: '°C', descriptionText: descriptionText])
                }
                else {
                    logDebug("temperature (0402) attribute ${descriptionMap.attrId} ${descriptionMap.attrInt} skipped")
                }
            }
            else {
                logDebug("temperature (0402) command ${descriptionMap.command} skipped")
            }
        }
        else if (descriptionMap.cluster == '0405' || descriptionMap.clusterId == '0405' || descriptionMap.clusterInt == 1029) {
            if (descriptionMap.command == '0A' || descriptionMap.command == '01') {
                if (descriptionMap.attrId == '0000' || descriptionMap.attrInt == 0) {
                    logDebug('humidity (0405) measured value')
                    def humidityValue = zigbee.convertHexToInt(descriptionMap.value)
                    logDebug("humidity report is ${humidityValue}")
                    humidityValue = humidityValue / 100
                    def descriptionText = "${device.displayName} humidity is ${humidityValue}%"
                    logText(descriptionText)
                    events.add([name: 'humidity', value: humidityValue, unit: '%', descriptionText: descriptionText])
                }
                else {
                    logDebug("humidity (0405) attribute ${descriptionMap.attrId} ${descriptionMap.attrInt} skipped")
                }
            }
            else {
                logDebug("humidity (0405) command ${descriptionMap.command} skipped")
            }
        }
        else if (descriptionMap.cluster == '0001' || descriptionMap.clusterId == '0001' || descriptionMap.clusterInt == 1) {
            if (descriptionMap.command == '0A' || descriptionMap.command == '01') {
                if (descriptionMap.attrId == '0021' || descriptionMap.attrInt == 33) {
                    logDebug('power configuration (0001) battery percentage report')
                    def batteryValue = zigbee.convertHexToInt(descriptionMap.value)
                    if (batteryValue > 100) {
                        logDebug('battery value is more than 100, dividing by 2')
                        batteryValue = batteryValue / 2;
                    }
                    logDebug("battery percentage report is ${batteryValue}")
                    def descriptionText = "${device.displayName} battery percent is ${batteryValue}%"
                    logText(descriptionText)
                    events.add([name: 'battery', value: batteryValue, unit: '%', descriptionText: descriptionText, isStateChange: true])
                }
                else if (descriptionMap.attrId == '0020' || descriptionMap.attrInt == 32) {
                    logDebug('power configuration (0001) battery voltage report')
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