## Simulation Demo
WHCA\*-based path-finding algorithm solving bottlenecks (click to play video):
[![WHCA\*-based path-finding algorithm solving bottlenecks](http://img.youtube.com/vi/DRx-17AHaw4/0.jpg)](http://www.youtube.com/watch?v=DRx-17AHaw4 "WHCA\*-based path-finding algorithm solving bottlenecks (click to play video)")

Local-Repair A\* path-finding algorithm simulation (click to play video):
[![Local-Repair A\* path-finding algorithm simulation](http://img.youtube.com/vi/RVZFUfl6UFk/0.jpg)](http://www.youtube.com/watch?v=RVZFUfl6UFk "Local-Repair A\* path-finding algorithm simulation (click to play video)")

## How to build & run
In order to build and run application, you only need to have [JDK SE 8][1] and [Maven][2] already installed.
```bash
$ cd coop-pathfinder
$ mvn spring-boot:run
```
[1]: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
[2]: https://maven.apache.org

### Running pre-compiled jar
You can also run pre-compiled jar with only JRE installed:
```bash
$ cd coop-pathfinder
$ mvn package # building jar (optional)
$ java -jar target/coop-pathfinder.jar # running jar
```

## Used technologies
* Java 8 SE
* JavaFX
* Spring Framework
* Spring Boot
* Spring Boot JavaFx Support
* jUnit
* Maven
* Guava
* Logback
