/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.model;

import java.io.File;
import java.io.StringReader;

import javax.xml.parsers.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IResourceActionFilter;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/*
 * This PropertyParser is used to examine XML files in
 * an effort to learn a bit more about their context.
 * This will then help Eclipse determine, for example,
 * what menu entries are really appropriate for this 
 * particular XML file.  As an example, a plugin.xml file
 * should not have context menu entries relating to
 * running Ant scripts.
 * 
 * Currently (Eclipse 3.0) this parser only looks for
 * the root level tag and the name of the DTD (if any).
 * This parser may be extended to look for other, relevant
 * pieces of information.  The information found is stored
 * as a persistent property on this resource.  This way,
 * we only need to parse this file again if the file
 * has changed.  If no changes have occurred, we simply
 * access the persistent property and do not parse.
 * 
 * This parser should be aware of the environment in
 * which it is being called in that it needs to be
 * called a minimun number of times (we don't want to 
 * reparse every XML file each time someone asks for
 * a context menu), it needs to do minimal computing/parsing,
 * and it needs to handle any unexpected or error
 * results cleanly.
 * 
 * Current restrictions in XML parsing logic require that
 * if a DTD is mentioned in and XML file, that DTD file
 * must be present and accessible.  The PropertyParser does
 * not require this to be true, but the underlying XML
 * parser does require this.
 */

public class PropertyParser extends DefaultHandler implements LexicalHandler {
	/**
	 * @deprecated
	 * Use the extension point
	 * org.eclipse.core.runtime.contentTypes instead.  This internal class
	 * will be removed in 3.1.
	 */

	// model parser
	private static SAXParser parser;
	private static SAXParserFactory parserFactory;
	
	// The XML file/resource being parsed
	private IResource parseResource = null;
	int x = initializeParser();
	
