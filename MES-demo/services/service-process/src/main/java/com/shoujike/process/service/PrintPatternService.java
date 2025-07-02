package com.shoujike.process.service;

import com.shoujike.common.exception.BusinessException;
import com.shoujike.common.exception.EntityNotFoundException;
import com.shoujike.process.model.dto.PrintPatternCreateDTO;
import com.shoujike.process.model.dto.PrintPatternDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PrintPatternService {
    PrintPatternDTO createPrintPattern(PrintPatternCreateDTO createDTO) throws BusinessException;
    PrintPatternDTO updatePrintPattern(Integer id, PrintPatternCreateDTO updateDTO) throws EntityNotFoundException, BusinessException;
    PrintPatternDTO getPrintPatternById(Integer id) throws EntityNotFoundException;
    PrintPatternDTO getPrintPatternByCode(String patternCode) throws EntityNotFoundException;
    List<PrintPatternDTO> getPrintPatternsByMachineModel(String machineModel);
    Page<PrintPatternDTO> getAllPrintPatterns(Pageable pageable);
    void deletePrintPattern(Integer id) throws EntityNotFoundException;
    public String storeImage(MultipartFile file) throws BusinessException;
}

