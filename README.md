[![MIT License](http://img.shields.io/badge/license-MIT-blue.svg?style=flat)](LICENSE) [![Build Status](https://travis-ci.org/frontyard/cordova-plugin-android-utilities.svg?branch=master)](https://travis-ci.org/frontyard/cordova-plugin-android-utilities) [![Code Climate](https://codeclimate.com/github/frontyard/cordova-plugin-android-utilities/badges/gpa.svg)](https://codeclimate.com/github/frontyard/cordova-plugin-android-utilities)
# cordova-plugin-android-utilities

Collection of various utilities for Cordova/Phonegap apps.

Create desktop shortcut for your Cordova app:
```js
window.AndroidUtilities.createDesktopShortcut(successCallback, errorCallback);
```

Get application build info
```js
window.AndroidUtilities.getApplicationInfo(successCallback, errorCallback)
```
successCallback will receive application build info through it's first parameter. Following properties are available:
* debug
* displayName
* basePackageName
* packageName
* versionName
* versionCode
* buildType
* flavor

## Installation

```sh
cordova plugin add cordova-plugin-android-utilities
```

## Supported Platforms

* Android
