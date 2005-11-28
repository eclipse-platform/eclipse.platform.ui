/***************************************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/

package org.eclipse.help.internal.xhtml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.help.internal.HelpPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;


/**
 * Handles all XSL transforms applied to XHTML UA content.
 */
public class UATransformManager {


	private static Transformer createTransformer(Document document) {
		try {
			// identity xslt.
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();

			// setup properties, for doctype.
			DocumentType docType = document.getDoctype();
			if (docType != null) {
				String value = docType.getSystemId();
				transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, value);
				value = document.getDoctype().getPublicId();
				transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, value);
				transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); //$NON-NLS-1$

			} else
				;

			return transformer;

		} catch (TransformerConfigurationException tce) {
			HelpPlugin.logError("Transformer Config error: " + tce.getMessage(), null); //$NON-NLS-1$

			Throwable x = tce;
			if (tce.getException() != null)
				x = tce.getException();
			HelpPlugin.logError("Transformer Stack trace: ", x); //$NON-NLS-1$
		}
		return null;
	}

	public static String convertToString(Document document) {
		try {
			Transformer transformer = createTransformer(document);
			DOMSource source = new DOMSource(document);
			StringWriter stringBuffer = new StringWriter();
			StreamResult result = new StreamResult(stringBuffer);

			transformer.transform(source, result);
			return stringBuffer.toString();

		} catch (TransformerException te) {
			HelpPlugin.logError("Transformer error: " + te.getMessage(), te); //$NON-NLS-1$
			Throwable x = te;
			if (te.getException() != null)
				x = te.getException();
			HelpPlugin.logError("Transformer Stack trace: ", x); //$NON-NLS-1$
		}
		return null;

	}

	public static InputStream getAsInputStream(Document document) {
		byte[] ba = null;
		String xhtml = convertToString(document);
		try {
			ba = xhtml.getBytes("UTF-8");
		} catch (UnsupportedEncodingException ue) {
			;
		}
		ByteArrayInputStream is = new ByteArrayInputStream(ba);
		return is;
	}


}
