@echo off

if not exist Minecraft_Mod.jar echo Minecraft_Mod.jar is missing! && exit /b /1
if not exist minecraft_server.jar echo minecraft_server.jar is missing! && exit /b /1

javac -cp Minecraft_Mod.jar TradeCraft.java
if errorlevel 1 exit /b 1

jar cvf TradeCraft.jar *.class > nul

echo Built!
