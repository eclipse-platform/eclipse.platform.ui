//
// OutlinePreparingHandler.java
//
// Copyright:
// GEBIT Gesellschaft fuer EDV-Beratung
// und Informatik-Technologien mbH, 
// Berlin, Duesseldorf, Frankfurt (Germany) 2002
// All rights reserved.
//
package org.eclipse.ui.externaltools.internal.ant.editor.outline;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.FileUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.externaltools.internal.ant.editor.PlantyException;
import org.eclipse.ui.externaltools.internal.ant.editor.xml.XmlAttribute;
import org.eclipse.ui.externaltools.internal.ant.editor.xml.XmlElement;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Used when parsing the content of the editor to create a tree model that is
 * used as model for the outline view.
 * <P>
 * The handler makes use of the xml parser's locator to pre-determine  
 * positions of tags. The locations as they can be determined at this point
 * are set for the <code>XmlElement</code> instances.  The locations that are
 * set are not the correct ones yet, after parsing. They have to be corrected
 * afterwards. This actually happens in the OutlineView. The locations that are
 * set at this point are only the ones that the locator gives us in 
 * <code>startElement()</code> and <code>endElement()</code>.
 * 
 * @version 25.11.2002
 * @author Alf Schiefelbein
 */
public class OutlinePreparingHandler extends DefaultHandler {


    /**
     * The locator that tells us the location of the currently parsed element 
     * in the parsed document.
     */
    protected Locator locator;


    /**
     * Stack of still open elements.
     * <P>
     * On top of the stack is the innermost element.
     */
    protected Stack stillOpenElements = new Stack();


    /**
     * The root element of the DOM Tree that we create while parsing.
     */
    protected XmlElement rootElement;
    
    protected File mainFile;


