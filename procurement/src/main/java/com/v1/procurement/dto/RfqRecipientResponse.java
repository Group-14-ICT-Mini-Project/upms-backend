package com.v1.procurement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response for RFQ recipient")
public class RfqRecipientResponse {

    @Schema(description = "Recipient ID")
    private Long id;

    @Schema(description = "Supplier email")
    private String supplierEmail;

    @Schema(description = "Supplier name")
    private String supplierName;

    @Schema(description = "Supplier ID")
    private String supplierId;

    @Schema(description = "Invitation sent date")
    private LocalDateTime invitationSentDate;

    @Schema(description = "Invitation status")
    private String invitationStatus;

    @Schema(description = "Response status")
    private String responseStatus;

    @Schema(description = "Bid submitted date")
    private LocalDateTime bidSubmittedDate;
}
