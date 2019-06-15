package com.kirin.plugins.notify.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.springframework.util.CollectionUtils;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.Map.Entry;

public class HttpClientUtils {
    private static final Log logger = LogFactory.getLog(HttpClientUtils.class);
    private static int HTTPCLIENT_CONNECT_TIMEOUT = 60000;
    private static int HTTPCLIENT_RESPONSE_TIMEOUT = 60000;

    public HttpClientUtils() {
    }

    public static String getResult(String url, String userName, String password) throws Exception {
        CloseableHttpClient client = createSSLInsecureClient();
        HttpGet httpGet = new HttpGet(url);

        String var9;
        try {
            setAuth(userName, password, url, client);
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(HTTPCLIENT_RESPONSE_TIMEOUT).setConnectTimeout(HTTPCLIENT_CONNECT_TIMEOUT).build();
            httpGet.setConfig(requestConfig);
            HttpResponse response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200 && statusCode != 302) {
                throw new Exception(statusCode + " Error");
            }

            HttpEntity httpEntity = response.getEntity();
            var9 = EntityUtils.toString(httpEntity, "utf-8");
        } catch (Exception var18) {
            throw new Exception(var18);
        } finally {
            try {
                client.close();
            } catch (Exception var17) {
                throw new Exception(var17);
            }
        }

