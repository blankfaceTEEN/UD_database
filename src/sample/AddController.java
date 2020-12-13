package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.DateTimeStringConverter;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class AddController  implements Initializable {
    static final String DB_URL = "jdbc:postgresql://127.0.0.1:5432/market";
    static final String USER = "postgres";
    static final String PASS = "admin";

    @FXML
    private Label label1 = new Label();
    @FXML
    private Label label2 = new Label();
    @FXML
    private Label label3 = new Label();
    @FXML
    private Label label4 = new Label();
    @FXML
    private Label label5 = new Label();
    @FXML
    private Label label6 = new Label();
    @FXML
    private Label label7 = new Label();
    @FXML
    private Label label8 = new Label();

    @FXML
    private TextField column1 = new TextField();
    @FXML
    private TextField column2 = new TextField();
    @FXML
    private TextField column3 = new TextField();
    @FXML
    private TextField column4 = new TextField();
    @FXML
    private TextField column5 = new TextField();
    @FXML
    private TextField column6 = new TextField();
    @FXML
    private TextField column7 = new TextField();
    @FXML
    private TextField column8 = new TextField();

    private Label[] labels = new Label[8];
    private TextField[] textFields = new TextField[8];
    private ArrayList<String> columns = new ArrayList<String>();
    private String choiceBoxValue = "tracks";
    private int sourceSize = 0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void transport(ArrayList<String> columns, String choiceBoxValue) {
        this.columns = columns;
        sourceSize = columns.size();
        this.choiceBoxValue = choiceBoxValue;
        try {
            initLabels();
        } catch (ParseException e) {
            System.out.println("Ошибка даты");
            e.printStackTrace();
        }
    }

    private void initLabels() throws ParseException {
        labels[0] = label1;
        labels[1] = label2;
        labels[2] = label3;
        labels[3] = label4;
        labels[4] = label5;
        labels[5] = label6;
        labels[6] = label7;
        labels[7] = label8;

        textFields[0] = column1;
        textFields[1] = column2;
        textFields[2] = column3;
        textFields[3] = column4;
        textFields[4] = column5;
        textFields[5] = column6;
        textFields[6] = column7;
        textFields[7] = column8;

        for (int i = 0; i < columns.size(); i++) {
            labels[i].setText(columns.get(i));
            textFields[i].setEditable(true);
        }

        textFields[0].textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    textFields[0].setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });

        if (choiceBoxValue.equals("authors_tracks") || choiceBoxValue.equals("playlists_tracks") || choiceBoxValue.equals("buyers_favorite_tracks")) {
            textFields[1].textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue,
                                    String newValue) {
                    if (!newValue.matches("\\d*")) {
                        textFields[1].setText(newValue.replaceAll("[^\\d]", ""));
                    }
                }
            });
        } else if (choiceBoxValue.equals("orders")) {
            textFields[1].textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    textFields[1].setText(newValue.replaceAll("[^\\d{4}[-]\\d{2}[-]\\d{2}]", ""));
                }
            });
        } else if (choiceBoxValue.equals("buyers") || choiceBoxValue.equals("authors")) {

        } else {
            textFields[1].textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\sa-zA-Zа-яА-Я*")) {
                    textFields[1].setText(newValue.replaceAll("[^\\sa-zA-Zа-яА-Я]", ""));
                }
            });
        }

        for(int i = columns.size(); i < 8; i++) {
            columns.add("");
            System.out.println(columns.size());
        }
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public void addRecord() {
        System.out.println("Идёт добавление записи!");
        ArrayList<String> into = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();

        for(int i = 0; i < 8; i++) {
            if (i != 7 && !columns.get(i).equals("") && !textFields[i].getText().equals("") && i != sourceSize - 1) {
                into.add(columns.get(i) + ",");
                if (!columns.get(i).equals("track_id") && !columns.get(i).equals("buyer_id")
                        && !columns.get(i).equals("author_id") && !columns.get(i).equals("order_id")
                        && !columns.get(i).equals("genre_id") && !columns.get(i).equals("mood_id")
                        && !columns.get(i).equals("license_id") && !columns.get(i).equals("payment_method_id")
                        && !columns.get(i).equals("playlist_id") && !columns.get(i).equals("bpm")
                        && !columns.get(i).equals("amount") && !columns.get(i).equals("license_price")
                        && !columns.get(i).equals("genre") && !columns.get(i).equals("mood")
                        && !columns.get(i).equals("license") && !columns.get(i).equals("payment_method")
                )
                    values.add("'" + textFields[i].getText() + "',");
                else
                    values.add(textFields[i].getText() + ",");
            } else {
                into.add(columns.get(i));
                if (!columns.get(i).equals("track_id") && !columns.get(i).equals("buyer_id")
                        && !columns.get(i).equals("author_id") && !columns.get(i).equals("order_id")
                        && !columns.get(i).equals("genre_id") && !columns.get(i).equals("mood_id")
                        && !columns.get(i).equals("license_id") && !columns.get(i).equals("payment_method_id")
                        && !columns.get(i).equals("playlist_id") && !columns.get(i).equals("bpm")
                        && !columns.get(i).equals("amount") && !columns.get(i).equals("license_price")
                        && !columns.get(i).equals("genre") && !columns.get(i).equals("mood")
                        && !columns.get(i).equals("license") && !columns.get(i).equals("payment_method")
                )
                    if (i < sourceSize)
                        values.add("'" + textFields[i].getText() + "'");
                else
                    values.add(textFields[i].getText());
            }
        }

        String SQL = "INSERT INTO market.public." + choiceBoxValue +
                " (" + into.get(0) + into.get(1) + into.get(2) + into.get(3) + into.get(4) + into.get(5) + into.get(6)
                + into.get(7) + ")"
                + " VALUES (" + values.get(0) + values.get(1) + values.get(2) + values.get(3) + values.get(4) +
                values.get(5) + values.get(6) + values.get(7) + ")";
        System.out.println(SQL);

        try {
            Connection conn = connect();
            conn.createStatement().executeQuery(SQL);
            System.out.println("Добавили запись");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        for (int i = 0; i < columns.size(); i++) {
            textFields[i].setText("");
        }
    }
}
