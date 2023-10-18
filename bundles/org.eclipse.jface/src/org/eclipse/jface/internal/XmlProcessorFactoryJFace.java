/*******************************************************************************
 *  Copyright (c) 2023 Joerg Kubitz and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jface.internal;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * XML processing which prohibits external entities.
 *
 * @see <a href="https://rules.sonarsource.com/java/RSPEC-2755/">RSPEC-2755</a>
 */
/* A copy of org.eclipse.core.internal.runtime.XmlProcessorFactory for jface. */
public class XmlProcessorFactoryJFace {
	private XmlProcessorFactoryJFace() {
		// static Utility only
	}

	private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY_ERROR_ON_DOCTYPE = createDocumentBuilderFactoryWithErrorOnDOCTYPE();

	/**
	 * Creates DocumentBuilderFactory which throws SAXParseException when detecting
	 * external entities. It's magnitudes faster to call
	 * {@link #createDocumentBuilderWithErrorOnDOCTYPE()}.
	 *
	 * @return javax.xml.parsers.DocumentBuilderFactory
	 */
	public static synchronized DocumentBuilderFactory createDocumentBuilderFactoryWithErrorOnDOCTYPE() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// completely disable DOCTYPE declaration:
		try {
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); //$NON-NLS-1$
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return factory;
	}

	/**
	 * Creates DocumentBuilder which throws SAXParseException when detecting
	 * external entities. The builder is not thread safe.
	 *
	 * @return javax.xml.parsers.DocumentBuilder
	 * @throws ParserConfigurationException
	 */
	public static DocumentBuilder createDocumentBuilderWithErrorOnDOCTYPE() throws ParserConfigurationException {
		return DOCUMENT_BUILDER_FACTORY_ERROR_ON_DOCTYPE.newDocumentBuilder();
	}

}