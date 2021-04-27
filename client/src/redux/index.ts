import { combineReducers } from "redux";
import {
  SearchResultStore,
  SearchResultStoreActions,
  searchResultStoreReducer,
} from "./searchResult";

export interface StoreState {
  searchResultStore: SearchResultStore;
}

export type RootActions = SearchResultStoreActions;

export const rootReducer = combineReducers({
  searchResultStore: searchResultStoreReducer,
});

export default rootReducer;
