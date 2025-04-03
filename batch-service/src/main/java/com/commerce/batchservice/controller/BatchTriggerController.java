package com.commerce.batchservice.controller;

import com.commerce.batchservice.entity.Wishlist;
import com.commerce.batchservice.repository.WishlistRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/batch")
@Slf4j
public class BatchTriggerController {

    private final JobLauncher jobLauncher;
    private final Job wishlistCleanupJob;


    private final WishlistRepository wishlistRepository;

    @PostMapping("/dummy")
    public ResponseEntity<String> wishlistDummy()  {

        List<Wishlist> dummyWishlist = IntStream.range(0, 100)
            .mapToObj(i -> Wishlist.builder()
                .userId("test-user-" + i)
                .productId((long) i)
                .wishlistCreatedAt(LocalDateTime.now().minusDays(31))
                .build())
            .collect(Collectors.toList());

        wishlistRepository.saveAll(dummyWishlist);
        log.info("✅ 더미 위시리스트 100개가 삽입되었습니다.");

        return ResponseEntity.ok("Wishlist dummy data 생성 완료!");
    }

    @PostMapping("/wishlist-cleanup")
    public ResponseEntity<String> runWishlistCleanup() throws Exception {
        JobParameters params = new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())
            .toJobParameters();

        log.info("🚀 Job 실행 시도 중 - params: {}", params); // ✅ 추가
        JobExecution execution = jobLauncher.run(wishlistCleanupJob, params);
        log.info("✅ Job 실행 결과: {}", execution.getStatus()); // ✅ 추가

        return ResponseEntity.ok("Wishlist cleanup job 실행됨!");
    }
}