    /**
     * Creates an instance.
     */
    public OutlinePreparingHandler(File mainFile) throws ParserConfigurationException {
        super();
        this.mainFile= mainFile;
    }


    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
     */
    public void startElement(
        String aUri,
        String aLocalName,
        String aQualifiedName,
        Attributes anAttributes)
        throws SAXException {
        /*
         * While the crimson parser passes the tag name as local name, apache's
         * xerces parser, passes the tag name as qualilfied name and an empty 
         * string as local name.
         */

        
         
        // Create a Dom Element
        XmlElement tempElement = createXmlElement(aLocalName, aQualifiedName, anAttributes);

        // set starting the location
        tempElement.setStartingRow(locator.getLineNumber());
        tempElement.setStartingColumn(locator.getColumnNumber());
        
        // Is it our root
        if(rootElement == null) {
            rootElement = tempElement;
        } 
        
        // Add it as child to parent
        else {
            XmlElement tempLastOpenedElement = (XmlElement)stillOpenElements.peek();
            tempLastOpenedElement.addChildNode(tempElement);
        }
    
        stillOpenElements.push(tempElement);
        
        super.startElement(aUri, aLocalName, aQualifiedName, anAttributes);
    }
    
    
    /**
     * Creates an <code>XmlElement</code> instance from the specified parameters.
     */
    protected XmlElement createXmlElement(
    		String aLocalName, 
    		String aQualifiedName, 
    		Attributes anAttributes) {
		String tempElementName = null;

        XmlElement tempElement = null;
        if(aLocalName.length() > 0) {
            tempElementName = aLocalName;
        }
        else if(aQualifiedName.length() > 0) {
            tempElementName = aQualifiedName;
        }   
        else {
            throw new PlantyException("Error when parsing document: Neither a local name nor qualified of an element specified");
        }
        if(tempElementName.equalsIgnoreCase("target") || tempElementName.equalsIgnoreCase("project")) {
        	tempElement = new XmlElement(tempElementName) {
                public String getDisplayName() {
                    XmlAttribute tempXmlAttr = getAttributeNamed("name");
                    if(tempXmlAttr != null) {
                    	return tempXmlAttr.getValue();
                    }
                    return super.getDisplayName();
                }
        	};
        }
        else if(tempElementName.equalsIgnoreCase("property")) {
        	tempElement = new XmlElement(tempElementName) {
                public String getDisplayName() {
                    XmlAttribute tempXmlAttr = getAttributeNamed("name");
                    if(tempXmlAttr != null) {
                    	return tempXmlAttr.getValue();
                    }
                    tempXmlAttr = getAttributeNamed("file");
                    if(tempXmlAttr != null) {
                    	return "file="+tempXmlAttr.getValue();
                    }	
                    tempXmlAttr = getAttributeNamed("resource");
                    if(tempXmlAttr != null) {
                    	return "resource="+tempXmlAttr.getValue();
                    }	
                    tempXmlAttr = getAttributeNamed("environment");
                    if(tempXmlAttr != null) {
                    	return "environment="+tempXmlAttr.getValue();
                    }	
                    return super.getDisplayName();
                }
        	};
        }	
        else if(tempElementName.equalsIgnoreCase("antcall")) {
        	tempElement = new XmlElement(tempElementName) {
                public String getDisplayName() {
		        	String tempDisplayName = "antcall ";
                    XmlAttribute tempXmlAttr = getAttributeNamed("target");
                    if(tempXmlAttr != null) {
                    	tempDisplayName += tempXmlAttr.getValue();
                    }
                    return tempDisplayName;
                }
        	};
        }	
        else if(tempElementName.equalsIgnoreCase("mkdir")) {
        	tempElement = new XmlElement(tempElementName) {
                public String getDisplayName() {
		        	String tempDisplayName = "mkdir ";
                    XmlAttribute tempXmlAttr = getAttributeNamed("dir");
                    if(tempXmlAttr != null) {
                    	tempDisplayName += tempXmlAttr.getValue();
                    }
                    return tempDisplayName;
                }
        	};
        }	
        else if(tempElementName.equalsIgnoreCase("copy")) {
        	tempElement = new XmlElement(tempElementName) {
                public String getDisplayName() {
		        	String tempDisplayName = "copy ";
                    XmlAttribute tempXmlAttr = getAttributeNamed("file");
                    if(tempXmlAttr != null) {
                    	tempDisplayName += tempXmlAttr.getValue();
                    }
                    return tempDisplayName;
                }
        	};
        }	
        else if(tempElementName.equalsIgnoreCase("tar") 
        	|| tempElementName.equalsIgnoreCase("jar")
        	|| tempElementName.equalsIgnoreCase("war")
        	|| tempElementName.equalsIgnoreCase("zip")) {
        	tempElement = new XmlElement(tempElementName) {
                public String getDisplayName() {
		        	String tempDisplayName = super.getDisplayName();
                    XmlAttribute tempXmlAttr = getAttributeNamed("destfile");
                    if(tempXmlAttr != null) {
                    	tempDisplayName += " " + tempXmlAttr.getValue();
                    }
                    return tempDisplayName;
                }
        	};
        }	
        else if(tempElementName.equalsIgnoreCase("untar") 
        	|| tempElementName.equalsIgnoreCase("unjar")
        	|| tempElementName.equalsIgnoreCase("unwar")
        	|| tempElementName.equalsIgnoreCase("gunzip")
        	|| tempElementName.equalsIgnoreCase("bunzip2")
        	|| tempElementName.equalsIgnoreCase("unzip")) {
        	tempElement = new XmlElement(tempElementName) {
                public String getDisplayName() {
		        	String tempDisplayName = super.getDisplayName();
                    XmlAttribute tempXmlAttr = getAttributeNamed("src");
                    if(tempXmlAttr != null) {
                    	tempDisplayName += " " + tempXmlAttr.getValue();
                    }
                    return tempDisplayName;
                }
        	};
        }	
        else if(tempElementName.equalsIgnoreCase("gzip") 
        		|| tempElementName.equalsIgnoreCase("bzip2")) {
        	tempElement = new XmlElement(tempElementName) {
                public String getDisplayName() {
		        	String tempDisplayName = super.getDisplayName();
                    XmlAttribute tempXmlAttr = getAttributeNamed("zipfile");
                    if(tempXmlAttr != null) {
                    	tempDisplayName += " " + tempXmlAttr.getValue();
                    }
                    return tempDisplayName;
                }
        	};
        }	
        else if(tempElementName.equalsIgnoreCase("exec")) {
        	tempElement = new XmlElement(tempElementName) {
                public String getDisplayName() {
		        	String tempDisplayName = "exec ";
                    XmlAttribute tempXmlAttr = getAttributeNamed("command");
                    if(tempXmlAttr != null) {
                    	tempDisplayName += tempXmlAttr.getValue();
                    }
                    tempXmlAttr = getAttributeNamed("executable");
                    if(tempXmlAttr != null) {
                    	tempDisplayName += tempXmlAttr.getValue();
                    }
                    return tempDisplayName;
                }
        	};
        }	
        else if(tempElementName.equalsIgnoreCase("delete")) {
        	tempElement = new XmlElement(tempElementName) {
                public String getDisplayName() {
		        	String tempDisplayName = "delete ";
                    XmlAttribute tempXmlAttr = getAttributeNamed("file");
                    if(tempXmlAttr != null) {
                    	return tempDisplayName + tempXmlAttr.getValue();
                    }
                    tempXmlAttr = getAttributeNamed("dir");
                    if(tempXmlAttr != null) {
                    	return tempDisplayName + tempXmlAttr.getValue();
                    }
                    return tempDisplayName;
                }
        	};
        }	
        else {
	        tempElement = new XmlElement(tempElementName);
        }

		// Add all attributes
		for(int i=0; i<anAttributes.getLength(); i++) {
			String tempAttrName = anAttributes.getLocalName(i);
			if(tempAttrName == null || tempAttrName.length() == 0) {
				tempAttrName = anAttributes.getQName(i);
			}
			String tempAttrValue = anAttributes.getValue(i);
			tempElement.addAttribute(new XmlAttribute(tempAttrName, tempAttrValue));				
		}
         
        return tempElement;
    }


    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    public void endElement(String aUri, String aLocalName, String aQualifiedName)
        throws SAXException {

        String tempTagName = aLocalName.length() > 0 ? aLocalName : aQualifiedName;
        
        XmlElement tempLastStillOpenElement = (XmlElement)stillOpenElements.peek(); 
        if(tempLastStillOpenElement != null && tempLastStillOpenElement.getName().equalsIgnoreCase(tempTagName)) {
            tempLastStillOpenElement.setEndingRow(locator.getLineNumber());
            tempLastStillOpenElement.setEndingColumn(locator.getColumnNumber());
            stillOpenElements.pop();
            
//            if(!stillOpenElements.empty()) {
//                XmlElement tempSecondLastStillOpenElement = (XmlElement)stillOpenElements.peek();
//                tempSecondLastStillOpenElement.addChildNode(tempLastStillOpenElement);
//            }
        }
    }


