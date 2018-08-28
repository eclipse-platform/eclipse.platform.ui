/*******************************************************************************
 * Copyright (c) 2001, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
