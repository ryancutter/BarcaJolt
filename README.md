Introduction
============

Lightweight Java CouchDB library specifically built for interacting with Cloudant databases on Android devices. Uses org.apache.http and org.json objects. Current API provides read-only access to Cloudant databases and implements some of Cloudant's API. Perhaps more to follow in future releases contingent on user requests.

Installation
============

JAR available in Downloads section. Also feel free to rip source code in accordance with MIT/BSD license.

Usage
=====

API available at http://ryancutter.webfactional.com/barcajolt/. Here's some sample code (host, username, and password aren't real): 

BarcaJolt barca = new BarcaJolt("barcatime.cloudant.com", "platseenetheheyetheryoun", "pkDrmwOaqVgKlcY3wGPWfssV");<p>
    	
JSONObject finalsObject = barca.get("/bigdatabase/_design/zapit/_view/finals");<p>

int totalRows = BarcaJolt.getTotalRows(finalsObject);<br>
int offset = BarcaJolt.getOffset(finalsObject);<br>
JSONArray finalsArray = BarcaJolt.getRows(finalsObject);<br>

for(int i = 0; i < finalsArray.length(); i ++) {<br>
&nbsp;&nbsp;JSONObject finals = BarcaJolt.getValueFromArray(finalsArray, i);<br>
&nbsp;&nbsp;String date = finals.getString("Date").substring(0, 10);<br>
&nbsp;&nbsp;String symbol = BarcaJolt.getKeyFromArray(finalsArray, i);<br>

Policy
======

MIT/BSD license - no restrictions.

TODO
====

Bunches. This version contains a very partial implementation of Cloudant's API. I'd like to include all of the Search API (http://support.cloudant.com/kb/search/search-api) and perhaps support write operations as well. I'm open to all suggesstions. 

Author
======

Written by Ryan Cutter (ryancutter at gmail dot com).  Homepage at http://ryancutter.org.
