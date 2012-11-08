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

    String GetTableName(){
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

    void CreateTable(Connection conn) {
        try {
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
            st.close();
        } catch (SQLException E) {
            System.out.println("Error in Creating the Table in to table: " + Name + E);
        }
    }

    private void InsertFromPartialSource(Connection conn, SAXReader reader) {
        try {
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
            PreparedStatement ps = conn.prepareStatement(CreatePreparedInsertStatement());
            String xPath = "//" + SrcNodeXML;
            int rowCount = 0;
            while (result.next()) {
                String linkField = result.getString(srcColName);
                String strRowLink = DataUrl + linkField;
                //Now be patient, the thing is that every link might not be reachable so check for that    
                //first and then proceed.
                HttpURLConnection connection = (HttpURLConnection) new URL(strRowLink).openConnection();
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
                Node rowNode = doc.selectSingleNode(xPath);
                for (int index = 0; index < Columns.size(); index++) {
                    TableCol col = Columns.get(index);
                    String colVal = "";
                    if (index != Columns.size() - 1) {
                        colVal = rowNode.selectSingleNode(col.GetColName()).getText();
                    } else {
                        colVal = linkField;
                    }
                    ps.setObject(index + 1, colVal);
                }
                rowCount++;
                ps.addBatch();
                if (rowCount % 100 == 0) {
                    ps.executeBatch();
                }
            }
            if (rowCount > 0) {
                int RecordsUpdated[] = ps.executeBatch();
            }
            conn.commit();
            ps.close();
            st.close();
        } catch (SQLException E) {
            System.out.println("Error in insert in to table: " + Name + E);
        } catch (DocumentException E) {
            System.out.println("Error in opening the url in InsertFromPartialSource Fun: " + E);
        } catch (MalformedURLException e) {
            System.out.println("URL field is not proper " + e);

        } catch (IOException e) {
            System.out.println("Link is not responding" + e);
        }

    }

    private void InsertFromFullSource(Connection conn, SAXReader reader) {
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
        } catch (SQLException E) {
            System.out.println("Error in insert in to table: " + Name + E);
        } catch (DocumentException E) {
            System.out.println("Error in opening the url in insertFromFullSrc Fun: " + E);
        }
    }

    private String CreatePreparedInsertStatement() {
        String insertStmt = "insert into " + Name + "(";
        String val = " values (";
        for (int index = 0; index < Columns.size(); index++) {
            TableCol col = Columns.get(index);
            if (index != Columns.size() - 1) {
                insertStmt = insertStmt + col.GetColName() + ", ";
                val += "?, ";
            } else {
                insertStmt = insertStmt + col.GetColName() + ")";
                val += "?)";
            }
        }
        insertStmt = insertStmt + val;
        return insertStmt;
    }
}
