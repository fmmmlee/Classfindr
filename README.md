# Classfindr

Goal: a service that scrapes WWU's classfinder and offers a mirror database with more diverse and accessible tools.

**Current status:** Not in a usable state for an end user, but things are coming along well, and it's functional for writing to my database. Future updates will focus on writing to a local database rather than AWS in order to avoid the upload bottleneck.

**Building:** I use Maven to build the project.
  1. Clone/download the repository, navigate to the folder where you put it (in the directory with the pom.xml), and run `maven clean install` in terminal
  2. This should produce a usable uberjar (about 9 MB at the time of this commit).
  3. Ensure that you have the <a href="https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/setup-install.html">AWS SDK</a> configured on your system.
  4. The tool should be ready to run.
  
  Note: the duplicate-checking reads from two json files that you have to create yourself (until I make them auto-create if not present). Their names are crn.json and duplicates.json. It's fine to initialize them as empty JSON files (just "{}" with no quotes as the text).

## Current ToDo
- Add writing to a local database instead of DynamoDB
- Write functions to read from DynamoDB
- Add config file to direct whether to write to local or AWS
- Auto-create missing duplicate-checking files if not present
- Overhaul document parser; it's wildly overcomplicated for what it needs to do
- Implement BatchWriteItem
- Possibly spin more threads off of update_thread to more quickly upload the data
- Add constraints for requests to WWU servers in a YAML or JSON
- write script to run a build of this every so often to keep the database updated

## "Sometime"
- user website or app makes requests either to WWU's classfinder or to the AWS mirror (or to another service altogether), depending on the nature of the query and urgency of the information

### Completed ToDos:
- Implement a metrics function with timers to analyze speed of different implementations
- check AWS traffic tier limitations and check if requests need to be metered
- Implement duplicate CRN checker
- Comment clarification

<br/>
<br/>

**UI**

Here's a shot of what the UI looks like at this stage (or something close to it)
<p align="left">
  <img src="https://user-images.githubusercontent.com/30479162/57667061-20b97200-75b7-11e9-9650-8233ae3930ec.JPG" width="500" title="a sample run of the scraper and uploader">
</p>
