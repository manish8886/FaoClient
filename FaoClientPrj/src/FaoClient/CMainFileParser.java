/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package FaoClient;

import java.net.URL;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import java.util.Vector;
/**
 *
 * @author Manish Jain
 */
public class CMainFileParser {
    String strFilePath;
    public CMainFileParser(String strMainFilePath) {
        strFilePath = strMainFilePath;
    }
    public void StartParser(Vector<CFaoTable>FaoTables){
       FaoTables.clear();
       SAXReader reader = new SAXReader();
       Document document;
      try{
            document= reader.read(strFilePath);
      }
    catch (DocumentException Exp){
        System.out.println("Main File is not present at the given path");
        }   
    }
}