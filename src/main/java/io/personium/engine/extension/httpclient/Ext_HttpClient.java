/**
 * personium.io
 * Copyright 2017-2018 FUJITSU LIMITED
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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.util.TextUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.annotations.JSConstructor;
import org.mozilla.javascript.annotations.JSFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.personium.engine.extension.support.AbstractExtensionScriptableObject;
import io.personium.engine.extension.support.ExtensionErrorConstructor;
import io.personium.engine.extension.support.ExtensionLogger;
import io.personium.engine.extension.wrapper.PersoniumInputStream;

/**
 * Engine-Extension HttpClient.
 */
@SuppressWarnings("serial")
public class Ext_HttpClient extends AbstractExtensionScriptableObject { // CHECKSTYLE IGNORE - Method name is for extension specification.

    /** Logger. */
    private static final Logger log = LoggerFactory.getLogger(Ext_HttpClient.class);

    /** Json keys. */
    private static final String KEY_SKIP_HOSTNAME_VERIFICATION = "IgnoreHostnameVerification";
    private static final String KEY_DEFAULT_HEADERS = "DefaultHeaders";

    /** Ignore SSL varification flag. */
    private boolean ignoreHostnameVerification = false;

    /** Default headers. */
    private JSONObject defaultHeaders = null;

    /**
     * Default constructor.
     */
    public Ext_HttpClient() {
    }

    /**
     * Constructor.
     * @param parameters Json parameters.<p>
     * SkipHostnameVerification true:Skip ssl verification.
     */
    @JSConstructor
    public Ext_HttpClient(NativeObject parameters) {
        ExtensionLogger logger = new ExtensionLogger(this.getClass());
        setLogger(this.getClass(), logger);

        if (parameters != null && !parameters.isEmpty()) {
            setIgnoreHostnameVerification(parameters);
            setDefaultHeaders(parameters);
        }
    }

    private void setIgnoreHostnameVerification(NativeObject parameters) {
        Object argParam = parameters.get(KEY_SKIP_HOSTNAME_VERIFICATION);
        if (argParam == null) {
            return;
        }
        if (!(argParam instanceof Boolean)) {
            String message = String.format("Parameter [%s] is not Boolean.", KEY_SKIP_HOSTNAME_VERIFICATION);
            this.getLogger().info(message);
            throw ExtensionErrorConstructor.construct(message);
        }
        ignoreHostnameVerification = (Boolean) argParam;
    }

    private void setDefaultHeaders(NativeObject parameters) {
        Object argParam = parameters.get(KEY_DEFAULT_HEADERS);
        if (argParam == null) {
            return;
        }
        if (!(argParam instanceof String)) {
            String message = String.format("Parameter [%s] is not String.", KEY_DEFAULT_HEADERS);
            this.getLogger().info(message);
            throw ExtensionErrorConstructor.construct(message);
        }
        try {
            defaultHeaders = (JSONObject) (new JSONParser()).parse((String) argParam);
        } catch (org.json.simple.parser.ParseException e) {
            String message = String.format("Parameter [%s] is not JSON: %s.", KEY_DEFAULT_HEADERS, e.getMessage());
            this.getLogger().info(message);
            throw ExtensionErrorConstructor.construct(message);
        }
    }

    /**
     * Public name to JavaScript.
     */
    @Override
    public String getClassName() {
        return "HttpClient";
    }

    /**
     * get.
     * @param url String
     * @param headers JSONObject
     * @param respondsAsStream true:stream/false:text
     * @return JSONObject
     */
    @JSFunction
    public NativeObject get(String url, NativeObject headers, boolean respondsAsStream) {
        // Verification.
        verifyParamIsEmpty(url, "url");

        HttpGet get = new HttpGet(url);
        addRequestHeaders(get, headers);

        try (CloseableHttpClient httpclient = createHttpClient()) {
            // Request
            HttpResponse res = httpclient.execute(get);
            // Response
            return createResponseToJavascript(res, respondsAsStream);
        } catch (IOException e) {
            throw ExtensionErrorConstructor.construct(createErrorMessage(e));
        }
    }

    /**
     * postParam (String).
     * This method name is old version (v1.0.1 or older).
     * @param uri String
     * @param headers NativeObject
     * @param contentType String
     * @param params String
     * @return NativeObject
     */
    @JSFunction
    public NativeObject postParam(String uri, NativeObject headers, String contentType, String params) {
        return post(uri, headers, contentType, params, null, null);
    }

//    /**
//     * postStream (PersoniumInputStream).
//     * @param uri String
//     * @param headers NativeObject
//     * @param contentType String
//     * @param params String
//     * @param pis PersoniumInputStream
//     * @param fileName String
//     * @return NativeObject
//     */
// PostのStreamは、動作が確認できていないためコメント。
//    @JSFunction
//    public NativeObject postStream(String uri, NativeObject headers, String contentType,
//            PersoniumInputStream pis, String fileName) {
//        return post(uri, headers, contentType, null, pis, fileName);
//    }

