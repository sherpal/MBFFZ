# MBFFZ

This is an example of a (real time) multi-player
game server using the amazing
[cask](http://www.lihaoyi.com/cask/).


The game frontend uses
[Scala.js](http://www.scala-js.org/)
as well as the
[Laminar](https://github.com/raquo/Laminar)
framework,
and the (real time) communication uses
[boopickle](https://github.com/suzaku-io/boopickle)
via WebSockets.

## The game

The game essentially serves as a pretext for
this example. Nonetheless, it's quite fun! The
more players, the funnier. (Currently only 10 players
can play together.)


The game takes place in a 800x600 continuous 2d
plane. Each player is free to move (using the arrow keys),
except for some white obstacles.


Each now and then, triangles (zombies) appear 
and chase at all times
the closest player (that is, they change their mind
all the time). 
Once a zombie collides a player,
that player dies instantly.


The last player alive wins the game.


The number of zombies that spawn at the same
time is given by the formula
`nbrOfPlayers + (timeFromBeginningInSeconds) / 30`
where the division is an integer division.


The time (in seconds) between two zombie spawn
is given by the formula
`max(2.5, 10 - timeFromBeginningInMinutes)`.

## How to play

In order to play, you'll need to

- clone this repository (`git clone https://github.com/sherpal/MBFFZ`)
- [sbt](https://www.scala-sbt.org/)
- change the `hostName` in
`shared/src/main/scala/utils/Constants.scala`
to your IP address
- go to sbt, and uses the `fullOptCompileCopy` task
(this is used to make the frontend JavaScript file
using Scala.js, and to copy it in `backend/src/main/resources/frontend`)
- use the `run` sbt task.


Now every player may go in a browser to
`http://[host]:8080/`. /!\ They must be on the
same wifi, otherwise you'll need to do more!
Note that the players don't need to have anything
installed, only the computer server has to have sbt
installed.

### Bugs

The game is still a bit buggy and will need
optimisation in the future.


### UX

The UX is very basic and you need some serious
lifting.
