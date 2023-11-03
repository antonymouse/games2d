#  The main classes
The diagram below shows the main classes of the game. The diagram was done in drawio, so there is no shared model 
between the code and the diagram. The relations in the diagrams don't exactly represent variables in code.

## Explanations
### Level
Level class is the main container for all activity happening within this game level. The class reads its own 
configuration from the text file. The configuration describes the level's map, including all sprites that can show on
this level. The dependency injection framework (Guice) also injects all Interactions (more below) into the Level. Level
uses a provider (also injected by the DI) to create all sprites, which configure themselves according to the configuration
files they find in the injected directory. Then on every game tick the class simply calculates the sprite-to-sprite
distance matrix, calls all sprites passing the time and any messages from the keyboard (or other input device).
### Sprite
Sprite is responsible for checking up with the associated behaviors and drawing the right animations in the right place.
The differences between Sprites are in their configuration and their behavior. The configuration includes animations
associated with Sprites, awareness radii and so on. The behavior is separated between classes implementing BehaviorStyle
or Interaction interfaces. 
### BehaviorStyle
The implementations are concerned with the Sprite's standalone behavior. I.e. the behavior in absence of interactions
with any other sprites. An implementation then doesn't need to be concerned with anything, but Sprite's own properties.
### Interaction
The implementations are concerned with Sprites behavior in interaction with any other Sprites. They also need to decide
if an interaction is actually taking place by analysing information from both Sprites: the original sprite the 
Interaction is actually attached to and the candidate Sprite it might be interacting with. To minimize the knowledge
a Sprite needs to decide on these things, an Interaction has 2-step configuration.
A Level configures an Interaction with a map of Sprite-Sprite distances and the list of all Sprites on this Level. The
Sprite it's attached to then configures an Interaction with Sprite-specific data. Behaviors and Interactions are invoked
by the Sprite they are attached to on every game time tick.