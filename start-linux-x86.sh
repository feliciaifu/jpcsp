#!/bin/sh
java -Xmx1024m -Xss2m -XX:ReservedCodeCacheSize=64m -Djava.library.path=lib/linux-x86:lib/jinput-2.0.9-natives-all -classpath "bin/jpcsp.jar:lib/lwjgl-3.2.3/lwjgl.jar:lib/lwjgl-3.2.3/lwjgl-openal.jar:lib/lwjgl-3.2.3/lwjgl-opengl.jar:lib/lwjgl-3.2.3/lwjgl-jawt.jar:lib/lwjgl-3.2.3/lwjgl-natives-linux.jar:lib/lwjgl-3.2.3/lwjgl-openal-natives-linux.jar:lib/lwjgl-3.2.3/lwjgl-opengl-natives-linux.jar" jpcsp.MainGUI $@
