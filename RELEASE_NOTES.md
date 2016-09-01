# Anyline SDK Android Release Notes #

## Anyline SDK 3.6.1 ##
Release Date 2016-08-25

### Improved ###
- Energy OCR speed

### Fixed ###
- issue with x86 on Marshmallow (Android 6, API 23)


## Anyline SDK 3.6.0 ##
Release Date 2016-07-13

### New ###
- Includes a new OCR engine for the energy use cases to improve accuracy

### Fixed ###
- setting isBeepOnResult, isBlinkOnResult, isVibrateOnResult or isCancelOnResult trough xml not working


## Anyline SDK 3.5.2 ##
Release Date 2016-06-16

- build with libpng 1.5.27 (fix for security vulnerability)


## Anyline SDK 3.5.1 ##
Release Date 2016-06-06

### New ###
- added option to ignore whitespaces in AnylineOCR LINE mode (makes it faster)

### Improved ###
- MRZ: ~10-20% faster
- Energy: ~5-10% faster
- AnylineOCR:
    - LINE: ~0-50% faster
    - GRID: ~5-10% faster

### Updated ###
- Visual Scan Feedback updated for some use cases

### Fixed ###
- Visual feedback not possible with custom script that has internal crop
- Visual feedback was deactivated if no config was provided and false was returned in onTextOutlineDetected()


## Anyline SDK 3.5 ##
Release Date 2016-05-04

### New ###
- SDK:
    - added configurable visual scan feedback
- Energy Module:
    - new modes for analog meters with 4 pre-decimal places
    - new modes for analog meters with 7 pre-decimal places
    - new mode for analog meters with white background and 5 or 6 pre-decimal places
    - new mode for analog gas meters with 6 pre-decimal places

### Improved ###
- Energy Module:
    - Electric meter mode no longer requires a red area
- AnylineOCR
    - updated Voucher Code use-case with new Anyline Font
    - updated ISBN use-case to scan codes starting with ISBN-10: or ISBN-13:
- Documentation:
     - improved documentation on how to load a custom command file
     - improved documentation on how to add an image to the cutout view config
- updated OpenSSL version

### Fixed ###
- SDK:
    - fixed a bug where maxHeightPercentage was ignored in JSON view config
- Energy Module:
    - fixed a bug that 11111 was often returned as result for some meter types
- AnylineOCR:
    - fixed a bug where the reported text outline was positioned incorrectly if the cutout contained a crop
    - fixed a bug where GRID mode would crash if min and maxCharHeight where not set
    - fixed a bug where a custom command file would not override certain parameters


## Anyline SDK 3.4.1 ##
Release Date 2016-03-30

### New ###
- added experimental sharpness detection to Anyline OCR
- configure Anyline OCR via json

### Fixed ###
- no beep on next result when returned to scan from an other activity via back button
- crash when rapidly switching scan scripts
- focus issues with some nexus devices


## Anyline SDK 3.4 ##
Release Date 2016-02-18

### New ###
- added Anyline OCR module (generic module for custom use cases)
- added scanning of documents

### Improved ###
- Camera and Focus settings
- MRZ speed

### Fixed ###
- issue with preview layout (on devices that support 1080 preview on 720 screen, not the full preview was shown)
- very rare crash when starting a scan view


## Anyline SDK 3.3 - Epson ##
Release Date 2016-01-21

### New ###
- add order code scanning: scans alphanumeric codes (11 characters) with varying font sizes
- Adapt CameraView and AnylineConfig to work with EPSON BT2000 available hardware and provided Developer SDK from Epson

### Improved ###
- fixed scaling issue on low resolution preview 


## Anyline SDK 3.3  ##
Release Date 2015-12-17

### New ###
- added scanning of heat meters
- added scanning of water meters
- added electric meter scanning with decimal place
- added scanning of generic digital meters

### Improved ###
- barcode scanning accuracy improved
- MRZ scanning: higher tolerance for targeting the IDs
- SDK interaction:
    - get the rect of the cutout to position your GUI around it
    - added a configurable offset the flash button
    - get information if the scanning is currently running

### Changed ###
- libraries for armv6 architecture no longer included (can be provided by request though)


## Anyline SDK 3.2.1 ##
Release Date 2015-10-23

### Improved ###
- Barcode Module
    - Better and faster scanning
    - Same barcode can now be scanned again (after 2 second timeout)
- MRZ Module
    - Refined scanning of TD1 size MROTDs
    - Refined scanning of MRPs and other TD3 size MRTDs
    - Added support for TD2 size MROTDs
    - Added support for French ID cards
        - Known Limitations: check digit validation does not work on French ID cards

