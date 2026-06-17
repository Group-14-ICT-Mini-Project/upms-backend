package com.v1.bidding.model;

public enum BidStatus {
    DRAFT("Draft"),
    SUBMITTED("Submitted"),
    RECEIVED("Received"),
    ACKNOWLEDGED("Acknowledged"),
    UNDER_EVALUATION("Under Evaluation"),
    EVALUATED("Evaluated"),
    REJECTED("Rejected"),
    ACCEPTED("Accepted"),
    AWARDED("Awarded");

    private final String displayName;

    BidStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
