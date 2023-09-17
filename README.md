# Live Football World Cup Score Board

## Current stable version: **v1.0**
## Description

This library provides functionality of live scoreboard for Soccer World Cup tournaments.

- It allows clients to **start** new matches, **update** their score and **finish** the matches.
- It provides also functionality to provide **summary of ongoing live** matches.

### How to use the library

- Requirements
  - Project needs to use `Java 14` or higher

### API

### How to use library

```java
import org.sportradar.soccer.worldcup.LiveScoreboard;
import org.sportradar.soccer.worldcup.Summary;

//...........

LiveScoreboard board = LiveScoreboard.getInstance(); //provides new instance

board.startMatch("TEAM_A","TEAM_B"); //starts new match
board.startMatch("TEAM_C","TEAM_D");

board.updateScore("TEAM_A",1,"TEAM_B",2); //update score of the match

board.finishMatch("TEAM_A","TEAM_B"); //finish the match

List<Summary.Score>summary = board.getSummary().getScores() //provides current list of scores

```

### Current constraints

#### Team names data unification and validation
> **IMPORTANT**  
> Library is not responsible for any incoming data unification or checking its functional correctness.
> This responsibility is on the clients side.
> This is very important especially for String based team names.

For the `LiveScoreboard` class those cases are perfectly fine, and given matches would be added to the board, even though teams are already on the board.

```java

board.startMatch("Poland","England"); 
board.startMatch("polanD","england"); //new match is going to be properly started, 2 matches on board
```
#### Penalty shootout support
At this moment there is **no option to record penalty shootout** scores alongside match score from the regular time or overtime.

This functionality is going to be added in **v1.1** of the library, hopefully it would come before World Cup knockout stage starts ;)

#### API constraints

##### ``startMatch(String homeTeam, String awayTeam)``
- any of provided teams cannot be already part of other match on the board, otherwise `IllegalStateException` is thrown

##### ``updateScore(String homeTeam, int homeScore, String awayTeam, int awayScore)``
- score values needs to be positive integers (including 0), otherwise `IllegalArgumentException` is thrown
- match needs to already started and be on the board, otherwise `IllegalStateException` is thrown


##### ``finishMatch(String homeTeam, String awayTeam)``
- no specific constraints
- **Note:** If there is no match on the board for given teams method will not report any errors, operation would be successful

## Contributing
Check [CONTIRBUTING.md](CONTRIBUTING.md)

## Technical notes

### KISS
As it was stated in technical requirements **v1.0** version is build in the simple/compact way
- Vanilla Java code was used, no external libraries (like `Lombok` ect)
- Library does not use any dedicated Exception classes. It takes advantage of build in `IllegalArgumentException` and `IllegalStateException`
- There is only one public facade class exposed to the clients, `LiveScoreboard` class. This faced returns instance of `Summary`, which is "view" from which clients can read the data.
  - it was important to wrap `List<Score>` with additional class, it would allow extending/adding new information/data in the summary in non-breaking way.
- data structures and its usage were designed **mainly** for the `getSummary()` method business requirements, if there would be other
  requirements in the future, like returning scores in different order, this would need additional development.
- no specific interfaces were defined
  - there is internal `InMemoryMatchRepository` used, which at this moment is just purely used as internal implementation detail of `LiveScoreboard` class. There was no need to define interface for it.
  - however if in the future other data sources would be taken into consideration (Redis, MongoDB etc) then it would
    make sens to define common `MatchRepository`interface.
- `Match` class is `package-private` class, it could be also defined as `inner` class of `LiveScorebord` class, however due to the fact that it would add quite a lot of lines of code ther the decision was to keep it in separate file. Maybe after adding `Lombok` and reducing boilerplate code, then it could be transformed into inner class.

### Performance
> **NOTE**  
> Consideration characteristics of World Cup tournament (amount of matches happening at the same time) and how often scores are changing during the game, the assumption was made that **write** (start, finish, update) operations are going to happen much less often than **read** (getSummary) operations.
> However the library needs to provide data consistency to make sure that scores are correct.

**Data consistency** is going to be provided by usage of thread safe [SynchronizedMap](https://docs.oracle.com/javase/8/docs/api/java/util/Collections.html#synchronizedMap-java.util.Map-) implementation. It would have minimal overhead on the write operations performance, but this is not a factor in Scoreboard use case at this moment.

**Read performance**
`InMemoryMatchRepository` class is using cache approach to keep already properly sorted set of data, ready to be returned to the client. This data is recalculated every time write operation modifies Scoreboard. This way performance of `getSumary` method should be matching high load.


**Considerations for the future**
- if memory usage would be at some point probelamtic, `int` match score values could be represented by `short` or maybe even `byte` data type.