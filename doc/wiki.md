
## OnlineHanziRecognizer

### The project :

 This project is a "Java online Handwritten chinese character recognizer".
 While the user handwrites a chinese character using the mouse, the computer recognizes the handwritten character and display the results. 
 Every time the user adds a stroke, the computer restarts the recognition process and updates the results.
 The results are a list of most probable candidate character.
 Two interfaces are provided , a Swing UI and a web UI.
 
#### Screenshots swing UI
![](https://github.com/itanghiu/onlineHanziRecognizer/blob/master/doc/HanziRecogSwingUI.PNG)

#### Screenshots Web UI
![](https://github.com/itanghiu/onlineHanziRecognizer/blob/master/doc/OnlineHanziRecogWebUI.PNG)

#### Launching the Swing UI interface :

#####Through command line :

In a terminal :
- change directory to .. onlineHanziRecognizer\HanziRecog
- type in : mvn clean compile assembly:single
- change directory to .. onlineHanziRecognizer\HanziRecogSwingUI
- type in : mvn clean compile assembly:single
- type in : java -jar ./target/HanziRecogSwingUI-1.0-SNAPSHOT-jar-with-dependencies.jar

#####In IntelliJ editor:
Launch the class : hanzirecog.swingui.StartApp


#### Launching the web server

#####Through command line :

- change directory to .. onlineHanziRecognizer\HanziRecog
- type in : mvn clean compile assembly:single
- change directory to .. onlineHanziRecognizer
- type in : mvn package
- type in : java -jar .\target\hanziRecogWebServer-1.0-SNAPSHOT.jar
- In a browser, enter the url: http://localhost:8585/

#####In IntelliJ editor:

- Right-click on the class onlineHanziRecognizer\src\main\java\com\foryousoft\Application.java 
- choose "Run web server"


#### More on the project : 

the algorithm is based on the "HanziDict" project created by Jordan Kiang (jordan@kiang.org). 

The original project is at : http://www.kiang.org/jordan/software/hanzilookup/hanzidict.html

The goal of the project "OnlineHanziRecognizer" is to modernize "Hanzidict" by :
  - refactoring it  and benefit from the latest Java features,
  - using modern technologies like maven and Spring,
  - adding a web interface

#####Refactoring :
The initial project has been completely rewritten and refactorized:
  - the structure of the project has been completely modified. The engine code has been cleanly separated from the Swing UI code,
  - enums are used in the code as well as java 8 features like generics, lambdas or streams,
  - modern and useful frameworks like Spring and Maven have been used 
  
#####Web interface : 
In complement to the Swing interface initially present in the project, a web interface has been added.
The backend is based on the Spring framework, while the frontend relies on the javascript framework jsignature.

  
####Structure of the project:

The whole project is made up of three parts:
  - the main application is a web server,
  - the module "HanziRecog" is the recognition engine
  - the module "HanziRecogUI" is the Java Swing UI
  
 
 #### Code internals :
 
 ChineseCharController :
 This controller class is based on the Spring's DeferredResult class. This class allows to perform asynchronous request processing.
  "Asynchronous request processing"  makes it possible to process an incoming HTTP request in another thread, freeing the request receiver thread.
  By offloading the relatively long-running computation (chinese character recognition) from an http-worker thread t1 to a separate thread t2, 
  t1 is no longer blocked and can handle incoming client requests.
  This asynchronous request processing model helps scale an application well during high loads.
  
  The code that does all the heavy-lifting in the controller is the following one :
  
    DeferredResult<ResponseEntity<String>> deferredResult = new DeferredResult<>();
       CompletableFuture.supplyAsync(() -> hanziRecogSrv.recognizeHanzi(strokes))
               .whenComplete((charResults, throwable) -> {
                             ResponseEntity r =new ResponseEntity(charResults, HttpStatus.OK);
                             deferredResult.setResult(r); }
               );
       return deferredResult;

 CompletableFuture.supplyAsync() creates a new thread that will compute the relatively time-consuming recognition task.
 After the thread is created, the thread that handled this request is freed and can again handle new incoming request.
 .whenComplete() : when the computation is complete, the deferredResult is filled with the result. This result is finally sent back to the client who is notified of it through a callback.