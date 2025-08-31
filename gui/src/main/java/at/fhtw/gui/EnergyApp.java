package at.fhtw.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EnergyApp extends Application {

    // REST-API Basis
    private static final String API_URL = "http://localhost:8081/energy";

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // Labels für Prozent-Anzeige
    private Label communityDepletedLabel;
    private Label gridPortionLabel;

    private Timer autoRefreshTimer;
    private boolean autoRefreshRunning = false;

    @Override
    public void start(Stage stage) {
        Label titleLabel = new Label("Energy Community Dashboard");

        // Current Percentage
        Label currentLabel = new Label("Current Percentage:");
        communityDepletedLabel = new Label("Community Depleted: – %");
        gridPortionLabel = new Label("Grid Portion: – %");

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> fetchCurrent());

        Button autoRefreshButton = new Button("Start Auto-Refresh");
        autoRefreshButton.setOnAction(e -> toggleAutoRefresh(autoRefreshButton));

        // Historical Usage
        Label historicalLabel = new Label("Historical Usage:");
        DatePicker startDate = new DatePicker();
        DatePicker endDate = new DatePicker();
        Button showDataButton = new Button("Show Data");
        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        showDataButton.setOnAction(e -> fetchHistorical(startDate, endDate, resultArea));

        GridPane historicalGrid = new GridPane();
        historicalGrid.setHgap(10);
        historicalGrid.setVgap(10);
        historicalGrid.add(new Label("Start:"), 0, 0);
        historicalGrid.add(startDate, 1, 0);
        historicalGrid.add(new Label("End:"), 0, 1);
        historicalGrid.add(endDate, 1, 1);

        VBox root = new VBox(20,
                titleLabel,
                currentLabel, communityDepletedLabel, gridPortionLabel,
                refreshButton, autoRefreshButton,
                historicalLabel, historicalGrid, showDataButton, resultArea
        );
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 600, 500);
        stage.setScene(scene);
        stage.setTitle("Energy Community");
        stage.setOnCloseRequest(e -> { if (autoRefreshTimer != null) autoRefreshTimer.cancel(); });
        stage.show();
    }

    /** Holt den aktuellen Prozent-Stand. Robust gegen beide Formate. */
    private void fetchCurrent() {
        var req = HttpRequest.newBuilder(URI.create(API_URL + "/current")).GET().build();

        new Thread(() -> {
            try {
                var res = http.send(req, HttpResponse.BodyHandlers.ofString());

                if (res.statusCode() == 204) {
                    Platform.runLater(() -> {
                        communityDepletedLabel.setText("Community Depleted: (no data)");
                        gridPortionLabel.setText("Grid Portion: (no data)");
                    });
                    return;
                }
                if (res.statusCode() >= 400) {
                    Platform.runLater(() -> showError("HTTP " + res.statusCode() + " — " + res.body()));
                    return;
                }

                String body = res.body();
                JsonNode root = mapper.readTree(body);

                // Fall A: Prozent-Objekt (gewünschtes Format)
                if (root.has("communityDepleted") || root.has("gridPortion")) {
                    CurrentPercentageDTO dto = mapper.treeToValue(root, CurrentPercentageDTO.class);
                    Platform.runLater(() -> {
                        communityDepletedLabel.setText(String.format("Community Depleted: %.2f %%", dto.communityDepleted));
                        gridPortionLabel.setText(String.format("Grid Portion: %.2f %% (hour %s)", dto.gridPortion, dto.hour));
                    });
                    return;
                }

                // Fall B (Fallback): Es kamen Usage-Daten, wir rechnen um
                if (root.has("communityUsed") || root.has("gridUsed") || root.has("communityProduced")) {
                    EnergyUsageDTO u = mapper.treeToValue(root, EnergyUsageDTO.class);

                    double used = u.communityUsed;
                    double grid = u.gridUsed;
                    double gridPortion = (used > 0) ? (grid / used) * 100.0 : 0.0;
                    // einfache Annahme: "Depleted" ~ Anteil NICHT aus dem Grid (für Fallback ausreichend)
                    double communityDepleted = Math.max(0.0, Math.min(100.0, 100.0 - gridPortion));

                    Platform.runLater(() -> {
                        communityDepletedLabel.setText(String.format("Community Depleted: %.2f %%", communityDepleted));
                        gridPortionLabel.setText(String.format("Grid Portion: %.2f %% (hour %s)", gridPortion, u.hour));
                    });
                    return;
                }

                // Unbekanntes Format
                Platform.runLater(() -> showError("Unexpected response for /energy/current: " + body));

            } catch (Exception ex) {
                showError("Error fetching current data: " +
                        (ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage()));
            }
        }).start();
    }

    /** Historische Nutzungsdaten (unverändert) */
    private void fetchHistorical(DatePicker startDate, DatePicker endDate, TextArea out) {
        if (startDate.getValue() == null || endDate.getValue() == null) {
            showError("Please select both start and end dates.");
            return;
        }
        String start = startDate.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String end = endDate.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE);

        String url = API_URL + "/historical?start=" + start + "&end=" + end;
        var req = HttpRequest.newBuilder(URI.create(url)).GET().build();

        new Thread(() -> {
            try {
                var res = http.send(req, HttpResponse.BodyHandlers.ofString());

                if (res.statusCode() >= 400) {
                    Platform.runLater(() -> showError("HTTP " + res.statusCode() + " — " + res.body()));
                    return;
                }

                EnergyUsageDTO[] list = mapper.readValue(res.body(), EnergyUsageDTO[].class);

                String text = Arrays.stream(list)
                        .map(d -> String.format("%s  produced=%.3f  used=%.3f  grid=%.3f",
                                d.hour, d.communityProduced, d.communityUsed, d.gridUsed))
                        .collect(Collectors.joining("\n"));

                Platform.runLater(() -> out.setText(text.isEmpty() ? "(no data)" : text));
            } catch (Exception ex) {
                Platform.runLater(() -> showError("Error fetching historical data: " +
                        (ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage())));
            }
        }).start();
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void toggleAutoRefresh(Button btn) {
        if (autoRefreshRunning) {
            if (autoRefreshTimer != null) {
                autoRefreshTimer.cancel();
                autoRefreshTimer = null;
            }
            autoRefreshRunning = false;
            btn.setText("Start Auto-Refresh");
        } else {
            autoRefreshTimer = new Timer(true);
            autoRefreshTimer.scheduleAtFixedRate(new TimerTask() {
                @Override public void run() { fetchCurrent(); }
            }, 0, 10_000);
            autoRefreshRunning = true;
            btn.setText("Stop Auto-Refresh");
        }
    }

    // === DTOs ===

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CurrentPercentageDTO {
        @JsonAlias({"hour", "hourIso"})
        public String hour;

        @JsonAlias({"communityDepleted", "community_depleted"})
        public double communityDepleted;

        @JsonAlias({"gridPortion", "grid_portion"})
        public double gridPortion;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EnergyUsageDTO {
        @JsonAlias({"hour", "hourIso"})
        public String hour;

        public double communityProduced;
        public double communityUsed;
        public double gridUsed;
    }

    public static void main(String[] args) {
        launch();
    }
}
