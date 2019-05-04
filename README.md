# Classfindr

Eventually, a service that scrapes WWU's classfinder and offers a mirror database with more diverse and accessible tools.

Current status: Just connected to AWS; I'll probably test with different school terms in order to confirm universal functionality and then work on optimizing for efficiency and logical structure before moving on to data analysis tools. I'll also need to write an API so others can access the DynamoDB database.

## Current ToDo:
- Add constraints for requests to WWU servers (possibly in a separate client file)
- Add queries with different advantages for different times
- check AWS traffic tier limitations and check if requests need to be metered
- set up time after which no more updates for a given quarter are included in the update cycle (after add/drop penalties or end of quarter, for example)

## Planned Features:
- user website or app makes requests either to WWU's classfinder or to the AWS mirror, depending on the nature of the query and urgency of the information
