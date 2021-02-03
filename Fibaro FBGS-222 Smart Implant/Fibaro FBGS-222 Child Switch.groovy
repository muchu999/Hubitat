metadata {
	definition (name: "Fibaro FGBS-222 Child Switch", namespace: "christi999", author: "", importUrl: "https://raw.githubusercontent.com/muchu999/Hubitat/master/Fibaro%20FBGS-222%20Smart%20Implant/Fibaro%20FBGS-222%20Child%20Switch.groovy") {
		capability "Actuator"
		capability "Refresh"
		capability "Sensor"
		capability "Switch"
	}

	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: "${name}", action: "on",
				icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: "${name}", action: "off",
				icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
		}
	}
}

void on() {
	parent.childOn(device.deviceNetworkId)
}

void off() {
	parent.childOff(device.deviceNetworkId)
}

void refresh() {
	parent.childRefresh(device.deviceNetworkId)
}
