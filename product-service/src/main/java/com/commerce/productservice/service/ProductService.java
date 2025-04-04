package com.commerce.productservice.service;

import com.commerce.common.exception.CustomException;
import com.commerce.productservice.dto.ProductDetailDto;
import com.commerce.productservice.dto.ProductDetailDto.ProductOptionDto;
import com.commerce.productservice.dto.ProductListDto;
import com.commerce.productservice.dto.ProductOptionDetailDto;
import com.commerce.productservice.dto.ProductPriceDto;
import com.commerce.productservice.dto.ProductSaveDto;
import com.commerce.productservice.dto.ProductSearchDto;
import com.commerce.productservice.dto.WishlistListDto;
import com.commerce.productservice.entity.Product;
import com.commerce.productservice.entity.ProductOption;
import com.commerce.productservice.feign.ImageServiceClient;
import com.commerce.productservice.kafka.ImageEventProducer;
import com.commerce.productservice.kafka.OrderStatusProducer;
import com.commerce.productservice.repository.ProductOptionRepository;
import com.commerce.productservice.repository.ProductRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final WishlistService wishlistService;

    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;

    // Image Event Kafka
    private final ImageEventProducer imageEventProducer;
    // Order Status Kafka
    private final OrderStatusProducer orderStatusProducer;

    // Image Service Client
    private final ImageServiceClient imageServiceClient;



    /**
     * 상품 저장 Service
     *
     * @param productSaveDto
     */
    @Transactional
    public void saveProduct(ProductSaveDto productSaveDto) {
        // 상품 엔티티 생성 및 저장
        Product product = productSaveDto.toEntity();
        Product savedProduct = productRepository.save(product);

        // 상품 옵션 저장
        List<ProductOption> options = productSaveDto.getOptions().stream()
            .map(option -> {
                ProductOption productOption = option.toEntity();
                productOption.setProduct(savedProduct);
                return productOption;
            })
            .collect(Collectors.toList());

        productOptionRepository.saveAll(options);


        // 이미지 처리 및 Kafka 이벤트 전송
        String imageLob = productSaveDto.getImageLob();
        if (imageLob != null && !imageLob.isEmpty()) {

            // 이미지 전송
            imageEventProducer.sendImageEvent(imageLob, savedProduct.getId());
        }
    }

    /**
     * 상품 상세 정보 조회 Service
     *
     * @param productId the product id
     */
    @Transactional(readOnly = true)
    @CircuitBreaker(name = "imageService", fallbackMethod = "fallbackImage")
    public ProductDetailDto findProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new CustomException("해당 상품이 존재하지 않습니다. ID: " + productId));

        Long imageId = product.getImageId();
        String imageLob = null;
        if (imageId != null) {
            try {
                // Feign Client 로 imageLob정보 조회
                imageLob = imageServiceClient.getImageLob(imageId);
            } catch (Exception e) {
                log.error("이미지 서비스 호출 중 오류 발생: {}", e.getMessage(), e);
                throw e; // Circuit Breaker가 이 예외를 감지하도록 다시 던짐
            }
        }


        return ProductDetailDto.fromEntity(product , imageLob);
    }


    /**
     * 상품 목록 조회 (페이징) Service
     *
     * @param searchDto the search dto
     * @param pageable  the pageable
     */
    @Transactional(readOnly = true)
    public Page<ProductListDto> findProductList(ProductSearchDto searchDto, Pageable pageable) {
        List<WishlistListDto> cacheWishList = Collections.emptyList();
        if (searchDto.getLoginId() != null) {
            cacheWishList = wishlistService.findWishlist(searchDto.getLoginId());
        }


        Page<ProductListDto> productList = productRepository.findProductsList(searchDto, pageable);


        // 위시리스트에 있는 상품 ID 목록 생성
        Set<Long> wishedProductIds = cacheWishList.stream()
            .map(WishlistListDto::getProductId)
            .collect(Collectors.toSet());

        // 각 상품의 이미지 정보 조회
        productList.getContent().forEach(product -> {

            // 위시리스트 체크
            product.setIsWishlisted(wishedProductIds.contains(product.getId()));

            Long imageId = product.getImageId();
            if (imageId != null) {
                try {
                    // Feign Client로 imageLob 정보 조회
                    String imageLob = imageServiceClient.getImageLob(imageId);
                    product.setImageLob(imageLob);
                } catch (Exception e) {
                    log.error("---------------------------------------------------------------------------");
                    log.error("이미지 정보 가져오기 실패");
                    log.error("imageId: {}", imageId);
                    log.error("productId: {}", product.getId());
                    log.error("e.getMessage: {}", e.getMessage());
                }
            }
        });

        return productList;
    }


    /**
     * 상품 재고 감소 Service
     *
     * @param productId 상품 ID
     * @param optionId 옵션 ID (null 가능)
     * @param quantity 감소시킬 수량
     */
    @Transactional
    public void decreaseStock(Long productId, Long optionId, Integer quantity, Long orderId) {

        if (optionId != null) {
            // 옵션 재고 감소
            ProductOption option = productOptionRepository.findById(optionId)
                .orElseThrow(() -> new CustomException("해당 상품 옵션이 존재하지 않습니다. OptionId: " + optionId));

            // 재고 확인
            if (option.getStock() < quantity) {
                log.error("재고 부족 - OptionId: {}, 현재 재고: {}, 요청 수량: {}",
                    optionId, option.getStock(), quantity);
                orderStatusProducer.sendOrderStatusUpdateEvent(orderId, "OUT_OF_STOCK");
            }else{
                log.info("옵션 재고 감소 완료 - OptionId: {}, 감소 수량: {}, 남은 재고: {}",
                    optionId, quantity, option.getStock());

                // 재고 감소
                option.setStock(option.getStock() - quantity);
                productOptionRepository.save(option);
            }

        }
    }

    /**
     * 상품 재고 증가 Service (주문 취소 시 재고 복구)
     *
     * @param productId 상품 ID
     * @param optionId 옵션 ID (null 가능)
     * @param quantity 증가시킬 수량
     * @param orderId 주문 ID
     */
    @Transactional
    public void increaseStock(Long productId, Long optionId, Integer quantity, Long orderId) {
        try {
            if (optionId != null) {
                // 옵션 재고 증가
                ProductOption option = productOptionRepository.findById(optionId)
                    .orElseThrow(() -> new CustomException("해당 상품 옵션이 존재하지 않습니다. OptionId: " + optionId));

                // 재고 증가
                option.setStock(option.getStock() + quantity);
                productOptionRepository.save(option);

                log.info("옵션 재고 증가 완료 - OptionId: {}, 증가 수량: {}, 현재 재고: {}, 주문 ID: {}",
                    optionId, quantity, option.getStock(), orderId);
            }
        } catch (Exception e) {
            log.error("재고 증가 중 오류 발생 - ProductId: {}, OptionId: {}, 수량: {}, 주문 ID: {}",
                productId, optionId, quantity, orderId, e);
            throw new CustomException("재고 증가 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }




    // Feign Client 영역
    /**
     * 상품 유효성 검증 (Feign Client용)
     *
     * @param productId 상품 ID
     */
    @Transactional(readOnly = true)
    public boolean validateProduct(Long productId) {
        return productRepository.existsById(productId);
    }

    /**
     * 상품 이미지 ID 조회
     *
     * @param productId the product id
     */
    @Transactional(readOnly = true)
    public Long findProductImageId(Long productId) {
        return productRepository.findById(productId).orElseThrow().getImageId();
    }

    /**
     * 상품 가격 정보 조회
     *
     * @param productId 상품 ID
     */
    @Transactional(readOnly = true)
    public ProductPriceDto getProductPrice(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new CustomException("해당 상품이 존재하지 않습니다. ID: " + productId));

        return new ProductPriceDto(product.getPrice());
    }

    /**
     * 상품 옵션 정보 조회
     *
     * @param productId 상품 ID
     * @param optionId 옵션 ID
     */
    @Transactional(readOnly = true)
    public ProductOptionDto getProductOption(Long productId, Long optionId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new CustomException("해당 상품이 존재하지 않습니다. ID: " + productId));

        ProductOption option = productOptionRepository.findById(optionId)
            .orElseThrow(() -> new CustomException("해당 옵션이 존재하지 않습니다. ID: " + optionId));

        // 해당 상품의 옵션인지 확인
        if (!option.getProduct().getId().equals(productId)) {
            throw new CustomException("해당 상품의 옵션이 아닙니다. ProductId: " + productId + ", OptionId: " + optionId);
        }

        return ProductOptionDto.builder()
            .id(option.getId())
            .name(option.getName())
            .price(option.getPrice())
            .stock(option.getStock())
            .build();
    }

    /**
     * 장바구니 service에서 요청하는 상품 상세 정보
     *
     * @param productId the product id
     * @param optionId  the option id
     * @return the product option
     */
    @Transactional(readOnly = true)
    public ProductOptionDetailDto findProductInfo(Long productId, Long optionId) {

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new CustomException("해당 상품이 존재하지 않습니다. ID: " + productId));

        ProductOption option = productOptionRepository.findById(optionId)
            .orElseThrow(() -> new CustomException("해당 옵션이 존재하지 않습니다. ID: " + optionId));

        String productTitle = product.getTitle();
        String optionName = option.getName();
        Integer price = option.getPrice();

        Long imageId = product.getImageId();
        String imageLob = null;

        if (imageId != null) {
            // Feign Client로 imageLob 정보 조회
            imageLob = imageServiceClient.getImageLob(imageId);
        }


        return ProductOptionDetailDto.builder()
            .productTitle(productTitle)
            .optionName(optionName)
            .price(price)
            .imageLob(imageLob)
            .build();
    }




    // Fallback 시 이미지 정보 없이 return
    public ProductDetailDto fallbackImage(Long productId, Throwable throwable) {
        log.error("이미지 서비스 호출 실패: {}", throwable.getMessage());

        // fallback 로직
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new CustomException("해당 상품이 존재하지 않습니다. ID: " + productId));

        return ProductDetailDto.fromEntity(product, null); // 이미지 없이 상품 정보만 반환
    }
}