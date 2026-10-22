package com.xiaosuda.dto;

import lombok.Data;

import java.util.List;

@Data
public class ImageUploadRequest {
    private List<String> images;
}
