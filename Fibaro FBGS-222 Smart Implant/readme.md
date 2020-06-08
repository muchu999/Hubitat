From the Hubitat community: https://community.hubitat.com/t/release-fibaro-smart-implant-fgbs-222/42983

-Install the 5 files as 5 drivers. Use the main driver file "...smart implant" for the device "type" of the device. 
 This main driver will take care of using the other drivers as required (it will create child devices automatically).
 
-Periodic reports only work when multiChannelAssociation is set in a very specific way. Nothing you should worry about 
 unless you plan on changing the code...
 
-By default, outputs are individually linked to the digital inputs unless the inputs are set to be analog. Use 
 local protection = 2 to decouple outputs from intputs.

-The on-board temperature sensor always report high the device itself generates some heat.

-There is little support for the zwave scenes and buttons modes. Not many people are using those features afaik.

-Don't forget to "save preferences" and then press the "configure" button when changing parameters. When configure is pressed, 
 configuration parameters are sent over the air to the implant, this can take some times (1 minute), check the log to see 
 what is happening if you want.
 
-The data from the implant might not be updated right away when the driver is first installed. In the case of the digital
 inputs (with notifications), a notification will only be sent by the implant once the state of the input changes. You therefore 
 have to change the input state (contact sensor or whatever else you are using) manually at least once before the child device will 
 register the current state of the input.
