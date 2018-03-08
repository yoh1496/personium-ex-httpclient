# personium-ex-httpclient

## About

[Personium](http://personium.io) Engine Extension to provide HTTP client functionality.

## Note

Supported methods are "GET", "POST", "PUT", "DELETE" only at this point.

## Usage (GET)

```` Javascript
  // Always return the same format to the caller
  var createResponse = function(tempCode, tempBody) {
    var isString = typeof tempBody == "string";
    return {
        status: tempCode,
        headers: {"Content-Type":"application/json"},
        body: [isString ? tempBody : JSON.stringify(tempBody)]
    };
  }
  
  var url = "http://www.example.com/";
  var headers = {'Accept': 'text/plain'};
  var httpClient = new _p.extension.HttpClient();
  var httpCode, response;

  try {
      response = httpclient.get(url, headers);
  } catch (e) {
      // System exception
      return createResponse(500, e);
  }
  httpCode = parseInt(response.status);
  // Create API usually returns HTTP code 201
  if (httpCode !== 200) {
      // Personium exception
      return createResponse(httpCode, response.body);
  }

  // Do something and then return data
  return createResponse(200, response.body);

````

## Usage (POST)

```` Javascript
  // Always return the same format to the caller
  var createResponse = function(tempCode, tempBody) {
    var isString = typeof tempBody == "string";
    return {
        status: tempCode,
        headers: {"Content-Type":"application/json"},
        body: [isString ? tempBody : JSON.stringify(tempBody)]
    };
  }

  var url = "http://www.example.com/";
  var contentType = "application/x-www-form-urlencoded;";
  var headers = {'Accept': 'text/plain'};
  var body = "bodyParameter1=XXXXX&bodyParameter2=YYYYY";
  var httpClient = new _p.extension.HttpClient();
  var httpCode, response;

  try {
      response = httpclient.post(url, headers, contentType, body);
  } catch (e) {
      // System exception
      return createResponse(500, e);
  }
  httpCode = parseInt(response.status);
  // Create API usually returns HTTP code 201
  if (httpCode !== 201) {
      // Personium exception
      return createResponse(httpCode, response.body);
  }

  // Do something and then return data
  return createResponse(200, response.body);

````

## Usage (PUT)

```` Javascript
  // Always return the same format to the caller
  var createResponse = function(tempCode, tempBody) {
    var isString = typeof tempBody == "string";
    return {
        status: tempCode,
        headers: {"Content-Type":"application/json"},
        body: [isString ? tempBody : JSON.stringify(tempBody)]
    };
  }

  var url = "http://www.example.com/";
  var contentType = "application/x-www-form-urlencoded;";
  var headers = {'Accept': 'text/plain'};
  var body = "bodyParameter1=XXXXX&bodyParameter2=YYYYY";
  var httpClient = new _p.extension.HttpClient();
  var httpCode, response;

  try {
      response = httpclient.put(url, headers, contentType, body);
  } catch (e) {
      // System exception
      return createResponse(500, e);
  }
  httpCode = parseInt(response.status);
  // Create API usually returns HTTP code 201
  if (httpCode !== 201) {
      // Personium exception
      return createResponse(httpCode, response.body);
  }

  // Do something and then return data
  return createResponse(200, response.body);

````


## Usage (DELETE)

```` Javascript
  // Always return the same format to the caller
  var createResponse = function(tempCode, tempBody) {
    var isString = typeof tempBody == "string";
    return {
        status: tempCode,
        headers: {"Content-Type":"application/json"},
        body: [isString ? tempBody : JSON.stringify(tempBody)]
    };
  }
  
  var url = "http://www.example.com/";
  var headers = {'Accept': 'text/plain'};
  var httpClient = new _p.extension.HttpClient();
  var httpCode, response;

  try {
      response = httpclient.delete(url, headers);
  } catch (e) {
      // System exception
      return createResponse(500, e);
  }
  httpCode = parseInt(response.status);
  // DELETE request usually returns HTTP code 204
  if (httpCode !== 204) {
      // Personium exception
      return createResponse(httpCode, response.body);
  }

  // Do something and then return data
  return createResponse(200, response.body);

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

