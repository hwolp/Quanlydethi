package quanlydethi.dbconnector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLServerConnector {

    private static final String DB_SERVER_NAME = "LAPTOP-TF3R4DSP"; 
    private static final String DB_INSTANCE_NAME = "MSSQLSERVER01"; 
    private static final String DB_PORT = "1433"; 
    private static final String DB_NAME = "Quanlydethi";     

    private static String getWindowsAuthConnectionString() {
        String connectionUrl = "jdbc:sqlserver://" + DB_SERVER_NAME;
        if (DB_INSTANCE_NAME != null && !DB_INSTANCE_NAME.trim().isEmpty()) {
            connectionUrl += "\\" + DB_INSTANCE_NAME;
        }
        connectionUrl += ":" + DB_PORT +
                         ";databaseName=" + DB_NAME +
                         ";integratedSecurity=true" +
                         ";encrypt=true" +
                         ";trustServerCertificate=true" +
                         ";loginTimeout=30;";
        return connectionUrl;
    }


    public static Connection getConnection() throws SQLException {
        Connection conn = null;
        try {
            String connectionUrl = getWindowsAuthConnectionString();
            conn = DriverManager.getConnection(connectionUrl);
            System.out.println("Kết nối đến SQL Server thành công!");

        } catch (SQLException e) {
            System.err.println("Lỗi kết nối SQL Server: " + e.getMessage());
            e.printStackTrace();
            throw e; 
        }
        return conn;
    }
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("Kiểm tra kết nối thành công và đã đóng kết nối.");
            } else {
                System.out.println("Kiểm tra kết nối thất bại.");
            }
        } catch (SQLException e) {
         
        }
    }
}