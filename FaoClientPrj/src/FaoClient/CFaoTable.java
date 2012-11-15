package FaoClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
 *
 * @author Manish Jain
 */
public class CFaoTable {

    Vector<TableCol> Columns;
    String Name;
    String SrcNodeXML;
    String DataUrl;
    SourceType Type;
    public IFaoTableEngine IEngine;

    enum SourceType {

        Full, Partial
    };

    public CFaoTable() {
        Name = "";
        SrcNodeXML = "";
        DataUrl = "";
        Columns = new Vector();
        Type = SourceType.Partial; //Means the URL of the Data Source is not Complete
    }

    void SetEngineInstance(IFaoTableEngine engine) {
        IEngine = engine;
    }

    String GetTableName() {
        return Name;
    }

    void SetTableName(String str) {
        Name = str;
    }

    void SetColumns(Vector<TableCol> colVec) {
        Columns = colVec;
    }

    void SetDataSource(String Path) {
        DataUrl = Path;
    }

    void SetSourceNode(String NodeName) {
        SrcNodeXML = NodeName;
    }

    void SetSourceType(SourceType t) {
        if (t != Type) {
            Type = t;
        }
    }

    boolean CreateTable(Connection conn) {
        try {
            System.out.println("Creating table with name: "+Name);
            Statement st = conn.createStatement();
            String QueryVal = "create table " + Name + "(";
            for (int index = 0; index < Columns.size(); index++) {
                TableCol col = Columns.get(index);
                QueryVal += col.GetColName();
                QueryVal = QueryVal + " " + TableCol.GetTypeString(col.GetColType());
                if (index != Columns.size() - 1) {
                    QueryVal += ",";
                } else {
                    QueryVal += ")";
                }
            }
            st.executeUpdate(QueryVal);
            System.out.println("Table "+Name+" has been created");
            SetPrimaryKey(conn, st);
            st.close();
            return true;
        } catch (SQLException E) {
            System.out.println("Creation of "+ Name +" has failed. And application is exiting.");
            return false;
        }
    }

    private boolean SetPrimaryKey(Connection conn,Statement st){
        try {
            System.out.println("Setting Primary Key For table with name: " + Name);
            String QueryVal = "alter table " + Name + " add primary key (";
            for (int index = 0; index < Columns.size(); index++) {
                TableCol col = Columns.get(index);
                if (col.IsPrimaryKey()) {
                    QueryVal += col.GetColName();
                    QueryVal += ")";
                    break;
                }

            }
            st.executeUpdate(QueryVal);
            System.out.println("Key has been set for table: "+Name);
            st.close();
            return true;
        } catch (SQLException E) {
            System.out.println("Key Couldn't be set for "+ Name +". And application is exiting.");
            return false;
        }        
    } 
    public boolean StartLogging(Connection conn, SAXReader reader) {
        if (SrcNodeXML == "" || DataUrl == "") {
            return false;
        }
        if (Type == CFaoTable.SourceType.Full) {
            return InsertFromFullSource(conn, reader);
        } else {
            return InsertFromPartialSource(conn, reader);
        }

    }

