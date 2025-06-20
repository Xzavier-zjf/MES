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
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.shoujike.process.service.InjectionParamService;
import com.shoujike.process.service.PrintPatternService;

import java.util.List;

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
    public ResponseEntity<InjectionParamDTO> createInjectionParam(
            @Valid @RequestBody InjectionParamCreateDTO createDTO) throws BusinessException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(injectionParamService.createInjectionParam(createDTO));
    }

    @PutMapping("/injection-params/{id}")
    public ResponseEntity<InjectionParamDTO> updateInjectionParam(
            @PathVariable Integer id,
            @Valid @RequestBody InjectionParamCreateDTO updateDTO) throws EntityNotFoundException {
        return ResponseEntity.ok(injectionParamService.updateInjectionParam(id, updateDTO));
    }

    @GetMapping("/injection-params/{id}")
    public ResponseEntity<InjectionParamDTO> getInjectionParamById(@PathVariable Integer id) throws EntityNotFoundException {
        return ResponseEntity.ok(injectionParamService.getInjectionParamById(id));
    }

    @GetMapping("/injection-params/task/{taskId}")
    public ResponseEntity<InjectionParamDTO> getInjectionParamByTaskId(@PathVariable Integer taskId) throws EntityNotFoundException {
        return ResponseEntity.ok(injectionParamService.getInjectionParamByTaskId(taskId));
    }

    @DeleteMapping("/injection-params/{id}")
    public ResponseEntity<Void> deleteInjectionParam(@PathVariable Integer id) throws EntityNotFoundException {
        injectionParamService.deleteInjectionParam(id);
        return ResponseEntity.noContent().build();
    }

    // ================ 印刷图案管理 ================
    @PostMapping("/print-patterns")
    public ResponseEntity<PrintPatternDTO> createPrintPattern(
            @Valid @RequestBody PrintPatternCreateDTO createDTO) throws BusinessException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(printPatternService.createPrintPattern(createDTO));
    }

    @PutMapping("/print-patterns/{id}")
    public ResponseEntity<PrintPatternDTO> updatePrintPattern(
            @PathVariable Integer id,
            @Valid @RequestBody PrintPatternCreateDTO updateDTO) throws BusinessException, EntityNotFoundException {
        return ResponseEntity.ok(printPatternService.updatePrintPattern(id, updateDTO));
    }

    @GetMapping("/print-patterns/{id}")
    public ResponseEntity<PrintPatternDTO> getPrintPatternById(@PathVariable Integer id) throws EntityNotFoundException {
        return ResponseEntity.ok(printPatternService.getPrintPatternById(id));
    }

    @GetMapping("/print-patterns/code/{patternCode}")
    public ResponseEntity<PrintPatternDTO> getPrintPatternByCode(@PathVariable String patternCode) throws EntityNotFoundException {
        return ResponseEntity.ok(printPatternService.getPrintPatternByCode(patternCode));
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
    public ResponseEntity<Void> deletePrintPattern(@PathVariable Integer id) throws EntityNotFoundException {
        printPatternService.deletePrintPattern(id);
        return ResponseEntity.noContent().build();
    }
}