import React, { Fragment, useState } from "react";
import { Grid, makeStyles, Button } from "@material-ui/core";
import { MoreVert } from "@material-ui/icons";
import _ from "lodash";
import { IDocument } from "../types/SearchResult";

const useStyles = makeStyles((theme) => ({
  container: {
    marginBottom: "0.5rem",
    marginTop: "0.5rem",
  },
  image: {
    width: "350px",
  },
  title: {
    color: theme.palette.info.dark,
    fontSize: "1.2rem",
  },
  highlight: {
    backgroundColor: "yellow",
    height: "fit-content",
    fontWeight: 600,
  },
}));

type Props = {
  carDocument: IDocument;
};

const CarCard = ({ carDocument }: Props) => {
  const classes = useStyles();
  const [displayDetails, setDisplayDetails] = useState<boolean>(false);

  const renderDetail = () => {
    if (displayDetails) {
      return (
        <Fragment>
          <Grid container item xs={12}>
            <Grid item xs={3}>{`VIN: ${carDocument.vin}`}</Grid>
            <Grid item xs={8}>
              {carDocument.certified ? "Certified" : ""}
            </Grid>
            <Grid item xs={1}>
              <Button
                onClick={() => setDisplayDetails(false)}
                variant='outlined'
              >
                Less
              </Button>
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
            <Grid item xs={12}>{`Spec: ${_.get(
              carDocument,
              "spec",
              ""
            )}`}</Grid>
          </Grid>
          <Grid container item xs={12}>
            <Grid
              item
              xs={12}
            >{`Descirption: ${carDocument.description}`}</Grid>
          </Grid>
        </Fragment>
      );
    } else {
      const content = carDocument.textHighlightList.join("...");
      const enhancedContent = content.replaceAll(
        "<em>",
        `<em class=${classes.highlight}>`
      );
      return (
        <Grid container item xs={12}>
          <Grid container item xs={11}>
            <span dangerouslySetInnerHTML={{ __html: enhancedContent }}></span>
          </Grid>
          <Grid item xs={1}>
            <Button
              endIcon={<MoreVert />}
              onClick={() => setDisplayDetails(true)}
              variant='outlined'
            >
              More
            </Button>
          </Grid>
        </Grid>
      );
    }
  };

  return (
    <Grid className={classes.container} container spacing={3}>
      <Grid container item xs={4}>
        <Grid item xs={12}>
          <img
            className={classes.image}
            src={carDocument.imageUrl}
            alt={carDocument.title}
          />
        </Grid>
      </Grid>
      <Grid container item xs={8} spacing={3}>
        <Grid container item xs={12}>
          <Grid className={classes.title} item xs={12}>
            {carDocument.title}
          </Grid>
        </Grid>
        {renderDetail()}
      </Grid>
    </Grid>
  );
};

export default CarCard;
