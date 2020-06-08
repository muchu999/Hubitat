//---------------------------
// Settings for Venmar:
// [0, 0],   parameterNumber:68, size:2) 
// [0, 0],   parameterNumber:67, size:2) 
// [0, 0],   parameterNumber:66, size:2) 
// [0, 0],   parameterNumber:65, size:2) 
// [0, 0],   parameterNumber:64, size:2) 
// [0],      parameterNumber:63, size:1)    
// [0, 0],   parameterNumber:157, size:2)
// [0, 0],   parameterNumber:156, size:2)
// [0],      parameterNumber:155, size:1)   
// [0],      parameterNumber:154, size:1)   
// [0, 0],   parameterNumber:153, size:2)
// [0, 0],   parameterNumber:152, size:2)
// [10],     parameterNumber:151, size:1)  
// [10],     parameterNumber:150, size:1)  
// [0, 255], parameterNumber:54, size:2) 
// [0, 255], parameterNumber:52, size:2) 
// [0, 0],   parameterNumber:49, size:2) 
// [0, 255], parameterNumber:47, size:2) 
// [0],      parameterNumber:41, size:1)    
// [0],      parameterNumber:40, size:1)    
// [0],      parameterNumber:25, size:1)    
// [0],      parameterNumber:24, size:1)    
// [4],      parameterNumber:21, size:1)    
// [1],      parameterNumber:20, size:1)    
//
//---------------------------
metadata {
	definition (name: "Fibaro FGBS-222 Smart Implant", namespace: "christi999", author: "Jørn Lode") {
		capability "Configuration"
		capability "Actuator"
		capability "Refresh"        
        capability "Temperature Measurement"  


	    //command "getParameterReport", [[name:"parameterNumber",type:"NUMBER", description:"Parameter Number (omit for a complete listing of parameters that have been set)", constraints:["NUMBER"]]]
	    command "setMode",[[name:"mode",type:"ENUM", description:"HRV ventilation Mode", constraints:['Max Recirculation','OFF','Min Vent','Max Vent']]]
        
        attribute "mode", "string"     // 'Max Recirculation','OFF','Min Vent','Max Vent'
        attribute "temperatureSensorVoltage", "number"
        attribute "temperature", "number"
        attribute "trap", "string"


		fingerprint deviceId: "4096", inClusters: "0x5E,0x25,0x85,0x8E,0x59,0x55,0x86,0x72,0x5A,0x73,0x98,0x9F,0x5B,0x31,0x60,0x70,0x56,0x71,0x75,0x7A,0x6C,0x22"
	}


	preferences {
		generate_preferences(configuration_model())
        input name: "debugOutput",   type: "bool", title: "<b>Enable debug logging?</b>",   description: "<br>", defaultValue: true            
	}
}

//---------------------------
//
//---------------------------
def installed() {
	logDebug "installed"
	initialize()
}

//---------------------------
//
//---------------------------
def updated() {
	logDebug "updated"
	initialize()
    //dbCleanUp()
}
//---------------------------
//
//---------------------------
private dbCleanUp() {
  //  unschedule()
 // clean up state variables that are obsolete
    state.remove("trapSensorVoltage")
    state.remove("modeSensorVoltage")
    state.remove("ventilationMode")
    

}

def endOfTransition()
{
    state.inTransition = 0
}


