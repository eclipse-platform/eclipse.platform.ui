/*******************************************************************************
 * Copyright (c) 2002, 2003 GEBIT Gesellschaft fuer EDV-Beratung
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.MessageFormat;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.ant.internal.ui.model.AntUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The <code>DefaultHandler</code> for the parsing of the currently edited file.
 */
public class AntEditorSaxDefaultHandler extends DefaultHandler {

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
     * The parent element that we search for.
     * <P>
     * This variable will be set during the parsing process, when we first
     * passed the cursor location.
     */
    protected Element parentElement;

    /**
     * Flag that determines whether we are finished with parsing.
     * <P>
     * This is usually the case when the parent element was found and closed.
     */
    protected boolean parsingFinished;

    /**
     * Document that is used to create the elements.
     */
    protected Document document;

    /**
     * The startingRow where the cursor is located in the document.
     * <P>
     * The first startingRow is refered to with an index of '0'.
     */
    protected int rowOfCursorPosition = -1;
    
    /**
     * The startingColumn where the cursor is located in the document.
     * <P>
     * The first startingColumn is refered to with an index of '0'.
     */
    protected int columnOfCursorPosition = -1;
    
    /**
     * The name of the document root element or null if none seen.
     */
	public String rootElementName;
	
	/**
	 * Used as a helper for resolving external relative entries.
	 */
	private File mainFileContainer;
	
	private IDocument editorDocument;
	
	/**
	 * The find replace adapter for the document
	 */
	private FindReplaceDocumentAdapter findReplaceAdapter;

    /**
     * Creates an AntEditorSaxDefaultHandler, with the specified parameters.
     * 
     * @param rowOfCursorPosition the startingRow where the cursor is located in the
     * document. The first startingRow is refered to with an index of '0'.
     * @param columnOfCursorPosition the startingColumn where the cursor is located in
     * the document. The first startingColumn is refered to with an index of '0'.
     */
    public AntEditorSaxDefaultHandler(IDocument document, File fileContainer, int rowOfCursorPosition, int columnOfCursorPosition) throws ParserConfigurationException {
        super();
        if (rowOfCursorPosition < 0 || columnOfCursorPosition < 0) {
            throw new IllegalArgumentException(MessageFormat.format(AntEditorMessages.getString("AntEditorSaxDefaultHandler.Invalid_cursor_position"), new String[]{Integer.toString(rowOfCursorPosition), Integer.toString(columnOfCursorPosition)})); //$NON-NLS-1$
        }
        this.rowOfCursorPosition = rowOfCursorPosition;
        this.columnOfCursorPosition = columnOfCursorPosition;
        this.mainFileContainer= fileContainer;
        this.editorDocument= document;
        findReplaceAdapter= new FindReplaceDocumentAdapter(document);
        initialize();
    }

    /**
     * Initializes the handler.
     */
    protected void initialize() throws ParserConfigurationException {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        document = documentBuilder.newDocument();
    }
    
    /**
     * Checks whether the parent element, that we are searching for can be or
     * has already been determined.
     * <P>
     * This will be done by comparing the current parsing position with the
     * cursor position. If we just passed the cursor position and the parent
     * element has not been set yet, it will be set.
     * 
     * @return <code>true</code> if the parent element is known otherwise
     * <code>false</code>
     */
    protected boolean checkForParentElement(String tagName) {
    	if(parentElement == null) {
    		if(locator != null) {
    			//The locator's numbers are 1-based though, we do everything
    			//0-based.
    			int lineNum = locator.getLineNumber();
    			int columnNum = locator.getColumnNumber() - 1;
    			if (columnNum < 0) {
    				
    				try {
    					int offset = getOffset(lineNum, getLastCharColumn(lineNum));
    					IRegion result= findReplaceAdapter.search(offset, tagName, false, false, false, false); //$NON-NLS-1$
    					if (result != null) {
    						offset= result.getOffset();
    						columnNum= getColumn(offset, lineNum);
    					}
    				} catch (BadLocationException e) {
    				}
    			}
    			lineNum= lineNum - 1;
    			if(lineNum > rowOfCursorPosition
    					|| (lineNum == rowOfCursorPosition && columnNum > columnOfCursorPosition)
    					&& !stillOpenElements.isEmpty()) {
    				parentElement = (Element)stillOpenElements.peek();
    				return true;
    			}
    		}
    		return false;
    	}
    	
    	// Parent element has been set already before
    	return true;
    }
    
