set BIN=C:\Downloads\Minecraft_mod 129\bin
copy TradeCraft.jar "%BIN%\plugins"
copy TradeCraft.txt "%BIN%"
pushd "%BIN%"
call server_nogui.bat
popd
