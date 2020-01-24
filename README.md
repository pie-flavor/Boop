# Boop

Boop is a chat plugin that notifies you when you're mentioned in chat. By default, if you typed "`@pie_flavor`" in a chat message, and I was on the server, it would make a 'ping!' noise and highlight "`@pie_flavor`" in yellow. If you typed "`@staff`", it would 'ping!' all ops (or everyone who had the permission `boop.group.staff`).

### Configuration

`prefix`: This is the prefix that defines what is meant by 'mention'. Defaults to "@".  
`name{}`: If `recolor` is true, then the instance of the mention (e.g. `@pie_flavor`) is colored with the color `color`. If `color-all` is true, then the instance of the mention is colored with the color `alt-color` for everyone who is _not_ its target.  
`message{}`: If `recolor` is true, then the entire message will be colored `color`.  
`sound{}`: If `play` is true, then the sound `sound` will be played.  
`groups[]`: Each string in here represents a group of people that can be mentioned. A person is in a group if they have the permission node `boop.group.<group_name>`. If the group name contains dots, they are changed in the permission to be underscores.  
`title{}`: If `use` is true, then `text` will be shown to the player as a subtitle when they are mentioned.
`restricted[]`: Anything in this list has restricted usage for mentioning; the permission `boop.use.<boop_text>` will be required to boop them. For example, you may have a group `all`, and all users have `boop.group.all`; if you restrict `all`, and only give `boop.use.all` to admins, only admins will be able to boop via `@all`.
`aliases{}`: Here, you can supply aliases for players. For example, if I gave myself an alias `Owner` on my server, players typing `@Owner` would boop me. This is recommended over groups for single players.  
`blacklisted-channels[]`: A list of class names of channels to _not_ replace with a BoopableChannel. e.g. `io.github.nucleuspowered.nucleus.api.chat.NucleusChatChannel$StaffChat`

### Commands

`/sponge plugins reload` is supported, reloading the entire config.

### Changelog

1.0.0: \*uncontrollable dinging\*  
1.0.1: Changed the location of the config.  
1.1.0: Added groups.  
1.2.0: Added tab-completion for both players and groups; changed mentions to be case-insensitive.  
1.3.0: Added restriction and subtitles.  
1.3.1: Fixed bug where every message booped; changed 'stay' time of subtitle to 2 seconds.  
1.4.0: Added colors for non-targets.  
1.4.1: Fixed bug with config.  
1.4.2: Fixed bug with colors.  
1.4.3: Fixed bug with group permissions.  
1.4.4: Fixed a bug with disappearing boops.  
1.4.5: Fixed a bug with disappearing messages.  
1.5.0: Added aliases and made the channel usable by plugins.  
1.6.0: Added channel blacklists.

#### Note

This plugin uses bStats, which collects data about your server. This data is in no way intrusive, is completely anonymized, and has a negligible impact on server performance, so there is no reason whatsoever to disable it. However, if you wish to anyway, simply set `enabled` to `false` in `config/bStats/config.conf`.
