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

package org.eclipse.ant.internal.ui.editor;

import java.io.File;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jface.text.IDocument;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;

/**
 * SAX Parsing Handler that determines the enclosing target task element in
 * respect to a specified cursor position.
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
     * @param cursorRow the startingRow where the cursor is located in the
     * document. The first startingRow is refered to with an index of '0'.
     * @param cursorColumn the startingColumn where the cursor is located in
     * the document. The first startingColumn is refered to with an index of '0'.
     */
    public EnclosingTargetSearchingHandler(IDocument document, File mainFileContainer, int cursorRow, int cursorColumn) throws ParserConfigurationException {
    	super(document, mainFileContainer, cursorRow, cursorColumn);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    public void endElement(String aUri, String aLocalName, String aQualifiedName) {

		if(parsingFinished) {
            return;
        }        
        
        // Checks whether we know the parent for sure
        boolean parentKnown = checkForParentElement();
        
        String tagName = aLocalName.length() > 0 ? aLocalName : aQualifiedName;

		if(tagName.equals("target")) { //$NON-NLS-1$
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
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
     */
    public void startElement(String aUri, String aLocalName, String aQualifiedName, Attributes anAttributes) {
       
        if(parsingFinished) {
            return;
        }        

        // Checks wether we know the parent for sure
        checkForParentElement();

        //While the crimson parser passes the tag name as local name, apache's
        //xerces parser, passes the tag name as qualilfied name and an empty 
        // string as local name.
        String tagName = aLocalName.length() > 0 ? aLocalName : aQualifiedName;
        if(tagName == null || tagName.length() == 0) {
            throw new AntEditorException(AntEditorMessages.getString("EnclosingTargetSearchingHandler.Error_parsing")); //$NON-NLS-1$
        }
		
		if(tagName.equals("target")) { //$NON-NLS-1$
	        Element element = document.createElement(tagName);
	        String targetName = anAttributes.getValue("name"); //$NON-NLS-1$
	        if(targetName != null && targetName.length() > 0) {
		        element.setAttribute("name", targetName); //$NON-NLS-1$
	        }
	        stillOpenElements.push(element);
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
                 //The locator's numbers are 1-based though, we do everything
                 //0-based.
                int lineNr = locator.getLineNumber() -1;
                int columnNr = locator.getColumnNumber() -1;
                if(lineNr> rowOfCursorPosition ||
                    (lineNr == rowOfCursorPosition && columnNr > columnOfCursorPosition)) {
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
	 * This method shall only be called if the parser has passed the cursor 
	 * position already.
	 */
    protected void determineEnclosingTargetTaskElement() {
        while(parentElement == null && !stillOpenElements.empty()) {
            Element stillOpen = (Element)stillOpenElements.pop();
            if(stillOpen.getTagName().equals("target")) { //$NON-NLS-1$
                parentElement = stillOpen;
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
     * be guessed, <code>guessParent</code> may be specified.
     * 
     * @param guessParent whether the parent should be guessed
     * @return the parent element or <code>null</code> if not known.
     */
    public Element getParentElement(boolean guessParent) {
        if(enclosingTargetElementDetermined) {
            return parentElement;
        }
        if(guessParent) {
            if(!stillOpenElements.empty()) {
            	determineEnclosingTargetTaskElement();
                return parentElement;
            }
        }
        return null;
    }
}
