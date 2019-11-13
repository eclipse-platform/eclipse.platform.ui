/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.views.properties.tabbed;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyRegistryClassSectionFilter;

/**
 * An abstract implementation of a section descriptor for the tabbed property
 * view.
 *
 * @author Anthony Hunter
 * @since 3.4
 */
public abstract class AbstractSectionDescriptor implements ISectionDescriptor {

	private TabbedPropertyRegistryClassSectionFilter classFilter;

	/**
	 * Constructor for AbstractSectionDescriptor.
	 */
	public AbstractSectionDescriptor() {
		super();
		classFilter = new TabbedPropertyRegistryClassSectionFilter(null);
	}

	/**
	 * Constructor for AbstractSectionDescriptor.
	 *
	 * @param typeMapper
	 *            the type mapper for the section.
	 */
	public AbstractSectionDescriptor(ITypeMapper typeMapper) {
		super();
		classFilter = new TabbedPropertyRegistryClassSectionFilter(typeMapper);
	}

	@Override
	public boolean appliesTo(IWorkbenchPart part, ISelection selection) {
		return classFilter.appliesToSelection(this, selection);
	}

	@Override
	public String getAfterSection() {
		return TOP;
	}

	@Override
	public int getEnablesFor() {
		return ENABLES_FOR_ANY;
	}

	@Override
	public IFilter getFilter() {
		return null;
	}

	@Override
	public List getInputTypes() {
		return new ArrayList();
	}
}