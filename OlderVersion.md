# V1.0.1  

## Get example  
    var url = "http://www.example.com/";
    var headers = {'Accept': 'text/plain'};

    try {
      var httpClient = new _p.extension.HttpClient();
      var response = { status: "", headers : {}, body :"" };
      response = httpClient.get(url, headers);
    } catch (e) {
      return {
          status: 500,
          headers: {"Content-Type":"text/html"},
          body: ["Server Error occurred. " + e]
      };
    }

    // Check whether response is null or not
    if (response) {
        return {
          status: 200,
          headers: {"Content-Type":"text/plain"},
          body: ['{"status":' + response.status + ', "headers" ' + response.headers.toString()
               + ' "body":' + response.body + '}']
        };
    } else {
        // error handling
    }

## POST example  
    var url = "http://www.example.com/";
    var body = "bodyParameter1=XXXXX&bodyParameter2=YYYYY";
    var contentType = "application/x-www-form-urlencoded;";
    var headers = {'Accept': 'text/plain'};

    try {
      var httpClient = new _p.extension.HttpClient();
      var response = { status: "", headers : {}, body :"" };
      response = httpClient.postParam(url, body, contentType, headers);
    } catch (e) {
      return {
          status: 500,
          headers: {"Content-Type":"text/html"},
          body: ["Server Error occurred. " + e]
      };
    }

    if (response) {
        return {
          status: 200,
          headers: {"Content-Type":"application/json"},
          body: ['{"status":' + response.status + ', "headers" ' + response.headers.toString()
               + ' "body":'+ response.body + '}']
        };
    } else {
        // error handling
    }
