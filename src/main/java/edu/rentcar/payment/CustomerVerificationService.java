package edu.rentcar.payment;

@FunctionalInterface
public interface CustomerVerificationService {
    boolean isAuthorized(String customerId) throws Exception;
}