### Online Hanzi recongnizer

#### Launching the Swing UI interface :

#####Through command line :

In a terminal :
- change directory to .. onlineHanziRecognizer\
- type in : mvn clean compile assembly:single
- change directory to .. onlineHanziRecognizer\HanziRecogSwingUI
- type in : mvn clean compile assembly:single
- type in : java -jar ./target/HanziRecogSwingUI-1.0-SNAPSHOT-jar-with-dependencies.jar

#####In IntelliJ editor:
Launch the class : hanzirecog.swingui.StartApp


#### Launching the web server

#####Through command line :

- change directory to .. onlineHanziRecognizer
- type in : mvn package
- type in : java -jar .\target\hanziRecogWebServer-1.0-SNAPSHOT.jar
- In a browser, enter the url: http://localhost:8585/

#####In IntelliJ editor:

- Right-click on the class onlineHanziRecognizer\src\main\java\com\foryousoft\Application.java 
- choose "Run web server"