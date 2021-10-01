package by.gdev.http.head.cache;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HeaderElementIterator;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.Args;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import by.gdev.http.head.cache.impl.FileServiceImpl;
import by.gdev.http.head.cache.impl.GsonServiceImpl;
import by.gdev.http.head.cache.impl.HttpServiceImpl;
import by.gdev.http.head.cache.service.FileService;
import by.gdev.http.head.cache.service.GsonService;
import by.gdev.http.head.cache.service.HttpService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
	
	GsonService gsonService;
	
	@Before
	public void init() {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setDefaultMaxPerRoute(5);
        cm.setMaxTotal(20);
        CloseableHttpClient builder = HttpClients.custom().setKeepAliveStrategy((response, context) -> {
                    Args.notNull(response, "HTTP response");
                    final HeaderElementIterator it = new BasicHeaderElementIterator(
                            response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                    if (it.hasNext()) {
                        log.info("used keep alive 5000");
                        return 5000L;
                    }
                    return -1;
                }).setConnectionManager(cm).evictIdleConnections(10, TimeUnit.SECONDS).build();
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(604800).setSocketTimeout(604800).build();
		Gson GSON = new Gson();
		HttpService httpService = new HttpServiceImpl(builder, requestConfig);
		FileService fileService = new FileServiceImpl(httpService, GSON, StandardCharsets.UTF_8, Paths.get("target"), 604800);
		gsonService = new GsonServiceImpl(GSON, fileService);
	}
	
	@Test
	public void main() throws JsonSyntaxException, IOException, NoSuchAlgorithmException {
		MyTestType myTest = gsonService.getObject("https://gdev.by/repo/test.json", MyTestType.class, false);
		System.out.println(myTest);
	}
}