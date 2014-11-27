BSCrawler
=========

A BlogSpot crawler designed to index all the blogs hosted on blogspot and their language.

During my work at Columbia University speech lab I was requested to harvest the web for blog
data in low resource languages (Kazakh, Telugu, Tok Pisin, Lithuanian, Kurmanji). Unfortunately there are not many
blogs in those languages and Google/Bing does not support filtering results by all of these languages.
This tool is designed to crawl the entire blogspot blog chain and index each blog and its language using
Google's Compact Language Detection library.  It's designed in a multi-threaded  architecture with possible support for running in a cluster.


This is still work in progress so no build/install instructions yet.


#####Dependencies:
- Maven 3.2.1
- MySQL 5.5
- [Google CLD](https://code.google.com/p/cld2/)


