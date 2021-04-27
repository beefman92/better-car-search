import axios from "axios";
import { ISearchResult } from "src/types/SearchResult";

const client = axios.create({ baseURL: "http://localhost:8080/api/v1" });

const search = async ({
  query,
  limit,
  offset,
}: {
  query: string;
  limit?: number;
  offset?: number;
}) => {
  const response = await client.get(`/search`, {
    params: { query, limit, offset },
  });
  return response.data as ISearchResult;
};

const searchWithFacet = async ({
  query,
  facet,
  limit,
  offset,
}: {
  query: string;
  facet: string;
  limit?: number;
  offset?: number;
}) => {
  const response = await client.get(`/search-facet`, {
    params: { query, facet, limit, offset },
  });
  return response.data as ISearchResult;
};

const api = {
  search,
  searchWithFacet,
};

export default api;