    /**
     * post (String).
     * @param uri String
     * @param headers NativeObject
     * @param contentType String
     * @param params String
     * @return NativeObject
     */
    @JSFunction
    public NativeObject post(String uri, NativeObject headers, String contentType, String params) {
        return post(uri, headers, contentType, params, null, null);
    }

    /**
     * putParam (String).
     * This method name is old version (v1.0.1 or older).
     * @param uri String
     * @param headers NativeObject
     * @param contentType String
     * @param params String
     * @return NativeObject
     */
    @JSFunction
    public NativeObject putParam(String uri, NativeObject headers, String contentType, String params) {
        return put(uri, headers, contentType, params, null, null);
    }

    /**
     * put (String).
     * @param uri String
     * @param headers NativeObject
     * @param contentType String
     * @param params String
     * @return NativeObject
     */
    @JSFunction
    public NativeObject put(String uri, NativeObject headers, String contentType, String params) {
        return put(uri, headers, contentType, params, null, null);
    }

    /**
     * patch (String).
     * @param url String
     * @param headers NativeObject
     * @param contentType String
     * @param params String
     * @return NativeObject
     */
    @JSFunction
    public NativeObject patch(String url, NativeObject headers, String contentType, String params) {
        boolean respondsAsStream = false;

        // Verification.
        verifyParamIsEmpty(url, "url");
        verifyParamIsEmpty(contentType, "contentType");
        verifyParamIsEmpty(params, "body");

        HttpPatch patch = new HttpPatch(url);
        // set contentType
        patch.addHeader("Content-Type", contentType);
        // set headers
        addRequestHeaders(patch, headers, contentType);
        // set body
        addRequestBody(patch, params);

        try (CloseableHttpClient httpclient = createHttpClient()) {
            // Request
            HttpResponse res = httpclient.execute(patch);
            // Response
            return createResponseToJavascript(res, respondsAsStream);
        } catch (IOException e) {
            throw ExtensionErrorConstructor.construct(createErrorMessage(e));
        }
    }

    /**
     * delete.
     * @param url String
     * @param headers JSONObject
     * @param respondsAsStream true:stream/false:text
     * @return JSONObject
     */
    @JSFunction
    public NativeObject delete(String url, NativeObject headers, boolean respondsAsStream) {
        // Verification.
        verifyParamIsEmpty(url, "url");

        HttpDelete delete = new HttpDelete(url);
        addRequestHeaders(delete, headers);

        try (CloseableHttpClient httpclient = createHttpClient()) {
            // Request
            HttpResponse res = httpclient.execute(delete);
            // Response
            return createResponseToJavascript(res, respondsAsStream);
        } catch (IOException e) {
            throw ExtensionErrorConstructor.construct(createErrorMessage(e));
        }
    }

    /**
     * Post.
     * @param url String
     * @param headers NativeObject
     * @param contentType String
     * @param params String
     * @param is PersoniumInputStream
     * @param fileName String
     * @return NativeObject
     */
    private NativeObject post(String url, NativeObject headers, String contentType,
                              String params, PersoniumInputStream pis, String fileName) {
        boolean respondsAsStream = false;

        // Verification.
        verifyParamIsEmpty(url, "url");
        verifyParamIsEmpty(contentType, "contentType");
        verifyParamIsEmpty(params, "body");

        HttpPost post = new HttpPost(url);
        // set contentType
        post.addHeader("Content-Type", contentType);
        // set headers
        addRequestHeaders(post, headers, contentType);
        // set body
        addRequestBody(post, params);

        try (CloseableHttpClient httpclient = createHttpClient()) {
            // Request
            HttpResponse res = httpclient.execute(post);
            // Response
            return createResponseToJavascript(res, respondsAsStream);
        } catch (IOException e) {
            throw ExtensionErrorConstructor.construct(createErrorMessage(e));
        }
    }

    /**
     * put.
     * @param url String
     * @param headers NativeObject
     * @param contentType String
     * @param params String
     * @param is PersoniumInputStream
     * @param fileName String
     * @return NativeObject
     */
    private NativeObject put(String url, NativeObject headers, String contentType,
                              String params, PersoniumInputStream pis, String fileName) {
        boolean respondsAsStream = false;

        // Verification.
        verifyParamIsEmpty(url, "url");
        verifyParamIsEmpty(contentType, "contentType");
        verifyParamIsEmpty(params, "body");

        HttpPut put = new HttpPut(url);
        // set contentType
        put.addHeader("Content-Type", contentType);
        // set headers
        addRequestHeaders(put, headers, contentType);
        // set body
        addRequestBody(put, params);

        try (CloseableHttpClient httpclient = createHttpClient()) {
            // Request
            HttpResponse res = httpclient.execute(put);
            // Response
            return createResponseToJavascript(res, respondsAsStream);
        } catch (IOException e) {
            throw ExtensionErrorConstructor.construct(createErrorMessage(e));
        }
    }

