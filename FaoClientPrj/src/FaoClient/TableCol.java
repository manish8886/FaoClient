/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package FaoClient;
/**
 *
 * @author Manish Jain
 */
public class TableCol {
       enum ColType{NOTYPET,INTEGERT,STRINGT,REALT,DOUBLET};   
       private  int ColID;
       private ColType DataType;
       private boolean bAutoIncr;
       private boolean bPrimarykey;
       private String ColName;
       public static String GetTypeString(ColType T){
            switch(T){
                case INTEGERT:{
                    return "integer";
                }
                case STRINGT:
                    return "varchar(512)";
                case REALT:
                    return "real";
                case DOUBLET:
                    return "double";
                default: 
                        return "";
                }

       }
    public static ColType GetColTypeFrmString(String T) {
        switch (T) {
            case "integer": {
                return ColType.INTEGERT;
            }
            case "string":
                return ColType.STRINGT;
            case "real":
                return ColType.REALT;
            case "double":
                return ColType.DOUBLET;
            default:
                return ColType.NOTYPET;
        }

    }
        public static int GetSqlColTypeFrmType(ColType T) {
        switch (T) {
            case INTEGERT: {
                return java.sql.Types.INTEGER;
            }
            case STRINGT:
                return java.sql.Types.VARCHAR;
            case REALT:
                return java.sql.Types.FLOAT;
            case DOUBLET:
                return java.sql.Types.DOUBLE;
            default:
                return java.sql.Types.NULL;
        }

    }
       
       public TableCol(int id,ColType type, String Name,boolean b,boolean bKey){
           ColID = id;
           DataType = type;
           ColName = Name;
           bAutoIncr = b;
           bPrimarykey = bKey;
       }
       public boolean IsAutoIncr(){
           return bAutoIncr;
       }
       public boolean IsPrimaryKey(){
           return bPrimarykey;
       }
       public int GetColID(){
            return ColID;
    }
       public ColType GetColType(){
            return DataType;
    }
       public String GetColName(){
           return ColName;
       }
 }
