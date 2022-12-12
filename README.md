# LaserBeamer
A java application to use a video projector (beamer) like a show laser.

>The compiled application can be found on the release page "[LaserBeamer.jar](https://github.com/ModellbahnFreak/LaserBeamer/releases)". It requires Java 17 or higher to run. *Detailed instructions in the Wiki*

For information on how to use the LaserBeamer-Software, look at the [LaserBeamer wiki](https://github.com/ModellbahnFreak/LaserBeamer/wiki)

The project was inspired by many different light shows and especially by the program ["Beamertool /Lasersim für Raspberry Pi"](https://forum.dmxcontrol-projects.org/index.php?thread/8091-beamertool-lasersim-f%C3%BCr-raspberry-pi-support-thread/&pageNo=1)


*Note: The creator of the application takes no responsibilities for anything caused by the usage of the program or the app in any form.*


## Packaging
Have Java 17 installed and in PATH

Unix:
```bash
./gradlew clean build fatJar
```

Windows:
```bash
.\gradlew.bat clean build fatJar
```

Find the file `LaserBeamer-VERSION-all.jar` in the directory `build/libs`