        return var9;
    }

    public static String postRequest(String url, String userName, String password) throws Exception {
        CloseableHttpClient client = createSSLInsecureClient();
        setAuth(userName, password, url, client);
        HttpPost httpPost = new HttpPost(url);

        String var10;
        try {
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(HTTPCLIENT_RESPONSE_TIMEOUT).setConnectTimeout(HTTPCLIENT_CONNECT_TIMEOUT).build();
            httpPost.setConfig(requestConfig);
            HttpResponse response = client.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200 && statusCode != 302) {
                throw new Exception(statusCode + " Error");
            }

            HttpEntity httpEntity = response.getEntity();
            var10 = EntityUtils.toString(httpEntity, "utf-8");
        } catch (Exception var19) {
            throw new Exception(var19);
        } finally {
            try {
                client.close();
            } catch (Exception var18) {
                throw new Exception(var18);
            }
        }

        return var10;
    }

    public static String postRequest(String url, Map<String, String> params, String userName, String password) throws Exception {
        CloseableHttpClient client = createSSLInsecureClient();
        setAuth(userName, password, url, client);
        HttpPost httpPost = new HttpPost(url);
        Set<Entry<String, String>> esParams = params.entrySet();
        List<NameValuePair> httpParams = new ArrayList();
        Iterator var8 = esParams.iterator();

        while(var8.hasNext()) {
            Entry<String, String> esParam = (Entry)var8.next();
            httpParams.add(new BasicNameValuePair((String)esParam.getKey(), (String)esParam.getValue()));
        }

        httpPost.setEntity(new UrlEncodedFormEntity(httpParams, "utf-8"));

        String var13;
        try {
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(HTTPCLIENT_RESPONSE_TIMEOUT).setConnectTimeout(HTTPCLIENT_CONNECT_TIMEOUT).build();
            httpPost.setConfig(requestConfig);
            HttpResponse response = client.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200 && statusCode != 302) {
                throw new Exception(statusCode + " Error");
            }

            HttpEntity httpEntity = response.getEntity();
            var13 = EntityUtils.toString(httpEntity, "utf-8");
        } catch (Exception var22) {
            throw new Exception(var22);
        } finally {
            try {
                client.close();
            } catch (Exception var21) {
                throw new Exception(var21);
            }
        }

        return var13;
    }

    public static String postXml(String url, String value, String userName, String password) throws Exception {
        HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager());
        HttpConnectionManagerParams managerParams = client.getHttpConnectionManager().getParams();
        managerParams.setConnectionTimeout(HTTPCLIENT_CONNECT_TIMEOUT);
        managerParams.setSoTimeout(HTTPCLIENT_RESPONSE_TIMEOUT);
        setAuth(userName, password, url, client);
        PostMethod method = new PostMethod(url);
        method.setRequestEntity(new StringRequestEntity(value, "text/xml", "UTF-8"));

        String var9;
        try {
            int statusCode = client.executeMethod(method);
            if (statusCode != 200 && statusCode != 302) {
                throw new Exception(statusCode + " Error");
            }

            byte[] resultBytes = method.getResponseBody();
            var9 = new String(resultBytes, "utf-8");
        } catch (Exception var13) {
            throw new Exception(var13);
        } finally {
            method.releaseConnection();
        }

        return var9;
    }

    private static void setAuth(String username, String password, String url, HttpClient httpClient) throws Exception {
        if (username != null) {
            httpClient.getParams().setAuthenticationPreemptive(true);
            Credentials defaultcreds = new UsernamePasswordCredentials(username, password);
            URL tUrl = new URL(url);
            httpClient.getState().setCredentials(new AuthScope(tUrl.getHost(), tUrl.getPort()), defaultcreds);
        }

    }

    private static void setAuth(String username, String password, String url, CloseableHttpClient httpClient) throws Exception {
    }

    public static String postRequest(String urlStr, Map<String, String> params) {
        CloseableHttpClient client = createSSLInsecureClient();
        Set<Entry<String, String>> esParams = params.entrySet();
        String httpParams = "";

        try {
            Entry esParam;
            String value;
            for(Iterator var30 = esParams.iterator(); var30.hasNext(); httpParams = httpParams + (String)esParam.getKey() + "=" + value + "&") {
                esParam = (Entry)var30.next();
                value = (String)esParam.getValue();
                if (StringUtils.isNotBlank(value)) {
                    value = URLEncoder.encode((String)esParam.getValue(), "utf-8");
                }
            }

            if (httpParams.endsWith("&")) {
                httpParams = httpParams.substring(0, httpParams.length() - 1);
                urlStr = urlStr + "?" + httpParams;
            }

            HttpPost httpPost = new HttpPost(urlStr);
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(HTTPCLIENT_RESPONSE_TIMEOUT).setConnectTimeout(HTTPCLIENT_CONNECT_TIMEOUT).build();
            httpPost.setConfig(requestConfig);
            HttpResponse response = client.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200 && statusCode != 302) {
                throw new Exception(statusCode + " Error");
            }

            HttpEntity httpEntity = response.getEntity();
            String var10 = EntityUtils.toString(httpEntity, "utf-8");
            return var10;
        } catch (Exception var28) {
            Exception e = var28;

            try {
                throw new Exception(e);
            } catch (Exception var27) {
                var27.printStackTrace();
            }
        } finally {
            try {
                client.close();
            } catch (Exception var26) {
                Exception e = var26;

                try {
                    throw new Exception(e);
                } catch (Exception var25) {
                    var25.printStackTrace();
                }
            }

        }

        return null;
    }

    public static String postRequestByForm(String urlStr, Map<String, String> params) {
        CloseableHttpClient httpClient = createSSLInsecureClient();
        CloseableHttpResponse response = null;

        String var9;
        try {
            HttpPost httpPost = new HttpPost(urlStr);
            List<NameValuePair> list = new ArrayList();
            Iterator var6 = params.entrySet().iterator();

            while(var6.hasNext()) {
                Entry<String, String> entry = (Entry)var6.next();
                list.add(new BasicNameValuePair((String)entry.getKey(), (String)entry.getValue()));
            }

            RequestConfig config = RequestConfig.custom().setConnectTimeout(60000).setSocketTimeout(15000).build();
            httpPost.setConfig(config);
            httpPost.setEntity(new UrlEncodedFormEntity(list));
            response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                httpPost.abort();
                throw new RuntimeException("HttpClient,error status code :" + statusCode);
            }

            HttpEntity entity = response.getEntity();
            if (entity == null) {
                EntityUtils.consume(entity);
                return null;
            }

            var9 = EntityUtils.toString(entity, "UTF-8");
        } catch (Exception var23) {
            throw new RuntimeException(var23);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException var22) {
                    throw new RuntimeException(var22);
                }
            }

            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException var21) {
                    throw new RuntimeException(var21);
                }
            }

        }

        return var9;
    }

    public static String postFileData(String urlStr, Map<String, String> params, File file, String fileName) {
        HttpClient client = new HttpClient();
        PostMethod webPage = null;

        String var17;
        try {
            client.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
            client.getHttpConnectionManager().getParams().setSoTimeout(30000);
            URL url = new URL(urlStr);
            client.getHostConfiguration().setHost(url.getHost(), url.getPort(), url.getProtocol());
            webPage = new PostMethod(url.getPath());
            org.apache.commons.httpclient.NameValuePair[] reqParams = null;
            int statusCode;
            if (CollectionUtils.isEmpty(params)) {
                reqParams = new org.apache.commons.httpclient.NameValuePair[0];
            } else {
                reqParams = new org.apache.commons.httpclient.NameValuePair[params.size()];
                Iterator<Entry<String, String>> entries = params.entrySet().iterator();

                Entry entry;
                for(statusCode = 0; entries.hasNext(); reqParams[statusCode++] = new org.apache.commons.httpclient.NameValuePair((String)entry.getKey(), (String)entry.getValue())) {
                    entry = (Entry)entries.next();
                }
            }

            webPage.setQueryString(reqParams);
            if (StringUtils.isBlank(fileName)) {
                fileName = "warName";
            }

            Part[] parts = new Part[]{new FilePart(fileName, file)};
            webPage.setRequestEntity(new MultipartRequestEntity(parts, webPage.getParams()));
            statusCode = client.executeMethod(webPage);
            if (200 != statusCode) {
                return null;
            }

            var17 = webPage.getResponseBodyAsString();
        } catch (Exception var14) {
            logger.error(var14.getMessage(), var14);
            return null;
        } finally {
            if (null != webPage) {
                webPage.releaseConnection();
            }

        }

        return var17;
    }

    public static String postFileData(String urlStr, Map<String, String> params, File file) {
        return postFileData(urlStr, params, file, (String)null);
    }

    public static String getResultNoAuth(String url) throws Exception {
        CloseableHttpClient client = createSSLInsecureClient();
        HttpGet httpGet = new HttpGet(url);

        String var7;
        try {
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(HTTPCLIENT_RESPONSE_TIMEOUT).setConnectTimeout(HTTPCLIENT_CONNECT_TIMEOUT).build();
            httpGet.setConfig(requestConfig);
            HttpResponse response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200 && statusCode != 302) {
                throw new Exception(statusCode + " Error");
            }

            HttpEntity httpEntity = response.getEntity();
            var7 = EntityUtils.toString(httpEntity, "utf-8");
        } catch (Exception var16) {
            throw new Exception(var16);
        } finally {
            try {
                client.close();
            } catch (Exception var15) {
                throw new Exception(var15);
            }
        }

        return var7;
    }

    public static String postJsonRequestWithToken(String url, JSONObject json, String token) throws Exception {
        HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager());
        HttpConnectionManagerParams managerParams = client.getHttpConnectionManager().getParams();
        managerParams.setConnectionTimeout(HTTPCLIENT_CONNECT_TIMEOUT);
        managerParams.setSoTimeout(HTTPCLIENT_RESPONSE_TIMEOUT);
        PostMethod method = new PostMethod(url);
        method.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
        method.setRequestHeader("Authorization", "Bearer "+token);
        RequestEntity entity = new StringRequestEntity(json.toString(), "text/xml", "utf-8");
        method.setRequestEntity(entity);
        Object var6 = null;

        String var7;
        try {
            client.executeMethod(method);
            byte[] resultBytes = method.getResponseBody();
            var7 = new String(resultBytes, "utf-8");
        } catch (Exception var11) {
            throw new Exception(var11);
        } finally {
            method.releaseConnection();
        }

        return var7;
    }

    public static CloseableHttpClient createSSLInsecureClient() {
        try {
            SSLContext sslContext = (new SSLContextBuilder()).loadTrustMaterial((KeyStore)null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    return true;
                }
            }).build();
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
            return HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build();
        } catch (KeyManagementException var2) {
            var2.printStackTrace();
        } catch (NoSuchAlgorithmException var3) {
            var3.printStackTrace();
        } catch (KeyStoreException var4) {
            var4.printStackTrace();
        }

        return HttpClients.createDefault();
    }
}
