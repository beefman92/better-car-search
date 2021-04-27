import React from "react";
import { BrowserRouter, Route, Switch } from "react-router-dom";
import { Paper, makeStyles } from "@material-ui/core";
import { Provider } from "react-redux";
import { createStore } from "redux";
import TopBar from "./components/TopBar";
import { rootReducer } from "./redux";
import Home from "./components/Home";
import SearchResult from "./components/SearchResult";

const store = createStore(rootReducer);

const useStyles = makeStyles((theme) => ({
  paperContainer: {
    minHeight: "calc(100% - 128px)",
    marginBottom: theme.spacing(2),
    marginLeft: theme.spacing(3),
    marginRight: theme.spacing(3),
    marginTop: theme.spacing(3),
    paddingBottom: theme.spacing(2),
    paddingLeft: theme.spacing(2),
    paddingRight: theme.spacing(2),
    paddingTop: theme.spacing(2),
  },
}));

const App = () => {
  const classes = useStyles();

  return (
    <Provider store={store}>
      <BrowserRouter>
        <TopBar />
        <Paper className={classes.paperContainer} elevation={3}>
          <Switch>
            <Route path='/search-result'>
              <SearchResult />
            </Route>
            <Route path='/'>
              <Home />
            </Route>
          </Switch>
        </Paper>
      </BrowserRouter>
    </Provider>
  );
};

export default App;
