package edu.rentcar.controller;

import edu.rentcar.data.SampleData;
import edu.rentcar.app.ApplicationServices;
import edu.rentcar.model.Customer;
import edu.rentcar.model.Vehicle;
import edu.rentcar.model.factory.RentalModeFactory;
import edu.rentcar.model.factory.BasicModeFactory;
import edu.rentcar.model.factory.PremiumModeFactory;
import edu.rentcar.model.factory.AllRiskModeFactory;
import edu.rentcar.model.RentalRequest;
import edu.rentcar.reservation.ReservationWorkflowResult;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.util.StringConverter;
import java.time.LocalDate;

public class ReservationController {

    @FXML private ComboBox<Customer> customerCombo;
    @FXML private ComboBox<Vehicle> vehicleCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> rentalModeCombo;

    @FXML private CheckBox gpsCheck;
    @FXML private CheckBox babySeatCheck;
    @FXML private CheckBox additionalDriverCheck;

    @FXML private Label totalRateLabel;
    @FXML private Button confirmReservationButton;

    @FXML
    private void onConfirmReservation(ActionEvent event) {
        Customer customer = customerCombo.getValue();
        Vehicle vehicle = vehicleCombo.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String mode = rentalModeCombo.getValue();

        if (customer == null || vehicle == null || startDate == null || endDate == null || mode == null) {
            showAlert(Alert.AlertType.WARNING, "Incomplete Form", "Please fill in all mandatory fields.");
            return;
        }

        java.util.List<String> services = new java.util.ArrayList<>();
        if (gpsCheck.isSelected()) services.add("GPS");
        if (babySeatCheck.isSelected()) services.add("BABY_SEAT");
        if (additionalDriverCheck.isSelected()) services.add("ADDITIONAL_DRIVER");

        try {
            // Nota: Si te sigue marcando rojo 'reservationWorkflowFacade()', mira el paso 2 de abajo
            ReservationWorkflowResult result = ApplicationServices.reservationWorkflowFacade()
                    .createReservation(customer, vehicle, startDate, endDate, mode, services, "tkn_test_12345");

            if ("CONFIRMED".equals(result.reservation().getStatus())) {
                showAlert(Alert.AlertType.INFORMATION, "Reservation Confirmed",
                        "Successfully reserved! ID: " + result.reservation().getId() +
                                "\nTotal Paid: $" + result.total());
            } else {
                showAlert(Alert.AlertType.ERROR, "Payment Failed", "The credit card transaction was rejected.");
            }

        } catch (IllegalArgumentException | IllegalStateException ex) {
            showAlert(Alert.AlertType.ERROR, "Business Rule Error", ex.getMessage());
        }
    }

    // Este es el método que te faltaba y por eso salía en rojo 'showAlert'
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
