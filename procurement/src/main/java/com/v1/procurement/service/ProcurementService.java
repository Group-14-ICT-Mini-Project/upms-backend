package com.v1.procurement.service;

import com.v1.procurement.dto.*;
import com.v1.procurement.model.*;
import com.v1.procurement.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ProcurementService {

    @Autowired
    ProcurementRepository procurementRepository;
    @Autowired
    ProcurementMethodRepository procurementMethodRepository;
    @Autowired
    ProcurementCategoryRepository procurementCategoryRepository;
    @Autowired
    RfqRecipientRepository rfqRecipientRepository;
    @Autowired
    NewspaperPublicationRepository newspaperPublicationRepository;
    @Autowired
    PromiseLkPostRepository promiseLkPostRepository;

    public ProcurementService(ProcurementRepository procurementRepository,
                              ProcurementMethodRepository procurementMethodRepository,
                              ProcurementCategoryRepository procurementCategoryRepository,
                              RfqRecipientRepository rfqRecipientRepository,
                              NewspaperPublicationRepository newspaperPublicationRepository,
                              PromiseLkPostRepository promiseLkPostRepository) {
        this.procurementRepository = procurementRepository;
        this.procurementMethodRepository = procurementMethodRepository;
        this.procurementCategoryRepository = procurementCategoryRepository;
        this.rfqRecipientRepository = rfqRecipientRepository;
        this.newspaperPublicationRepository = newspaperPublicationRepository;
        this.promiseLkPostRepository = promiseLkPostRepository;
    }

    /**
     * Create a new procurement with automatic method selection
     */
    public ProcurementResponse createProcurement(CreateProcurementRequest request, Long userId) {
        log.info("Creating new procurement with estimated value: {}", request.getEstimatedValue());

        // Validate method and category exist
        ProcurementMethod method = procurementMethodRepository.findById(request.getProcurementMethodId())
                .orElseThrow(() -> new RuntimeException("Procurement method not found"));

        ProcurementCategory category = procurementCategoryRepository.findById(request.getProcurementCategoryId())
                .orElseThrow(() -> new RuntimeException("Procurement category not found"));

        // Generate reference number
        String referenceNumber = generateReferenceNumber();

        // Create procurement
        Procurement procurement = Procurement.builder()
                .referenceNumber(referenceNumber)
                .title(request.getTitle())
                .description(request.getDescription())
                .estimatedValue(request.getEstimatedValue())
                .procurementMethod(method)
                .procurementCategory(category)
                .status(ProcurementStatus.DRAFT)
                .openingDate(request.getOpeningDate())
                .closingDate(request.getClosingDate())
                .documentFee(request.getDocumentFee())
                .requiresBidBond(request.getRequiresBidBond())
                .bidBondPercentage(request.getBidBondPercentage())
                .createdByUserId(userId)
                .isActive(true)
                .faculty(request.getFaculty())
                .department(request.getDepartment())
                .requisitionType(request.getRequisitionType())
                .currentStockBalance(request.getCurrentStockBalance())
                .fundingSource(request.getFundingSource())
                .build();

        Procurement saved = procurementRepository.save(procurement);
        log.info("Procurement created successfully with reference: {}", referenceNumber);

        return mapToProcurementResponse(saved);
    }

    /**
     * Automatic procurement method selection based on estimated value
     * NSM: < 25M LKR
     * NCB: 25M - 500M LKR
     * LCB: > 500M LKR
     */
    public ProcurementMethod selectProcurementMethod(BigDecimal estimatedValue) {
        log.info("Selecting procurement method for value: {}", estimatedValue);

        BigDecimal nsm_threshold = new BigDecimal("25000000");
        BigDecimal ncb_threshold = new BigDecimal("500000000");

        String methodCode;
        if (estimatedValue.compareTo(nsm_threshold) < 0) {
            methodCode = "NSM";
        } else if (estimatedValue.compareTo(ncb_threshold) < 0) {
            methodCode = "NCB";
        } else {
            methodCode = "LCB";
        }

        return procurementMethodRepository.findByMethodCode(methodCode)
                .orElseThrow(() -> new RuntimeException("Procurement method not found: " + methodCode));
    }

    /**
     * Update procurement details and workflow status.
     * Detail edits are only allowed while in DRAFT; status transitions may be
     * submitted by downstream workflow roles.
     */
    public ProcurementResponse updateProcurement(Long procurementId, UpdateProcurementRequest request) {
        log.info("Updating procurement: {}", procurementId);

        Procurement procurement = procurementRepository.findById(procurementId)
                .orElseThrow(() -> new RuntimeException("Procurement not found"));

        boolean hasDetailUpdates = request.getTitle() != null
                || request.getDescription() != null
                || request.getEstimatedValue() != null
                || request.getOpeningDate() != null
                || request.getClosingDate() != null
                || request.getDocumentFee() != null
                || request.getRequiresBidBond() != null
                || request.getBidBondPercentage() != null
                || request.getFaculty() != null
                || request.getDepartment() != null
                || request.getRequisitionType() != null
                || request.getCurrentStockBalance() != null
                || request.getFundingSource() != null;

        if (hasDetailUpdates && !ProcurementStatus.DRAFT.equals(procurement.getStatus())) {
            throw new RuntimeException("Can only update procurements in DRAFT status");
        }

        if (request.getTitle() != null) procurement.setTitle(request.getTitle());
        if (request.getDescription() != null) procurement.setDescription(request.getDescription());
        if (request.getEstimatedValue() != null) procurement.setEstimatedValue(request.getEstimatedValue());
        if (request.getOpeningDate() != null) procurement.setOpeningDate(request.getOpeningDate());
        if (request.getClosingDate() != null) procurement.setClosingDate(request.getClosingDate());
        if (request.getDocumentFee() != null) procurement.setDocumentFee(request.getDocumentFee());
        if (request.getRequiresBidBond() != null) procurement.setRequiresBidBond(request.getRequiresBidBond());
        if (request.getBidBondPercentage() != null) procurement.setBidBondPercentage(request.getBidBondPercentage());
        if (request.getFaculty() != null) procurement.setFaculty(request.getFaculty());
        if (request.getDepartment() != null) procurement.setDepartment(request.getDepartment());
        if (request.getRequisitionType() != null) procurement.setRequisitionType(request.getRequisitionType());
        if (request.getCurrentStockBalance() != null) procurement.setCurrentStockBalance(request.getCurrentStockBalance());
        if (request.getFundingSource() != null) procurement.setFundingSource(request.getFundingSource());
        if (request.getStatus() != null) procurement.setStatus(resolveStatus(request.getStatus()));

        // Downstream workflow fields can also be corrected while still in DRAFT
        if (request.getBudgetCode() != null) procurement.setBudgetCode(request.getBudgetCode());
        if (request.getSupplierName() != null) procurement.setSupplierName(request.getSupplierName());
        if (request.getPoNumber() != null) procurement.setPoNumber(request.getPoNumber());
        if (request.getGrnNumber() != null) procurement.setGrnNumber(request.getGrnNumber());
        if (request.getInvoiceNumber() != null) procurement.setInvoiceNumber(request.getInvoiceNumber());
        if (request.getInvoiceAmount() != null) procurement.setInvoiceAmount(request.getInvoiceAmount());
        if (request.getRejectionReason() != null) procurement.setRejectionReason(request.getRejectionReason());

        Procurement updated = procurementRepository.save(procurement);
        log.info("Procurement updated successfully");

        return mapToProcurementResponse(updated);
    }

    public ProcurementStatus resolveStatus(String status) {
        String normalized = status.trim()
                .toUpperCase()
                .replace('-', '_')
                .replace(' ', '_');

        if ("FUNDS_VERIFIED".equals(normalized)) {
            return ProcurementStatus.FUNDS_VERIFIED;
        }
        if ("PENDING_FUND_VERIFICATION".equals(normalized) || "DRAFT".equals(normalized)) {
            return ProcurementStatus.DRAFT;
        }
        if ("BIDDING_OPEN".equals(normalized)) {
            return ProcurementStatus.PUBLISHED;
        }
        if ("TECHNICAL_EVALUATION".equals(normalized)) {
            return ProcurementStatus.UNDER_EVALUATION;
        }
        if ("AUTHORITY_APPROVAL".equals(normalized)) {
            return ProcurementStatus.EVALUATED;
        }

        for (ProcurementStatus value : ProcurementStatus.values()) {
            if (value.name().equals(normalized) || value.getDisplayName().equalsIgnoreCase(status.trim())) {
                return value;
            }
        }

        throw new RuntimeException("Invalid procurement status: " + status);
    }

    /**
     * Set the budget code - performed by the Bursar
     */
    public ProcurementResponse assignBudgetCode(Long procurementId, String budgetCode) {
        log.info("Assigning budget code to procurement: {}", procurementId);

        Procurement procurement = procurementRepository.findById(procurementId)
                .orElseThrow(() -> new RuntimeException("Procurement not found"));

        procurement.setBudgetCode(budgetCode);

        Procurement updated = procurementRepository.save(procurement);
        log.info("Budget code assigned successfully");

        return mapToProcurementResponse(updated);
    }

    /**
     * Record the Purchase Order details - performed by the Supplies Division (SDC)
     */
    public ProcurementResponse recordPurchaseOrder(Long procurementId, String poNumber, String supplierName) {
        log.info("Recording purchase order for procurement: {}", procurementId);

        Procurement procurement = procurementRepository.findById(procurementId)
                .orElseThrow(() -> new RuntimeException("Procurement not found"));

        procurement.setPoNumber(poNumber);
        procurement.setSupplierName(supplierName);

        Procurement updated = procurementRepository.save(procurement);
        log.info("Purchase order recorded successfully");

        return mapToProcurementResponse(updated);
    }

    /**
     * Record the GRN (Goods Received Note) number - performed by the Storekeeper
     */
    public ProcurementResponse recordGrn(Long procurementId, String grnNumber) {
        log.info("Recording GRN for procurement: {}", procurementId);

        Procurement procurement = procurementRepository.findById(procurementId)
                .orElseThrow(() -> new RuntimeException("Procurement not found"));

        procurement.setGrnNumber(grnNumber);

        Procurement updated = procurementRepository.save(procurement);
        log.info("GRN recorded successfully");

        return mapToProcurementResponse(updated);
    }

    /**
     * Record invoice details - performed by the Finance Division
     */
    public ProcurementResponse recordInvoice(Long procurementId, String invoiceNumber, BigDecimal invoiceAmount) {
        log.info("Recording invoice for procurement: {}", procurementId);

        Procurement procurement = procurementRepository.findById(procurementId)
                .orElseThrow(() -> new RuntimeException("Procurement not found"));

        procurement.setInvoiceNumber(invoiceNumber);
        procurement.setInvoiceAmount(invoiceAmount);

        Procurement updated = procurementRepository.save(procurement);
        log.info("Invoice recorded successfully");

        return mapToProcurementResponse(updated);
    }

    /**
     * Retrieve procurement by ID
     */
    @Transactional(readOnly = true)
    public ProcurementResponse getProcurementById(Long procurementId) {
        Procurement procurement = procurementRepository.findById(procurementId)
                .orElseThrow(() -> new RuntimeException("Procurement not found"));
        return mapToProcurementResponse(procurement);
    }

    /**
     * List all procurements with pagination
     */
    @Transactional(readOnly = true)
    public Page<ProcurementResponse> getAllProcurements(Pageable pageable) {
        return procurementRepository.findAll(pageable).map(this::mapToProcurementResponse);
    }

    /**
     * List procurements by status
     */
    @Transactional(readOnly = true)
    public Page<ProcurementResponse> getProcurementsByStatus(ProcurementStatus status, Pageable pageable) {
        return procurementRepository.findByStatus(status, pageable).map(this::mapToProcurementResponse);
    }

    /**
     * Publish procurement and send RFQs
     */
    public ProcurementResponse publishProcurement(Long procurementId, PublishProcurementRequest request) {
        log.info("Publishing procurement: {}", procurementId);

        Procurement procurement = procurementRepository.findById(procurementId)
                .orElseThrow(() -> new RuntimeException("Procurement not found"));

        if (!ProcurementStatus.DRAFT.equals(procurement.getStatus())) {
            throw new RuntimeException("Only DRAFT procurements can be published");
        }

        procurement.setStatus(ProcurementStatus.PUBLISHED);
        procurement.setPublishedDate(LocalDateTime.now());

        // Send RFQs to suppliers
        if (request.getRfqRecipientEmails() != null && !request.getRfqRecipientEmails().isEmpty()) {
            for (String email : request.getRfqRecipientEmails()) {
                RfqRecipient recipient = RfqRecipient.builder()
                        .procurement(procurement)
                        .supplierEmail(email)
                        .invitationStatus("SENT")
                        .invitationSentDate(LocalDateTime.now())
                        .build();
                rfqRecipientRepository.save(recipient);
                log.info("RFQ sent to: {}", email);
            }
        }

        // Post on promise.lk if required
        if (Boolean.TRUE.equals(request.getPostOnPromiseLk())) {
            postOnPromiseLk(procurement);
        }

        Procurement updated = procurementRepository.save(procurement);
        return mapToProcurementResponse(updated);
    }

    /**
     * Add RFQ recipient
     */
    public RfqRecipientResponse addRfqRecipient(Long procurementId, RfqRecipientRequest request) {
        log.info("Adding RFQ recipient for procurement: {}", procurementId);

        Procurement procurement = procurementRepository.findById(procurementId)
                .orElseThrow(() -> new RuntimeException("Procurement not found"));

        RfqRecipient recipient = RfqRecipient.builder()
                .procurement(procurement)
                .supplierEmail(request.getSupplierEmail())
                .supplierName(request.getSupplierName())
                .supplierId(request.getSupplierId())
                .invitationStatus("PENDING")
                .responseStatus("NOT_RESPONDED")
                .build();

        RfqRecipient saved = rfqRecipientRepository.save(recipient);
        log.info("RFQ recipient added successfully");

        return mapToRfqRecipientResponse(saved);
    }

    /**
     * Get RFQ recipients for a procurement
     */
    @Transactional(readOnly = true)
    public List<RfqRecipientResponse> getRfqRecipients(Long procurementId) {
        return rfqRecipientRepository.findByProcurementId(procurementId)
                .stream()
                .map(this::mapToRfqRecipientResponse)
                .collect(Collectors.toList());
    }

    /**
     * Post procurement on promise.lk
     */
    private void postOnPromiseLk(Procurement procurement) {
        log.info("Posting procurement to promise.lk: {}", procurement.getReferenceNumber());

        PromiseLkPost post = PromiseLkPost.builder()
                .procurement(procurement)
                .postingDate(java.time.LocalDate.now())
                .postingStatus("POSTED")
                .isVerified(false)
                .build();

        promiseLkPostRepository.save(post);
        log.info("Promise.lk post created");
    }

    /**
     * Generate unique reference number
     */
    private String generateReferenceNumber() {
        return "PROC-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Map entity to DTO - includes every field on the Procurement entity
     */
    private ProcurementResponse mapToProcurementResponse(Procurement procurement) {
        return ProcurementResponse.builder()
                .id(procurement.getId())
                .referenceNumber(procurement.getReferenceNumber())
                .title(procurement.getTitle())
                .description(procurement.getDescription())
                .estimatedValue(procurement.getEstimatedValue())
                .procurementMethodName(procurement.getProcurementMethod().getMethodName())
                .categoryName(procurement.getProcurementCategory().getCategoryName())
                .status(procurement.getStatus().getDisplayName())
                .approvalLevel(procurement.getApprovalLevel())
                .openingDate(procurement.getOpeningDate())
                .closingDate(procurement.getClosingDate())
                .documentFee(procurement.getDocumentFee())
                .requiresBidBond(procurement.getRequiresBidBond())
                .bidBondPercentage(procurement.getBidBondPercentage())
                .createdByUserId(procurement.getCreatedByUserId())
                .createdDate(procurement.getCreatedDate())
                .updatedDate(procurement.getUpdatedDate())
                .isActive(procurement.getIsActive())
                .publishedDate(procurement.getPublishedDate())
                .faculty(procurement.getFaculty())
                .department(procurement.getDepartment())
                .requisitionType(procurement.getRequisitionType())
                .currentStockBalance(procurement.getCurrentStockBalance())
                .fundingSource(procurement.getFundingSource())
                .budgetCode(procurement.getBudgetCode())
                .supplierName(procurement.getSupplierName())
                .poNumber(procurement.getPoNumber())
                .grnNumber(procurement.getGrnNumber())
                .invoiceNumber(procurement.getInvoiceNumber())
                .invoiceAmount(procurement.getInvoiceAmount())
                .rejectionReason(procurement.getRejectionReason())
                .build();
    }

    private RfqRecipientResponse mapToRfqRecipientResponse(RfqRecipient recipient) {
        return RfqRecipientResponse.builder()
                .id(recipient.getId())
                .supplierEmail(recipient.getSupplierEmail())
                .supplierName(recipient.getSupplierName())
                .supplierId(recipient.getSupplierId())
                .invitationSentDate(recipient.getInvitationSentDate())
                .invitationStatus(recipient.getInvitationStatus())
                .responseStatus(recipient.getResponseStatus())
                .bidSubmittedDate(recipient.getBidSubmittedDate())
                .build();
    }

    /**
     * Delete procurement (only if DRAFT)
     */
    public void deleteProcurement(Long procurementId) {
        log.info("Deleting procurement: {}", procurementId);

        Procurement procurement = procurementRepository.findById(procurementId)
                .orElseThrow(() -> new RuntimeException("Procurement not found"));

        if (!ProcurementStatus.DRAFT.equals(procurement.getStatus())) {
            throw new RuntimeException("Can only delete procurements in DRAFT status");
        }

        procurementRepository.delete(procurement);
        log.info("Procurement deleted successfully");
    }
}
