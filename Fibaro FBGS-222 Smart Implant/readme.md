From the Hubitat community: https://community.hubitat.com/t/fibaro-smart-implant/10527/15

-Install the 5 files as 5 drivers. Use the main driver file "...smart implant" for the device "type" of the device. 
 This main driver will take care of using the other drivers as required (it will create child devices automatically).
 
-Periodic reports only work when multiChannelAssociation is set in a very specific way. Nothing you should worry about 
 unless you plan on changing the code...
 
-By default, outputs are individually linked to the digital inputs unless the inputs are set to be analog. Once the input 
 has acted upon its corresponding output, it is always possible to set it back to whatever you want using the switch commands.
 Enabling local protection (local protection value=2) will allow the the outputs to be independant from the inputs

-The on-board temperature sensor always report high because it is heated by the device.

-I didn't test much the scenes and buttons modes

-Some of the commands in the parent device are there just so they work for the children, use the commands in the 
 children devices for setting outputs and refresh

-Don't forget to "save preferences" and then press the "configure" button when changing parameters. When configure is pressed, 
 configuration parameters are sent over the air to the implant, this can take some times (10 to 30 sec), check the log to see 
 what is happening if you want.
 
 -The data from the implant might not be updated wight away when the driver is first installed... In the case of the digital
  input with notifications, a notification will only be sent once the state of the input changes. You therefore have to change
  the input manually at least once before the child device will be updated.
