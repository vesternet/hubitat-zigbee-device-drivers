/**
 *    Vesternet VES-ZB-SWI-015 2 Channel Switch Child Switch
 *
 */
metadata {
    definition (name: 'Vesternet VES-ZB-SWI-015 2 Channel Switch Child Switch', namespace: 'Vesternet', author: 'Vesternet', component: true, importUrl: 'https://raw.githubusercontent.com/vesternet/hubitat-zigbee-device-drivers/main/Vesternet%20VES-ZB-SWI-015%202%20Channel%20Switch%20Child%20Switch.groovy') {
        capability 'Switch'
        capability 'Actuator'
        capability 'Sensor'
        capability 'Configuration'
        capability 'Refresh'
    }
    preferences {
        input name: 'powerFailState', type: 'enum', title: 'Load State After Power Failure', options: [ 'off': 'Off', 'on': 'On', 'opposite': 'Opposite', 'previous': 'Previous' ], defaultValue: 'previous'
        input name: 'logEnable', type: 'bool', title: 'Enable Debug Logging', defaultValue: true
        input name: 'txtEnable', type: 'bool', title: 'Enable descriptionText Logging', defaultValue: true
    }
}

def installed() {
    device.updateSetting('logEnable', [value: 'true', type: 'bool'])
    device.updateSetting('txtEnable', [value: 'true', type: 'bool'])
    logDebug('installed called')
    device.updateSetting('powerFailState', [value: 'previous', type: 'enum'])
    runIn(1800,logsOff)
}

def updated() {
    logDebug('updated called')
    log.warn("debug logging is: ${logEnable == true}")
    log.warn("descriptionText logging is: ${txtEnable == true}")
    log.warn("power fail state is: ${powerFailState ?: 'previous'}")
    state.clear()
    unschedule()
    if (logEnable) runIn(1800,logsOff)
}

def configure() {
    logDebug('configure called')
    parent?.componentConfigure(this.device)
}

def refresh() {
    logDebug('refresh called')
    parent?.componentRefresh(this.device)
}

def on() {
    logDebug('on called')
    parent?.componentOn(this.device)
}

def off() {
    logDebug('off called')
    parent?.componentOff(this.device)
}

void parse(String description) {
    logDebug('parse called')
    log.warn 'parse(String description) not implemented'
}

void parse(List description) {
    description.each {
        if (it.name in ['switch']) {
            logText(it.descriptionText)
            sendEvent(getEvent(it))
        }
    }
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