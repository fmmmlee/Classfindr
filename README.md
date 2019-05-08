# Classfindr

Eventually, a service that scrapes WWU's classfinder and offers a mirror database with more diverse and accessible tools.

Current status: Just implemented multithreading for the three classes which do most of the work.

## Current ToDo
- Write functions to read from the database; I know some uploads aren't getting placed in the database but I'm not sure why (duplicate CRNs? must check these, and if so alter the primary key on the AWS console)
- Implement a metrics function with timers to analyze speed of different implementations
- Implement BatchWriteItem
- Possibly spin more threads off of update_thread to more quickly upload the data 
- Add constraints for requests to WWU servers in a YAML or JSON
- check AWS traffic tier limitations and check if requests need to be metered
- write script to run a build of this every so often to keep the database updated

## "Sometime"
- user website or app makes requests either to WWU's classfinder or to the AWS mirror, depending on the nature of the query and urgency of the information
