/*
* Fibaro FGBS-222 Smart Implant 
*
* Description:
*
* Required Information:
*
* Features List:
* 
* Licensing:
*
* Version Control:
* 1.7.4 - Adding flexibility/capabilities to child analog input
* 1.7.3 - First attempt at fixing C7 issues
* 1.7.2 - Merged changes to Z-wave security from jabbera
* 1.7.1 - Adding reinstall command to erase child devices and clear state variables
* 1.7.0 - Adding support for Hubitat Package Manager
* 1.6   - Update after HUB update broke compatibility
* 1.5   - Added support for Z-WAVE secure mode
* 1.4   - Added Local/RF protection individually for in/out 1 and in/out 2
* 1.3   - Stupid double configuration mistake
* 1.2   - Removed child commands from parent device and corrected code accordingly using sendHubCommand().
*         Corrected state variable handling for driver version and number of external sensors
* 1.1   - Fixed state variables handling (version was not updated)
* 1.0   - Changed description strings
* 0.9   - Added temperature calibration offsets for sensors
* 0.8   - Corrected incorrect text desctiption of RF protection
* 0.7   - Corrected name of child device for event log of temperature
* 0.6   - Added some temperature scale handling and driver version info
* 0.5   - Moved all device commands to "configure", some where sent during "save preferences"
* 0.4   - Adding "ContactSensor" capability to digital Input
* 0.3   - Changing format of preferences display and fixed code tab/spaces
* 0.2   - Added device protection settings
* 0.1   - Initial design, based on @boblehest Githubcode
* 
* Thank you(s):
* This code is based on the original design from @boblehest on Github
*/

public static String version()      {  return "1.7.4"  }
metadata {
	definition (name: "Fibaro FGBS-222 Smart Implant", namespace: "christi999", author: "", importUrl: "https://raw.githubusercontent.com/muchu999/Hubitat/master/Fibaro%20FBGS-222%20Smart%20Implant/Fibaro%20FBGS-222%20Smart%20Implant.groovy") {	
		command( "Reinstall", [["name":"Confirmation*",	"description":"Choose Yes to confirm reinstalling the driver, child devices and state variables will be erased", "type":"ENUM", "constraints":["no","yes"]]])		
		command( "CheckConfig" )
		capability "Configuration"
		
		attribute "extSensorChildCount",  "number" 
		attribute "driverVersion",        "string"   
         
		fingerprint deviceId: "4096", inClusters: "0x5E,0x25,0x85,0x8E,0x59,0x55,0x86,0x72,0x5A,0x73,0x98,0x9F,0x5B,0x31,0x60,0x70,0x56,0x71,0x75,0x7A,0x6C,0x22"
	}

	preferences {
		generate_preferences(configuration_model())
		input name:"sensorOffset0",  type:"decimal", title:"<b>Internal Sensor Temp Offset</b>", description:"degrees", defaultValue:0.0, range: "-7..7"
		input "extSensorCount", "enum", title: "<b>Number of External Sensors?</b>", options: ["0","1","2","3","4","5","6"], defaultValue: "0", required: false
		input name:"sensorOffset1", type:"decimal", title:"<b>External Sensor 1 Temp Offset</b>", description:"degrees", defaultValue:0.0, range: "-7..7"
		input name:"sensorOffset2", type:"decimal", title:"<b>External Sensor 2 Temp Offset</b>", description:"degrees", defaultValue:0.0, range: "-7..7"
		input name:"sensorOffset3", type:"decimal", title:"<b>External Sensor 3 Temp Offset</b>", description:"degrees", defaultValue:0.0, range: "-7..7"
		input name:"sensorOffset4", type:"decimal", title:"<b>External Sensor 4 Temp Offset</b>", description:"degrees", defaultValue:0.0, range: "-7..7"
		input name:"sensorOffset5", type:"decimal", title:"<b>External Sensor 5 Temp Offset</b>", description:"degrees", defaultValue:0.0, range: "-7..7"
		input name:"sensorOffset6", type:"decimal", title:"<b>External Sensor 6 Temp Offset</b>", description:"degrees", defaultValue:0.0, range: "-7..7"
		input "localProtection1", "enum", title: "<b>Input/Output 1 - Local Device Protection?</b>", description: "0:Unprotected, 2:State of output cannot be changed by the B-button or corresponding Input", options: ["0","2"], defaultValue: "0", required: true
		input "rfProtection1", "enum", title: "<b>Input/Output 1 - RF Device Protection?</b>", description: "0:Unprotected, 1:No RF control – command class basic and switch binary are rejected, every other command classwill be handled", options: ["0","1"], defaultValue: "0", required: true
		input "localProtection2", "enum", title: "<b>Input/Output 2 - Local Device Protection?</b>", description: "0:Unprotected, 2:State of output cannot be changed by the B-button or corresponding Input", options: ["0","2"], defaultValue: "0", required: true
		input "rfProtection2", "enum", title: "<b>Input/Output 2 - RF Device Protection?</b>", description: "0:Unprotected, 1:No RF control – command class basic and switch binary are rejected, every other command classwill be handled", options: ["0","1"], defaultValue: "0", required: true
		input "tempUnits", "enum", title: "<b>Temperature Units?</b>", description: "default: The units used by your hub", options: ["default","F","C"], defaultValue: "default", required: true
		input name: "debugOutput",   type: "bool", title: "<b>Enable debug logging?</b>",   description: "<br>", defaultValue: true               
	}
}

