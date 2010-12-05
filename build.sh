#!/bin/sh

javac -cp Minecraft_Mod.jar TradeCraft.java

jar cvf TradeCraft.jar *.class > /dev/null

echo Built!
