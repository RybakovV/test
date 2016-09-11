package ua.com.juja.sqlcmd.model;

import java.sql.*;
import java.util.Arrays;


public class PostgresqlDatabaseManager implements DatabaseManager {

    private Connection connection;
    private String databaseName;
    private String userName;
    private String userPassword;

    @Override
    public String getTableString(String tableName) {
        int maxColumnSize;
            maxColumnSize = getMaxColumnSize(tableName);
        if (maxColumnSize==0){
            return getEmptyTable(tableName);
        }else{
            return getHeaderOfTheTable(tableName) + getStringTableData(tableName);
        }
    }

    @Override
    public String getStringTableData(String tableName) {
        int rowsCount;
        rowsCount = getCountRows(tableName);
        int maxColumnSize = getMaxColumnSize(tableName);
        String result = "" ;
        if (maxColumnSize%2==0){
            maxColumnSize+=2;
        }else{
            maxColumnSize+=3;
        }
        Statement statement;
        ResultSet resultSet;

        try {
            int columnCount = getColumnCount(tableName);
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM public." + tableName);
            while (resultSet.next()) {
                result += "║";
                for (int i = 1; i <= columnCount; i++) {
                    if (resultSet.getString(i).length() % 2 == 0) {
                        for (int j = 0; j < (maxColumnSize - resultSet.getString(i).length()) / 2; j++) {
                            result += " ";
                        }
                        result += resultSet.getString(i);
                        for (int j = 0; j < (maxColumnSize - resultSet.getString(i).length()) / 2; j++) {
                            result += " ";
                        }
                        result += "║";
                    } else {
                        for (int j = 0; j < (maxColumnSize - resultSet.getString(i).length()) / 2; j++) {
                            result += " ";
                        }
                        result += resultSet.getString(i);
                        for (int j = 0; j <= (maxColumnSize - resultSet.getString(i).length()) / 2; j++) {
                            result += " ";
                        }
                        result += "║";
                    }
                }
                result +="\n";
                if (rowsCount > 1) {
                    result += "╠";
                    for (int j = 1; j < columnCount ; j++) {
                        for (int i = 0; i < maxColumnSize; i++) {
                            result += "═";
                        }
                        result += "╬";
                    }
                    for (int i = 0; i < maxColumnSize; i++) {
                        result += "═";
                    }
                    result += "╣\n";
                }else{
                    result += "╚";
                    for (int j = 1; j < columnCount ; j++) {
                        for (int i = 0; i < maxColumnSize; i++) {
                            result += "═";
                        }
                        result += "╩";
                    }
                    for (int i = 0; i < maxColumnSize; i++) {
                        result += "═";
                    }
                    result += "╝\n";
                }
                rowsCount--;
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String getHeaderOfTheTable(String tableName) {
        int maxColumnSize = getMaxColumnSize(tableName);
        if (maxColumnSize == 0) {
            return getEmptyTable(tableName);
        }
        String result = "";
        Statement statement;
        ResultSet resultSet;
        int columnCount;
        try {
            if (maxColumnSize % 2 == 0) {
                maxColumnSize += 2;
            } else {
                maxColumnSize += 3;
            }
            columnCount = getColumnCount(tableName);
            result += "╔";
            for (int j = 1; j < columnCount; j++) {
                for (int i = 0; i < maxColumnSize; i++) {
                    result += "═";
                }
                result += "╦";
            }
            for (int i = 0; i < maxColumnSize; i++) {
                result += "═";
            }
            result += "╗\n";
            statement = connection.createStatement();
            resultSet = statement.executeQuery(
                    "SELECT * FROM information_schema.columns " +
                            "WHERE table_schema = 'public' " +
                            "AND table_name = '" + tableName + "'");
            while (resultSet.next()) {
                result += "║";
                if (resultSet.getString("column_name").length() % 2 == 0) {
                    for (int i = 0; i < (maxColumnSize - resultSet.getString("column_name").length()) / 2; i++) {
                        result += " ";
                    }
                    result += resultSet.getString("column_name");
                    for (int i = 0; i < (maxColumnSize - resultSet.getString("column_name").length()) / 2; i++) {
                        result += " ";
                    }
                } else {
                    for (int i = 0; i < (maxColumnSize - resultSet.getString("column_name").length()) / 2; i++) {
                        result += " ";
                    }
                    result += resultSet.getString("column_name");
                    for (int i = 0; i <= (maxColumnSize - resultSet.getString("column_name").length()) / 2; i++) {
                        result += " ";
                    }
                }
            }
            result += "║\n";

            //last string of the header
            if (getCountRows(tableName) != 0) {
                result += "╠";
                for (int j = 1; j < columnCount; j++) {
                    for (int i = 0; i < maxColumnSize; i++) {
                        result += "═";
                    }
                    result += "╬";

                }
                for (int i = 0; i < maxColumnSize; i++) {
                    result += "═";
                }
                result += "╣\n";
            } else {
                result += "╚";
                for (int j = 1; j < columnCount; j++) {
                    for (int i = 0; i < maxColumnSize; i++) {
                        result += "═";
                    }
                    result += "╩";

                }
                for (int i = 0; i < maxColumnSize; i++) {
                    result += "═";
                }
                result += "╝\n";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public DataSet[] getTableData(String tableName){

        int tableSize = getCountRows(tableName);
        DataSet[] result = new DataSet[tableSize];
        Statement statement;
        ResultSet resultSet;
        try {
            statement = connection.createStatement();
            if (statement != null) {
                resultSet = statement.executeQuery("SELECT * FROM public." + tableName);
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                int index = 0;
                int columnCount = getColumnCount(tableName);
                while (resultSet.next()){
                    DataSet dataSet = new DataSet();
                    for (int i = 1; i <= columnCount; i++) {
                        dataSet.put(resultSetMetaData.getColumnName(i), resultSet.getObject(i));
                    }
                    result[index] = dataSet;
                    index++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String getEmptyTable(String tableName) {
        String textEmptyTable="║ Table '" + tableName + "' is empty or does not exist ║";
        String result = "╔";
        for (int i = 0; i < textEmptyTable.length()-2; i++) {
            result += "═";
        }
        result += "╗\n";
        result += textEmptyTable + "\n";
        result += "╚";
        for (int i = 0; i < textEmptyTable.length()-2; i++) {
            result += "═";
        }
        result += "╝";
        return result;
    }

    @Override
    public int getMaxColumnSize(String tableName) {
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (statement != null) {
                resultSet = statement.executeQuery(
                        "SELECT * FROM information_schema.columns " +
                                "WHERE table_schema = 'public' " +
                                "AND table_name = '" + tableName + "'");
            }

            if (resultSet != null){
                if (resultSet.next()) {
                    int maxLength = resultSet.getString("column_name").length();
                    while (resultSet.next()) {
                        if (maxLength < resultSet.getString("column_name").length()) {
                            maxLength = resultSet.getString("column_name").length();
                        }
                    }
                    resultSet = statement.executeQuery("SELECT * FROM public." + tableName);
                    ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                    int columnCount = resultSetMetaData.getColumnCount();
                    while (resultSet.next()) {
                        for (int i = 1; i <= columnCount; i++) {
                            if (maxLength < resultSet.getString(i).length()) {
                                maxLength = resultSet.getString(i).length();
                            }
                        }
                    }
                    resultSet.close();
                    statement.close();
                    return maxLength;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }


    private int getCountRows(String tableName) {
        Statement statement;
        int countRows = 0;
        ResultSet resultSet;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT COUNT(*) FROM public." + tableName);
            resultSet.next();
            countRows = resultSet.getInt(1);
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return countRows;
    }

    @Override
    public String[] getAllTablesOfDataBase() {

        int countTables = 0;
        String[] tables = new String[100];
        String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema='public'";

        Statement statement = null;
        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Can't connect to Database");
        }
        ResultSet resultSet;
        try {
            if (statement != null) {
                resultSet = statement.executeQuery(sql);
                while (resultSet.next()) {
                    tables[countTables] = resultSet.getString("table_name");
                    countTables++;
                }
                resultSet.close();
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Can't to perform query");

        }
        tables = Arrays.copyOf(tables, countTables, String[].class);
        return tables;
    }

    @Override
    public void connectToDataBase(String database, String user, String password){
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Please register you JDBC driver", e);
        }
        try {
            String url = "jdbc:postgresql://localhost/" + database;
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException ePostgresConnection) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException eClassNotFoundMySQL) {
                throw new RuntimeException("Please register you JDBC driver", eClassNotFoundMySQL);
            }
            try {
                String url = "jdbc:mysql://127.0.0.1:3306/" + database + "?useSSL=false";
                connection = DriverManager.getConnection(url, user, password);
            } catch (SQLException eMySQLException) {
                connection = null;
                throw new RuntimeException(String.format("Can't connect to Database: %s by User: %s or Password: %s. ", database, user, password), eMySQLException);
            }
        }
    }

    @Override
    public void update(String tableName, int id, DataSet data) {

        try {
            Statement statement;
            statement = connection.createStatement();

            String columnNameSet = Arrays.toString(data.getColumnNames());
            columnNameSet = columnNameSet.substring(1,columnNameSet.length()-1);

            String valueSet = data.getValuesString();

            String sql = "UPDATE public." + tableName + " SET (" + columnNameSet + ") = (" + valueSet + ") WHERE id = " + id;

            statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getColumnCount(String tableName) {
        Statement statement;
        int columnCount = 0;
        ResultSetMetaData resultSetMetaData;
        try {
            statement = connection.createStatement();
            ResultSet resultSet;
            if (statement != null) {
                resultSet = statement.executeQuery("SELECT * FROM public." + tableName);
                resultSetMetaData = resultSet.getMetaData();
                columnCount = resultSetMetaData.getColumnCount();
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            return columnCount;
        }
        return columnCount;
    }

    @Override
    public String[] getColumnNames(String tableName) {
        int columnCount = getColumnCount(tableName);
        String[] columnNames = new String[columnCount];
        if (columnCount>0){
            Statement statement;
            ResultSet resultSet;
            ResultSetMetaData resultSetMetaData;
            try {
                statement = connection.createStatement();
                resultSet = statement.executeQuery("SELECT * FROM public." + tableName);
                resultSetMetaData = resultSet.getMetaData();
                for (int i = 0; i < columnCount ; i++) {
                    columnNames[i] = resultSetMetaData.getColumnName(i+1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
        return columnNames;
    }

    @Override
    public void insert(String tableName, DataSet data) {
        Statement statement;
        try {
            statement = connection.createStatement();

            String valueSet = data.getValuesString();
            String columnNameSet = Arrays.toString(data.getColumnNames());
            columnNameSet = columnNameSet.substring(1,columnNameSet.length()-1);

            String sql = "INSERT INTO public." + tableName +  "(" + columnNameSet + ") VALUES (" + valueSet +")";
            statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }



    }

    @Override
    public void clear(String tableName) {
        String sql = "DELETE FROM public." + tableName;
        Statement statement;
        try {
            statement = connection.createStatement();
            statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getUserPassword() {
        return userPassword;
    }

    @Override
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    @Override
    public String getVersionDatabase() {
        Statement statement = null;
        String result = "";
        ResultSet resultSet = null;
        String sql;
        try {
            statement = connection.createStatement();
            sql = "SHOW \"event_source\"";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            result += resultSet.getString(1);
        } catch (SQLException e) {
            try {
                statement = connection.createStatement();
                sql =  "SHOW VARIABLES LIKE \"version_comment\"";
                resultSet = statement.executeQuery(sql);
                resultSet.next();
                result += resultSet.getString(2);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result.split(" ")[0];
    }

    @Override
    public boolean isConnected() {
        return connection != null;
    }

}
