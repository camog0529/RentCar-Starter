package edu.rentcar.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;

/**
 * Handles the application shell and navigation between independent FXML views.
 */
public class MainController {

    @FXML
    private StackPane contentPane;
    @FXML
    private Label pageTitle;

    @FXML
    public void initialize() {
        showDashboard();
    }

    @FXML
    private void showDashboard() {
        loadView("dashboard-view.fxml", "Dashboard");
    }

    @FXML
    private void showCustomers() {
        loadView("customer-view.fxml", "Customers");
    }

    @FXML
    private void showVehicles() {
        loadView("vehicle-view.fxml", "Vehicles");
    }

    @FXML
    private void showReservations() {
        loadView("reservation-view.fxml", "Reservations");
    }

    private void loadView(String viewFile, String title) {
        URL viewUrl = getClass().getResource("/edu/rentcar/view/" + viewFile);
        if (viewUrl == null) {
            showNavigationError("Could not find view: " + viewFile);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(viewUrl);
            Parent view = loader.load();
            contentPane.getChildren().setAll(view);
            pageTitle.setText(title);

            // 🛠️ INYECCIÓN EN TIEMPO DE REAL: Refresca los ComboBox y tablas de forma dinámica
            Object controller = loader.getController();
            if (controller instanceof edu.rentcar.controller.ReservationController) {
                ((edu.rentcar.controller.ReservationController) controller).initialize();
            } else if (controller instanceof edu.rentcar.controller.CustomerController) {
                ((edu.rentcar.controller.CustomerController) controller).initialize();
            }

        } catch (IOException exception) {
            showNavigationError("Could not load " + viewFile + ": " + exception.getMessage());
        }
    }

    private void showNavigationError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Navigation Error");
        alert.setHeaderText("The requested screen could not be opened.");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
