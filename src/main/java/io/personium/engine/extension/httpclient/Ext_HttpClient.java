/**
 * Personium
 * Copyright 2017 FUJITSU LIMITED
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

import io.personium.engine.extension.support.AbstractExtensionScriptableObject;
import io.personium.engine.extension.support.ExtensionErrorConstructor;
import io.personium.engine.extension.support.ExtensionLogger;

import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.annotations.JSConstructor;
import org.mozilla.javascript.annotations.JSFunction;

/**
 * Engine-Extension HttpClient.
 */
@SuppressWarnings("serial")
public class Ext_HttpClient extends AbstractExtensionScriptableObject {

    /**
     * Public name to JavaScript.
     */
    @Override
    public String getClassName() {
        return "HttpClient";
    }

    /**
     * constructor.
     */
    @JSConstructor
    public Ext_HttpClient() {
    	ExtensionLogger logger = new ExtensionLogger(this.getClass());
    	setLogger(this.getClass(), logger);
    }

    /**
     * get.
     * @param uri String
     * @param headers JSONObject
     * @return JSONObject
     */

    @JSFunction
    public NativeObject get(String uri, NativeObject headers) {
    	NativeObject result = null;

        if (null == uri || uri.isEmpty()) {
		    String message = "URL parameter is not set.";
		    this.getLogger().info(message);
		    throw ExtensionErrorConstructor.construct(message);
        }

		try (CloseableHttpClient httpclient = HttpClientBuilder.create().build()) {
    		HttpGet request = new HttpGet(uri);

	        // set request headers
	        if (null != headers) {
	        	for (Entry<Object, Object> e : headers.entrySet()){
	        		request.addHeader(e.getKey().toString(), e.getValue().toString());
        		}
	        }

	        HttpResponse res = null;
	        res = httpclient.execute(request);

	    	// Retrieve the status
	        String status = String.valueOf(res.getStatusLine().getStatusCode());

	        // Retrieve the response headers
	        JSONObject headersJson = new JSONObject();
	        Header[] resHeaders = res.getAllHeaders();
	        for (Header header : resHeaders) {
	        	headersJson.put(header.getName(), header.getValue());
	        }

	        // get Body
	        String body = "";
	        HttpEntity entity = res.getEntity();
            if (entity != null) {
        		// Text only.
    	    	body = EntityUtils.toString(entity, "UTF-8");
			}

	        // set NativeObject
            result = new NativeObject();
	        result.put("status", result, status);
	        result.put("headers", result, headersJson.toString());
	        result.put("body", result, body);

	    } catch (Exception e) {
            String message = "An error occurred.";
            this.getLogger().warn(message, e);
            String errorMessage = String.format("%s Cause: [%s]",
            		message, e.getClass().getName() + ": " + e.getMessage());
            throw ExtensionErrorConstructor.construct(errorMessage);
		}
        return result;
    }

    /**
     * Post.
     * @param uri String
     * @param body String
     * @param contentType String
     * @param headers NativeObject
     * @return NativeObject
     */
    @SuppressWarnings("unchecked")
    @JSFunction
	public NativeObject post(String uri, String body, String contentType, NativeObject headers) {
    	NativeObject result = null;

        if (null == uri || uri.isEmpty()) {
		    String message = "URL parameter is not set.";
		    this.getLogger().info(message);
		    throw ExtensionErrorConstructor.construct(message);
        }
        if (null == contentType || contentType.isEmpty()) {
		    String message = "contentType parameter is not set.";
		    this.getLogger().info(message);
		    throw ExtensionErrorConstructor.construct(message);
        }
        if (null == body || body.isEmpty()) {
		    String message = "body parameter is not set.";
		    this.getLogger().info(message);
		    throw ExtensionErrorConstructor.construct(message);
        }

		try (CloseableHttpClient httpclient = HttpClientBuilder.create().build()) {
	        HttpPost request = null;

	        // set params from body
        	request = new HttpPost(uri);
        	HttpEntity entity = new ByteArrayEntity(body.getBytes("UTF-8"));
        	request.setEntity(entity);

        	// set contentType
	        request.setHeader("Content-Type", contentType);

	        // set heades
	        if (null != headers) {
                for (Entry<Object, Object> e : headers.entrySet()){
                	request.addHeader(e.getKey().toString(), e.getValue().toString());
        	    }
            }

	        // execute
	        HttpResponse res = null;
			res = httpclient.execute(request);

			// get Status
	    	String status = String.valueOf(res.getStatusLine().getStatusCode());
	        if(res.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
	            System.out.println("StatusCode:" + res.getStatusLine().getStatusCode());
	            return null;
	        }

	        // response headers
	        JSONObject headersJson = new JSONObject();
	        Header[] resHeaders = res.getAllHeaders();
	        for (Header header : resHeaders) {
            	System.out.println(header.getName() + ":" + header.getValue());
	        	headersJson.put(header.getName(), header.getValue());
	        }

            // entity
	        String resBody = "";
	        HttpEntity resEntity = res.getEntity();
	        if (resEntity != null) {
           		// Text only.
           		resBody = EntityUtils.toString(resEntity, "UTF-8");
	        }

	        // set NativeObject
            result = new NativeObject();
	        result.put("status", result, status);
	        result.put("headers", result, headersJson.toString());
	        result.put("body", result, resBody);

        }catch (Exception e) {
            String message = "An error occurred.";
            this.getLogger().warn(message, e);
            String errorMessage = String.format("%s Cause: [%s]",
            		message, e.getClass().getName() + ": " + e.getMessage());
            throw ExtensionErrorConstructor.construct(errorMessage);
        }
    	return result;
    }
}
