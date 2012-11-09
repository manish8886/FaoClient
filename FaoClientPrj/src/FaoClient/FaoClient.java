/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package FaoClient;

import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;
import java.sql.*;

public class FaoClient {

    public static void DropAllTableInDataBase(Connection conn) {
        try {
            Statement st = conn.createStatement();
            DatabaseMetaData metadata = conn.getMetaData();
            ResultSet result = metadata.getTables(null, null, "%", null);
            while (result.next()) {
                String tableName = result.getString("TABLE_NAME");
                String query = "drop table " + tableName + ";";
                st.executeUpdate(query);
            }
            st.close();
        } catch (SQLException E) {
            System.out.print("Unable to Delete the table in DropAllTableFun" + E);
        }
    }

    public static void main(String[] args) {
        try {
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter the Hostname:");
            String hostname = console.readLine();
            System.out.println("Enter the Portnumber:");
            String portnumber = console.readLine();
            System.out.println("Enter the Databasename from which you want to connect:");
            String dbName = console.readLine();
            System.out.println("Enter User Name for the database:");
            String userName = console.readLine();
            System.out.println("Enter Password:");
            String passWord = console.readLine();
//            System.out.println("Do you want to delete existing tables");
//            String strDeletion = console.readLine();
            String strDbLocation = "jdbc:mysql://";
            Connection conn;
            Class.forName("com.mysql.jdbc.Driver");
            strDbLocation = strDbLocation + hostname + ":" + portnumber + "/" + dbName;
            String strPassWord = new String(passWord);
            conn = DriverManager.getConnection(strDbLocation, userName, strPassWord);
            CEngine engineInst = CEngine.CreateEngineInstance();
            if (engineInst.Initialise(conn, "E:\\Data\\Study\\Home\\DIC_PROJ1\\FaoClient\\FaoClientPrj\\PrivateDataFiles\\Main.xml")) {
                boolean bSuccess = engineInst.Start();
                if (bSuccess) {
                    System.out.println("All data has been stored in the database");
                }
            }
        } catch (Exception E) {
            System.out.println("Unable to connect to database" + E);
            return;
        }


    }
}
