package com.shoujike.process.controller;

import com.shoujike.common.exception.BusinessException;
import com.shoujike.common.exception.EntityNotFoundException;
import jakarta.validation.Valid;
import com.shoujike.process.model.dto.InjectionParamCreateDTO;
import com.shoujike.process.model.dto.InjectionParamDTO;
import com.shoujike.process.model.dto.PrintPatternCreateDTO;
import com.shoujike.process.model.dto.PrintPatternDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.shoujike.process.service.InjectionParamService;
import com.shoujike.process.service.PrintPatternService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/process")
@Validated
public class ProcessController {

    private final InjectionParamService injectionParamService;
    private final PrintPatternService printPatternService;

    @Autowired
    public ProcessController(InjectionParamService injectionParamService,
            PrintPatternService printPatternService) {
        this.injectionParamService = injectionParamService;
        this.printPatternService = printPatternService;
    }

    // ================ 注塑工艺参数管理 ================
    @PostMapping("/injection-params")
    public ResponseEntity<?> createInjectionParam(
            @Valid @RequestBody InjectionParamCreateDTO createDTO) {
        try {
            InjectionParamDTO dto = injectionParamService.createInjectionParam(createDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (BusinessException ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage() != null ? ex.getMessage() : "业务异常"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "服务器内部错误"));
        }
    }

    @GetMapping("/injection-params/all")
    public ResponseEntity<Page<InjectionParamDTO>> getAllInjectionParams(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(injectionParamService.getAllInjectionParams(pageable));
    }

    @PutMapping("/injection-params/{id}")
    public ResponseEntity<?> updateInjectionParam(
            @PathVariable Integer id,
            @Valid @RequestBody InjectionParamCreateDTO updateDTO) {
        try {
            InjectionParamDTO result = injectionParamService.updateInjectionParam(id, updateDTO);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", ex.getMessage() != null ? ex.getMessage() : "资源不存在"));
        } catch (BusinessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage() != null ? ex.getMessage() : "业务异常"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "服务器内部错误"));
        }
    }

    @GetMapping("/injection-params/{id}")
    public ResponseEntity<?> getInjectionParamById(@PathVariable Integer id) {
        try {
            InjectionParamDTO result = injectionParamService.getInjectionParamById(id);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", ex.getMessage() != null ? ex.getMessage() : "资源不存在"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "服务器内部错误"));
        }
    }

    @GetMapping("/injection-params/task/{taskId}")
    public ResponseEntity<?> getInjectionParamByTaskId(@PathVariable String taskId) {
        try {
            InjectionParamDTO result = injectionParamService.getInjectionParamByTaskId(taskId);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", ex.getMessage() != null ? ex.getMessage() : "资源不存在"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "服务器内部错误"));
        }
    }

    @DeleteMapping("/injection-params/{id}")
    public ResponseEntity<?> deleteInjectionParam(@PathVariable Integer id) {
        try {
            injectionParamService.deleteInjectionParam(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", ex.getMessage() != null ? ex.getMessage() : "资源不存在"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "服务器内部错误"));
        }
    }

    // ================ 印刷图案管理 ================
    @PostMapping("/print-patterns")
    public ResponseEntity<?> createPrintPattern(
            @Valid @RequestBody PrintPatternCreateDTO createDTO) { // 修改参数名称匹配前端

        try {

            PrintPatternDTO result = printPatternService.createPrintPattern(createDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage(),
                    "timestamp", System.currentTimeMillis()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "文件上传失败: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()));
        }
    }

    // 处理multipart/form-data格式的更新请求（带文件上传）
    @PutMapping(value = "/print-patterns/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updatePrintPatternWithFile(
            @PathVariable Integer id,
            @Valid @ModelAttribute PrintPatternCreateDTO updateDTO,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {

        try {
            // 处理文件更新（可选）
            if (imageFile != null && !imageFile.isEmpty()) {
                String newImageUrl = printPatternService.storeImage(imageFile);
                updateDTO.setImageUrl(newImageUrl);
            } else {
                // 当未上传新文件时，确保使用现有图片URL
                PrintPatternDTO existing = printPatternService.getPrintPatternById(id);
                if (existing.getImageUrl() == null) {
                    throw new BusinessException("无法更新：原记录未包含图片，必须上传新图片");
                }
                updateDTO.setImageUrl(existing.getImageUrl());
            }

            PrintPatternDTO result = printPatternService.updatePrintPattern(id, updateDTO);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", ex.getMessage() != null ? ex.getMessage() : "资源不存在"));
        } catch (BusinessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage() != null ? ex.getMessage() : "业务异常"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "服务器内部错误"));
        }
    }

    // 处理application/json格式的更新请求（不带文件上传）
    @PutMapping(value = "/print-patterns/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updatePrintPattern(
            @PathVariable Integer id,
            @Valid @RequestBody PrintPatternCreateDTO updateDTO) {

        try {
            System.out.println("收到JSON格式的更新请求，ID: " + id);
            System.out.println("更新数据: " + updateDTO);
            
            // 对于JSON请求，直接使用传递的imageUrl，不做额外处理
            // 这样前端可以通过imageUrl字段更新图片链接
            
            PrintPatternDTO result = printPatternService.updatePrintPattern(id, updateDTO);
            System.out.println("更新结果: " + result);
            
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException ex) {
            System.err.println("实体未找到: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", ex.getMessage() != null ? ex.getMessage() : "资源不存在"));
        } catch (BusinessException ex) {
            System.err.println("业务异常: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage() != null ? ex.getMessage() : "业务异常"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "服务器内部错误: " + ex.getMessage()));
        }
    }

    @GetMapping("/print-patterns/{id}")
    public ResponseEntity<?> getPrintPatternById(@PathVariable Integer id) {
        try {
            PrintPatternDTO result = printPatternService.getPrintPatternById(id);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", ex.getMessage() != null ? ex.getMessage() : "资源不存在"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "服务器内部错误"));
        }
    }

    @GetMapping("/print-patterns/code/{patternCode}")
    public ResponseEntity<?> getPrintPatternByCode(@PathVariable String patternCode) {
        try {
            PrintPatternDTO result = printPatternService.getPrintPatternByCode(patternCode);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", ex.getMessage() != null ? ex.getMessage() : "资源不存在"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "服务器内部错误"));
        }
    }

    @GetMapping("/print-patterns")
    public ResponseEntity<List<PrintPatternDTO>> getPrintPatternsByMachineModel(
            @RequestParam(required = false) String machineModel) {
        if (machineModel != null) {
            return ResponseEntity.ok(printPatternService.getPrintPatternsByMachineModel(machineModel));
        }
        // 返回所有图案（第一页）
        Page<PrintPatternDTO> page = printPatternService.getAllPrintPatterns(PageRequest.of(0, 20));
        return ResponseEntity.ok(page.getContent());
    }

    @GetMapping("/print-patterns/all")
    public ResponseEntity<Page<PrintPatternDTO>> getAllPrintPatterns(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(printPatternService.getAllPrintPatterns(pageable));
    }

    @DeleteMapping("/print-patterns/{id}")
    public ResponseEntity<?> deletePrintPattern(@PathVariable Integer id) {
        try {
            printPatternService.deletePrintPattern(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", ex.getMessage() != null ? ex.getMessage() : "资源不存在"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "服务器内部错误"));
        }
    }
}