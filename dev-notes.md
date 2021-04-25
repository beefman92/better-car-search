# Develop Notes

## API Anatomy

### Cargurus

#### Search Cars

```
https://www.cargurus.com/Cars/searchResults.action?zip=95123&inventorySearchWidgetType=AUTO&searchId=07e49dcd-cb51-416b-931b-1e77cb6d5a44&nonShippableBaseline=0&sortDir=ASC&sourceContext=carGurusHomePageModel&distance=50&sortType=DEAL_SCORE&offset=60&maxResults=15&filtersModified=true
```

This api is used to fetch car search result.

| Parameter | Description |
|---|---|
| zip | zip code |
| distance | radius of search area |
| sortType | sort criteria of search result, like DEAL_SCORE |
| offset | offset |
| sortDir | sort direction |
| maxResults | limit of search results |
| inventorySearchWidgetType | unknown. AUTO |
| searchId | a uuid. Usage is unknown |
| nonShippableBaseline | unknown. 0 |
| sourceContext | unknown. carGurusHomePageModel |
| filtersModified | unknown. true |

#### Fetch car details

```
https://www.cargurus.com/Cars/detailListingJson.action?inventoryListing=272125815&searchZip=95123&searchDistance=50&inclusionType=undefined
```

This api fetches data displayed on detail pages for a car. 

| Parameter | Description |
|----|----|
| inventoryListing | car id. Use search car api to get this id |
| searchZip | zip code |
| searchDistance | radius of search area |
| inclusionType | unknown, undefined |

### Edmunds

The API server has some authentication logic. It's hard to fetch data from API server directly. I have to use the selenium.
