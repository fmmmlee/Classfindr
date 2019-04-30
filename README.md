# Classfindr

#### Don't ask about the name

It was what I came up with on the spot, trying to avoid sounding like the other takes on Classfinder out there; I remembered this meme about how tech companies are all just taking a word ending in -er and making it -r (Flikr, Grindr, Tumblr, etc) and I thought it was funny. Yeah.

Eventually, a service that scrapes WWU's classfinder and offers a mirror database with more diverse and accessible tools.

## Current ToDo:
- Make basic structure of data fetching work
- Set up database to hold data for analytic requests
- Add constraints for requests to WWU servers (possibly in a separate client file)
- Add purpose-built queries for different times (default query for regular updates on future quarters, high-priority queries for registration season
- set up time after which no more updates for a given quarter are included in the update cycle (after add/drop penalties or end of quarter, for example)

## Planned Features:
- databaseUpdate runs every so often, updating a noSQL server running on AWS
- client website or app makes requests either to WWU's classfinder or to the AWS mirror, depending on the nature of the query and latency priority (around registration time, latency is always top priority, whereas fetching a dataset for big data analysis on classes held in a certain building for the past 15 years is not)
