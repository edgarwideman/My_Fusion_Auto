# My_Fusion_Auto

This is the app for the center dash display running on Android 9 or newer. This is part of a project that I am doing in my spare time to add a more feature rich and high tech experience to my 2010 Ford Fusion SEL. 

This project will be open sourced and free for anyone to use or reiterate as they please. For the maps to work you will have to create a file named "google_maps_key.xml" in the res/values folder. Go to https://console.cloud.google.com/google/maps-apis/ and generate your free Google Maps API key and enter it in the "YOUR_KEY_HERE" field in the file you just create.
```
<resources>
    <string name="google_maps_key" templateMergeStrategy="preserve" translatable="false">YOUR_KEY_HERE</string>
</resources>
```