    /**
     * We use the document locator to locate the position of elements in the
     * document exactly.
     * 
     * @see org.xml.sax.ContentHandler#setDocumentLocator(Locator)
     */
    public void setDocumentLocator(Locator aLocator) {
        locator = aLocator;
        super.setDocumentLocator(aLocator);
    }


    /**
     * Returns the parent that has been created during a prior 
     * parsing.
     * <P>
     * It is quite common that parsing stopped before the current cursor 
     * position. That happens when the parser finds an error within the parsed 
     * document before. 
     * 
     * @return the parent element or <code>null</code> if no parent exists
     */
    public XmlElement getRootElement() {
        return rootElement;
    }


    /* (non-Javadoc)
     * @see org.xml.sax.ErrorHandler#error(SAXParseException)
     */
    public void error(SAXParseException anException) throws SAXException {
        super.error(anException);
    }

    /**
     * We have to handle fatal errors.
     * <P>
     * They come up whenever we parse a not valid file, what we do all the time.
     * Therefore a fatal error is nothing special for us.
     * <P>
     * Actually, we ignore all fatal errors for now.
     * 
     * @see org.xml.sax.ErrorHandler#fatalError(SAXParseException)
     */
    public void fatalError(SAXParseException anException) throws SAXException {
        if(locator != null) {
          //  int tempLineNr = locator.getLineNumber() -1;
          //  int tempColumnNr = locator.getColumnNumber() -1;
          //  super.fatalError(anException);
        }
    }

	/**
	 * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
	 */
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
			int index= systemId.indexOf(':');
			if (index > 0) {
				//remove file:
				systemId= systemId.substring(index+1, systemId.length());
			}
			File relativeFile= null;
			IPath filePath= new Path(systemId);
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(filePath);
			if (file == null) {
				//relative path
				try {
					//this call is ok if mainFile is null
					relativeFile= FileUtils.newFileUtils().resolveFile(mainFile, systemId);
				} catch (BuildException be) {
					return null;
				}
			}
		
			if (relativeFile.exists()) {
				try {
					return new InputSource(new FileReader(relativeFile));
				} catch (FileNotFoundException e) {
					return null;
				}
				
			}
					
		return super.resolveEntity(publicId, systemId);
	}

}