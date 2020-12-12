package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    static final String DB_URL = "jdbc:postgresql://127.0.0.1:5432/market";
    static final String USER = "postgres";
    static final String PASS = "admin";

    private ObservableList<ObservableList> data;
    public String choiceBoxValue = "tracks";
    @FXML
    private TableView tableDB = new TableView();
    @FXML
    private ChoiceBox tablesChoiceBox = new ChoiceBox();

    private static Connection getDBConnection() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found. Include it in your library path ");
            e.printStackTrace();
        }

        System.out.println("PostgreSQL JDBC Driver successfully connected");
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (SQLException e) {
            System.out.println("Connection Failed");
            e.printStackTrace();
        }

        if (connection != null) {
            System.out.println("You successfully connected to database now");
        } else {
            System.out.println("Failed to make connection to database");
        }

        return connection;
    }

    private static void insert() throws SQLException {
        Connection dbConnection = null;
        Statement statement = null;
        String insertTableSQL = "";

        try {
            dbConnection = getDBConnection();
            statement = dbConnection.createStatement();

            // выполнить SQL запрос
            statement.execute(insertTableSQL);
            System.out.println("Table \"dbuser\" is created!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }
        }
    }

    public void buildData() {
        for (int i = 0; i < tableDB.getColumns().size(); i++) {
            tableDB.getColumns().clear();
        }
        Connection c;
        data = FXCollections.observableArrayList();

        try {
            c = getDBConnection();
            String SQL = "SELECT * from market.public." + choiceBoxValue;
            ResultSet rs = c.createStatement().executeQuery(SQL);

            for(int i=0 ; i<rs.getMetaData().getColumnCount(); i++){
                final int j = i;
                TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i+1));
                col.setCellValueFactory((Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>) param -> new SimpleStringProperty(param.getValue().get(j).toString()));
                tableDB.getColumns().addAll(col);
                System.out.println("Column ["+i+"] ");
            }

            while(rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for(int i=1 ; i<=rs.getMetaData().getColumnCount(); i++){
                    row.add(rs.getString(i));
                }
                System.out.println("Row [1] added "+row );
                data.add(row);
            }

            tableDB.setItems(data);
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("Error on Building Data");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tablesChoiceBox.getItems().removeAll(tablesChoiceBox.getItems());
        tablesChoiceBox.getItems().addAll("tracks", "authors", "orders", "mood", "genres", "licenses", "playlists", "buyers", "payment_methods", "buyers_favorite_tracks", "authors_tracks", "playlists_tracks");
        tablesChoiceBox.getSelectionModel().select("tracks");
        ChangeListener<String> changeListener = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                choiceBoxValue = newValue;
                buildData();
            }
        };
        tablesChoiceBox.getSelectionModel().selectedItemProperty().addListener(changeListener);
    }
}
