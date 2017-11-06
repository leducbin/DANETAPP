Android Whitelabel App (DANET)
==============================

This Android project is Movideo's white label app project which customised for BHD and build Danet Android App.

Deployment
----------

The project can be opened through Android Studio and it is build using the Gradle build system.

Technical Details
-----------------
* Application Id: "com.movideo.whitelabel"
* Minimum Sdk Version: 17
* Target Sdk Version: 23
* Version Code: 9
* Version Name: "1.0.8"
* Java version: 1.7

##### Permissions Used

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

Dependencies & Local Repositories
---------------------------------

###### Baracus-Android-SDK (/repository)
Android library project under Movideo which handles all the REST communications with backend API and holds the main model classes used in the app.

###### Movideo-Player-SDK (/repository)
Android library project under Movideo which handles video content playback functionalities from streaming the web sources.

###### Butter Knife (com.jakewharton:butterknife:7.0.1)
Butter Knife finds and automatically cast the corresponding view in your layout by using annotation fields with @Bind and a view ID.

###### Picasso (com.squareup.picasso:picasso:2.5.2)
Loads images from  URL to ImageViews.

###### Facebook SDK (com.facebook.android:facebook-android-sdk:4.7.0)
Used for Facebook login.

###### Hockey App SDK (net.hockeyapp.android:HockeySDK:3.6.0)
Integrated to track and push test version and to obtain crash reports.

###### Circular Viewpager (com.github.tobiasbuchholz:circularviewpager:1.0.0)
Handles looper image carousal in viewpager used in home page.

#### Reference to Gradle
```gradle
repositories {
    mavenCentral()
    maven {
        url '../repository'
    }
}

dependencies {
    compile('com.movideo:movideo-player-sdk:1.0.0') {
        exclude group: 'xmlpull'
    }

    compile 'com.jakewharton:butterknife:7.0.1'
    compile 'com.squareup.picasso:picasso:2.5.2'
    compile 'com.facebook.android:facebook-android-sdk:4.7.0'
    compile 'net.hockeyapp.android:HockeySDK:3.6.0'
    compile 'com.github.tobiasbuchholz:circularviewpager:1.0.0'
    compile 'com.android.support:support-v4:23.1.1'
}
```