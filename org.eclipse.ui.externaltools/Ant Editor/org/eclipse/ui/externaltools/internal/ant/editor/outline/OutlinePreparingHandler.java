/**********************************************************************
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
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
import org.eclipse.ui.externaltools.internal.ant.editor.xml.IAntEditorConstants;
import org.eclipse.ui.externaltools.internal.ant.editor.xml.XmlAttribute;
import org.eclipse.ui.externaltools.internal.ant.editor.xml.XmlElement;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;
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
public class OutlinePreparingHandler extends DefaultHandler implements LexicalHandler {

    /**
     * The locator that tells us the location of the currently parsed element 
     * in the parsed document.
     */
	private Locator locator;


    /**
     * Stack of still open elements.
     * <P>
     * On top of the stack is the innermost element.
     */
	private Stack stillOpenElements = new Stack();


    /**
     * The root element of the DOM Tree that we create while parsing.
     */
	private XmlElement rootElement;
    
    /**
     * Used as a helper for resolving external relative entries.
     */
    private File mainFileContainer;


    /**
     * Creates an instance.
     */
    public OutlinePreparingHandler(File mainFileContainer) throws ParserConfigurationException {
        super();
        this.mainFileContainer= mainFileContainer;
    }


    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
     */
    public void startElement(String aUri, String aLocalName, String aQualifiedName, Attributes anAttributes)
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
        } else {  // Add it as child to parent
            XmlElement tempLastOpenedElement = (XmlElement)stillOpenElements.peek();
            tempLastOpenedElement.addChildNode(tempElement);
            tempElement.setExternal(tempLastOpenedElement.isExternal());
        }
    
        stillOpenElements.push(tempElement);
        
        super.startElement(aUri, aLocalName, aQualifiedName, anAttributes);
    }
    
    
    /**
     * Creates an <code>XmlElement</code> instance from the specified parameters.
     */
    protected XmlElement createXmlElement(String aLocalName, String aQualifiedName, Attributes attributes) {
		String tempElementName = null;

        XmlElement tempElement = null;
        if(aLocalName.length() > 0) {
            tempElementName = aLocalName;
        }
        else if(aQualifiedName.length() > 0) {
            tempElementName = aQualifiedName;
        }   
        else {
            throw new PlantyException(AntOutlineMessages.getString("OutlinePreparingHandler.Error_Name")); //$NON-NLS-1$
        }
        String elementType= null;
        if(tempElementName.equalsIgnoreCase("target")) { //$NON-NLS-1$
        	elementType= IAntEditorConstants.TYPE_TARGET;
        	tempElement = new XmlElement(tempElementName) {
                public String getDisplayName() {
                    XmlAttribute tempXmlAttr = getAttributeNamed(IAntEditorConstants.ATTR_NAME);
                    if(tempXmlAttr != null) {
                    	StringBuffer name= new StringBuffer(tempXmlAttr.getValue());
                    	XmlAttribute defaultTarget= getParentNode().getAttributeNamed(IAntEditorConstants.ATTR_DEFAULT);
                    	if (defaultTarget != null && defaultTarget.getValue().equals(tempXmlAttr.getValue())) {
                    		name.append(AntOutlineMessages.getString("OutlinePreparingHandler._[default]_2")); //$NON-NLS-1$
                    	}
                    	return name.toString();
                    }
                    return super.getDisplayName();
                }
        	};
        } else if (tempElementName.equalsIgnoreCase("project")) { //$NON-NLS-1$
			elementType= IAntEditorConstants.TYPE_PROJECT;
			tempElement = new XmlElement(tempElementName) {
				public String getDisplayName() {
					XmlAttribute tempXmlAttr = getAttributeNamed(IAntEditorConstants.ATTR_NAME);
					if(tempXmlAttr != null) {
						return tempXmlAttr.getValue();
					}
					return super.getDisplayName();
				}
			};
        } else if(tempElementName.equalsIgnoreCase("property")) { //$NON-NLS-1$
			elementType= IAntEditorConstants.TYPE_PROPERTY;
        	tempElement = new XmlElement(tempElementName) {
                public String getDisplayName() {
                    XmlAttribute tempXmlAttr = getAttributeNamed(IAntEditorConstants.ATTR_NAME);
                    if(tempXmlAttr != null) {
                    	return tempXmlAttr.getValue();
                    }
                    tempXmlAttr = getAttributeNamed(IAntEditorConstants.ATTR_FILE);
                    if(tempXmlAttr != null) {
                    	return "file="+tempXmlAttr.getValue(); //$NON-NLS-1$
                    }	
                    tempXmlAttr = getAttributeNamed(IAntEditorConstants.ATTR_RESOURCE);
                    if(tempXmlAttr != null) {
                    	return "resource="+tempXmlAttr.getValue(); //$NON-NLS-1$
                    }	
                    tempXmlAttr = getAttributeNamed(IAntEditorConstants.ATTR_ENVIRONMENT);
                    if(tempXmlAttr != null) {
                    	return "environment="+tempXmlAttr.getValue(); //$NON-NLS-1$
                    }	
                    return super.getDisplayName();
                }
        	};
        }	
        else if(tempElementName.equalsIgnoreCase("antcall")) { //$NON-NLS-1$
			elementType= IAntEditorConstants.TYPE_ANTCALL;
        	tempElement = new XmlElement(tempElementName) {
                public String getDisplayName() {
		        	String tempDisplayName = "antcall "; //$NON-NLS-1$
                    XmlAttribute tempXmlAttr = getAttributeNamed(IAntEditorConstants.ATTR_TARGET);
                    if(tempXmlAttr != null) {
                    	tempDisplayName += tempXmlAttr.getValue();
                    }
                    return tempDisplayName;
                }
        	};
        }	
        else if(tempElementName.equalsIgnoreCase("mkdir")) { //$NON-NLS-1$
			elementType= IAntEditorConstants.TYPE_MKDIR;
        	tempElement = new XmlElement(tempElementName) {
                public String getDisplayName() {
		        	String tempDisplayName = "mkdir "; //$NON-NLS-1$
                    XmlAttribute tempXmlAttr = getAttributeNamed(IAntEditorConstants.ATTR_DIR);
                    if(tempXmlAttr != null) {
                    	tempDisplayName += tempXmlAttr.getValue();
                    }
                    return tempDisplayName;
                }
        	};
        }	
        else if(tempElementName.equalsIgnoreCase("copy")) { //$NON-NLS-1$
			elementType= IAntEditorConstants.TYPE_COPY;
        	tempElement = new XmlElement(tempElementName) {
                public String getDisplayName() {
		        	String tempDisplayName = "copy "; //$NON-NLS-1$
                    XmlAttribute tempXmlAttr = getAttributeNamed(IAntEditorConstants.ATTR_FILE);
                    if(tempXmlAttr != null) {
                    	tempDisplayName += tempXmlAttr.getValue();
                    }
                    return tempDisplayName;
                }
        	};
        }	
        else if(tempElementName.equalsIgnoreCase("tar")  //$NON-NLS-1$
        	|| tempElementName.equalsIgnoreCase("jar") //$NON-NLS-1$
        	|| tempElementName.equalsIgnoreCase("war") //$NON-NLS-1$
        	|| tempElementName.equalsIgnoreCase("zip")) { //$NON-NLS-1$
			elementType= IAntEditorConstants.TYPE_ARCHIVE;
        	tempElement = new XmlElement(tempElementName) {
                public String getDisplayName() {
		        	String tempDisplayName = super.getDisplayName();
                    XmlAttribute tempXmlAttr = getAttributeNamed(IAntEditorConstants.ATTR_DESTFILE);
                    if(tempXmlAttr != null) {
                    	tempDisplayName += " " + tempXmlAttr.getValue(); //$NON-NLS-1$
                    }
                    return tempDisplayName;
                }
        	};
        }	
        else if(tempElementName.equalsIgnoreCase("untar")  //$NON-NLS-1$
        	|| tempElementName.equalsIgnoreCase("unjar") //$NON-NLS-1$
        	|| tempElementName.equalsIgnoreCase("unwar") //$NON-NLS-1$
        	|| tempElementName.equalsIgnoreCase("gunzip") //$NON-NLS-1$
        	|| tempElementName.equalsIgnoreCase("bunzip2") //$NON-NLS-1$
        	|| tempElementName.equalsIgnoreCase("unzip")) { //$NON-NLS-1$
			elementType= IAntEditorConstants.TYPE_DECOMPRESS;
        	tempElement = new XmlElement(tempElementName) {
                public String getDisplayName() {
		        	String tempDisplayName = super.getDisplayName();
                    XmlAttribute tempXmlAttr = getAttributeNamed(IAntEditorConstants.ATTR_SRC);
                    if(tempXmlAttr != null) {
                    	tempDisplayName += " " + tempXmlAttr.getValue(); //$NON-NLS-1$
                    }
                    return tempDisplayName;
                }
        	};
        }	
        else if(tempElementName.equalsIgnoreCase("gzip")  //$NON-NLS-1$
        		|| tempElementName.equalsIgnoreCase("bzip2")) { //$NON-NLS-1$
			elementType= IAntEditorConstants.TYPE_COMPRESS;
        	tempElement = new XmlElement(tempElementName) {
                public String getDisplayName() {
		        	String tempDisplayName = super.getDisplayName();
                    XmlAttribute tempXmlAttr = getAttributeNamed(IAntEditorConstants.ATTR_ZIPFILE);
                    if(tempXmlAttr != null) {
                    	tempDisplayName += " " + tempXmlAttr.getValue(); //$NON-NLS-1$
                    }
                    return tempDisplayName;
                }
        	};
        }	
        else if(tempElementName.equalsIgnoreCase("exec")) { //$NON-NLS-1$
			elementType= IAntEditorConstants.TYPE_EXEC;
        	tempElement = new XmlElement(tempElementName) {
                public String getDisplayName() {
		        	String tempDisplayName = "exec "; //$NON-NLS-1$
                    XmlAttribute tempXmlAttr = getAttributeNamed(IAntEditorConstants.ATTR_COMMAND);
                    if(tempXmlAttr != null) {
                    	tempDisplayName += tempXmlAttr.getValue();
                    }
                    tempXmlAttr = getAttributeNamed(IAntEditorConstants.ATTR_EXECUTABLE);
                    if(tempXmlAttr != null) {
                    	tempDisplayName += tempXmlAttr.getValue();
                    }
                    return tempDisplayName;
                }
        	};
        }	
        else if(tempElementName.equalsIgnoreCase("delete")) { //$NON-NLS-1$
			elementType= IAntEditorConstants.TYPE_DELETE;
        	tempElement = new XmlElement(tempElementName) {
                public String getDisplayName() {
		        	String tempDisplayName = "delete "; //$NON-NLS-1$
                    XmlAttribute tempXmlAttr = getAttributeNamed(IAntEditorConstants.ATTR_FILE);
                    if(tempXmlAttr != null) {
                    	return tempDisplayName + tempXmlAttr.getValue();
                    }
                    tempXmlAttr = getAttributeNamed(IAntEditorConstants.ATTR_DIR);
                    if(tempXmlAttr != null) {
                    	return tempDisplayName + tempXmlAttr.getValue();
                    }
                    return tempDisplayName;
                }
        	};
        }	
        else {
			elementType= IAntEditorConstants.TYPE_UNKNOWN;
	        tempElement = new XmlElement(tempElementName);
        }

		if (attributes != null) { 
			// Add all attributes
			for(int i=0; i < attributes.getLength(); i++) {
				String tempAttrName = attributes.getLocalName(i);
				if(tempAttrName == null || tempAttrName.length() == 0) {
					tempAttrName = attributes.getQName(i);
				}
				String tempAttrValue = attributes.getValue(i);
				tempElement.addAttribute(new XmlAttribute(tempAttrName, tempAttrValue));				
			}
		}
		// Add the type attribute
		tempElement.addAttribute(new XmlAttribute(IAntEditorConstants.ATTR_TYPE, elementType));
        tempElement.setFilePath(locator.getSystemId());
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
		generateErrorElementHierarchy(anException);
    }

    /**
     * Fatal errors occur when we parse a non valid file. Populate the error
     * element hierarchy.
     * 
     * @see org.xml.sax.ErrorHandler#fatalError(SAXParseException)
     */
    public void fatalError(SAXParseException anException) throws SAXException {
    	generateErrorElementHierarchy(anException);
    }

	private void generateErrorElementHierarchy(SAXParseException exception) {
		if (rootElement == null) {
			rootElement= new XmlElement(exception.getSystemId());
		}
		rootElement.setIsErrorNode(true);
		
		if (rootElement.getStartingRow() == 0) {
			//an error occurred attempting to create the root element
			if(locator != null) {
				rootElement.setStartingColumn(locator.getColumnNumber());
				rootElement.setStartingRow(locator.getLineNumber());
			}
		}
		
		int lineNumber= exception.getLineNumber();
		StringBuffer message= new StringBuffer(exception.getMessage());
		if (lineNumber != -1){
			message.append(AntOutlineMessages.getString("OutlinePreparingHandler._line___2") + lineNumber); //$NON-NLS-1$
		}

		XmlElement errorNode= new XmlElement(message.toString());
		errorNode.setIsErrorNode(true);
		if(locator != null) {
			errorNode.setStartingColumn(locator.getColumnNumber());
			errorNode.setStartingRow(locator.getLineNumber());
		}
		rootElement.addChildNode(errorNode);
	}
	
	/**
	 * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
	 */
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
			int index= systemId.indexOf(':');
			if (index > 0) {
				//remove file:
				systemId= systemId.substring(index + 1, systemId.length());
			}
			File resolvedFile= null;
			IPath filePath= new Path(systemId);
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(filePath);
			if (file == null || !file.exists()) {
				//relative path
				try {
					//this call is ok if mainFileContainer is null
					resolvedFile= FileUtils.newFileUtils().resolveFile(mainFileContainer, systemId);
				} catch (BuildException be) {
					return null;
				}
			} else {
				resolvedFile= file.getLocation().toFile();
			}
		
			if (resolvedFile != null && resolvedFile.exists()) {
				try {
					InputSource inputSource= new InputSource(new FileReader(resolvedFile));
					inputSource.setSystemId(resolvedFile.getAbsolutePath());
					return inputSource;
				} catch (FileNotFoundException e) {
					return null;
				}
			}
					
		return super.resolveEntity(publicId, systemId);
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#comment(char[], int, int)
	 */
	public void comment(char[] ch, int start, int length) throws SAXException {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#endCDATA()
	 */
	public void endCDATA() throws SAXException {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#endDTD()
	 */
	public void endDTD() throws SAXException {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
	 */
	public void endEntity(String name) throws SAXException {
		endElement(null, name, ""); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#startCDATA()
	 */
	public void startCDATA() throws SAXException {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#startDTD(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void startDTD(String name, String publicId, String systemId) throws SAXException {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
	 */
	public void startEntity(String name) throws SAXException {
		startElement(null, name, "", null); //$NON-NLS-1$
		XmlElement external= (XmlElement)stillOpenElements.peek();
		external.setExternal(true);
		external.setRootExternal(true);
	}
}