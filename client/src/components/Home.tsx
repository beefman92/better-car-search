import React, { useState } from "react";
import { useDispatch } from "react-redux";
import {
  Grid,
  Tabs,
  Tab,
  Box,
  Typography,
  AppBar,
  Button,
  TextField,
  Backdrop,
  CircularProgress,
  makeStyles,
} from "@material-ui/core";
import { useHistory } from "react-router-dom";
import { Search } from "@material-ui/icons";
import SwipeableViews from "react-swipeable-views";
import api from "../api";
import Toast, { ToastState } from "./Toast";
import { setSearchResultStore } from "src/redux/searchResult";

const useStyles = makeStyles((theme) => ({
  container: {
    height: "inherit",
  },
  panel: {
    borderBottomRightRadius: "2px",
    borderBottomLeftRadius: "2px",
    borderColor: theme.palette.grey[200],
    borderStyle: "solid",
    borderWidth: "2px",
  },
  inputPanel: {
    height: "200px",
  },
  filePanel: {
    height: "200px",
  },
  fileInput: {
    display: "none",
  },
  backdrop: {
    zIndex: theme.zIndex.drawer + 1,
    color: "#fff",
  },
}));

const Home = () => {
  const classes = useStyles();
  const [activeTab, setActiveTab] = useState<number>(0);
  const [query, setQuery] = useState<string>("");
  const [loading, setLoading] = useState<boolean>(false);
  const [toastState, setToastState] = useState<ToastState>({
    open: false,
    severity: "info",
    message: "",
  });
  const dispatch = useDispatch();
  const history = useHistory();

  const handleSubmitQuery = () => {
    setLoading(true);
    api
      .search({ query })
      .then((searchResult) => {
        dispatch(
          setSearchResultStore({
            searchResult,
            query,
          })
        );
        history.push("/search-result");
      })
      .catch((reason) => {
        setToastState({
          open: true,
          severity: "error",
          message: String(reason),
        });
      })
      .finally(() => {
        setLoading(false);
      });
  };

  return (
    <Grid
      alignContent='center'
      className={classes.container}
      container
      justify='center'
      spacing={3}
    >
      <Grid item xs={6}>
        <AppBar position='static' color='default'>
          <Tabs
            indicatorColor='primary'
            onChange={(event, newValue) => setActiveTab(newValue as number)}
            textColor='primary'
            value={activeTab}
            variant='fullWidth'
          >
            <Tab label='Search' />
          </Tabs>
        </AppBar>
        <SwipeableViews
          index={activeTab}
          onChangeIndex={(index) => setActiveTab(index)}
        >
          <TabPanel
            className={`${classes.panel} ${classes.inputPanel}`}
            index={0}
            value={activeTab}
          >
            <Grid container spacing={3}>
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label='Search something'
                  onChange={(event) => setQuery(event.target.value)}
                  value={query}
                  variant='outlined'
                />
              </Grid>
              <Grid container item justify='flex-end' spacing={3} xs={12}>
                <Grid item xs={"auto"}>
                  <Button
                    color='primary'
                    endIcon={<Search />}
                    fullWidth
                    onClick={handleSubmitQuery}
                    variant='contained'
                  >
                    Search
                  </Button>
                </Grid>
              </Grid>
            </Grid>
          </TabPanel>
        </SwipeableViews>
      </Grid>
      <Backdrop className={classes.backdrop} open={loading}>
        <CircularProgress color='inherit' size={100} />
      </Backdrop>
      <Toast
        toastState={toastState}
        onClose={() =>
          setToastState({ open: false, severity: "info", message: "" })
        }
      ></Toast>
    </Grid>
  );
};

interface TabProps {
  className: string;
  value: number;
  index: number;
  children: React.ReactNode;
}

function TabPanel(props: TabProps) {
  const { children, value, index, className } = props;

  return (
    <div className={className} hidden={value !== index}>
      {value === index && (
        <Box p={3}>
          <Typography>{children}</Typography>
        </Box>
      )}
    </div>
  );
}

export default Home;
