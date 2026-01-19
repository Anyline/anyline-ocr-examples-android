# anyline-ocr-examples-android
Anyline - A powerful OCR SDK for Android

## File summary

* `AnylineExamples` - Example app source code
* `README.md` - This readme.
* `LICENSE.md` - The license file.

## License Key Setup

Before building the example app, you need to configure your Anyline license key.

### Option 1: Using Environment Variable (Recommended)

1. Set the `ANYLINE_MOBILE_SDK_LICENSE_KEY` environment variable in your shell profile (`~/.zshrc` or `~/.bashrc`):
   ```bash
   export ANYLINE_MOBILE_SDK_LICENSE_KEY='your-license-key-here'
   ```

2. Reload your shell or run `source ~/.zshrc`

3. Run the generation script:
   ```bash
   ./AnylineExamples/scripts/generate_license_key.sh
   ```

### Option 2: Manual Setup

1. Copy the template file:
   ```bash
   cp AnylineExamples/app/src/main/res/values/strings_license.xml.example \
      AnylineExamples/app/src/main/res/values/strings_license.xml
   ```

2. Edit `strings_license.xml` and replace `YOUR_LICENSE_KEY_HERE` with your actual license key

### Getting a License Key

Get your license key at: https://documentation.anyline.com/main-component/license-key-generation.html

## API Reference

The API reference for the Anyline SDK for Android can be found here: https://documentation.anyline.com/android-sdk-component/latest/index.html

## Getting Started

Check out our getting-started section here: https://documentation.anyline.com/android-sdk-component/latest/getting-started.html

## Documentation

Check out our developer guide here: https://documentation.anyline.com/android-sdk-component/latest/index.html

## License

See LICENSE file.