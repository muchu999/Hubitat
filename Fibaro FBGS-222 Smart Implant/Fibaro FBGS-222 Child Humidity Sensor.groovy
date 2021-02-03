metadata {
	definition (name: "Fibaro FGBS-222 Child Humidity Sensor", namespace: "christi999", author: "", importUrl: "https://raw.githubusercontent.com/muchu999/Hubitat/master/Fibaro%20FBGS-222%20Smart%20Implant/Fibaro%20FBGS-222%20Child%20Relative Humidity%20Sensor.groovy") {
		capability "Refresh"
		capability "Sensor"
		capability "Relative Humidity Measurement"
	}
}

void refresh() {
	parent.childRefresh(device.deviceNetworkId)
}
