package com.v1.bidding.controller;

import com.v1.bidding.dto.*;
import com.v1.bidding.service.BiddingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/bidding")
@Tag(name = "Bidding", description = "Bid submission and management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class BiddingController {

    private final BiddingService biddingService;

    public BiddingController(BiddingService biddingService) {
        this.biddingService = biddingService;
    }

    @PostMapping("/{procurementId}/submit-bid")
    @Operation(
            summary = "Submit new bid",
            description = "Submit bid with mandatory requirements: bid bond (120-day), VAT/SSCL declaration, document fee",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Bid submitted successfully"),
                    @ApiResponse(responseCode = "400", description = "Missing mandatory requirements"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public ResponseEntity<?> submitBid(
            @PathVariable Long procurementId,
            @Valid @RequestBody SubmitBidRequest request,
            Authentication authentication) {
        try {
            log.info("Bid submission received for procurement: {}", procurementId);

            Long bidderId = Long.parseLong(authentication.getName());

            BidResponse response = biddingService.submitBid(request, bidderId, procurementId);

            log.info("Bid submitted successfully: {}", response.getBidReference());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Error submitting bid: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to submit bid: " + e.getMessage());
        }
    }

    @GetMapping("/{bidId}")
    @Operation(
            summary = "Get bid details",
            description = "Retrieve complete bid information including documents and validations",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Bid found"),
                    @ApiResponse(responseCode = "404", description = "Bid not found")
            }
    )
    public ResponseEntity<?> getBidDetails(@PathVariable Long bidId) {
        try {
            log.info("Retrieving bid details: {}", bidId);
            BidResponse response = biddingService.getBidById(bidId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving bid: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Bid not found");
        }
    }

    @GetMapping("/procurement/{procurementId}/bids")
    @Operation(
            summary = "List bids for procurement",
            description = "Get all bids submitted for a specific procurement",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Bids retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Procurement not found")
            }
    )
    public ResponseEntity<?> listBids(
            @PathVariable Long procurementId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("Retrieving bids for procurement: {}", procurementId);
            Page<BidResponse> response = biddingService.getBidsByProcurement(procurementId, PageRequest.of(page, size));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving bids: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Procurement not found");
        }
    }

    @GetMapping("/bidder/{bidderId}/my-bids")
    @Operation(
            summary = "Get my bids",
            description = "Retrieve all bids submitted by the current bidder",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Bids retrieved successfully")
            }
    )
    public ResponseEntity<?> getMyBids(
            @PathVariable Long bidderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("Retrieving bids for bidder: {}", bidderId);
            Page<BidResponse> response = biddingService.getBidsByBidder(bidderId, PageRequest.of(page, size));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving bidder bids: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve bids");
        }
    }

    @GetMapping("/procurement/{procurementId}/compliant-bids")
    @Operation(
            summary = "Get compliant bids",
            description = "Get only compliant bids for a procurement (passed all validations)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Compliant bids retrieved")
            }
    )
    public ResponseEntity<?> getCompliantBids(@PathVariable Long procurementId) {
        try {
            log.info("Retrieving compliant bids for procurement: {}", procurementId);
            return ResponseEntity.ok(biddingService.getCompliantBids(procurementId));

        } catch (Exception e) {
            log.error("Error retrieving compliant bids: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Procurement not found");
        }
    }

    @PostMapping("/{bidId}/reject")
    @Operation(
            summary = "Reject bid",
            description = "Reject a bid with reason and category",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Bid rejected successfully"),
                    @ApiResponse(responseCode = "404", description = "Bid not found")
            }
    )
    public ResponseEntity<?> rejectBid(
            @PathVariable Long bidId,
            @RequestBody Map<String, String> rejectionDetails) {
        try {
            log.info("Rejecting bid: {}", bidId);
            biddingService.rejectBid(bidId, 
                    rejectionDetails.get("reason"), 
                    rejectionDetails.get("category"));
            return ResponseEntity.ok("Bid rejected successfully");

        } catch (Exception e) {
            log.error("Error rejecting bid: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to reject bid");
        }
    }

    @PostMapping("/{bidId}/amendments")
    @Operation(
            summary = "Submit bid amendment",
            description = "Submit an amendment to an existing bid",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Amendment submitted"),
                    @ApiResponse(responseCode = "404", description = "Bid not found")
            }
    )
    public ResponseEntity<?> submitBidAmendment(
            @PathVariable Long bidId,
            @RequestBody Map<String, String> amendment) {
        try {
            log.info("Submitting amendment for bid: {}", bidId);
            biddingService.submitBidAmendment(bidId, 
                    amendment.get("type"), 
                    amendment.get("description"));
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Amendment submitted successfully");

        } catch (Exception e) {
            log.error("Error submitting amendment: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to submit amendment");
        }
    }

    @GetMapping("/{bidId}/bond")
    @Operation(
            summary = "Get bid bond details",
            description = "Retrieve bid bond information",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Bond details retrieved"),
                    @ApiResponse(responseCode = "404", description = "Bond not found")
            }
    )
    public ResponseEntity<?> getBidBond(@PathVariable Long bidId) {
        try {
            log.info("Retrieving bid bond for bid: {}", bidId);
            return ResponseEntity.ok(biddingService.getBidBond(bidId));

        } catch (Exception e) {
            log.error("Error retrieving bid bond: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Bid bond not found");
        }
    }

    @GetMapping("/{bidId}/vat-sscl")
    @Operation(
            summary = "Get VAT/SSCL declaration",
            description = "Retrieve VAT/SSCL declaration details",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Declaration retrieved"),
                    @ApiResponse(responseCode = "404", description = "Declaration not found")
            }
    )
    public ResponseEntity<?> getVatSsclDeclaration(@PathVariable Long bidId) {
        try {
            log.info("Retrieving VAT/SSCL declaration for bid: {}", bidId);
            return ResponseEntity.ok(biddingService.getVatSsclDeclaration(bidId));

        } catch (Exception e) {
            log.error("Error retrieving VAT/SSCL declaration: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Declaration not found");
        }
    }

    @GetMapping("/health")
    @Operation(
            summary = "Health check",
            description = "Bidding service health status",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Service is healthy")
            }
    )
    public ResponseEntity<?> health() {
        return ResponseEntity.ok("Bidding service is running");
    }

    // Helper map import
    static class Map<K, V> extends java.util.HashMap<K, V> {}
}