//---------------------------
//
//---------------------------
private setMode(value)  
{
    log.info "Executing setMode value: $value"
	def cmds = []
	switch (value) {
        case "Max Recirculation":
			logDebug "Changing Ventilation Mode to Max Recirculation..."
		    cmds << toEndpoint(zwave.switchBinaryV1.switchBinarySet(switchValue: 0x00), 5).format()
		    cmds << toEndpoint(zwave.switchBinaryV1.switchBinarySet(switchValue: 0x00), 6).format()                
		    cmds << toEndpoint(zwave.switchBinaryV1.switchBinaryGet(), 5).format()
            cmds << toEndpoint(zwave.switchBinaryV1.switchBinaryGet(), 6).format()
            //cmds << toEndpoint(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x15), 3).format()
            //cmds << toEndpoint(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x15), 4).format()
            sendEvent(name: "mode", value: value, unit: "") 
            state.mode = value
            break
        case "OFF":
			logDebug "Changing Ventilation Mode to OFF..."
		    cmds << toEndpoint(zwave.switchBinaryV1.switchBinarySet(switchValue: 0x00), 5).format()
		    cmds << toEndpoint(zwave.switchBinaryV1.switchBinarySet(switchValue: 0xFF), 6).format()                
		    cmds << toEndpoint(zwave.switchBinaryV1.switchBinaryGet(), 5).format()
            cmds << toEndpoint(zwave.switchBinaryV1.switchBinaryGet(), 6).format()
            //cmds << toEndpoint(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x15), 3).format()
            //cmds << toEndpoint(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x15), 4).format()
            sendEvent(name: "mode", value: value, unit: "") 
            state.mode = value
        break        
        case "Min Vent":
			logDebug "Changing Ventilation Mode to Min Vent..."
		    cmds << toEndpoint(zwave.switchBinaryV1.switchBinarySet(switchValue: 0xFF), 5).format()
		    cmds << toEndpoint(zwave.switchBinaryV1.switchBinarySet(switchValue: 0x00), 6).format()                
		    cmds << toEndpoint(zwave.switchBinaryV1.switchBinaryGet(), 5).format()
            cmds << toEndpoint(zwave.switchBinaryV1.switchBinaryGet(), 6).format()
            //cmds << toEndpoint(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x15), 3).format()
            //cmds << toEndpoint(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x15), 4).format()
            sendEvent(name: "mode", value: value, unit: "")      
            state.mode = value
        break        
        case "Max Vent":
			logDebug "Changing Ventilation Mode to Max Vent..."
		    cmds << toEndpoint(zwave.switchBinaryV1.switchBinarySet(switchValue: 0xFF), 5).format()
		    cmds << toEndpoint(zwave.switchBinaryV1.switchBinarySet(switchValue: 0xFF), 6).format()                
		    cmds << toEndpoint(zwave.switchBinaryV1.switchBinaryGet(), 5).format()
            cmds << toEndpoint(zwave.switchBinaryV1.switchBinaryGet(), 6).format()
            //cmds << toEndpoint(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x15), 3).format()
            //cmds << toEndpoint(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x15), 4).format()
            sendEvent(name: "mode", value: value, unit: "") 
            state.mode = value
        break        
		default:
			log.warn "Ventilation Mode $value unsupported."
			break
       
	}
    
    state.inTransition = 1
	unschedule(endOfTransition)
	runIn(13, endOfTransition)
	return delayBetween(cmds, 1000)
    
}


//---------------------------
//
//---------------------------
private logDebug(msg) {
	if (settings?.debugOutput || settings?.debugOutput == null) {
		log.debug "$msg"
	}
}

//---------------------------
//
//---------------------------
private zwaveEvent(hubitat.zwave.commands.basicv1.BasicReport cmd)
{
    logDebug "BasicReport"

}

//---------------------------
//
//---------------------------
private zwaveEvent(hubitat.zwave.commands.basicv1.BasicSet cmd)
{
    logDebug "BasicSet"
    createEvent(name:"sensor", value: cmd.value ? "active" : "inactive")
}

//---------------------------
//
//---------------------------
private initialize() {

    //state.clear()
    state.Version = "0.1"

	if (!childDevices) {
		addChildDevices()
	}
    state.rerefreshCount = 0
	state.inTransition = 0
	unschedule(endOfTransition)
    
	formatCommands([
        zwave.versionV1.versionGet(),
        zwave.protectionV2.protectionSet(localProtectionState : 2, rfProtectionState: 0 ),
        zwave.associationV2.associationRemove(groupingIdentifier: 1, nodeId: zwaveHubNodeId),
		zwave.associationV2.associationRemove(groupingIdentifier: 2, nodeId: zwaveHubNodeId),
		zwave.associationV2.associationRemove(groupingIdentifier: 3, nodeId: zwaveHubNodeId),
        zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier: 1, nodeId: [zwaveHubNodeId]),
		zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier: 2, nodeId: [zwaveHubNodeId]),
        zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier: 3, nodeId: [zwaveHubNodeId]),
        
        // Set multichannel associations after they are cleared.
        // multiChannelAssociationSet starts with a list of nodeIds then the marker "0" followed by list of endpoints (node,ep)
        // In the case of the Fibaro, if a nodeId for group 1 is provided, it means the hub doesn't support 
        // multichannels so it won't send automatic reports. We therefore don't provide NodeIds and start directly with the marker "0"
        // The associations set here will also determine the kind of message signatures received by the hub,
        // such as notificationReport or basicSet with/without endpoints and or groups...
        zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: 1, nodeId: [0,1,1]),   // 
        zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: 2, nodeId: [0,1,1]),   // Used when IN1 input is triggered (using Basic Command Class).
        zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: 3, nodeId: [0,1,2]),   // Used when IN2 input is triggered (using Basic Command Class).     
	    zwave.associationV2.associationGet( groupingIdentifier: 1),	 
        zwave.associationV2.associationGet( groupingIdentifier: 2),	
        zwave.associationV2.associationGet( groupingIdentifier: 3),	
        zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier: 1),
        zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier: 2),
        zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier: 3)
        ], 500)
    
    
    
}

