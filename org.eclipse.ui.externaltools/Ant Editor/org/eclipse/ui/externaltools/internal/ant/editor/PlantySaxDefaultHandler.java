//
// PlantySaxDefaultHandler.java
//
// Copyright:
// GEBIT Gesellschaft fuer EDV-Beratung
// und Informatik-Technologien mbH, 
// Berlin, Duesseldorf, Frankfurt (Germany) 2002
// All rights reserved.
//
package org.eclipse.ui.externaltools.internal.ant.editor;

import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * The <code>DefaultHandler</code> for the parsing of the currently edited file.
 * 
 * @version 19.09.2002
 * @author Alf Schiefelbein
 */
public class PlantySaxDefaultHandler extends DefaultHandler {

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
     * Flag that determines wether we are finished with parsing.
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
     * Creates an PlantySaxDefaultHandler, with the specified parameters.
     * 
     * @param aRowOfCursorPosition the startingRow where the cursor is located in the
     * document. The first startingRow is refered to with an index of '0'.
     * @param aColumnOfCursorPosition the startingColumn where the cursor is located in
     * the document. The first startingColumn is refered to with an index of '0'.
     */
    public PlantySaxDefaultHandler(int aRowOfCursorPosition, int aColumnOfCursorPosition) throws ParserConfigurationException {
        super();
		if (ExternalToolsPlugin.getDefault() != null && ExternalToolsPlugin.getDefault().isDebugging()) {
			ExternalToolsPlugin.getDefault().log("PlantySaxDefaultHandler(" +aRowOfCursorPosition+ ", "+aColumnOfCursorPosition+ ")", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        if(aRowOfCursorPosition < 0 || aColumnOfCursorPosition < 0) {
            throw new IllegalArgumentException("The cursor position of " +aRowOfCursorPosition+ ", " +aColumnOfCursorPosition+ "(startingRow, startingColumn) is not valid");
        }
        rowOfCursorPosition = aRowOfCursorPosition;
        columnOfCursorPosition = aColumnOfCursorPosition;
        initialize();
    }


    /**
     * Initializes the handler.
     */
    protected void initialize() throws ParserConfigurationException {
        DocumentBuilder tempDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        document = tempDocumentBuilder.newDocument();
    }



    /**
     * Checks wether the parent element, that we are searching for can be or
     * has already been determined.
     * <P>
     * This will be done by comparing the current parsing position with the
     * cursor position. If we just passed the cursor position and the parent
     * element has not been set yet, it will be set.
     * 
     * @return <code>true</code> if the parent element is known otherwise
     * <code>false</code>
     */
    protected boolean checkForParentElement() {
        if(parentElement == null) {
            if(locator != null) {

                /*
                 * The locator's numbers are 1-based though, we do everything
                 * 0-based.
                 */

                int tempLineNr = locator.getLineNumber() -1;
                int tempColumnNr = locator.getColumnNumber() -1;
                if(tempLineNr> rowOfCursorPosition ||
                    (tempLineNr == rowOfCursorPosition && tempColumnNr > columnOfCursorPosition)) {
                        parentElement = (Element)stillOpenElements.peek();
                        if (ExternalToolsPlugin.getDefault() != null && ExternalToolsPlugin.getDefault().isDebugging()) {
							ExternalToolsPlugin.getDefault().log("PlantySaxDefaultHandler.checkForParentElement(): Parent element found: " +parentElement, null); //$NON-NLS-1$
                        }
                        return true;
                    }
            }
            return false;
        }
        
        // Parent element has been set already before
        return true;
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
        
		 if (ExternalToolsPlugin.getDefault() != null && ExternalToolsPlugin.getDefault().isDebugging()) {
			ExternalToolsPlugin.getDefault().log("PlantySaxDefaultHandler.startElement(" +aUri+ ", " +aLocalName+ ", "+aQualifiedName+ ", "+anAttributes+ ")", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        }
        if(parsingFinished) {
            return;
        }        

        // Checks wether we know the parent for sure
        checkForParentElement();

        // Create a Dom Element
        String tempTagName = aLocalName.length() > 0 ? aLocalName : aQualifiedName;
        if(tempTagName == null || tempTagName.length() == 0) {
            throw new PlantyException("Error when parsing document: Neither a local name nor qualified of an element specified");
        }
        // This code added to determine root element in a rational way bf
        if (rootElementName == null)
        	rootElementName = tempTagName;
        
        Element tempElement = document.createElement(tempTagName);
        
        stillOpenElements.push(tempElement);
        
        super.startElement(aUri, aLocalName, aQualifiedName, anAttributes);
    }


    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    public void endElement(String aUri, String aLocalName, String aQualifiedName)
        throws SAXException {

			if (ExternalToolsPlugin.getDefault() != null && ExternalToolsPlugin.getDefault().isDebugging()) {
			ExternalToolsPlugin.getDefault().log("PlantySaxDefaultHandler.endElement(" +aUri+ ", " +aLocalName+ ", "+aQualifiedName+ ")", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}

        if(parsingFinished) {
            return;
        }        
        
        // Checks wether we know the parent for sure
        boolean tempParentKnown = checkForParentElement();
        
        String tempTagName = aLocalName.length() > 0 ? aLocalName : aQualifiedName;

		if(!stillOpenElements.isEmpty()) {        
	        Element tempLastStillOpenElement = (Element)stillOpenElements.peek(); 
	        if(tempLastStillOpenElement != null && tempLastStillOpenElement.getTagName().equals(tempTagName)) {
	            stillOpenElements.pop();
	            
	            if(!stillOpenElements.empty()) {
	                Element tempSecondLastStillOpenElement = (Element)stillOpenElements.peek();
	                tempSecondLastStillOpenElement.appendChild(tempLastStillOpenElement);
	            }
	            if(tempParentKnown && parentElement != null && parentElement.getTagName().equals(tempTagName)) {
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
        super.setDocumentLocator(aLocator);
    }


    /**
     * Returns the parent element that has been determined during a prior 
     * parsing.
     * <P>
     * It is quite common that parsing stopped before the current cursor 
     * position. That happens when the parser finds an error within the parsed 
     * document before. In that case the parent element might be guessed to be
     * the one that opened last. To tell the handler wether the parent should
     * be guessed, <code>aGuessParentFlag</code> may be specified.
     * 
     * @param aGuessParentFlag wether the parent should be guessed
     * @return the parent element or <code>null</code> if not known.
     */
    public Element getParentElement(boolean aGuessParentFlag) {
        if(parentElement != null) {
            return parentElement;
        }
        if(aGuessParentFlag) {
            if(!stillOpenElements.empty()) {
                return (Element)stillOpenElements.peek();
            }
        }
        return null;
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

}