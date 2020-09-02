/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.mapping;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;
import us.monoid.web.XMLResource;
import static us.monoid.web.Resty.content;

/**
 *
 * @author uceeftu
 */
public class DOServiceMapping {
    private final String orchestratorAddress;
    private final int orchestratorPort;
    private final String slicePrefix;
    private final String mappingURI;
    
    JSONObject MdOMappings;
    JSONObject DOMappings;
    
    XMLResource mappingInfo;
    
    Resty rest;
    
    
    public DOServiceMapping(String orchestratorAddress, int orchestratorPort, String slicePrefix, JSONObject inMappings) {
        this.orchestratorAddress = orchestratorAddress;
        this.orchestratorPort = orchestratorPort;
        this.slicePrefix = slicePrefix;
        this.MdOMappings = inMappings;
        
        //this.mappingURI = "http://" + this.orchestratorAddress + ":" + this.orchestratorPort + "/ro/" + this.slicePrefix + "/mappings";
        this.mappingURI = "http://" + this.orchestratorAddress + ":" + this.orchestratorPort + "/" + this.slicePrefix + "/mappings";
        rest = new Resty();
        DOMappings = new JSONObject();
    }
    
    
    private Document createRequestBody() throws IOException {
        DocumentBuilderFactory dbf;
        DocumentBuilder builder;
        Document doc=null;
        
        try {
            dbf = DocumentBuilderFactory.newInstance();
            builder = dbf.newDocumentBuilder();
            doc = builder.newDocument();
            
            Element mappings = doc.createElement("mappings");
            doc.appendChild(mappings);
            
            Iterator bisbisKeys = MdOMappings.keys();
            while (bisbisKeys.hasNext()) {
                String bisbis = (String)bisbisKeys.next();
                JSONArray nfIDs = MdOMappings.getJSONArray(bisbis);
                for (int i=0; i<nfIDs.length(); i++) {
                    Element mapping = doc.createElement("mapping");
                    mappings.appendChild(mapping);

                    Element obj = doc.createElement("object");
                    mapping.appendChild(obj);
            
                    obj.insertBefore(doc.createTextNode("/virtualizer/nodes/node[id=" + bisbis + 
                                                        "]/NF_instances/node[id=" + nfIDs.getString(i) + 
                                                        "]"), obj.getLastChild());
                }
            }
            
            /*Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
            DOMSource source = new DOMSource(doc);
            StreamResult console = new StreamResult(System.out);
            transformer.transform(source, console);*/
            
        } catch (ParserConfigurationException | DOMException | JSONException | IllegalArgumentException /*| TransformerException*/ e) {
                throw new IOException("Error while generating XML request body" + e.getMessage());
          }
        return doc; 
    }
    
    
    private byte[] getRequestBodyAsBytes(Document doc) throws IOException {
        byte [] array;
        
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            ByteArrayOutputStream bos=new ByteArrayOutputStream();
            StreamResult result=new StreamResult(bos);
            transformer.transform(source, result);
            array = bos.toByteArray();
        } catch (TransformerException e) {
            throw new IOException("Error while converting XML document to byte array" + e.getMessage());
        }
        return array;
    }        

    

public JSONObject getServiceMapping() throws IOException {
        Document doc;
        
        String bisbisID=null;
        String nfID=null;
        
        String objectID;
        String domainType;
        
        try {
            mappingInfo = rest.xml(mappingURI, content(getRequestBodyAsBytes(createRequestBody())));
            doc = mappingInfo.doc();
            doc.getDocumentElement().normalize();
            
            JSONObject bisbisMappings = new JSONObject();
            NodeList mappings = doc.getElementsByTagName("mapping");
            for (int i = 0; i < mappings.getLength(); i++) {
                
                Node mapping = mappings.item(i);
                
                if (mapping.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) mapping; 
                    String rawObject = eElement.getElementsByTagName("object").item(0).getTextContent();
                    
                    Pattern p = Pattern.compile("id=([^\\]]*)");
                    Matcher m = p.matcher(rawObject);
                    
                    if(m.find()) {
                        bisbisID = m.group(1);
                    }
                    
                    if (m.find()) {
                        nfID = m.group(1);
                    }
                    
                    if (bisbisID != null && nfID != null) {
                       bisbisMappings.put("bisbis", bisbisID);
                    }
                    else
                       throw new IOException("Cannot find bisbis ID or NFid in the DO mapping info");
                    
                    
                    NodeList target = eElement.getElementsByTagName("target");
                    for (int j = 0; j < target.getLength(); j++) {
                        JSONObject NFMappings = new JSONObject();
                        NFMappings.put("id", nfID);
                        Node targetElement = target.item(j);

                        if (targetElement.getNodeType() == Node.ELEMENT_NODE) {
                           Element tElement = (Element) targetElement;
                           objectID = tElement.getElementsByTagName("object").item(0).getTextContent();
                           
                           if (!objectID.contains("ERROR")) {
                               NFMappings.put("internalid", objectID);
                               
                               domainType = tElement.getElementsByTagName("domain").item(0).getTextContent();
                               NFMappings.put("type", domainType);
                           }
                           else
                               NFMappings.put("id", "not found");
                        }
                        bisbisMappings.append("instances", NFMappings);
                    }
                }
                // not sure if we can have different bisbis in a domain, 
                // if this is the case the following put should be an accumulate/append
                DOMappings.put("mapping", bisbisMappings); 
                //System.out.println(DOMappings.toString(1));
            }
        } catch (IOException | DOMException | JSONException | NullPointerException e) {
            throw new IOException("Error while gathering mapping info from the DO: " + e.getMessage());
        }
        return DOMappings;
    }

    
}