# minesweeper-API
API test

We ask that you complete the following challenge to evaluate your development skills. Please use the programming language and framework discussed during your interview to accomplish the following task.

PLEASE DO NOT FORK THE REPOSITORY. WE NEED A PUBLIC REPOSITORY FOR THE REVIEW. 

## The Game
Develop the classic game of [Minesweeper](https://en.wikipedia.org/wiki/Minesweeper_(video_game))

## Show your work

1.  Create a Public repository ( please dont make a pull request, clone the private repository and create a new plublic one on your profile)
2.  Commit each step of your process so we can follow your thought process.

## What to build
The following is a list of items (prioritized from most important to least important) we wish to see:
* Design and implement  a documented RESTful API for the game (think of a mobile app for your API)
* Implement an API client library for the API designed above. Ideally, in a different language, of your preference, to the one used for the API
* When a cell with no adjacent mines is revealed, all adjacent squares will be revealed (and repeat)
* Ability to 'flag' a cell with a question mark or red flag
* Detect when game is over
* Persistence
* Time tracking
* Ability to start a new game and preserve/resume the old ones
* Ability to select the game parameters: number of rows, columns, and mines
* Ability to support multiple users/accounts
 
## Deliverables we expect:
* URL where the game can be accessed and played (use any platform of your preference: heroku.com, aws.amazon.com, etc)
* Code in a public Github repo
* README file with the decisions taken and important notes

## Time Spent
You do not need to fully complete the challenge. We suggest not to spend more than 5 hours total, which can be done over the course of 2 days.  Please make commits as often as possible so we can see the time you spent and please do not make one commit.  We will evaluate the code and time spent.
 
What we want to see is how well you handle yourself given the time you spend on the problem, how you think, and how you prioritize when time is insufficient to solve everything.

Please email your solution as soon as you have completed the challenge or the time is up.

# TODO
- [x] Design and implement RESTful API
- [ ] Document API
- [ ] Implement an API client library for the API designed above. Python.
- [x] When a cell with no adjacent mines is revealed, all adjacent squares will be revealed (and repeat)
- [x] Ability to 'flag' a cell with a question mark or red flag
- [x] Detect when game is over
- [x] Persistence
- [x] Time tracking
- [x] Ability to preserve/resume the old ones
- [x] Game random generator configurable with number of rows, columns, and mines
- [x] Ability to support multiple users/accounts
- [x] Register user
- [ ] flag and reveal should be PATCH over /game/:id

# API
## Authentication
This app uses Basic access authentication https://en.wikipedia.org/wiki/Basic_access_authentication

## /games

- `GET /games/new?mines=5&height=10&width10`
Creates a random generated game, with mines and dimensions specified. All arguments are optional and have configurable defaults.

- `GET /games/:id`
Retrieve an already created game for visualization.
 
- `PATCH /games/:id/:x/:y { "revealed": true }`
Reveals a position (x, y) and returns the modified game by this action. 

- `PATCH /games/:id/:x/:y { "flag": ":flag_type" }`
Flags a position (x, y) with :flag_type of flag (`red_flag`|`question_mark`|`null`) and returns the modified game by this action.

## /users

- `POST /users { "username": ":username", "password": ":password" }`
Creates an user with :username and :password.