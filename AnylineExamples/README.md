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
    maven { url 'https://anylinesdk.blob.core.windows.net/testing/'}
}

dependencies {
    //add the anyline sdk as dependency (maybe adapt version name)
    implementation 'io.anyline:anylinesdk:43.0.0'
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
  "cameraConfig": {
    "captureResolution": "720"
  },
  "flashConfig": {
    "mode": "manual",
    "alignment": "bottom_right"
  },
  "viewPluginConfig": {
    "pluginConfig": {
      "id": "Universal Serial Numbers",
      "ocrConfig": {},
      "startScanDelay": 1000,
      "cancelOnResult": true
    },
    "cutoutConfig": {
      "style": "rect",
      "width": 720,
      "alignment": "top_half",
      "maxWidthPercent": "80%",
      "ratioFromSize": {
        "width": 720,
        "height": 144
      },
      "strokeWidth": 2,
      "strokeColor": "FFFFFF",
      "cornerRadius": 4,
      "outerColor": "000000",
      "outerAlpha": 0.5,
      "feedbackStrokeColor": "0099FF",
      "offset": {
        "x": 0,
        "y": -15
      }
    },
    "scanFeedbackConfig": {
      "style": "CONTOUR_RECT",
      "strokeColor": "0099FF",
      "fillColor": "220099FF",
      "beepOnResult": true,
      "vibrateOnResult": true,
      "blinkAnimationOnResult": true
    }
  }
}
```

__JSON__

This config file must be located in the assets folder of your Android project.

Some of the most important config options may be:

Parameter | Description
--------- | ------------
captureResolution |	the preferred camera preview size
cutoutConfig | defining which area of the preview will be “cutout” (analyzed to find bar/QR code)
flashConfig |	defines the flash mode, where to place the flash symbol, etc.
beepOnResult |	enables sound on successful scanning process
vibrateOnResult | provides haptic feedback for a successful scanning process 
blinkAnimationOnResult |	visual feedback for a successful scanning process
cancelOnResult | if true, the scanning process will be stopped after one result and needs to be restarted manually (for modules only)

__XML__

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

Detailed information about how to configure and implement Anyline: [Documentation](https://documentation-preview.anyline.com/android-sdk-component/43.0.0/index.html)

## Get Help (Support)

We don't actively monitor the Github Issues, please raise a support request using the [Anyline Helpdesk](https://anyline.atlassian.net/servicedesk/customer/portal/2/group/6).
When raising a support request based on this Github Issue, please fill out and include the following information:

```
Support request concerning Anyline Github Repository: anyline-ocr-examples-android
```

Thank you!

## License

See LICENSE file.

To claim a free developer / trial license key, go to: [Anyline SDK Register Form](https://ocr.anyline.com/request/sdk-trial).