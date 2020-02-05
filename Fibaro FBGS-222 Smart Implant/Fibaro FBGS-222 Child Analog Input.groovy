metadata {
	definition (name: "Fibaro FGBS-222 Child Analog Input", namespace: "christi999", author: "") {
		capability "Refresh"
		capability "Voltage Measurement"
		capability "Sensor"
     
	}
}

void refresh() {
	parent.childRefresh(device.deviceNetworkId)
}


