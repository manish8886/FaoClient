package FaoClient;

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

    void InsertFromFullSource(Connection conn, SAXReader reader) {
        try {
            Document doc = reader.read(DataUrl);
            String xPath = SrcNodeXML;
            String curPath = doc.getUniquePath();
            String text = doc.getText();
            String name = doc.getStringValue();
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
            int RecordsUpdated[] = ps.executeBatch();
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
