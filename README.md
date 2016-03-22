1. wot-demo-devices

2. wot-scripting-api
  1. add hardware emulation: to add and implement the simulation environment for a hardware in a java file like the devices.lightbulb. The emulated device will have a set of sub resources like manufacturer, serialNo, power, ledcolour, etc.
  2. add PHY api: the PHY api will be having the interfaces needed to control and fetch data from the hardware devices emulated above. They should be able to input the javascript and internally call the interfaces for the hardwares to control, communicate.

Ac app that controls light bulb: With the defnition of the PHY api once done, the Actinium app is to be integrated with the PHY api so that it gets control of the light bulb by calling the functions of the phy api.

Add more hardware: to emulate more hardwares like the light-bulb and to see that the physical api designed is still able to communicate with this newly added device.
