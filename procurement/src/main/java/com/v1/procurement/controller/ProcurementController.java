package com.v1.procurement.controller;

import com.v1.procurement.dto.*;
import com.v1.procurement.model.ProcurementStatus;
import com.v1.procurement.service.ProcurementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/procurement")
@Tag(name = "Procurement", description = "Procurement and RFQ management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ProcurementController {

    private final ProcurementService procurementService;

    public ProcurementController(ProcurementService procurementService) {
        this.procurementService = procurementService;
    }

    @PostMapping("/create")
    @Operation(
            summary = "Create new procurement/RFQ",
            description = "Create a new procurement request. System automatically determines method based on value.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Procurement created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public ResponseEntity<?> createProcurement(
            @Valid @RequestBody CreateProcurementRequest request,
            Authentication authentication) {
        try {
            log.info("Creating new procurement: {}", request.getTitle());

            // Get user ID from JWT token
            Long userId = Long.parseLong(authentication.getName());

            ProcurementResponse response = procurementService.createProcurement(request, userId);

            log.info("Procurement created successfully: {}", response.getReferenceNumber());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Error creating procurement: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to create procurement: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get procurement details",
            description = "Retrieve detailed information about a specific procurement",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Procurement details retrieved"),
                    @ApiResponse(responseCode = "404", description = "Procurement not found")
            }
    )
    public ResponseEntity<?> getProcurementById(@PathVariable Long id) {
        try {
            log.info("Retrieving procurement: {}", id);
            ProcurementResponse response = procurementService.getProcurementById(id);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving procurement: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Procurement not found");
        }
    }

    @GetMapping("/list")
    @Operation(
            summary = "List all procurements",
            description = "Retrieve paginated list of all procurements",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of procurements retrieved"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public ResponseEntity<?> getAllProcurements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("Retrieving all procurements - page: {}, size: {}", page, size);
            Page<ProcurementResponse> response = procurementService.getAllProcurements(PageRequest.of(page, size));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving procurements: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve procurements");
        }
    }

    @GetMapping("/status/{status}")
    @Operation(
            summary = "Get procurements by status",
            description = "Retrieve procurements filtered by status",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Filtered procurements retrieved")
            }
    )
    public ResponseEntity<?> getProcurementsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("Retrieving procurements by status: {}", status);
            ProcurementStatus procStatus = ProcurementStatus.valueOf(status.toUpperCase());
            Page<ProcurementResponse> response = procurementService.getProcurementsByStatus(procStatus, PageRequest.of(page, size));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving procurements by status: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid status or retrieval failed");
        }
    }

    @PutMapping("/{id}/update")
    @Operation(
            summary = "Update procurement",
            description = "Update procurement details (only for DRAFT status)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Procurement updated"),
                    @ApiResponse(responseCode = "400", description = "Cannot update non-DRAFT procurement")
            }
    )
    public ResponseEntity<?> updateProcurement(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProcurementRequest request) {
        try {
            log.info("Updating procurement: {}", id);
            ProcurementResponse response = procurementService.updateProcurement(id, request);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error updating procurement: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to update procurement: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/publish")
    @Operation(
            summary = "Publish procurement and send RFQs",
            description = "Publish procurement to suppliers and optionally post on promise.lk",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Procurement published successfully"),
                    @ApiResponse(responseCode = "400", description = "Cannot publish non-DRAFT procurement")
            }
    )
    public ResponseEntity<?> publishProcurement(
            @PathVariable Long id,
            @Valid @RequestBody PublishProcurementRequest request) {
        try {
            log.info("Publishing procurement: {}", id);
            ProcurementResponse response = procurementService.publishProcurement(id, request);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error publishing procurement: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to publish procurement: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/rfq-recipients")
    @Operation(
            summary = "Add RFQ recipient",
            description = "Add a supplier to receive RFQ for this procurement",
            responses = {
                    @ApiResponse(responseCode = "201", description = "RFQ recipient added"),
                    @ApiResponse(responseCode = "404", description = "Procurement not found")
            }
    )
    public ResponseEntity<?> addRfqRecipient(
            @PathVariable Long id,
            @Valid @RequestBody RfqRecipientRequest request) {
        try {
            log.info("Adding RFQ recipient for procurement: {}", id);
            RfqRecipientResponse response = procurementService.addRfqRecipient(id, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Error adding RFQ recipient: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to add RFQ recipient: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/rfq-recipients")
    @Operation(
            summary = "Get RFQ recipients",
            description = "Retrieve list of suppliers who received RFQ for this procurement",
            responses = {
                    @ApiResponse(responseCode = "200", description = "RFQ recipients retrieved")
            }
    )
    public ResponseEntity<?> getRfqRecipients(@PathVariable Long id) {
        try {
            log.info("Retrieving RFQ recipients for procurement: {}", id);
            List<RfqRecipientResponse> response = procurementService.getRfqRecipients(id);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving RFQ recipients: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Procurement not found");
        }
    }

    @DeleteMapping("/{id}/delete")
    @Operation(
            summary = "Delete procurement",
            description = "Delete procurement (only for DRAFT status)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Procurement deleted"),
                    @ApiResponse(responseCode = "400", description = "Cannot delete non-DRAFT procurement")
            }
    )
    public ResponseEntity<?> deleteProcurement(@PathVariable Long id) {
        try {
            log.info("Deleting procurement: {}", id);
            procurementService.deleteProcurement(id);
            return ResponseEntity.ok("Procurement deleted successfully");

        } catch (Exception e) {
            log.error("Error deleting procurement: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to delete procurement: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    @Operation(
            summary = "Health check",
            description = "Check if procurement service is running",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Service is healthy")
            }
    )
    public ResponseEntity<?> health() {
        log.info("Procurement service health check");
        return ResponseEntity.ok("Procurement service is running");
    }
}
