/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.intro.impl.model.loader;

import java.io.*;

import javax.xml.parsers.*;

import org.eclipse.ui.internal.intro.impl.util.*;
import org.w3c.dom.*;
import org.xml.sax.*;


/**
 *  
 */
public class IntroContentParser {

    private static String TAG_INTRO_CONTENT = "introContent"; //$NON-NLS-1$

    private Document document;

    /**
     * Creates a config parser assuming that the passed content represents a URL
     * to the content file.
     */
    public IntroContentParser(String content) {
        try {
            document = parse(content);
            if (document != null) {
                // xml file is loaded. validate that we have the correct root
                // element name.
                Element rootElement = document.getDocumentElement();
                if (!rootElement.getTagName().equals(TAG_INTRO_CONTENT)) {
                    document = null;
                    String msg = "Intro content file has incorrect parent tag: " //$NON-NLS-1$
                            + content;
                    Log.warning(msg);
                }
            }
        } catch (Exception e) {
            Log.error("Could not load Intro content file: " + content, e); //$NON-NLS-1$
        }
    }


    private Document parse(String fileURI) {
        Document document = null;
        try {
            DocumentBuilder parser = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
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
}