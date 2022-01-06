import React from 'react';
import './App.css';

//
const client = new WebSocket('ws://' + window.location.hostname + ':8080/servlet-war-0.0.1-SNAPSHOT/socket');

const INTERVAL_VALUE = 100;
export default class App extends React.Component {
  //
  //Initializing
  //
  constructor(props) {
    super(props);
    this.state = {
      msg: '',
      width: 40,
      height: 30,
      board: [],
      playerLives: 0,
      playerFlags: 0,
      playerRevealed: 0,
      playerScore: 0.0,
      opponentPosX: 0,
      opponentPosY: 0,
      opponentBlockId: "",
      opponentLives: 0,
      opponentFlags: 0,
      opponentRevealed: 0,
      opponentScore: 0.0,
      frameStamp: 0,
      over: false,
      clicked: 3
    }
    this.mousePosX = 0;
    this.mousePosY = 0;
    this.hoverBlockX = 0;
    this.hoverBlockY = 0;
    this.timer = {}
    this.messageSender = this.messageSender.bind(this);
    document.addEventListener("contextmenu", (event) => {
      event.preventDefault();
    });
    document.body.onmousedown = function (e) {
      if (e.button === 1) {
        e.preventDefault();
      }
    }
  }
  initBoard() {
    let board = [];
    for (let r = 0; r < this.state.height; r++) {
      let row = [];
      for (let c = 0; c < this.state.width; c++) {
        row.push({ val: 0, checked: false, flag: false, owner: "" });
      }
      board.push(row)
    }
    this.setState({ board: board });
  }
  componentDidMount() {
    this.initBoard();
    client.onopen = () => {
      console.log('WebSocket Client Connected');
    };
    client.onmessage = (message) => {
      let gameState = JSON.parse(message.data);
      this.assignValues(gameState);
    };
    this.timer = setInterval(this.messageSender, INTERVAL_VALUE);
  }
  //
  // Connection setting
  //
  messageSender() {
    this.sendMessage();
  }
  sendMessage() {
    try {
      let message = {
        field: {
          x: this.hoverBlockX,
          y: this.hoverBlockY
        },
        cursor: {
          x: this.mousePosX,
          y: this.mousePosY
        },
        clickType: this.state.clicked,
        lastFrame: this.state.frameStamp,
      }
      if (client.OPEN) {
        client.send(JSON.stringify(message));
      }
      //clearing click type
      this.setState({ clicked: 3 });
    } catch (error) {
      console.log(error)
    }
  }
  //
  // Setters and tools
  //
  handleCheck(event) {
    let type = event.button;
    this.setState({ clicked: type });
  }
  setHoverBlockId(idY, idX) {
    if (this.state.clicked == 3) {
      this.hoverBlockX = idX;
      this.hoverBlockY = idY;
    }
  }
  getTimerValue() {
    var minutes = Math.floor(this.state.frameStamp / 60000);
    var seconds = ((this.state.frameStamp % 60000) / 1000).toFixed();
    return minutes + ":" + (seconds < 10 ? '0' : '') + seconds;
  }
  assignValues(gameState) {
    this.setOpponentPointer(gameState.pointer);
    this.setPlayersInfo(gameState.player, gameState.opponent);
    this.setBoardFields(gameState.changedFields);
    this.setState({ frameStamp: gameState.timeFrame })
    console.log(gameState.timeFrame)
    if (gameState.over) {
      this.setState({ over: true });
    }
  }
  setBoardFields(fields) {
    for (let field of fields) {
      let cpy = this.state.board;
      let valcpy = field.value;
      if (valcpy.value === "0") {
        valcpy.value = "";
      }
      cpy[field.key.y][field.key.x] = valcpy;
      this.setState({ board: cpy });
    }
  }
  setOpponentPointer(pointerState) {
    this.setState({ opponentBlockId: pointerState.field.y + ":" + pointerState.field.x });
    this.setState({ opponentPosX: pointerState.pointerPos.x });
    this.setState({ opponentPosY: pointerState.pointerPos.y });
  }
  setPlayersInfo(player, opponent) {
    this.setState({ playerLives: player.lives });
    this.setState({ playerFlags: player.flags });
    this.setState({ playerRevealed: player.revealedFields });
    this.setState({ playerScore: player.score });
    this.setState({ opponentLives: opponent.lives });
    this.setState({ opponentFlags: opponent.flags });
    this.setState({ opponentRevealed: opponent.revealedFields });
    this.setState({ opponentScore: opponent.score });
  }
  mouseMoved(event) {
    this.mousePosX = event.nativeEvent.offsetX;
    this.mousePosY = event.nativeEvent.offsetY;
  }
  opponentsCursorPose() {
    try {
      var bounds = document.getElementById(this.state.opponentBlockId).getBoundingClientRect();
      let x = this.state.opponentPosX + bounds.left;
      let y = this.state.opponentPosY + bounds.top;
      return { X: x, Y: y };
    } catch (e) {
      return { X: 0, Y: 0 };
    }
  }
  //
  //Rendering
  //
  renderCellValue(col, row) {
    if (this.state.board.length > 0) {
      let val = this.state.board[row][col];
      if (val.checked) {
        if (val.value === 0) {
          return "";
        } else if (val.value === "m") {
          return <small>💣</small>
        } else {
          return val.value;
        }
      }
      else if (val.flag) {
        return <small>📌</small>
      }
    }
    else {
      return "";
    }
  }
  renderCell(colId, rowId) {
    return (
      <button key={rowId + ":" + colId} id={rowId + ":" + colId} onMouseOver={e => this.setHoverBlockId(rowId, colId)} onMouseDown={e => this.handleCheck(e)} className={"cell" + this.isChecked(colId, rowId) + this.setOwnerClass(colId, rowId) + this.isFlag(colId, rowId)} >{this.renderCellValue(colId, rowId)}</button>
    )
  }
  isChecked(col, row) {
    if (this.state.board.length > 0) {
      try {
        if (this.state.board[row][col].checked && this.state.board[row][col].value === "m") {
          return " vm"
        }
        let val = this.state.board[row][col].checked ? " checked" : ""
        return this.state.board[row][col].value !== 0 && this.state.board[row][col].value !== "m" ? val + " v" + this.state.board[row][col].value : val;
      } catch (err) {
        console.log("Error on isChecked: " + col + ":" + row)
        return ""
      }
    }
    return ""
  }
  isFlag(col, row) {
    if (this.state.board.length > 0) {
      try {
        return this.state.board[row][col].flag ? " flg" : "";
      } catch (err) {
        console.log("Error on isFlag: " + col + ":" + row)
        return ""
      }
    }
    return ""
  }
  setOwnerClass(col, row) {
    if (this.state.board.length > 0) {
      if (this.state.board[row][col].owner !== "") {
        let type = this.state.board[row][col].owner;
        if (type === "y") {
          return " you"
        }
        else if (type === "o") {
          return " opponent"
        }
      }
    }
    return "";
  }
  renderBoardRow(rowId) {
    let cells = [];
    for (let i = 0; i < this.state.width; i++) {
      cells.push(this.renderCell(i, rowId));
    }
    return (<p key={rowId} className="boardRow">{cells}</p>)
  }
  renderBoard() {
    let rows = [];
    for (let i = 0; i < this.state.height; i++) {
      rows.push(this.renderBoardRow(i))
    }
    return (
      <div id="gameBoard" onMouseMove={this.mouseMoved.bind(this)} onContextMenu={() => { return false; }}>
        {rows}
        <div id="opponentPointer" style={{ left: this.opponentsCursorPose().X, top: this.opponentsCursorPose().Y, position: 'absolute' }}>🔨</div>
      </div>
    )
  }
  renderPlayersInfo() {
    return (
      <div id="playersInfo">
        <div class="you">
          You {this.state.over ? <h2><ruby>{this.state.playerScore.toFixed(2)}<rt>SCORE</rt></ruby></h2> : <p><ruby>{this.state.playerLives}<rt>LIVES</rt></ruby> <ruby>{this.state.playerFlags}<rt>FLAGS</rt></ruby> <ruby>{this.state.playerRevealed}<rt>REVEALED</rt></ruby></p>}
        </div>
        <div>
          <strong>{this.getTimerValue()}</strong>
        </div>
        <div class="opponent">
          opponent {this.state.over ? <h2><ruby>{this.state.opponentScore.toFixed(2)}<rt>SCORE</rt></ruby></h2> : <p><ruby>{this.state.opponentLives}<rt>LIVES</rt></ruby> <ruby>{this.state.opponentFlags}<rt>FLAGS</rt></ruby> <ruby>{this.state.opponentRevealed}<rt>REVEALED</rt></ruby></p>}
        </div>
      </div>
    )
  }
  render() {
    return (
      <div className="App">
        {this.renderPlayersInfo()}
        {this.renderBoard()}
      </div>
    );
  }
}

