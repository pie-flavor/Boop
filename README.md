# Boop

Boop is a chat plugin that notifies you when you're mentioned in chat. By default, if you typed "`@pie_flavor`" in a chat message, and I was on the server, it would make a 'ping!' noise and highlight "`@pie_flavor`" in yellow. If you typed "`@staff`", it would 'ping!' all ops (or everyone who had the permission `boop.group.staff`).

### Configuration

`prefix`: This is the prefix that defines what is meant by 'mention'. Defaults to "@".

`name{}`: If `recolor` is true, then the instance of the mention (e.g. `@pie_flavor`) is colored with the color `color`. If `color-all` is true, then the instance of the mention is colored with the color `alt-color` for everyone who is _not_ its target.

`message{}`: If `recolor` is true, then the entire message will be colored `color`.

`sound{}`: If `play` is true, then the sound `sound` will be played.

`groups[]`: Each string in here represents a group of people that can be mentioned. A person is in a group if they have the permission node `boop.group.<group_name>`. If the group name contains dots, they are changed in the permission to be underscores.

### Commands

`/sponge plugins reload` is supported, reloading the entire config.

### Changelog

1.0.0: \*uncontrollable dinging\*

1.0.1: Changed the location of the config.

1.1.0: Added groups.

1.2.0: Added tab-completion for both players and groups; changed mentions to be case-insensitive.

1.3.0: Added restriction and subtitles

1.3.1: Fixed bug where every message booped; changed 'stay' time of subtitle to 2 seconds

1.4.0: Added colors for non-targets.

1.4.1: Fixed bug with config.
