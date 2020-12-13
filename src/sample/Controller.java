package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    static final String DB_URL = "jdbc:postgresql://127.0.0.1:5432/market";
    static final String USER = "postgres";
    static final String PASS = "admin";

    private ObservableList<ObservableList> data;
    private ArrayList<String> columns = new ArrayList<String>();
    public String choiceBoxValue = "tracks";
    private boolean ascdesc = true;
    private int index = -1;
    private ArrayList<TextField> textFields = new ArrayList<>();
    @FXML
    private TableView tableDB = new TableView();
    @FXML
    private ChoiceBox tablesChoiceBox = new ChoiceBox();
    @FXML
    private ChoiceBox columnsChoiceBox = new ChoiceBox();
    @FXML
    private Button insertButton = new Button();

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

    @FXML
    private void insertWindow(ActionEvent event) throws IOException {
        Stage stage = new Stage();
        FXMLDocumentController(stage);
    }

    protected void FXMLDocumentController(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("add.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 400, 400);
        stage.getIcons().add(new Image("file:src/sample/icon.png"));
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Добавить запись");
        AddController addController = (AddController) loader.getController();
        addController.transport(columns, choiceBoxValue);
        stage.show();
    }

    @FXML
    private void insertUpdateWindow(ActionEvent event) throws IOException {
        Stage stage = new Stage();
        FXMLDocumentUpdateController(stage);
    }

    protected void FXMLDocumentUpdateController(Stage stage) throws IOException {
        if (index != -1) {
            ObservableList row = (ObservableList) tableDB.getItems().get(index);
            System.out.println("Изменяем строку: " + row);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("update.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 400, 400);
            stage.getIcons().add(new Image("file:src/sample/icon.png"));
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setTitle("Изменить запись");
            UpdateController updateController = (UpdateController) loader.getController();
            updateController.transport(columns, choiceBoxValue, index, row);
            stage.show();
        }
    }

    public void buildData() {
        columns.clear();
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
                System.out.println("Column ["+i+"]: " + col.getText());
                columns.add(col.getText());
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
            updateGroup();
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("Error on Building Data");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        buildData();
        tablesChoiceBox.getItems().removeAll(tablesChoiceBox.getItems());
        tablesChoiceBox.getItems().addAll("tracks", "authors", "orders", "mood", "genres", "licenses", "playlists", "buyers", "payment_methods", "buyers_favorite_tracks", "authors_tracks", "playlists_tracks");
        tablesChoiceBox.getSelectionModel().select("tracks");
        updateGroup();
        ChangeListener<String> changeListener = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                choiceBoxValue = newValue;
                buildData();
            }
        };
        tablesChoiceBox.getSelectionModel().selectedItemProperty().addListener(changeListener);

        tableDB.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                index = tableDB.getSelectionModel().getSelectedIndex();
                //Person person = table.getItems().get(index);
                System.out.println(index);
            }
        });
    }

    public void grouping() {
        columns.clear();
        for (int i = 0; i < tableDB.getColumns().size(); i++) {
            tableDB.getColumns().clear();
        }
        Connection c;
        data = FXCollections.observableArrayList();
        String group = "";

        try {
            c = getDBConnection();
            if (ascdesc) {
                group = "SELECT * from market.public." + choiceBoxValue + " ORDER BY " + columnsChoiceBox.getValue() + " ASC";
            } else {
                group = "SELECT * from market.public." + choiceBoxValue + " ORDER BY " + columnsChoiceBox.getValue() + " DESC";
            }
            ResultSet rs = c.createStatement().executeQuery(group);

            for(int i=0 ; i<rs.getMetaData().getColumnCount(); i++){
                final int j = i;
                TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i+1));
                col.setCellValueFactory((Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>) param -> new SimpleStringProperty(param.getValue().get(j).toString()));
                tableDB.getColumns().addAll(col);
                System.out.println("Column ["+i+"]: " + col.getText());
                columns.add(col.getText());
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

    public void asc() {
        ascdesc = true;
        grouping();
    }

    public void desc() {
        ascdesc = false;
        grouping();
    }

    private void updateGroup() {
        columnsChoiceBox.getItems().removeAll(columnsChoiceBox.getItems());
        for (int i = 0; i < columns.size(); i++) {
            columnsChoiceBox.getItems().add(columns.get(i));
        }
        columnsChoiceBox.getSelectionModel().select(columns.get(0));
    }

    private void updateData(String column, String newValue, String id) {
        try (
                Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement stmt = connection.prepareStatement("UPDATE " + choiceBoxValue + " SET " + column + " = ? WHERE" + id + " = ? ");
        ) {
            stmt.setString(1, newValue);
            stmt.setString(2, id);
            stmt.execute();
        } catch (SQLException ex) {
            System.err.println("Error");
            ex.printStackTrace(System.err);
        }
    }

    public void delete() {
        ObservableList row = (ObservableList) tableDB.getItems().get(index);
        System.out.println("Удаляем строку: " + row);
        String SQL = "DELETE FROM market.public." + choiceBoxValue + " WHERE " + columns.get(0) + " = " + row.get(0).toString();
        System.out.println(SQL);
        try {
            Connection conn = getDBConnection();
            conn.createStatement().executeQuery(SQL);
            System.out.println("Удалили запись" + row);
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
