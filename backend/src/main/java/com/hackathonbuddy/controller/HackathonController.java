package com.hackathonbuddy.controller;

import com.hackathonbuddy.dto.request.CreateHackathonRequest;
import com.hackathonbuddy.dto.response.ApiResponse;
import com.hackathonbuddy.dto.response.HackathonPageResponse;
import com.hackathonbuddy.dto.response.HackathonResponse;
import com.hackathonbuddy.entity.User;
import com.hackathonbuddy.model.Hackathon;
import com.hackathonbuddy.repository.RegistrationRepository;
import com.hackathonbuddy.repository.UserRepository;
import com.hackathonbuddy.service.HackathonAggregationService;
import com.hackathonbuddy.service.HackathonService;
import com.hackathonbuddy.util.HackathonDateUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/hackathons")
@Tag(name = "Hackathons", description = "Hackathon aggregation, search, and management API")
public class HackathonController {

    private final HackathonService hackathonService;
    private final HackathonAggregationService aggregationService;
    private final UserRepository userRepository;
    private final com.hackathonbuddy.service.NotificationService notificationService;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private RegistrationRepository registrationRepository;

    public HackathonController(
            HackathonService hackathonService,
            HackathonAggregationService aggregationService,
            UserRepository userRepository,
            com.hackathonbuddy.service.NotificationService notificationService) {
        this.hackathonService = hackathonService;
        this.aggregationService = aggregationService;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setRegistrationRepository(RegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
    }

    private User getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userRepository.findByEmail(userDetails.getUsername()).orElse(null);
    }

