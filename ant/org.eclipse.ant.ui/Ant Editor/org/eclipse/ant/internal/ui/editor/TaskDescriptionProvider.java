/*******************************************************************************
 * Copyright (c) 2002, 2004 GEBIT Gesellschaft fuer EDV-Beratung
 * und Informatik-Technologien mbH, 
 * Berlin, Duesseldorf, Frankfurt (Germany) and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     GEBIT Gesellschaft fuer EDV-Beratung und Informatik-Technologien mbH - initial API and implementation
 * 	   IBM Corporation - bug fixes
 *******************************************************************************/

package org.eclipse.ant.internal.ui.editor;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The <code>TaskDescriptionProvider</code> provides the additional descriptions
 * for tasks and attributes for the code assist.
 * <P>
 * Descriptions for task are originally provided with the XML file
 * <code>TASKS_DESCRIPTION_XML_FILE_NAME</code>. This file is parsed by the
 * provider and requested descriptions are returned.
 * <P>
 * Check out the documentation for the public methods of this class. 
 * 
 */
public class TaskDescriptionProvider {

    /**
     * The file that contains all task descriptions.
     */
    public static final String TASKS_DESCRIPTION_XML_FILE_NAME = "/anttasks_1.6.0.xml"; //$NON-NLS-1$

    public static final String XML_TAG_TASKS = "tasks"; //$NON-NLS-1$
    public static final String XML_TAG_TASK = "task"; //$NON-NLS-1$
    public static final String XML_TAG_ELEMENTS = "elements"; //$NON-NLS-1$
    public static final String XML_TAG_ATTRIBUTE = "attribute"; //$NON-NLS-1$
    public static final String XML_TAG_ATTRIBUTES = "attributes"; //$NON-NLS-1$
    public static final String XML_TAG_ELEMENT = "element"; //$NON-NLS-1$
    public static final String XML_TAG_STRUCTURE = "structure"; //$NON-NLS-1$
    public static final String XML_TAG_DESCRIPTION = "description"; //$NON-NLS-1$
    public static final String XML_ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
    public static final String XML_ATTRIBUTE_REQUIRED = "required"; //$NON-NLS-1$
    
    private static TaskDescriptionProvider fgDefault;

    protected Map taskNodes = new HashMap();
    
    /**
     * Meant to be a singleton
     */
    private TaskDescriptionProvider() {
        initialize();
    }
    
    public static TaskDescriptionProvider getDefault() {
    	if (fgDefault == null) {
    		fgDefault= new TaskDescriptionProvider();
    	}
    	return fgDefault;
    }

    /**
     * Parses the task description xml file and stores the information.
     */
    protected void initialize() {
        Document tempDocument = parseFile(TASKS_DESCRIPTION_XML_FILE_NAME);
        Node tempRootNode = tempDocument.getDocumentElement();
        NodeList tempChildNodes = tempRootNode.getChildNodes();
        for(int i=0; i<tempChildNodes.getLength(); i++) {
            Node tempNode = tempChildNodes.item(i);
            if(tempNode.getNodeType() == Node.ELEMENT_NODE) {
                String tempTagName = tempNode.getNodeName();
                if(tempTagName.equals(XML_TAG_TASK)) {
                    NamedNodeMap tempAttributes = tempNode.getAttributes();
                    Node tempAttributeNode = tempAttributes.getNamedItem(XML_ATTRIBUTE_NAME);
                    if(tempAttributeNode != null) {
                        String tempTaskName = tempAttributeNode.getNodeValue();
                        if(tempTaskName != null) {
                            taskNodes.put(tempTaskName, tempNode);
                        }
                    }
                }
            }
        }
    }
    

    /**
     * Returns the (DOM) document as a result of parsing the file with the 
     * specified file name.
     * <P>
     * The file will be loaded as resource, thus must begin with '/' and must
     * be relative to the classpath.
     */
    protected Document parseFile(String aFileName) {
        Document tempDocument = null;

        DocumentBuilderFactory tempFactory = DocumentBuilderFactory.newInstance();
        tempFactory.setIgnoringComments(true);
        tempFactory.setIgnoringElementContentWhitespace(true);
        tempFactory.setCoalescing(true);

        try {
            DocumentBuilder tempDocBuilder = tempFactory.newDocumentBuilder();
            URL tempURL = getClass().getResource(aFileName);
            InputSource tempInputSource = new InputSource(tempURL.toExternalForm());
            tempDocument = tempDocBuilder.parse(tempInputSource);
        } catch (ParserConfigurationException e) {
			AntUIPlugin.log(e);
        }
        catch (IOException ioException) {
			AntUIPlugin.log(ioException);
        }
        catch (SAXException saxException) {
			AntUIPlugin.log(saxException);
        }

        return tempDocument;
    }

