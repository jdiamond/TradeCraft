#!/bin/sh

javac -cp Minecraft_Mod.jar *.java

jar cvf TradeCraft.jar *.class > /dev/null

echo Built!
