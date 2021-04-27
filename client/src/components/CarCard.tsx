import React from "react";
import { Grid, makeStyles } from "@material-ui/core";
import _ from "lodash";
import { IDocument } from "../types/SearchResult";

const useStyles = makeStyles((theme) => ({
  container: {
    marginBottom: "0.5rem",
    marginTop: "0.5rem",
  },
  image: {
    width: "400px",
  },
  title: {
    color: theme.palette.info.dark,
    fontSize: "1.2rem",
  },
}));

type Props = {
  carDocument: IDocument;
};

const CarCard = ({ carDocument }: Props) => {
  const classes = useStyles();

  return (
    <Grid className={classes.container} container spacing={3}>
      <Grid container item xs={4}>
        <Grid item xs={12}>
          {/* <img
            className={classes.image}
            src={carDocument.imageUrl}
            alt={carDocument.title}
          /> */}
        </Grid>
      </Grid>
      <Grid container item xs={8} spacing={3}>
        <Grid container item xs={12}>
          <Grid className={classes.title} item xs={12}>
            {carDocument.title}
          </Grid>
        </Grid>
        <Grid container item xs={12}>
          <Grid item xs={3}>{`VIN: ${carDocument.vin}`}</Grid>
          <Grid item xs={3}>
            {carDocument.certified ? "Certified" : ""}
          </Grid>
        </Grid>
        <Grid container item xs={12}>
          <Grid item xs={3}>{`Make: ${carDocument.make}`}</Grid>
          <Grid item xs={3}>{`Model: ${carDocument.model}`}</Grid>
        </Grid>
        <Grid container item xs={12}>
          <Grid item xs={3}>{`Year: ${carDocument.year}`}</Grid>
          <Grid item xs={3}>{`Mileage: ${carDocument.mileage}`}</Grid>
          <Grid item xs={3}>{`Price: ${carDocument.price}`}</Grid>
        </Grid>
        <Grid container item xs={12}>
          <Grid item xs={12}>{`Spec: ${_.get(carDocument, "spec", "")}`}</Grid>
        </Grid>
        <Grid container item xs={12}>
          <Grid item xs={12}>{`Descirption: ${carDocument.description}`}</Grid>
        </Grid>
      </Grid>
    </Grid>
  );
};

export default CarCard;