def Reinstall(confirm) {
    logDebug "Reinstall()"
	if(confirm == "yes") {
		logDebug "Proceeding with reinstall"
		removeChildDevices(getChildDevices())
		state.clear()
		installed()
	}
}

def removeChildDevices(delete) {
	delete.each {deleteChildDevice(it.deviceNetworkId)}
}


def installed() {
	logDebug "installed"
	state.driverVersion = "${version()}"
	state.extSensorChildCount = 0
	initialize()
}

def updated() {
	logDebug "updated"
	initialize()
}


private initialize() {
	state.driverVersion = "${version()}"
	if (!childDevices) {
		addChildDevices()
		debugOutput = 1
	}
	updateChildTemperatureSensors()
}


// ----------------------------------------------------------------------------
// ------------------------------- CHILD DEVICES ------------------------------
// ----------------------------------------------------------------------------
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
		logDebug("Child device creation failed")
		sendEvent(
			descriptionText: "Child device creation failed",
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
// Temperature sensors (EP 7)
//---------------------------
private addChildTemperatureSensors() {
	(7).eachWithIndex { ep, index ->
	addChildDevice("Fibaro FGBS-222 Child Temperature Sensor",
			childNetworkId(ep), [componentLabel: "Internal temperature sensor",
			completedSetup: true, label: "${device.displayName} - Temperature ${index+1}",
			isComponent: true])  
	}
	updateChildTemperatureSensors()
}

//---------------------------
// Temperature sensors (EP 8-13)
//---------------------------
private updateChildTemperatureSensors() {
	ns = extSensorCount.toInteger()
	if(!state.extSensorChildCount)
		state.extSensorChildCount=0
	
	if(ns < state.extSensorChildCount) {
		((8+ns)..(7 + state.extSensorChildCount)).eachWithIndex { ep, index -> 
		deleteChildDevice(childNetworkId(ep))
		}
	}
	else if (ns > state.extSensorChildCount) {
		((8 + (state.extSensorChildCount).toInteger())..(7+ns)).eachWithIndex { ep, index ->
		addChildDevice("Fibaro FGBS-222 Child Temperature Sensor",
			childNetworkId(ep), [componentLabel: "External temperature sensor",
			completedSetup: true, label: "${device.displayName} - Temperature ${index+2}",
			isComponent: true])  
		}
	}
	state.extSensorChildCount = ns 
}



//---------------------------
// 
//---------------------------
private childRefresh(String dni) {
	def ep = channelNumber(dni).toInteger()
	logDebug "childRefresh, ep=$ep"   
    
	switch(ep) {
		case 1:
		case 2:
			// No way to refresh this???
			//formatCommands([
			//], 500)
			break;
		case 3:
		case 4:
			formatCommands([
				toEndpoint(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x15), ep),              
			], 500 )
			break;
		case 5:
		case 6:
			formatCommands([
			toEndpoint(zwave.switchBinaryV1.switchBinaryGet(), ep)
			], 500 )
			break;
		case 7..13:
			formatCommands([
     			toEndpoint(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01), ep)
			], 500)

			break;
	}
 }

