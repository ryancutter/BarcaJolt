Introduction
============

Lightweight Java CouchDB library specifically built for interacting with Cloudant databases on Android devices. Uses org.apache.http and org.json objects. Current API provides read-only access to Cloudant databases and implements some of Cloudant's API. Perhaps more to follow in future releases contingent on user requests.

Installation
============

JAR available in Downloads section. Also feel free to rip source code in accordance with MIT/BSD license.

Usage
=====

Not complicated. Add your HOST, PORT, PROTOCOL, USERNAME, PASSWORD parameters. Everything else is static:

JSONObject jObject = BarcaJolt.get("/bigdatabase/_design/frenzy/_view/finals");

int totalRows = BarcaJolt.getTotalRows(jObject);<br>
int offset = BarcaJolt.getOffset(jObject);<br>
JSONArray rowArray = BarcaJolt.getRows(jObject);<p>
        
for(int i = 0; i < rowArray.length(); i++) {<br>
&nbsp;&nbsp;JSONObject valueArray = BarcaJolt.getValueFromArray(rowArray, i);<br>
&nbsp;&nbsp;String key = BarcaJolt.getKeyFromArray(rowArray, i);<br>
&nbsp;&nbsp;String id = BarcaJolt.getIdFromArray(rowArray, i);<br>

Policy
======

MIT/BSD license - no restrictions.

TODO
====

Bunches. This version contains a very partial implementation of Cloudant's API. I'd like to include all of the Search API (http://support.cloudant.com/kb/search/search-api) and perhaps support write operations as well. I'm open to all suggesstions. 

Author
======

Written by Ryan Cutter (ryancutter at gmail dot com).  Homepage at http://ryancutter.org.
