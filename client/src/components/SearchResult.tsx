import React, { Fragment, useState } from "react";
import _ from "lodash";
import {
  Grid,
  Typography,
  Button,
  Divider,
  makeStyles,
} from "@material-ui/core";
import {
  Clear,
  KeyboardArrowLeft,
  KeyboardArrowRight,
} from "@material-ui/icons";
import NumberFormat from "react-number-format";
import { useSelector, useDispatch } from "react-redux";
import { getSearchResultStore } from "src/redux/searchResult";
import {
  IRangeCount,
  ICount,
  IRangeFacet,
  RangeType,
} from "src/types/SearchResult";
import api from "src/api";
import { setSearchResultStore } from "src/redux/searchResult";

const useStyles = makeStyles((theme) => ({
  facetTag: {
    border: `1px solid ${theme.palette.info.light}`,
    borderRadius: "5px",
    margin: "0.5rem",
    padding: "0.5rem",
    "&:hover": {
      cursor: "pointer",
      backgroundColor: theme.palette.info.light,
      color: "white",
    },
  },
  facetTagActive: {
    backgroundColor: theme.palette.info.dark,
    border: `1px solid ${theme.palette.info.dark}`,
    borderRadius: "5px",
    color: "white",
    margin: "0.5rem",
    padding: "0.5rem",
    "&:hover": {
      cursor: "pointer",
      backgroundColor: theme.palette.info.dark,
    },
  },
  facetHeader: {
    paddingRight: "0.25rem",
  },
  facetCount: {
    borderLeft: `1px solid ${theme.palette.info.dark}`,
    fontWeight: 600,
    paddingLeft: "0.5rem",
  },
  unselectFacetButton: {
    marginLeft: "0.5rem",
  },
}));

