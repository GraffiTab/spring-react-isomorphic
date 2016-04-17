import React, { Component, PropTypes } from 'react';

export default class App extends Component {

  static propTypes = {
    children: PropTypes.element,
  };

  render() {
    return (
      <div>
        <p>App</p>
        { this.props.children }
      </div>
    )
  }
}
