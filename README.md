[![Build Status](http://ci.ialistannen.de/buildStatus/icon?job=MiMaInterpreter)](http://ci.ialistannen.de/job/MiMaInterpreter/)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=MiMaInterpreter&metric=alert_status)](https://sonarcloud.io/dashboard?id=MiMaInterpreter)

## Short description
This program is an interpreter for the MiMa (Minimal Maschine),
which is basically a very, very simplified model of a Von-Neumann machine.

This program focuses on interpreting MiMa assembly,
and mostly ignores how the machine is actually implemented in hardware.

## Features
* Viewing registers (Instruction Pointer, Instruction and Accumulator) and memory
* Highlighted instruction pointer position in the memory view
* Central pane with syntax highlighting for writing your program
* Execute a program until it completes (maybe even gracefully)
* Execute a program until the next breakpoint (added by clicking on a line number in the gutter) is hit
* Execute a program step by step (forwards and backwards) at any point in time
  (So after hitting a breakpoint or just from the beginning / end)

* Load a program from disk (though you can not write it again as of now)

## Screenshots
![Program screenshot](/images/Main_screen.jpg?raw=true "The main program screen")

## Feature requests and bugs
Please open an [issue](https://github.com/I-Al-Istannen/MiMaInterpreter/issues/new/new) :)

## Requirements
* Java **8** with JavaFx (you will have JavaFx if you use the oracle JRE, so windows users should have it by default.
  On Linux install `openjfx` from the package manager of your choice.)
* Note that this program is *incompatible* with newer java versions, due to JFoenix being unable to cope

## Download
[![Download](https://media-elerium.cursecdn.com/attachments/202/434/jenkins.png)](http://ci.ialistannen.de/job/MiMaInterpreter/)
