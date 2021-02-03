metadata {
	definition (name: "Fibaro FGBS-222 Child Temperature Sensor", namespace: "christi999", author: "", importUrl: "https://raw.githubusercontent.com/muchu999/Hubitat/Master/Fibaro%20FBGS-222%20Smart%20Implant/Fibaro%20FBGS-222%20Child%20Temperature%20Sensor.groovy") {
		capability "Refresh"
		capability "Sensor"
		capability "Temperature Measurement"
	}
}

void refresh() {
	parent.childRefresh(device.deviceNetworkId)
}
