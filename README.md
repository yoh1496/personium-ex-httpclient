# personium-ex-httpclient

## About

This is a [Personium](http://personium.io)'s Engine extension for enable HTTP request from server-side script.

## Note

This can use "GET" and "POST" request only.

## Usage (GET)

````
  var urlY = "http://www.example.com/";
  var headersY = {'Accept': 'application/json'};

  try {
      var httpclient = new _p.extension.HttpClient();
      var apiRes = { status: "", headers : {}, body :"" };
      apiRes = httpclient.get(urlY, headersY);
  } catch (e) {
      return {
          status: 500,
          headers: {"Content-Type":"text/plain"},
          body: ["Server Error occured" + e]
      };
  }
  if (apiRes !==  null && apiRes.status === "200") {
      return {
          status: 200,
          headers: {"Conent-Type":"text/plain"},
          body: ['{"status":' + apiRes.status + ', "resHeaders" '+ apiRes.headers.toString() +' "resBody":'+ apiRes.body.toString() + '}']
      };
  }

````
## Usage (POST)

````
  var urlY = "http://www.example.com/";
  var bodyY = "bodyParameter1=XXXXX&bodyParameter2=YYYYY";
  var contentTypeY = "application/x-www-form-urlencoded;";
  var headersY = {'Accept': 'application/json'};


  try {
      var httpclient = new _p.extension.HttpClient();
      var apiRes = { status: "", headers : {}, body :"" };
      apiRes = httpclient.post(urlY, bodyY, contentTypeY, headersY);;
  } catch (e) {
      return {
          status: 500,
          headers: {"Content-Type":"text/plain"},
          body: ["Server Error occured" + e]
      };
  }
  if (apiRes !==  null && apiRes.status === "200") {
      return {
          status: 200,
          headers: {"Conent-Type":"text/plain"},
          body: ['{"status":' + apiRes.status + ', "resHeaders" '+ apiRes.headers.toString() +' "resBody":'+ apiRes.body.toString() + '}']
      };
  }


````

## License

```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Copyright 2017 FUJITSU LIMITED
```

