# SIM password wallet

[![CircleCI](https://img.shields.io/circleci/project/bertrandmartel/sim-password-wallet.svg?maxAge=2592000?style=plastic)](https://circleci.com/gh/bertrandmartel/sim-password-wallet)
[![Coverage Status](https://coveralls.io/repos/github/bertrandmartel/sim-password-wallet/badge.svg?branch=master)](https://coveralls.io/github/bertrandmartel/sim-password-wallet?branch=master)
[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](LICENSE.md)

SIM password wallet is an Android application interacting with a JavaCard applet installed on SIM card. This application stores a list of encrypted password. The encryption/decryption process only takes place on SIM card (UICC). It uses [Open Mobile API](https://github.com/seek-for-android/pool/wiki/SmartcardAPI) to communicate with SIM card.

2 modes are available :

* passwords stored on application (SQLite db)
* passwords stored on SIM card (EEPROM)

You can switch from one mode to another easily and data are migrated correctly from the app storage to SIM EEPROM and vice versa.

In both modes, AES 128 CBC is used on UICC to encrypt/decrypt passwords.

A pin code must be configured the first time user open the app. The pin code security part comes from [this tutorial from Eric Vétillard](https://github.com/bertrandmartel/javacard-tutorial#jc101-password-pin--password-application-with-pin-security). This pin code can be changed using the options menu.

![App preview](https://user-images.githubusercontent.com/5183022/30868430-e39796ba-a2de-11e7-94a3-a5d8fd33401e.gif)

## Build

```bash
git clone git@github.com:bertrandmartel/sim-password-wallet.git
cd sim-password-wallet
./gradlew clean build
```

* to use the emulator with pcsc support, check [these instructions](https://github.com/bertrandmartel/pcsc-android-emulator)

## External libraries

* [Lollipin](https://github.com/omadahealth/LolliPin)

## Dev libraries

* [javacard tutorial](https://github.com/bertrandmartel/javacard-tutorial)
* [seek for Android](https://github.com/seek-for-android/pool)
* [JavaCard Gradle plugin](https://github.com/bertrandmartel/javacard-gradle-plugin)
* [pcsc emulator](https://github.com/bertrandmartel/pcsc-android-emulator)

## License

* applet is released under MIT :

        The MIT License (MIT) Copyright (c) 2017 Bertrand Martel

* application is released under GPLV3 :

        Copyright (C) 2017  Bertrand Martel

        This program is free software; you can redistribute it and/or
        modify it under the terms of the GNU General Public License
        as published by the Free Software Foundation; either version 3
        of the License, or (at your option) any later version.

        SIM password wallet is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with SIM password wallet.  If not, see <http://www.gnu.org/licenses/>.
