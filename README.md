# Classfindr [![Build Status](https://travis-ci.org/fmmmlee/Classfindr.svg?branch=master)](https://travis-ci.org/fmmmlee/Classfindr)

**Goal:** a service that scrapes WWU's classfinder and offers a mirror database with more diverse and accessible tools.

**Current status:** No tools are available for an end user, but the program is functional for scraping data and uploading to AWS. Future updates will focus on writing to a local database rather than AWS in order to avoid the upload bottleneck.

## Building:

### Prerequisites

- <a href="https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/setup-install.html">**AWS SDK**</a> - see link for setup and configuration

### Setting Up:

  1. Clone or download the repository
  2. Navigate to the repository folder
  3. Run `mvn clean install` in terminal - this should produce an uberjar (about 9 MB at the time of this commit).
  4. Run with `java -jar [name of Classfindr jar]`

### Built With
- <a href="https://maven.apache.org/">Maven</a>
- <a href="https://github.com/google/gson">Gson</a>
- <a href="https://jsoup.org/">Jsoup</a>

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

### Completed ToDos:
- Implement a metrics function with timers to analyze speed of different implementations
- check AWS traffic tier limitations and check if requests need to be metered
- Implement duplicate CRN checker
- Comment clarification

### "Sometime" - low-priority long term possible features
- user website or app makes requests either to WWU's classfinder or to the AWS mirror (or to another service altogether), depending on the nature of the query and urgency of the information

<br/>
<br/>

**UI**

Here's a shot of what the UI looks like at this stage (or something close to it)
<p align="left">
  <img src="https://user-images.githubusercontent.com/30479162/57667061-20b97200-75b7-11e9-9650-8233ae3930ec.JPG" width="500" title="a sample run of the scraper and uploader">
</p>
