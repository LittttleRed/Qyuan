package org.buaa.buaa_astro_architect.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class FeignRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        Map<String, String> headers = getHeaders();
        headers.forEach(requestTemplate::header);
    }

    private Map<String, String> getHeaders() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            Map<String, String> map = new LinkedHashMap<>();
            Enumeration<String> headerNames = attributes.getRequest().getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String key = headerNames.nextElement();
                String value = attributes.getRequest().getHeader(key);
                if (key.equalsIgnoreCase("content-length")) {
                    continue;
                }
                map.put(key, value);
            }
            return map;
        } else {
            return new HashMap<>();
        }
    }
}
