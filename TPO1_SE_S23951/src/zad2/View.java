package zad2;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;

public class View {

    private static final String WIKI_BASE_URL = "https://pl.wikipedia.org/wiki/";

    public void init() {
        String country = JOptionPane.showInputDialog("Wprowadz nazwe kraju:");
        String city = JOptionPane.showInputDialog("Wprowadz nazwe miasta:");
        String currency = JOptionPane.showInputDialog("Wprowadz kod waluty, np. EUR:");

        Service service = new Service(country);
        String weatherJson = service.getWeather(city);
        Double rate1 = service.getRateFor(currency);
        Double rate2 = service.getNBPRate();

        SwingUtilities.invokeLater(() -> {
            JFrame appFrame = new JFrame("Serwis Informacyjny");
            appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            JLabel weatherLabel = new JLabel("Aktualna pogoda: " + weatherJson);
            JLabel rate1Label = new JLabel("Kurs " + currency + ": " + rate1);
            JLabel rate2Label = new JLabel("Kurs PLN: " + rate2);

            panel.add(weatherLabel);
            panel.add(rate1Label);
            panel.add(rate2Label);

            JFXPanel jfxPanel = new JFXPanel();
            panel.add(jfxPanel);
            appFrame.add(panel);
            appFrame.pack();
            appFrame.setSize(1280, 800);
            appFrame.setVisible(true);

            Platform.runLater(() -> {
                WebView webView = new WebView();
                webView.setPrefSize(1200, 600);
                WebEngine webEngine = webView.getEngine();
                webEngine.load(WIKI_BASE_URL + city);
                jfxPanel.setScene(new Scene(webView));
            });
        });
    }

}