//---------------------------
//
//---------------------------
private zwaveEvent(hubitat.zwave.commands.versionv1.VersionCommandClassReport cmd) {
    logDebug "in version command class report"
    //if (state.debug)
    logDebug "---VERSION COMMAND CLASS REPORT V1--- ${device.displayName} has version: ${cmd.commandClassVersion} for command class ${cmd.requestedCommandClass} - payload: ${cmd.payload}"
}

// ----------------------------------------------------------------------------
// ------------------------------- CHILD DEVICES ------------------------------
// ----------------------------------------------------------------------------

//---------------------------
//
//---------------------------
private childNetworkId(ep) {
	"${device.deviceNetworkId}-ep${ep}"
}

//---------------------------
//
//---------------------------
private addChildDevices() {
	try {
		addChildSwitches()
        addChildAnalogInputs()
        addChildDigitalInputs()
        addChildTemperatureSensors()
	} catch (e) {
		sendEvent(
			descriptionText: "Child device creation failed.",
			eventType: "ALERT",
			name: "childDeviceCreation",
			value: e.toString(),
			displayed: true,
		)
	}
}


//---------------------------
// Digital inputs (EP 1 & 2)
//---------------------------
 private addChildDigitalInputs() {
 	(1..2).eachWithIndex { ep, index ->
 		addChildDevice("Fibaro FGBS-222 Child Digital Input",
			childNetworkId(ep), [componentLabel: "Input ${index+1} - Digital",
			completedSetup: true, label: "${device.displayName} - Digital Input ${index+1}",
			isComponent: true])                               
 	}
}


//---------------------------
// Analog inputs (EP 3 & 4)
//---------------------------
private addChildAnalogInputs() {
	(3..4).eachWithIndex { ep, index ->
		addChildDevice("Fibaro FGBS-222 Child Analog Input",
			childNetworkId(ep), [componentLabel: "Input ${index+1} - Analog",
			completedSetup: true, label: "${device.displayName} - Analog Input ${index+1}",
			isComponent: true])        
	}
}


//---------------------------
// Outputs (EP 5 & 6)
//---------------------------
private addChildSwitches() {
	(5..6).eachWithIndex { ep, index ->
		addChildDevice("Fibaro FGBS-222 Child Switch",
			childNetworkId(ep), [componentLabel: "Output ${index+1}",
			completedSetup: true, label: "${device.displayName} - Output ${index+1}",
			isComponent: true])
	}
}


//---------------------------
// Temperature sensors (EP 7-13)
//---------------------------
private addChildTemperatureSensors() {
	(7).eachWithIndex { ep, index ->
    addChildDevice("Fibaro FGBS-222 Child Temperature Sensor",
			childNetworkId(ep), [componentLabel: "Output ${index+1}",
			completedSetup: true, label: "${device.displayName} - Temperature ${index+1}",
			isComponent: true])  
    }
}

//---------------------------
//
//---------------------------
def refresh() {
    logDebug "HRV Refresh"

	if(!state.inTransition)
	{
		formatCommands([
    	//toEndpoint(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01), 7),
		toEndpoint(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x15), 4),
		toEndpoint(zwave.switchBinaryV1.switchBinaryGet(), 1)
    	], 500)
	}
	else
	{
		runIn(10, refresh)
	}
}

//---------------------------
//
//---------------------------
def updateTrapTemp() {
	formatCommands([toEndpoint(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x15), 4)], 500)
}