//---------------------------
//
//---------------------------
private setSwitch(value, channel) {
	def cmds = []
	cmds << toEndpoint(zwave.switchBinaryV1.switchBinarySet(switchValue: value), channel)
	cmds <<	toEndpoint(zwave.switchBinaryV1.switchBinaryGet(), channel)
	formatCommands(cmds,500)
}

//---------------------------
//
//---------------------------
def childOn(String dni) {
	def ep = channelNumber(dni)
	logDebug "Child on @ ep ${ep}"
	setSwitch(0xff, ep)
}

//---------------------------
//
//---------------------------
def childOff(String dni) {
	def ep = channelNumber(dni)
	logDebug "Child off @ ep ${ep}"
	setSwitch(0, ep)
}

// ----------------------------------------------------------------------------
// ----------------------------- MESSAGE PARSING ------------------------------
// ----------------------------------------------------------------------------
//---------------------------
//
//---------------------------
def parse(String description) {
	def result = []

	def cmd = zwave.parse(description)
	if (cmd) {
		result += zwaveEvent(cmd)
		//logDebug "Parsed ${cmd} to ${result.inspect()}"
	} else {
		log.warn "Non-parsed event: ${description}"
	}

	result
}

//---------------------------
//
//---------------------------
private zwaveEvent(hubitat.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand()
	if (encapsulatedCommand) {
		if(cmd.destinationEndPoint != 0) {
			zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint, cmd.destinationEndPoint)
 		}
 		else {
			zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint)
		}
	} 
	else {
		log.warn "Ignored encapsulated command: ${cmd}"
	}
}

//---------------------------
//
//---------------------------
private zwaveEvent(hubitat.zwave.commands.multichannelv4.MultiChannelCmdEncap cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand()
	if (encapsulatedCommand) {
		if(cmd.destinationEndPoint != 0) {
			zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint, cmd.destinationEndPoint)
 		}
 		else {
			zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint)
		}
	} 
	else {
		log.warn "Ignored encapsulated command: ${cmd}"
	}
}


//---------------------------
//
//---------------------------
private zwaveEvent(hubitat.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, ep) {
	def target = childDevices.find { it.deviceNetworkId == childNetworkId(ep) }
	target?.sendEvent(name: "switch", value: cmd.value ? "on" : "off")
	logDebug "SwitchBinaryReport - ep=$ep $target $cmd "
}

//---------------------------
//
//---------------------------
private zwaveEvent(hubitat.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, ep, sp) {
	def target = childDevices.find { it.deviceNetworkId == childNetworkId(ep) }
	target?.sendEvent(name: "switch", value: cmd.value ? "on" : "off")
	logDebug "SwitchBinaryReport - ep=$ep sp=$sp $target $cmd "
}

//---------------------------
//
//---------------------------
private zwaveEvent(hubitat.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd, ep) {
	def target = childDevices.find { it.deviceNetworkId == childNetworkId(ep) }  
	logDebug "Sensor @ endpoint ${ep} has value ${cmd.scaledSensorValue} - ep=$ep $target $cmd "
	
	endpoint = ep.toInteger() 
	switch(endpoint) {
		case 1..2:
			target?.sendEvent(name: "contact", value: cmd.scaledSensorValue)
			break
		case 3..4:
			unit = "v"
			target?.parse([[name:"voltage", value:cmd.scaledSensorValue, descriptionText:"${target} voltage is ${cmd.scaledSensorValue}${unit}", unit: unit]])
		break
		case 7..13:
			(finalVal,units) = convertTemperature(cmd)
			finalVal = finalVal.toFloat() + settings["${"sensorOffset" + (endpoint-7).toString()}"]
			finalVal = Math.round(finalVal* 10.0)/10.0
			target?.sendEvent(name: "temperature", value: finalVal, unit: units, descriptionText:"${target} temperature is ${finalVal}${units}" )
			break
	}
}


