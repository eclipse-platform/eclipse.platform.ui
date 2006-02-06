/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;

import java.util.Set;

import org.eclipse.ui.navigator.internal.extensions.OverridePolicy;


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
	String getId();

	/**
	 * Returns the name of this navigator extension
	 * 
	 * @return the name of this navigator extension
	 */
	String getName();

	/**
	 * Returns the priority of the navigator content extension.
	 * 
	 * @return the priority of the navigator content extension. Returns 0 (zero)
	 *         if no priority was specified.
	 */
	int getPriority();
 
	/**
	 * The enabledByDefault attribute specifies whether an extension should be
	 * activated in the context of a viewer automatically. Users may override
	 * this setting through the "Types of Content" dialog.
	 * 
	 * @return true if the extension is enabled by default.
	 */
	boolean isActiveByDefault(); 
 
	/**
	 * Determine if this content extension is enabled for the given element.
	 * 
	 * @param anElement
	 *            The element that should be used for the evaluation.
	 * @return True if and only if the extension is enabled for the element.
	 */
	boolean isTriggerPoint(Object anElement);
	
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
	boolean isPossibleChild(Object anElement);
	
	/**
	 * @return Returns the suppressedExtensionId or null if none specified.
	 */
	String getSuppressedExtensionId();

	/**
	 * @return Returns the overridePolicy or null if this extension does not
	 *         override another extension.
	 */
	OverridePolicy getOverridePolicy();
	
	/**
	 * @return The descriptor of the <code>suppressedExtensionId</code> if non-null.
	 */
	INavigatorContentDescriptor getOverriddenDescriptor();
	
	/**
	 * 
	 * Does not force the creation of the set of overriding extensions.
	 * 
	 * @return True if this extension has overridding extensions.
	 */
	boolean hasOverridingExtensions();
	

	/**
	 * @return The set of overridding extensions (of type
	 *         {@link INavigatorContentDescriptor}
	 */
	Set getOverriddingExtensions();
	

}