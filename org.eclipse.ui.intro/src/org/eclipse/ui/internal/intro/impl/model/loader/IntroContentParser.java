/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.intro.impl.model.loader;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.ui.internal.intro.impl.IIntroConstants;
import org.eclipse.ui.internal.intro.impl.model.util.BundleUtil;
import org.eclipse.ui.internal.intro.impl.util.Log;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * 
 */
public class IntroContentParser {

    private static String TAG_INTRO_CONTENT = "introContent"; //$NON-NLS-1$
    private static String TAG_HTML = "html"; //$NON-NLS-1$
    protected static String XHTML1_TRANSITIONAL = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"; //$NON-NLS-1$
    protected static String XHTML1_STRICT = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"; //$NON-NLS-1$
    protected static String XHTML1_FRAMESET = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd"; //$NON-NLS-1$


    /*
     * Load XHTML dtds from intro plugin location.
     */
    protected static Hashtable dtdMap = new Hashtable();

    static {
        String dtdBaseLocation = "dtds/xhtml1-20020801/"; //$NON-NLS-1$

        String dtdLocation = dtdBaseLocation + "xhtml1-transitional.dtd"; //$NON-NLS-1$
        URL dtdURL_T = BundleUtil.getResourceAsURL(dtdLocation,
            IIntroConstants.PLUGIN_ID);
        dtdMap.put(XHTML1_TRANSITIONAL, dtdURL_T);

        dtdLocation = dtdBaseLocation + "xhtml1-strict.dtd"; //$NON-NLS-1$
        URL dtdURL_S = BundleUtil.getResourceAsURL(dtdLocation,
            IIntroConstants.PLUGIN_ID);
        dtdMap.put(XHTML1_STRICT, dtdURL_S);

        dtdLocation = dtdBaseLocation + "xhtml1-frameset.dtd"; //$NON-NLS-1$
        URL dtdURL_F = BundleUtil.getResourceAsURL(dtdLocation,
            IIntroConstants.PLUGIN_ID);
        dtdMap.put(XHTML1_FRAMESET, dtdURL_F);
    }



    private Document document;
    private boolean hasXHTMLContent;

    /**
     * Creates a config parser assuming that the passed content represents a URL
     * to the content file.
     */
    public IntroContentParser(String content) {
        try {
            document = parse(content);
            if (document != null) {
                // xml file is loaded. It can be either XHTML or intro XML.
                Element rootElement = document.getDocumentElement();
                // DocumentType docType = document.getDoctype();
                if (rootElement.getTagName().equals(TAG_INTRO_CONTENT)) {
                    // intro xml file.
                    hasXHTMLContent = false;
                } else if (rootElement.getTagName().equals(TAG_HTML)) {
                    // rely on root element to detect if we have an XHTML file
                    // and not on doctype. We need to support xhtml files with
                    // no doctype.
                    hasXHTMLContent = true;
                } else
                    // not intro XML nor XHTML.
                    document = null;
            }
        } catch (Exception e) {
            Log.error("Could not load Intro content file: " + content, e); //$NON-NLS-1$
        }
    }


    private Document parse(String fileURI) {
        Document document = null;
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory
                .newInstance();
            docFactory.setValidating(false);
            // if this is not set, Document.getElementsByTagNameNS() will fail.
            docFactory.setNamespaceAware(true);
            docFactory.setExpandEntityReferences(false);
            DocumentBuilder parser = docFactory.newDocumentBuilder();

            parser.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(String publicId,
                        String systemId) throws SAXException, IOException {

                    if (systemId.equals(XHTML1_TRANSITIONAL)
                            || systemId.equals(XHTML1_STRICT)
                            || systemId.equals(XHTML1_FRAMESET)) {
                        // InputStream in = new StringBufferInputStream(
                        // "<?xml version=\"1.0\" encoding=\"utf-8\"?>");
                        // return new InputSource(in);

                        // be carefull here to support running as a jarred
                        // plugin.
                        URL dtdURL = (URL) dtdMap.get(systemId);
                        InputSource in = new InputSource(dtdURL.openStream());
                        in.setSystemId(dtdURL.toExternalForm());
                        return in;
                    }
                    return null;
                }
            });

            document = parser.parse(fileURI);
            return document;

        } catch (SAXParseException spe) {
            StringBuffer buffer = new StringBuffer("IntroParser error in line "); //$NON-NLS-1$
            buffer.append(spe.getLineNumber());
            buffer.append(", uri "); //$NON-NLS-1$
            buffer.append(spe.getSystemId());
            buffer.append("\n"); //$NON-NLS-1$   
            buffer.append(spe.getMessage());

            // Use the contained exception.
            Exception x = spe;
            if (spe.getException() != null)
                x = spe.getException();
            Log.error(buffer.toString(), x);

        } catch (SAXException sxe) {
            Exception x = sxe;
            if (sxe.getException() != null)
                x = sxe.getException();
            Log.error(x.getMessage(), x);

        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            Log.error(pce.getMessage(), pce);

        } catch (IOException ioe) {
            Log.error(ioe.getMessage(), ioe);
        }
        return null;
    }


    /**
     * Returned the DOM representing the intro xml content file. May return null
     * if parsing the file failed.
     * 
     * @return Returns the document.
     */
    public Document getDocument() {
        return document;
    }

    public boolean hasXHTMLContent() {
        return hasXHTMLContent;
    }


    public static String convertToString(Document document) {
        try {
            // identity xslt.
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();

            DOMSource source = new DOMSource(document);

            StringWriter stringBuffer = new StringWriter();
            StreamResult result = new StreamResult(stringBuffer);

            // setup properties, for doctype.
            DocumentType docType = document.getDoctype();
            if (docType != null) {
                String value = docType.getSystemId();
                // transformer.clearParameters();
                transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, value);
                value = document.getDoctype().getPublicId();
                transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, value);
                transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                    "yes"); //$NON-NLS-1$
                // transformer.setOutputProperty(OutputKeys.MEDIA_TYPE,
                // "text/html");
                // transformer
                // .setOutputProperty(OutputKeys.ENCODING, "iso-8859-1");
            } else
                Log
                    .warning("XHTML file used to display this Intro page does not have a Document type defined. " //$NON-NLS-1$
                            + "XHTML requires document types to be defined."); //$NON-NLS-1$

            transformer.transform(source, result);
            return stringBuffer.toString();

        } catch (TransformerConfigurationException tce) {
            // Error generated by the parser
            Log.error("Transformer Config error: " + tce.getMessage(), null); //$NON-NLS-1$
            // Use the contained exception, if any
            Throwable x = tce;
            if (tce.getException() != null)
                x = tce.getException();
            Log.error("Transformer Stack trace: ", x); //$NON-NLS-1$

        } catch (TransformerException te) {
            // Error generated by the parser
            Log.error("Transformer error: " + te.getMessage(), te); //$NON-NLS-1$
            // Use the contained exception, if any
            Throwable x = te;
            if (te.getException() != null)
                x = te.getException();
            Log.error("Transformer Stack trace: ", x); //$NON-NLS-1$

        }
        return null;

    }



}
