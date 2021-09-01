The TaxEd Suite
Suite of tools to hack RSDK based games
by nextvolume (tails92@gmail.com)

Release 0.9
2nd July 2014
-----------------

This is a suite of tools comprising a level editor and an animation editor
for games based on the Retro Sonic Engine (RSDK).
It supports editing the level maps for Sonic Nexus, Sonic CD, Sonic 1, Sonic 2
and Retro-Sonic (both the 2006 Dreamcast demo and the 2007 SAGE demo).

Obviously, you need to unpack the datafiles first before you can get to the maps,
use Retrun-Sonic (http://unhaut.fav.cc/retrun) for that 

TaxEd.jar is the premade Java Archive file for the application, run it using
the Java Virtual Machine installed on your computer.

A dialog prompting you to choose what tool to use will appear on the screen.
There are right now two choices available: Level Editor and Animation Editor.
New tools may be added in the future as more aspects of the games are understood.

If you are hacking Sonic Nexus, you need to decrypt the files to modify them,
and encrypt them back again manually;
see http://unhaut.fav.cc/retrun/nexus.html for more information.

******
Level Editor
******

The program is quite easy to use, and should be not different to what you expect
from a level editor.

Objects are not yet displayed with the image for their type, so right now
they are displayed as 24x32 green rectangles.
More reverse engineering needs to be done in order to provide that feature.

Many things are still not implemented: backgrounds and collision blocks are
notably unsupported.

******
Animation Editor
******

This program, like the rest of the suite, is meant to be easy to use, 
at least to people who are accustomed to other editing tools and who know what
they are doing.

The Animation Editor is mostly feature-complete, it supports loading and saving
animations in Sonic 1/CD/2 format.

Importing animations from Retro-Sonic for Dreamcast, Retro-Sonic SAGE 2007 Demo,
and Sonic Nexus SAGE 2008 Demo is also supported, although results might not 
be fully correct. Internally such animations are translated to Sonic 1/CD/2 format.
Saving such imported animations in Sonic 1/CD/2 format might work, but whether
they are accepted by the games is untested.

It is possible to export animations as JSON data, this is especially useful
for Web applications or to avoid yet another format.

It is obviously also possible to use this animation editor for your own purposes
and projects that are totally unrelated to game modification. 
There is no real dependency on anything RSDK-related, as the animation format is 
neutral enough it could be used by mostly anything, and while this format has its
limitations, it is good enough for a platformer and most games.

Look at the source code for information about the animation format, or you
can just work on animation files, then export to JSON data for use by your project.

******
Other tools
******

These tools do not have an user interface and only work on the command line.

RGFX = Image format used by Retro-Sonic SAGE 2006 (Dreamcast) and 2007 demos

img2rgfx: Convert an image to RGFX format image
rgfx2img: Convert a RGFX format image to an image

These tools can be run like this:
java -cp TaxEd.jar tools.TOOL_NAME [... arguments ... ]

where TOOL_NAME is the name of the tool. For example to run img2rgfx:
java -cp TaxEd.jar tools.img2rgfx

******
BUILDING
******

This suite is built by using Apache Ant (http://ant.apache.org).
``ant'' will build the suite.
``ant clean'' will clean the distribution.

******
ENDING WORDS...
******

Better documentation in the future, for now, enjoy playing and hacking at this.

P.S.: You need to repack the datafiles, and to recreate the APK/IPA/etc.
to play the modified levels for those versions. That's not for novice users.
I will provide better documentation about that in the future, as well.
