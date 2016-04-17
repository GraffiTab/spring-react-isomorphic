import 'babel-polyfill';
import { match } from 'react-router';
import routes from './routes.jsx';
import ReactDOM from 'react-dom/server';


function serverRender(url) {
  match({ routes, location: '/path/user' }, (error, redirectionLocation, renderProps) => {
    let data = {title: '', css: '', body: ''};

    data.body = ReactDOM.renderToString(
      <RouterContext {...renderProps} />
    );

    return data.body;
  });

}