### Changed ###
- BarcodeResultListener: added the format of the barcode to the
```
// old
void onResult(String result, AnylineImage anylineImage);
// new
void onResult(String result, BarcodeScanView.BarcodeFormat barcodeFormat, AnylineImage anylineImage);
```
- MRZ Identification
    - New fields issuingCountryCode and nationalityCountryCode
    - New field personalNumber2 (may be used in TD1 size MROTD)
    - New field allCheckDigitsValid
        - Indicates if all check digits are valid according to the algorithm defined in ICAO 9303-3 section 4.9
    - Deprecated countryCode (Use issuingCountryCode and nationalityCountryCode instead)
    - All fields set to empty strings instead of null if they are not present


## Anyline SDK 3.2 RC5 ##
Release Date 2015-09-23

### Fixed ###
- a problem with auto flash in energy module

### Changed ###
- libraries for MIPS architecture no longer included (can be provided by request though)

### New ###
- Added default reporting to Energy module (can be disabled)

## Anyline SDK 3.2 RC4 ##

### Fixed ###
- Crash on device without flash (and flashmode not none)

## Anyline SDK 3.2 RC3 ##
Release Date 2015-09-17
- different logo for watermark

## Anyline SDK 3.2 RC2 ##

### Fixed ###
- crash on Android < 4.3
- crash when community edition was resumed from background

## Anyline SDK 3.2 RC1 ##

### New ###
- Community Version
- New Licensing (Update requires new License Key)
- New internal build system with nightly tests

### Improved ###
- Energy Module: Electric Meter scan improved (better 6-7 digit distinction, less errors)

### Fixed ###
- CameraOpenListener in Modules not called
- Setting barcode format in barcode module has no effect


## Anyline SDK 3.1.1 ##
Release Date 2015-09-07

- fixed a reporting bug


## Anyline SDK 3.1 ##
Release Date 2015-09-01

- RC4 promoted to final 3.1 release


## Anyline SDK 3.1 RC4 ##
Release Date 2015-07-24

### Fixed ###
- ID Card MRZ Scanning does not work on multiple surnames
- Barcode scanning not working in Energy-Module

### New ###
- auto-flash support for all modules (alpha stage ... still prefere manual)

### Changed ###
- Barcode Module now also Reports an Image

```
//Listener changed from
void onResult(String result);
//To
void onResult(String result, AnylineImage anylineImage);
```

- MRZ Module getter Name in Identification Object changed (to be aliened with iOS)

```
//old
identification.getType();
//new
identification.getDocumentType();

//old
identification.getCheckDigitExpiration();
//new
identification.getCheckDigitExpirationDate();
```


## Anyline SDK 3.1 RC3 ##
Release Date 2015-07-15

### Improved ###
- Added support for all machine readable german passports in MRZ module
- Refined ID recognition in MRZ module


## Anyline SDK 3.1 RC2 ##
Release Date 2015-07-14

### New ###
- Added general support for german passports in MRZ module


## Anyline SDK 3.1 RC 1##
Release Date 2015-07-07

### New ###
- Ported 50 Operations to C++
- Added Modules and Views for:
- Anyline Barcode
- Anyline Energy
- Anyline MRZ
- Visual, haptic, sound feedback for successful scanning result
- Module interfaces configurable with interface builder
- New Operations:
- Count Nonzero Pixels
- Entropy in Rect
- Barcode find and rotate
- Contour Count
- Find Rotate Angle for Contour Lines
- Rect from Contours
- Remove Data Points
- Resolve Contour Intersect Contour
- Create Mask filled
- Draw Contours
- Draw Lines
- Draw Rect
- Draw Specs
- Find Hough Lines
- Histogram Equalization
- Init Contour Template
- Init Regex
- Init Size
- Is Image Equal
- Rect Distance
- Contour Template Finder
- Contour Template Loader
- OCR Contour
- Contrast Threshold
- Gradient Threshold
- Morphology Threshold
- Bounding Rect From Spec
- Extend Rect
- Count Results
- Match Result to Spec


## Anyline SDK 2.5.0 ##
Release Date 2015-01-30

### New ###
- Filter Contours Area Operation
- Adapt DataPoint for bounding rects in line
- CMYK Channel Operation
- HSV Channel Operation
- RGB Channel Operation
- Overlay Thresholding Operation
- own OCR Engine

### Improved ###
- TextDataPoints with character count
- BSThresholding with threshold factor
- parallelized Tesseract Operation
- descriptive patrameters for CodeParser
- refactored operations to use descriptive parameters and default values

### Fixed ###
- Tesseract OCR bugfix
- Resize Operation


## Anyline SDK 2.4.2 ##
Release Date 2015-01-07

### New ###
- Cut Thresholding Operation
- RGB Channel Extraction Operation
- HSV Channel Extraction Operation
- YMBC Channel Extraction Operation
- DataPoints for Line Operations
- Filter Contours in Area Operation

### Improved ###
- Parallelisation in OCR Detection
- BS Thresholding

### Fixed ###
- OCR bugfix


