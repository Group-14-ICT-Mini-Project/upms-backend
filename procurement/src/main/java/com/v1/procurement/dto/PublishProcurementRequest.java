package com.v1.procurement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to publish procurement and send RFQs")
public class PublishProcurementRequest {

    @NotNull(message = "Publish flag required")
    @Schema(description = "Whether to publish", example = "true")
    private Boolean publish;

    @Schema(description = "List of RFQ recipient emails")
    private List<String> rfqRecipientEmails;

    @Schema(description = "Whether to post on promise.lk")
    private Boolean postOnPromiseLk;

    @Schema(description = "Whether to publish in newspaper")
    private Boolean publishInNewspaper;
}
