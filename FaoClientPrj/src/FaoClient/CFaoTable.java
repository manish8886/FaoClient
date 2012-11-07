/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package FaoClient;
import java.util.Vector;
/**
 *
 * @author Manish Jain
 */
public class CFaoTable {
    String Name;
    String SrcTableName;
    int iBoundColId;
    Vector<String>ColNames;
    String Link;

    public CFaoTable() {
        Name = "";
        SrcTableName = "";
        iBoundColId = -1;
        Link = "";
    }
    enum SourceType{Full,Partial};
    void SetTableName(String str){
        Name = str;
    }
    void SetColNames(Vector<String> colString){
        ColNames= colString;
    }
    void SetDataSourcePath(String Path){
        Link = Path;
    }
 };
