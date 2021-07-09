/**
 * personium.io
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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpStatus;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mozilla.javascript.NativeObject;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import io.personium.engine.extension.wrapper.PersoniumInputStream;

@SuppressWarnings("unused")
public class Ext_HttpClientTest {

    // mock server
    private static final String MOCK_SERVER_URL       = "http://localhost:8080";

    // http_get
    private static final String PATH_HTTP_GET_TEXT    = "/getText";
    private static final String PATH_HTTP_GET_STREAM  = "/getStream";

    // http_post
    private static final String PATH_HTTP_POST_TEXT   = "/postText";
    private static final String PATH_HTTP_POST_STREAM = "/postStream";

    private static final String POST_PARAMS_TEXT      = "key1=value1&key2=value2&key3=value3";
    private static final String POST_CONTENT_TYPE     = "application/x-www-form-urlencoded;";

    // file
    private static final String POST_FILE_PATH        = "/tmp/";
    private static final String POST_WRITE_FILE       = "test_write.jpg";
    private static final String POST_READ_FILE        = "test_read.png";
    private static final String POST_WRITE_BASE64     = "test_jpflag.jpg";
    private static final String BASE64_DATA           = "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAIBAQIBAQICAgICAgICAwUDAwMDAwYEBAMFBwYH"
      + "BwcGBwcICQsJCAgKCAcHCg0KCgsMDAwMBwkODw0MDgsMDAz/2wBDAQICAgMDAwYDAwYMCAcIDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDA"
      + "wMDAwMDAwMDAwMDAz/wAARCAALAA8DASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQR"
      + "BRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJip"
      + "KTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQF"
      + "BgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSE"
      + "lKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP0"
      + "9fb3+Pn6/9oADAMBAAIRAxEAPwD9L/8Ags7+258QP2NvhJ4X/wCEBtPsV14qvpbe48Rvai5j0jyRHIsCpIjRebODJgyZwkE21Cfnj73/AIJWftT+Lv2vf2SLDx"
      + "R400r7HrFrfTaV9vSEwxa+kKx/6aibQq5ZnjcR5TzIJNuwfu0978a+BtF+JPhm50XxFo+l6/o97t+0WGpWkd1bT7XDrvjcFWw6qwyOCoPUVb0PQ7Lwxolnpum2"
      + "drp+nafAlta2ttEsUFtEihUjRFAVVVQAFAAAAArzY4Susa8Q6r5GrcvS/wDXz6bH3lfiXJp8KUskhgIrFxqOTxF/elHXTa+zUeW/KuXmS5m2v//Z";

    // default headers
    private static final String DEFAULT_HEADER_KEY    = "X-Personium-RequestKey";
    private static final String DEFAULT_HEADER_VALUE  = "TestRequestKey";

    // headers
    private static final String HEADER_KEY            = "X-Personium-Test";
    private static final String HEADER_VALUE          = "Test";

    // parameters json keys
    private static final String KEY_SKIP_HOSTNAME_VERIFICATION = "IgnoreHostnameVerification";
    private static final String KEY_DEFAULT_HEADERS            = "DefaultHeaders";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    @BeforeClass
    public static void beforeClass() {

    }

    @Before
    public void before() throws Exception {

    }

    @After
    public void after() {

    }

    /*
     * http_get_text.
     */
    @Test
    public void http_get_text() throws ParseException {
        stubFor(get(urlEqualTo(PATH_HTTP_GET_TEXT))
                .willReturn(aResponse()
                    .withStatus(HttpStatus.SC_OK)
                    .withBody("body content")
                    .withHeader("Content-Type", "text/plain")));

        NativeObject req_headers = new NativeObject();
        req_headers.put(HEADER_KEY, req_headers, HEADER_VALUE);

        Ext_HttpClient ext_httpClient = new Ext_HttpClient();

        /**
         * ext_httpClient.get
         * String uri, NativeObject headers
         */
        NativeObject result = ext_httpClient.get(MOCK_SERVER_URL + PATH_HTTP_GET_TEXT, req_headers, false);
        String status = (String) result.get("status");
        String res_headers_str = (String) result.get("headers");
        String res_body = (String) result.get("body");

        JSONObject res_headers = (JSONObject) (new JSONParser()).parse(res_headers_str);

        assertEquals(Integer.toString(HttpStatus.SC_OK), status);
        assertEquals("text/plain", res_headers.get("Content-Type"));
        assertEquals("body content", res_body);

        verify(getRequestedFor(urlEqualTo(PATH_HTTP_GET_TEXT))
                .withHeader(HEADER_KEY, matching(HEADER_VALUE)));
    }

    /*
     * http_get_text_with_default_headers.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void http_get_text_with_default_headers() throws ParseException {
        stubFor(get(urlEqualTo(PATH_HTTP_GET_TEXT))
                .willReturn(aResponse()
                    .withStatus(HttpStatus.SC_OK)
                    .withBody("body content")
                    .withHeader("Content-Type", "text/plain")));

        NativeObject default_headers = new NativeObject();
        default_headers.put(DEFAULT_HEADER_KEY, default_headers, DEFAULT_HEADER_VALUE);
        NativeObject parameters = new NativeObject();
        parameters.put(KEY_DEFAULT_HEADERS, parameters, default_headers);

        Ext_HttpClient ext_httpClient = new Ext_HttpClient(parameters);

        NativeObject req_headers = new NativeObject();
        req_headers.put(HEADER_KEY, req_headers, HEADER_VALUE);

        /**
         * ext_httpClient.get
         * String uri, NativeObject headers
         */
        NativeObject result = ext_httpClient.get(MOCK_SERVER_URL + PATH_HTTP_GET_TEXT, req_headers, false);
        String status = (String) result.get("status");
        String res_headers_str = (String) result.get("headers");
        String res_body = (String) result.get("body");

        JSONObject res_headers = (JSONObject) (new JSONParser()).parse(res_headers_str);

        assertEquals(Integer.toString(HttpStatus.SC_OK), status);
        assertEquals("text/plain", res_headers.get("Content-Type"));
        assertEquals("body content", res_body);

        verify(getRequestedFor(urlEqualTo(PATH_HTTP_GET_TEXT))
                .withHeader(HEADER_KEY, matching(HEADER_VALUE))
                .withHeader(DEFAULT_HEADER_KEY, matching(DEFAULT_HEADER_VALUE)));
    }

    /*
     * http_get_text_with_default_headers.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void http_get_text_with_overridden_default_headers() throws ParseException {
        stubFor(get(urlEqualTo(PATH_HTTP_GET_TEXT))
                .willReturn(aResponse()
                    .withStatus(HttpStatus.SC_OK)
                    .withBody("body content")
                    .withHeader("Content-Type", "text/plain")));

        NativeObject default_headers = new NativeObject();
        default_headers.put(DEFAULT_HEADER_KEY, default_headers, DEFAULT_HEADER_VALUE);
        NativeObject parameters = new NativeObject();
        parameters.put(KEY_DEFAULT_HEADERS, parameters, default_headers);

        Ext_HttpClient ext_httpClient = new Ext_HttpClient(parameters);

        NativeObject req_headers = new NativeObject();
        req_headers.put(HEADER_KEY, req_headers, HEADER_VALUE);
        req_headers.put(DEFAULT_HEADER_KEY, req_headers,  "Overridden");

        /**
         * ext_httpClient.get
         * String uri, NativeObject headers
         */
        NativeObject result = ext_httpClient.get(MOCK_SERVER_URL + PATH_HTTP_GET_TEXT, req_headers, false);
        String status = (String) result.get("status");
        String res_headers_str = (String) result.get("headers");
        String res_body = (String) result.get("body");

        JSONObject res_headers = (JSONObject) (new JSONParser()).parse(res_headers_str);

        assertEquals(Integer.toString(HttpStatus.SC_OK), status);
        assertEquals("text/plain", res_headers.get("Content-Type"));
        assertEquals("body content", res_body);

        verify(getRequestedFor(urlEqualTo(PATH_HTTP_GET_TEXT))
                .withHeader(DEFAULT_HEADER_KEY, matching("Overridden")));
    }

    /*
     * http_get_stream.
     */
    @Test
    public void http_get_stream() {
        stubFor(get(urlEqualTo(PATH_HTTP_GET_STREAM))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(new byte[] {1, 2, 3, 4})
                    .withHeader("Content-Type", "image/jpeg")));

        NativeObject req_headers = new NativeObject();
        req_headers.put(HEADER_KEY, req_headers, HEADER_VALUE);

        Ext_HttpClient ext_httpClient = new Ext_HttpClient();

        /**
         * ext_httpClient.get
         * String uri, NativeObject headers
         */
        NativeObject result = ext_httpClient.get(MOCK_SERVER_URL + PATH_HTTP_GET_STREAM, req_headers, true);
        String status = (String) result.get("status");
        String res_headers = (String) result.get("headers");
        PersoniumInputStream res_body = (PersoniumInputStream) result.get("body");

        // stream to write file
        // Confirm the actually acquired image file.
