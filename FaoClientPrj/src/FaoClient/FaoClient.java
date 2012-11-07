/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package FaoClient;
import java.sql.*;
public class FaoClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String dataSourceName = "testMyDB";
        String dbURL = "jdbc:odbc:"+dataSourceName;
        try{
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            Connection con = DriverManager.getConnection(dbURL);
            Statement s = con.createStatement();
            s.execute("create table school2(Grade integer,Remark text)");
            s.close();  
            con.close();    
        }
        catch(Exception E){
            System.out.println("Kamilian"+E);   
        }
        }
        
    }
