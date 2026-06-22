package com.cours.app.controller;

import java.util.List;
import java.util.Optional;

import com.cours.app.MainApp;
import com.cours.app.dao.ProduitDAO;
import com.cours.app.model.Product;
import com.cours.app.util.Animations;
import com.cours.app.util.PasswordUtil;
import com.cours.app.util.Toast;
import com.cours.app.util.ToggleSwitch;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class DashboardController {

    @FXML private StackPane root;

    // En-tête
    @FXML private Label welcomeLabel;
    @FXML private Label avatarLabel;
    @FXML private TextField searchField;
    @FXML private Label themeIcon;
    @FXML private Label themeLabel;
    @FXML private HBox navUsers;
    @FXML private HBox navStats;
    @FXML private HBox navSettings;

    // Statistiques
    @FXML private HBox statRow;
    @FXML private Label statTotal;
    @FXML private Label statMaries;
    @FXML private Label statCelibataires;

    // Tableau
    @FXML private Label tableCount;
    @FXML private Button refreshBtn;
    @FXML private Label refreshIcon;
    @FXML private TableView<Product> table;
    @FXML private TableColumn<Product, String> colNom;
    @FXML private TableColumn<Product, String> colprix;
    @FXML private TableColumn<Product, String> colquantite;
    @FXML private TableColumn<Product, Boolean> colStatut;
    @FXML private TableColumn<Product, Product> colActions;

    // Formulaire
    @FXML private VBox formCard;
    @FXML private Label formTitle;
    @FXML private Label formHint;
    @FXML private TextField nomField;
    @FXML private TextField prixField;
    @FXML private TextField quantiteField;
    @FXML private PasswordField passwordField;
    @FXML private StackPane DispoHolder;
    @FXML private Label Disponibilite;
    @FXML private Button saveBtn;
    @FXML private Button deleteBtn;

    private final ProduitDAO dao = new ProduitDAO();
    private final ObservableList<Product> master = FXCollections.observableArrayList();
    private ToggleSwitch DispoSwitch;
    private RotateTransition refreshSpin;
    private Product editing;
    private Product current;

    private int lastTotal = 0;
    private int lastDispo = 0;
    private int lastNonDispo = 0;

    @FXML
    private void initialize() {
        setupSwitch();
        setupColumns();
        setupTable();
        setupSearch();

        refreshSpin = Animations.spin(refreshIcon);
        Animations.hoverScale(avatarLabel, 1.08);
        syncThemeButton();

        loadData();
        clearForm();
        playIntro();
    }

    public void setCurrentUser(Product p) {
        this.current = p;
        avatarLabel.setText(initials(p));
        welcomeLabel.setText("Bonjour — gérez vos produits");
    }

    // ---------- configuration ----------
    private void setupSwitch() {
        DispoSwitch = new ToggleSwitch();
        DispoHolder.getChildren().add(DispoSwitch);
        DispoSwitch.selectedProperty().addListener(
                (o, was, now) -> Disponibilite.setText(now ? "Disponible" : "N'est pas disponible"));
    }

    private void setupColumns() {
        colNom.setCellValueFactory(c -> c.getValue().nomProperty());
        colprix.setCellValueFactory(c -> c.getValue().prenomProperty());
        colquantite.setCellValueFactory(c -> c.getValue().emailProperty());

        colNom.setPrefWidth(130);
        colprix.setPrefWidth(130);
        colquantite.setPrefWidth(240);
        colStatut.setPrefWidth(140);
        colActions.setPrefWidth(110);
        colActions.setMinWidth(100);

        colStatut.setCellValueFactory(c -> c.getValue().DispoProperty());
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean dispo, boolean empty) {
                super.updateItem(dispo, empty);
                if (empty || dispo == null) {
                    setGraphic(null);
                    return;
                }
                Label chip = new Label(dispo ? "Disponible" : "N'est pas disponible");
                chip.getStyleClass().add("chip");
                chip.getStyleClass().add(dispo ? "chip--dispo" : "chip--non_dispo");
                setGraphic(chip);
            }
        });

        // Modifier et Supprimer (button)
        colActions.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button edit = iconButton("✎", "icon-btn");
            private final Button del = iconButton("🗑", "icon-btn", "icon-btn--danger");
            private final HBox box = new HBox(8, edit, del);

            {
                box.setAlignment(Pos.CENTER);
                edit.setOnAction(e -> {
                    Product p = rowProduct();
                    if (p != null) {
                        table.getSelectionModel().select(u);
                    }
                });
                del.setOnAction(e -> {
                    Product p = rowProduct();
                    if (p != null) {
                        confirmDelete(p);
                    }
                });
            }

            private Product rowProduct() {
                int i = getIndex();
                List<Product> items = getTableView().getItems();
                return (i >= 0 && i < items.size()) ? items.get(i) : null;
            }

            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : box);
            }
        });
    }

    private void setupTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Aucun produit — ajoutez-en un !"));

        table.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            if (nv != null) {
                editProduct(nv);
            }
        });

        // Fondu à l'apparition
        table.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.itemProperty().addListener((o, oldU, newU) -> {
                if (newU != null) {
                    row.setOpacity(0);
                    FadeTransition fade = new FadeTransition(Duration.millis(240), row);
                    fade.setToValue(1);
                    fade.play();
                }
            });
            return row;
        });

        FilteredList<Product> filtered = new FilteredList<>(master, u -> true);
        SortedList<Product> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
        this.filtered = filtered;
    }

    private FilteredList<Product> filtered;

    private void setupSearch() {
        searchField.textProperty().addListener((o, ov, nv) -> {
            String q = nv == null ? "" : nv.trim().toLowerCase();
            filtered.setPredicate(u -> {
                if (q.isEmpty()) {
                    return true;
                }
                String haystack = (u.getNom() + " " + u.getPrix() + " " + u.getQuantite()).toLowerCase();
                String statut = u.isDisponible() ? "marié marie" : "célibataire celibataire";
                return haystack.contains(q) || statut.contains(q);
            });
            int n = filtered.size();
            tableCount.setText(n + (n > 1 ? " résultats" : " résultat"));
        });
    }

    // ---------- données ----------

    private void loadData() {
        List<Product> products = dao.findAll();
        master.setAll(products);

        int total = products.size();
        int dispo = (int) products.stream().filter(Product::isDisponible).count();
        int nonDispo = total - dispo;

        Animations.countUp(statTotal, lastTotal, total, 700);
        Animations.countUp(statMaries, lastDispo, dispo, 700);
        Animations.countUp(statCelibataires, lastNonDispo, nonDispo, 700);
        lastTotal = total;
        lastDispo = dispo;
        lastNonDispo = nonDispo;

        tableCount.setText(total + (" produits"));
    }

    // ---------- actions formulaire ----------

    @FXML
    private void handleNew() {
        table.getSelectionModel().clearSelection();
        clearForm();
        animateForm();
        nomField.requestFocus();
    }

    @FXML
    private void handleCancel() {
        table.getSelectionModel().clearSelection();
        clearForm();
        animateForm();
    }

    @FXML
    private void handleSave() {
        String nom = text(nomField);
        Double prix = Double.valueOf(text(prixField));
        int quantite = Integer.parseInt(text(quantiteField));

        if (nom.isEmpty() || prix.isNaN()) {
            reject("nom et prix sont obligatoires.");
            return;
        }

        int excludeId = editing == null ? 0 : editing.getId();

        if (editing == null) {
            Product u = new Product(0, nom, prix, quantite, DispoSwitch.isSelected());
            dao.insert(u);
            Toast.success(root, "Produits « " + u.fullName() + " » ajouté avec succès");
        } else {
            editing.setNom(nom);
            editing.setPrix(prix);
            editing.setQuantite(quantite);
            editing.setDisponible(DispoSwitch.isSelected());

            dao.update(editing);
            Toast.success(root, "Modifications enregistrées");
        }

        loadData();
        table.getSelectionModel().clearSelection();
        clearForm();
        Animations.pulse(statTotal);
    }

    @FXML
    private void handleDelete() {
        if (editing != null) {
            confirmDelete(editing);
        }
    }

    private void confirmDelete(Product u) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(MainApp.stage());
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer « " + u.fullName() + " » ?");
        alert.setContentText("Cette action est définitive.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            dao.delete(u.getId());
            Toast.info(root, "Produit supprimé");
            loadData();
            table.getSelectionModel().clearSelection();
            clearForm();
        }
    }

    private void editProduct(@org.jetbrains.annotations.UnknownNullability Product u) {
        editing = u;
        formTitle.setText("Modifier le produit");
        formHint.setText("Laissez le mot de passe vide pour le conserver.");
        nomField.setText(u.getNom());
        prixField.setText(String.valueOf(u.getPrix()));
        quantiteField.setText(String.valueOf(u.getQuantite()));
        passwordField.clear();
        DispoSwitch.setSelected(u.isDisponible());
        saveBtn.setText("Enregistrer les modifications");
        showDelete(true);
        animateForm();
    }

    private void clearForm() {
        editing = null;
        formTitle.setText("Nouvel utilisateur");
        formHint.setText("Remplissez les champs puis enregistrez.");
        nomField.clear();
        prixField.clear();
        quantiteField.clear();
        passwordField.clear();
        DispoSwitch.setSelected(false);
        saveBtn.setText("Enregistrer");
        showDelete(false);
    }

    // ---------- barre latérale ----------

    @FXML
    private void handleRefresh() {
        refreshSpin.play();
        loadData();
        PauseTransition stop = new PauseTransition(Duration.millis(650));
        stop.setOnFinished(e -> {
            refreshSpin.stop();
            refreshIcon.setRotate(0);
        });
        stop.play();
        Toast.info(root, "Liste actualisée");
    }

    @FXML
    private void toggleTheme() {
        MainApp.toggleTheme();
        syncThemeButton();
        Animations.pulse(themeIcon);
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(MainApp.stage());
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Se déconnecter ?");
        alert.setContentText("Vous reviendrez à l'écran de connexion.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            MainApp.goToLogin();
        }
    }

    // ---------- animations & helpers ----------

    private void playIntro() {
        Platform.runLater(() -> {
            Animations.staggerIn(List.of(navUsers, navStats, navSettings), 70, 250);
            double delay = 120;
            for (var card : statRow.getChildren()) {
                Animations.popIn(card, 480, delay);
                Animations.hoverScale(card, 1.02);
                delay += 110;
            }
            Animations.slideInUp(table, 24, 520, 360);
            Animations.slideInUp(formCard, 24, 520, 460);
        });
    }

    private void animateForm() {
        TranslateTransition move = new TranslateTransition(Duration.millis(260), formCard);
        move.setFromX(22);
        move.setToX(0);
        FadeTransition fade = new FadeTransition(Duration.millis(260), formCard);
        fade.setFromValue(0.35);
        fade.setToValue(1);
        new ParallelTransition(move, fade).play();
    }

    private void syncThemeButton() {
        if (MainApp.isDarkMode()) {
            themeIcon.setText("☀");
            themeLabel.setText("Thème clair");
        } else {
            themeIcon.setText("🌙");
            themeLabel.setText("Thème sombre");
        }
    }

    private void showDelete(boolean show) {
        deleteBtn.setManaged(show);
        deleteBtn.setVisible(show);
    }

    private void reject(String message) {
        Toast.error(root, message);
        Animations.shake(formCard);
    }

    private Button iconButton(String text, String... classes) {
        Button b = new Button(text);
        b.getStyleClass().addAll(classes);
        return b;
    }

    private static String text(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private static String initials(@org.jetbrains.annotations.UnknownNullability Product u) {
        double p = u.getPrix();
        String n = u.getNom();
        String a = p.isEmpty() ? "" : p.substring(0, 1);
        String b = n.isEmpty() ? "" : n.substring(0, 1);
        String res = (a + b).toUpperCase();
        return res.isEmpty() ? "?" : res;
    }
}
