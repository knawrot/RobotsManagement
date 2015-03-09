#Mobile Robot Management Application for Android-base Device#

The project has two contributors:
- Krzysztof Nawrot (the owner of this repository)
- Michal Lorens

 
##Brief description
Application serves as a utility to manage a group of robots 
(see [Project Capo](http://project-capo.github.io/) for more details) 
located within a defined area (specified by the map used; 
by default - AGH, D17, second floor, "Laboratorium Robotów").
Developed for both inside (map in *.json* format) and outside (*Google Maps*) 
purpouses.



##Specification

#####Target device
Despite the variety of Android-based devices nowadays, 
the best results occur when running the application on a tablet. 
Tested on *ASUS Transformer TF101G.*

#####Target OS
Application is only compatibile with Android 4.0 or higher.

#####Other requirements
- robots and the application within the same WiFi connection
- Google Play Services installed on a device



##How to run an application

There is no standalone version (*.apk*) for this project. 
If you want to run it on your device, you frist need to *set up your workspace*
and then compile it on your own.



##How to set up a workspace

In order to set up a workspace you first need to download ADT ([Android Development Tools](http://developer.android.com/tools/help/adt.html)). Once you integrate your IDE with ADT you will need two aditional dependencies (imported as separate projects - if using Eclipse):
- [Android Support Library](http://developer.android.com/tools/support-library/index.html) - download via Android site
- [Google Play Services](https://developer.android.com/google/play-services/index.html)

Please also note, that the application uses Android SDK v14.
