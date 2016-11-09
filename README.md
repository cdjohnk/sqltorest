# sqltorest
A Jersey and Hibernate based library for quickly standing up DB backed restful API's.

Currently for GET only.

This is very beta right now (11/2/2016). Lots of updates coming in the next few days: function, tests, docs.

# API Searching

First off, the simplest way to search is via GET with a query string in the URL:

http://www.apihost.com/api/resources?name=res&count=3

This type of searching, while rudimentary, is very useful and will be supported. However, this type of search ignores non-trivial search scenarios like inequalities, ranges, string functions, etc.

To support this type of searching, a search defined in JSON can be POSTed to a URI specific for searching:

http://www.apihost.com/api/resources/search

The JSON search object would look like this:
<pre>
{
"fields":["test.name","test.count"],
"terms":[ {"linkage":"and","field":"test.name","operator":"like","value":"%res%","id":"1"}, 
    {"linkage":"or","field":"test.count","operator":"gte","value":"3", "id":"2"}, 
    {"linkage":"or", "terms": [
        {"linkage":"and","field":"test.name","operator":"like","value":"ros%","id":"3"},  
        {"linkage":"or","field":"test.count","operator":"lte","value":"3", "id":"4"} ]}],
"orderby":[ {"field":"test.name","direction":"desc"}, 
    {"field":"test.count","direction":"asc"}],
"pagestart":"1",
"pagesize":"10"
}
</pre>
The fields array is optional. If included, only those fields will be returned. If not included, all fields will be returned.

Javascript search objects must always include both a terms and orderby object as arrays. The arrays may both be empty. If one or more term objects are supplied, each must always have id and linkage properties, and either a nested terms object or field, operator, and value properties. If one or more orderby objects are supplied, they must always have field and direction properties. Field types are NOT specified in the search terms. Nevertheless, not all operators may be used with all field types – in particular, the string operators listed below will only work on strings.

Pagestart and pagesize properties are both optional. If not provided, pagestart defaults to 1. If not provided, pagesize defaults to 25.

As an example, if the above search object were executed against a table named common_names, then the resulting query might look something like this:
<pre>
SELECT test.name, test.count
FROM Test as test
WHEREtest.name LIKE '%:name1%' and
test.count &gt;= :count2 or
(test.name LIKE '%:name3%' and
test.count &lt; :count4)
ORDER BY test.name desc, test.count asc
</pre>
With the pagestart and pagesize properties being handled in code.

Here are the supported search operators:
<pre>
All Types
equals - equals
gt – greater than
lt – less than
gte – greater than or equals
lte – less than or equals
empty – no value or null
notempty – values of greater than zero length

Strings
like – string searching - use '%' to match any string. ex: 'Dav%' finds any string beginning with 'Dav'.
</pre>
Valid formats for date/time values are limited to the following:
<pre>
YYYY-MM-DD
YYYY-MM-DD 00:00
YYYY-MM-DD 00:00:00.000
</pre>
Note that, while inequality operators (lt, gt, lte, gte) will work for any type, the results they produce will be specific to that type: strings will be selected based on alpha ordering, numbers on numeric ordering, and date/times on time ordering.
