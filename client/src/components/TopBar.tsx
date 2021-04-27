import React from "react";
import { AppBar, Toolbar, Button, Grid } from "@material-ui/core";
import { Home, Build } from "@material-ui/icons";
import { useHistory } from "react-router-dom";

const TopBar = () => {
  const history = useHistory();

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
        </Grid>
      </Toolbar>
    </AppBar>
  );
};

export default TopBar;