//---------------------------
//
//---------------------------
private setSwitch(value, channel) {
	def cmds = [
		toEndpoint(zwave.switchBinaryV1.switchBinarySet(switchValue: value), channel).format(),
		toEndpoint(zwave.switchBinaryV1.switchBinaryGet(), channel).format(),
	]

	if (value) {
		cmds + [
			"delay 3500",
			toEndpoint(zwave.switchBinaryV1.switchBinaryGet(), channel).format(),
		]
	} else {
		cmds
	}
}




//---------------------------
//
//---------------------------
private zwaveEvent(hubitat.zwave.commands.multichannelassociationv2.MultiChannelAssociationReport cmd) {
    log.info "MultiChannelAssociationReport- groupingIdentifier:${cmd.groupingIdentifier}, maxNodesSupported:${cmd.maxNodesSupported}, nodes:${cmd.nodeId}"
    log.info "MultiChannelAssociationReport- $cmd"
}


//---------------------------
//
//---------------------------
def parse(String description) {
	def result = []

	def cmd = zwave.parse(description)
	if (cmd) {
		result += zwaveEvent(cmd)
		logDebug "Parsed ${cmd} to ${result.inspect()}"
	} else {
		logDebug "Non-parsed event: ${description}"
	}

	result
}

//---------------------------
//
//---------------------------
private def zwaveEvent(hubitat.zwave.commands.notificationv3.NotificationReport cmd,ep) {
    logDebug "----notification type: ${cmd.notificationType} $cmd"
    def result = []
	def target = childDevices.find { it.deviceNetworkId == childNetworkId(ep) }  
    if (cmd.notificationType == 7) {
    //  spec says type 7 is 'Home Security'
        switch (cmd.event) {
            case 0:
            //  spec says this is 'clear previous alert'
				target?.sendEvent(name: "contact", value: "open") 
                sendEvent(name: "trap", value: "closed", descriptionText: "$device.displayName is closed", displayed: true)
                state.trap = "closed"
                log.info "Trap closed"
                if( (state.mode=="Min Vent") || (state.mode=="Max Vent"))
                {
                    log.info "Trap closed in vent mode, defrost cycle activated"
                }

                break
            case 2:
            //  spec says this is 'tamper'
				target?.sendEvent(name: "contact", value: "closed")   
                sendEvent(name: "trap", value: "open", descriptionText: "$device.displayName is open", displayed: true)
                state.trap = "open"
                log.info "Trap opened"
                break
            default:
                break
        }
    } else {
        log.warn "Need to handle this cmd.notificationType: ${cmd.notificationType}"
        result << createEvent(descriptionText: cmd.toString())
    }
    result    
}


//---------------------------
//
//---------------------------
private zwaveEvent(hubitat.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand()
	if (encapsulatedCommand) {
		logDebug "Not Ignored encapsulated command: ${cmd}"        
		zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint)
	} else {
		logDebug "Ignored encapsulated command: ${cmd}"
	}
}


//---------------------------
//
//---------------------------
private zwaveEvent(hubitat.zwave.commands.multichannelv4.MultiChannelCmdEncap cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand()
	if (encapsulatedCommand) {
		logDebug "Not Ignored encapsulated command: ${cmd}"        
		zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint)
	} else {
		logDebug "Ignored encapsulated command: ${cmd}"
	}
}


//---------------------------
//
//---------------------------
private zwaveEvent(hubitat.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, ep) {
    logDebug "----SwitchBinaryReport: ${cmd} "    
	def target = childDevices.find { it.deviceNetworkId == childNetworkId(ep) }
	target?.sendEvent(name: "switch", value: cmd.value ? "on" : "off")
}

// CONFIGURATION REPORT
//---------------------------
//
//---------------------------
private zwaveEvent(hubitat.zwave.commands.configurationv1.ConfigurationReport cmd) {
	def configuration = new XmlSlurper().parseText(configuration_model())

	def paramName = cmd.parameterNumber.toString()
	def parameterInfo = configuration.Value.find { it.@index == paramName }
	def byteSize = sizeOfParameter(parameterInfo)
	if (byteSize != cmd.size) {
		log.warn "Parameter ${paramName} has unexpected size ${cmd.size} (expected ${byteSize})"
	}

	def remoteValue = bytesToInteger(cmd.configurationValue)
	def localValue = settings[cmd.parameterNumber.toString()].toInteger()

	if (localValue != remoteValue) {
		log.warn "Parameter ${paramName} has value ${remoteValue.inspect()} after trying to set it to ${localValue.inspect()}"
	}
}


