# Classfindr

Eventually, a service that scrapes WWU's classfinder and offers a mirror database with more diverse and accessible tools.

**Current status:** Not in a usable state for an end user, but things are coming along well, and it's functional for writing to my database.

**Building:** If you have maven on your system, (I think) it shouldn't be hard to build. Just clone/download the repository, navigate to the folder where you put it (in the directory with the pom.xml), and run `maven clean install` in terminal and it should produce a usable uberjar (about 9 MB at the time of this commit) for you. Do note that it won't be usable at this stage, however, because a) you probably don't have a DynamoDB table called "course" on your own AWS account, and b) the duplicate-checking reads from two json files that you have to create yourself (until I put in a catch block or something to write them if they're not present).

## Current ToDo
- Write functions to read from the database; I know some uploads aren't getting placed in the database but I'm not sure why (implemented duplicate CRN checker, will take a look at the output when I have time)
- Go through code and clarify comments, add documentation tags
- Overhaul document parser; it's wildly overcomplicated for what it needs to be
- Implement BatchWriteItem
- Possibly spin more threads off of update_thread to more quickly upload the data 
- Add constraints for requests to WWU servers in a YAML or JSON
- write script to run a build of this every so often to keep the database updated

## "Sometime"
- user website or app makes requests either to WWU's classfinder or to the AWS mirror, depending on the nature of the query and urgency of the information

### Completed ToDos:
- Implement a metrics function with timers to analyze speed of different implementations
- check AWS traffic tier limitations and check if requests need to be metered
- Implement duplicate CRN checker

Here's a shot of what the UI looks like at this stage (or something close to it)
![console run](exampleclassfinderrun.jpg)
