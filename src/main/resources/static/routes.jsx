import React from 'react';
import App from './components/app';
import User from './components/user';
import { Route } from 'react-router';

let routes = (
  <Route>
    <Route path="/" component={App}>
      <Route path="path/user" component={User} />
    </Route>
  </Route>
);

export default routes;