//---------------------------
//
//---------------------------
private zwaveEvent(hubitat.zwave.commands.sensormultilevelv11.SensorMultilevelReport cmd, ep, sp) {
    
	def target = childDevices.find { it.deviceNetworkId == childNetworkId(ep) }  
	logDebug "Sensor @ endpoint ${ep} has value ${cmd.scaledSensorValue} - ep=$ep sp=$sp $target $cmd "
	
	endpoint = ep.toInteger() 
	switch(endpoint) {
		case 1..2:
			target?.sendEvent(name: "contact", value: cmd.scaledSensorValue)
			break
		case 3..4:
			unit = "v"
			target?.parse([[name:"voltage", value:cmd.scaledSensorValue, descriptionText:"${target} voltage is ${cmd.scaledSensorValue}${unit}", unit: unit]])
			break
		case 7..13:
			(finalVal,units) = convertTemperature(cmd)
			finalVal = finalVal.toFloat() + settings["${"sensorOffset" + (endpoint-7).toString()}"]
			finalVal = Math.round(finalVal* 10.0)/10.0
			target?.sendEvent(name: "temperature", value: finalVal, unit: units, descriptionText:"${target} temperature is ${finalVal}${units}" )
			break
	}
}


//---------------------------
//
//---------------------------
private zwaveEvent(hubitat.zwave.commands.basicv2.BasicSet cmd) {
	logDebug "BasicSet: $cmd"        
}


//---------------------------
//
//---------------------------
private zwaveEvent(hubitat.zwave.commands.basicv2.BasicSet cmd, ep) {
	def target = childDevices.find { it.deviceNetworkId == childNetworkId(ep) }  
	logDebug "$target $cmd ep= $ep"
	switch(ep.toInteger()) {
		case 0:
			target?.sendEvent(name: "value", value: cmd.value)
			break        
		case 1..2:
			target?.sendEvent(name: "value", value: cmd.value)
			break
		default:
			log.warn "Unsupported endpoint for BasicSet cmd=$cmd ep=$ep"
			break
	}
}

//---------------------------
// Needs rework
//---------------------------
private zwaveEvent(hubitat.zwave.commands.basicv2.BasicSet cmd, ep, gr) {
	logDebug "BasicSet: $cmd ep=$ep gr=$gr"
	if(ep==0) {
		switch(gr) {
			case 2: 
				target = childDevices.find { it.deviceNetworkId == childNetworkId(1)}
				target?.sendEvent(name: "value", value: cmd.value)                                        
				break
			case 3:
				target = childDevices.find { it.deviceNetworkId == childNetworkId(2)}
				target?.sendEvent(name: "value", value: cmd.value)                                        
				break
			default:
				log.warn "Unsupported ep value for BasicSet cmd=$cmd ep2=$ep2"
				break            
		}
	}
}



//---------------------------
//
//---------------------------
private def zwaveEvent(hubitat.zwave.commands.notificationv3.NotificationReport cmd) {
	logDebug "NotificationReport V3: $cmd"

	def target = childDevices.find { it.deviceNetworkId == childNetworkId(ep) }  
	def result = []
	if (cmd.notificationType == 7) {
		//  spec says type 7 is 'Home Security'
		switch (cmd.event) {
			case 0:
				//  spec says this is 'clear previous alert'
				target?.sendEvent(name: "contact", value: "open") 
				break
			case 2:
				//  spec says this is 'tamper'
				target?.sendEvent(name: "contact", value: "closed")     
				break
			default:
				break
		}
	} 
	else {
		log.warn "Need to handle this cmd.notificationType: ${cmd.notificationType}"
		result << createEvent(descriptionText: cmd.toString())
	}
	result    
}