    /**
     * Verify that the parameter is empty.
     * If the parameter is empty throw exception.
     * @param param target parameter
     * @param paramName parameter name
     */
    private void verifyParamIsEmpty(String param, String paramName) {
        if (TextUtils.isEmpty(param)) {
            String message = String.format("Parameter [%s] is not set.", paramName);
            this.getLogger().info(message);
            throw ExtensionErrorConstructor.construct(message);
        }
    }

    /**
     * Add http request headers.
     * @param request http request method object
     * @param headers http headers
     * @return Request with header added
     */
    private HttpRequestBase addRequestHeaders(HttpRequestBase request, NativeObject headers) {
        return addRequestHeaders(request, headers, null);
    }

    /**
     * Add http request headers.
     * @param request http request method object
     * @param headers http headers
     * @param contentType
     * @return Request with header added
     */
    private HttpRequestBase addRequestHeaders(HttpRequestBase request, NativeObject headers, String contentType) {
        // Set default headers.
        if (defaultHeaders != null) {
            for (@SuppressWarnings("rawtypes") Iterator iterator = defaultHeaders.keySet().iterator();
                    iterator.hasNext();) {
                String key = (String) iterator.next();
                if (key.equals("Content-Type") && contentType != null) {
                    continue;
                }
                if (headers.get(key) == null) {
                    headers.put(key, headers, (String) defaultHeaders.get(key));
                }
            }
        }
        // Set request headers.
        if (headers != null) {
            for (Entry<Object, Object> e : headers.entrySet()) {
                request.addHeader(e.getKey().toString(), e.getValue().toString());
            }
        }
        if (log.isDebugEnabled()) {
            for (Header header: request.getAllHeaders()) {
                log.debug("{}: {}", header.getName(), header.getValue());
            }
        }
        return request;
    }

    /**
     * Add http request body.
     * @param request http request method object
     * @param bodyString http body string
     * @return Request with body added
     */
    private HttpRequestBase addRequestBody(HttpEntityEnclosingRequestBase request, String bodyString) {
        // set body
        try {
            request.setEntity(new ByteArrayEntity(bodyString.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            throw ExtensionErrorConstructor.construct(createErrorMessage(e));
        }
        return request;
    }

    /**
     * Create response from HttpResponse to javascript.
     * @param res http response
     * @param respondsAsStream Flag as to whether body is returned as stream
     * @return response to javascript
     */
    @SuppressWarnings("unchecked")
    private NativeObject createResponseToJavascript(HttpResponse res, boolean respondsAsStream) {
        // Retrieve the status.
        int resStatus = res.getStatusLine().getStatusCode();
        if (log.isDebugEnabled()) {
            log.debug("status:" + resStatus);
        }

        // Retrieve the response headers.
        JSONObject resHeaders = new JSONObject();
        for (Header header : res.getAllHeaders()) {
            resHeaders.put(header.getName(), header.getValue());
        }

        // Set NativeObject.
        NativeObject result = new NativeObject();
        result.put("status", result, Integer.toString(resStatus));
        result.put("headers", result, resHeaders.toString());

        HttpEntity entity = res.getEntity();
        if (entity != null) {
            if (respondsAsStream) {
                try {
                    // InputStream > PersoniumInputStream.
                    InputStream is = new BufferedHttpEntity(res.getEntity()).getContent();
                    PersoniumInputStream pis = new PersoniumInputStream((InputStream) is);
                    result.put("body", result, (PersoniumInputStream) pis);
                } catch (IOException e) {
                    throw ExtensionErrorConstructor.construct(createErrorMessage(e));
                }
            } else {
                try {
                    // String.
                    result.put("body", result, EntityUtils.toString(entity, "UTF-8"));
                } catch (ParseException | IOException e) {
                    throw ExtensionErrorConstructor.construct(createErrorMessage(e));
                }
            }
        }
        return result;
    }

    /**
     * Create and return instance of HttpClient.
     * @return HttpClient
     */
    private CloseableHttpClient createHttpClient() {
        HttpClientBuilder builder = HttpClients.custom();

        // SSL verify settings.
        if (ignoreHostnameVerification) {
            try {
                builder.setSSLContext(createSkipSSLVerifyContext());
            } catch (GeneralSecurityException e) {
                throw ExtensionErrorConstructor.construct(createErrorMessage(e));
            }
        }

        // Proxy settings.
        builder.useSystemProperties();

        return builder.build();
    }

    /**
     * Create and return the SSLContext that skips ssl verification.
     * @return SSL context.
     * @throws GeneralSecurityException security error
     */
    private SSLContext createSkipSSLVerifyContext() throws GeneralSecurityException {
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        TrustManager tm = new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        sslContext.init(null, new TrustManager[] {tm}, null);
        return sslContext;
    }

    /**
     * Create and return error message.
     * @param e error
     * @return Error message
     */
    private String createErrorMessage(Exception e) {
        String message = "An error occurred.";
        this.getLogger().warn(message, e);
        return String.format("%s Cause: [%s: %s]", message, e.getClass().getName(), e.getMessage());
    }
}