	private int initializeParser() {
		try {
			parserFactory = SAXParserFactory.newInstance();
			parserFactory.setNamespaceAware(true);
			try {
				parserFactory.setFeature("http://xml.org/sax/features/string-interning", true); //$NON-NLS-1$
				parserFactory.setValidating(false);
			} catch (SAXException e) {
				IDEWorkbenchPlugin.log("Problem initializing parser", new Status(IStatus.ERROR,IDEWorkbenchPlugin.IDE_WORKBENCH, IStatus.ERROR, "Problem initializing parser", e)); //$NON-NLS-1$ //$NON-NLS-2$
			}
			parser = parserFactory.newSAXParser();
			XMLReader reader = parser.getXMLReader();
			reader.setProperty("http://xml.org/sax/properties/lexical-handler", this); //$NON-NLS-1$
		} catch (Exception e) {
			IDEWorkbenchPlugin.log("Problem initializing parser", new Status(IStatus.ERROR,IDEWorkbenchPlugin.IDE_WORKBENCH, IStatus.ERROR, "Problem initializing parser", e)); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return 1;
	}

	/*
	 * Cause parsing to happen on the specified resource.
	 * 
	 * @param resource the resource being parsed.  This 
	 *     resource is assumed to be a valid XML file.
	 */
	synchronized public void parseResource(IResource resource) throws Exception {
		if (resource == null)
			return;
		parseResource = resource;
		// Need to get a File version of this resource
		IPath location = resource.getLocation();
		if (location == null)
			return;
		File file = location.toFile();
		if (file.length() == 0L) {
			// Some SAX parsers will throw a SAXParseException for a
			// zero-length file.  We'll just decide there's nothing to
			// do and return gracefully.  First, set the last modification
			// time so we don't have to check this again unless the file
			// changes.
			long modTime = parseResource.getModificationStamp();
			QualifiedName modKey = new QualifiedName(IDEWorkbenchPlugin.IDE_WORKBENCH, WorkbenchResource.XML_LAST_MOD);
			try {
				parseResource.setPersistentProperty(modKey, new Long(modTime).toString());
			} catch (CoreException c) {
				IDEWorkbenchPlugin.log("Problem parsing element", c.getStatus()); //$NON-NLS-1$
			}
			return;
		}
		try {
			parser.parse(file, this);
		} catch (SAXException s) {
			// If the SAXException is the one we threw
			// to abort the parsing, just ignore it and
			// continue processing.
			if (!s.getMessage().equals("PropertyParser stop")) { //$NON-NLS-1$
				// We got a real error, so log it but
				// continue processing.
				IDEWorkbenchPlugin.log("Problem parsing file", new Status(IStatus.ERROR,IDEWorkbenchPlugin.IDE_WORKBENCH, IStatus.ERROR, "Problem parsing file", s)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 * 
	 */

	public void startElement(String uri, String elementName, String qName, Attributes attributes)
	throws SAXException {

		/* We have hit an XML element.  We are only concerned
		 * with the root level element.  Figure out what it
		 * is and store it as a persistent property.  Once
		 * we have the root level element stored, no further
		 * parsing of this file is required.  As a result,
		 * this method will then throw a SAXException (the
		 * recommended way of aborting parsing).
		 * 
		 * This method is not intended to be called.  It is
		 * only called from within the XML SAX parser being
		 * used.
		 */
		long modTime = parseResource.getModificationStamp();
		QualifiedName modKey = new QualifiedName(IDEWorkbenchPlugin.IDE_WORKBENCH, WorkbenchResource.XML_LAST_MOD);
		try {
			parseResource.setPersistentProperty(modKey, new Long(modTime).toString());
		} catch (CoreException c) {
			IDEWorkbenchPlugin.log("Problem parsing element", c.getStatus()); //$NON-NLS-1$
		}
		// We are only interested in the first element.
		QualifiedName key;
		String propertyName = IResourceActionFilter.XML_FIRST_TAG;
		key = new QualifiedName(IDEWorkbenchPlugin.IDE_WORKBENCH, propertyName);
		try {
			parseResource.setPersistentProperty(key, elementName);
		} catch (CoreException c) {
			IDEWorkbenchPlugin.log("Problem parsing element", c.getStatus()); //$NON-NLS-1$
		}
		// And now we wish to abort the parsing.  The only other thing
		// we looked for was the dtd name.  By definition, the dtd
		// declaration must occur before the first element.
		throw new SAXException("PropertyParser stop"); //$NON-NLS-1$
	}
	
	
	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#comment(char[], int, int)
	 */
	public void comment(char[] ch, int start, int length) throws SAXException {
		//No interesting behavior
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#endCDATA()
	 */
	public void endCDATA() throws SAXException {
		//No interesting behavior
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#endDTD()
	 */
	public void endDTD() throws SAXException {
		//No interesting behavior
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
	 */
	public void endEntity(String name) throws SAXException {
		//No interesting behavior
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#startCDATA()
	 */
	public void startCDATA() throws SAXException {
		//No interesting behavior
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#startDTD(java.lang.String, java.lang.String, java.lang.String)
	 * 
	 */
	public void startDTD(String name, String publicId, String systemId)
		throws SAXException {

		/* We have hit an DTD request for this XML file.
		 * The name of the DTD wanted for this XML file will
		 * be stored as a persistent property.
		 * 
		 * This method is not intended to be called.  It is
		 * only called from within the XML SAX parser being
		 * used.
		 */
		if (systemId == null)
			return;
		
		QualifiedName qname = new QualifiedName(IDEWorkbenchPlugin.IDE_WORKBENCH, IResourceActionFilter.XML_DTD_NAME);
		try {
			parseResource.setPersistentProperty(qname, systemId);
		} catch (CoreException c) {
			IDEWorkbenchPlugin.log("Problem parsing dtd element", c.getStatus()); //$NON-NLS-1$
		}
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
	 */
	public void startEntity(String name) throws SAXException {
		//No interesting behavior
	}

    /* (non-Javadoc)
     * Resolve external entity definitions to an empty string.  This is to speed
     * up processing of files with external DTDs.  Not resolving the contents of
     * the DTD is ok, as only the System ID of the DTD declaration is used.
     * 
     * @see org.xml.sax.helpers.DefaultHandler#resolveEntity(java.lang.String, java.lang.String)
     */
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException {
        return new InputSource(new StringReader("")); //$NON-NLS-1$
    }
}
