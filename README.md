# Classfindr [![Build Status](https://travis-ci.org/fmmmlee/Classfindr.svg?branch=master)](https://travis-ci.org/fmmmlee/Classfindr) [![Current Version](https://img.shields.io/badge/version-0.3.3-blue.svg?style=flat)](https://github.com/fmmmlee/Classfindr/blob/master/pom.xml)

**Goal:** a service that scrapes Western Washington University's classfinder and generates a mirror database with more diverse and accessible tools.

**Current status:** No tools are available for an end user, but the program is functional for scraping data and uploading to AWS. Embedded database integration with H2 is partially functional, but has no tools for accessing the data yet. Local writing also contains bugs of varying degrees of severity.

## Building:

### Prerequisites

- <a href="https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/setup-install.html">**AWS SDK**</a> - see link for setup and configuration
- <a href="https://maven.apache.org/">**Maven**</a>

### Setting Up:

  1. Clone or download the repository
  2. Navigate to the repository folder
  3. Run `mvn clean install` in terminal - this should produce an uberjar (about 9 MB at the time of this commit).
  4. Run with `java -jar [name of Classfindr jar]`

### Built With
- <a href="https://maven.apache.org/">Maven</a>
- <a href="https://jsoup.org/">Jsoup</a>
- <a href="https://www.h2database.com">H2 DB</a>

## Current ToDo
- Complete documentation and in-code comments
- Write functions to read from DynamoDB/local database
- Overhaul document parser; it's convoluted and fails to account for some of the formatting idiosyncrasies of WWU's Classfinder
- Implement BatchWriteItem
- Add constraints for requests to WWU servers in a YAML or JSON
- write script to run a build of this every so often to keep the database updated

### Completed ToDos:
- Clean up overall application structure and file organization
- Implement a metrics function with timers to analyze speed of different implementations
- check AWS traffic tier limitations and check if requests need to be metered
- Add writing to a local database instead of DynamoDB
- Implement duplicate CRN checker
- Auto-create missing duplicate-checking files if not present
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
