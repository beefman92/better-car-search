export interface ISearchResult {
  total: number;
  documentList: IDocument[];
  priceFacet: IRangeFacet;
  yearFacet: IRangeFacet;
  mileageFacet: IRangeFacet;
  fieldFacetList: IFieldFacet[];
}

export interface IDocument {
  title: string;
  certified: boolean;
  vin: string;
  year: number;
  make: string;
  model: string;
  mileage: number;
  spec: string;
  description: string;
  price: number;
  imageUrl: string;
}

export interface IRangeFacet {
  name: string;
  start: number;
  end: number;
  gap: number;
  countList: IRangeCount[];
}

export interface IRangeCount extends ICount {
  key: string;
  rangeType: RangeType;
  count: number;
  left: number;
  right: number;
}

export interface IFieldFacet {
  name: string;
  countList: IFieldCount[];
}

export interface IFieldCount extends ICount {
  key: string;
  count: number;
}

export interface ICount {
  name: string;
  id: string;
}

export enum RangeType {
  LEFT_MOST = "LEFT_MOST",
  MIDDLE = "MIDDLE",
  RIGHT_MOST = "RIGHT_MOST",
}
