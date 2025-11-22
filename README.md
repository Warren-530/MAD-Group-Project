# MAD-Group-Project
Repository for WIA2007 Mobile Application Development Group Project - Maggie Goreng

## Team Information
This is a collaborative project for 6 team members developing a native Android application using Android Studio with Java and XML.

## Project Setup

### Prerequisites
- Android Studio (latest version recommended)
- Java Development Kit (JDK) 8 or higher
- Android SDK with API level 34

### Opening the Project
1. Clone this repository
2. Open Android Studio
3. Select "Open an existing project"
4. Navigate to the cloned repository folder and select it
5. Wait for Gradle to sync

### Building the Project
You can build the project using:
- Android Studio: Build > Make Project
- Command line: `./gradlew build` (Linux/Mac) or `gradlew.bat build` (Windows)

### Running the App
1. Connect an Android device or start an emulator
2. Click the "Run" button in Android Studio or use `./gradlew installDebug`

## Project Structure
```
app/
├── src/
│   └── main/
│       ├── java/com/maggiegoreng/madproject/
│       │   └── MainActivity.java
│       ├── res/
│       │   ├── layout/
│       │   │   └── activity_main.xml
│       │   ├── values/
│       │   │   ├── colors.xml
│       │   │   ├── strings.xml
│       │   │   └── themes.xml
│       │   └── mipmap-*/
│       └── AndroidManifest.xml
├── build.gradle
└── proguard-rules.pro
```

## Contributing
All team members should:
1. Create feature branches for new work
2. Follow Java coding conventions
3. Test changes before pushing
4. Create pull requests for review

## License
This project is licensed under the MIT License - see the LICENSE file for details.