    /**
     * Returns the description string for the specified task.
     * 
     * @return description string or <code>null</code> if task not known or
     * no description available.
     */
    public String getDescriptionForTask(String aTaskName) {
        Element taskElement = (Element)taskNodes.get(aTaskName);
        if(taskElement != null) {
            return getDescriptionOfNode(taskElement);
        }
        return null;
    }


    /**
     * Returns the description of the specified node.
     * <P>
     * The node must be either one of task node or attribute node.
     */
    protected String getDescriptionOfNode(Node aNode) {
        NodeList tempChildNodes = aNode.getChildNodes();
        for (int i=0; i<tempChildNodes.getLength(); i++) {
            Node tempNode = tempChildNodes.item(i);
            if(tempNode instanceof Element && XML_TAG_DESCRIPTION.equals(tempNode.getNodeName())) {
                Element tempDescriptionElement = (Element)tempNode;
                Node tempChildNode = tempDescriptionElement.getFirstChild();
                if(tempChildNode instanceof Text) {
                    return ((Text)tempChildNode).getData();
                }
                break; 
            }
        }
        return null;
    }
    
    /**
     * Returns the Required value of the specified node.
     * <P>
     * Currently the XML file has Required defined as NOTDEFINED in
     * some cases. If so the value returned is an empty string
     */
    protected String getRequiredOfNode(Node aNode) {
    	
    	String tmpNodeName = aNode.getNodeName();
    	String tmpRequiredValue = null;
    	
   		if(aNode.getNodeType() == Node.ELEMENT_NODE && 
   			(XML_TAG_ATTRIBUTE.equals(tmpNodeName) || XML_TAG_ELEMENT.equals(tmpNodeName)) ) {
        	  
        	  tmpRequiredValue = aNode.getAttributes().getNamedItem(XML_ATTRIBUTE_REQUIRED).getNodeValue();
   		}
   		
   		if(tmpRequiredValue == null || tmpRequiredValue.equals("NOTDEFINED")) { //$NON-NLS-1$
   			return ""; //$NON-NLS-1$
   		}
   		
   		return tmpRequiredValue;
                   
    }

    
    /**
     * Returns the description string for the specified attribute of the 
     * specified task.
     * 
     * @return description string or <code>null</code> if task or attribute 
     * not known or no description available.
     */
    public String getDescriptionForTaskAttribute(String aTaskName, String anAttributeName) {
        
        String tmpDescription = null;	
        	
        Node tmpAttributesNode = getAttributesNode(aTaskName);
        	
        if(tmpAttributesNode != null) {
        	
        	tmpDescription = getDescriptionForNodeNamedWithNameInNodeList( XML_TAG_ATTRIBUTE, anAttributeName,
        																tmpAttributesNode.getChildNodes());															
    		//If Description is null we try the elements section else we're satisfied.
    		if( tmpDescription != null ) {
    			return tmpDescription;
    		}
        }
        //Not yet found. Try the elements Node
    	tmpAttributesNode = getElementsNode(aTaskName);
    	if(tmpAttributesNode != null) {
    		tmpDescription = getDescriptionForNodeNamedWithNameInNodeList( XML_TAG_ELEMENT, anAttributeName,
            														   tmpAttributesNode.getChildNodes());
            
            return tmpDescription;  
            
        }
        return null;
    }
    
	/**
     * Returns the required string value for the specified attribute of the
     * specified task.
     * 
     * @return required string or <code>null</code> if task or attribute not 
     * known or no description available.
     */
    public String getRequiredAttributeForTaskAttribute(String aTaskName, String anAttributeName) {
 
        String tmpRequired = null;	
        	
        Node tmpAttributesNode = getAttributesNode(aTaskName);
        	
        if(tmpAttributesNode != null) {
        	
        	tmpRequired = getRequiredForNodeNamedWithNameInNodeList( XML_TAG_ATTRIBUTE, anAttributeName,
        																tmpAttributesNode.getChildNodes());															
    		
    		//If Required is null we try the elements section else we're satisfied.
    		if( tmpRequired != null ) {
    			return tmpRequired;
    		}
        }
        
        //Not yet found. Try the elements Node
    	tmpAttributesNode = getElementsNode(aTaskName);
    	if(tmpAttributesNode != null) {
    		tmpRequired = getDescriptionForNodeNamedWithNameInNodeList( XML_TAG_ELEMENT, anAttributeName,
            														   tmpAttributesNode.getChildNodes());
            //Return it even if its null
            return tmpRequired;  
            
        }
        
        //Not found return null
        return null;
    }
    