//---------------------------
//
//---------------------------
private def zwaveEvent(hubitat.zwave.commands.notificationv8.NotificationReport cmd,ep) {
	logDebug "NotificationReport V8: ep=$ep $cmd"
    
	def target = childDevices.find { it.deviceNetworkId == childNetworkId(ep) }  
	def result = []
	if (cmd.notificationType == 7) {
		//  spec says type 7 is 'Home Security'
		switch (cmd.event) {
			case 0:
				//  spec says this is 'clear previous alert'
				target?.sendEvent(name: "contact", value: "open") 
				break
			case 2:
				//  spec says this is 'tamper'
				target?.sendEvent(name: "contact", value: "closed")     
				break
			default:
				break
		}
	} 
	else {
		log.warn "Need to handle this cmd.notificationType: ${cmd.notificationType}"
		result << createEvent(descriptionText: cmd.toString())
	}
	result    
}


//---------------------------
//
//---------------------------
private def zwaveEvent(hubitat.zwave.commands.notificationv8.NotificationReport cmd,ep,sp) {
	logDebug "NotificationReport V8: ep=$ep sp=$sp $cmd"
    
	def target = childDevices.find { it.deviceNetworkId == childNetworkId(ep) }  
	def result = []
	if (cmd.notificationType == 7) {
		//  spec says type 7 is 'Home Security'
		switch (cmd.event) {
			case 0:
				//  spec says this is 'clear previous alert'
				target?.sendEvent(name: "contact", value: "open") 
				break
			case 2:
				//  spec says this is 'tamper'
				target?.sendEvent(name: "contact", value: "closed")     
				break
			default:
				break
		}
	} 
	else {
		log.warn "Need to handle this cmd.notificationType: ${cmd.notificationType}"
		result << createEvent(descriptionText: cmd.toString())
	}
	result    
}

//---------------------------
//
//---------------------------
private zwaveEvent(hubitat.zwave.commands.basicv1.BasicReport cmd, ep) {
	logDebug "basicReport: ep=$ep $cmd"
	def target = childDevices.find { it.deviceNetworkId == childNetworkId(ep) }  
	switch(ep.toInteger()) {
		case 1..2:
  			break
		default:
			log.warn "Unsupported endpoint for BasicReport cmd=$cmd ep=$ep"
			break
	}
}


//---------------------------
//
//---------------------------
private zwaveEvent(hubitat.zwave.commands.basicv1.BasicReport cmd) {
	logDebug "basicReport: $cmd"
}


//---------------------------
//
//---------------------------
private zwaveEvent(hubitat.zwave.commands.centralscenev3.CentralSceneNotification cmd, v1, v2) {
	logDebug "CentralSceneNotification v1=$v1 v2=$v2 $cmd"
}


