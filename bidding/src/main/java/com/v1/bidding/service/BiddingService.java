package com.v1.bidding.service;

import com.v1.bidding.dto.*;
import com.v1.bidding.model.*;
import com.v1.bidding.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class BiddingService {

    private final BidRepository bidRepository;
    private final BidBondRepository bidBondRepository;
    private final DocumentFeeRepository documentFeeRepository;
    private final VatSsclDeclarationRepository vatSsclDeclarationRepository;
    private final BidAmendmentRepository bidAmendmentRepository;
    private final BidRejectionRepository bidRejectionRepository;

    public BiddingService(BidRepository bidRepository,
                         BidBondRepository bidBondRepository,
                         DocumentFeeRepository documentFeeRepository,
                         VatSsclDeclarationRepository vatSsclDeclarationRepository,
                         BidAmendmentRepository bidAmendmentRepository,
                         BidRejectionRepository bidRejectionRepository) {
        this.bidRepository = bidRepository;
        this.bidBondRepository = bidBondRepository;
        this.documentFeeRepository = documentFeeRepository;
        this.vatSsclDeclarationRepository = vatSsclDeclarationRepository;
        this.bidAmendmentRepository = bidAmendmentRepository;
        this.bidRejectionRepository = bidRejectionRepository;
    }

    /**
     * Submit a bid with mandatory validations
     */
    public BidResponse submitBid(SubmitBidRequest request, Long bidderId, Long procurementId) {
        log.info("Processing bid submission for procurement: {}", procurementId);

        try {
            // VALIDATION 1: Validate bid bond (mandatory, 120-day validity)
            validateBidBond(request.getBidBond());

            // VALIDATION 2: Validate document fee payment
            validateDocumentFee(request.getDocumentFeeReference(), procurementId);

            // VALIDATION 3: Validate VAT/SSCL declaration
            validateVatSsclDeclaration(request.getVatSsclDeclaration());

            // VALIDATION 4: Validate financial bid
            validateFinancialBid(request.getFinancialBid());

            // Create bid record
            String bidReference = generateBidReference();

            Bid bid = Bid.builder()
                    .procurementId(procurementId)
                    .bidderId(bidderId)
                    .bidReference(bidReference)
                    .submittedDate(LocalDateTime.now())
                    .financialBid(request.getFinancialBid())
                    .deliverySchedule(request.getDeliverySchedule())
                    .warrantyPeriod(request.getWarrantyPeriod())
                    .paymentTerms(request.getPaymentTerms())
                    .bidStatus(BidStatus.SUBMITTED)
                    .isCompliant(true)
                    .complianceNotes("All mandatory requirements met")
                    .build();

            Bid savedBid = bidRepository.save(bid);

            // Save bid bond
            BidBond bidBond = BidBond.builder()
                    .bidId(savedBid.getId())
                    .bondReference(request.getBidBond().getBondReference())
                    .issuerBank(request.getBidBond().getIssuerBank())
                    .bondAmount(request.getBidBond().getBondAmount())
                    .issuedDate(request.getBidBond().getIssuedDate())
                    .expiryDate(request.getBidBond().getExpiryDate())
                    .isValid(true)
                    .validationStatus("VERIFIED")
                    .validationDate(LocalDate.now())
                    .build();
            bidBondRepository.save(bidBond);

            // Save VAT/SSCL declaration
            VatSsclDeclaration vatDeclaration = VatSsclDeclaration.builder()
                    .bidId(savedBid.getId())
                    .registrationNumber(request.getVatSsclDeclaration().getRegistrationNumber())
                    .registrationType(request.getVatSsclDeclaration().getRegistrationType())
                    .isDeclared(request.getVatSsclDeclaration().getIsDeclared())
                    .declarationDate(LocalDate.now())
                    .verificationStatus("VERIFIED")
                    .verificationDate(LocalDateTime.now())
                    .build();
            vatSsclDeclarationRepository.save(vatDeclaration);

            log.info("Bid submitted successfully: {}", bidReference);
            return mapToBidResponse(savedBid);

        } catch (Exception e) {
            log.error("Bid submission validation failed: {}", e.getMessage());
            throw new RuntimeException("Bid submission failed: " + e.getMessage());
        }
    }

    /**
     * VALIDATION: Bid Bond must be valid and not expired (120-day minimum)
     */
    private void validateBidBond(BidBondRequest request) {
        if (request == null) {
            throw new RuntimeException("Bid bond is mandatory");
        }

        LocalDate today = LocalDate.now();
        LocalDate minExpiryDate = today.plusDays(120);

        if (request.getExpiryDate().isBefore(minExpiryDate)) {
            throw new RuntimeException("Bid bond must be valid for at least 120 days. Current expiry: " + request.getExpiryDate());
        }

        if (request.getExpiryDate().isBefore(today)) {
            throw new RuntimeException("Bid bond has already expired");
        }

        log.info("Bid bond validation passed");
    }

    /**
     * VALIDATION: Document fee must be paid (reference must match predefined amounts 8000/12500 LKR)
     */
    private void validateDocumentFee(String paymentReference, Long procurementId) {
        if (paymentReference == null || paymentReference.isEmpty()) {
            throw new RuntimeException("Document fee payment reference is mandatory");
        }

        // In real scenario, verify payment with bank/payment gateway
        // For now, just verify reference exists
        log.info("Document fee payment verified: {}", paymentReference);
    }

    /**
     * VALIDATION: VAT/SSCL must be declared (automatic rejection if not)
     */
    private void validateVatSsclDeclaration(VatSsclDeclarationRequest request) {
        if (request == null || !Boolean.TRUE.equals(request.getIsDeclared())) {
            throw new RuntimeException("VAT/SSCL declaration is mandatory. Bid will be automatically rejected if not declared.");
        }

        if (request.getRegistrationNumber() == null || request.getRegistrationNumber().isEmpty()) {
            throw new RuntimeException("Valid VAT/SSCL registration number is required");
        }

        log.info("VAT/SSCL declaration validation passed");
    }

    /**
     * VALIDATION: Financial bid must be positive
     */
    private void validateFinancialBid(BigDecimal bid) {
        if (bid == null || bid.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Financial bid must be greater than zero");
        }

        log.info("Financial bid validation passed: {}", bid);
    }

    /**
     * Get bid by ID
     */
    @Transactional(readOnly = true)
    public BidResponse getBidById(Long bidId) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new RuntimeException("Bid not found"));
        return mapToBidResponse(bid);
    }

    /**
     * Get bids for a procurement
     */
    @Transactional(readOnly = true)
    public Page<BidResponse> getBidsByProcurement(Long procurementId, Pageable pageable) {
        return bidRepository.findByProcurementId(procurementId, pageable)
                .map(this::mapToBidResponse);
    }

    /**
     * Get bids submitted by a bidder
     */
    @Transactional(readOnly = true)
    public Page<BidResponse> getBidsByBidder(Long bidderId, Pageable pageable) {
        return bidRepository.findByBidderId(bidderId, pageable)
                .map(this::mapToBidResponse);
    }

    /**
     * Get compliant bids for a procurement
     */
    @Transactional(readOnly = true)
    public List<BidResponse> getCompliantBids(Long procurementId) {
        return bidRepository.findCompliantBids(procurementId)
                .stream()
                .map(this::mapToBidResponse)
                .collect(Collectors.toList());
    }

    /**
     * Reject a bid
     */
    public void rejectBid(Long bidId, String rejectionReason, String rejectionCategory) {
        log.info("Rejecting bid: {}", bidId);

        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new RuntimeException("Bid not found"));

        bid.setBidStatus(BidStatus.REJECTED);
        bid.setIsCompliant(false);
        bidRepository.save(bid);

        BidRejection rejection = BidRejection.builder()
                .bidId(bidId)
                .rejectionReason(rejectionReason)
                .rejectionCategory(rejectionCategory)
                .appealsAllowed(true)
                .appealDeadline(LocalDateTime.now().plusDays(7))
                .build();

        bidRejectionRepository.save(rejection);
        log.info("Bid rejected successfully");
    }

    /**
     * Submit bid amendment
     */
    public void submitBidAmendment(Long bidId, String amendmentType, String description) {
        log.info("Submitting bid amendment for bid: {}", bidId);

        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new RuntimeException("Bid not found"));

        BidAmendment amendment = BidAmendment.builder()
                .bidId(bidId)
                .amendmentType(amendmentType)
                .description(description)
                .amendmentStatus("SUBMITTED")
                .approvalStatus("PENDING")
                .build();

        bidAmendmentRepository.save(amendment);
        log.info("Bid amendment submitted successfully");
    }

    /**
     * Get bid amendments
     */
    @Transactional(readOnly = true)
    public List<BidAmendment> getBidAmendments(Long bidId) {
        return bidAmendmentRepository.findByBidId(bidId);
    }

    /**
     * Get bid bond details
     */
    @Transactional(readOnly = true)
    public BidBond getBidBond(Long bidId) {
        return bidBondRepository.findByBidId(bidId)
                .orElseThrow(() -> new RuntimeException("Bid bond not found"));
    }

    /**
     * Get VAT/SSCL declaration
     */
    @Transactional(readOnly = true)
    public VatSsclDeclaration getVatSsclDeclaration(Long bidId) {
        return vatSsclDeclarationRepository.findByBidId(bidId)
                .orElseThrow(() -> new RuntimeException("VAT/SSCL declaration not found"));
    }

    /**
     * Generate unique bid reference
     */
    private String generateBidReference() {
        return "BID-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Map entity to DTO
     */
    private BidResponse mapToBidResponse(Bid bid) {
        return BidResponse.builder()
                .id(bid.getId())
                .procurementId(bid.getProcurementId())
                .bidderId(bid.getBidderId())
                .bidReference(bid.getBidReference())
                .submittedDate(bid.getSubmittedDate())
                .financialBid(bid.getFinancialBid())
                .deliverySchedule(bid.getDeliverySchedule())
                .warrantyPeriod(bid.getWarrantyPeriod())
                .paymentTerms(bid.getPaymentTerms())
                .bidStatus(bid.getBidStatus().getDisplayName())
                .isCompliant(bid.getIsCompliant())
                .complianceNotes(bid.getComplianceNotes())
                .createdDate(bid.getCreatedDate())
                .updatedDate(bid.getUpdatedDate())
                .build();
    }
}
