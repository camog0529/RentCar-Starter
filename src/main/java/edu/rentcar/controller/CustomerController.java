package edu.rentcar.controller;

import edu.rentcar.app.ApplicationServices;
import edu.rentcar.model.Customer;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class CustomerController {

    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> authorizedCombo;

    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> idCol;
    @FXML private TableColumn<Customer, String> nameCol;
    @FXML private TableColumn<Customer, String> emailCol;
    @FXML private TableColumn<Customer, String> authorizedCol;

    @FXML
    public void initialize() {
        authorizedCombo.getItems().setAll("true", "false");
        authorizedCombo.setValue("true");

        // Mapeo estricto de columnas utilizando getters de la clase Customer
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFullName()));
        emailCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        authorizedCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().isAuthorized())));

        // Cargar los registros iniciales del repositorio en la tabla
        customerTable.getItems().setAll(ApplicationServices.CUSTOMER_REPOSITORY.findAll());
    }

    @FXML
    private void onRegisterCustomer(ActionEvent event) {
        String id = idField.getText();
        String name = nameField.getText();
        String email = emailField.getText();
        String authStr = authorizedCombo.getValue();

        if (id == null || id.trim().isEmpty() || name == null || name.trim().isEmpty() || email == null || email.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Campos Incompletos", "Por favor completa todos los campos del cliente.");
            return;
        }

        boolean isAuthorized = Boolean.parseBoolean(authStr);

        // Instanciamos el modelo usando el constructor existente
        Customer newCustomer = new Customer(id.trim(), name.trim(), email.trim(), isAuthorized);

        // Guardamos en nuestro nuevo repositorio
        ApplicationServices.CUSTOMER_REPOSITORY.save(newCustomer);

        // Refrescamos la tabla de la interfaz de inmediato
        customerTable.getItems().setAll(ApplicationServices.CUSTOMER_REPOSITORY.findAll());
        onClearFields(null);

        showAlert(Alert.AlertType.INFORMATION, "Registro Exitoso", "Cliente creado e incorporado al sistema con éxito.");
    }

    @FXML
    private void onClearFields(ActionEvent event) {
        idField.clear();
        nameField.clear();
        emailField.clear();
        authorizedCombo.setValue("true");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}