/*******************************************************************************
 * Copyright (c) 2002, 2003 GEBIT Gesellschaft fuer EDV-Beratung
 * und Informatik-Technologien mbH, 
 * Berlin, Duesseldorf, Frankfurt (Germany).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     GEBIT Gesellschaft fuer EDV-Beratung und Informatik-Technologien mbH - initial API and implementation
 * 	   IBM Corporation - bug fixes
 *******************************************************************************/

package org.eclipse.ui.externaltools.internal.ant.editor.outline;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Stack;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.FileUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.externaltools.internal.ant.editor.PlantyException;
import org.eclipse.ui.externaltools.internal.ant.editor.xml.IAntEditorConstants;
import org.eclipse.ui.externaltools.internal.ant.editor.xml.XmlAttribute;
import org.eclipse.ui.externaltools.internal.ant.editor.xml.XmlElement;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
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
	 * Used as a helper for resolving external relative entries
	 * and paths for error elements.
	 */
    private File mainFile;

    /**
     * Helper for generating <code>IProblem</code>s
     */
    private XEErrorHandler errorHandler;

    /**
     * The parsed document
     */
    private IDocument document;
    
    /**
     * Whether the current element is the root of the external entity tree
     */
    private boolean isTopLevelRootExternal;

    /**
     * Whether the current callbacks are from elements in the DTD.
     */
    private boolean isInDTD;
   
     /**
     * Start offset of the last seen external entity.
     */
    private int lastExternalEntityOffset= -1;

    /**
     * Creates an instance.
     */
    public OutlinePreparingHandler(ILocationProvider locationProvider) {
		super();
		IPath location= locationProvider.getLocation();
		if(location != null) {
			this.mainFile= location.toFile();
		}
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

        // Is it our root
        if(rootElement == null) {
            rootElement = tempElement;
        } else {  // Add it as child to parent
            XmlElement tempLastOpenedElement = (XmlElement)stillOpenElements.peek();
            tempLastOpenedElement.addChildNode(tempElement);
            tempElement.setExternal(isExternal());
        }
    
		// set starting the location
		computeStartLocation(tempElement);
        
        stillOpenElements.push(tempElement);
        
        super.startElement(aUri, aLocalName, aQualifiedName, anAttributes);
    }
    
    
    /**
     * Creates an <code>XmlElement</code> instance from the specified parameters.
     */
    private XmlElement createXmlElement(String aLocalName, String aQualifiedName, Attributes attributes) {
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
						XmlElement parent= getParentNode();
						XmlAttribute type= parent != null ? parent.getAttributeNamed(IAntEditorConstants.ATTR_TYPE) : null;
						while (parent != null && (type == null || !type.getValue().equals(IAntEditorConstants.TYPE_PROJECT))){
							parent= parent.getParentNode();
							if (parent != null) {
								type= parent.getAttributeNamed(IAntEditorConstants.ATTR_TYPE);
							}
						}
						if (parent != null) {
                    		XmlAttribute defaultTarget= parent.getAttributeNamed(IAntEditorConstants.ATTR_DEFAULT);
                    		if (defaultTarget != null && defaultTarget.getValue().equals(tempXmlAttr.getValue())) {
                    			name.append(AntOutlineMessages.getString("OutlinePreparingHandler._[default]_2")); //$NON-NLS-1$
                    		}
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
	    	computeEndLocation(tempLastStillOpenElement);
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
        if (errorHandler != null) {
        	errorHandler.setDocumentLocator(aLocator);
        }
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


    /*
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    public void warning(SAXParseException anException) throws SAXException {
		if (errorHandler != null) {
			XmlElement element= createProblemElement(anException);
			errorHandler.warning(anException, element);
		}
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ErrorHandler#error(SAXParseException)
     */
    public void error(SAXParseException anException) throws SAXException {
		generateErrorElementHierarchy();
		if (errorHandler != null) {
			XmlElement errorElement= createProblemElement(anException);
			errorHandler.error(anException, errorElement);
		}
    }

    /**
     * Fatal errors occur when we parse a non valid file. Populate the error
     * element hierarchy.
     * 
     * @see org.xml.sax.ErrorHandler#fatalError(SAXParseException)
     */
    public void fatalError(SAXParseException anException) throws SAXException {
		generateErrorElementHierarchy();
		if (errorHandler != null) {
			XmlElement errorElement= createProblemElement(anException);
			errorHandler.fatalError(anException, errorElement);
		}
    }

	private XmlElement createProblemElement(SAXParseException exception) {
		int lineNumber= exception.getLineNumber();
		StringBuffer message= new StringBuffer(exception.getMessage());
		if (lineNumber != -1){
			message.append(AntOutlineMessages.getString("OutlinePreparingHandler._line___2") + lineNumber); //$NON-NLS-1$
		}

		XmlElement errorNode= new XmlElement(message.toString());
		errorNode.setFilePath(exception.getSystemId());
		errorNode.setExternal(isExternal());
		errorNode.setIsErrorNode(true);
		computeErrorLocation(errorNode, exception);
		return errorNode;
	}
	
	private void generateErrorElementHierarchy() {
		XmlElement openElement= getLastOpenElement();
		while (openElement != null) {
			openElement.setIsErrorNode(true);
			openElement= openElement.getParentNode();
		}
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
					resolvedFile= FileUtils.newFileUtils().resolveFile(mainFile.getParentFile(), systemId);
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
		if (isInDTD || isExternal()) {
			return;
		}

		try {
			lastExternalEntityOffset= getOffset(locator.getLineNumber(), locator.getColumnNumber()) - 1;
		} catch (BadLocationException e) {
			ExternalToolsPlugin.getDefault().log(e);
		}
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
		isInDTD= false;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
	 */
	public void endEntity(String name) throws SAXException {
		if (isInDTD) {
			return;
		}
		XmlElement element= getLastOpenElement();
		boolean isNestedRootExternal= element == null || element.getParentNode() == null || element.getParentNode().isExternal();
		if (!isNestedRootExternal) {
			isTopLevelRootExternal= true;
		}
		endElement(null, name, ""); //$NON-NLS-1$
		if (!isNestedRootExternal) {
			isTopLevelRootExternal= false;
		}
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
		isInDTD= true;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
	 */
	public void startEntity(String name) throws SAXException {
		if (isInDTD) {
			return;
		}
		boolean isNestedRootExternal= isExternal();
		if (!isNestedRootExternal) {
			isTopLevelRootExternal= true;
		}
		startElement(null, name, "", null); //$NON-NLS-1$
		XmlElement external= (XmlElement)stillOpenElements.peek();
		external.setExternal(true);
		external.setRootExternal(true);
		external.getAttributes().removeAll(external.getAttributes());
		external.addAttribute(new XmlAttribute(IAntEditorConstants.ATTR_TYPE, IAntEditorConstants.TYPE_EXTERNAL));
		if (!isNestedRootExternal) {
			isTopLevelRootExternal= false;
		}
	}

	private void computeStartLocation(XmlElement element) {
		if (element.isExternal()) {
			return;
		}
		
		try {
			int offset;
 			
			if (isTopLevelRootExternal) {
				String source= "&" + element.getName() + ";";  //$NON-NLS-1$ //$NON-NLS-2$
				offset= document.search(lastExternalEntityOffset + 1, source, true, true, false);
				lastExternalEntityOffset= offset;
			} else {
				int locatorLine= locator.getLineNumber();
				int locatorColumn= locator.getColumnNumber();
				String prefix= "<"; //$NON-NLS-1$

				if (locatorColumn <= 0) {
					offset= getOffset(locatorLine, getLastCharColumn(locatorLine));
					offset= document.search(offset, prefix + element.getName(), false, false, false);
					
					lastExternalEntityOffset= offset;
				} else {
					offset= getOffset(locatorLine, locatorColumn);
					
					lastExternalEntityOffset= offset - 1;
					
					offset= document.search(offset - 1, prefix, false, true, false); 
				}
			}
 			
			int line= getLine(offset);
			int column= getColumn(offset, line);
			
			element.setOffset(offset);
			element.setStartingRow(line);
			element.setStartingColumn(column);
		} catch (BadLocationException e) {
			//ignore as the parser may be out of sync with the document during reconciliation
		}
	}

	private void computeEndLocation(XmlElement element) {
		if (element.isExternal() && !isTopLevelRootExternal) {
			return;
		}
		
		try {
			int length, line, column;

			if (isTopLevelRootExternal) {
				length= element.getName().length() + 2;
				line= element.getStartingRow();
				column= element.getStartingColumn() + length;
			} else {
				line= locator.getLineNumber();
				column= locator.getColumnNumber();
				
				int offset;
 			
				if (column <= 0) {
					int lineOffset= getOffset(line, 1);
					offset= document.search(lineOffset, element.getName(), true, false, false);
					if (offset < 0 || getLine(offset) != line) {
						offset= lineOffset;
					}
					String endDelimiter= ">"; //$NON-NLS-1$
					offset= document.search(lineOffset, endDelimiter, true, true, false); //$NON-NLS-1$
					if (offset < 0 || getLine(offset) != line) {
						offset= lineOffset;
						column= 1;
					} else {
						offset++;
						column= getColumn(offset, line);
					}
				} else {
					offset= getOffset(line, column);
				}
				
				length= offset - element.getOffset();
				
				lastExternalEntityOffset= offset - 1;
			}
 			
			element.setLength(length);
			element.setEndingRow(line);
			element.setEndingColumn(column);
		} catch (BadLocationException e) {
			//ignore as the parser may be out of sync with the document during reconciliation
		}
	}

	private void computeErrorLocation(XmlElement element, SAXParseException exception) {
		if (element.isExternal()) {
			return;
		}
		
		try {
			int line= exception.getLineNumber();
			int startColumn= exception.getColumnNumber();
			int endColumn;
			
			if (line <= 0) {
				line= locator.getLineNumber();
				startColumn= locator.getColumnNumber();
				
				if (line <= 0) {
					line= 1;
					startColumn= 1;
				}
			}

			if (startColumn <= 0) {
				startColumn= 1;
				endColumn= getLastCharColumn(line) + 1;
			} else {
				if (startColumn > 1) {
					--startColumn;
				}
				
				endColumn= startColumn;
				if (startColumn <= getLastCharColumn(line)) {
					++endColumn;
				}
			}
			
			int offset= getOffset(line, startColumn);

			element.setStartingRow(line);
			element.setStartingColumn(startColumn);
			element.setOffset(offset);
			
			element.setEndingRow(line);
			element.setEndingColumn(endColumn);
			element.setLength(endColumn - startColumn);
		} catch (BadLocationException e) {
			//ignore as the parser may be out of sync with the document during reconciliation
		}		
	}

	public void fixEndLocations() {
		fixEndLocations(null);
	}
	
	public void fixEndLocations(SAXParseException e) {
		XmlElement lastOpenElement= getLastOpenElement();
		if (lastOpenElement == null) {
			return;
		}
		
		boolean recoverFromExternal= lastOpenElement.isExternal();
		
		while (lastOpenElement.isExternal() && (!lastOpenElement.isRootExternal() || (lastOpenElement.getParentNode() != null && lastOpenElement.getParentNode().isExternal()))) {
			stillOpenElements.pop();
			lastOpenElement= getLastOpenElement();
		}
		
		try {
			int offset, line, column;
			
			if (recoverFromExternal) {
				XmlElement element= (XmlElement) stillOpenElements.peek();
				int length= element.getName().length() + 2;
				offset= element.getOffset() + length;
				line= element.getStartingRow();
				column= element.getStartingColumn() + length;
			} else if (e == null) {
				XmlElement element= (XmlElement) stillOpenElements.peek();			
				offset= element.getOffset();
				line= element.getStartingRow();
				column= element.getStartingColumn();
			} else {
				line= e.getLineNumber();
				column= e.getColumnNumber();
				
				if (line <= 0) {
					line= 1;
				}
				
				if (column <= 0) {
					column= 1;
				}

				offset= getOffset(line, column);
			}			
			
			while (!stillOpenElements.empty()) {
				XmlElement element= (XmlElement) stillOpenElements.pop();			
				element.setLength(offset - element.getOffset());
				element.setEndingRow(line);
				element.setEndingColumn(column);
			}
		} catch (BadLocationException ble) {
			//ignore as the parser may be out of sync with the document during reconciliation
		}
	}

	public XmlElement getLastOpenElement() {
		if (!stillOpenElements.empty()) {
			return (XmlElement) stillOpenElements.peek();
		}
		return null;
	}

	private boolean isExternal() {
		if (!stillOpenElements.empty()) {
			return ((XmlElement) stillOpenElements.peek()).isExternal();
		}
		return false;
	}

	private int getLastCharColumn(int line) throws BadLocationException {
		String lineDelimiter= document.getLineDelimiter(line - 1);
		int lineDelimiterLength= lineDelimiter != null ? lineDelimiter.length() : 0;
		return document.getLineLength(line - 1) - lineDelimiterLength;
	}

	private int getOffset(int line, int column) throws BadLocationException {
		return document.getLineOffset(line - 1) + column - 1;
	}

	private int getLine(int offset) throws BadLocationException {
		return document.getLineOfOffset(offset) + 1;
	}

	private int getColumn(int offset, int line) throws BadLocationException {
		return offset - document.getLineOffset(line - 1) + 1;
	}

	public void begin() {
		if (errorHandler != null) {
			errorHandler.beginReporting();
		}
	}

	public void end() {
		if (errorHandler != null) {
			errorHandler.endReporting();
		}
	}

	public void setDocument(IDocument document) {
		this.document= document;
	}

	public void setProblemRequestor(IProblemRequestor requestor) {
		if (requestor != null) {
			errorHandler= new XEErrorHandler(requestor);
		} else {
			errorHandler= null;
		}
	}
}