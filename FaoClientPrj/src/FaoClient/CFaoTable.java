package FaoClient;
import java.sql.*;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.dom4j.Document;
import org.dom4j.Node;
/**
 *
 * @author Manish Jain
 */
public class CFaoTable {
    Vector<TableCol>Columns;
    String Name;
    String SrcNodeXML;
    String DataUrl;
    SourceType Type;
    enum SourceType{Full,Partial};
    public CFaoTable() {
        Name = "";
        SrcNodeXML = "";
        DataUrl = "";
        Columns.clear();
        Type = SourceType.Partial; //Means the URL of the Data Source is not Complete
    }
    void SetTableName(String str){
        Name = str;
    }
    void SetColumns(Vector<TableCol> colVec){
        Columns= colVec;
    }
    void SetDataSource(String Path){
        DataUrl = Path;
    }
    void SetSourceType(SourceType t){
        if(t!= Type){
            Type = t;
        }
    }
    void InsertFromFullSource(Connection conn,Document doc){
        List listOfNode= doc.selectNodes(SrcNodeXML);
        try {   
        conn.setAutoCommit(false);
        Statement  st = conn.createStatement();
         //Here the data stored in each node will consitute our one row in 
        // the table
        for(Iterator iter = listOfNode.iterator(); iter.hasNext();){
           Node rowElement = (Node) iter.next();
           String insertStmt = "insert into "+Name+" values (";
            for(int index=0;index<Columns.size();index++){
            TableCol col= Columns.get(index);
            String colVal = rowElement.selectSingleNode(col.GetColName()).getText();
            if(col.GetColType()==TableCol.ColType.STRINGT){
                    colVal= "'"+colVal+"'";
            }
            if(index != Columns.size()-1){
                    insertStmt =  insertStmt + colVal+", ";   
            }
             else{
                    insertStmt =  insertStmt + colVal+")";
                 }
            }
                   st.addBatch(insertStmt);
            }
            st.executeBatch();
            conn.commit();
        }
    catch(SQLException E){
             System.out.println("Error in insert in to table: "+ Name + E);
        }
    }
    
}
 
