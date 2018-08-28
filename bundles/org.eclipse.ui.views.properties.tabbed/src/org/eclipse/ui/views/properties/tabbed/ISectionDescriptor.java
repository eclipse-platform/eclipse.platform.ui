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

import java.util.List;

import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Represents a section descriptor on the tabbed property sections extensions.
 * <p>
 * This interface should not be extended or implemented. New instances should be
 * created using <code>AbstractSectionDescriptor</code>.
 * </p>
 *
 * @author Anthony Hunter
 */
public interface ISectionDescriptor {

	/**
	 * If enabledFor is not specified in the descriptor, enable for any
	 * selection.
	 */
	public final int ENABLES_FOR_ANY = -1;

	/**
	 * If afterSection is not specified in the descriptor, we default to be the
	 * top section.
	 * @since 3.4
	 */
	public static final String TOP = "top"; //$NON-NLS-1$

	/**
	 * Get the unique identifier for the section.
	 *
	 * @return the unique identifier for the section.
	 */
	public String getId();

	/**
	 * Get the section filter instance for this section. Return null if no
	 * filter for this section.
	 *
	 * @return the section filter instance.
	 */
	public IFilter getFilter();

	/**
	 * Get the valid input types for this section.
	 *
	 * @return the valid input types for this section.
	 */
	public List getInputTypes();

	/**
	 * Get the class that implements this section.
	 *
	 * @return the class that implements this section.
	 */
	public ISection getSectionClass();

	/**
	 * Get the target tab where this section will appear.
	 *
	 * @return the target tab where this section will appear.
	 */
	public String getTargetTab();

	/**
	 * Get the value for section enablement which is a precise number of items
	 * selected. For example: enablesFor="1" enables the section only when one
	 * item is selected. If not specified, enable for any selection and return
	 * ENABLES_FOR_ANY.
	 *
	 * @return the value for section enablement.
	 */
	public int getEnablesFor();

	/**
	 * Determines if this section applies to the selection.
	 *
	 * @param part
	 *            the current workbench part.
	 * @param selection
	 *            the selection.
	 * @return <code>true</code> if this section applies to the current
	 *         selection.
	 */
	public boolean appliesTo(IWorkbenchPart part, ISelection selection);

	/**
	 * Get the section identifier below which this section is displayed.
	 * <code>"top"</code> is returned if this section is the top most section
	 * or if there is one section in the tab.
	 *
	 * @return the section identifier.
	 */
	public String getAfterSection();
}
