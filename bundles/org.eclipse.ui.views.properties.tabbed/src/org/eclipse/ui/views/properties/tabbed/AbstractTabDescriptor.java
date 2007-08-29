/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.views.properties.tabbed;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.views.properties.tabbed.TabbedPropertyViewPlugin;

/**
 * An abstract implementation of a tab descriptor for the tabbed property view.
 * 
 * @author Anthony Hunter
 * @since 3.4
 */
public abstract class AbstractTabDescriptor implements ITabDescriptor,
		Cloneable {

	private List sectionDescriptors;

	/**
	 * Constructor for AbstractTabDescriptor.
	 */
	public AbstractTabDescriptor() {
		super();
		sectionDescriptors = new ArrayList(5);
	}

	/*
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException exception) {
			IStatus status = new Status(IStatus.ERROR, TabbedPropertyViewPlugin
					.getPlugin().getBundle().getSymbolicName(), 666, exception
					.getMessage(), exception);
			TabbedPropertyViewPlugin.getPlugin().getLog().log(status);
		}
		return null;
	}

	/*
	 * @see org.eclipse.ui.views.properties.tabbed.ITabDescriptor#createTab()
	 */
	public TabContents createTab() {
		List sections = new ArrayList(getSectionDescriptors().size());
		for (Iterator iter = getSectionDescriptors().iterator(); iter.hasNext();) {
			ISectionDescriptor descriptor = (ISectionDescriptor) iter.next();
			ISection section = descriptor.getSectionClass();
			sections.add(section);
		}
		TabContents tab = new TabContents();
		tab.setSections((ISection[]) sections.toArray(new ISection[sections
				.size()]));
		return tab;
	}

	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (this.getClass() == object.getClass()) {
			AbstractTabDescriptor descriptor = (AbstractTabDescriptor) object;
			if (this.getCategory().equals(descriptor.getCategory()) &&
					this.getId().equals(descriptor.getId()) &&
					this.getSectionDescriptors().size() == descriptor
							.getSectionDescriptors().size()) {

				Iterator i = this.getSectionDescriptors().iterator();
				Iterator j = descriptor.getSectionDescriptors().iterator();

				// the order is important here - so as long as the sizes of the
				// lists are the same and id of the section at the same
				// positions are the same - the lists are the same
				while (i.hasNext()) {
					ISectionDescriptor source = (ISectionDescriptor) i.next();
					ISectionDescriptor target = (ISectionDescriptor) j.next();
					if (!source.getId().equals(target.getId())) {
						return false;
					}
				}

				return true;
			}

		}

		return false;
	}

	/*
	 * @see org.eclipse.ui.views.properties.tabbed.ITabDescriptor#getAfterTab()
	 */
	public String getAfterTab() {
		return TOP;
	}

	/*
	 * @see org.eclipse.ui.views.properties.tabbed.ITabItem#getImage()
	 */
	public Image getImage() {
		return null;
	}

	/**
	 * Get the list of section descriptors for the tab.
	 * 
	 * @return the list of section descriptors for the tab.
	 */
	public List getSectionDescriptors() {
		return sectionDescriptors;
	}

	/*
	 * @see org.eclipse.ui.views.properties.tabbed.ITabItem#getText()
	 */
	public String getText() {
		return getLabel();
	}

	/*
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {

		int hashCode = getCategory().hashCode();
		hashCode ^= getId().hashCode();
		Iterator i = this.getSectionDescriptors().iterator();
		while (i.hasNext()) {
			ISectionDescriptor section = (ISectionDescriptor) i.next();
			hashCode ^= section.getId().hashCode();
		}
		return hashCode;
	}

	/*
	 * @see org.eclipse.ui.views.properties.tabbed.ITabItem#isIndented()
	 */
	public boolean isIndented() {
		return false;
	}

	/*
	 * @see org.eclipse.ui.views.properties.tabbed.ITabItem#isSelected()
	 */
	public boolean isSelected() {
		return false;
	}

	/**
	 * Set the list of section descriptors for the tab.
	 * 
	 * @param sectionDescriptors
	 *            the list of section descriptors for the tab.
	 */
	public void setSectionDescriptors(List sectionDescriptors) {
		this.sectionDescriptors = sectionDescriptors;
	}
}