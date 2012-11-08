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
       enum ColType{NOTYPET,INTEGERT,STRINGT,REALT};   
       private  int ColID;
       private ColType DataType;
       private String ColName;
       public static String GetTypeString(ColType T){
            switch(T){
                case INTEGERT:{
                    return "integer";
                }
                case STRINGT:
                    return "text";
                case REALT:
                    return "real";
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
            default:
                return java.sql.Types.NULL;
        }

    }
       
       public TableCol(int id,ColType type, String Name){
           ColID = id;
           DataType = type;
           ColName = Name;
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
