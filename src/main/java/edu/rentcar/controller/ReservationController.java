package edu.rentcar.controller;

import edu.rentcar.data.SampleData;
import edu.rentcar.app.ApplicationServices;
import edu.rentcar.model.Customer;
import edu.rentcar.model.Vehicle;
import edu.rentcar.reservation.ReservationWorkflowResult;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.time.LocalDate;

public class ReservationController {

    @FXML private ComboBox<Customer> customerCombo;
    @FXML private ComboBox<Vehicle> vehicleCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> rentalModeCombo;

    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private ComboBox<String> paymentStatusCombo;

    @FXML private CheckBox gpsCheck;
    @FXML private CheckBox babySeatCheck;
    @FXML private CheckBox additionalDriverCheck;
    @FXML private Button confirmReservationButton;
    @FXML private Label totalRateLabel;

    // Tabla e Historial de Reservas
    @FXML private TableView<ReservationWorkflowResult> historyTable;
    @FXML private TableColumn<ReservationWorkflowResult, String> idCol;
    @FXML private TableColumn<ReservationWorkflowResult, String> customerCol;
    @FXML private TableColumn<ReservationWorkflowResult, String> statusCol;
    @FXML private TableColumn<ReservationWorkflowResult, String> totalCol;

    @FXML
    public void initialize() {
        customerCombo.getItems().setAll(ApplicationServices.CUSTOMER_REPOSITORY.findAll());
        vehicleCombo.getItems().setAll(SampleData.vehicles());
        rentalModeCombo.getItems().setAll("BASIC", "PREMIUM", "ALL_RISK");
        paymentMethodCombo.getItems().setAll("CREDIT_CARD", "DEBIT_CARD");
        paymentStatusCombo.getItems().setAll("APPROVED", "DECLINED");
        paymentStatusCombo.setValue("APPROVED");

        // Enlace de datos de las celdas de la tabla
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().reservation().getId()));

        customerCol.setCellValueFactory(data -> {
            String customerId = data.getValue().reservation().getCustomerId();
            Customer matchedCustomer = ApplicationServices.CUSTOMER_REPOSITORY.findAll().stream()
                    .filter(customer -> customer.getId().equals(customerId))
                    .findFirst()
                    .orElse(null);

            return new SimpleStringProperty(
                    matchedCustomer == null ? customerId : matchedCustomer.toString());
        });

        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().reservation().getStatus()));
        totalCol.setCellValueFactory(data -> new SimpleStringProperty("$" + data.getValue().total().toString()));

        // Listeners para calcular el precio en vivo al cambiar opciones
        java.util.List.of(customerCombo, vehicleCombo, startDatePicker, endDatePicker, rentalModeCombo).forEach(control -> {
            if (control instanceof ComboBox) ((ComboBox<?>) control).setOnAction(e -> calculateLivePrice());
            else if (control instanceof DatePicker) ((DatePicker) control).setOnAction(e -> calculateLivePrice());
        });
    }

    private void calculateLivePrice() {
        if (vehicleCombo.getValue() != null && startDatePicker.getValue() != null && endDatePicker.getValue() != null && rentalModeCombo.getValue() != null) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(startDatePicker.getValue(), endDatePicker.getValue());
            if (days > 0) {
                java.math.BigDecimal totalEstimated = java.math.BigDecimal.valueOf(vehicleCombo.getValue().getDailyRate() * days);
                if (totalRateLabel != null) {
                    totalRateLabel.setText("Estimated Cost: $" + totalEstimated);
                    totalRateLabel.setVisible(true);
                }
            }
        }
    }

    @FXML
    private void onConfirmReservation(ActionEvent event) {
        Customer customer = customerCombo.getValue();
        Vehicle vehicle = vehicleCombo.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String mode = rentalModeCombo.getValue();
        String paymentMethod = paymentMethodCombo.getValue();
        String selectedStatus = paymentStatusCombo.getValue();

        if (customer == null || vehicle == null || startDate == null || endDate == null || mode == null || paymentMethod == null || selectedStatus == null) {
            showAlert(Alert.AlertType.WARNING, "Incomplete Form", "Please fill in all mandatory fields.");
            return;
        }

        String tokenInput = "APPROVED".equals(selectedStatus) ? "tok_approved" : "tok_declined";

        java.util.List<String> services = new java.util.ArrayList<>();
        if (gpsCheck.isSelected()) services.add("GPS");
        if (babySeatCheck.isSelected()) services.add("BABY_SEAT");
        if (additionalDriverCheck.isSelected()) services.add("ADDITIONAL_DRIVER");

        try {
            ReservationWorkflowResult result = ApplicationServices.reservationWorkflowFacade()
                    .createReservation(customer, vehicle, startDate, endDate, mode, services, tokenInput);

            if ("CONFIRMED".equals(result.reservation().getStatus())) {
                showAlert(Alert.AlertType.INFORMATION, "Reservation Confirmed",
                        "Successfully reserved! ID: " + result.reservation().getId() +
                                "\nMétodo: " + paymentMethod + "\nTotal Paid: $" + result.total());

                if (historyTable != null) {
                    historyTable.getItems().add(result);
                }

                clearFields();
            } else {
                showAlert(Alert.AlertType.ERROR, "Payment Not Completed", result.paymentResult().message());
            }

        } catch (IllegalArgumentException | IllegalStateException ex) {
            showAlert(Alert.AlertType.ERROR, "Business Rule Error", ex.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        customerCombo.setValue(null);
        vehicleCombo.setValue(null);
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        rentalModeCombo.setValue(null);
        paymentMethodCombo.setValue(null);
        paymentStatusCombo.setValue("APPROVED");
        gpsCheck.setSelected(false);
        babySeatCheck.setSelected(false);
        additionalDriverCheck.setSelected(false);
        if (totalRateLabel != null) {
            totalRateLabel.setVisible(false);
        }
    }

    // Este método se encargará de refrescar los datos de la pantalla en vivo
    public void refreshData() {
        if (customerCombo != null) {
            customerCombo.getItems().setAll(ApplicationServices.CUSTOMER_REPOSITORY.findAll());
        }
        if (vehicleCombo != null) {
            vehicleCombo.getItems().setAll(edu.rentcar.data.SampleData.vehicles());
        }
    }
}
