/**
 * personium.io
 * Copyright 2014 FUJITSU LIMITED
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.personium.engine.extension.httpclient;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mozilla.javascript.NativeObject;

@SuppressWarnings("unused")
public class Ext_HttpClientTest {

	// http_get
	private static final String URI_HTTP_GET = "http://get.example/";

	// http_post
	private static final String URI_HTTP_POST = "http://post.example/";
	private static final String POST_PARAMS = "key1=value1&key2=value2&key3=value3";
	private static final String POST_CONTENT_TYPE = "text/plain";

	// headers
	private static final String HEADER_KEY = "Accept";
	private static final String HEADER_VALUE = "application/json";

    @BeforeClass
    public static void beforeClass() {

    }

    @Before
    public void before() throws Exception {

    }

    @After
    public void after() {

    }

    @Test
    public void http_get() {
    	NativeObject headers = new NativeObject();
    	headers.put(HEADER_KEY, headers, HEADER_VALUE);

    	Ext_HttpClient ext_httpClient = new Ext_HttpClient();

    	/**
         * ext_httpClient.get
         * String uri, NativeObject headers
         */
    	NativeObject result = ext_httpClient.get(URI_HTTP_GET, headers);
    	String res_status = (String)result.get("status");
    	String res_body = (String)result.get("body");
    	String res_headers = (String)result.get("headers");

    	assertEquals(Integer.parseInt(res_status), HttpStatus.SC_OK);
    }

    @Test
    public void http_post() {
    	NativeObject headers = new NativeObject();
    	headers.put(HEADER_KEY, headers, HEADER_VALUE);

    	Ext_HttpClient ext_httpClient = new Ext_HttpClient();

    	/**
         * ext_httpClient.post
         * String uri, String body, String contentType, NativeObject headers
         */
    	NativeObject result = ext_httpClient.post(URI_HTTP_POST, POST_PARAMS,
    			POST_CONTENT_TYPE, headers);
    	String res_status = (String)result.get("status");
    	String res_body = (String)result.get("body");
    	String res_headers = (String)result.get("headers");

    	assertEquals(Integer.parseInt(res_status), HttpStatus.SC_OK);
    }
}
