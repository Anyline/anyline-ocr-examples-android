## Android Getting Started ##

The Android bundle contains the following parts:

- **AnylineSDK:**           contains the anyline-android-release.aar library
- **AnylineSDKExamples:**   contains a simple app where an example for each module is implemented - it can be installed right away
- **Documentation:**        contains the java doc for the anyline-sdk, and this web documentation
- **LICENSE:**              third party license agreements
- **README:**               contains a quick start - setup and module description
- **RELEASE_NOTES:**        information about any changes

### Sample Data ###

In order to try out our examples app yourself, you can download  (https://anylinewebsiteresource.blob.core.windows.net/wordpressmedia/2016/08/Examples-Sheet-Anyline-3.6.pdf) this file with test images or scan things in the real world ;)
 
### Requirements ###

- Android device with SDK >= 15
- decent camera functionality (recommended: 720p and adequate auto focus)


### Quick Start Guide ###

#### 1. Add AnylineSDK as dependency ####

##### Via Maven  #####

- Add AnylineSDK to the dependencies in build.gradle

```groovy
//root section of the file
repositories {
    //add the anyline maven repo
    maven { url 'https://anylinesdk.blob.core.windows.net/maven/'}
}

dependencies {
    //add the anyline sdk as dependency (maybe adapt version name)
    compile 'io.anyline:anylinesdk:3.6.0@aar'
    //... your other dependencies
}
```  

**Epson specific**

To run Anyline for Epson BT2000 you have to integrate a specific Anyline SDK version.
Therefore change the line in dependecies as follows: 

compile 'io.anyline:anylinesdk:3.3.0-epson@aar'




##### Or via local copy of the aar #####

Copy the .aar to the libs directory of your project (app/libs) and adapt build.gradle.

- Add AnylineSDK to the dependencies in build.gradle

```groovy
//root section of the file
repositories {
    flatDir {
        dirs 'libs'
    }
}

dependencies {
    compile(name:'anylinesdk-3.6.0', ext:'aar')
    //... your other dependencies
}
```

**Epson specific**

To integrate the Epson specific Anyline SDK adapt the line in dependecies as follows:

compile(name:'anylinesdk-3.3.0-epson', ext:'aar')




#### 2. Limit the build to the provided architectures ####

We currently only provide our SDK for the architectures armeabi-v7a and x86.
This is sufficient to support most devices (none known for which this is not sufficient)
and it keeps the file size smaller. To further reduce the file size it is possible to generate and submit two separate apks
see this  (http://tools.android.com/tech-docs/new-build-system/user-guide/apk-splits) user guide for more details.
If other dependencies or your own native code include builds for more architectures, system load errors will occur.

- To limit the build to the two architectures you can add a ndk filter to your build.gradle:

```groovy
android {
    ....
    defaultConfig {
        ....
        ndk {
            abiFilters "armeabi-v7a", "x86"
        }
    }
}
```

- depending on the gradle plugin version you may need add the following in your gradle.properties

```
android.useDeprecatedNdk=true
```

- (Note that the old NDK integration may be deprecated, but the new NDK integration is experimental)



#### 3. Provide a config file (json or xml) ####

- Example barcode_view_config.json:

```json
{
	"captureResolution":"720p",
  	"cutout": {
		"style": "rect",
		"maxWidthPercent": "80%",
		"alignment": "center",
		"ratioFromSize": {
	     	"width": 100,
	     	"height": 80
		},
		"strokeWidth": 2,
		"cornerRadius": 4,
		"strokeColor": "FFFFFF",
		"outerColor": "000000",
		"outerAlpha": 0.3
	 },
	 "flash": {
		"mode": "manual",
	 	"alignment": "bottom_right"
	 },
	 "beepOnResult": true,
	 "vibrateOnResult": true,
	 "blinkAnimationOnResult": true,
	 "cancelOnResult": true
}
```

The config file enables a quick and easy adaption of the "Scan-View". You can either provide a json-file or use the XML-attributes in the layout file itself.
A detailed description of all available attributes can be found in [Anyline Config] (#anyline-config)

**Epson specific**

The Epson BT2000 has no flash or vibration alarm integrated. Hence, all settings regarding flash and 'vibrateOnResult' will be ignored. Furthermore the Epson has no built-in speakers, therefore the 'beepOnResult' will only be played when the headset is connected

###### JSON ######

This config file must be located in the **assets** folder of your Android project.

Some of the most important config options may be:

Parameter | Description
-------- | -----------
captureResolution | the preferred camera preview size
cutout | defining which area of the preview will be "cutout" (analyzed to find bar/QR code)
flash | defines the flash mode, where to place the flash symbol, etc.
beepOnResult | enables sound on successful scanning process (for modules only)
vibrateOnResult | provides haptic feedback for a successful scanning process (for modules only)
blinkOnResult |  visual feedback for a successful scanning process (for modules only)
cancelOnResult | if true, the scanning process will be stopped after one result and needs to be restarted manually (for modules only)

###### XML ######


```xml
<RelativeLayout
	 xmlns:android="http://schemas.android.com/apk/res/android"
	 xmlns:app="http://schemas.android.com/apk/res-auto"
	 android:layout_width="match_parent"
	 android:layout_height="match_parent">
	<at.nineyards.anyline.modules.energy.EnergyScanView
	    android:id="@+id/energy_scan_view"
	    android:layout_width="match_parent"
	    android:layout_height="match_parent"
	    app:cutout_alignment="top"
	    app:cutout_style="rect"
	    app:cutout_outside_color="#55000000"
	    app:cutout_offset_y="120"
	    app:cutout_rect_corner_radius_in_dp="4"
	    app:cutout_stroke_width_in_dp="2"
	    app:cutout_stroke_color="#FFFFFF"
	    app:flash_mode="manual"
	    app:flash_alignment="bottom_right"
	    app:beep_on_result="true"
	    app:vibrate_on_result="true"
	    app:blink_animation_on_result="true"
	    app:cancel_on_result="true"
	 />
</RelativeLayout>
```

Alternatively to a json config, it is also possible to configure the view (EnergyScanView, MrzScanView, BarcodeScanView) using XML-attributes in the layout-file.



#### 4. Add your license ####

Add your license to your string resources (can be a separate xml file).

```
<string name="anyline_license_key" translatable="false">
    icaCo34adsfasdferJBAerisdlfkjerj1234adsflkerhlakherDAdfjlafherGs\n
    h4ll0we1t7h1s1sno74r34ll1c3n53yO00asdfkaer455alksdfASDSlallernde\n
    YXRmb3JtIjogWyAiQW5kcm9pZCIgXSwgInNjb3BlIjogWyAiQUxMIiBdLCAidG9s\n
    icaCo34adsfasdferJBAerisdlfkjerj1234adsflkerhlakherDAdfjlafherGs\n
    cHhoTm9UZ0g1cys3M2dhUW1SMUpXblJLWEhFeE02eHRNWHpaNEViWXdXODFmWmRl\n
    YXRmb3JtIjogWyAiQW5kcm9pZCIgXSwgInNjb3BlIjogWyAiQUxMIiBdLCAidG9s\n
    c0xjNEwzbjBLV0tnOG80NzdGSHA3OHUydWFVCkRqUUU4S0RWK240RkFVZ3FnUHg5\n
    icaCo34adsfasdferJBAerisdlfkjerj1234adsflkerhlakherDAdfjlafherGs\n
    aWNldnRTZEU2Q3hkYldZUVdpQjNkUXFqckxuWW0vaE1CSEx2OHRUdWpyN09MWDhC\n
    h4ll0we1t7h1s1sno74r34ll1c3n53yO00asdfkaer455alksdfASDSlallernde\n
    icaCo34adsfasdferJBAerisdlfkjerj1234adsflkerhlakherDAdfjlafherGs\n
    SEptQVE9PQo=\n
</string>
```

#### 5. Init Anyline in your Activity ####
There are module specific options - take a look at the description of the desired module to get more detailed information.

#### 6. Enjoy scanning and have fun :) ####


For more documentation see the 'Documentation' folder.