//---------------------------
//
//---------------------------
private zwaveEvent(hubitat.zwave.commands.configurationv1.ConfigurationReport cmd) {
	logDebug "ConfigurationReport $cmd"
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

//---------------------------
//
//---------------------------
private zwaveEvent(hubitat.zwave.commands.associationv2.AssociationReport cmd) {
	logDebug "AssociationReport: $cmd"
	if (cmd.groupingIdentifier == 1) {
		if (cmd.nodeId != []) {
			log.warn "Association for grouping 1 nodeId has unexpected value ${cmd.nodeId}, expected []"
		}
	}
	else if (cmd.groupingIdentifier == 2) {
		if (cmd.nodeId != []) {
			log.warn "Association for grouping 2 nodeId has unexpected value ${cmd.nodeId}, expected []"
		}
	}
	else if (cmd.groupingIdentifier == 3) {
		if (cmd.nodeId != []) {
			log.warn "Association for grouping 3 nodeId has unexpected value ${cmd.nodeId}, expected []"
		}
	}
	else
	{
		log.warn "Association for grouping ${cmd.groupingIdentifier} nodeId is unextpected, should be 1,2 or 3"
	}
	
}

//---------------------------
//
//---------------------------
private zwaveEvent(hubitat.zwave.commands.multichannelassociationv3.MultiChannelAssociationReport cmd) {
	logDebug "MultiChannelAssociationReport: $cmd"
	if (cmd.groupingIdentifier == 1) {
		if (cmd.nodeId != []) {
			log.warn "MultiChannelAssociation for grouping 1 nodeId has unexpected value ${cmd.nodeId}, expected []"
		}
		if (cmd.multiChannelNodeIds[0].nodeId != 1) {
			log.warn "MultiChannelAssociation for grouping 1 multiChannelNodeIds[0].nodeId has unexpected value ${cmd.multiChannelNodeIds[0].nodeId}, expected 1"
		}
		if (cmd.multiChannelNodeIds[0].endPointId != 0) {
			log.warn "MultiChannelAssociation for grouping 1 multiChannelNodeIds[0].endPointId has unexpected value ${cmd.multiChannelNodeIds[0].endPointId}, expected 0"
		}
	}
	else if (cmd.groupingIdentifier == 2) {
		if (cmd.nodeId != []) {
			log.warn "MultiChannelAssociation for grouping 2 nodeId has unexpected value ${cmd.nodeId}, expected []"
		}
		if (cmd.multiChannelNodeIds != []) {
			log.warn "MultiChannelAssociation for grouping 2 multiChannelNodeIds has unexpected value ${cmd.multiChannelNodeIds}, expected []"
		}
	}
	else if (cmd.groupingIdentifier == 3) {
		if (cmd.nodeId != []) {
			log.warn "MultiChannelAssociation for grouping 3 nodeId has unexpected value ${cmd.nodeId}, expected []"
		}
		if (cmd.multiChannelNodeIds != []) {
			log.warn "MultiChannelAssociation for grouping 4 multiChannelNodeIds has unexpected value ${cmd.multiChannelNodeIds}, expected []"
		}
	}
}

//---------------------------
//
//---------------------------
private zwaveEvent(hubitat.zwave.commands.versionv1.VersionCommandClassReport cmd) {
	logDebug "VersionCommandClassReport: $cmd"
}

//---------------------------
//
//---------------------------
private zwaveEvent(hubitat.zwave.commands.protectionv2.ProtectionReport cmd, ep) {
	if (ep.toInteger() == 5){
		if (cmd.localProtectionState != localProtection1.toInteger()) {
			log.warn "Parameter Input/Output 1 Local Device Protection has unexpected value ${cmd.localProtectionState} (expected ${localProtection1.toInteger()})"
		}
		if (cmd.rfProtectionState != rfProtection1.toInteger()) {
			log.warn "Parameter Input/Output 1 RF Device Protection has unexpected value ${cmd.localProtectionState} (expected ${localProtection1.toInteger()})"
		}
	}
	if (ep.toInteger() == 6) {
		if (cmd.localProtectionState != localProtection2.toInteger()) {
			log.warn "Parameter Input/Output 2 Local Device Protection has unexpected value ${cmd.localProtectionState} (expected ${localProtection1.toInteger()})"
		}
		if (cmd.rfProtectionState != rfProtection2.toInteger()) {
			log.warn "Parameter Input/Output 2 RF Device Protection has unexpected value ${cmd.localProtectionState} (expected ${localProtection1.toInteger()})"
		}
	}
	logDebug "ProtectionReport: $cmd, ep: $ep"
}

//---------------------------
//
//---------------------------
private zwaveEvent(hubitat.zwave.commands.versionv1.VersionReport cmd) {
	logDebug "VersionReport: $cmd"
    
	BigDecimal fw = cmd.firmware0Version //applicationVersion
	fw = fw + cmd.firmware0SubVersion/10 // applicationSubVersion / 100
    
	state.firmware = fw
	//if(fw < 1.10)
	//    log.warn "--- WARNING: Device handler expects devices to have firmware 1.10 or later"
}

//---------------------------
//
//---------------------------
private zwaveEvent(hubitat.zwave.Command cmd, ep=null) {
	log.warn "Unhandled event ${cmd} (endpoint ${ep})"
	//device = getChildDevice(deviceNetworkId)
	//createEvent(descriptionText: "${device.displayName}: ${cmd}")

}

//---------------------------
//
//---------------------------
private zwaveEvent(hubitat.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	logDebug "SecurityMessageEncapsulation: $cmd"
	hubitat.zwave.Command encapsulatedCommand = cmd.encapsulatedCommand(CMD_CLASS_VERS)
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from ${cmd}"
	}
}

		
// ----------------------------------------------------------------------------
// ------------------------------ CONFIGURATION -------------------------------
// ----------------------------------------------------------------------------

