package com.commerce.imageservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Table(name = "image")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id", nullable = false)
    private Long id; // 이미지 ID

    @Column(name = "image_lob", nullable = false)
    @Lob
    private String imageLob;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false)
    private ImageType imageType; // 이미지 타입 (대표이미지, 상세이미지 등)

    // 이미지 타입 Enum
    public enum ImageType {
        THUMBNAIL,    // 대표 이미지
        DETAIL       // 상세 이미지
    }
}