BIN="/Users/jason/Downloads/Minecraft_mod 131/bin"
echo $BIN
cp TradeCraft.jar "$BIN/plugins"
cp TradeCraft.txt "$BIN"
pushd "$BIN"
./server_nogui.sh
popd
