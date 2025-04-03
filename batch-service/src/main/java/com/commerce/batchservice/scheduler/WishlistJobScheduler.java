package com.commerce.batchservice.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WishlistJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job wishlistCleanupJob;

    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시
    public void runWishlistCleanupJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis()) // 유니크 파라미터
            .toJobParameters();

        jobLauncher.run(wishlistCleanupJob, params);
        System.out.println("✅ 스케줄러: wishlistCleanupJob 실행됨");
    }

}