// MULTISENSOR REPORT
//---------------------------
//
//---------------------------
private zwaveEvent(hubitat.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd, ep) {
	logDebug "Sensor @ endpoint ${ep} has value ${cmd.scaledSensorValue}"
    
    def target = childDevices.find { it.deviceNetworkId == childNetworkId(ep) }  
    if(ep==7)
    {
        //target?.sendEvent(name: "temperature", value: cmd.scaledSensorValue)
    }
    else if(ep==3)
    {
        //target?.sendEvent(name: "voltage", value: cmd.scaledSensorValue)
        //sendEvent(name: "trapSensorVoltage", value: cmd.scaledSensorValue, unit: "v")           
        //state.trapSensorVoltage = cmd.scaledSensorValue

    }
    else if(ep==4)
    {
        target?.sendEvent(name: "voltage", value: cmd.scaledSensorValue)
        sendEvent(name: "temperatureSensorVoltage", value: cmd.scaledSensorValue, unit: "v")  
        
        state.temperatureSensorVoltage = cmd.scaledSensorValue
        def temp = (cmd.scaledSensorValue*(-68.8)/3.19+198.81-32)*5/9
        temp = Math.round(temp * 10) / 10
        if(temp < -30.0) 
        {
            if(state.rerefreshCount<3)
            {
                log.info "ReRefresh trap temperature"
                runIn(5, updateTrapTemp)
                state.rerefreshCount += 1
            }
            else
            {

                if(state.rerefreshCount<100)
                {
                    log.warn "ReRefresh failed"
                }
                state.rerefreshCount = 100
            }
        }
        else
        {
            state.rerefreshCount = 0
            sendEvent(name: "temperature", value:temp , unit: "C")  
            state.temperature = temp
        }        
    }
	//createEvent(name: "temperature", value: cmd.scaledSensorValue)
}


//---------------------------
//
//---------------------------
private zwaveEvent(hubitat.zwave.commands.sensormultilevelv1.SensorMultilevelReport cmd) {
   logDebug "SensorMultilevelReport $cmd"
 }

//---------------------------
//
//---------------------------
private zwaveEvent(hubitat.zwave.Command cmd, ep=null) {
	logDebug "Unhandled event ${cmd} (endpoint ${ep})"
	createEvent(descriptionText: "${device.displayName}: ${cmd}")
}


// ----------------------------------------------------------------------------
// ------------------------------ CONFIGURATION -------------------------------
// ----------------------------------------------------------------------------

//---------------------------
//
//---------------------------
def configure() {
	def configuration = new XmlSlurper().parseText(configuration_model())
	def cmds = []

	configuration.Value.each {
		def settingValue = settings[it.@index.toString()].toInteger()
		def byteSize = sizeOfParameter(it)

		if (settingValue != null) {
			if (settingValue == "") {
				logDebug "Setting ${it.@index} is empty"
			}

			def index = it.@index.toInteger()
			cmds << zwave.configurationV1.configurationSet(configurationValue: integerToBytes(settingValue, byteSize), parameterNumber: index, size: byteSize)
			cmds << zwave.configurationV1.configurationGet(parameterNumber: index)
		} else {
			logDebug "Setting ${it.@index} has null value"
		}
	}
	
	logDebug "cmds: ${cmds}"
	
	formatCommands(cmds)
}

//---------------------------
//
//---------------------------
private generate_preferences(configuration_model) {
	def configuration = new XmlSlurper().parseText(configuration_model)

	configuration.Value.each {
		switch(it.@type) {
			case ["byte", "short", "four"]:
				input "${it.@index}", "number",
					title: "<b>${it.@label}: </b>",
					description: "${it.Help}",                    
					range: "${it.@min}..${it.@max}",
					defaultValue: "${it.@value}",
					displayDuringSetup: "${it.@displayDuringSetup}"
				break
			case "list":
				// def items = []
				// it.Item.each { items << ["${it.@value}": "${it.@label}"] }
				def items = it.Item.collect { ["${it.@value}": "${it.@label}"] }
				input "${it.@index}", "enum",
				    title: "<b>${it.@label}</b>",
					description: "${it.Help}",
					defaultValue: "${it.@value}",
					displayDuringSetup: "${it.@displayDuringSetup}",
					options: items
				break
			case "decimal":
				input "${it.@index}", "decimal",
					title: "<b>${it.@label}</b>",
					description: "${it.Help}",
					range: "${it.@min}..${it.@max}",
					defaultValue: "${it.@value}",
					displayDuringSetup: "${it.@displayDuringSetup}"
				break
			case "boolean":
				input "${it.@index}", "bool",
					title: it.@label != "" ? "${it.@label}\n" + "${it.Help}" : "" + "${it.Help}",
					defaultValue: "${it.@value}",
					displayDuringSetup: "${it.@displayDuringSetup}"
				break
			case "paragraph":
				input title: "${it.@label}",
                    description: "${it.Help}",
					type: "paragraph",
					element: "paragraph"
				break
		}
	}
}