    private int getColumn(int offset, int line) throws BadLocationException {
    	return offset - editorDocument.getLineOffset(line - 1) + 1;
    }
    
    private int getOffset(int line, int column) throws BadLocationException {
    	return editorDocument.getLineOffset(line - 1) + column - 1;
    }
    
    private int getLastCharColumn(int line) throws BadLocationException {
    	String lineDelimiter= editorDocument.getLineDelimiter(line - 1);
    	int lineDelimiterLength= lineDelimiter != null ? lineDelimiter.length() : 0;
    	return editorDocument.getLineLength(line - 1) - lineDelimiterLength;
    }
    
    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
     */
    public void startElement(String uri, String localName, String qualifiedName, Attributes attributes) {
       
        if(parsingFinished) {
            return;
        }        
  
         // While the crimson parser passes the tag name as local name, apache's
         // xerces parser, passes the tag name as qualilfied name and an empty 
         // string as local name.
        String tagName = localName.length() > 0 ? localName : qualifiedName;
        if(tagName == null || tagName.length() == 0) {
            throw new AntEditorException(AntEditorMessages.getString("AntEditorSaxDefaultHandler.Error_parsing")); //$NON-NLS-1$
        }
        
        // Checks whether we know the parent for sure
        checkForParentElement('<' + tagName); //$NON-NLS-1$
        
        // Create a Dom Element
        Element element = document.createElement(tagName);
        stillOpenElements.push(element);
        
        // This code added to determine root element in a rational way
        if (rootElementName == null) {
        	rootElementName = tagName;
        }  
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    public void endElement(String aUri, String aLocalName, String aQualifiedName) {

		if(parsingFinished) {
            return;
        }        
        
        String tagName = aLocalName.length() > 0 ? aLocalName : aQualifiedName;
        // Checks whether we know the parent for sure
        boolean parentKnown = checkForParentElement('/' + tagName);
        
		if(!stillOpenElements.isEmpty()) {        
	        Element lastStillOpenElement = (Element)stillOpenElements.peek(); 
	        if(lastStillOpenElement != null && lastStillOpenElement.getTagName().equals(tagName)) {
	            stillOpenElements.pop();
	            
	            if(!stillOpenElements.empty()) {
	                Element secondLastStillOpenElement = (Element)stillOpenElements.peek();
	                secondLastStillOpenElement.appendChild(lastStillOpenElement);
	            }
	            if(parentKnown && parentElement != null && parentElement.getTagName().equals(tagName)) {
	                parsingFinished = true;
	            }
	        }
		}
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#setDocumentLocator(Locator)
     */
    public void setDocumentLocator(Locator aLocator) {
        locator = aLocator;
    }

    /**
     * Returns the parent element that has been determined during a prior 
     * parsing.
     * <P>
     * It is quite common that parsing stopped before the current cursor 
     * position. That happens when the parser finds an error within the parsed 
     * document before. In that case the parent element might be guessed to be
     * the one that opened last. To tell the handler whether the parent should
     * be guessed, <code>guessParent</code> may be specified.
     * 
     * @param guessParent whether the parent should be guessed
     * @return the parent element or <code>null</code> if not known.
     */
    public Element getParentElement(boolean guessParent) {
        if(parentElement != null) {
            return parentElement;
        }
        if(guessParent) {
            if(!stillOpenElements.empty()) {
                return (Element)stillOpenElements.peek();
            }
        }
        return null;
    }

    /**
     * We have to handle fatal errors.
     * <P>
     * Fatal errors come up whenever we parse a not valid file, which we do all the time.
     * Therefore a fatal error is nothing special for us.
     * <P>
     * 
     * @see org.xml.sax.ErrorHandler#fatalError(SAXParseException)
     */
    public void fatalError(SAXParseException anException) {
    }
    
	/**
	 * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
	 */
	public InputSource resolveEntity(String publicId, String systemId) {
		int index= systemId.indexOf(':');
		if (index > 0) {
			//remove file:
			systemId= systemId.substring(index+1, systemId.length());
		}
		File resolvedFile= null;
		IFile file= AntUtil.getFileForLocation(systemId, mainFileContainer);
		if (file == null) {
			return null;
		} 
		
		resolvedFile= file.getLocation().toFile();
		
		try {
			InputSource inputSource= new InputSource(new FileReader(resolvedFile));
			inputSource.setSystemId(resolvedFile.getAbsolutePath());
			return inputSource;
		} catch (FileNotFoundException e) {
			return null;
		}
	}
}