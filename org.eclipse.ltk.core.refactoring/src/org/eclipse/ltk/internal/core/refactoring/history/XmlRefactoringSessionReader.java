/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring.history;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringSessionDescriptor;

import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * XML-based implementation of
 * {@link org.eclipse.ltk.internal.core.refactoring.history.IRefactoringSessionReader}.
 * <p>
 * The input object is exspected to be of type {@link org.xml.sax.InputSource}.
 * </p>
 * 
 * @since 3.2
 */
final class XmlRefactoringSessionReader extends DefaultHandler implements IRefactoringSessionReader {

	/** The comment of the refactoring session, or <code>null</code> */
	private String fComment= null;

	/**
	 * The current list of refactoring descriptors, or <code>null</code>
	 * (element type: <code>RefactoringDescriptor</code>)
	 */
	private List fRefactoringDescriptors= null;

	/** The current version of the refactoring script, or <code>null</code> */
	private String fVersion= null;

	/**
	 * Creates a core exception from the given throwable.
	 * 
	 * @param throwable
	 *            the throwable
	 * @return the core exception
	 */
	private CoreException createCoreException(final Throwable throwable) {
		return new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 0, throwable.getLocalizedMessage(), null));
	}

	/**
	 * Creates a new parser from the specified factory.
	 * 
	 * @param factory
	 *            the parser factoring to use
	 * @return the created parser
	 * @throws ParserConfigurationException
	 *             if no parser is available with the given configuration
	 * @throws SAXException
	 *             if an error occurs while creating the parser
	 */
	private SAXParser createParser(final SAXParserFactory factory) throws ParserConfigurationException, SAXException {

		final SAXParser parser= factory.newSAXParser();
		final XMLReader reader= parser.getXMLReader();

		try {

			reader.setFeature("http://xml.org/sax/features/validation", false); //$NON-NLS-1$
			reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); //$NON-NLS-1$

		} catch (SAXNotRecognizedException exception) {
			// Do nothing
		} catch (SAXNotSupportedException exception) {
			// Do nothing
		}
		return parser;
	}

	/**
	 * @inheritDoc
	 */
	public RefactoringSessionDescriptor readSession(final Object input) throws CoreException {
		if (input instanceof InputSource) {
			try {
				final InputSource source= (InputSource) input;
				source.setSystemId("/"); //$NON-NLS-1$
				createParser(SAXParserFactory.newInstance()).parse(source, this);
				if (fRefactoringDescriptors != null && fVersion != null) {
					final RefactoringSessionDescriptor descriptor= new RefactoringSessionDescriptor((RefactoringDescriptor[]) fRefactoringDescriptors.toArray(new RefactoringDescriptor[fRefactoringDescriptors.size()]), fVersion, fComment);
					fRefactoringDescriptors= null;
					fVersion= null;
					fComment= null;
					return descriptor;
				}
			} catch (IOException exception) {
				throw createCoreException(exception);
			} catch (ParserConfigurationException exception) {
				throw createCoreException(exception);
			} catch (SAXException exception) {
				throw createCoreException(exception);
			}
		}
		return null;
	}

	public void startElement(final String uri, final String localName, final String qualifiedName, final Attributes attributes) throws SAXException {
		if (XmlRefactoringSessionTransformer.ELEMENT_REFACTORING.equals(qualifiedName)) {
			final int length= attributes.getLength();
			final Map map= new HashMap(length);
			String id= ""; //$NON-NLS-1$
			String description= ""; //$NON-NLS-1$
			String comment= null;
			String project= null;
			for (int index= 0; index < length; index++) {
				final String name= attributes.getQName(index);
				final String value= attributes.getValue(index);
				if (XmlRefactoringSessionTransformer.ATTRIBUTE_ID.equals(name))
					id= value;
				else if (XmlRefactoringSessionTransformer.ATTRIBUTE_DESCRIPTION.equals(name))
					description= value;
				else if (XmlRefactoringSessionTransformer.ATTRIBUTE_COMMENT.equals(name))
					comment= value;
				else if (XmlRefactoringSessionTransformer.ATTRIBUTE_PROJECT.equals(name))
					project= value;
				else if (!"".equals(name) && !"".equals(value)) //$NON-NLS-1$//$NON-NLS-2$
					map.put(name, value);
			}
			if (fRefactoringDescriptors == null)
				fRefactoringDescriptors= new ArrayList();
			fRefactoringDescriptors.add(new RefactoringDescriptor(id, project, description, comment, map));
		} else if (XmlRefactoringSessionTransformer.ELEMENT_SESSION.equals(qualifiedName)) {
			final String version= attributes.getValue(XmlRefactoringSessionTransformer.ATTRIBUTE_VERSION);
			if (version != null && !"".equals(version)) //$NON-NLS-1$
				fVersion= version;
			fComment= attributes.getValue(XmlRefactoringSessionTransformer.ATTRIBUTE_COMMENT);
		}
	}
}
