package com.commerce.batchservice.job;

import com.commerce.batchservice.entity.Wishlist;
import com.commerce.batchservice.entity.WishlistDeletedLog;
import com.commerce.batchservice.repository.WishlistDeletedLogRepository;
import com.commerce.batchservice.repository.WishlistRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Configuration
@RequiredArgsConstructor
@Slf4j
public class WishlistCleanupJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final WishlistRepository wishlistRepository;
    private final WishlistDeletedLogRepository logRepository;
    private final EntityManagerFactory entityManagerFactory;

    private static final int CHUNK_SIZE = 10;

    @Bean
    public Job wishlistCleanupJob() {
        System.out.println("테스트");
        return new JobBuilder("wishlistCleanupJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(deleteOldWishlistStep())
            .build();
    }

    @Bean
    public Step deleteOldWishlistStep() {
        return new StepBuilder("deleteOldWishlistStep", jobRepository)
            .<Wishlist, WishlistDeletedLog>chunk(CHUNK_SIZE, transactionManager)
            .reader(wishlistItemReader())
            .processor(wishlistItemProcessor())
            .writer(wishlistItemWriter())
            .faultTolerant() // 에러 허용 시작
            .retry(RuntimeException.class) // 재 시도할 예외 지정
            .retryLimit(1) // 최대 재시도 횟수
            .skip(RuntimeException.class) // 이 예외는 스킵하고 계속 진행
            .skipLimit(10) // 최대 10개까지 스킵 허용
            .listener(skipListener())
            .build();
    }

    @Bean
    public SkipListener<Wishlist, WishlistDeletedLog> skipListener() {
        return new SkipListener<>() {
            @Override
            public void onSkipInProcess(Wishlist item, Throwable t) {
                log.info("⚠️ Skip 발생! userId = " + item.getUserId() + ", reason: " + t.getMessage());
            }
        };
    }

    @Bean
    @StepScope
    public ItemReader<Wishlist> wishlistItemReader() {
        return new ListItemReader<>(
            wishlistRepository.findByWishlistCreatedAtBefore(LocalDateTime.now().minusDays(30))
        );
    }

    @Bean
    public ItemProcessor<Wishlist, WishlistDeletedLog> wishlistItemProcessor() {
        return wishlist -> {
            // 10% 확률로 예외 발생
            if (Math.random() < 0.1) {
                log.info("🔥 예외 발생 - Retry 테스트용");
                throw new RuntimeException("🔥 랜덤 예외 발생");
            }

            return WishlistDeletedLog.builder()
                .userId(wishlist.getUserId())
                .productId(wishlist.getProductId())
                .deletedAt(LocalDateTime.now())
                .build();
        };
    }
    @Bean
    public ItemWriter<WishlistDeletedLog> wishlistItemWriter() {
        return items -> {
            log.info("Writer 실행 - 처리할 아이템 수: " + items.size());

            logRepository.saveAll(items);

            items.forEach(item -> {
                List<Wishlist> target = wishlistRepository.findByUserIdAndProductId(
                    item.getUserId(), item.getProductId()
                );
                wishlistRepository.deleteAll(target);
            });

            log.info("Writer 완료");
        };
    }
}