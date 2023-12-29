# games2d
## The goal
The goal of this project is to explore gaming platform development in Java. A lot of people do that, there are many
school courses, textbooks, and commercial projects. My specific goals to see what kind of design abstractions the
design based on general design principles used in regular software development will lead me to. The typical approach of 
game engines is to optimize as much as they can and ignore the principles of software engineering. I want to do the
opposite.
Delivering complete products isn't the goal. I want to get the code to the point where it executes and demonstrates the
correct behaviors for most interesting game platform use-cases. By "platform" I mean an execution environment for a
certain class of games (for example, 2d platformers). Such execution environment should be able to play any game of this
class mostly defined by configuration. Of course, a configuration is also "the code". In this case, though, the
configuration should be really simple and held by external ascii files.
## Design and Structure
These concepts are addressed by documentation in docs folder.
## Platform development news
### 12/27/23
I added the horizontal scrolling, so the level is now more functional. The "game" is currently not fully implemented: there 
is no scoring, no level reloading, no damage counting and so on. However if anyone needs the missing functionality and wants 
to use the code please don't hesitate to let me know by opening an issue or emailing. At very least I'll be happy to look at your 
pull request and most likely I'll just add the missing functionality myself.
### 12/19/2023
The platform supports a game field with various types of cells and npcs. Npcs interact with players. Npcs, players, and
game field is fully configurable. The NPCs and player's behavior is customizable, but relies on code components for
implementation.
Constraints:
1. The level map doesn't scroll
2. There is only 1 resolution.
### 10/27/2023
Currently the game is constrained in the following ways:
1. It's a tile-based game
2. The level map doesn't scroll
3. There is only 1 sprite (active character), no NPC
4. The choice of the default frame to show for the sprite is hard-coded
5. There is only 1 resolution.
