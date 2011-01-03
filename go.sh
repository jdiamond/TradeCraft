BIN="/Users/jason/Downloads/Minecraft_mod 133/bin"
echo $BIN
cp TradeCraft.jar "$BIN/plugins"
cp TradeCraft.txt "$BIN"
cp TradeCraft.properties "$BIN"
pushd "$BIN"
./server_nogui.sh
popd
