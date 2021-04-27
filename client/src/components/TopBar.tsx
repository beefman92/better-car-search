import React, { useState, useEffect } from "react";
import {
  AppBar,
  Toolbar,
  Button,
  Grid,
  InputBase,
  makeStyles,
  fade,
} from "@material-ui/core";
import { Home, Search } from "@material-ui/icons";
import { useHistory } from "react-router-dom";
import { useSelector, useDispatch } from "react-redux";
import {
  getSearchResultStore,
  setSearchResultStore,
} from "src/redux/searchResult";
import api from "src/api";

const useStyles = makeStyles((theme) => ({
  search: {
    position: "relative",
    borderRadius: theme.shape.borderRadius,
    backgroundColor: fade(theme.palette.common.white, 0.15),
    "&:hover": {
      backgroundColor: fade(theme.palette.common.white, 0.25),
    },
    marginLeft: 0,
    width: "100%",
    [theme.breakpoints.up("sm")]: {
      marginLeft: theme.spacing(1),
      width: "auto",
    },
  },
  searchIcon: {
    padding: theme.spacing(0, 2),
    height: "100%",
    position: "absolute",
    pointerEvents: "none",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    "&:hover": {
      cursor: "pointer",
    },
  },
  inputRoot: {
    color: "inherit",
  },
  inputInput: {
    padding: theme.spacing(1, 1, 1, 0),
    // vertical padding + font size from searchIcon
    paddingLeft: `calc(1em + ${theme.spacing(4)}px)`,
    transition: theme.transitions.create("width"),
    width: "100%",
    [theme.breakpoints.up("sm")]: {
      width: "20ch",
      "&:focus": {
        width: "30ch",
      },
    },
  },
}));

const TopBar = () => {
  const classes = useStyles();
  const history = useHistory();
  const dispatch = useDispatch();
  const [query, setQuery] = useState<string>("");
  const searchResultStore = useSelector(getSearchResultStore);

  useEffect(() => {
    setQuery(searchResultStore.query);
  }, [searchResultStore.query]);

  const handleSearchOnClick = () => {
    api.search({ query, limit: 20, offset: 0 }).then((searchResult) => {
      dispatch(setSearchResultStore({ searchResult, query }));
    });
  };

  const renderSearchInput = () => {
    if ("/search-result" === history.location.pathname) {
      return (
        <div className={classes.search}>
          <div className={classes.searchIcon} onClick={handleSearchOnClick}>
            <Search />
          </div>
          <InputBase
            placeholder='Search…'
            classes={{
              root: classes.inputRoot,
              input: classes.inputInput,
            }}
            inputProps={{ "aria-label": "search" }}
            value={query}
            onChange={(event) => {
              setQuery(event.target.value);
            }}
            onKeyDown={(event) => {
              if (event.key === "Enter") {
                handleSearchOnClick();
              }
            }}
          />
        </div>
      );
    }
  };

  return (
    <AppBar position='static'>
      <Toolbar>
        <Grid container spacing={3}>
          <Grid item xs='auto'>
            <Button
              color='inherit'
              endIcon={<Home />}
              onClick={() => {
                history.push("/");
              }}
              variant='outlined'
            >
              Home
            </Button>
          </Grid>
          <Grid item xs='auto'>
            {renderSearchInput()}
          </Grid>
        </Grid>
      </Toolbar>
    </AppBar>
  );
};

export default TopBar;
