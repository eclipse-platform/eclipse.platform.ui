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
 * 	   IBM Corporation - bug 24108
 *******************************************************************************/

package org.eclipse.ant.ui.internal.editor;

import java.io.File;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.ant.ui.internal.model.AntUIPlugin;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * SAX Parsing Handler that determines the enclosing target task element in
 * respect to a specified cursor position.
 * 
 * @author Alf Schiefelbein
 */
public class EnclosingTargetSearchingHandler extends AntEditorSaxDefaultHandler {

	/**
	 * Whether the enclosing target element has been determined.
	 * <P>
	 * This is the case if the method 
	 * <code>determineEnclosingTargetTaskElement</code> has been called once.
	 */
	protected boolean enclosingTargetElementDetermined;

    /**
     * Creates an EnclosingTargetSearchingHandler, with the specified parameters.
     * 
     * @param aRowOfCursorPosition the startingRow where the cursor is located in the
     * document. The first startingRow is refered to with an index of '0'.
     * @param aColumnOfCursorPosition the startingColumn where the cursor is located in
     * the document. The first startingColumn is refered to with an index of '0'.
     */
    public EnclosingTargetSearchingHandler(File mainFileContainer, int aRowOfCursorPosition, int aColumnOfCursorPosition) throws ParserConfigurationException {
    	super(mainFileContainer, aRowOfCursorPosition, aColumnOfCursorPosition);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    public void endElement(String aUri, String aLocalName, String aQualifiedName)
        throws SAXException {

		if (AntUIPlugin.getDefault() != null && AntUIPlugin.getDefault().isDebugging()) {
        	AntUIPlugin.log("AntEditorSaxDefaultHandler.endElement(" +aUri+ ", " +aLocalName+ ", "+aQualifiedName+ ")", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}

        if(parsingFinished) {
            return;
        }        
        
        // Checks wether we know the parent for sure
        boolean tempParentKnown = checkForParentElement();
        
        String tempTagName = aLocalName.length() > 0 ? aLocalName : aQualifiedName;

		if(tempTagName.equals("target")) { //$NON-NLS-1$
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
        
		 if (AntUIPlugin.getDefault() != null && AntUIPlugin.getDefault().isDebugging()) {
			AntUIPlugin.log("AntEditorSaxDefaultHandler.startElement(" +aUri+ ", " +aLocalName+ ", "+aQualifiedName+ ", "+anAttributes+ ")", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        }
        if(parsingFinished) {
            return;
        }        

        // Checks wether we know the parent for sure
        checkForParentElement();

        // Create a Dom Element
        String tempTagName = aLocalName.length() > 0 ? aLocalName : aQualifiedName;
        if(tempTagName == null || tempTagName.length() == 0) {
            throw new AntEditorException(AntEditorMessages.getString("EnclosingTargetSearchingHandler.Error_parsing")); //$NON-NLS-1$
        }
		
		if(tempTagName.equals("target")) { //$NON-NLS-1$
	        Element tempElement = document.createElement(tempTagName);
	        String tempTargetName = anAttributes.getValue("name"); //$NON-NLS-1$
	        if(tempTargetName != null && tempTargetName.length() > 0) {
		        tempElement.setAttribute("name", tempTargetName); //$NON-NLS-1$
	        }
	        stillOpenElements.push(tempElement);
		}
    }

    /**
     * Checks whether the enclosing task element, that we are searching for can
     * be or has already been determined.
     * <P>
     * This will be done by comparing the current parsing position with the
     * cursor position. If we just passed the cursor position and the enclosing
     * task element has not been set yet, it will be set.
     * 
     * @return <code>true</code> if the enclosing task element is known 
     * otherwise <code>false</code>
     */
    protected boolean checkForParentElement() {
        if(parentElement == null && !enclosingTargetElementDetermined) {
            if(locator != null) {

                /*
                 * The locator's numbers are 1-based though, we do everything
                 * 0-based.
                 */

                int tempLineNr = locator.getLineNumber() -1;
                int tempColumnNr = locator.getColumnNumber() -1;
                if(tempLineNr> rowOfCursorPosition ||
                    (tempLineNr == rowOfCursorPosition && tempColumnNr > columnOfCursorPosition)) {
                        determineEnclosingTargetTaskElement();
	                    return true;
                    }
            }
            return false;
        }
        
        // Parent element has been set already before
        return true;
    }

	
	/**
	 * Determines the enclosing target task element.
	 * <P>
	 * This Method shall only be called if the parser has passed the cursor 
	 * position allready.
	 */
    protected void determineEnclosingTargetTaskElement() {
        while(parentElement == null && !stillOpenElements.empty()) {
            Element tempElement = (Element)stillOpenElements.pop();
            if(tempElement.getTagName().equals("target")) { //$NON-NLS-1$
                parentElement = tempElement;
				if (AntUIPlugin.getDefault() != null && AntUIPlugin.getDefault().isDebugging()) {
					AntUIPlugin.log("EnclosingTargetSearchingHandler.checkForParentElement(): Enclosing target element found: " +parentElement, null); //$NON-NLS-1$
                }
            }
        }
    	enclosingTargetElementDetermined = true;
    }
    
    /**
     * Returns the enclosing target task element that has been determined 
     * during a prior parsing.
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
        if(enclosingTargetElementDetermined) {
            return parentElement;
        }
        if(aGuessParentFlag) {
            if(!stillOpenElements.empty()) {
            	determineEnclosingTargetTaskElement();
                return parentElement;
            }
        }
        return null;
    }
}
