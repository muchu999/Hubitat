metadata {
	definition (name: "Fibaro FGBS-222 Child Temperature Sensor", namespace: "christi999", author: "") {
		capability "Refresh"
		capability "Sensor"
        capability "Temperature Measurement"
	}
}

void refresh() {
	parent.childRefresh(device.deviceNetworkId)
}
