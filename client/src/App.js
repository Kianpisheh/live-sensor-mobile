import React, { Component } from "react";
import "./App.css";
import ChartBox from "./components/ChartBox";

class App extends Component {
  constructor(props) {
    super(props);
    this.getNewData = this.getNewData.bind(this);
    this.labels = [1, 2, 3, 4, 5, 6];
    this.data = [0.22, 0.6, 0.33, 0.12, 0.3, 0.72];
    this.newLabel = 7;
    setInterval(this.getNewData, 1);
    this.d = 0;
  }

  render() {
    this.data.push(Math.random());
    this.data.shift();

    return (
      <div className="App">
        <header className="App-header">
          <ChartBox
            newData={this.d}
            data={this.data}
            labels={this.labels}
            idx={this.newLabel}
          ></ChartBox>
        </header>
      </div>
    );
  }

  getNewData() {
    this.d = Math.random();
    this.forceUpdate();
    this.newLabel += 1;
  }
}

export default App;