//---------------------------
//
//---------------------------
def configure() {
	def configuration = new XmlSlurper().parseText(configuration_model())
	def delay = 1000
	def cmds = []
	
	cmds = []
	cmds << zwave.associationV2.associationRemove(groupingIdentifier: 1, nodeId: zwaveHubNodeId)
	cmds << zwave.associationV2.associationRemove(groupingIdentifier: 2, nodeId: zwaveHubNodeId)
	cmds << zwave.associationV2.associationRemove(groupingIdentifier: 3, nodeId: zwaveHubNodeId)
	cmds << zwave.multiChannelAssociationV3.multiChannelAssociationRemove(groupingIdentifier: 1, nodeId: [zwaveHubNodeId])
	cmds << zwave.multiChannelAssociationV3.multiChannelAssociationRemove(groupingIdentifier: 2, nodeId: [zwaveHubNodeId])
	cmds << zwave.multiChannelAssociationV3.multiChannelAssociationRemove(groupingIdentifier: 3, nodeId: [zwaveHubNodeId])
	cmds << zwave.multiChannelAssociationV3.multiChannelAssociationSet(groupingIdentifier: 1, nodeId: [0,1,0])  // 
	//cmds << zwave.multiChannelAssociationV3.multiChannelAssociationSet(groupingIdentifier: 2, nodeId: [0,1,1])   // Used when IN1 input is triggered (using Basic Command Class).
	//cmds << zwave.multiChannelAssociationV3.multiChannelAssociationSet(groupingIdentifier: 3, nodeId: [0,1,2])   // Used when IN2 input is triggered (using Basic Command Class).     
	logDebug "cmds: ${cmds}"
	formatCommandsWithPause(cmds, delay)
	pauseExecution(2000)
	
	cmds = []
	cmds << toEndpoint(zwave.protectionV2.protectionSet(localProtectionState : localProtection1.toInteger(), rfProtectionState: rfProtection1.toInteger()), 5)
	cmds << toEndpoint(zwave.protectionV2.protectionSet(localProtectionState : localProtection2.toInteger(), rfProtectionState: rfProtection2.toInteger()), 6)
	logDebug "cmds: ${cmds}"
	formatCommandsWithPause(cmds, delay)
	pauseExecution(2000)

	cmds = []
	configuration.Value.each {
		def settingValue = settings[it.@index.toString()].toInteger()
		def byteSize = sizeOfParameter(it)

		if (settingValue != null) {
			if (settingValue == "") {
				logDebug "Setting ${it.@index} is empty"
			}

			def index = it.@index.toInteger()
			cmds << zwave.configurationV1.configurationSet(configurationValue: integerToBytes(settingValue, byteSize), parameterNumber: index, size: byteSize)
		} else {
			logDebug "Setting ${it.@index} has null value"
		}
	}
	logDebug "cmds: ${cmds}"
	formatCommandsWithPause(cmds, delay)
	pauseExecution(2000)
	logDebug "Configure() Done"
}

