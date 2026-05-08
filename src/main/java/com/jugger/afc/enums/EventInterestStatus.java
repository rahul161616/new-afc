package com.jugger.afc.enums;

public enum EventInterestStatus {
    INTERESTED,
    CONFIRMED,
    GOING,
    MAYBE,
    NOT_AVAILABLE,
    WAITLISTED,
    DROPPED;

    public boolean countsAsConfirmed() {
        return this == CONFIRMED || this == GOING;
    }

    public EventInterestStatus toApiStatus() {
        return this == GOING ? CONFIRMED : this;
    }
}
