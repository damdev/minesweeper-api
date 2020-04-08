import requests
from jsonmodels import models, fields, errors, validators



class Position(models.Base):
    
    x = fields.IntField(required=True)
    y = fields.IntField(required=True)
    mine = fields.BoolField(nullable=True)
    flag = fields.StringField(nullable=True)
    revealed = fields.BoolField(nullable=True)
    adjacent_mines = fields.IntField(nullable=True)

    @staticmethod
    def from_json(json):
        p = Position()
        p.x = json['x']
        p.y = json['y']
        p.mine = json['mine']
        p.flag = json['flag']
        p.revealed = json['revealed']
        p.adjacent_mines = json['adjacent_mines']
        return p


class Board(models.Base):
    
    ascii = fields.StringField(required=True)
    positions = fields.ListField(Position, required=True)

    @staticmethod
    def from_json(json):
        b = Board()
        b.ascii = json['ascii']
        b.positions = [Position.from_json(p) for p in json['positions']]
        return b


class Game(models.Base):

    id = fields.StringField(required=True)
    status = fields.StringField(required=True)
    owner = fields.StringField(required=True)
    start_time = fields.IntField(required=True)
    elapsed_time_seconds = fields.IntField(required=True)
    finish_time = fields.IntField(nullable=True)
    board = fields.EmbeddedField(Board)
    last_move_error = fields.StringField(nullable=True)


    @staticmethod
    def from_json(json):
        g = Game()
        g.id = json['id']
        g.status = json['status']
        g.owner = json['owner']
        g.start_time = json['start_time']
        g.elapsed_time_seconds = json['elapsed_time_seconds']
        g.finish_time = json['finish_time']
        g.board = Board.from_json(json['board'])
        g.last_move_error = json['last_move_error']
        return g


class MinesweeperApi:
    username = ''
    password = ''
    endpoint = ''

    def __init__(self, endpoint = 'http://damdev-minesweeper-api.herokuapp.com'):
        self.endpoint = endpoint

    def login(self, username, password):
        self.username = username
        self.password = password

    def new_game(self, mines, width, height):
        response = requests.get("%s/games/new" % self.endpoint, params = { 'mines': mines, 'width': width, 'height': height }, auth=(self.username, self.password))
        if response.status_code == 200:
            return Game.from_json(response.json())
        else:
            raise Exception("%s: %s" % (str(response.status_code),response.text))

    def get_game(self, game_id):
        response = requests.get("%s/games/%s" % (self.endpoint), game_id, params = { 'mines': mines, 'width': width, 'height': height }, auth=(self.username, self.password))
        if response.status_code == 200:
            return Game.from_json(response.json())
        else:
            raise Exception("%s: %s" % (str(response.status_code),response.text))

    def reveal(self, game_id, x, y):
        response = requests.patch("%s/games/%s/%s/%s" % (self.endpoint, game_id, x, y), auth=(self.username, self.password), json={'revealed': True})
        if response.status_code == 200:
            return Game.from_json(response.json())
        else:
            raise Exception("%s: %s" % (str(response.status_code),response.text))

    def flag(self, game_id, x, y, flag_type):
        response = requests.patch("%s/games/%s/%s/%s" % (self.endpoint, game_id, x, y), auth=(self.username, self.password), json={'flag': flag_type})
        if response.status_code == 200:
            return Game.from_json(response.json())
        else:
            raise Exception("%s: %s" % (str(response.status_code),response.text))

    def unflag(self, game_id, x, y):
        response = requests.patch("%s/games/%s/%s/%s" % (self.endpoint, game_id, x, y), auth=(self.username, self.password), json={'flag': None})
        if response.status_code == 200:
            return Game.from_json(response.json())
        else:
            raise Exception("%s: %s" % (str(response.status_code),response.text))


if __name__ == "__main__":
    api = MinesweeperApi()
    api.login('dam', 'dam')
    width = 10
    height = 10
    game = api.new_game(15, width, height)

    print("http://damdev-minesweeper-api.herokuapp.com/games/%s" % game.id)

    flagged = api.flag(game.id, 5, 5, 'red_flag')
    flagged = api.flag(game.id, 8, 8, 'question_mark')
    
    print(flagged.to_struct())

    unflagged = api.unflag(game.id, 5, 5)
    unflagged = api.unflag(game.id, 8, 8)
    
    print(unflagged.to_struct())

