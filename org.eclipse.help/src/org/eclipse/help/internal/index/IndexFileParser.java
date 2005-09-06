/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.index;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.toc.HrefUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author sturmash
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class IndexFileParser extends DefaultHandler {

    private IndexBuilder builder;

    private IndexFile indexFile;

    private static SAXParserFactory parserFactory = SAXParserFactory
            .newInstance();

    private static XMLParserPool parserPool = new XMLParserPool();
    
    /**
     * @param builder
     */
    public IndexFileParser(IndexBuilder builder) {
        this.builder = builder;
    }

    /**
     * @param file
     */
    public void parse(IndexFile file) {
        this.indexFile = file;
        InputStream istream = indexFile.getInputStream();
        if (istream == null) return;
        InputSource isource = new InputSource(istream);
        String filePath = "/" + file.getPluginID() + "/" + file.getHref(); //$NON-NLS-1$ //$NON-NLS-2$
        isource.setSystemId(filePath);
        try {
            SAXParser parser = parserPool.obtainParser();
            try {
                parser.parse(isource, this);
                istream.close();
            } finally {
                parserPool.releaseParser(parser);
            }
		} catch (ParserConfigurationException pce) {
			HelpPlugin.logError(
					"SAXParser implementation could not be loaded.", pce); //$NON-NLS-1$
		} catch (SAXException se) {
			HelpPlugin.logError("Error loading Index file " + file //$NON-NLS-1$
					+ ".", se); //$NON-NLS-1$
		} catch (IOException ioe) {
			HelpPlugin.logError("Error loading Index file " + file //$NON-NLS-1$
					+ ".", ioe); //$NON-NLS-1$
		}
    }

    /**
     * This class maintain pool of parsers that can be used for parsing TOC
     * files. The parsers should be returned to the pool for reuse.
     */
    static class XMLParserPool {

        private ArrayList pool = new ArrayList();

        SAXParser obtainParser() throws ParserConfigurationException,
                SAXException {
            SAXParser p;
            int free = pool.size();
            if (free > 0) {
                p = (SAXParser) pool.remove(free - 1);
            } else {
                p = parserFactory.newSAXParser();
            }
            return p;
        }

        void releaseParser(SAXParser parser) {
            pool.add(parser);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (qName.equals("entry")) { //$NON-NLS-1$
            builder.exitIndexEntry();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        if (qName.equals("entry")) { //$NON-NLS-1$
            builder.addIndexEntry(attributes.getValue("keyword")); //$NON-NLS-1$
        } else if (qName.equals("topic")) { //$NON-NLS-1$
			builder.addTopic( attributes.getValue("title"),  //$NON-NLS-1$
					          HrefUtil.normalizeHref(indexFile.getPluginID(), attributes.getValue("href")), //$NON-NLS-1$
					          attributes.getValue("location")); //$NON-NLS-1$
        } else {
            return;
        }
    }
     
}
