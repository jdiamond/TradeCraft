set BIN=C:\Downloads\Minecraft_mod 133\bin
copy TradeCraft.jar "%BIN%\plugins"
copy TradeCraft.txt "%BIN%"
copy TradeCraft.properties "%BIN%"
pushd "%BIN%"
call server_nogui.bat
popd
