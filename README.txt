This is a plugin for hMod that allows you to buy and sell items using gold
ingots as a currency.

This plugin does not maintain any state. Players are forced to carry their gold
ingots around with them. If they don't want them stolen, they shouldn't leave
them where they can be stolen.

This plugin models an open economy. Players can purchase an infinite amount of
items if they have enough gold.

You, the server administrator, get to control what items can be traded and for
how much.

The plugin is installed like any other hMod plugin. Put the TradeCraft.jar file
in your plugins folder and edit server.properties so that TradeCraft appears in
the plugins property.

The plugin is configured using a file called TradeCraft.txt that you need to
create in the same folder as your server.properties folder. Details on the
format of the file appear below.

To create a "store", place a chest (single, not double) next to a wall. Place a
sign on the wall behind the chest, but above it. The text for the sign can say
whatever you want, but one of the lines needs to contain the item type that can
be traded at that "store" surrounded by square brackets.

For example, if you want to create a "store" that lets you buy and sell sand,
the sign could say this:

Buy and sell
[Sand]
here!

Or it could just say this:

[Sand]

The item type has to be on a line all by itself and there can't be any spaces
outside or inside the square brackets. Case isn't important. The item types are
defined by you in the TradeCraft.txt file.

To use the "store", you'll be placing items into the chest and right-clicking
on the sign above it.

To sell items, put your items in the chest and then right-click the sign. If
you put enough items in the chest to earn at least one gold ingot, all of the
items in the chest will be replaced with gold ingots.

To buy items, put some gold ingots in the chest and then right-click the sign.
If you put enough gold ingots in the chest to purchase at least one item, all
of the gold ingots in the chest will be replaced with the items.

All of the items in the chest must be of the same type. They can be the type
indicated by the sign above the chest or all gold ingots.

No "change" is given. If you put too many gold ingots in the chest, the excess
is lost. Likewise, if you put more items in the chest than what would give you
an exact number of gold ingots, the excess is lost.

To configure what can be traded and for how much, you need to edit
TradeCraft.txt. The file should look like this:

# Comments look like this.
Sand,12,32:1
Diamond,264,1:64

The first value is the name of the item as you want it to appear on your signs
and in the messages the players see when they make their trades.

The second value is the block or item ID. You can see these values here:

http://www.minecraftwiki.net/wiki/Data_values

The last value is the exchange rate. The number before the colon is how many of
that item needs to be sold to earn the number of gold ingots specified as the
number after the colon. In the above example, players have to sell 32 sand
blocks to get a single gold ingot. They have to pay 64 gold ingots to buy a
single diamond.

You don't have to use the number 1 on either side of the colon. For example,
you could use a ratio like 3:2. That means that selling 3 items will get you 2
gold ingots. Selling 6 items will get you 4 gold ingots. Likewise, spending 2
gold ingots will get you 3 items and spending 4 gold ingots will get you 6
items.

There is currently no way to see the configured exchange rates in the game.
Put it on the signs you place above your chests.