//        InputStreamToFile(res_body, POST_FILE_PATH, POST_WRITE_FILE);

        assertEquals(Integer.toString(HttpStatus.SC_OK), status);

        verify(getRequestedFor(urlEqualTo(PATH_HTTP_GET_STREAM))
                .withHeader(HEADER_KEY, matching(HEADER_VALUE)));
    }

    /*
     * http_post_text.
     */
    @Test
    public void http_post_text() throws ParseException {
        stubFor(post(urlEqualTo(PATH_HTTP_POST_TEXT))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody("body content")
                    .withHeader("Content-Type", "text/plain")));

        NativeObject req_headers = new NativeObject();
        req_headers.put(HEADER_KEY, req_headers, HEADER_VALUE);

        Ext_HttpClient ext_httpClient = new Ext_HttpClient();

        /**
         * ext_httpClient.post text
         * String uri, String body, String contentType,
         * NativeObject headers, boolean respondsAsStream
         */
        NativeObject result = ext_httpClient.postParam(
            MOCK_SERVER_URL + PATH_HTTP_POST_TEXT, req_headers, POST_CONTENT_TYPE, POST_PARAMS_TEXT);
        String status = (String) result.get("status");
        String res_headers_str = (String) result.get("headers");
        String res_body = (String) result.get("body");


        JSONObject res_headers = (JSONObject) (new JSONParser()).parse(res_headers_str);

        assertEquals(Integer.toString(HttpStatus.SC_OK), status);
        assertEquals("text/plain", res_headers.get("Content-Type"));
        assertEquals("body content", res_body);

        verify(postRequestedFor(urlEqualTo(PATH_HTTP_POST_TEXT))
                .withHeader(HEADER_KEY, matching(HEADER_VALUE))
                .withHeader("Content-Type", matching(POST_CONTENT_TYPE)));
    }

    /*
     * http_post_text_with_default_headers.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void http_post_text_with_default_headers() throws ParseException {
        stubFor(post(urlEqualTo(PATH_HTTP_POST_TEXT))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody("body content")
                    .withHeader("Content-Type", "text/plain")));

        NativeObject default_headers = new NativeObject();
        default_headers.put(DEFAULT_HEADER_KEY, default_headers, DEFAULT_HEADER_VALUE);
        NativeObject parameters = new NativeObject();
        parameters.put(KEY_DEFAULT_HEADERS, parameters, default_headers);

        Ext_HttpClient ext_httpClient = new Ext_HttpClient(parameters);

        NativeObject req_headers = new NativeObject();
        req_headers.put(HEADER_KEY, req_headers, HEADER_VALUE);

        /**
         * ext_httpClient.post text
         * String uri, String body, String contentType,
         * NativeObject headers, boolean respondsAsStream
         */
        NativeObject result = ext_httpClient.postParam(
            MOCK_SERVER_URL + PATH_HTTP_POST_TEXT, req_headers, POST_CONTENT_TYPE, POST_PARAMS_TEXT);
        String status = (String) result.get("status");
        String res_headers_str = (String) result.get("headers");
        String res_body = (String) result.get("body");


        JSONObject res_headers = (JSONObject) (new JSONParser()).parse(res_headers_str);

        assertEquals(Integer.toString(HttpStatus.SC_OK), status);
        assertEquals("text/plain", res_headers.get("Content-Type"));
        assertEquals("body content", res_body);

        verify(postRequestedFor(urlEqualTo(PATH_HTTP_POST_TEXT))
                .withHeader(HEADER_KEY, matching(HEADER_VALUE))
                .withHeader(DEFAULT_HEADER_KEY, matching(DEFAULT_HEADER_VALUE))
                .withHeader("Content-Type", matching(POST_CONTENT_TYPE)));
    }

    /*
     * http_post_text_with_content_type_default_headers.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void http_post_text_with_content_type_default_headers() throws ParseException {
        stubFor(post(urlEqualTo(PATH_HTTP_POST_TEXT))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody("body content")
                    .withHeader("Content-Type", "text/plain")));

        NativeObject default_headers = new NativeObject();
        default_headers.put("Content-Type", default_headers, "application/json");
        NativeObject parameters = new NativeObject();
        parameters.put(KEY_DEFAULT_HEADERS, parameters, default_headers);

        Ext_HttpClient ext_httpClient = new Ext_HttpClient(parameters);

        NativeObject req_headers = new NativeObject();
        req_headers.put(HEADER_KEY, req_headers, HEADER_VALUE);

        /**
         * ext_httpClient.post text
         * String uri, String body, String contentType,
         * NativeObject headers, boolean respondsAsStream
         */
        NativeObject result = ext_httpClient.postParam(
            MOCK_SERVER_URL + PATH_HTTP_POST_TEXT, req_headers, POST_CONTENT_TYPE, POST_PARAMS_TEXT);
        String status = (String) result.get("status");
        String res_headers_str = (String) result.get("headers");
        String res_body = (String) result.get("body");


        JSONObject res_headers = (JSONObject) (new JSONParser()).parse(res_headers_str);

        assertEquals(Integer.toString(HttpStatus.SC_OK), status);
        assertEquals("text/plain", res_headers.get("Content-Type"));
        assertEquals("body content", res_body);

        verify(postRequestedFor(urlEqualTo(PATH_HTTP_POST_TEXT))
                .withHeader(HEADER_KEY, matching(HEADER_VALUE))
                .withHeader("Content-Type", matching(POST_CONTENT_TYPE)));
    }

    /*
     * http_post_stream.
     */
    @Test
    public void http_post_stream() {
        stubFor(post(urlEqualTo(PATH_HTTP_POST_STREAM))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody("body content")
                .withHeader("Content-Type", "text/plain")));

        NativeObject req_headers = new NativeObject();
        req_headers.put(HEADER_KEY, req_headers, HEADER_VALUE);

        NativeObject default_headers = new NativeObject();
        default_headers.put("Content-Type", default_headers, "application/json");
        NativeObject parameters = new NativeObject();
        parameters.put(KEY_DEFAULT_HEADERS, parameters, default_headers);

        Ext_HttpClient ext_httpClient = new Ext_HttpClient(parameters);

        InputStream is = null;
        String postFilePath = ClassLoader.getSystemResource(POST_READ_FILE).getPath();
        // For Test File operation.
        try {
            is = FileToInputStream(postFilePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        InputStreamToFile(is, POST_FILE_PATH, POST_WRITE_BASE64);
        String base64_str = FileToBase64(postFilePath);

        InputStream _is = Base64ToInputStream(BASE64_DATA);
        PersoniumInputStream pis = new PersoniumInputStream((InputStream) _is);

        /**
         * ext_httpClient.post stream
         * String uri, String body, String contentType,
         * NativeObject headers, boolean respondsAsStream
         */
        NativeObject result = ext_httpClient.postStream(
            MOCK_SERVER_URL + PATH_HTTP_POST_STREAM, req_headers, POST_CONTENT_TYPE, pis, "image.jpg");

        String status = (String)result.get("status");
        String res_headers = (String)result.get("headers");
        String res_body = (String)result.get("body");

        assertEquals(status, Integer.toString(HttpStatus.SC_OK));
    }

    /*
     * conversion InputStream to File.
     */
    private void InputStreamToFile(InputStream is, String path, String name) {
        int BYTESIZE = 1024;
        FileOutputStream out = null;
        try {
            File file = new File(path, name);
            out = new FileOutputStream(file, false);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

        try {
            byte[] buffer = new byte[BYTESIZE];
            int len = 0;
            while (true) {
                if ((len = is.read(buffer)) == -1) {
                    throw new EOFException();
                }
                out.write(buffer, 0, len);
                if (len < BYTESIZE) break;
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * conversion File to Base64.
     */
    @SuppressWarnings("resource")
    private String FileToBase64(String file) {
        int BYTESIZE = 1024;
        FileInputStream fs = null;
        try {
            fs = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        byte[] buffer = new byte[BYTESIZE];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            int len = 0;
            while (true) {
                if ((len = fs.read(buffer)) == -1) {
                    throw new EOFException();
                }
                out.write(buffer, 0, len);
                if (len < BYTESIZE) break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(Base64.encodeBase64(out.toByteArray()));
    }

    /*
     * conversion File to InputStream.
     */
    private InputStream FileToInputStream(String file) throws FileNotFoundException {
        return (InputStream) new BufferedInputStream(new FileInputStream(file));
    }

    /*
     * conversion Base64 to InputStream
     */
    private InputStream Base64ToInputStream(String str) {
        return new ByteArrayInputStream(Base64.decodeBase64(str));
    }
}
