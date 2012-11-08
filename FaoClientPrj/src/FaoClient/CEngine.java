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

    private CEngine() {
        xmlReader = null;
        databaseConnector = null;
        tableCollection = new Vector();
        iMainTblIndex = -1;
        mainFileName = "";
    }
    public static CEngine CreateEngineInstance(){
         CEngine engineInstance = new CEngine();
         return engineInstance;
    }
    public boolean Initialise(String DataSrcName, String xmlFileName) {
        boolean bSucceess = true;
        String dbURL = "jdbc:odbc:" + DataSrcName;
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            databaseConnector = DriverManager.getConnection(dbURL);
            xmlReader = new SAXReader();
            mainFileName = xmlFileName;//Initialise here so that if connection was failed to database, it remain uninitialised.
            if (!ParseMainFile()) {
                return false;
            }

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
        //If we are starting the engine drop all the tables previouly created
        DropAllTableInDataBase(databaseConnector);
        //Now Start Creating
        //First we need to create the main table and fill it up so other table can query the data.
        CFaoTable mainTable = tableCollection.get(iMainTblIndex);
        mainTable.SetEngineInstance(this);
        if(!mainTable.CreateTable(databaseConnector)){
            return false;//Can't proceed;
        }
        if(!mainTable.StartLogging(databaseConnector, xmlReader)){
            return false;
        }
        for(int i = 0;i<tableCollection.size();i++){
            if(i==iMainTblIndex){
                continue;
            }
            CFaoTable tableOb = tableCollection.get(i);
            tableOb.SetEngineInstance(this);
            if(!tableOb.CreateTable(databaseConnector))
                continue;//Skip populating this table
            tableOb.StartLogging(databaseConnector, xmlReader);
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
                    String strID = colNode.selectSingleNode("@id").getText();
                    String strType = colNode.selectSingleNode("@type").getText();
                    TableCol col = new TableCol(Integer.parseInt(strID), TableCol.GetColTypeFrmString(strType), strName);
                    colVector.add(col);
                }
                //Now get sourceNode 
                String tblName = tblNode.selectSingleNode("@name").getText();
                String srcNodeVal = tblNode.selectSingleNode("sourcenode").getText();
                Node linkNode = tblNode.selectSingleNode("link");
                String strURL = linkNode.getText();
                int linkType = Integer.parseInt(linkNode.selectSingleNode("@type").getText());

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
            return bSuccess;

        } catch (DocumentException E) {
            System.out.println("Error in opening the Main file ParseMainFile Fun: " + E);
            return false;
        }
    }
    private void DropAllTableInDataBase(Connection conn) {
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
}