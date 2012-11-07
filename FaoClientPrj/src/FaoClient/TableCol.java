/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package FaoClient;

/**
 *
 * @author dell
 */
public class TableCol {
       enum ColType{INTEGERT,STRINGT};   
       private  int ColID;
       private ColType DataType;
       private String ColName;
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
