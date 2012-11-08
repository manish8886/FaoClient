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
       enum ColType{INTEGERT,STRINGT,REALT};   
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
