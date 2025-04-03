package com.commerce.batchservice.job;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.commerce.batchservice.entity.Wishlist;
import com.commerce.batchservice.repository.WishlistDeletedLogRepository;
import com.commerce.batchservice.repository.WishlistRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WishlistCleanupJobConfigTest {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private WishlistDeletedLogRepository logRepository;

    @Autowired
    private Job wishlistCleanupJob;

    @Autowired
    private JobLauncher jobLauncher;

    @BeforeEach
    void setUp() {
        wishlistRepository.deleteAll();
        logRepository.deleteAll();

        List<Wishlist> dummyWishlist = IntStream.range(0, 100)
            .mapToObj(i -> Wishlist.builder()
                .userId("test-user-" + i)
                .productId((long) i)
                .wishlistCreatedAt(LocalDateTime.now().minusDays(31))
                .build())
            .collect(Collectors.toList());

        wishlistRepository.saveAll(dummyWishlist);
    }

    @Test
    @DisplayName("배치 실행 시 wishlist 데이터가 삭제되고 log 테이블에 저장된다")
    void testWishlistCleanupJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())
            .toJobParameters();

        JobExecution execution = jobLauncher.run(wishlistCleanupJob, params);

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        assertEquals(0, wishlistRepository.count());
        assertEquals(100, logRepository.count());
    }
}
