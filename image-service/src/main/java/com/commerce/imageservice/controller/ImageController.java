package com.commerce.imageservice.controller;

import com.commerce.imageservice.repository.ImageRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Image-service의 Controller
 *
 * @author : choiseongho
 * @fileName : ImageController.java
 * @since : 2025-01-03
 */

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/image")
public class ImageController {

    private final ImageRepository imageRepository;


    @GetMapping("/{id}")
    @Operation(summary = "[Feign] 이미지 ID 조회")
    public String findImageId(@PathVariable(name = "id" ) Long id) {
        return imageRepository.findById(id).orElseThrow().getImageLob();
    }
}
