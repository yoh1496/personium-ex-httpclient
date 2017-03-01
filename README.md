# personium-ex-httpclient

## About

[Personium](http://personium.io) Engine Extension to provide HTTP client functionality.

## Note

Supported methods are "GET" and "POST" only at this point.

## Usage (GET)

````
  var url = "http://www.example.com/";
  var headers = {'Accept': 'text/plain'};

  try {
      var httpclient = new _p.extension.HttpClient();
      var response = { status: "", headers : {}, body :"" };
      response = httpclient.get(url, headers);
  } catch (e) {
      return {
          status: 500,
          headers: {"Content-Type":"text/html"},
          body: ["Server Error occurred. " + e]
      };
  }
  return {
      status: 200,
      headers: {"Content-Type":"text/plain"},
      body: ['{"status":' + response.status + ', "headers" ' + response.headers.toString()
           + ' "body":' + response.body + '}']
  };

````
## Usage (POST)

````
  var url = "http://www.example.com/";
  var body = "bodyParameter1=XXXXX&bodyParameter2=YYYYY";
  var contentType = "application/x-www-form-urlencoded;";
  var headers = {'Accept': 'text/plain'};

  try {
      var httpclient = new _p.extension.HttpClient();
      var response = { status: "", headers : {}, body :"" };
      response = httpclient.post(url, body, contentType, headers);
  } catch (e) {
      return {
          status: 500,
          headers: {"Content-Type":"text/html"},
          body: ["Server Error occurred. " + e]
      };
  }
  return {
      status: 200,
      headers: {"Content-Type":"application/json"},
      body: ['{"status":' + response.status + ', "headers" ' + response.headers.toString()
           + ' "body":'+ response.body + '}']
  };

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

