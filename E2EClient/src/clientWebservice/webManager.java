package clientWebservice;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public class webManager {
	
	private String XML_FILE_REQUEST_PATH= null;
	private String XML_FILE_RESPONSE_PATH= null;
	
	public void callWebservice(String url, String  requestFile, String  responseFile) throws Exception, SOAPException{
		try {
	        SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
	        SOAPConnection soapConnection = soapConnectionFactory.createConnection();
	        
	        XML_FILE_REQUEST_PATH = requestFile;
	        XML_FILE_RESPONSE_PATH= responseFile;
	        
	        SOAPMessage soapResponse = soapConnection.call(createSOAPRequestFromFile(), url);
	        
	        // Process the SOAP Response
	        createSOAPResponseFile(soapResponse);
	        
	        // Process the SOAP Response
	        //printSOAPResponse(soapResponse);
	
	        soapConnection.close();
		}
		catch (Exception e) {
            System.err.println("Error occurred while sending SOAP Request to Server");
            e.printStackTrace();
        }
	}
	
    private SOAPMessage createSOAPRequestFromFile() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();
        
        DOMSource domSource = new DOMSource(parse(XML_FILE_REQUEST_PATH));
        soapPart.setContent(domSource);
        
        /* Print the request message */
        //System.out.print("Request SOAP Message = ");
        //soapMessage.writeTo(System.out);
        //System.out.println("");
        
        return soapMessage;
    }
      
    /**
      * This method is used to parse the xml file to document
      * 
       * @param xmlFilePath
      * @return 
       * @throws Exception 
       */
      public Document parse(String xmlFilePath) throws Exception {
        Document doc =  null;
        DocumentBuilder parser = null;
        DocumentBuilderFactory docBuilderFactory = null;
        try {
            
            docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setValidating(false);
            docBuilderFactory.setNamespaceAware(true);
            
                  parser = docBuilderFactory.newDocumentBuilder();
            
            doc =  parser.parse(new File(xmlFilePath));
            
        } catch(Exception e) {
             e.printStackTrace();
          } finally {
        //    System.out.println("");
        }
        return doc;
    }
      
      public void createSOAPResponseFile(SOAPMessage soapResponse) throws IOException, TransformerException, SOAPException{
            
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            
            Source source = soapResponse.getSOAPPart().getContent();
            StreamResult result = new StreamResult(new File(XML_FILE_RESPONSE_PATH));
            
            transformer.transform(source, result);
      
      }
      
      public void printSOAPResponse(SOAPMessage soapResponse) throws IOException, TransformerException, SOAPException{
            
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            
            Source source = soapResponse.getSOAPPart().getContent();

            System.out.print("\n\nResponse SOAP Message = ");
            StreamResult result = new StreamResult(System.out);
            
            transformer.transform(source, result);
      
      }	
}
