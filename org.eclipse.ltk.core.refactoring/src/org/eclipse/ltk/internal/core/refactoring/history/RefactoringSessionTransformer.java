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
 * Transformer to XML-based refactoring histories.
 * 
 * @since 3.2
 */
final class RefactoringSessionTransformer {

	/** The version value */
	private static final String VALUE_VERSION= "1.0"; //$NON-NLS-1$

	/** The current document, or <code>null</code> */
	private Document fDocument= null;

	/** The current refactoring node, or <code>null</code> */
	private Node fRefactoring= null;

	/** The current session node, or <code>null</code> */
	private Node fSession= null;

	/**
	 * Begins the tranformation of a refactoring specified by the given
	 * arguments.
	 * <p>
	 * Calls to
	 * {@link RefactoringSessionTransformer#beginRefactoring(String, long, String, String, String)}
	 * must be balanced with calls to
	 * {@link RefactoringSessionTransformer#endRefactoring()}. If the
	 * transformer is already processing a refactoring, nothing happens.
	 * </p>
	 * 
	 * @param id
	 *            the unique identifier of the refactoring
	 * @param stamp
	 *            the time stamp of the refactoring, or <code>-1</code>
	 * @param project
	 *            the non-empty name of the project this refactoring is
	 *            associated with, or <code>null</code>
	 * @param description
	 *            a human-readable description of the refactoring
	 * @param comment
	 *            the comment associated with the refactoring, or
	 *            <code>null</code>
	 * @throws CoreException
	 *             if an error occurs while creating a new refactoring
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
	 * Begins the transformation of a refactoring session.
	 * <p>
	 * Calls to {@link RefactoringSessionTransformer#beginSession(String)}
	 * must be balanced with calls to
	 * {@link RefactoringSessionTransformer#endSession()}. If the
	 * transformer is already processing a session, nothing happens.
	 * </p>
	 * 
	 * @param comment
	 *            the comment associated with the refactoring session, or
	 *            <code>null</code>
	 * @throws CoreException
	 *             if an error occurs while creating a new session
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
	 * Creates a refactoring argument with the specified name and value.
	 * <p>
	 * If no refactoring is currently processed, this call has no effect.
	 * </p>
	 * 
	 * @param name
	 *            the non-empty name of the argument
	 * @param value
	 *            the non-empty value of the argument
	 * 
	 * @throws CoreException
	 *             if an error occurs while creating a new argument
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
	 * Ends the tranformation of the current refactoring.
	 * <p>
	 * If no refactoring is currently processed, this call has no effect.
	 * </p>
	 */
	public void endRefactoring() {
		fRefactoring= null;
	}

	/**
	 * Ends the transformation of the current refactoring session.
	 * <p>
	 * If no refactoring session is currently processed, this call has no
	 * effect.
	 * </p>
	 */
	public void endSession() {
		fSession= null;
	}

	/**
	 * Returns the result of the transformation process.
	 * <p>
	 * This method must only be called once during the life time of a
	 * transformer.
	 * </p>
	 * 
	 * @return the object representing the refactoring session, or
	 *         <code>null</code> if no session has been transformed
	 */
	public Object getResult() {
		final Document document= fDocument;
		fDocument= null;
		return document;
	}
}