    /**
     * Returns the Elements Node of the specified TaskName
     * 
     * @param aTaskName The name of the task
     * @return The Elements Node of the Task.
     */
    protected Node getElementsNode(String aTaskName) {
    	
    	Node tmpStructureNode = getStructureNode(aTaskName);
    	if(tmpStructureNode != null) {
    		return getChildNodeNamedOfTypeFromNode(XML_TAG_ELEMENTS, Node.ELEMENT_NODE,
    												tmpStructureNode);
    	}
    	return null;
    }
    
    /**
     * Returns the Attributes Node of the specified TaskName
     * 
     * @param aTaskName The name of the task
     * @return The Attributes Node of the Task or <code>null</code> if one
     * does not exist.
     */    
    protected Node getAttributesNode(String aTaskName) {
    	
        Node tmpStructureNode = getStructureNode(aTaskName);
        if(tmpStructureNode != null){
        	return getChildNodeNamedOfTypeFromNode(XML_TAG_ATTRIBUTES, Node.ELEMENT_NODE,
                                                             tmpStructureNode);
    	} 
        return null;
    }

    /**
     * Returns the Structure Node of the specified TaskName
     * 
     * @param aTaskName The name of the task
     * @return The Structure Node of the Task.
     */        
    protected Node getStructureNode(String aTaskName) {	
    	Element taskElement = (Element)taskNodes.get(aTaskName);
        if(taskElement != null) {
        	//Dig us down to the Structure node
        	Node structureNode = getChildNodeNamedOfTypeFromNode(XML_TAG_STRUCTURE, Node.ELEMENT_NODE,
        	                                                     taskElement);
        	return structureNode;
        }
        return null;
    }
    
    /**
     * Returns the Description for a Node satisfying the criterias in the
     * NodeList given as Argument.
     * 
     * @param aNodeName The Name of the Node
     * @param anAttributeName The string of the Name value
     * @param anAttributesNodeList The NodeList to search in.
     * @return The Description found or null if none is found
     */
    protected String getDescriptionForNodeNamedWithNameInNodeList( String aNodeName, String anAttributeName,
    																 NodeList anAttributesNodeList) {
    	for (int i=0; i<anAttributesNodeList.getLength(); i++) {
                Node tempNode = anAttributesNodeList.item(i);
                if(tempNode.getNodeType() == Node.ELEMENT_NODE && aNodeName.equals(tempNode.getNodeName())) {
                	if( anAttributeName.equals(getTaskAttributeName(tempNode)) ) {
                    	return getDescriptionOfNode(tempNode);
                	}
                }
        }
        
        //Not found
        return null;																 	
	}
	
	
    /**
     * Returns the Name of Task Attribute.
     * 
     * @return The Name of the Attribute.
     */
    public String getTaskAttributeName(Node aTaskAttributeNode) {
    	NamedNodeMap tmpNamedNodeMap = aTaskAttributeNode.getAttributes();	
    	return tmpNamedNodeMap.getNamedItem(XML_ATTRIBUTE_NAME).getNodeValue();
    }
    
    /**
     * Returns the ChildNode of the node defined by the Arguments. The
     * first child found matching the criterias is returned.
     * 
     * @param aNodeName The Name of the Node to return.
     * @param aType The Type of the node @see Node
     * @param aParentNode The Node to get the child from
     * 
     * @return The First Child Node found matching the criterias,
     * or null if none is found.
     */
    protected Node getChildNodeNamedOfTypeFromNode(String aNodeName, short aNodeType, Node aParentNode) {
    
    	NodeList tmpNodeList = aParentNode.getChildNodes();
		for(int i=0; i<tmpNodeList.getLength(); ++i ) {
			Node tmpNode = tmpNodeList.item(i);
			if( (tmpNode.getNodeType() == aNodeType) && aNodeName.equals(tmpNode.getNodeName()) ) {
				return tmpNode;
			}
		}
		//Not found
		return null;    	
    }
    
     /**
     * Returns the Required Field for a Node satisfying the criterias in the
     * NodeList given as Argument.
     * 
     * @param aNodeName The Name of the Node
     * @param anAttributeName The string of the Name value
     * @param anAttributesNodeList The NodeList to search in.
     * @return The Description found or null if none is found
     */
    protected String getRequiredForNodeNamedWithNameInNodeList( String aNodeName, String anAttributeName,
    																 NodeList anAttributesNodeList) {
    	for (int i=0; i<anAttributesNodeList.getLength(); i++) {
                Node tempNode = anAttributesNodeList.item(i);
                if(tempNode.getNodeType() == Node.ELEMENT_NODE && aNodeName.equals(tempNode.getNodeName())) {
                	if( anAttributeName.equals(getTaskAttributeName(tempNode)) ) {
                    	return getRequiredOfNode(tempNode);
                	}
                }
        }
        
        //Not found
        return null;																 	
    }
}