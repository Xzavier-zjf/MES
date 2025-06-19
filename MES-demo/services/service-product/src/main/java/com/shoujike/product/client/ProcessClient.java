package com.shoujike.product.client;

import com.alibaba.nacos.api.remote.response.Response;
import com.shoujike.common.InjectionParamDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "service-order")
public interface ProcessClient {
    @PostMapping("/injection-params")
    Response createInjectionParam(@RequestBody InjectionParamDTO param);
}