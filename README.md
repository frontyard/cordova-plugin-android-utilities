[![MIT License](http://img.shields.io/badge/license-MIT-blue.svg?style=flat)](LICENSE) [![Build Status](https://travis-ci.org/frontyard/cordova-plugin-android-utilities.svg?branch=master)](https://travis-ci.org/frontyard/cordova-plugin-android-utilities) [![Code Climate](https://codeclimate.com/github/frontyard/cordova-plugin-android-utilities/badges/gpa.svg)](https://codeclimate.com/github/frontyard/cordova-plugin-android-utilities)

## Description
Collection of various utilities for Cordova/Phonegap apps.

## Functions

###  Create desktop shortcut for your Cordova app:
```js
window.AndroidUtilities.createDesktopShortcut(successCallback, errorCallback);
```

### Get application build info:
```js
window.AndroidUtilities.getApplicationInfo(successCallback, errorCallback)
```
successCallback will receive application build info through its first parameter. Following properties are available:
* debug
* displayName
* basePackageName
* packageName
* versionName
* versionCode
* buildType
* flavor

### Install apk:
```js
window.AndroidUtilities.installApk(fullPath, successCallback, errorCallback)
```

### Uninstall application:
```js
window.AndroidUtilities.uninstallApk(packageName, successCallback, errorCallback)
```

### Check if application is installed:
```js
window.AndroidUtilities.isApkInstalled(packageName, successCallback, errorCallback)
```

### Get media audio volume:
```js
window.AndroidUtilities.getAudioVolume(successCallback, errorCallback)
```
successCallback will receive int value between 0 and 100.

### Set media audio volume:
```js
window.AndroidUtilities.setAudioVolume(volume, successCallback, errorCallback)
```
volume is int value between 0 and 100.

## Installation

```sh
cordova plugin add cordova-plugin-android-utilities
```

## Supported Platforms

* Android
