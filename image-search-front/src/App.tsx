import React from 'react';
import { BrowserRouter as Router, Route } from "react-router-dom";
import Search from './pages/Search';
import './App.css';

const App: React.FC = () => {
  return (
      <Router>
          <Route exact path="/" component={Search} />
      </Router>
  );
}

export default App;