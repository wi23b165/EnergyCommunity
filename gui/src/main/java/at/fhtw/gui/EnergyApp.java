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
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

public class EnergyApp extends Application {
    private static final String API_URL = "http://localhost:8081/energy"; // <- API runs on 8081
    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private Label communityLabel;
    private Label gridLabel;

    @Override
    public void start(Stage stage) {
        Label titleLabel = new Label("Energy Community Dashboard");

        // Current data section
        Label currentLabel = new Label("Current Data:");
        communityLabel = new Label("Community Produced: –");
        gridLabel = new Label("Grid Used: –");
        Button refreshButton = new Button("Refresh");

        refreshButton.setOnAction(e -> fetchCurrent());

        // Historical data section
        Label historicalLabel = new Label("Historical Data:");
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
                currentLabel, communityLabel, gridLabel, refreshButton,
                historicalLabel, historicalGrid, showDataButton, resultArea
        );
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 600, 500);
        stage.setScene(scene);
        stage.setTitle("Energy Community");
        stage.show();
    }

    private void fetchCurrent() {
        var req = HttpRequest.newBuilder(URI.create(API_URL + "/current")).GET().build();
        new Thread(() -> {
            try {
                var res = http.send(req, HttpResponse.BodyHandlers.ofString());
                if (res.statusCode() == 204) {
                    Platform.runLater(() -> {
                        communityLabel.setText("Community Produced: (no data)");
                        gridLabel.setText("Grid Used: (no data)");
                    });
                    return;
                }
                EnergyUsageDTO dto = mapper.readValue(res.body(), EnergyUsageDTO.class);
                Platform.runLater(() -> {
                    communityLabel.setText(String.format("Community Produced: %.3f kWh | Used: %.3f kWh", dto.communityProduced, dto.communityUsed));
                    gridLabel.setText(String.format("Grid Used: %.3f kWh (hour %s)", dto.gridUsed, dto.hourIso));
                });
            } catch (Exception ex) {
                showError("Error fetching current data: " + ex.getMessage());
            }
        }).start();
    }

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
                    Platform.runLater(() -> out.setText("Error: " + res.body()));
                    return;
                }
                EnergyUsageDTO[] list = mapper.readValue(res.body(), EnergyUsageDTO[].class);

                String text = Arrays.stream(list)
                        .map(d -> String.format("%s  produced=%.3f  used=%.3f  grid=%.3f",
                                d.hourIso, d.communityProduced, d.communityUsed, d.gridUsed))
                        .collect(Collectors.joining("\n"));   // <— keine reduce-Zeile mehr

                Platform.runLater(() -> out.setText(text.isEmpty() ? "(no data)" : text));
            } catch (Exception ex) {
                showError("Error fetching historical data: " + ex.getMessage());
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

    // minimal DTO to map API responses
    public static class EnergyUsageDTO {
        public String hourIso;
        public double communityProduced;
        public double communityUsed;
        public double gridUsed;
    }

    public static void main(String[] args) {
        launch();
    }
}