/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring;

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
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.ltk.core.refactoring.IRefactoringCoreStatusCodes;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringSessionDescriptor;

import org.eclipse.ltk.internal.core.refactoring.history.RefactoringContributionManager;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Refactoring session reader for XML-based refactoring sessions.
 * 
 * @since 3.2
 */
public final class RefactoringSessionReader extends DefaultHandler {

	/** The comment of the refactoring session, or <code>null</code> */
	private String fComment= null;

	/** Should project information be included? */
	private final boolean fProjects;

	/**
	 * The current list of refactoring descriptors, or <code>null</code>
	 * (element type: <code>RefactoringDescriptor</code>)
	 */
	private List fRefactoringDescriptors= null;

	/**
	 * List of exceptions occurred while creating the descriptors 
	 */
	private List fDescriptorStatus= null;
	
	/** Has a session been found during parsing? */
	private boolean fSessionFound= false;

	/** The current version of the refactoring script, or <code>null</code> */
	private String fVersion= null;

	/**
	 * Creates a new refactoring session reader.
	 * 
	 * @param projects
	 *            <code>true</code> to include project information,
	 *            <code>false</code> otherwise
	 */
	public RefactoringSessionReader(final boolean projects) {
		fProjects= projects;
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
	 * Reads a refactoring history descriptor from the specified input object.
	 * 
	 * @param source
	 *            the input source
	 * @return a corresponding refactoring history descriptor, or
	 *         <code>null</code>
	 * @throws CoreException
	 *             if an error occurs while reading form the input source
	 */
	public RefactoringSessionDescriptor readSession(final InputSource source) throws CoreException {
		fSessionFound= false;
		fDescriptorStatus= new ArrayList();
		try {
			source.setSystemId("/"); //$NON-NLS-1$
			createParser(SAXParserFactory.newInstance()).parse(source, this);
			if (fDescriptorStatus.size() != 0)
				throw new CoreException(new MultiStatus(RefactoringCorePlugin.getPluginId(), IRefactoringCoreStatusCodes.REFACTORING_HISTORY_FORMAT_ERROR, (IStatus[]) fDescriptorStatus.toArray(new IStatus[fDescriptorStatus.size()]), RefactoringCoreMessages.RefactoringSessionReader_invalid_values_in_xml, null));
			if (!fSessionFound)
				throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), IRefactoringCoreStatusCodes.REFACTORING_HISTORY_FORMAT_ERROR, RefactoringCoreMessages.RefactoringSessionReader_no_session, null));
			if (fRefactoringDescriptors != null) {
				if (fVersion == null || "".equals(fVersion)) //$NON-NLS-1$
					throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), IRefactoringCoreStatusCodes.MISSING_REFACTORING_HISTORY_VERSION, RefactoringCoreMessages.RefactoringSessionReader_missing_version_information, null));
				if (!IRefactoringSerializationConstants.CURRENT_VERSION.equals(fVersion))
					throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), IRefactoringCoreStatusCodes.UNSUPPORTED_REFACTORING_HISTORY_VERSION, RefactoringCoreMessages.RefactoringSessionReader_unsupported_version_information, null));
				return new RefactoringSessionDescriptor((RefactoringDescriptor[]) fRefactoringDescriptors.toArray(new RefactoringDescriptor[fRefactoringDescriptors.size()]), fVersion, fComment);
			}
		} catch (IOException exception) {
			throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), IRefactoringCoreStatusCodes.REFACTORING_HISTORY_IO_ERROR, exception.getLocalizedMessage(), null));
		} catch (ParserConfigurationException exception) {
			throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), IRefactoringCoreStatusCodes.REFACTORING_HISTORY_IO_ERROR, exception.getLocalizedMessage(), null));
		} catch (SAXException exception) {
			throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), IRefactoringCoreStatusCodes.REFACTORING_HISTORY_IO_ERROR, exception.getLocalizedMessage(), null));
		} finally {
			fRefactoringDescriptors= null;
			fVersion= null;
			fComment= null;
			fDescriptorStatus= null;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void startElement(final String uri, final String localName, final String qualifiedName, final Attributes attributes) throws SAXException {
		if (IRefactoringSerializationConstants.ELEMENT_REFACTORING.equals(qualifiedName)) {
			final int length= attributes.getLength();
			final Map map= new HashMap(length);
			String id= ""; //$NON-NLS-1$
			String stamp= ""; //$NON-NLS-1$
			String description= ""; //$NON-NLS-1$
			String comment= null;
			String flags= "0"; //$NON-NLS-1$
			String project= null;
			for (int index= 0; index < length; index++) {
				final String name= attributes.getQName(index);
				final String value= attributes.getValue(index);
				if (IRefactoringSerializationConstants.ATTRIBUTE_ID.equals(name))
					id= value;
				else if (IRefactoringSerializationConstants.ATTRIBUTE_STAMP.equals(name))
					stamp= value;
				else if (IRefactoringSerializationConstants.ATTRIBUTE_DESCRIPTION.equals(name))
					description= value;
				else if (IRefactoringSerializationConstants.ATTRIBUTE_FLAGS.equals(name))
					flags= value;
				else if (IRefactoringSerializationConstants.ATTRIBUTE_COMMENT.equals(name)) {
					if (!"".equals(value)) //$NON-NLS-1$
						comment= value;
				} else if (fProjects && IRefactoringSerializationConstants.ATTRIBUTE_PROJECT.equals(name))
					project= value;
				else if (!"".equals(name)) //$NON-NLS-1$
					map.put(name, value);
			}
			int flag= 0;
			try {
				flag= Integer.parseInt(flags);
			} catch (NumberFormatException exception) {
				// Do nothing
			}

			RefactoringDescriptor descriptor= null;
			try {
				descriptor= RefactoringContributionManager.getInstance().createDescriptor(id, project, description, comment, map, flag);
			} catch (RuntimeException e) {
				fDescriptorStatus.add(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), e.getLocalizedMessage(), e));
			}
			if (descriptor != null) {
				try {
					descriptor.setTimeStamp(Long.valueOf(stamp).longValue());
				} catch (NumberFormatException exception) {
					// Do nothing
				}
				if (fRefactoringDescriptors == null)
					fRefactoringDescriptors= new ArrayList();
				fRefactoringDescriptors.add(descriptor);
			}
		} else if (IRefactoringSerializationConstants.ELEMENT_SESSION.equals(qualifiedName)) {
			fSessionFound= true;
			final String version= attributes.getValue(IRefactoringSerializationConstants.ATTRIBUTE_VERSION);
			if (version != null && !"".equals(version)) //$NON-NLS-1$
				fVersion= version;
			fComment= attributes.getValue(IRefactoringSerializationConstants.ATTRIBUTE_COMMENT);
		}
	}
}
