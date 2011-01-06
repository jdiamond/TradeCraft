TradeCraft
==========

This is a plugin for hMod that allows you to buy and sell items using gold
ingots (bars) as a currency.

Installation
============

The plugin is installed like any other hMod plugin. Put the TradeCraft.jar file
in your plugins folder and edit server.properties so that TradeCraft appears in
the plugins property.

The plugin is configured using a file called TradeCraft.txt that you need to
create in the same folder as your server.properties folder. Details on the
format of the file appear below.

There is also a file called TradeCraft.properties that you can put in the same
folder as your server.properties folder. You can set properties that configure
how the plugin works in this file. Look inside the example file that comes with
the plugin to see the properties you can set.

Shops
=====

Players buy and sell items at "shops".

To create a shop, place a chest (single, not double) next to a wall. Place a
sign on the wall behind the chest, but above it.

Here's the view of a shop from the side:

xs
xc

x = any block type that's not a sign or chest.
s = a sign block
c = a chest block

When placing the sign above the chest, right-click the wall block that would be
behind the sign.

There are two kinds of shops: Infinite shops and player-owned shops.

Infinite shops
==============

To create an infinite shop, put the name of the item type the shop sells
surrounded by square brackets on any line of the sign. The other lines can
contain any text you want.

For example, to create an infinite shop where players can buy and sell sand,
the text on the sign could look like this:

Buy and sell
[Sand]
here!

Or, it could just say this:

[Sand]

The item type has to be on a line all by itself and there can't be any spaces
outside or inside the square brackets. Case isn't important. The item types are
defined by you in the TradeCraft.txt file.

No state is maintained in infinite shops. Players can buy an infinite amount
of items (assuming they have enough gold) from infinite shops. They can also
sell an infinite amount of items (earning an infinite amount of gold).

TODO: Allow administrators to disable infinite shops using
TradeCraft.properties.

Player-owned shops
==================

Player-owned shops are created just like infinite shops, but the player who
builds the shop gets to decide the exchange rates.

Player-owned shops are also limited in the amount of items that can be bought
or sold there. The owner of the shop needs to deposit items and/or gold in
order to keep it in operation.

The format for the text on a player-owned shop must look something like this:

[Sand]
Buy for 32:1
Sell for 48:1
-injektilo-

The first line is the type of item bought and sold at that shop. This has to
be a type that's configured in TradeCraft.txt (even though buying and/or
selling that type of item from infinite shops may be disabled).

The second line contains the exchange rate (items to gold) for buying items
from that shop. The third line contains the exchange rate (items to gold) for
selling items from that shop. The "Buy for" and "Sell for" prefixes on both of
those lines is optional. Either line can be empty to disable buying or selling
at that shop.

The last line is the name of the player. It must be surrounded with dashes. If
the player's name is too long to fit on the line with the dashes, the player
can use any number of characters that will fit, but those characters have to
be part of their name. For example, if the player's name was
"NumberOneMinecraftFan", they could use "-NumberOne-" as the name on the sign.

TODO: Allow administrators to set aliases or nicknames for players for use on
signs.

Players are not allowed to create signs that contain other players' names.
Likewise, players are not allowed to destroy signs (or the chests underneath
them) containing other players' names.

Using shops
===========

To use the shop, you'll be placing items into the chest and right-clicking on
the sign above it.

To buy items, put some gold in the chest and then right-click the sign. If you
put enough gold in the chest to purchase at least one item, all of the gold in
the chest will be replaced with the items.

To sell items, put your items in the chest and then right-click the sign. If
you put enough items in the chest to earn at least one gold, all of the items
in the chest will be replaced with gold.

All of the items in the chest must be of the same type. They can be the type
indicated by the sign above the chest or all gold.

No "change" is given. If you put too much gold in the chest, the excess is
lost. Likewise, if you put more items in the chest than what would give you an
exact number of gold, the excess is lost.

Managing player-owned shops
===========================

The player who owns a shop can deposit and withdraw both items and gold from
the shops they own. They cannot buy or sell from their own shops.

When a player right-clicks the sign for a shop that he owns, the following
happens:

If the chest is empty and there is gold available to withdraw, the chest is
filled with that gold. Otherwise, any available items are withdrawn into the
chest.

If the chest only contains gold, the gold is deposited and any available items
are withdrawn.

If the chest only contains items, the items are deposited and the chest is
cleared.

The gold and items in a shop is NOT shared across all shops for that type of
item owned by that player. Each shop maintains its own state keyed off the
location of the sign and stored in TradeCraft.data.

Configuration
=============

To configure what can be traded and for how much (at infinite shops), you need
to edit TradeCraft.txt. The file should look like this:

# Comments look like this.
Sand,12,32:1
Diamond,264,1:64

The first value is the name of the item as you want it to appear on your signs
and in the messages the players see when they make their trades.

The second value is the block or item ID. You can see these values here:

http://www.minecraftwiki.net/wiki/Data_values

The third value is the exchange rate. The number before the colon is how many
of that item needs to be sold to earn the number of gold specified as the
number after the colon. In the above example, players have to sell 32 sand
blocks to get a single gold. They have to pay 64 gold to buy a single diamond.

You don't have to use the number 1 on either side of the colon. For example,
you could use a ratio like 3:2. That means that selling 3 items will get you 2
gold. Selling 6 items will get you 4 gold. Likewise, spending 2 gold will get
you 3 items and spending 4 gold will get you 6 items.

It's possible to configure separate exchange rates for buying and selling. If
only a single ratio is specified, that ratio is used for both buying and
selling. If two ratios are specified, the first is for buying and the second is
for selling.

For example, to let players buy 32 sand for 1 gold, but only be able to sell 64
sand for 1 gold, you would configure it like this:

Sand,12,32:1,64:1

If you use a ratio of 0:0, that disables buying or selling of that item type.

For example, to allow players to buy diamonds, but not sell them, you would
configure it like this:

Diamond,264,1:64,0:0

If you click on a sign above an empty chest, you'll see a message saying what
the exchange rates for both buying and selling are. This is only necessary for
infinite shops since player-owned shops have to display their exchange rates
on the sign.
