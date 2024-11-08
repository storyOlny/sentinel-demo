package com.jyz.restTemplate;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;

/**
 * restTemplate自定义拦截器，为了增加sentinel的执行策略
 */
public class MyRestTemplateInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        entryInstance(request, body, execution);
        return execution.execute(request, body);
    }

    private ClientHttpResponse entryInstance(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        Entry entry = null;
        try {
            URI uri = request.getURI();
            String ip = uri.getHost();
            String port = String.valueOf(uri.getPort());
            String origin = ip + "-" + port;
            entry = SphU.entry(origin, EntryType.IN);
            return execution.execute(request, body);
        } catch (BlockException e) {
            throw new RuntimeException("flow error.");
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }
}
