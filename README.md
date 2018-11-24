## Short description
This program is an interpreter for the MiMa (Minimal Maschine),
which is basically a very, very simplified model of a Von-Neumann machine.

This program focuses on interpreting MiMa assembly however,
mostly ignoring how the machine is actually implemented in hardware.

## Features
* Viewing registers (Instruction pointer, Instruction and Accumulator) and memory
* Highlighted position in the memory for the next instruction that will be executed
* Central pane with syntax highlighting in which you can write a program,
  which can then be executed
* Execute a program until it completes (maybe even gracefuklly)
* Execute a program until the next breakpoint (added by clicking on a line number in the gutter)
* Execute a program step by step (forwards and backwards) at any point in time
  (So after hitting a breakpoint or just from the beginning / end)

* Loading programs from disk (though not writing as of now)

## Screenshots
![Program screenshot](/images/Main_screen.jpg?raw=true "The main program screen")

## Feature requests and bugs
Please open an [issue](https://github.com/I-Al-Istannen/MiMaInterpreter/issues/new/new) :)
