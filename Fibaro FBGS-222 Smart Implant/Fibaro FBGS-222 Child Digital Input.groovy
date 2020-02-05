metadata {
	definition (name: "Fibaro FGBS-222 Child Digital Input", namespace: "christi999", author: "") {
		// capability "Refresh"  //didn't find a way to do it manually, will get notifications otherwise...  
		capability "Contact Sensor"
	}
}

void refresh() {
	parent.childRefresh(device.deviceNetworkId)
}

