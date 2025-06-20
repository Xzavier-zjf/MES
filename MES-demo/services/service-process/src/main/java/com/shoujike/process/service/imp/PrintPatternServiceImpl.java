package com.shoujike.process.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PrintPatternServiceImpl implements PrintPatternService {

    private final PrintPatternMapper printPatternMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public PrintPatternDTO createPrintPattern(PrintPatternCreateDTO createDTO) throws BusinessException {
        // 检查图案编号是否唯一
        if (printPatternMapper.findByPatternCode(createDTO.getPatternCode()) != null) {
            throw new BusinessException("图案编号已存在");
        }

        PrintPattern pattern = new PrintPattern();
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

        // 检查图案编号唯一性（如果变更）
        if (!pattern.getPatternCode().equals(updateDTO.getPatternCode()) &&
                printPatternMapper.findByPatternCode(updateDTO.getPatternCode()) != null) {
            throw new BusinessException("新图案编号已存在");
        }

        pattern.setPatternCode(updateDTO.getPatternCode());
        pattern.setPatternName(updateDTO.getPatternName());
        pattern.setMachineModel(updateDTO.getMachineModel());
        pattern.setImageUrl(updateDTO.getImageUrl());
        pattern.setDefaultPrintSpeed(updateDTO.getDefaultPrintSpeed());
        pattern.setDefaultPressure(updateDTO.getDefaultPressure());

        printPatternMapper.updateById(pattern);
        return convertToDTO(pattern);
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
}