    @GetMapping
    @Operation(summary = "Get all hackathons (aggregated real-time & internal)", description = "Fetches and merges live scraped hackathons from Unstop, HackerEarth, and Devpost with active database hackathons. Automatically removes finished/expired hackathons.")
    public ResponseEntity<ApiResponse<List<HackathonResponse>>> getAllHackathons(
            @Parameter(description = "Platform filter: devpost, devfolio, hackerearth, unstop, internal") @RequestParam(required = false) List<String> platform,
            @Parameter(description = "Status filter: open, upcoming") @RequestParam(required = false) String status,
            @Parameter(description = "Case-insensitive search in title, organizer, or tags") @RequestParam(required = false) String search,
            @Parameter(description = "Mode filter: Online, In-Person") @RequestParam(required = false) String mode,
            @Parameter(description = "Featured filter: true, false") @RequestParam(required = false) Boolean featured,
            @Parameter(description = "Sort field: deadline, title, platform") @RequestParam(required = false, defaultValue = "deadline") String sort,
            @Parameter(description = "Sort direction: asc, desc") @RequestParam(required = false, defaultValue = "asc") String direction,
            @Parameter(description = "0-indexed page number") @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(required = false, defaultValue = "60") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = getCurrentUser(userDetails);
        List<HackathonResponse> combined = new ArrayList<>();

        // Normalize platform filter set
        Set<String> platformSet = (platform != null && !platform.isEmpty())
                ? platform.stream().flatMap(p -> Arrays.stream(p.split(",")))
                        .map(String::trim).map(String::toLowerCase).filter(s -> !s.isEmpty()).collect(Collectors.toSet())
                : Collections.emptySet();

        // 1. Fetch internal non-expired database hackathons if no platform filter or filter includes "internal"
        if (platformSet.isEmpty() || platformSet.contains("internal")) {
            try {
                List<HackathonResponse> internal = hackathonService.getAllHackathons(user);
                if (internal != null) {
                    combined.addAll(internal);
                }
            } catch (Exception e) {
                log.error("Failed to load internal hackathons: {}", e.getMessage());
            }
        }

        // 2. Fetch live real-time scraped hackathons from Unstop, HackerEarth, Devpost
        try {
            HackathonPageResponse externalPage = aggregationService.fetchAndAggregate(
                    platform, status, search, mode, featured, sort, direction, 0, 100);
            if (externalPage != null && externalPage.getContent() != null) {
                for (Hackathon ext : externalPage.getContent()) {
                    combined.add(mapExternalToResponse(ext, user));
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch live aggregated hackathons: {}", e.getMessage());
        }

        // 3. Client-level filtering for internal items if search/mode/status were applied
        if (search != null && !search.trim().isEmpty()) {
            String q = search.trim().toLowerCase();
            combined = combined.stream().filter(h ->
                    (h.getTitle() != null && h.getTitle().toLowerCase().contains(q)) ||
                    (h.getCategory() != null && h.getCategory().toLowerCase().contains(q)) ||
                    (h.getDescription() != null && h.getDescription().toLowerCase().contains(q)) ||
                    (h.getOrganizer() != null && h.getOrganizer().toLowerCase().contains(q))
            ).collect(Collectors.toList());
        }

        if (mode != null && !mode.trim().isEmpty()) {
            String m = mode.trim().toLowerCase();
            combined = combined.stream().filter(h ->
                    (h.getMode() != null && h.getMode().toLowerCase().contains(m)) ||
                    (h.getLocation() != null && h.getLocation().toLowerCase().contains(m))
            ).collect(Collectors.toList());
        }

        return ResponseEntity.ok(ApiResponse.<List<HackathonResponse>>builder()
                .success(true)
                .message("Hackathons retrieved (" + combined.size() + " active opportunities)")
                .data(combined)
                .build());
    }

    @GetMapping("/external")
    @Operation(summary = "Get aggregated external portal hackathons", description = "Aggregates hackathons directly from Devpost, Devfolio, HackerEarth, and Unstop.")
    public ResponseEntity<ApiResponse<HackathonPageResponse>> getExternalHackathons(
            @Parameter(description = "Platform filter: devpost, devfolio, hackerearth, unstop") @RequestParam(required = false) List<String> platform,
            @Parameter(description = "Status filter: open, upcoming") @RequestParam(required = false) String status,
            @Parameter(description = "Case-insensitive search in title, organizer, or tags") @RequestParam(required = false) String search,
            @Parameter(description = "Mode filter: Online, In-Person") @RequestParam(required = false) String mode,
            @Parameter(description = "Featured filter: true, false") @RequestParam(required = false) Boolean featured,
            @Parameter(description = "Sort field: deadline, title, platform") @RequestParam(required = false, defaultValue = "deadline") String sort,
            @Parameter(description = "Sort direction: asc, desc") @RequestParam(required = false, defaultValue = "asc") String direction,
            @Parameter(description = "0-indexed page number") @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(required = false, defaultValue = "50") int size) {

        HackathonPageResponse pageResponse = aggregationService.fetchAndAggregate(
                platform, status, search, mode, featured, sort, direction, page, size);
        return ResponseEntity.ok(ApiResponse.success("External hackathons retrieved", pageResponse));
    }

    @GetMapping("/recommended")
    @Operation(summary = "Get recommended hackathons")
    public ResponseEntity<ApiResponse<List<HackathonResponse>>> getRecommended(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        List<HackathonResponse> hackathons = hackathonService.getRecommendedHackathons(user);
        return ResponseEntity.ok(ApiResponse.<List<HackathonResponse>>builder()
                .success(true).message("Recommended hackathons retrieved").data(hackathons).build());
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming hackathons")
    public ResponseEntity<ApiResponse<List<HackathonResponse>>> getUpcoming(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        List<HackathonResponse> hackathons = hackathonService.getUpcomingHackathons(user);
        return ResponseEntity.ok(ApiResponse.<List<HackathonResponse>>builder()
                .success(true).message("Upcoming hackathons retrieved").data(hackathons).build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get hackathon by ID", description = "Retrieves an aggregated external hackathon by string ID (e.g., unstop-123, hackerearth-slug) or internal database hackathon by numeric ID.")
    public ResponseEntity<ApiResponse<?>> getById(
            @Parameter(description = "Hackathon ID (numeric DB ID or string external ID)") @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = getCurrentUser(userDetails);

        if (id.startsWith("devpost-") || id.startsWith("devfolio-") || id.startsWith("hackerearth-") || id.startsWith("unstop-")) {
            Hackathon externalHackathon = aggregationService.findById(id).orElse(null);
            if (externalHackathon != null) {
                return ResponseEntity.ok(ApiResponse.success("External hackathon retrieved", mapExternalToResponse(externalHackathon, user)));
            }
            return ResponseEntity.status(404).body(ApiResponse.error("Hackathon not found with id: " + id));
        }

        try {
            Long numericId = Long.parseLong(id);
            HackathonResponse hackathon = hackathonService.getHackathonById(numericId, user);
            return ResponseEntity.ok(ApiResponse.<HackathonResponse>builder()
                    .success(true).message("Hackathon retrieved").data(hackathon).build());
        } catch (NumberFormatException e) {
            Hackathon externalHackathon = aggregationService.findById(id).orElse(null);
            if (externalHackathon != null) {
                return ResponseEntity.ok(ApiResponse.success("External hackathon retrieved", mapExternalToResponse(externalHackathon, user)));
            }
            return ResponseEntity.status(404).body(ApiResponse.error("Hackathon not found with id: " + id));
        }
    }

    @PostMapping
    @Operation(summary = "Create a new hackathon")
    public ResponseEntity<ApiResponse<HackathonResponse>> createHackathon(
            @RequestBody CreateHackathonRequest request) {
        HackathonResponse hackathon = hackathonService.createHackathon(request);

        // Broadcast announcement to all users
        try {
            userRepository.findAll().forEach(u -> {
                notificationService.createNotification(u, "Hackathon Announcement: " + hackathon.getTitle(),
                        "New hackathon '" + hackathon.getTitle() + "' is now open for registration! Deadline: " +
                        (hackathon.getRegistrationDeadline() != null ? hackathon.getRegistrationDeadline() : "Soon"),
                        "hackathon", "🚀", "View Hackathon", "/hackathons");
            });
        } catch (Exception ignored) {}

        return ResponseEntity.ok(ApiResponse.<HackathonResponse>builder()
                .success(true).message("Hackathon created").data(hackathon).build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete hackathon by ID")
    public ResponseEntity<ApiResponse<Void>> deleteHackathon(@PathVariable Long id) {
        hackathonService.deleteHackathon(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true).message("Hackathon deleted").build());
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update hackathon status")
    public ResponseEntity<ApiResponse<HackathonResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        HackathonResponse hackathon = hackathonService.updateHackathonStatus(id, status);
        return ResponseEntity.ok(ApiResponse.<HackathonResponse>builder()
                .success(true).message("Hackathon status updated").data(hackathon).build());
    }

    private HackathonResponse mapExternalToResponse(Hackathon ext, User user) {
        String category = "AI/ML";
        if (ext.getTags() != null && !ext.getTags().isEmpty()) {
            category = ext.getTags().get(0);
        }
        category = normalizeCategory(category, ext.getTitle());

        String platform = ext.getPlatform() != null ? ext.getPlatform() : "External";
        String color = getPlatformColor(platform);
        String icon = getPlatformIcon(platform);

        boolean isRegistered = false;
        if (user != null && ext.getTitle() != null) {
            try {
                isRegistered = registrationRepository.findByUser(user).stream()
                        .anyMatch(r -> r.getHackathon() != null &&
                                (ext.getTitle().equalsIgnoreCase(r.getHackathon().getTitle()) ||
                                 (ext.getId() != null && ext.getId().equalsIgnoreCase(String.valueOf(r.getHackathon().getId())))));
            } catch (Exception ignored) {}
        }

        int matchScore = 80 + Math.abs((ext.getTitle() != null ? ext.getTitle() : "Hackathon").hashCode() % 19);
        String cleanTitle = cleanText(ext.getTitle() != null ? ext.getTitle() : "Unnamed Hackathon");

        return HackathonResponse.builder()
                .id(ext.getId())
                .title(cleanTitle)
                .description(ext.getOrganizer() != null
                        ? "Organized by " + cleanText(ext.getOrganizer()) + " on " + platform + ". Live competition open for developers."
                        : "Live real-time opportunity on " + platform + ". Join now to compete and win.")
                .category(category)
                .organizer(ext.getOrganizer() != null ? ext.getOrganizer() : platform)
                .platform(platform)
                .startDate(ext.getDeadline())
                .endDate(ext.getDeadline())
                .registrationDeadline(ext.getDeadline())
                .prizePool(ext.getPrize() != null && !ext.getPrize().trim().isEmpty() ? ext.getPrize() : "₹1,00,000")
                .location(ext.getMode() != null ? ext.getMode() : "Online")
                .mode(ext.getMode() != null ? ext.getMode() : "Online")
                .imageUrl(ext.getLogo())
                .websiteUrl(ext.getUrl())
                .url(ext.getUrl())
                .isActive(true)
                .matchScore(matchScore)
                .isRegistered(isRegistered)
                .date(HackathonDateUtils.formatDisplayDate(ext.getDeadline()))
                .deadline(ext.getDeadline())
                .prize(ext.getPrize() != null && !ext.getPrize().trim().isEmpty() ? ext.getPrize() : "₹1,00,000")
                .participants(ext.getParticipants() != null ? ext.getParticipants() : 0)
                .duration("48 Hours")
                .match(matchScore)
                .status(ext.getStatus() != null ? ext.getStatus() : "Open")
                .level("All Levels")
                .color(color)
                .icon(icon)
                .type(platform.toLowerCase())
                .build();
    }

    private String getPlatformColor(String platform) {
        if (platform == null) return "purple";
        switch (platform.toLowerCase()) {
            case "unstop": return "blue";
            case "hackerearth": return "teal";
            case "devpost": return "purple";
            default: return "orange";
        }
    }

    private String getPlatformIcon(String platform) {
        if (platform == null) return "🏆";
        switch (platform.toLowerCase()) {
            case "unstop": return "🎯";
            case "hackerearth": return "⚡";
            case "devpost": return "💻";
            default: return "🚀";
        }
    }

    private String normalizeCategory(String rawTag, String title) {
        String combined = ((rawTag != null ? rawTag : "") + " " + (title != null ? title : "")).toLowerCase();
        if (combined.contains("ai") || combined.contains("machine learning") || combined.contains("neural") || combined.contains("gpt") || combined.contains("data") || combined.contains("deep learning")) {
            return "AI/ML";
        }
        if (combined.contains("web3") || combined.contains("crypto") || combined.contains("blockchain") || combined.contains("ethereum") || combined.contains("defi") || combined.contains("chain")) {
            return "Web3";
        }
        if (combined.contains("fintech") || combined.contains("bank") || combined.contains("finance") || combined.contains("payment") || combined.contains("money")) {
            return "FinTech";
        }
        if (combined.contains("climate") || combined.contains("green") || combined.contains("environment") || combined.contains("earth") || combined.contains("sustain")) {
            return "Environment";
        }
        if (combined.contains("health") || combined.contains("bio") || combined.contains("med") || combined.contains("humanity") || combined.contains("care")) {
            return "Healthcare";
        }
        if (combined.contains("city") || combined.contains("urban") || combined.contains("smart") || combined.contains("traffic") || combined.contains("robot") || combined.contains("iot") || combined.contains("hardware")) {
            return "Smart City";
        }
        return "AI/ML";
    }

    private String cleanText(String text) {
        if (text == null) return null;
        return text.replace("??", " - ").trim();
    }
}
