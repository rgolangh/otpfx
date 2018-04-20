package org.rgo.otpfx.presentation.token;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.rgo.otpfx.business.boundary.TokenGenerator;
import org.rgo.otpfx.business.boundary.TokenRepository;
import org.rgo.otpfx.model.Token;

import javax.inject.Inject;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TokenPresenter implements Initializable {

    @FXML
    Label message;
    @FXML
    FlowPane otpPane;
    @FXML
    Button addToken;
    @FXML
    Button addFromURI;
    @Inject
    TokenGenerator otpGenerator;
    @Inject
    TokenRepository tokenRepository;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        otpPane.setPrefWrapLength(200);
        addToken.setText("\uf055 \uf084");
        addFromURI.setText("\uf055 \uf08e ");
        refreshOtpGrid();
    }

    private void refreshOtpGrid() {
        otpPane.getChildren().clear();
        tokenRepository.getAll()
                .forEach(otp -> otpPane.getChildren().add(hboxFromOtp(otp)));
        otpPane.setVisible(true);
    }

    public void addToken(ActionEvent actionEvent) {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Create New OTP");

        ButtonType ok = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        // Create the name and key labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField name = new TextField();
        name.setPromptText("Name");
        TextField key = new TextField();
        key.setPromptText("Key");
        CheckBox checkBox = new CheckBox();
        checkBox.setAccessibleText("Hotp?");
        checkBox.setTooltip(new Tooltip("Check for Hotp token type. Default Totp"));

        grid.add(new Label("Name:"), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("Key:"), 0, 1);
        grid.add(key, 1, 1);
        grid.add(new Label("Hotp:"), 0, 2);
        grid.add(checkBox, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the name field by default.
        Platform.runLater(() -> name.requestFocus());

        name.textProperty().addListener(
                (observable, oldValue, newValue) -> key.setDisable(newValue.trim().isEmpty()));
        // Convert the result to a name-key-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ok) {
                return new Pair<>(name.getText(), key.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();
        result.ifPresent(pair -> {
            Token token;
            if (pair.getValue().isEmpty()) {
                token = otpGenerator.createToken(pair.getKey(), checkBox.isSelected());
            } else {
                token = otpGenerator.createToken(pair.getKey(), pair.getValue(), checkBox.isSelected());
            }
            otpPane.getChildren().add(hboxFromOtp(token));
        });
    }

    private HBox hboxFromOtp(final Token token) {
        HBox hbox = new HBox();
        hbox.getStyleClass().add("otp-record");
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(2);
        hbox.setPrefWidth(400);
        Label otpName = new Label(token.getName());
        HBox grow = new HBox();
        HBox.setHgrow(grow, Priority.ALWAYS);
        Label otpNow = new Label("");
        otpNow.getStyleClass().add("otp-now");
        otpName.setOnMouseClicked(mouseEvent -> {
            String now = token.now();
            tokenRepository.save(token);
            otpNow.setText(now);
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(now);
            clipboard.setContent(content);
        });
        Label delete = new Label("\uf00d");
        delete.getStyleClass().add("icons-small");
        delete.setOnMouseClicked(event -> {
            tokenRepository.delete(token.getName());
            refreshOtpGrid();
        });
        VBox vgrow = new VBox();
        VBox.setVgrow(vgrow, Priority.ALWAYS);
        VBox actions = new VBox(vgrow, delete);
        hbox.getChildren().addAll(otpName, grow, otpNow, actions);
        return hbox;
    }

    public void addFromUri(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add key from URI");
        dialog.setContentText("URI:");

        // Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            URI uri = null;
            try {
                uri = URI.create(result.get());
            } catch (Exception e) {
                dialog.setOnCloseRequest(new EventHandler<DialogEvent>() {
                    @Override public void handle(DialogEvent event) {
                        //FIXME handle wrong uri here
                    }
                });
            }
            boolean hotp = uri.getHost().equalsIgnoreCase("hotp");
            Map<String, String> params = Stream.of(uri.getQuery().split("&"))
                    .filter(Objects::nonNull)
                    .map(split -> split.split("="))
                    .collect(Collectors.toMap(a -> (a[0]), a -> a[1]));
            if (params.containsKey("secret")) {
                otpPane.getChildren().add(hboxFromOtp(otpGenerator.createToken(
                        uri.getRawPath().replaceFirst("/", ""),
                        params.get("secret"),
                        hotp
                )));
            }
        }
    }
}
