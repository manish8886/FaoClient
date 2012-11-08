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
        class Engine implements IFaoTableEngine{
        String strSrcTblName;
        @Override
        public String GetSourceTableName(){
            return strSrcTblName;
        }
    }
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
            tableOb.SetSourceNode("self_governing");
            
            /*Here Testing code for Partial Table*/
            TableCol col1 = new TableCol(1, TableCol.ColType.REALT, "hasMaxLatitude");
            TableCol col2 = new TableCol(2, TableCol.ColType.STRINGT,"codeISO3");
            Vector<TableCol> colVector1 = new Vector();
            colVector1.add(col1);
            colVector1.add(col2);
            CFaoTable tableOb1 = new CFaoTable();
            tableOb1.SetColumns(colVector1);
            tableOb1.SetTableName("CoOrdinates");
            tableOb1.SetDataSource("http://www.fao.org/countryprofiles/geoinfo/ws/countryCoordinates/");
            tableOb1.SetSourceType(CFaoTable.SourceType.Partial);
            tableOb1.SetSourceNode("Data");
            SAXReader reader = new SAXReader();
            DropAllTableInDataBase(con);
            tableOb.CreateTable(con);
            tableOb.InsertFromFullSource(con, reader);
            Engine faoEngine = new FaoClient().new Engine();
            faoEngine.strSrcTblName = "Countries";
            tableOb1.IEngine = faoEngine;
            tableOb1.CreateTable(con);
            tableOb1.InsertFromPartialSource(con, reader);

            con.close();    
        }
        catch(Exception E){
            System.out.println("Kamilian"+E);   
        }
        }
        
    }
