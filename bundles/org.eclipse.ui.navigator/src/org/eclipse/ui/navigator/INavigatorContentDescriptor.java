/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;

import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * 
 * The descriptor provides a handle to a content extension. Information such as
 * the Id, the name, the priority, and whether the descriptor provides one or
 * more root elements is provided.
 * 
 * 
 * <p>
 * There is one {@link INavigatorContentExtension} for each content 
 * service. There is only one {@link INavigatorContentDescriptor}
 * for each extension.
 * </p>
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 *<p>
 * This interface is not intended to be implemented by clients.
 *</p>
 * @since 3.2
 * 
 */
public interface INavigatorContentDescriptor {

	/**
	 * Returns the navgiator content extension id
	 * 
	 * @return the navgiator content extension id
	 */
	public abstract String getId();

	/**
	 * Returns the name of this navigator extension
	 * 
	 * @return the name of this navigator extension
	 */
	public abstract String getName();

	/**
	 * Returns the priority of the navigator content extension.
	 * 
	 * @return the priority of the navigator content extension. Returns 0 (zero)
	 *         if no priority was specified.
	 */
	public abstract int getPriority();

	/**
	 * Returns whether the receiver is a root navigator content extension.
	 * Navigator content extensions are root extensions if they are referenced
	 * in a navigator view extension.
	 * 
	 * @return true if the receiver is a root navigator extension false if the
	 *         receiver is not a root navigator extension
	 */
	public abstract boolean isRoot();

	/**
	 * The enabledByDefault attribute specifies whether an extension should be
	 * activated in the context of a viewer automatically. Users may override
	 * this setting through the "Types of Content" dialog.
	 * 
	 * @return true if the extension is enabled by default.
	 */
	public abstract boolean isEnabledByDefault();

	/**
	 * 
	 * @return True if the loading of this configuration element has failed.
	 */
	public abstract boolean hasLoadingFailed();

	/**
	 * Determine if this content extension could provide the given element as a
	 * child.
	 * 
	 * <p>
	 * This method is used to determine what the parent of an element could be
	 * for Link with Editor support.
	 * </p>
	 * 
	 * @param anElement
	 *            The element that should be used for the evaluation.
	 * @return True if and only if the extension might provide an object of this
	 *         type as a child.
	 */
	public boolean isPossibleChild(Object anElement);

	/**
	 * Determine if this content extension is enabled for the given selection.
	 * The content extension is enabled for the selection if and only if it is
	 * enabled for each element in the selection.
	 * 
	 * @param aStructuredSelection
	 *            The selection from the viewer
	 * @return True if and only if the extension is enabled for each element in
	 *         the selection.
	 */
	public boolean isEnabledFor(IStructuredSelection aStructuredSelection);

	/**
	 * Determine if this content extension is enabled for the given element.
	 * 
	 * @param anElement
	 *            The element that should be used for the evaluation.
	 * @return True if and only if the extension is enabled for the element.
	 */
	public boolean isEnabledFor(Object anElement);

}