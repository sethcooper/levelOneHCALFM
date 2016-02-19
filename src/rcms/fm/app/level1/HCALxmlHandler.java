package rcms.fm.app.level1;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Scanner;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.DOMException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import rcms.fm.fw.user.UserActionException;

import rcms.resourceservice.db.resource.fm.FunctionManagerResource;
import rcms.util.logger.RCMSLogger;
import rcms.util.logsession.LogSessionException;

/**
 *  @author John Hakala
 *
 */

public class HCALxmlHandler {

  protected HCALFunctionManager functionManager = null;
  static RCMSLogger logger = null;
  public DocumentBuilder docBuilder;

  public HCALxmlHandler(HCALFunctionManager parentFunctionManager) {
    this.logger = new RCMSLogger(HCALFunctionManager.class);
    logger.warn("Constructing xmlHandler.");
    this.functionManager = parentFunctionManager;
    logger.warn("Done constructing xmlHandler.");
  }


  public Element getHCALuserXML() throws UserActionException {
    try {
      // return the userXML
      String userXmlString = "<userXML>" + ((FunctionManagerResource)functionManager.getQualifiedGroup().getGroup().getThisResource()).getUserXml() + "</userXML>";

      logger.debug("[HCAL " + functionManager.FMname + "]: got the userXML.");
      docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      InputSource inputSource = new InputSource();
      inputSource.setCharacterStream(new StringReader(userXmlString));
      Document hcalUserXML = docBuilder.parse(inputSource);
      hcalUserXML.getDocumentElement().normalize();
      logger.debug("[HCAL " + functionManager.FMname + "]: formatted the userXML.");
      return hcalUserXML.getDocumentElement();
    }
    catch (DOMException | ParserConfigurationException | SAXException | IOException e) {
      String errMessage = "[HCAL " + functionManager.FMname + "]: Got an error when trying to retrieve the userXML: " + e.getMessage();
      logger.error(errMessage);
      throw new UserActionException(errMessage);
    }
  }
  public String getHCALuserXMLelementContent(String tagName) throws UserActionException {
    try {
      Element hcalUserXML = getHCALuserXML();
      if (!hcalUserXML.equals(null) && !hcalUserXML.getElementsByTagName(tagName).equals(null)) {
        if (hcalUserXML.getElementsByTagName(tagName).getLength()==1) {
          return hcalUserXML.getElementsByTagName(tagName).item(0).getTextContent();
        }
        else {
          String errMessage = (hcalUserXML.getElementsByTagName(tagName).getLength()==0) ? " was not found in the userXML. Will use value supplied by level1 or default value." : " was found with more than one occurrance in the userXML.";
          throw new UserActionException("[HCAL " + functionManager.FMname + "]: The userXML element with tag name '" + tagName + "'" + errMessage);
        }
      }
      else return null;
    }     
    catch (UserActionException e) {throw e;}
  }
}
