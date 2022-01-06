# Web Minesweeper

### 1. Introduction
Two player, online and real time, prototype version of mine sweeper. Each player plays in web client connecting to game server. 

Game uses web client written in React, with JavaEE in the back end - all connected using websockets.

### 2. Rules
The game variant in prototype is 40x30 board with 30% of fields as mines.

Basic minesweeper rules applies: left mouse button checks field, right  mouse button places a flag over field and the middle/scroll button checks surrounding of checked field.

But since this is network game, some rules are added:
* Mines are places before first move - so when starting a game it's easy to hit a mine.
* Players has 5 lives, before the game ends. Helps with keeping game at steady pace.
* Mines detonated before game is over, are treated like flags. So during middle/scroll click sum of flags and detonated mines is checked.

Final score for each player is calculated as:
```
Player score= (correctly marked fields * lives left + revealed fields * lives left)/game time
```
Where game time is number of minutes with seconds as decimal fraction part (e.g. 3m 45s => 3.45).

For example:
Game finishes after 3 minutes and 45 seconds.
For player one game finished with 3 lives left, with 12 mines marked correctly, and 35 fields  revealed. So final score is:
```
(12*3+35*3)/3.45=(36+135)/3.45=49.57
```
For player two game finished with only one life, but with 30 mines marked correctly, and 150 fields revealed. So final score is:
```
(30*1+150*1)/3.45=(30+150)/3.45=52.17
```
So second player wins. 

### 3. Project goals
* To check capabilities of websockets in real time, constant client-server data transmission. 
* To check how React can be used in such application (also without resort to using canvas).
* To check JavaEE's websockets on Java 11 and WildFly server.

### 4. Specification

Project is done using:
* React 17.0.2 application as frontend 
* JavaEE (Jakarta) on Java 11.0.2 (OpenJDK) and WildFly (preview-26.0.0.Final)

### Client
Only role of a client is to periodically send data about players move to server, and then render changes received from server. Also opponent's cursor position is represented as a hammer.

### Server
Server hosts multiple game instances. After websocket connection is open, player is either assigned to game where other player is waiting, or new game is created, when all games has players. On disconnection of one of a players, the game is also terminated. Each game instance has buffer of last 60 moves in case of lost packages with game state. 

### Process:
1. Client sends data about players move periodically to server via open websocket. Data contains information about field player has cursor over, position of a cursor on that field, player's action type, and timestamp of last data package received from server.
2. Server applies move to the game that is running on server.
3. Server responds via open websocket, with both players stats, opposite player's cursor location, collection of changed fields (that were changed after timestamp from 1st point was created), and new timestamp. Also when game is over, additional flag is attached to that.