//---------------------------
//
//---------------------------
private configuration_model() {
	'''
<configuration>
	<Value type="list" genre="config" instance="1" index="20" label="Input 1 - operating mode" value="2" size="1">
		<Help>This parameter allows to choose mode of 1st input (IN1). Change it depending on connected device.</Help>
		<Item label="Normally closed alarm input (Notification)" value="0" />
		<Item label="Normally open alarm input (Notification)" value="1" />
		<Item label="Monostable button (Central Scene)" value="2" />
		<Item label="Bistable button (Central Scene)" value="3" />
		<Item label="Analog input without internal pull-up (Sensor Multilevel)" value="4" />
		<Item label="Analog input with internal pullup (Sensor Multilevel)" value="5" />
	</Value>

	<Value type="list" genre="config" instance="1" index="21" label="Input 2 - operating mode" value="2" size="1">
		<Help>This parameter allows to choose mode of 2nd input (IN2). Change it depending on connected device.</Help>
		<Item label="Normally closed alarm input (Notification)" value="0" />
		<Item label="Normally open alarm input (Notification)" value="1" />
		<Item label="Monostable button (Central Scene)" value="2" />
		<Item label="Bistable button (Central Scene)" value="3" />
		<Item label="Analog input without internal pull-up (Sensor Multilevel)" value="4" />
		<Item label="Analog input with internal pullup (Sensor Multilevel)" value="5" />
	</Value>

	<Value type="list" genre="config" instance="1" index="24" label="Inputs orientation" value="0" size="1">
		<Help>This parameter allows reversing operation of IN1 and IN2 inputs without changing the wiring. Use in case of incorrect wiring.</Help>
		<Item label="default (IN1 - 1st input, IN2 - 2nd input)" value="0" />
		<Item label="reversed (IN1 - 2nd input, IN2 - 1st input)" value="1" />
	</Value>

	<Value type="list" genre="config" instance="1" index="25" label="Outputs orientation" value="0" size="1">
		<Help>This parameter allows reversing operation of OUT1 and OUT2 inputs without changing the wiring. Use in case of incorrect wiring.</Help>
		<Item label="default (OUT1 - 1st output, OUT2 - 2nd output)" value="0" />
		<Item label=" reversed (OUT1 - 2nd output, OUT2 - 1st output)" value="1" />
	</Value>

	<Value type="list" genre="config" instance="1" index="40" label="Input 1 - sent scenes" value="0" size="1">
		<Help>This parameter defines which actions result in sending scene ID and attribute assigned to them. Parameter is relevant only if parameter 20 is set to 2 or 3</Help>
		<Item label="No scenes sent" value="0" />
		<Item label="Key pressed 1 time" value="1" />
		<Item label="Key pressed 2 times" value="2" />
		<Item label="Key pressed 3 times" value="4" />
		<Item label="Key hold down and key released" value="8" />
	</Value>

	<Value type="list" genre="config" instance="1" index="41" label="Input 2 - sent scenes" value="0" size="1">
		<Help>This parameter defines which actions result in sending scene ID and attribute assigned to them. Parameter is relevant only if parameter 21 is set to 2 or 3.</Help>
		<Item label="No scenes sent" value="0" />
		<Item label="Key pressed 1 time" value="1" />
		<Item label="Key pressed 2 times" value="2" />
		<Item label="Key pressed 3 times" value="4" />
		<Item label="Key hold down and key released" value="8" />
	</Value>

	<Value type="short" genre="config" instance="1" index="47" label="Input 1 - value sent to 2nd association group when activated" min="0" max="255" value="255">
		<Help>
			This parameter defines value sent to devices in 2nd association group when IN1 input is triggered (using Basic Command Class).
			Available settings: 0-255.
			Default setting: 255.
		</Help>
	</Value>

	<Value type="short" genre="config" instance="1" index="49" label="Input 1 - value sent to 2nd association group when deactivated" min="0" max="255" value="255">
		<Help>
			This parameter defines value sent to devices in 2nd association group when IN1 input is deactivated (using Basic Command Class).
			Available settings: 0-255.
			Default setting: 255.
		</Help>
	</Value>

	<Value type="short" genre="config" instance="1" index="52" label="Input 2 - value sent to 3rd association group when activated" min="0" max="255" value="255">
		<Help>
			This parameter defines value sent to devices in 3rd association group when IN2 input is triggered (using Basic Command Class).
			Available settings: 0-255.
			Default setting: 255.
		</Help>
	</Value>

	<Value type="short" genre="config" instance="1" index="54" label="Input 2 - value sent to 3rd association group when deactivated" min="0" max="255" value="255">
		<Help>
			This parameter defines value sent to devices in 3rd association group when IN2 input is deactivated (using Basic Command Class).
			Available settings: 0-255.
			Default setting: 255.
		</Help>
	</Value>

	<Value type="byte" genre="config" instance="1" index="150" label="Input 1 - sensitivity" min="1" max="100" value="10">
		<Help>
			This parameter defines the inertia time of IN1 input in alarm modes.
			Adjust this parameter to prevent bouncing or signal disruptions. Parameter is relevant only if parameter 20 is set to 0 or 1 (alarm mode).
			Available settings: 1-100 (10ms-1000ms, 10ms step).
			Default setting: 10 (100ms).
		</Help>
	</Value>

	<Value type="byte" genre="config" instance="1" index="151" label="Input 2 - sensitivity" min="1" max="100" value="10">
		<Help>
			This parameter defines the inertia time of IN2 input in alarm modes.
			Adjust this parameter to prevent bouncing or signal disruptions. Parameter is relevant only if parameter 21 is set to 0 or 1 (alarm mode).
			Available settings: 1-100 (10ms-1000ms, 10ms step).
			Default setting: 10 (100ms).
		</Help>
	</Value>

	<Value type="short" genre="config" instance="1" index="152" label="Input 1 - delay of alarm cancellation" min="0" max="3600" value="0">
		<Help>
			This parameter defines additional delay of cancelling the alarm on IN1 input. Parameter is relevant only if parameter 20 is set to 0 or 1 (alarm mode).
			Available settings:
			0 - no delay.
			1-3600s.
			Default setting: 0 (no delay).
		</Help>
	</Value>

	<Value type="short" genre="config" instance="1" index="153" label="Input 2 - delay of alarm cancellation" min="0" max="3600" value="0">
		<Help>
			This parameter defines additional delay of cancelling the alarm on IN2 input. Parameter is relevant only if parameter 21 is set to 0 or 1 (alarm mode).
			Available settings:
			0 - no delay.
			1-3600s.
			Default setting: 0 (no delay).
		</Help>
	</Value>

	<Value type="list" genre="config" instance="1" index="154" label="Output 1 - logic of operation" value="0" size="1">
		<Help>This parameter defines logic of OUT1 output operation.</Help>
		<Item label="contacts normally open" value="0" />
		<Item label="contacts normally closed" value="1" />
	</Value>

	<Value type="list" genre="config" instance="1" index="155" label="Output 2 - logic of operation" value="0" size="1">
		<Help>This parameter defines logic of OUT2 output operation.</Help>
		<Item label="contacts normally open" value="0" />
		<Item label="contacts normally closed" value="1" />
	</Value>

	<Value type="short" genre="config" instance="1" index="156" label="Output 1 - auto off" min="0" max="27000" value="0">
		<Help>
			This parameter defines time after which OUT1 will be automatically deactivated.
			Available settings:
			0 - auto off disabled.
			1-27000 (0.1s-45min, 0.1s step).
			Default setting: 0 (auto off disabled).
		</Help>
	</Value>

	<Value type="short" genre="config" instance="1" index="157" label="Output 2 - auto off" min="0" max="27000" value="0">
		<Help>
			This parameter defines time after which OUT2 will be automatically deactivated.
			Available settings:
			0 - auto off disabled.
			1-27000 (0.1s-45min, 0.1s step).
			Default setting: 0 (auto off disabled).
		</Help>
	</Value>

	<Value type="byte" genre="config" instance="1" index="63" label="Analog inputs - minimal change to report" min="0" max="100" value="5">
		<Help>
			This parameter defines minimal change (from the last reported) of
			analog input value that results in sending new report. Parameter is
			relevant only for analog inputs (parameter 20 or 21 set to 4 or 5).
			Available settings:
			0 - (reporting on change disabled).
			1-100 (0.1-10V, 0.1V step).
			Default setting: 5 (0.5V).
		</Help>
	</Value>

	<Value type="short" genre="config" instance="1" index="64" label="Analog inputs - periodical reports" min="0" max="32400" value="0">
		<Help>
			This parameter defines reporting period of analog inputs value.
			Periodical reports are independent from changes in value (parameter 63). Parameter is relevant only for analog inputs (parameter
			20 or 21 set to 4 or 5).
			Available settings:
			0 (periodical reports disabled).
			60-32400 (60s-9h).
			Default setting: 0 (periodical reports disabled).
		</Help>
	</Value>

	<Value type="short" genre="config" instance="1" index="65" label="Internal temperature sensor - minimal change to report" min="0" max="255" value="5">
		<Help>
			This parameter defines minimal change (from the last reported)
			of internal temperature sensor value that results in sending new
			report.
			Available settings:
			0 - (reporting on change disabled).
			1-255 (0.1-25.5C).
			Default setting: 5 (0.5C).
		</Help>
	</Value>

	<Value type="short" genre="config" instance="1" index="66" label="Internal temperature sensor - periodical reports" min="0" max="32400" value="0">
		<Help>
			This parameter defines reporting period of internal temperature
			sensor value. Periodical reports are independent from changes in
			value (parameter 65).
			Available settings:
			0 (periodical reports disabled).
			60-32400 (60s-9h).
			Default setting: 0 (periodical reports disabled).
		</Help>
	</Value>

	<Value type="short" genre="config" instance="1" index="67" label="External sensors - minimal change to report" min="0" max="255" value="5">
		<Help>
			This parameter defines minimal change (from the last reported) of
			external sensors values (DS18B20 or DHT22) that results in sending new
			report. Parameter is relevant only for connected DS18B20 or DHT22
			sensors.
			Available settings:
			0 - (reporting on change disabled).
			1-255 (0.1-25.5 units).
			Default setting: 5 (0.5 units)
		</Help>
	</Value>

	<Value type="short" genre="config" instance="1" index="68" label="External sensors - periodical reports" min="0" max="32400" value="0">
		<Help>
			This parameter defines reporting period of analog inputs value.
			Periodical reports are independent from changes in value (parameter 67).
			Parameter is relevant only for connected DS18B20 or DHT22 sensors.
			Available settings:
			0 - (periodical reports disabled).
			60-32400 (60s-9h).
			Default setting: 0 (periodical reports disabled).
		</Help>
	</Value>
</configuration>
	'''
}

// ----------------------------------------------------------------------------
// --------------------------------- HELPERS ----------------------------------
// ----------------------------------------------------------------------------

//---------------------------
//
//---------------------------
private toEndpoint(cmd, endpoint) {
	zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint: endpoint)
		.encapsulate(cmd)
}


//---------------------------
//
//---------------------------
private formatCommands(cmds, delay=null) {
	def formattedCmds = cmds.collect { it.format() }

	if (delay) {
		delayBetween(formattedCmds, delay)
	} else {
		formattedCmds
	}
}
	
//---------------------------
//
//---------------------------
private bytesToInteger(array) {
	array.inject(0) { result, i -> (result << 8) | i }
}

//---------------------------
//
//---------------------------
private integerToBytes(value, length) {
	(length-1..0).collect { (value >> (it * 8)) & 0xFF }
}

//---------------------------
//
//---------------------------
private sizeOfParameter(paramData) {
	switch (paramData.@type) {
		case "short":
			2
			break
		case "four":
			4
			break
		case "list":
			paramData.@size.toInteger()
			break
		default:
			1
			break
	}
}

//---------------------------
//
//---------------------------
private channelNumber(String dni) {
	dni.split("-ep")[-1].toInteger()
}
