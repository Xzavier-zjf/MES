package com.shoujike.common.client;

import com.shoujike.common.dto.DeviceDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "equipment-com.shoujike.process.service")
public interface EquipmentClient {
    @GetMapping("/api/v1/equipment/devices/{id}")
    ResponseEntity<DeviceDTO> getDeviceById(@PathVariable("id") Integer id);
}