# StaffSecure
Forces users with a given permission to login on join if their IP-Address changed. Yea, may be a bad plugin...

## Permissions
The permissions for this plugin can be changed in the config.yml

## Messages
This plugin has an extensive language file. Using that and the config (where it is explained), you can change pretty much every message there is. 
It also allows for some nice things like embedding other keys or modifying how a number, date, percentage or other things look like

## Password storage
The passwords are stored hashed and if I didn't mess up shouldn't be too easy to crack.  
This means that even the server owner should NOT be able to get your passwords.

As a consequence, the password can only be reset. This is where the `/staffSecure getPin` command comes into play.  
If you execute that command, a message will be send to the console with an unique token.  
Using that token you can reset your password with the `/staffSecure pin <pin> <new password>` command without angle brackets.

## Idea
This plugin was made after a plugin request on the bukkit forums [here](https://bukkit.org/threads/staff-logins.435080/).

## Dependencies
This plugin depends on [PerceiveCore](https://github.com/PerceiveDev/PerceiveCore), at [this state](https://github.com/PerceiveDev/PerceiveCore/tree/103fbaf43f40df9b18b20db2b461e3d6ddce0281).
The download for it will be found together with the first release on this repository.
