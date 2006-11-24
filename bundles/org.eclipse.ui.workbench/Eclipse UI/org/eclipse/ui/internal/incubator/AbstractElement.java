/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.incubator;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @since 3.3
 * 
 */
public abstract class AbstractElement {

	private AbstractProvider provider;
	
	/**
	 * @param provider
	 */
	public AbstractElement(AbstractProvider provider) {
		super();
		this.provider = provider;
	}
	
	/**
	 * Returns the label to be displayed to the user.
	 * 
	 * @return the label
	 */
	public abstract String getLabel();

	/**
	 * Returns the image descriptor for this element.
	 * 
	 * @return an image descriptor, or null if no image is available
	 */
	public abstract ImageDescriptor getImageDescriptor();

	/**
	 * Returns the id for this element. The id has to be unique within the
	 * AbstractProvider that provided this element.
	 * 
	 * @return the id
	 */
	public abstract String getId();

	/**
	 * Executes the associated action for this element.
	 */
	public abstract void execute();

	/**
	 * Return the label to be used for sorting and matching elements.
	 * 
	 * @return the sort label
	 */
	public String getSortLabel() {
		return getLabel();
	}
	
	/**
	 * @return Returns the provider.
	 */
	public AbstractProvider getProvider() {
		return provider;
	}	
}
