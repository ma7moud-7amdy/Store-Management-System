package StoreManagement;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import javax.xml.transform.Result;
import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

public class Main extends Application {

    private static final String USERNAME = "a";
    private static final String PASSWORD = "a";

    private BorderPane mainLayout;
    private Scene mainScene;
    private Stage stage;
    public static Connection conn;

    static {
        try {
            conn = getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Order selectedOrder;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;

        // Set the application icon
        //stage.getIcons().add(new Image(getClass().getResourceAsStream("logo.png")));
        // Show login page on application start
        stage.setTitle("Login");
        Scene loginScene = createLoginScene();
        stage.setScene(loginScene);
        stage.show();
    }

    private Scene createLoginScene() {
        // Styling
        Label titleLabel = new Label("Welcome to Store Management System");
        styleLabel(titleLabel, 30, "#2c3e50", true);

        Label usernameLabel = new Label("Username:");
        styleLabel(usernameLabel, 18, "#34495e", true);

        TextField usernameField = createStyledTextField("Enter your username");

        Label passwordLabel = new Label("Password:");
        styleLabel(passwordLabel, 18, "#34495e", true);

        PasswordField passwordField = createStyledPasswordField("Enter your password");

        Label messageLabel = new Label();
        styleLabel(messageLabel, 14, "red", false);

        Button loginButton = createStyledButton("Login");
        loginButton.setMaxWidth(200);
        loginButton.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10;");
        loginButton.setOnMouseEntered(e -> loginButton.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10;"));
        loginButton.setOnMouseExited(e -> loginButton.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10;"));

        // Actions
        loginButton.setOnAction(event -> {
            String inputUsername = usernameField.getText().trim();
            String inputPassword = passwordField.getText().trim();

            if (inputUsername.isEmpty() || inputPassword.isEmpty()) {
                messageLabel.setText("Please fill in all fields!");
            } else if (inputUsername.equals(USERNAME) && inputPassword.equals(PASSWORD)) {
                stage.setTitle("Store Management System");
                initializeMainPage();
                stage.setScene(mainScene);
                stage.setMaximized(true);
            } else {
                messageLabel.setText("Invalid username or password!");
            }
        });

        VBox formLayout = new VBox(20, titleLabel, usernameLabel, usernameField, passwordLabel, passwordField, loginButton, messageLabel);
        formLayout.setAlignment(Pos.CENTER);

        StackPane cardPane = new StackPane(formLayout);
        cardPane.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-border-radius: 20; -fx-background-radius: 20; "
                + "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.25), 20, 0, 0, 10);");

        StackPane root = new StackPane(cardPane);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #e0f7fa, #ffffff);");
        root.setPadding(new Insets(20));

        return new Scene(root, 1024, 768);
    }

    private void initializeMainPage() {
        mainLayout = new BorderPane();
        VBox sidebar = createSidebar();
        mainLayout.setLeft(sidebar);

        showHomePage();

        mainScene = new Scene(mainLayout, 1024, 768);
    }

    private VBox createSidebar() {
        // Create navigation buttons
        // Styling
        Button homeButton = createStyledButton("Home");
        Button productsButton = createStyledButton("Products");
        Button ordersButton = createStyledButton("Orders");
        Button customersButton = createStyledButton("Customers");
        Button staffButton = createStyledButton("Staff");
        Button signOutButton = createStyledSignOutButton("Sign Out");

        // Style buttons and make them bold
        for (Button button : new Button[]{homeButton, productsButton, ordersButton, customersButton, staffButton}) {
            button.setMaxWidth(Double.MAX_VALUE); // Make buttons expand to fill width
            // Set button styles including bold
            button.setStyle("-fx-font-weight: bold; -fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 18px; -fx-padding: 10;");
            // Set hover effects for buttons
            button.setOnMouseEntered(e -> button.setStyle("-fx-font-weight: bold; -fx-background-color: #34495e; -fx-text-fill: white; -fx-font-size: 20px; -fx-padding: 10;"));
            button.setOnMouseExited(e -> button.setStyle("-fx-font-weight: bold; -fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 18px; -fx-padding: 10;"));
        }

        // Spacer to push the Sign-Out button to the bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS); // Allow spacer to grow and push other elements up

        // Create sidebar layout
        VBox sidebar = new VBox(15, homeButton, productsButton, ordersButton, customersButton, staffButton, spacer, signOutButton);
        sidebar.setAlignment(Pos.TOP_CENTER);
        sidebar.setStyle("-fx-background-color: #2c3e50; -fx-padding: 20;");
        sidebar.setPrefWidth(240);

        // Actions
        homeButton.setOnAction(e -> showHomePage());
        productsButton.setOnAction(e -> {
            try {
                showProductsPage();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        ordersButton.setOnAction(e -> showOrdersPage());
        customersButton.setOnAction(e -> showCustomersPage());
        staffButton.setOnAction(e -> showStaffPage());
        signOutButton.setOnAction(e -> {
            // Confirmation Dialog for Logging Out
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Logout");
            alert.setHeaderText("Are you sure you want to log out?");
            alert.setContentText("You will be redirected to the sign-in page.");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Close the current stage and go back to the login page
                    stage.setTitle("Login");
                    stage.setMaximized(false);
                    stage.setScene(createLoginScene());
                }
            });

        });

        return sidebar;
    }

    private void showHomePage() {
        Label homeLabel = new Label("Welcome to the Home Page");
        styleLabel(homeLabel, 24, "#34495e", true);

        VBox homePage = new VBox(homeLabel);
        homePage.setAlignment(Pos.CENTER);
        mainLayout.setCenter(homePage);
    }

    // Add this helper method to establish a database connection with Windows Authentication
    private static Connection getConnection() throws SQLException {
        String url = "jdbc:sqlserver://localhost;databaseName=MarketDb;integratedSecurity=true;trustServerCertificate=true;";
        return DriverManager.getConnection(url);
    }

    /*----------------------------------------* Product *----------------------------------------*/
    private void showProductsPage() throws SQLException {
        TableView<Product> productTable = new TableView<>();
        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        ObservableList<Product> productsList = getProductList();
        productTable.setItems(productsList);

        // Define table columns with increased font size
        TableColumn<Product, String> idCol = new TableColumn<>("Product ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        idCol.setStyle("-fx-font-size: 16px;");

        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setStyle("-fx-font-size: 16px;");

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setStyle("-fx-font-size: 16px;");

        TableColumn<Product, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityCol.setStyle("-fx-font-size: 16px;");

        TableColumn<Product, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setStyle("-fx-font-size: 16px;");

        // Edit Button Column
        TableColumn<Product, Void> editCol = new TableColumn<>("Edit");
        editCol.setCellFactory(param -> new TableCell<Product, Void>() {
            private final Button editButton = new Button("Edit");

            {
                editButton.setStyle("-fx-background-color: #0078D7; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 5 10 5 10;");
                editButton.setOnAction(e -> {
                    Product product = (Product) getTableView().getItems().get(getIndex());
                    showAddProductDialog(product, productsList, productTable);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(editButton);
                }
            }
        });

        // Delete Button Column
        TableColumn<Product, Void> deleteCol = new TableColumn<>("Delete");
        deleteCol.setCellFactory(param -> new TableCell<Product, Void>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setStyle("-fx-background-color: #DC3545; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 5 10 5 10;");
                deleteButton.setOnAction(e -> {
                    Product selectedProduct = getTableView().getItems().get(getIndex());
                    boolean confirmed = showConfirmationDialog("Delete Product", "Are you sure you want to delete this product?");
                    if (confirmed) {
                        deleteProductFromDatabase(selectedProduct);
                        productsList.remove(selectedProduct); // Remove from ObservableList
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        // Add columns to the table
        productTable.getColumns().addAll(idCol, nameCol, priceCol, quantityCol, categoryCol, editCol, deleteCol);

        // Search Bar
        TextField searchField = new TextField();
        searchField.setPromptText("Search Products By Name or Category");
        searchField.setStyle("-fx-pref-width: 300px; -fx-font-size: 14px;");

        Button searchButton = new Button("Search");
        searchButton.setStyle("-fx-background-color: #28A745; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 5 10 5 10;");

        searchButton.setOnAction(e -> {
            String searchQuery = searchField.getText().trim().toLowerCase();
            if (!searchQuery.isEmpty()) {
                ObservableList<Product> filteredProducts = FXCollections.observableArrayList();
                for (Product product : productsList) {
                    if (product.getName().toLowerCase().contains(searchQuery)
                            || product.getCategory().toLowerCase().contains(searchQuery)) {
                        filteredProducts.add(product);
                    }
                }
                productTable.setItems(filteredProducts);
                productTable.refresh();
            } else {
                productTable.setItems(productsList); // Reset to all productsList
                productTable.refresh();
            }
        });

        // Add Product Button
        Button addButton = new Button("Add Product");
        addButton.setStyle("-fx-background-color: #0078D7; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 5 10 5 10;");
        addButton.setOnAction(e -> showAddProductDialog(null, productsList, productTable));

        // Top Control Bar
        HBox topBar = new HBox(10, searchField, searchButton, addButton);
        topBar.setPadding(new Insets(10, 10, 10, 10));
        topBar.setAlignment(Pos.CENTER_LEFT);

        // Main Layout
        VBox productsPage = new VBox(10, topBar, productTable);
        productsPage.setPadding(new Insets(10, 10, 10, 10));

        // Set the table to grow vertically
        VBox.setVgrow(productTable, Priority.ALWAYS);

        mainLayout.setCenter(productsPage);
    }

    private void deleteProductFromDatabase(Product product) {
        String query = "DELETE FROM products WHERE name = ? AND price = ? AND quantity = ? AND category = ?";
        try (Connection conn = getConnection()) {
            // Delete the product
            PreparedStatement deleteproductStmt = conn.prepareStatement(
                    "DELETE FROM products WHERE id = ?");
            deleteproductStmt.setInt(1, product.getProductId());
            deleteproductStmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Success", "products and their orders deleted successfully!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete product: " + e.getMessage());
            e.printStackTrace();

        }

    }

    // Show Product Dialog when editing existing product or adding new one.
    private void showAddProductDialog(Product productToEdit, ObservableList<Product> productsList, TableView<Product> productTable) {
        Stage dialog = new Stage();
        dialog.setTitle(productToEdit == null ? "Add New Product" : "Edit Product");

        // Form Fields with consistent sizes
        TextField nameField = new TextField(productToEdit == null ? "" : productToEdit.getName());
        nameField.setPrefWidth(250);
        nameField.setStyle("-fx-font-size: 16px;");

        TextField priceField = new TextField(productToEdit == null ? "" : String.valueOf(productToEdit.getPrice()));
        priceField.setPrefWidth(250);
        priceField.setStyle("-fx-font-size: 16px;");

        TextField quantityField = new TextField(productToEdit == null ? "" : String.valueOf(productToEdit.getQuantity()));
        quantityField.setPrefWidth(250);
        quantityField.setStyle("-fx-font-size: 16px;");

        TextField categoryField = new TextField(productToEdit == null ? "" : productToEdit.getCategory());
        categoryField.setPrefWidth(250);
        categoryField.setStyle("-fx-font-size: 16px;");

        // Save Button with Attractive Green Style
        Button saveButton = new Button("Save");
        saveButton.setPrefWidth(100);
        saveButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 8 15 8 15; -fx-border-radius: 5; -fx-background-radius: 5;");
        saveButton.setOnAction(e -> {
            // text fields values
            String name = nameField.getText().trim();
            String category = categoryField.getText().trim();
            if (name.isEmpty() || category.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "All fields must be filled.");
                return;
            }
            int quantity;
            double price;
            try {
                quantity = Integer.parseInt(quantityField.getText().trim());
                price = Double.parseDouble(priceField.getText().trim());
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter valid numeric values for price and quantity.");
                return;
            }

            // chose add new product or edit
            if (productToEdit == null) {
                // Add New Product
                try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO products (name, price, quantity, category) VALUES (?, ?, ?, ?)")) {
                    // store new product in database
                    pstmt.setString(1, name);
                    pstmt.setDouble(2, price);
                    pstmt.setInt(3, quantity);
                    pstmt.setString(4, category);
                    pstmt.executeUpdate();

                    // get id of new product
                    PreparedStatement getProductId = conn.prepareStatement("SELECT id FROM products WHERE CAST(name AS NVARCHAR(MAX)) = ? ");
                    getProductId.setString(1, name);
                    ResultSet res = getProductId.executeQuery();
                    res.next();
                    int id = res.getInt(1);

                    // Update TableView
                    productsList.add(new Product(id, name, price, quantity, category)); ///update zero
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                // Edit Existing Product
                try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(
                        "UPDATE products SET name = ?, price = ?, quantity = ?, category = ? WHERE CAST(name AS NVARCHAR(MAX)) = ? AND price = ? AND quantity = ? AND CAST(category AS NVARCHAR(MAX))= ?")) {
                    pstmt.setString(1, name);
                    pstmt.setDouble(2, price);
                    pstmt.setInt(3, quantity);
                    pstmt.setString(4, category);
                    pstmt.setString(5, productToEdit.getName());
                    pstmt.setDouble(6, productToEdit.getPrice());
                    pstmt.setInt(7, productToEdit.getQuantity());
                    pstmt.setString(8, productToEdit.getCategory());

                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected == 0) {
                        showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update customer in the database.");
                        return;
                    }

                    // Update TableView
                    productToEdit.setName(name);
                    productToEdit.setPrice(price);
                    productToEdit.setQuantity(quantity);
                    productToEdit.setCategory(category);
                    productsList.set(productsList.indexOf(productToEdit), productToEdit);
                    productTable.refresh(); // Refresh the table to reflect the changes

                    showAlert(Alert.AlertType.INFORMATION, "Success", "Customer updated successfully!");

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            dialog.close();

        });

        // Styling for labels with larger font
    Label nameLabel = new Label("Name:");
    nameLabel.setStyle("-fx-font-size: 16px;");
    Label priceLabel = new Label("Price:");
    priceLabel.setStyle("-fx-font-size: 16px;");
    Label quantityLabel = new Label("Quantity:");
    quantityLabel.setStyle("-fx-font-size: 16px;");
    Label categoryLabel = new Label("Category:");
    categoryLabel.setStyle("-fx-font-size: 16px;");

    // Layout with adjusted spacing and larger font
    VBox layout = new VBox(15, // Adjust spacing between elements
            nameLabel, nameField,
            priceLabel, priceField,
            quantityLabel, quantityField,
            categoryLabel, categoryField,
            saveButton
    );
    layout.setPadding(new Insets(20));
    layout.setAlignment(Pos.CENTER);

    // Set dialog dimensions
    Scene scene = new Scene(layout, 350, 450); // Adjust width and height
    dialog.setScene(scene);
    dialog.setResizable(false); // Prevent resizing
    dialog.initModality(Modality.APPLICATION_MODAL); // Make it modal
    dialog.showAndWait();
    }


    /*----------------------------------------*Orders*----------------------------------------*/
// Existing imports and class definitions
    private void showOrdersPage() {
        TableView<Order> orderTable = createOrderTable();
        ObservableList<Order> ordersList = FXCollections.observableArrayList();

        // Fetch ordersList from the database
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(
                "SELECT o.id, c.name AS custName, s.name AS staffName, o.order_date, o.total_amount "
                + "FROM orders o JOIN customers c ON o.customer_id = c.id JOIN staff s ON s.id = o.staff_id")) {
            while (rs.next()) {
                ordersList.add(new Order(
                        rs.getInt("id"),
                        rs.getString("custName"),
                        rs.getString("staffName"),
                        rs.getDate("order_date"),
                        rs.getDouble("total_amount")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Add Columns for TableView
        TableColumn<Order, Integer> idCol = new TableColumn<>("Order ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        idCol.setStyle("-fx-font-size: 16px;");

        TableColumn<Order, String> customerNameCol = new TableColumn<>("Customer Name");
        customerNameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerNameCol.setStyle("-fx-font-size: 16px;");

        TableColumn<Order, LocalDate> orderDateCol = new TableColumn<>("Order Date");
        orderDateCol.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        orderDateCol.setStyle("-fx-font-size: 16px;");

        TableColumn<Order, Double> totalAmountCol = new TableColumn<>("Total Price");
        totalAmountCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        totalAmountCol.setStyle("-fx-font-size: 16px;");

        // Delete Button Column
        TableColumn<Order, Void> deleteCol = new TableColumn<>("Delete");
        deleteCol.setCellFactory(param -> new TableCell<Order, Void>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setStyle("-fx-background-color: #DC3545; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 5 10 5 10;");
                deleteButton.setOnAction(e -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirm Deletion");
                    alert.setHeaderText("Are you sure you want to delete this order?");
                    alert.setContentText("You can't retrieve it back.");
                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            Order selectedOrder = getTableView().getItems().get(getIndex());
                            if (deleteOrderFromDatabase(selectedOrder)) {
                                ordersList.remove(selectedOrder);
                                orderTable.refresh();
                            }
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        // Order Details Button Column
        TableColumn<Order, Void> detailsCol = new TableColumn<>("Order Details");
        detailsCol.setCellFactory(param -> new TableCell<Order, Void>() {
            private final Button detailsButton = new Button("Show");

            {
                detailsButton.setStyle("-fx-background-color: #0078D7; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 5 10 5 10;");
                detailsButton.setOnAction(e -> {
                    Order selectedOrder = getTableView().getItems().get(getIndex());
                    if (selectedOrder != null) {
                        showOrderDetailsDialog(selectedOrder);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(detailsButton);
                }
            }
        });

        // Add Columns to TableView
        orderTable.getColumns().setAll(idCol, customerNameCol, orderDateCol, totalAmountCol, detailsCol, deleteCol);
        orderTable.setItems(ordersList);

        // Search Field and Button
        TextField searchField = new TextField();
        searchField.setPromptText("Search Orders");
        searchField.setStyle("-fx-font-size: 14px; -fx-pref-width: 300px;");

        Button searchButton = new Button("Search");
        searchButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 5 10 5 10;");
        searchButton.setOnAction(e -> {
            String query = searchField.getText().trim();
            if (query.isEmpty()) {
                orderTable.setItems(ordersList);
                orderTable.refresh();
            } else {
                ObservableList<Order> filteredOrders = FXCollections.observableArrayList();
                for (Order order : ordersList) {
                    if (String.valueOf(order.getOrderId()).contains(query)
                            || order.getCustomerName().toLowerCase().contains(query.toLowerCase())) {
                        filteredOrders.add(order);
                    }
                }
                orderTable.setItems(filteredOrders);
                orderTable.refresh();
            }
        });

        // Add Order Button
        Button addOrderButton = new Button("Add Order");
        addOrderButton.setStyle("-fx-background-color: #0078D7; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 5 10 5 10;");
        addOrderButton.setOnAction(e -> {
            showAddOrderDialog(ordersList, getProductList(), orderTable);
        });

        // Top Bar Layout
        HBox topBar = new HBox(10, searchField, searchButton, addOrderButton);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10));

        // Ensure TableView Resizes Correctly
        VBox ordersPage = new VBox(10, topBar, orderTable);
        ordersPage.setPadding(new Insets(10));
        VBox.setVgrow(orderTable, Priority.ALWAYS);

        mainLayout.setCenter(ordersPage);
    }

    private TableView<Order> createOrderTable() {
        TableView<Order> table = new TableView<>();

        table.getColumns().addAll(
                createColumn("Order ID", "orderId"),
                createColumn("Customer Name", "customerName"),
                createColumn("Order Date", "orderDate"),
                createColumn("Total Amount", "totalAmount")
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        return table;
    }

    private boolean deleteOrderFromDatabase(Order order) {
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM orders WHERE id = ?")) {
            pstmt.setInt(1, order.getOrderId());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; // Return true if a row was deleted
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Return false if the operation failed
    }

    private void showAddOrderDialog(ObservableList<Order> orders, ObservableList<Product> availableProducts, TableView<Order> orderTable) {
        Order newOrder = new Order();
        Stage dialog = new Stage();
        dialog.setTitle("Add New Order");
        dialog.setResizable(false);

        // Fields for entering new order details
        TextField customerNameField = new TextField();
        customerNameField.setPromptText("Enter Customer Name");
        customerNameField.setStyle("-fx-font-size: 14px;");

        TextField staffNameField = new TextField();
        staffNameField.setPromptText("Enter Staff Name");
        staffNameField.setStyle("-fx-font-size: 14px;");

        DatePicker orderDatePicker = new DatePicker();
        orderDatePicker.setPromptText("Select Order Date");
        orderDatePicker.setStyle("-fx-font-size: 14px;");
        orderDatePicker.setPrefWidth(300);

        TextField totalAmountField = new TextField();
        totalAmountField.setPromptText("Total Amount");
        totalAmountField.setStyle("-fx-font-size: 14px;");
        totalAmountField.setEditable(false); // Prevent manual edits

        // Product selection components
        ComboBox<Product> productComboBox = new ComboBox<>(availableProducts);
        // To get product Name
        productComboBox.setConverter(new StringConverter<Product>() {
            public String toString(Product product) {
                return product != null ? product.getName() : "";
            }

            @Override
            public Product fromString(String string) {
                return null; // Not used in this context
            }
        });
        productComboBox.setPromptText("Select Product");
        productComboBox.setStyle("-fx-font-size: 14px;");

        TextField unitsField = new TextField();
        unitsField.setPromptText("Enter Units");
        unitsField.setStyle("-fx-font-size: 14px;");

        Button addProductButton = new Button("Add Product");
        addProductButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 14px; -fx-pref-width: 120px;");

        Queue<Product> orderProducts = new LinkedList<>();
        Queue<Integer> orderProductsFreq = new LinkedList<>();

        addProductButton.setOnAction(e -> {
            try {
                boolean isCustomerExist = isCustomerExist(conn, customerNameField.getText().trim());
                if (!isCustomerExist) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Customer Field Is Not Defined !");
                    return;
                }

            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Fetch Issue", "Can't Check For Customer !");
            }

            if (orderDatePicker.getValue() == null) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Date Field Is Not Defined !");
                return;
            }

            Product selectedProduct = productComboBox.getValue();
            String unitsText = unitsField.getText().trim();
            int units;

            // Validate product and units
            if (selectedProduct == null) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please select a product");
                return;
            }

            try {
                units = Integer.parseInt(unitsText);
                if (units <= 0) {
                    throw new NumberFormatException();
                }
                // Check if units number is valid in database
                try {
                    PreparedStatement checkUnits = conn.prepareStatement("SELECT 1 FROM products WHERE CAST(name AS NVARCHAR(MAX)) = ? AND quantity >= ? ");
                    checkUnits.setString(1, selectedProduct.getName());
                    checkUnits.setInt(2, units);
                    boolean isAvailable = checkUnits.executeQuery().next();

                    if (!isAvailable) {
                        showAlert(Alert.AlertType.ERROR, "Out of Stock", "Units number you want exceeds the stock units of this product");
                        return;
                    }

                } catch (SQLException sqlEx) {
                    showAlert(Alert.AlertType.ERROR, "Fetching Issue", "Can't fetch product available quantity !");
                    return;
                }

            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Units must be a positive integer.");
                return;
            }

            // Calculate and update total amount
            double productCost = selectedProduct.getPrice() * units;
            double currentTotal = totalAmountField.getText().isEmpty() ? 0 : Double.parseDouble(totalAmountField.getText());
            orderProducts.add(selectedProduct);
            orderProductsFreq.add(units);
            totalAmountField.setText(String.valueOf(currentTotal + productCost));

            // Reset product and units fields
            productComboBox.setValue(null);
            unitsField.clear();
        });

        Button saveButton = new Button("Save");
        saveButton.setStyle("-fx-background-color: #0078D7; -fx-text-fill: white; -fx-font-size: 14px; -fx-pref-width: 100px;");

        saveButton.setOnAction(e -> {
            String customerName = customerNameField.getText().trim();
            String staffName = staffNameField.getText().trim();

            try {
                Date date = java.sql.Date.valueOf(orderDatePicker.getValue());
                try {
                    // Check Existence of customer
                    PreparedStatement checkCustomer = conn.prepareStatement("SELECT id as exp FROM customers WHERE CAST(name AS NVARCHAR(MAX)) = ?", Statement.RETURN_GENERATED_KEYS);
                    checkCustomer.setString(1, customerName);
                    ResultSet customerResult = checkCustomer.executeQuery();
                    System.out.println("Customer Fetched");

                    // Check Existence of staff
                    PreparedStatement checkStaff = conn.prepareStatement("SELECT id as exp FROM staff WHERE CAST(name AS NVARCHAR(MAX)) = ?", Statement.RETURN_GENERATED_KEYS);
                    checkStaff.setString(1, staffName);
                    ResultSet staffResult = checkStaff.executeQuery();
                    System.out.println("Staff Fetched");

                    boolean isCustomerExist = customerResult.next();
                    boolean isStaffExist = staffResult.next();
                    int staffId = staffResult.getInt(1);
                    int customerId = customerResult.getInt(1);

                    if (isCustomerExist && isStaffExist) {
                        System.out.println("Customer and Staff exist");
                        // Insert New Order for Customer
                        PreparedStatement insertOrder = conn.prepareStatement("INSERT INTO orders (customer_id, staff_id, order_date, total_amount) VALUES (?, ?, ?, ?)");
                        insertOrder.setInt(1, customerId);
                        insertOrder.setInt(2, staffId);
                        insertOrder.setDate(3, date);
                        insertOrder.setDouble(4, Double.parseDouble(totalAmountField.getText()));

                        // Update Customer Orders Count
                        PreparedStatement updateOrderCount = conn.prepareStatement("Update customers SET orders_count += 1 WHERE id = ?");
                        updateOrderCount.setInt(1, customerId);

                        // Update Staff Orders Count
                        PreparedStatement updateOrderSales = conn.prepareStatement("Update staff SET orders_sold += 1 WHERE id = ?");
                        updateOrderSales.setInt(1, staffId);

                        insertOrder.executeUpdate();
                        System.out.println("Order Insertion Executed");
                        updateOrderCount.executeUpdate();
                        System.out.println("Update Customer Executed");
                        updateOrderSales.executeUpdate();
                        System.out.println("Update Staff Executed");

                        PreparedStatement getOrderId = conn.prepareStatement("SELECT TOP 1 id FROM orders ORDER BY id DESC");
                        ResultSet lastOrder = getOrderId.executeQuery();
                        lastOrder.next();
                        System.out.println("Last Order Id Fetched");
                        int id = lastOrder.getInt(1);

                        newOrder.setOrderId(id);
                        newOrder.setCustomerName(customerName);
                        newOrder.setStaffName(staffName);
                        newOrder.setOrderDate(date);
                        newOrder.setTotalAmount(Double.parseDouble(totalAmountField.getText()));

                        while (!orderProducts.isEmpty() && !orderProductsFreq.isEmpty()) {
                            Product product = orderProducts.poll();
                            int units = orderProductsFreq.poll();
                            // minus each product quantity from database
                            PreparedStatement updateQuantity = conn.prepareStatement("UPDATE products SET quantity = ? WHERE CAST(name AS NVARCHAR(MAX)) = ?");
                            updateQuantity.setInt(1, product.getQuantity() - units);
                            updateQuantity.setString(2, product.getName());

                            int rowsAffected = updateQuantity.executeUpdate();

                            if (rowsAffected == 0) {
                                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update quantity in the database.");
                                return;
                            }
                            // Create order items
                            PreparedStatement insertOrderItems = conn.prepareStatement("INSERT INTO order_items (order_id, product_id, quantity, price) "
                                    + "VALUES (?, ?, ?, ?)");
                            insertOrderItems.setInt(1, newOrder.getOrderId());
                            insertOrderItems.setInt(2, product.getProductId());
                            insertOrderItems.setInt(3, units);
                            insertOrderItems.setDouble(4, product.getPrice() * units);
                            insertOrderItems.executeUpdate(); //⚠️⚠️⚠️⚠️⚠️⚠️ DONT FORGET IT

                        }
                        System.out.println("All Order Items Handled");
                        orders.add(newOrder);
                        orderTable.refresh();
                        showAlert(Alert.AlertType.INFORMATION, "Success", "New Order Added successfully!");
                        dialog.close();

                    } else {
                        showAlert(Alert.AlertType.ERROR, "Invalid Input", "Customer Doesn't Exist");
                        customerNameField.clear();
                    }
                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR, "Fetching Issue", "Can't fetch customer or staff from database");
                }
            } catch (NullPointerException nullExp) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Customer Name and Order Date Are Not Valid");
            }

        });

        Label customerNameLabel = new Label("Customer Name:");
        customerNameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label staffNameLabel = new Label("Staff Name:");
        staffNameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label orderDateLabel = new Label("Order Date:");
        orderDateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label totalAmountLabel = new Label("Total Amount:");
        totalAmountLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label productLabel = new Label("Product:");
        productLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TextField priceTextField = new TextField();
        priceTextField.setEditable(false); // Make it read-only
        priceTextField.setPromptText("Product Price");
        // Add price field for product
        productComboBox.valueProperty().addListener((obs, oldProduct, newProduct) -> {
            if (newProduct != null) {
                priceTextField.setText(String.format("%.2f", newProduct.getPrice()));
            } else {
                priceTextField.clear();
            }
        });
        HBox productRoot = new HBox(10, productComboBox, priceTextField);

        Label unitsLabel = new Label("Units:");
        unitsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Layout for the dialog
        VBox layout = new VBox(15,
                customerNameLabel, customerNameField,
                staffNameLabel, staffNameField,
                orderDateLabel, orderDatePicker,
                productLabel, productRoot,
                unitsLabel, unitsField,
                addProductButton,
                totalAmountLabel, totalAmountField,
                saveButton
        );
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #dcdcdc; -fx-border-radius: 5; -fx-border-width: 1;");

        // Scene and stage
        Scene scene = new Scene(layout, 400, 600);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void showOrderDetailsDialog(Order selectedOrder) {
        // Create a dialog
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL); // User cannot interact with the main window to close this window
        dialogStage.setTitle("Order Details");

        // Table to display product details
        TableView<Product> productTable = new TableView<>();
        ObservableList<Product> productList = FXCollections.observableArrayList();

        // Fetch product details for the selected order from the database
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT p.name, p.category, oi.quantity, oi.price "
                + "FROM order_items oi JOIN products p ON oi.product_id = p.id "
                + "WHERE oi.order_id = ?")) {
            stmt.setInt(1, selectedOrder.getOrderId());
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                System.out.println("No data returned for order_id: " + selectedOrder.getOrderId());
            } else {
                do {
                    String name = rs.getString("name");
                    String category = rs.getString("category");
                    int quantity = rs.getInt("quantity");
                    double price = rs.getDouble("price");
                    productList.add(new Product(name, price, quantity, category));
                } while (rs.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Set up the TableView columns
        productTable.getColumns().addAll(
                createColumn("Name", "name"),
                createColumn("Category", "category"),
                createColumn("Quantity", "quantity"),
                createColumn("Price", "price")
        );
        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        productTable.setItems(productList);
        productTable.refresh();

        // Labels to display Order Info
        Label orderInfo = new Label("Details for Order ID: " + selectedOrder.getOrderId());
        Label customerName = new Label("Customer Name: " + selectedOrder.getCustomerName());
        Label staffName = new Label("Staff Name: " + selectedOrder.getStaffName());
        Label orderDate = new Label("Order Date: " + selectedOrder.getOrderDate());
        Label totalPrice = new Label("Total Price: $" + selectedOrder.getTotalAmount());

        // OK Button
        Button okButton = new Button("OK");
        okButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 14px;");
        okButton.setOnAction(event -> dialogStage.close());

        // Layout for the Dialog
        VBox dialogLayout = new VBox(10, orderInfo, customerName, staffName, orderDate, totalPrice, productTable, okButton);
        dialogLayout.setAlignment(Pos.CENTER);
        dialogLayout.setPadding(new Insets(10));

        Scene scene = new Scene(dialogLayout, 500, 500);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

/////
    //
    /*----------------------------------------* Customer *----------------------------------------*/
    private void showCustomersPage() {
        TableView<Customer> customerTable = new TableView<>();
        ObservableList<Customer> customers = FXCollections.observableArrayList();

        customerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Define table columns with increased font size
        TableColumn<Customer, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setStyle("-fx-font-size: 16px;");

        TableColumn<Customer, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setStyle("-fx-font-size: 16px;");

        TableColumn<Customer, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setStyle("-fx-font-size: 16px;");

        TableColumn<Customer, String> ordersCountCol = new TableColumn<>("Orders");
        ordersCountCol.setCellValueFactory(new PropertyValueFactory<>("ordersCount"));
        ordersCountCol.setStyle("-fx-font-size: 16px;");

        TableColumn<Customer, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setStyle("-fx-font-size: 16px;");

        // Add columns to table
        customerTable.getColumns().addAll(nameCol, emailCol, usernameCol, ordersCountCol, statusCol);

        // Add Buttons Column to table
        addCustomerButtonsColumn(customerTable, customers);

        // Fetch data from the database
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT id, name, email, username, orders_count, status FROM customers")) {
            while (rs.next()) {
                customers.add(new Customer(
                        rs.getInt("id"), // ID
                        rs.getString("name"), // Name
                        rs.getString("email"), // Email
                        rs.getString("username"), // Username
                        rs.getInt("orders_count"), // Orders Count
                        rs.getString("status") // Status
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        customerTable.setItems(customers);

        // Search Field
        TextField searchField = new TextField();
        searchField.setPromptText("Search Customers");
        searchField.setStyle("-fx-font-size: 14px; -fx-padding: 5px; -fx-border-radius: 5px; -fx-pref-width: 300px;");

        // Search Button
        Button searchButton = new Button("Search");
        searchButton.setStyle("-fx-background-color: #28A745; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 5 10 5 10;");
        searchButton.setOnAction(e -> {
            String query = searchField.getText().toLowerCase().trim();
            if (query.isEmpty()) {
                customerTable.setItems(customers);
                customerTable.refresh();
            } else {
                ObservableList<Customer> filteredCustomers = FXCollections.observableArrayList();
                for (Customer customer : customers) {
                    if (customer.getName().toLowerCase().contains(query)
                            || customer.getEmail().toLowerCase().contains(query)
                            || customer.getUsername().toLowerCase().contains(query)) {
                        filteredCustomers.add(customer);
                    }
                }
                customerTable.setItems(filteredCustomers);
                customerTable.refresh();
            }
        });

        // Add Customer Button
        Button addCustomerButton = new Button("Add Customer");
        addCustomerButton.setStyle("-fx-background-color: #0078D7; -fx-text-fill: white; -fx-font-size: 14px; -fx-pref-width: 120px;");
        addCustomerButton.setOnAction(e -> showAddCustomerDialog(customers));

        // Add Top Bar (Search + Add Customer)
        HBox topBar = new HBox(10, searchField, searchButton, addCustomerButton);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 10, 10, 10));

        // Spacer below the table
        Region spacer = new Region();
        spacer.setMinHeight(10);

        // Main Layout: Combine Top Bar, Table, and Spacer
        VBox customersPage = new VBox(10, topBar, customerTable, spacer);
        customersPage.setPadding(new Insets(10));
        VBox.setVgrow(customerTable, Priority.ALWAYS);
        VBox.setVgrow(spacer, Priority.NEVER);

        // Set Customers Page in Main Layout
        mainLayout.setCenter(customersPage);
    }

    // Add Edit and Delete Buttons to Table
    private void addCustomerButtonsColumn(TableView<Customer> customerTable, ObservableList<Customer> customers) {
        // Edit Button Column
        TableColumn<Customer, Void> editColumn = new TableColumn<>("Edit");
        editColumn.setCellFactory(param -> new TableCell<Customer, Void>() {
            private final Button editButton = new Button("Edit");

            {
                editButton.setStyle("-fx-background-color: #0078D7; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 5 10 5 10;");
                editButton.setOnAction(event -> {
                    Customer selectedCustomer = getTableView().getItems().get(getIndex());
                    showEditCustomerDialog(selectedCustomer, customers);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(editButton);
                }
            }
        });

        // Delete Button Column
        TableColumn<Customer, Void> deleteColumn = new TableColumn<>("Delete");
        deleteColumn.setCellFactory(param -> new TableCell<Customer, Void>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setStyle("-fx-background-color: #DC3545; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 5 10 5 10;");
                deleteButton.setOnAction(event -> {
                    Customer selectedCustomer = getTableView().getItems().get(getIndex());
                    boolean confirmed = showConfirmationDialog("Delete Customer", "Are you sure you want to delete this customer?");
                    if (confirmed) {
                        deleteCustomerFromDatabase(selectedCustomer);
                        customers.remove(selectedCustomer);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        // Add columns to the table
        customerTable.getColumns().addAll(editColumn, deleteColumn);
    }

    private void showEditCustomerDialog(Customer customer, ObservableList<Customer> customers) {
    // Create a new dialog stage
    Stage dialog = new Stage();
    dialog.setTitle("Edit Customer");
    dialog.setResizable(false);

    // Text fields for editing customer details with consistent font size and padding
    TextField nameField = new TextField(customer.getName());
    nameField.setPromptText("Enter Name");
    nameField.setStyle("-fx-font-size: 16px; -fx-padding: 5px;");

    TextField emailField = new TextField(customer.getEmail());
    emailField.setPromptText("Enter Email");
    emailField.setStyle("-fx-font-size: 16px; -fx-padding: 5px;");

    TextField usernameField = new TextField(customer.getUsername());
    usernameField.setPromptText("Enter Username");
    usernameField.setStyle("-fx-font-size: 16px; -fx-padding: 5px;");

    TextField ordersCountField = new TextField(String.valueOf(customer.getOrdersCount()));
    ordersCountField.setPromptText("Enter Orders Count");
    ordersCountField.setStyle("-fx-font-size: 16px; -fx-padding: 5px;");

    TextField statusField = new TextField(customer.getStatus());
    statusField.setPromptText("Enter Status");
    statusField.setStyle("-fx-font-size: 16px; -fx-padding: 5px;");

    // Save button with consistent styling
    Button saveButton = new Button("Save");
    saveButton.setStyle("-fx-background-color: #0078D7; -fx-text-fill: white; -fx-font-size: 16px; -fx-pref-width: 100px; -fx-padding: 8px; -fx-border-radius: 5px;");
    saveButton.setOnAction(e -> {
        // Get field values
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String status = statusField.getText().trim();
        int ordersCount;

        // Validate Orders Count input
        try {
            ordersCount = Integer.parseInt(ordersCountField.getText().trim());
        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Orders Count must be a valid number.");
            return;
        }

        // Validate fields are not empty
        if (name.isEmpty() || email.isEmpty() || username.isEmpty() || status.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "All fields must be filled.");
            return;
        }

        // Update the customer in the database
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(
                "UPDATE customers SET name = ?, email = ?, username = ?, orders_count = ?, status = ? WHERE id = ?")) {

            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, username);
            pstmt.setInt(4, ordersCount);
            pstmt.setString(5, status);
            pstmt.setInt(6, customer.getId());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected == 0) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update customer in the database.");
                return;
            }

            // Update the customer object and the ObservableList
            customer.setName(name);
            customer.setEmail(email);
            customer.setUsername(username);
            customer.setOrdersCount(ordersCount);
            customer.setStatus(status);
            customers.set(customers.indexOf(customer), customer);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Customer updated successfully!");
            dialog.close();

        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while updating the database: " + ex.getMessage());
            ex.printStackTrace();
        }
    });

    // Labels with bold styling and consistent font size
    Label nameLabel = new Label("Name:");
    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

    Label emailLabel = new Label("Email:");
    emailLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

    Label usernameLabel = new Label("Username:");
    usernameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

    Label ordersCountLabel = new Label("Orders Count:");
    ordersCountLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

    Label statusLabel = new Label("Status:");
    statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

    // Layout for the dialog
    VBox layout = new VBox(11,
            nameLabel, nameField,
            emailLabel, emailField,
            usernameLabel, usernameField,
            ordersCountLabel, ordersCountField,
            statusLabel, statusField,
            saveButton
    );
    layout.setAlignment(Pos.CENTER);
    layout.setPadding(new Insets(20));
    layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #dcdcdc; -fx-border-radius: 5; -fx-border-width: 1;");

    // Create and show the dialog scene
    Scene scene = new Scene(layout, 350, 500);
    dialog.setScene(scene);
    dialog.show();
}


    // Delete Customer from Database
    private void deleteCustomerFromDatabase(Customer customer) {
        try (Connection conn = getConnection()) {
            // Delete the customer
            PreparedStatement deleteCustomerStmt = conn.prepareStatement(
                    "DELETE FROM customers WHERE id = ?");
            deleteCustomerStmt.setInt(1, customer.getId());
            deleteCustomerStmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Customer and their orders deleted successfully!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete customer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Wait for the user's response
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void showAddCustomerDialog(ObservableList<Customer> customers) {
    Stage dialog = new Stage();
    dialog.setTitle("Add New Customer");

    // Labels and Fields with consistent large font
    Label nameLabel = new Label("Name:");
    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
    TextField nameField = createStyledTextField("Enter customer name");
    nameField.setPrefWidth(250);

    Label emailLabel = new Label("Email:");
    emailLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
    TextField emailField = createStyledTextField("Enter customer email");
    emailField.setPrefWidth(250);

    Label usernameLabel = new Label("Username:");
    usernameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
    TextField usernameField = createStyledTextField("Enter customer username");
    usernameField.setPrefWidth(250);

    Label statusLabel = new Label("Status:");
    statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
    TextField statusField = createStyledTextField("Enter customer status");
    statusField.setPrefWidth(250);

    // Save Button (Larger font and consistent styling)
    Button saveButton = createStyledButton("Save");
    saveButton.setPrefWidth(100);
    saveButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 8 15 8 15; -fx-border-radius: 5; -fx-background-radius: 5;");
    saveButton.setOnAction(e -> {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String status = statusField.getText().trim();

        // Validate inputs
        if (name.isEmpty() || email.isEmpty() || username.isEmpty() || status.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "All fields must be filled!");
            return;
        }

        try {
            // Save to the database
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO customers (name, email, username, orders_count, status) VALUES (?, ?, ?, 0, ?)")) {
                pstmt.setString(1, name);
                pstmt.setString(2, email);
                pstmt.setString(3, username);
                pstmt.setString(4, status);
                pstmt.executeUpdate();

                PreparedStatement getCustomerId = conn.prepareStatement("SELECT id FROM customers WHERE CAST(name AS NVARCHAR(MAX)) = ? ");
                getCustomerId.setString(1, name);
                ResultSet res = getCustomerId.executeQuery();
                res.next();
                int id = res.getInt(1);

                // Update table and close dialog
                customers.add(new Customer(id, name, email, username, 0, status)); // ID set to 0 for simplicity
                showAlert(Alert.AlertType.INFORMATION, "Success", "Customer added successfully!");
                dialog.close();
            }

        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Orders Count must be a number!");
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save customer!");
        }
    });

    VBox layout = new VBox(15, // Adjust spacing between elements
            nameLabel, nameField,
            emailLabel, emailField,
            usernameLabel, usernameField,
            statusLabel, statusField,
            saveButton
    );
    layout.setPadding(new Insets(20));
    layout.setAlignment(Pos.CENTER);

    // Set dialog dimensions
    Scene scene = new Scene(layout, 350, 450); // Adjust width and height
    dialog.setScene(scene);
    dialog.setResizable(false); // Prevent resizing
    dialog.initModality(Modality.APPLICATION_MODAL); // Make it modal
    dialog.showAndWait();
}


    /*----------------------------------------* Staff *----------------------------------------*/
    private void showStaffPage() {
    TableView<Staff> staffTable = new TableView<>();
    ObservableList<Staff> staffList = FXCollections.observableArrayList();

    staffTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    // Define table columns with increased font size
    TableColumn<Staff, String> nameCol = new TableColumn<>("Name");
    nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
    nameCol.setStyle("-fx-font-size: 16px;");

    TableColumn<Staff, String> phoneCol = new TableColumn<>("Phone");
    phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
    phoneCol.setStyle("-fx-font-size: 16px;");

    TableColumn<Staff, String> genderCol = new TableColumn<>("Gender");
    genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
    genderCol.setStyle("-fx-font-size: 16px;");

    TableColumn<Staff, String> orders_soldCol = new TableColumn<>("Orders Sold");
    orders_soldCol.setCellValueFactory(new PropertyValueFactory<>("orders_sold"));
    orders_soldCol.setStyle("-fx-font-size: 16px;");

    // Add columns to table
    staffTable.getColumns().addAll(nameCol, phoneCol, genderCol, orders_soldCol);

    // Add Buttons Column (if necessary)
    addStaffButtonsColumn(staffTable, staffList);

    // Fetch data from the database
    try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT id, name, phone, gender, orders_sold FROM staff")) {
        while (rs.next()) {
            staffList.add(new Staff(
                    rs.getInt("id"), // ID
                    rs.getString("name"), // Name
                    rs.getString("phone"), // Phone
                    rs.getString("gender"), // Gender
                    rs.getInt("orders_sold") // Orders Sold
            ));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    staffTable.setItems(staffList);

    // Search Field
    TextField searchField = new TextField();
    searchField.setPromptText("Search Staff");
    searchField.setStyle("-fx-font-size: 14px; -fx-padding: 5px; -fx-border-radius: 5px; -fx-pref-width: 300px;");

    // Search Button
    Button searchButton = new Button("Search");
    searchButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 14px; -fx-pref-width: 80px;");
    searchButton.setOnAction(e -> {
        String query = searchField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            staffTable.setItems(staffList);
            staffTable.refresh();
        } else {
            ObservableList<Staff> filteredStaff = FXCollections.observableArrayList();
            for (Staff staff : staffList) {
                if (staff.getName().toLowerCase().contains(query)) {
                    filteredStaff.add(staff);
                }
            }
            staffTable.setItems(filteredStaff);
            staffTable.refresh();
        }
    });

    // Add Staff Button
    Button addStaffButton = new Button("Add Staff");
    addStaffButton.setStyle("-fx-background-color: #0078D7; -fx-text-fill: white; -fx-font-size: 14px; -fx-pref-width: 120px;");
    addStaffButton.setOnAction(e -> showAddStaffDialog(staffList));

    // Add Top Bar (Search + Add Staff)
    HBox topBar = new HBox(10, searchField, searchButton, addStaffButton);
    topBar.setAlignment(Pos.CENTER_LEFT);
    topBar.setPadding(new Insets(10, 10, 10, 10));

    // Spacer below the table
    Region spacer = new Region();
    spacer.setMinHeight(10);

    // Main Layout: Combine Top Bar, Table, and Spacer
    VBox staffPage = new VBox(10, topBar, staffTable, spacer);
    staffPage.setPadding(new Insets(10));
    VBox.setVgrow(staffTable, Priority.ALWAYS);
    VBox.setVgrow(spacer, Priority.NEVER);

    // Set Staff Page in Main Layout
    mainLayout.setCenter(staffPage);
}


    // show add staff dialog
    private void showAddStaffDialog(ObservableList<Staff> StaffList) {
    Stage dialog = new Stage();
    dialog.setTitle("Add New Staff");

    // Labels and Fields with consistent large font
    Label nameLabel = new Label("Name:");
    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
    TextField nameField = createStyledTextField("Enter Staff name");
    nameField.setPrefWidth(250);

    Label phoneLabel = new Label("Phone:");
    phoneLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
    TextField phoneField = createStyledTextField("Enter Staff Phone");
    phoneField.setPrefWidth(250);

    Label genderLabel = new Label("Gender:");
    genderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
    TextField genderField = createStyledTextField("Enter Staff Gender");
    genderField.setPrefWidth(250);

    // Save Button with consistent styling
    Button saveButton = createStyledButton("Save");
    saveButton.setPrefWidth(100);
    saveButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 8 15 8 15; -fx-border-radius: 5; -fx-background-radius: 5;");
    saveButton.setOnAction(e -> {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String gender = genderField.getText().trim();

        // Validate inputs
        if (name.isEmpty() || phone.isEmpty() || gender.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "All fields must be filled!");
            return;
        }

        try {
            // Save to the database
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO staff (name, phone, gender, orders_sold) VALUES (?, ?, ?, 0)")) {
                pstmt.setString(1, name);
                pstmt.setString(2, phone);
                pstmt.setString(3, gender);
                pstmt.executeUpdate();

                PreparedStatement getStaffId = conn.prepareStatement("SELECT id FROM staff WHERE CAST(name AS NVARCHAR(MAX)) = ? AND CAST(phone AS NVARCHAR(MAX)) = ?");
                getStaffId.setString(1, name);
                getStaffId.setString(2, phone);
                ResultSet res = getStaffId.executeQuery();
                res.next();
                int id = res.getInt(1);

                // Update table and close dialog
                StaffList.add(new Staff(id, name, phone, gender, 0));
                showAlert(Alert.AlertType.INFORMATION, "Success", "Staff added successfully!");
                dialog.close();
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save staff!");
        }
    });

    // Layout
    VBox layout = new VBox(15, // Adjust spacing between elements
            nameLabel, nameField,
            phoneLabel, phoneField,
            genderLabel, genderField,
            saveButton
    );
    layout.setPadding(new Insets(20));
    layout.setAlignment(Pos.CENTER);

    // Set dialog dimensions
    Scene scene = new Scene(layout, 350, 400); // Adjust width and height
    dialog.setScene(scene);
    dialog.setResizable(false); // Prevent resizing
    dialog.initModality(Modality.APPLICATION_MODAL); // Make it modal
    dialog.showAndWait();
}


    // Add Edit and Delete Buttons to Table
    private void addStaffButtonsColumn(TableView<Staff> staffTable, ObservableList<Staff> staffList) {
        // Edit Button Column
        TableColumn<Staff, Void> editColumn = new TableColumn<>("Edit");
        editColumn.setCellFactory(param -> new TableCell<Staff, Void>() {
            private final Button editButton = new Button("Edit");

            {
                editButton.setStyle("-fx-background-color: #0078D7; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 5 10 5 10;");
                editButton.setOnAction(event -> {
                    Staff selectedStaff = getTableView().getItems().get(getIndex());
                    showEditStaffDialog(selectedStaff, staffList);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(editButton);
                }
            }
        });

        // Delete Button Column
        TableColumn<Staff, Void> deleteColumn = new TableColumn<>("Delete");
        deleteColumn.setCellFactory(param -> new TableCell<Staff, Void>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setStyle("-fx-background-color: #DC3545; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 5 10 5 10;");
                deleteButton.setOnAction(event -> {
                    Staff selectedStaff = getTableView().getItems().get(getIndex());
                    boolean confirmed = showConfirmationDialog("Delete Staff", "Are you sure you want to delete this Staff?");
                    if (confirmed) {
                        deleteStaffFromDatabase(selectedStaff);
                        staffList.remove(selectedStaff);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        // Add columns to the table
        staffTable.getColumns().addAll(editColumn, deleteColumn);
    }

    private void showEditStaffDialog(Staff staff, ObservableList<Staff> staffList) {
    // Create a new dialog stage
    Stage dialog = new Stage();
    dialog.setTitle("Edit Staff");
    dialog.setResizable(false);

    // Text fields for editing staff details with larger font size
    TextField nameField = new TextField(staff.getName());
    nameField.setPromptText("Enter Name");
    nameField.setStyle("-fx-font-size: 16px;");

    TextField phoneField = new TextField(staff.getPhone());
    phoneField.setPromptText("Enter Phone");
    phoneField.setStyle("-fx-font-size: 16px;");

    TextField genderField = new TextField(staff.getGender());
    genderField.setPromptText("Enter Gender");
    genderField.setStyle("-fx-font-size: 16px;");

    // Save button with consistent styling
    Button saveButton = new Button("Save");
    saveButton.setStyle("-fx-background-color: #0078D7; -fx-text-fill: white; -fx-font-size: 16px; -fx-pref-width: 100px; -fx-font-weight: bold;");
    saveButton.setOnAction(e -> {
        // Get field values
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String gender = genderField.getText().trim();

        // Validate fields are not empty
        if (name.isEmpty() || phone.isEmpty() || gender.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "All fields must be filled.");
            return;
        }

        // Update the staff in the database
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(
                "UPDATE staff SET name = ?, phone = ?, gender = ? WHERE id = ?")) {

            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.setString(3, gender);
            pstmt.setInt(4, staff.getId());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected == 0) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update staff in the database.");
                return;
            }

            // Update the staff object and the ObservableList
            staff.setName(name);
            staff.setPhone(phone);
            staff.setGender(gender);
            staffList.set(staffList.indexOf(staff), staff);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Staff updated successfully!");
            dialog.close();

        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while updating the database: " + ex.getMessage());
            ex.printStackTrace();
        }
    });

    // Labels with bold styling and larger font
    Label nameLabel = new Label("Name:");
    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

    Label phoneLabel = new Label("Phone:");
    phoneLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

    Label genderLabel = new Label("Gender:");
    genderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

    // Layout for the dialog
    VBox layout = new VBox(15,
            nameLabel, nameField,
            phoneLabel, phoneField,
            genderLabel, genderField,
            saveButton
    );
    layout.setAlignment(Pos.CENTER);
    layout.setPadding(new Insets(20));
    layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #dcdcdc; -fx-border-radius: 5; -fx-border-width: 1;");

    // Create and show the dialog scene
    Scene scene = new Scene(layout, 350, 400);
    dialog.setScene(scene);
    dialog.show();
}


    // Delete staff from Database
    private void deleteStaffFromDatabase(Staff staff) {
        try (Connection conn = getConnection()) {
            // Delete the staff
            PreparedStatement deleteStaffStmt = conn.prepareStatement(
                    "DELETE FROM staff WHERE id = ?");
            deleteStaffStmt.setInt(1, staff.getId());
            deleteStaffStmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Success", "staff and their orders deleted successfully!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete customer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /*----------------------------------------Helper Methods----------------------------------------*/
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private <T> TableColumn<T, ?> createColumn(String title, String property) {
        TableColumn<T, Object> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        return column;
    }

    private TextField createStyledTextField(String placeholder) {
        TextField textField = new TextField();
        textField.setPromptText(placeholder);
        textField.setStyle("-fx-background-radius: 15; -fx-padding: 10;");
        return textField;
    }

    private PasswordField createStyledPasswordField(String placeholder) {
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(placeholder);
        passwordField.setStyle("-fx-background-radius: 15; -fx-padding: 10;");
        return passwordField;
    }

    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        button.setTextFill(Color.WHITE);
        button.setStyle("-fx-background-color: #2980b9; -fx-background-radius: 15;");
        return button;
    }

    private Button createStyledSignOutButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        button.setTextFill(Color.WHITE);
        button.setStyle("-fx-background-color: #c0392b; -fx-background-radius: 15;");
        return button;
    }

    private void styleLabel(Label label, int fontSize, String color, boolean bold) {
        label.setFont(Font.font("Arial", bold ? FontWeight.BOLD : FontWeight.NORMAL, fontSize));
        label.setTextFill(Color.web(color));
    }

    private ObservableList<Product> getProductList() { // connect and fetch Data
        ObservableList<Product> productsList = FXCollections.observableArrayList();;
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT id, name, price, quantity, category FROM products")) {

            while (rs.next()) {
                productsList.add(new Product(
                        rs.getInt("id"), // ID
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"),
                        rs.getString("category")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productsList;
    }

    private boolean isCustomerExist(Connection conn, String name) throws SQLException {
        PreparedStatement checkCustomer = conn.prepareStatement("SELECT 1 exp FROM customers WHERE CAST(name AS NVARCHAR(MAX)) = ?", Statement.RETURN_GENERATED_KEYS);
        checkCustomer.setString(1, name);
        ResultSet res = checkCustomer.executeQuery();
        return res.next();
    }
}
