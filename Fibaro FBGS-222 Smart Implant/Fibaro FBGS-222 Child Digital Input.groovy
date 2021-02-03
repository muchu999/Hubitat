metadata {
	definition (name: "Fibaro FGBS-222 Child Digital Input", namespace: "christi999", author: "", importUrl: "https://raw.githubusercontent.com/muchu999/Hubitat/master/Fibaro%20FBGS-222%20Smart%20Implant/Fibaro%20FBGS-222%20Child%20Digital%20Input.groovy") {
		// capability "Refresh"  //didn't find a way to do it manually, will get notifications otherwise...  
		capability "ContactSensor"
	}
}

void refresh() {
	parent.childRefresh(device.deviceNetworkId)
}