    private boolean InsertFromPartialSource(Connection conn, SAXReader reader) {
        try {
            if (conn == null || reader == null) {
                return false;
            }
            String SrcTblName = IEngine.GetSourceTableName();
            String srcColName = Columns.get(Columns.size() - 1).GetColName();
            //Partial means, that this table's data link is not complete, and this
            // wil be complete by after taking the data from the other table.We are assuming
            //here we only need the data for the last col of this table and we need to take
            // the data from the firts table of the SrcTable.Somehow, this will become the foregin key here
            String selectQuery = "select " + srcColName + " from " + SrcTblName;
            Statement st = conn.createStatement();
            ResultSet result = st.executeQuery(selectQuery);
            conn.setAutoCommit(false);
            String strPs= CreatePreparedInsertStatement();
            PreparedStatement ps = conn.prepareStatement(strPs);
            String xPath = "//" + SrcNodeXML;
            int rowCount = 0;
            while (result.next()) {
                String linkField = result.getString(srcColName);
                String strRowLink = DataUrl + linkField;
                //Now be patient, the thing is that every link might not be reachable so check for that    
                //first and then proceed.
                HttpURLConnection connection = (HttpURLConnection) new URL(strRowLink).openConnection();
                if (connection == null) {
                    continue;
                }
                connection.setRequestMethod("HEAD");
                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    //Means there is some some problem in connecting to that URL.
                    //so skip that page and get the next connection or row.
                    continue;
                }
                //Now after implementing the link we need to query the row data for the
                //columns excluding the last col
                Document doc = reader.read(strRowLink);
                List listOfNode = doc.selectNodes(xPath);
                Iterator iter = listOfNode.iterator();
                while (iter.hasNext()) {
//                Node rowNode = doc.selectSingleNode(xPath);
                    Node rowNode = (Node) iter.next();
                    if (rowNode == null) {//check the next node
                        continue;
                    }
                    for (int index = 0; index < Columns.size(); index++) {
                        TableCol col = Columns.get(index);
                        String colVal = "";
                        if(col.IsAutoIncr()){
                            colVal = colVal+(rowCount+1);
                        }
                        else if (index != Columns.size() - 1) {
                            Node colNode = rowNode.selectSingleNode(col.GetColName());
                            if (colNode != null) {
                                colVal = colNode.getText();
                            }
                        } else {
                            colVal = linkField;
                        }
                        if (!BindValueToStmt(ps, index + 1, col.GetColType(), colVal)) {
                            ps.setNull(index + 1, TableCol.GetSqlColTypeFrmType(col.GetColType()));
                        }
                    }
                    rowCount++;
                    ps.addBatch();
                    if (rowCount % 100 == 0) {
                        ps.executeBatch();
                        conn.commit();
                        conn.setAutoCommit(false);
                    }
                }
            }
            if (rowCount > 0) {
                 int[]updateCount = ps.executeBatch();
            }
            conn.commit();
            conn.setAutoCommit(true);
            ps.close();
            st.close();
            return true;
        } catch (SQLException E) {
            System.out.println("Error in insert in to table: " + Name + E);
            E.printStackTrace();
            return false;
        } catch (DocumentException E) {
            System.out.println("Error in opening the url in InsertFromPartialSource Fun: " + E);
            E.printStackTrace();
            return false;
        } catch (MalformedURLException e) {
            System.out.println("URL field is not proper " + e);
            e.printStackTrace();
            return false;

        } catch (IOException e) {
            System.out.println("Link is not responding" + e);
            e.printStackTrace();
            return false;
        } catch (NullPointerException E) {
            System.out.println("Null Pointer Exception in PartialInsert" + E);
            E.printStackTrace();
            return false;
        }

    }

    private boolean InsertFromFullSource(Connection conn, SAXReader reader) {
        try {
            Document doc = reader.read(DataUrl);
            String xPath = "//" + SrcNodeXML;
            List listOfNode = doc.selectNodes(xPath);
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement(CreatePreparedInsertStatement());
            Iterator iter = listOfNode.iterator();
            int count = 0;
            while (iter.hasNext()) {
                Node rowElement = (Node) iter.next();
                for (int index = 0; index < Columns.size(); index++) {
                    TableCol col = Columns.get(index);
                    String colVal = rowElement.selectSingleNode(col.GetColName()).getText();
                    ps.setObject(index + 1, colVal);
                }
                count++;
                ps.addBatch();
                if (count % 100 == 0) {
                    ps.executeBatch();
                }
            }
            if (count > 0) {
                int RecordsUpdated[] = ps.executeBatch();
            }
            conn.commit();
            ps.close();
            return true;
        } catch (SQLException E) {
            System.out.println("Error in insert in to table: " + Name + E);
            return false;
        } catch (DocumentException E) {
            System.out.println("Error in opening the url in insertFromFullSrc Fun: " + E);
            return false;
        }
    }

    private String CreatePreparedInsertStatement() {
        StringBuilder insertStmt = new StringBuilder("insert into " + Name + "(");
        String val = " values (";
        for (int index = 0; index < Columns.size(); index++) {
            TableCol col = Columns.get(index);
            if (index != Columns.size() - 1) {
                String Stmt = col.GetColName() + ", ";
                insertStmt.append(Stmt);
                val += "?, ";
            } else {
                String Stmt = col.GetColName() + ")";
                insertStmt.append(Stmt);
                val += "?)";
            }
        }
//        insertStmt = insertStmt + val;
        insertStmt.append(val);
        return insertStmt.toString();
    }

    private boolean BindValueToStmt(PreparedStatement ps, int index, TableCol.ColType type, String ColVal) {
        if ((ps == null) || (ColVal == "")) {
            return false;
        }
        try {
            switch (type) {
                case INTEGERT: {
                    ps.setInt(index, Integer.parseInt(ColVal));
                    return true;
                }
                case STRINGT: {
                    ps.setString(index, ColVal);
                    return true;
                }
                case REALT: {
                    ps.setFloat(index, Float.parseFloat(ColVal));
                    return true;
                }
                case DOUBLET: {
                    ps.setDouble(index, Double.parseDouble(ColVal));
                    return true;
                }
                default:
                    ps.setNull(index, java.sql.Types.NULL);
            }
            return true;
        } catch (SQLException E) {
            System.out.println("Error in Setting the value of the col in table: " + Name + E);
            E.printStackTrace();
            return false;
        }
    }
}
