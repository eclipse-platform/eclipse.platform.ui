/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.dynamic;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/*
 * Processes XML input streams by converting to a DOM, calling all the handlers,
 * then converting back into an XML input stream.
 */
public class XMLProcessor {

	private DOMProcessor processor;
	private DOMReader reader;
	private DOMWriter writer;
	
	/*
	 * Creates the processor, which will use the given handlers.
	 */
	public XMLProcessor(DOMProcessorHandler[] handlers) {
		processor = new DOMProcessor(handlers);
	}
	
	/*
	 * Processes the given input stream with the supplied document id,
	 * and returns a new processed input stream.
	 */
	public InputStream process(InputStream in, String id) throws IOException, SAXException, ParserConfigurationException, TransformerException, TransformerConfigurationException {
		if (reader == null) {
			reader = new DOMReader();
		}
		Document document = reader.read(in);
		processor.process(document.getDocumentElement(), id);
		if (writer == null) {
			writer = new DOMWriter();
		}
		return writer.write(document);
	}
}
