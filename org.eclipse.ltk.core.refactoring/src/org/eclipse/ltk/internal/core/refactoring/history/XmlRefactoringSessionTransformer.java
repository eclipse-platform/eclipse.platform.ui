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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.ltk.internal.core.refactoring.Assert;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * XML-based implementation of
 * {@link org.eclipse.ltk.internal.core.refactoring.history.IRefactoringSessionTransformer}.
 * 
 * @since 3.2
 */
public final class XmlRefactoringSessionTransformer implements IRefactoringSessionTransformer {

	/** The version value */
	private static final String VALUE_VERSION= "1.0"; //$NON-NLS-1$

	/** The current document, or <code>null</code> */
	private Document fDocument= null;

	/** The current refactoring node, or <code>null</code> */
	private Node fRefactoring= null;

	/** The current session node, or <code>null</code> */
	private Node fSession= null;

	/**
	 * {@inheritDoc}
	 */
	public void beginRefactoring(final String id, long stamp, final String project, final String description, final String comment) throws CoreException {
		Assert.isNotNull(id);
		Assert.isNotNull(description);
		try {
			if (fDocument == null)
				fDocument= DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException exception) {
			throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 0, exception.getLocalizedMessage(), null));
		} catch (FactoryConfigurationError exception) {
			throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 0, exception.getLocalizedMessage(), null));
		}
		if (fRefactoring == null) {
			try {
				fRefactoring= fDocument.createElement(IRefactoringSerializationConstants.ELEMENT_REFACTORING);
				final NamedNodeMap attributes= fRefactoring.getAttributes();
				Attr attribute= fDocument.createAttribute(IRefactoringSerializationConstants.ATTRIBUTE_ID);
				attribute.setValue(id);
				attributes.setNamedItem(attribute);
				if (stamp >= 0) {
					attribute= fDocument.createAttribute(IRefactoringSerializationConstants.ATTRIBUTE_STAMP);
					attribute.setValue(new Long(stamp).toString());
					attributes.setNamedItem(attribute);
				}
				attribute= fDocument.createAttribute(IRefactoringSerializationConstants.ATTRIBUTE_DESCRIPTION);
				attribute.setValue(description);
				attributes.setNamedItem(attribute);
				if (comment != null) {
					attribute= fDocument.createAttribute(IRefactoringSerializationConstants.ATTRIBUTE_COMMENT);
					attribute.setValue(comment);
					attributes.setNamedItem(attribute);
				}
				if (project != null) {
					attribute= fDocument.createAttribute(IRefactoringSerializationConstants.ATTRIBUTE_PROJECT);
					attribute.setValue(project);
					attributes.setNamedItem(attribute);
				}
				if (fSession == null)
					fDocument.appendChild(fRefactoring);
				else
					fSession.appendChild(fRefactoring);
			} catch (DOMException exception) {
				throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 0, exception.getLocalizedMessage(), null));
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void beginSession(final String comment) throws CoreException {
		if (fDocument == null) {
			try {
				fDocument= DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				fSession= fDocument.createElement(IRefactoringSerializationConstants.ELEMENT_SESSION);
				Attr attribute= fDocument.createAttribute(IRefactoringSerializationConstants.ATTRIBUTE_VERSION);
				attribute.setValue(VALUE_VERSION);
				fSession.getAttributes().setNamedItem(attribute);
				if (comment != null) {
					attribute= fDocument.createAttribute(IRefactoringSerializationConstants.ATTRIBUTE_COMMENT);
					attribute.setValue(comment);
					fSession.getAttributes().setNamedItem(attribute);
				}
				fDocument.appendChild(fSession);
			} catch (DOMException exception) {
				throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 0, exception.getLocalizedMessage(), null));
			} catch (ParserConfigurationException exception) {
				throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 0, exception.getLocalizedMessage(), null));
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void createArgument(final String name, final String value) throws CoreException {
		Assert.isNotNull(name);
		Assert.isTrue(!"".equals(name)); //$NON-NLS-1$
		Assert.isNotNull(value);
		Assert.isTrue(!"".equals(value)); //$NON-NLS-1$
		if (fDocument != null && fRefactoring != null && value != null) {
			try {
				final Attr attribute= fDocument.createAttribute(name);
				attribute.setValue(value);
				fRefactoring.getAttributes().setNamedItem(attribute);
			} catch (DOMException exception) {
				throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), 0, exception.getLocalizedMessage(), null));
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void endRefactoring() {
		fRefactoring= null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void endSession() {
		fSession= null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getResult() {
		final Document document= fDocument;
		fDocument= null;
		return document;
	}
}
