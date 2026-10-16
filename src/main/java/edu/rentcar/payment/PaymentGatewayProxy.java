package edu.rentcar.payment;

public final class PaymentGatewayProxy implements PaymentGateway {

    private final PaymentGateway delegate;
    private final CustomerVerificationService verificationService;

    public PaymentGatewayProxy(
            PaymentGateway delegate,
            CustomerVerificationService verificationService
    ) {
        if (delegate == null) {
            throw new IllegalArgumentException(
                    "El Proxy necesita una pasarela de pago para delegar.");
        }
        if (verificationService == null) {
            throw new IllegalArgumentException(
                    "El Proxy necesita un servicio para verificar al cliente.");
        }

        this.delegate = delegate;
        this.verificationService = verificationService;
    }

    @Override
    public PaymentResult process(PaymentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "No se puede verificar ni procesar una solicitud de pago nula.");
        }

        try {
            boolean authorized =
                    verificationService.isAuthorized(request.customerId());

            if (!authorized) {
                return new PaymentResult(
                        PaymentResult.Status.DECLINED,
                        "Cliente no autorizado; no se envió el pago."
                );
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return verificationFailed();
        } catch (Exception exception) {
            return verificationFailed();
        }

        return delegate.process(request);
    }

    private PaymentResult verificationFailed() {
        return new PaymentResult(
                PaymentResult.Status.FAILED,
                "No fue posible verificar la autorización del cliente; no se envió el pago."
        );
    }
}