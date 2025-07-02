package com.shoujike.process.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoujike.common.config.FileStorageConfig;
import com.shoujike.common.exception.BusinessException;
import com.shoujike.common.exception.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import com.shoujike.process.mapper.PrintPatternMapper;
import com.shoujike.process.model.dto.PrintPatternCreateDTO;
import com.shoujike.process.model.dto.PrintPatternDTO;
import com.shoujike.process.model.entity.PrintPattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.shoujike.process.service.PrintPatternService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrintPatternServiceImpl implements PrintPatternService {

    private final PrintPatternMapper printPatternMapper;
    private final ObjectMapper objectMapper;
    private final FileStorageConfig fileStorageConfig;
    @Override
    @Transactional
    public PrintPatternDTO createPrintPattern(PrintPatternCreateDTO createDTO) throws BusinessException {
        // 检查图案编号是否唯一
        if (printPatternMapper.findByPatternCode(createDTO.getPatternCode()) != null) {
            throw new BusinessException("图案编号已存在");
        }

        PrintPattern pattern = new PrintPattern();
        pattern.setPlanId(createDTO.getPlanId());
        pattern.setTaskId(createDTO.getTaskId());
        pattern.setDeviceId(createDTO.getDeviceId());
        pattern.setPatternCode(createDTO.getPatternCode());
        pattern.setPatternName(createDTO.getPatternName());
        pattern.setMachineModel(createDTO.getMachineModel());
        pattern.setImageUrl(createDTO.getImageUrl());
        pattern.setDefaultPrintSpeed(createDTO.getDefaultPrintSpeed());
        pattern.setDefaultPressure(createDTO.getDefaultPressure());

        printPatternMapper.insert(pattern);
        return convertToDTO(pattern);
    }

    @Override
    @Transactional
    public PrintPatternDTO updatePrintPattern(Integer id, PrintPatternCreateDTO updateDTO) throws EntityNotFoundException, BusinessException {
        PrintPattern pattern = printPatternMapper.selectById(id);
        if (pattern == null) {
            throw new EntityNotFoundException("印刷图案不存在");
        }

        // 新增：如果更新了图片，删除旧图片
        if (updateDTO.getImageUrl() != null && !updateDTO.getImageUrl().equals(pattern.getImageUrl())) {
            deleteOldImage(pattern.getImageUrl()); // 需要实现旧图片删除逻辑
        }

        // 保持原有验证逻辑不变
        if (updateDTO.getPatternCode() != null) {
            // 使用安全比较方式
            boolean codeChanged = !updateDTO.getPatternCode().equals(pattern.getPatternCode());
            // 原记录patternCode可能为null时，使用Objects.equals更安全
            boolean codeExists = printPatternMapper.findByPatternCode(updateDTO.getPatternCode()) != null;

            if (codeChanged && codeExists) {
                throw new BusinessException("新图案编号已存在");
            }
        }

        // 更新所有字段（包含imageUrl）
        pattern.setImageUrl(updateDTO.getImageUrl());
        pattern.setPatternCode(updateDTO.getPatternCode());
        pattern.setPatternName(updateDTO.getPatternName());
        pattern.setMachineModel(updateDTO.getMachineModel());
        pattern.setDefaultPrintSpeed(updateDTO.getDefaultPrintSpeed());
        pattern.setDefaultPressure(updateDTO.getDefaultPressure());

        printPatternMapper.updateById(pattern);
        return convertToDTO(pattern);
    }

    // 新增旧图片删除方法
    private void deleteOldImage(String imageUrl) throws BusinessException {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return; // 跳过空值的URL处理
        }
        try {
            Path oldImagePath = Paths.get(fileStorageConfig.getDir() + imageUrl.replace("/uploads/patterns/", ""));
            Files.deleteIfExists(oldImagePath);
        } catch (IOException e) {
            throw new BusinessException("旧图片删除失败: " + e.getMessage());
        }
    }

    @Override
    public PrintPatternDTO getPrintPatternById(Integer id) throws EntityNotFoundException {
        PrintPattern pattern = printPatternMapper.selectById(id);
        if (pattern == null) {
            throw new EntityNotFoundException("印刷图案不存在");
        }
        return convertToDTO(pattern);
    }

    @Override
    public PrintPatternDTO getPrintPatternByCode(String patternCode) throws EntityNotFoundException {
        PrintPattern pattern = printPatternMapper.findByPatternCode(patternCode);
        if (pattern == null) {
            throw new EntityNotFoundException("印刷图案不存在");
        }
        return convertToDTO(pattern);
    }

    @Override
    public List<PrintPatternDTO> getPrintPatternsByMachineModel(String machineModel) {
        List<PrintPattern> patterns = printPatternMapper.findByMachineModel(machineModel);
        return patterns.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<PrintPatternDTO> getAllPrintPatterns(Pageable pageable) {
        // 处理分页转换（MP页码从1开始）
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<PrintPattern> mpPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                        pageable.getPageNumber() + 1,
                        pageable.getPageSize()
                );

        QueryWrapper<PrintPattern> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("id");

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<PrintPattern> mpResult =
                printPatternMapper.selectPage(mpPage, wrapper);

        List<PrintPatternDTO> dtoList = mpResult.getRecords()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(
                dtoList,
                PageRequest.of((int)mpResult.getCurrent() - 1, (int)mpResult.getSize()),
                mpResult.getTotal()
        );
    }

    @Override
    @Transactional
    public void deletePrintPattern(Integer id) throws EntityNotFoundException {
        if (printPatternMapper.deleteById(id) == 0) {
            throw new EntityNotFoundException("印刷图案不存在");
        }
    }

    private PrintPatternDTO convertToDTO(PrintPattern pattern) {
        return objectMapper.convertValue(pattern, PrintPatternDTO.class);
    }

    public String storeImage(MultipartFile file) throws BusinessException {
        String uploadDir = fileStorageConfig.getDir();
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + fileName);

            Files.createDirectories(filePath.getParent());
            file.transferTo(filePath);

            return "/uploads/patterns/" + fileName;  // 返回访问路径
        } catch (IOException e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

}
