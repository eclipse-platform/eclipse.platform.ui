/*******************************************************************************
 * Copyright (c) 2001, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.properties.tabbed;

/**
 * Represents a section descriptor provider for tabbed property sections.
 * 
 * @author Anthony Hunter
 */
public interface ISectionDescriptorProvider {

	/**
	 * Returns all section descriptors for the contributor.
	 * @return all section descriptors.
	 */
	public ISectionDescriptor[] getSectionDescriptors();
}
