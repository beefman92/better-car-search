import { StoreState } from ".";
import { ISearchResult, IRangeFacet } from "src/types/SearchResult";

// Store
export type SearchResultStore = {
  searchResult: ISearchResult;
  query: string;
};

const initialState: SearchResultStore = {
  searchResult: {
    total: 0,
    documentList: [],
    priceFacet: {} as IRangeFacet,
    yearFacet: {} as IRangeFacet,
    mileageFacet: {} as IRangeFacet,
    fieldFacetList: [],
  },
  query: "",
};

// Constants
export type SET_SEARCH_RESULT_STORE = "SET_SEARCH_RESULT_STORE";
export const SET_SEARCH_RESULT_STORE = "SET_SEARCH_RESULT_STORE";

// Actions
export interface SetSearchResultStore {
  type: SET_SEARCH_RESULT_STORE;
  payload: SearchResultStore;
}

export const setSearchResultStore = (payload: SearchResultStore) => {
  return { type: SET_SEARCH_RESULT_STORE, payload };
};

export type SearchResultStoreActions = SetSearchResultStore;

// Reducer
export const searchResultStoreReducer = (
  state = initialState,
  action: SearchResultStoreActions
) => {
  switch (action.type) {
    case SET_SEARCH_RESULT_STORE: {
      return action.payload;
    }
    default:
      return state;
  }
};

// Selectors
export const getSearchResultStore = (state: StoreState) =>
  state.searchResultStore;
