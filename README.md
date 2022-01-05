	  _____         _ _         
	|  _  |___ _ _| |_|___ ___ 
	|     |   | | | | |   | -_|
	|__|__|_|_|_  |_|_|_|_|___|
	          |___|            


# Anyline Examples App 

[Anyline](https://www.anyline.com) is mobile OCR SDK, which can be configured by yourself to scan all kinds of numbers, characters, text and codes. 

The Example App provides you with many working preconfigured modules, which show the bandwith of what the SDK can scan. 

## Quick Start Guide

### 0. Clone or Download

* If you'd like to clone the repository you will have to use git-lfs. Use the following commands to install git-lfs.
```
brew install git-lfs
git lfs install
```

* If you prefer downloading a package, use the provided `zip` package on the [releases page](https://github.com/Anyline/anyline-ocr-examples-android/releases). Be aware that the github download zip button does not work for projects with git-lfs.

### 1. Add AnylineSDK as a dependency 

__Via Maven__

Add Anyline SDK to the dependencies in build.gradle 

```java
//root section of the file
repositories {
    //add the anyline maven repo
    maven { url 'https://anylinesdk.blob.core.windows.net/maven/'}
}

dependencies {
    //add the anyline sdk as dependency (maybe adapt version name)
    implementation 'io.anyline:anylinesdk:33.0.0'
    //... your other dependencies
}
```

Be aware that the AnylineSDK per default ships trained models for all of our supported use cases. This can increase your app size significantly, but there is a simple way to remove the assets you do not need. If you want to remove the unnecessary assets, add this to your application build.gradle file, in the android section and just remove from the list which scan modes you would like to keep:

```java
 packagingOptions {
        aaptOptions {
            ignoreAssetsPattern "module_energy:module_id:module_anyline_ocr:module_barcode:module_document:module_license_plate"
        }
 }
```


### 2. Provide a config file (json or xml)

The config file enables a quick and easy adaption of the “Scan-View”. You can either provide a json-file or use the XML-attributes in the layout file itself. A detailed description of all available attributes can be found in [Anyline Config](https://documentation.anyline.com/#anyline-config)


Example barcode_view_config.json:

```json
{
    "captureResolution":"1080p",
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

__JSON__

This config file must be located in the assets folder of your Android project.

Some of the most important config options may be:

Parameter | Description
--------- | ------------
captureResolution |	the preferred camera preview size
cutout | defining which area of the preview will be “cutout” (analyzed to find bar/QR code)
flash |	defines the flash mode, where to place the flash symbol, etc.
beepOnResult |	enables sound on successful scanning process (for modules only)
vibrateOnResult | provides haptic feedback for a successful scanning process (for modules only)
blinkOnResult |	visual feedback for a successful scanning process (for modules only)
cancelOnResult | if true, the scanning process will be stopped after one result and needs to be restarted manually (for modules only)

__XML__

Alternatively to a json config, it is also possible to configure the view (EnergyScanView, MrzScanView, BarcodeScanView) using XML-attributes in the layout-file.

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

### 3. Add your License 

Add your license to your string resources (can be a separate xml file).

### 4. Init Anyline in your Activity 

There are module specific options - take a look at the description of the desired module in the Anyline Documentation to get more detailed information.

```xml
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

### 5. Enjoy scanning and have fun! :movie_camera:


## Sample Codes & Documentation 

Have look at some of our code examples: [Sample Code](https://documentation.anyline.com/toc/platforms/android/demos.html)

Detailed information about how to configure and implement Anyline: [Documentation](https://documentation.anyline.com)


## License 

See LICENSE file.

To claim a free developer / trial license key, go to: [Anyline SDK Register Form](https://ocr.anyline.com/request/sdk-trial).