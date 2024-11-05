import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Scanner;

public class Main {
    private static final String API_TOKEN = "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";

    public static void main(String[] args) {
        JFrame frame = new JFrame("Hotdog or Not Hotdog");
        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JLabel label = new JLabel("Choisissez une image pour détecter si c'est un hotdog.");
        JButton chooseButton = new JButton("Choisir une image");
        JTextField filePathField = new JTextField(20);
        filePathField.setEditable(false);

        panel.add(label);
        panel.add(filePathField);
        panel.add(chooseButton);
        frame.add(panel);
        frame.setVisible(true);

        chooseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    filePathField.setText(selectedFile.getAbsolutePath());

                    try {
                        boolean isHotdog = detectHotdogWithAPI(selectedFile);
                        JOptionPane.showMessageDialog(frame, isHotdog ? "C'est un hotdog!" : "Ce n'est pas un hotdog.");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(frame, "Erreur lors de la détection.");
                    }
                }
            }
        });
    }

    private static boolean detectHotdogWithAPI(File imageFile) throws IOException {
        URL statusUrl = new URL("https://api-inference.huggingface.co/models/google/mobilenet_v2_1.0_224");
        HttpURLConnection statusConnection = (HttpURLConnection) statusUrl.openConnection();
        statusConnection.setRequestMethod("GET");
        statusConnection.setRequestProperty("Authorization", "Bearer " + API_TOKEN);

        if (statusConnection.getResponseCode() == 503) {
            System.out.println("indisponible.");
            return false;
        }
        statusConnection.disconnect();

        URL url = new URL("https://api-inference.huggingface.co/models/google/mobilenet_v2_1.0_224");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + API_TOKEN);
        connection.setRequestProperty("Content-Type", "application/octet-stream");
        connection.setDoOutput(true);

        try (OutputStream outputStream = connection.getOutputStream()) {
            Files.copy(imageFile.toPath(), outputStream);
        }

        Scanner scanner = new Scanner(connection.getInputStream());
        StringBuilder response = new StringBuilder();
        while (scanner.hasNext()) {
            response.append(scanner.nextLine());
        }
        scanner.close();
        connection.disconnect();

        return response.toString().toLowerCase().contains("hotdog");
    }
}
