### MatchThreeFX
Simple puzzle game about matching three or more tiles with the same colour.

<img src="https://user-images.githubusercontent.com/17854950/90309812-01ad3c80-deec-11ea-80a4-fa7b35df2c26.png" width="300px">

### Building .jar package
To compile and run this source, you need an implementation of  [Java Development Kit](https://cs.wikipedia.org/wiki/JDK) at least version 11 and appropriate [JavaFX](https://gluonhq.com/products/javafx) installed in your system.

Firstly create environmental variable containing path to `javafx/lib` for later usage:
```
$ FX_LIB="/path/to/javafx/lib"
```

Then compile sources into the build directory:
```
$ javac -d ./build --module-path=$FX_LIB --add-modules=javafx.controls src/*.java
```

Finally create a .jar package:
```
$ cd build
$ jar cvfm MatchThreeFX.jar ../src/Manifest.txt *
```

Now you can run application using created **MatchThreeFX.jar** file:
```
$ java --module-path=$FX_LIB --add-modules=javafx.controls -jar MatchThreeFX.jar
```
