package org.example.ecommerc_shop.common;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PICKING,
    SHIPPING,
    DELIVERED
    ,FAILED,
    RETURNING,
    REATTEMPT,
    CANCELLED,
    REJECTED;

    public boolean canTransitionTo(OrderStatus nextStatus) {
        switch (this) {
            case PENDING:
                return nextStatus == CONFIRMED;
            case CONFIRMED:
                return nextStatus == PICKING;
            case PICKING:
                return nextStatus == SHIPPING;
            case SHIPPING:
                return nextStatus == DELIVERED
                        || nextStatus == FAILED;
            case FAILED:
                return nextStatus == RETURNING
                        || nextStatus == REATTEMPT;
            case DELIVERED:
            case RETURNING:
            case REATTEMPT:
                return false;
            default:
                return false;
        }
    }
}
