# Hostel Management Mobile App

This project is an Android application designed for managing hostel operations, featuring user login, registration, and secure authentication using PIN and fingerprint biometrics.

## Features
- User registration and login
- PIN-based authentication with a custom number pad
- Fingerprint authentication support
- Integration with MongoDB for backend data storage
- Built using AndroidX and modern Android APIs

## Project Structure
- **app/**: Contains the Android app source code including UI components and authentication logic.
- **gradlew / gradlew.bat**: Gradle wrapper scripts for Unix and Windows environments.
- **gradle.properties**: Project-wide Gradle settings.
- **.idea/**: IDE configuration files.
- **proguard-rules.pro**: ProGuard configuration file.

## Prerequisites
- Java Development Kit (JDK) 17 or higher. Ensure that `JAVA_HOME` is correctly set.
- Gradle (the project includes Gradle wrappers for ease of use).

## Build and Run
1. Clone the repository:
    ```
    git clone <repository_url>
    ```
2. Navigate to the project directory:
    ```
    cd hostel-management-mobile-app
    ```
3. Build the project using Gradle:
    ```
    ./gradlew build
    ```
4. Deploy the app to your Android device or emulator using Android Studio or the command line.

## Configuration
- Adjust JVM options and Gradle settings in `gradle.properties` as needed.
- Ensure proper setup of environment variables, especially `JAVA_HOME`.

## License
This project is licensed under the Apache License, Version 2.0.
