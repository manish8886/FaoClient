package FaoClient;
import java.util.List;
import java.util.Iterator;
import java.sql.*;
import java.net.URL;
import java.util.Iterator;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import java.util.Vector;
import org.dom4j.Node;

/**
 *
 * @author Manish Jain
 */
public class CEngine implements IFaoTableEngine {

    private SAXReader xmlReader;
    private Connection databaseConnector;
    private Vector<CFaoTable> tableCollection;
    private int iMainTblIndex;
    private String mainFileName;

    @Override
    public String GetSourceTableName() {
        return tableCollection.get(iMainTblIndex).GetTableName();
    }

    public CEngine() {
        xmlReader = null;
        databaseConnector = null;
        tableCollection = new Vector();
        iMainTblIndex = -1;
        mainFileName = "";
    }

    public boolean Initialise(String DataSrcName, String xmlFileName) {
        boolean bSucceess = true;
        String dbURL = "jdbc:odbc:" + DataSrcName;
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            databaseConnector = DriverManager.getConnection(dbURL);
            xmlReader = new SAXReader();
            mainFileName = xmlFileName;//Initialise here so that if connection was failed to database, it remain uninitialised.
            return bSucceess;
        } catch (Exception E) {
            System.out.println("Unable to connect to database" + E);
            return false;
        }
    }

    public boolean Start() {
        boolean bStart = true;
        //First check is everything is set up for successfully intialisation of the engine
        if (xmlReader == null || databaseConnector == null || mainFileName == "") {
            return false;//Engine unable to Start           
        }
        return bStart;
    }

    private boolean ParseMainFile() {
        boolean bSuccess = true;
        if (xmlReader == null || databaseConnector == null || mainFileName == "") {
            return false;//Engine unable to Start           
        }
        try {
            Document xmlDoc = xmlReader.read(mainFileName);
            String srcNodePath = "/schema/table";
            List listOfNodes = xmlDoc.selectNodes(srcNodePath);
            Iterator iter = listOfNodes.iterator();
            while (iter.hasNext()) {
                Node tblNode = (Node) iter.next();
                List listColNodes = tblNode.selectNodes("col");
                Iterator colNodeIter = listColNodes.iterator();
                Vector<TableCol> colVector = new Vector();
                while (colNodeIter.hasNext()) {
                    Node colNode = (Node) colNodeIter.next();
                    String strName = colNode.getText();
                    String strID = colNode.selectSingleNode("id").getText();
                    String strType = colNode.selectSingleNode("type").getText();
                    TableCol col = new TableCol(Integer.parseInt(strID), TableCol.GetColTypeFrmString(strType), strName);
                    colVector.add(col);
                }
                //Now get sourceNode 
                String tblName = tblNode.selectSingleNode("name").getText();
                String srcNodeVal = tblNode.selectSingleNode("sourcenode").getText();
                Node linkNode = tblNode.selectSingleNode("link");
                String strURL = linkNode.getText();
                int linkType = Integer.parseInt(linkNode.selectSingleNode("type").getText());
                
                CFaoTable tbl = new CFaoTable();
                tbl.SetTableName(tblName);
                tbl.SetDataSource(strURL);
                tbl.SetColumns(colVector);
                tbl.SetSourceNode(srcNodeVal);
                if (linkType == 0) {
                    tbl.SetSourceType(CFaoTable.SourceType.Full);
                    iMainTblIndex = tableCollection.size();
                } else {
                    tbl.SetSourceType(CFaoTable.SourceType.Partial);
                }
                tableCollection.add(tbl);
            }

        } catch (DocumentException E) {
            System.out.println("Error in opening the url in insertFromFullSrc Fun: " + E);
        }
        return bSuccess;
    }
}