## Anyline SDK 2.4.1 ##
Release Date 2014-12-04

### New ###
- Watershed Operation
- Background Segmentation Thresholding
- Reflection Detection Operation
- QR / Barcode Scanning Operation
- Sort Contours Operation
- Init Image Operation for Parser
- Draw Border Operation
- Draw Bounding Rects in Image Operation
- Combine Images Operation
- Normalize Images Operation
- Set Color with Mask Operation
- Resolve Contours X Violation Operation

### Improved ###
- DataPoints with Grid Operation
- CodeParser Improvements
- Tesseract Operation works with Image Array
- More generic Thresholding Operation

### Fixed ###
- Values Stack bugfix
- Memory leaks with arm64
- rare occurring crashes fixed
- Digit DataPoint fixes


## Anyline SDK 2.4 ##
Release Date 2014-10-28

### New ###
- Mean Color in Rect Operation
- Color Distance Calculation Operation
- Validate Result Operation
- Clean Result Operation
- Find DataPoints Operation with configurable grid
- support for italic 7-segments

### Improved ###
- Interpreter refactored
- Improved Error & Exception handling

### Fixed ###
- fixed some possible Leaks
- fixed GPUImage bugs
- fixed minor issues with iOS 8


## Anyline SDK 2.3.5 ##
Release Date 2014-10-01

### Improved ###
- better OCR training capabilities

### Fixed ###
- Various Bugfixes


## Anyline SDK 2.3.4 ##
Release Date 2014-09-23

### New ###
- Square Angle Correction Operation
- Expand Square Operation
- Auto Rotate Image Operation
- Find Digit Position with Bounding Rects Operation
- Threshold Edge Detection GPU Operation

### Improved ###
- Find Square Operation
- ALSquare Bounding Rect Methods

### Fixed ###
- Various Bugfixes


## Anyline SDK 2.3.3 ##
Release Date 2014-09-11

### New ###
- Adaptive Luminance Thresholding Operation
- new Brightness in Rectangle Operation
- Luminance for Brightness Operation

### Improved ###
- Tesseract adaptive learning
- improved find data point
- find bounding squares with constraints
- better find data point area

### Fixed ###
- iOS8 color thresholding bug


## Anyline SDK 2.3.2 ##
Release Date 2014-08-23

### New ###
- Adapt Digit Position Operation with bounding rects
- Init Number Operation
- BS Thresholding Operation

### Improved ###
- Area constraint added for ApproxPolyDP Operation
- flipping values stack with none flipping support

### Fixed ###
- Observer removal bugfix
- reset equal count in values stack


## Anyline SDK 2.3.1 ##
Release Date 2014-08-12

### New ###
- new GetEqualCount Operation
- validation delegate for DisplayResult
- Init Operation for the ValuesStackFlipping

### Improved ###
- FindLargestSquareWithSizeRatio now works with Specs
- FindLargestSquareWithSizeRatio with ratio tolerance
- Transform Operation now works with Specs
- Flipping Values Stack now can accept single partial results
- DetectDigits with optional threshold parameter

### Fixed ###
- bugfixes for the ValuesStackFlipping
- bugfix for processing queue remove observer crash


## Anyline SDK 2.3 ##
Release Date 2014-08-05

### New ###
- modernised scripting language
- header part in scripting language
- support for encrypted command files
- support for reporting successful/unsuccessful scans
- specs are now defined with json
- validation delegate
- reporting KPIs

### Improved ###
- overall performance
- compatibility to android sdk

### Fixed ###
- various bugfixes


## Anyline SDK 2.2.1 ##
Release Date 2014-07-29

### Improved ###
- Exception handling
- Image processing clean ups

### Fixed ###
- Quality Calculation


## Anyline SDK 2.2 ##
Release Date 2014-07-14

### Improved ###
- better Image handling
- better thread managment
- better adapt digit positions
- overall performance improvements


## Anyline SDK 2.1 ##
Release Date 2014-06-04

### New ###
- iOS Interface Documentation added
- Anyline is now a fake dynamic library
- Anyline version & build number added
- Color Thresholding Operations
- Server Socket for Anylicious

### Fixed ###
- various bugfixes

### Improved ###
- better Image handling


## Anyline SDK 2.0 ##
Release Date 2014-04-29

### New ###
- Completely refactored the Anyline Framework.
- Anyline is now structured around Image Processing Operations.
- behaviour of Anyline is controlled over .alc command files.
- Simpler interface to communicate with Anyline.
- Overall faster performance.
- UI Stuff removed from Anyline binary.
- A lot of new Operations for the GPU.


## Anyline SDK 1.1 ##
Release Date 2014-02-05

### Improved ###
- Improved Display Overlay View with round corners.


## Anyline SDK 1.0 ##
Release Date 2014-01-10

### New ###
- Initial working version of Anyline for 7-segments.
