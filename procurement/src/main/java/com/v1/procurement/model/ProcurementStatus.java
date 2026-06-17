package com.v1.procurement.model;

public enum ProcurementStatus {
    DRAFT("Draft"),
    PUBLISHED("Published"),
    OPEN("Open for Bidding"),
    CLOSING_SOON("Closing Soon"),
    CLOSED("Bid Closing Completed"),
    UNDER_EVALUATION("Under Evaluation"),
    EVALUATED("Evaluation Completed"),
    SUPPLIER_SELECTED("Supplier Selected"),
    PURCHASE_ORDER_ISSUED("Purchase Order Issued"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    SUSPENDED("Suspended");

    private final String displayName;

    ProcurementStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
