### What is WozARd?

WozARd is a Wizard of Oz tool for mobile augmented reality. Wizard of Oz methodology is useful when conducting user studies of a system that is in early development. It is essential to be able to simulate part of the system and to collect feedback from potential users. Using a human to act as the system is one way to do this.

WozARd aims at offering a set of tools that help a test leader control the visual, tactile and auditive output that is presented to test participant. Additionally, it is suitable for using in an augmented reality environment where images are overlaid on the phone’s camera view or on glasses.

The main features include presentation of media such as images, video and sound, navigation and location based triggering, automatically taking photos, capability to log test results and visual feedback. There is also an integration for Sony SmartWatch that offers interaction possibilities.

WozARd consists of three Android applications:
* Wizard. An application that is running on the device used by the test leader (wizard device). The test leader uses this application to control and monitor the puppet application.
* Puppet. An application that is running on the device used by the test participant (puppet device).
* SmartWatch extension. An optional application that allows a test participant to perform touch taps and swipes that are sent to the wizard application. This application is running on the wizard device.

### Build instructions
The applications require Android:
* Wizard: Android 4.0, or higher, with Google APIs (API level >=14)
* Puppet: Android 2.3, or higher, (API level >=9)
* SmartWatch extension: Same as Wizard + [Sony Smart Extension APIs](http://developer.sonymobile.com/knowledge-base/sony-add-on-sdk/smart-extension-apis/)

### Installation and setup
1. Install the Wizard application on the wizard device.
2. Copy the [Content](Content) folder to the top level of the internal memory or memory card on the wizard device.
3. Optional: Install the SmartWatch extension. Note that this require a Sony SmartWatch or SmartWatch 2 device and related software (SmartWatch app ([1](https://play.google.com/store/apps/details?id=com.sonyericsson.extras.smartwatch) or [2](https://play.google.com/store/apps/details?id=com.sonymobile.smartconnect.smartwatch2)) and [SmartConnect](https://play.google.com/store/apps/details?id=com.sonyericsson.extras.liveware))
4. Install the Puppet application on the puppet device.
5. Copy the [Content](Content) folder to the top level of the internal memory or memory card on the puppet device.
6. Turn on Wi‐Fi Hotspot on the wizard device and then connect the puppet device to the wizard hotspot network. See [this guide](http://www.wikihow.com/Turn-Your-Android-Phone-Into-a-Wi%E2%80%90Fi-Hotspot) if you are unsure how to do this.
7. Start the Wizard application.
8. Start the Puppet application.
9. Make sure the Puppet application connects correctly to the Wizard app. The bar along the top part of the screen in the Wizard app should go from red to green color.
9. Configure the Puppet application so that it matches the capabilities of the device. For example, if you are using a standard smartphone or tablet the default setting should be fine. If on the other hand you are using a optical see through HMD (e.g. Epson Moverio) then you should turn on "optical see-through" in settings.

### Usage instructions
Currently there is no manual for how to use the applications. We are working on this so stay tuned. Don't hesitate to explore the Wizard application UI and try it out.

### Contact information
* Günter Alce, gunter.alce@sonymobile.com, Sony Mobile Communications
* Klas Hermodsson, klas.hermodsson@sonymobile.com, Sony Mobile Communications