const SearchResult = () => {
  const classes = useStyles();
  const searchResultStore = useSelector(getSearchResultStore);
  const [selectedCount, setSelectedCount] = useState<ICount>({} as ICount);
  // these three state are used in pagination
  const [limit, setLimit] = useState<number>(20);
  const [offset, setOffset] = useState<number>(0);
  const [isCountSelected, setIsCountSelected] = useState<boolean>(false); //
  const dispatch = useDispatch();

  const renderSearchResultDocument = () => {
    return searchResultStore.searchResult.documentList.map((document) => {
      return (
        <Grid key={document.vin} container item spacing={3} xs={12}>
          <Grid item xs={12}>
            <Divider />
          </Grid>
          <Grid item xs={12}>
            <Typography variant='body1'>{document.vin}</Typography>
          </Grid>
          <Grid item xs={12}>
            <Typography variant='body1'>{document.title}</Typography>
          </Grid>
          <Grid item xs={12}>
            <Typography variant='body2'>{document.spec}</Typography>
          </Grid>
          <Grid item xs={12}>
            <Typography variant='body2'>{document.description}</Typography>
          </Grid>
        </Grid>
      );
    });
  };

  const renderRangeItem = (count: IRangeCount, name: string) => {
    if (count.rangeType === RangeType.LEFT_MOST) {
      return (
        <Fragment>
          {"Less Than "}
          <NumberFormat
            displayType={"text"}
            prefix={name === "price" ? "$" : ""}
            suffix={name === "mileage" ? " miles" : ""}
            thousandSeparator
            value={count.right}
          />
        </Fragment>
      );
    } else if (count.rangeType === RangeType.RIGHT_MOST) {
      return (
        <Fragment>
          {"Greater Than "}{" "}
          <NumberFormat
            displayType={"text"}
            prefix={name === "price" ? "$" : ""}
            suffix={name === "mileage" ? " miles" : ""}
            thousandSeparator
            value={count.left}
          />
        </Fragment>
      );
    } else {
      return (
        <Fragment>
          <NumberFormat
            displayType={"text"}
            prefix={name === "price" ? "$" : ""}
            suffix={name === "mileage" ? " miles" : ""}
            thousandSeparator
            value={count.left}
          />
          {" - "}
          <NumberFormat
            displayType={"text"}
            prefix={name === "price" ? "$" : ""}
            suffix={name === "mileage" ? " miles" : ""}
            thousandSeparator
            value={count.right}
          />
        </Fragment>
      );
    }
  };

  const generateFacet = (count: ICount) => {
    if (!!_.get(count, "rangeType", "")) {
      const rangeCount = count as IRangeCount;
      if (rangeCount.rangeType === RangeType.LEFT_MOST) {
        return `${rangeCount.name}:[* TO ${rangeCount.right}]`;
      } else if (rangeCount.rangeType === RangeType.MIDDLE) {
        return `${rangeCount.name}:[${rangeCount.left} TO ${rangeCount.right}]`;
      } else {
        return `${rangeCount.name}:[${rangeCount.left} TO *]`;
      }
    } else {
      return "";
    }
  };

  const searchWithFacet = (count: ICount, offset: number, limit: number) => {
    return api
      .searchWithFacet({
        query: searchResultStore.query,
        facet: generateFacet(count),
        offset,
        limit,
      })
      .then((searchResult) => {
        const newSearchResult = {
          ...searchResultStore.searchResult,
          total: searchResult.total,
          documentList: searchResult.documentList,
        };
        dispatch(
          setSearchResultStore({
            searchResult: newSearchResult,
            query: searchResultStore.query,
          })
        );
      });
  };
  const search = (offset: number, limit: number) => {
    return api
      .search({
        query: searchResultStore.query,
        offset,
        limit,
      })
      .then((searchResult) => {
        dispatch(
          setSearchResultStore({ searchResult, query: searchResultStore.query })
        );
      });
  };

  const handleCountOnClick = (count: ICount) => {
    if (count.id !== _.get(selectedCount, "id", "")) {
      searchWithFacet(count, 0, limit);
      setOffset(0);
    }
    setIsCountSelected(true);
    setSelectedCount(count);
  };

  const renderRangeFacetCountList = (rangeFacet: IRangeFacet) => {
    return rangeFacet.countList.map((count) => {
      return (
        <Grid
          className={
            _.get(selectedCount, "id", "") === count.id
              ? classes.facetTagActive
              : classes.facetTag
          }
          item
          key={count.key}
          xs='auto'
          onClick={() => handleCountOnClick(count)}
        >
          <span className={classes.facetHeader}>
            {rangeFacet.name + ": "}
            {renderRangeItem(count, rangeFacet.name)}
          </span>
          <span className={classes.facetCount}>{`${count.count}`}</span>
        </Grid>
      );
    });
  };

  const renderRangeFacet = (facetName: string) => {
    if (!!_.get(searchResultStore.searchResult, `${facetName}.name`, "")) {
      const facet = _.get(
        searchResultStore.searchResult,
        facetName,
        {}
      ) as IRangeFacet;
      return renderRangeFacetCountList(facet);
    } else {
      return null;
    }
  };

  const handleUnselectFacetOnClick = () => {
    setSelectedCount({} as ICount);
    api
      .search({ query: searchResultStore.query, offset: 0, limit: 20 })
      .then((searchResult) => {
        dispatch(
          setSearchResultStore({ searchResult, query: searchResultStore.query })
        );
      });
  };

  const renderFacets = () => {
    return (
      <Fragment>
        <Button
          className={classes.unselectFacetButton}
          color={"secondary"}
          endIcon={<Clear />}
          onClick={handleUnselectFacetOnClick}
          variant='outlined'
        >
          Unselect Facet
        </Button>
        {renderRangeFacet("priceFacet")}
        {renderRangeFacet("yearFacet")}
        {renderRangeFacet("mileageFacet")}
      </Fragment>
    );
  };

  const computeTotalPage = () => {
    const total = searchResultStore.searchResult.total;
    if (total % limit === 0) {
      return total / limit;
    } else {
      return ((total / limit) | 0) + 1;
    }
  };

  const handleNextOnClick = () => {
    const newOffset = offset + limit;
    if (isCountSelected) {
      searchWithFacet(selectedCount, newOffset, limit);
    } else {
      search(newOffset, limit);
    }
    setOffset(newOffset);
  };

  const handlePreviousOnClick = () => {
    let newOffset = offset - limit;
    if (newOffset < 0) {
      newOffset = 0;
    }
    if (isCountSelected) {
      searchWithFacet(selectedCount, newOffset, limit);
    } else {
      search(newOffset, limit);
    }
    setOffset(newOffset);
  };

  const renderPageHeader = () => {
    return (
      <Grid container item justify='flex-end' xs={12} spacing={2}>
        <Grid item xs={"auto"}>
          <Typography variant='body1'>
            Page:{`${offset / limit + 1} / ${computeTotalPage()}`}
          </Typography>
        </Grid>
        <Grid item xs={"auto"}>
          <Button
            color='primary'
            disabled={offset === 0}
            endIcon={<KeyboardArrowLeft />}
            onClick={handlePreviousOnClick}
            variant='contained'
          >
            Previous
          </Button>
        </Grid>
        <Grid item xs={"auto"}>
          <Button
            color='primary'
            disabled={offset + limit >= searchResultStore.searchResult.total}
            endIcon={<KeyboardArrowRight />}
            onClick={handleNextOnClick}
            variant='contained'
          >
            Next
          </Button>
        </Grid>
      </Grid>
    );
  };

  return (
    <Grid container spacing={3}>
      <Grid item justify='flex-start' alignItems='flex-start' xs={2}>
        {renderFacets()}
      </Grid>
      <Grid container item xs={10}>
        {renderPageHeader()}
        {renderSearchResultDocument()}
      </Grid>
    </Grid>
  );
};

export default SearchResult;