//---------------------------
//
//---------------------------
def CheckConfig() {
	def configuration = new XmlSlurper().parseText(configuration_model())
	def delay = 1000
	def cmds = []
	
	cmds << zwave.versionV1.versionGet()
	cmds << toEndpoint(zwave.protectionV2.protectionGet(), 5)
	cmds << toEndpoint(zwave.protectionV2.protectionGet(), 6)
	cmds << zwave.associationV2.associationGet( groupingIdentifier: 1)
	cmds << zwave.associationV2.associationGet( groupingIdentifier: 2)
	cmds << zwave.associationV2.associationGet( groupingIdentifier: 3)
	cmds << zwave.multiChannelAssociationV3.multiChannelAssociationGet(groupingIdentifier: 1)
	cmds << zwave.multiChannelAssociationV3.multiChannelAssociationGet(groupingIdentifier: 2)
	cmds << zwave.multiChannelAssociationV3.multiChannelAssociationGet(groupingIdentifier: 3)

	logDebug "cmds: ${cmds}"
	formatCommandsWithPause(cmds, delay)
	pauseExecution(2000)

  	cmds = []
	configuration.Value.each {
		def settingValue = settings[it.@index.toString()].toInteger()
		def byteSize = sizeOfParameter(it)

		if (settingValue != null) {
			if (settingValue == "") {
				logDebug "Setting ${it.@index} is empty"
			}

			def index = it.@index.toInteger()
			cmds << zwave.configurationV1.configurationGet(parameterNumber: index)
		} else {
			logDebug "Setting ${it.@index} has null value"
		}
	}

	logDebug "cmds: ${cmds}"
	formatCommandsWithPause(cmds, delay)
	pauseExecution(2000)
	logDebug "CheckConfig() Done"
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
			Default setting: 0.
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
			Default setting: 0.
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
def convertTemperature(cmd) {
		if(tempUnits == "default") {
			units = "\u00b0" + getTemperatureScale()			
			finalVal = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmd.scale == 1 ? "F" : "C", cmd.precision)
		} else if(tempUnits == "F"){
			units = "\u00b0" + "F"
			if(cmd.scale == 1) {
				finalVal = cmd.scaledSensorValue
			} else {
				finalVal = cmd.scaledSensorValue * 9.0/5.0 + 32.0
				factor = 10**cmd.precision
				finalVal = Math.round(finalVal* factor)/factor
			}
			
		} else {	
			units = "\u00b0" + "C"
			if(cmd.scale == 1) {
				finalVal = (cmd.scaledSensorValue - 32.0) * 5.0/9.0
				factor = 10**cmd.precision
				finalVal = Math.round(finalVal* factor)/factor
			} else {
				finalVal = cmd.scaledSensorValue
			}
		}
	return [finalVal, units]
}


//---------------------------
//
//---------------------------
String secureCommand(hubitat.zwave.Command cmd) {
	secureCommand(cmd.format())
}


//---------------------------
//
//---------------------------
String secureCommand(String cmd) {
    if (getDataValue("zwaveSecurePairingComplete") != "true") {
        return cmd
    }
    Short S2 = getDataValue("S2")?.toInteger()
    String encap = ""
    String keyUsed = "S0"
    if (S2 == null) { //S0 existing device
        encap = "988100"
    } else if ((S2 & 0x04) == 0x04) { //S2_ACCESS_CONTROL
        keyUsed = "S2_ACCESS_CONTROL"
        encap = "9F0304"
    } else if ((S2 & 0x02) == 0x02) { //S2_AUTHENTICATED
        keyUsed = "S2_AUTHENTICATED"
        encap = "9F0302"
    } else if ((S2 & 0x01) == 0x01) { //S2_UNAUTHENTICATED
        keyUsed = "S2_UNAUTHENTICATED"
        encap = "9F0301"
    } else if ((S2 & 0x80) == 0x80) { //S0 on C7
        encap = "988100"
    }
    logDebug "$keyUsed"
    return "${encap}${cmd}"
}

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
	def formattedCmds = cmds.collect { secureCommand(it.format()) }
	
	if (delay) {
		sendHubCommand(new hubitat.device.HubMultiAction(delayBetween(formattedCmds,delay), hubitat.device.Protocol.ZWAVE))
	} else {
		sendHubCommand(new hubitat.device.HubMultiAction(formattedCmds, hubitat.device.Protocol.ZWAVE))
	}
}
	
//---------------------------
//
//---------------------------
private formatCommandsWithPause(cmds, delay=null) {
	def formattedCmds = cmds.collect { secureCommand(it.format()) }
	formattedCmds.each {
		sendHubCommand(new hubitat.device.HubAction(it, hubitat.device.Protocol.ZWAVE))
		if(delay) {
			pauseExecution(delay)
		}
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
def logDebug(msg) {
	if(debugOutput) {
		log.debug "{$msg}"
	}
}

//---------------------------
//
//---------------------------
private channelNumber(String dni) {
	dni.split("-ep")[-1].toInteger()
}
