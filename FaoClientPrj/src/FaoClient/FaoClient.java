/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package FaoClient;
import java.sql.*;
import java.util.Vector;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
public class FaoClient {

    /**
     * @param args the command line arguments
     */
    public static void DropAllTableInDataBase(Connection conn){
        try{
                Statement st = conn.createStatement();
               DatabaseMetaData metadata=  conn.getMetaData();
              ResultSet result= metadata.getTables(null, null, "%", null);
              while(result.next()){
                 String tableName =result.getString("TABLE_NAME");
                 String query = "drop table "+tableName+";";
                 st.executeUpdate(query);
             }
                st.close();
        }
        catch(SQLException E){
            System.out.print("Unable to Delete the table in DropAllTableFun"+E);
        }
    }
    public static void main(String[] args) {
        String dataSourceName = "testMyDB";
        String dbURL = "jdbc:odbc:"+dataSourceName;
        try{
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            Connection con = DriverManager.getConnection(dbURL);
            /*
            Statement s = con.createStatement();
            s.execute("create table school2(Grade integer,Remark text)");*/
            TableCol col = new TableCol(1, TableCol.ColType.STRINGT,"codeISO3");
            Vector<TableCol>colVector = new Vector();
            colVector.add(col);
            CFaoTable tableOb = new CFaoTable();
            tableOb.SetColumns(colVector);
            tableOb.SetTableName("Countries");
            tableOb.SetDataSource("http://www.fao.org/countryprofiles/geoinfo/ws/allCountries/EN/");
            tableOb.SetSourceType(CFaoTable.SourceType.Full);
            tableOb.SetSourceNode("//self_governing");
            SAXReader reader = new SAXReader();
            DropAllTableInDataBase(con);
            tableOb.CreateTable(con);
            tableOb.InsertBatchFromFullSource(con, reader);
            //s.close();  
            con.close();    
        }
        catch(Exception E){
            System.out.println("Kamilian"+E);   
        }
        }
        
    }
