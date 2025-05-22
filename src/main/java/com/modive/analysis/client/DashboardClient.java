package com.modive.analysis.client;

import com.modive.analysis.entity.Drive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "dashboard-service", url = "http://dashboard-service:8080")
public interface DashboardClient {

    @PostMapping("/dashboard/post-drive/{driveId}")
    ResponseEntity<Void> createPostDriveDashboard(
            @RequestHeader("X-User-Id") String userId, // TODO: userId 연동
            @PathVariable String driveId,
            @RequestBody Drive drive);

}
