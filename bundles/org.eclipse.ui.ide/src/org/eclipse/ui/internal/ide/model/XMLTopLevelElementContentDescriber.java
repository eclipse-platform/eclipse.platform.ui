/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.ide.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentDescriber;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A content describer for detecting the name of the top-level element of the
 * DTD system identifier in an XML file.  This supports two 
 * 
 * @since 3.0
 */
public final class XMLTopLevelElementContentDescriber extends DefaultHandler
		implements
			IContentDescriber, IExecutableExtension, LexicalHandler {
    
    /**
     * An exception indicating that the parsing should stop. This is usually
     * triggered when the top-level element has been found.
     * 
     * @since 3.0
     */
    private class StopParsingException extends SAXException {

        /**
         * Constructs an instance of <code>StopParsingException</code> with a
         * <code>null</code> detail message.
         */
        public StopParsingException() {
            super((String) null);
        }
    }

    /**
     * The name of the executable extension parameter containing the value of
     * <code>dtdToFind</code>.
     */

    public static final String DTD_TO_FIND = "dtd"; //$NON-NLS-1$

    /**
     * The name of the executable extension parameter containing the value of
     * <code>elementToFind</code>.
     */
    public static final String ELEMENT_TO_FIND = "element"; //$NON-NLS-1$

    /**
     * The system identifier for the DTD that was found while parsing the XML.
     * This member variable is <code>null</code> unless the file has been
     * parsed successful to the point of finding the DTD's system identifier.
     */
    private String dtdFound = null;

    /**
     * The system identifier that we wish to find. This value will be
     * initialized by the <code>setInitializationData</code> method. If no
     * value is provided, then this means that we don't care what the system
     * identifier will be.
     */
    private String dtdToFind = null;

    /**
     * The top-level element we are looking for. This value will be initialized
     * by the <code>setInitializationData</code> method. If no value is
     * provided, then this means that we don't care what the top-level element
     * will be.
     */
    private String elementToFind = null;

    /**
     * This is the name of the top-level element found in the XML file. This
     * member variable is <code>null</code> unless the file has been parsed
     * successful to the point of finding the top-level element.
     */
    private String topLevelElementName = null;

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ext.LexicalHandler#comment(char[], int, int)
     */
    public final void comment(final char[] ch, final int start, final int length) {
        // Not interested.
    }
    
    /**
     * Creates a new SAX parser for use within this instance.
     * 
     * @return The newly created parser.
     * 
     * @throws ParserConfigurationException
     *             If a parser of the given configuration cannot be created.
     * @throws SAXException
     *             If something in general goes wrong when creating the parser.
     * @throws SAXNotRecognizedException
     *             If the <code>XMLReader</code> does not recognize the
     *             lexical handler configuration option.
     * @throws SAXNotSupportedException
     *             If the <code>XMLReader</code> does not support the lexical
     *             handler configuration option.
     */
    private final SAXParser createParser() throws ParserConfigurationException,
            SAXException, SAXNotRecognizedException, SAXNotSupportedException {
		// Initialize the factory.
		final SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		parserFactory.setNamespaceAware(true);
		try {
			parserFactory.setFeature(
					"http://xml.org/sax/features/string-interning", true); //$NON-NLS-1$
            parserFactory.setValidating(false);
        } catch (final SAXException e) {
            // This is not a critical error. We can keep going....
            IDEWorkbenchPlugin.log("Problem initializing parser", //$NON-NLS-1$
                    new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH,
                            IStatus.ERROR, "Problem initializing parser", e)); //$NON-NLS-1$
        }
		
		// Initialize the parser.
		final SAXParser parser = parserFactory.newSAXParser();
		final XMLReader reader = parser.getXMLReader();
		reader.setProperty("http://xml.org/sax/properties/lexical-handler", //$NON-NLS-1$
				this);
		
		return parser;
	}
	
	/*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.content.IContentDescriber#describe(java.io.InputStream,
     *      org.eclipse.core.runtime.content.IContentDescription, int)
     */
    public final int describe(InputStream contents,
            IContentDescription description, int optionsMask)
            throws IOException {
	    
	    // I don't know how to provide anything but custom properties.
	    if ((optionsMask & IContentDescription.CUSTOM_PROPERTIES) == 0) {
	        return INDETERMINATE;
	    }
	    
	    // Parse the file into we have what we need (or an error occurs).
		try {
			final SAXParser parser = createParser();
			parser.parse(contents, this);
			
        } catch (final ParserConfigurationException e) {
            IDEWorkbenchPlugin.log("Problem parsing file", new Status( //$NON-NLS-1$
                    IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH,
                    IStatus.ERROR, "Problem parsing file", e)); //$NON-NLS-1$
            return INDETERMINATE;
            
        } catch (final StopParsingException e) {
            // Abort the parsing normally.  Fall through....
            
        } catch (final SAXException e) {
            IDEWorkbenchPlugin.log("Problem parsing file", new Status( //$NON-NLS-1$
                    IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH,
                    IStatus.ERROR, "Problem parsing file", e)); //$NON-NLS-1$
            return INDETERMINATE;
            
		}
		
        // Check to see if we matched our criteria.
        if ((elementToFind != null)
                && (!elementToFind.equals(topLevelElementName))) { return INVALID; }
        if ((dtdToFind != null) && (!dtdToFind.equals(dtdFound))) { return INVALID; }

        // We must be okay then.
        return VALID;
	}

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ext.LexicalHandler#endCDATA()
     */
    public final void endCDATA() {
        // Not interested.
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ext.LexicalHandler#endDTD()
     */
    public final void endDTD() {
        // Not interested.
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
     */
    public final void endEntity(final String name) {
        // Not interested.
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.content.IContentDescriber#getSupportedOptions()
     */
    public final int getSupportedOptions() {
        return IContentDescription.CUSTOM_PROPERTIES;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement,
     *      java.lang.String, java.lang.Object)
     */
    public final void setInitializationData(final IConfigurationElement config,
            final String propertyName, final Object data) {
        
        if (data instanceof String) {
            dtdToFind = (String) data;
        } else if (data instanceof Hashtable) {
            Hashtable parameters = (Hashtable) data; 
            dtdToFind = (String) parameters.get(DTD_TO_FIND);
            elementToFind = (String) parameters.get(ELEMENT_TO_FIND);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ext.LexicalHandler#startCDATA()
     */
    public final void startCDATA() {
        // Not interested.
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ext.LexicalHandler#startDTD(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public final void startDTD(final String name, final String publicId,
            final String systemId) throws SAXException {

        dtdFound = systemId;
        
        // If we don't care about the top-level element, we can stop here.
        if (elementToFind == null) {
            throw new StopParsingException();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public final void startElement(final String uri, final String elementName,
            final String qualifiedName, final Attributes attributes)
            throws SAXException {

        topLevelElementName = elementName;
        throw new StopParsingException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
     */
    public final void startEntity(final String name) {
        // Not interested.
    }
}