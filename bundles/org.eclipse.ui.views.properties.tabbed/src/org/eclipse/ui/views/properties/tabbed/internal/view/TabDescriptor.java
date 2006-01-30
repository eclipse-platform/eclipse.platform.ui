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
package org.eclipse.ui.views.properties.tabbed.internal.view;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.properties.tabbed.ITabItem;
import org.eclipse.ui.views.properties.tabbed.ISection;
import org.eclipse.ui.views.properties.tabbed.ISectionDescriptor;
import org.eclipse.ui.views.properties.tabbed.internal.TabbedPropertyViewPlugin;
import org.eclipse.ui.views.properties.tabbed.internal.TabbedPropertyViewStatusCodes;
import org.eclipse.ui.views.properties.tabbed.internal.l10n.TabbedPropertyMessages;

/**
 * Represents the default implementation of a tab descriptor on the tabbed
 * property tabs extensions.
 * 
 * @author Anthony Hunter
 */
public class TabDescriptor
	implements Cloneable, ITabItem {

	private static final String ATT_ID = "id"; //$NON-NLS-1$

	private static final String ATT_LABEL = "label"; //$NON-NLS-1$

	private static final String ATT_IMAGE = "image"; //$NON-NLS-1$

	private static final String ATT_INDENTED = "indented"; //$NON-NLS-1$

	private static final String ATT_CATEGORY = "category"; //$NON-NLS-1$

	private static final String ATT_AFTER_TAB = "afterTab"; //$NON-NLS-1$

	private static final String TOP = "top"; //$NON-NLS-1$

	private final static String TAB_ERROR = TabbedPropertyMessages.TabDescriptor_Tab_error;

	private String id;

	private String label;

	private Image image;

	private boolean selected;

	private boolean indented;

	private String category;

	private String afterTab;

	private List sectionDescriptors;

	/**
	 * Constructor for TabDescriptor.
	 * 
	 * @param configurationElement
	 *            the configuration element for the tab descriptor.
	 */
	public TabDescriptor(IConfigurationElement configurationElement) {
		if (configurationElement != null) {
			id = configurationElement.getAttribute(ATT_ID);
			label = configurationElement.getAttribute(ATT_LABEL);
			String imageString = configurationElement.getAttribute(ATT_IMAGE);
			if (imageString != null) {
				image = AbstractUIPlugin
					.imageDescriptorFromPlugin(
						configurationElement.getDeclaringExtension()
							.getNamespace(), imageString).createImage();
			}
			String indentedString = configurationElement
				.getAttribute(ATT_INDENTED);
			indented = indentedString != null && indentedString.equals("true"); //$NON-NLS-1$
			category = configurationElement.getAttribute(ATT_CATEGORY);
			afterTab = configurationElement.getAttribute(ATT_AFTER_TAB);
			if (id == null || label == null || category == null) {
				// the tab id, label and category are mandatory - log error
				handleTabError(configurationElement, null);
			}
		}
		if (getAfterTab() == null) {
			afterTab = TOP;
		}
		sectionDescriptors = new ArrayList(5);
		selected = false;
	}

	/**
	 * Get the unique identifier for the tab.
	 * 
	 * @return the unique identifier for the tab.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Get the text label for the tab.
	 * 
	 * @return the text label for the tab.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Get the identifier of the tab after which this tab should be displayed.
	 * When two or more tabs belong to the same category, they are sorted by the
	 * after tab values.
	 * 
	 * @return the identifier of the tab.
	 */
	protected String getAfterTab() {
		return afterTab;
	}

	/**
	 * Get the category this tab belongs to.
	 * 
	 * @return Get the category this tab belongs to.
	 */
	protected String getCategory() {
		return category;
	}

	/**
	 * Returns whether the given section was added to this tab. The section can
	 * be appended if its tab attribute matches the tab id. The afterSection
	 * attribute indicates the order in which the section should be appended.
	 * 
	 * @param target
	 *            the section descriptor to append.
	 */
	protected boolean append(ISectionDescriptor target) {
		if (!target.getTargetTab().equals(id)) {
			return false;
		}

		if (insertSectionDescriptor(target)) {
			return true;
		}

		sectionDescriptors.add(target);
		return true;
	}

	/**
	 * Insert the section descriptor into the section descriptor list.
	 * 
	 * @param target
	 *            the section descriptor to insert.
	 * @return <code>true</code> if the target descriptor was added to the
	 *         descriptors list.
	 */
	private boolean insertSectionDescriptor(ISectionDescriptor target) {
		if (target.getAfterSection().equals(TOP)) {
			sectionDescriptors.add(0, target);
			return true;
		}
		for (int i = 0; i < sectionDescriptors.size(); i++) {
			ISectionDescriptor descriptor = (ISectionDescriptor) sectionDescriptors
				.get(i);
			if (target.getAfterSection().equals(descriptor.getId())) {
				sectionDescriptors.add(i + 1, target);
				return true;
			} else {
				if (descriptor.getAfterSection().equals(target.getId())) {
					sectionDescriptors.add(i, target);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Instantiate this tab's sections.
	 */
	public Tab createTab() {
		List sections = new ArrayList(sectionDescriptors.size());
		for (Iterator iter = sectionDescriptors.iterator(); iter.hasNext();) {
			ISectionDescriptor descriptor = (ISectionDescriptor) iter.next();
			ISection section = descriptor.getSectionClass();
			sections.add(section);
		}
		Tab tab = new Tab();
		tab.setSections((ISection[]) sections.toArray(new ISection[sections
			.size()]));
		return tab;
	}

	/**
	 * Get the list of section descriptors for the tab.
	 * 
	 * @return the list of section descriptors for the tab.
	 */
	protected List getSectionDescriptors() {
		return sectionDescriptors;
	}

	/**
	 * Set the list of section descriptors for the tab.
	 * 
	 * @param sectionDescriptors
	 *            the list of section descriptors for the tab.
	 */
	protected void setSectionDescriptors(List sectionDescriptors) {
		this.sectionDescriptors = sectionDescriptors;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getId();
	}

	/**
	 * Handle the tab error when an issue is found loading from the
	 * configuration element.
	 * 
	 * @param configurationElement
	 *            the configuration element
	 * @param exception
	 *            an optional CoreException
	 */
	private void handleTabError(IConfigurationElement configurationElement,
			CoreException exception) {
		String pluginId = configurationElement.getDeclaringExtension()
			.getNamespace();
		String message = MessageFormat.format(TAB_ERROR,
			new Object[] {pluginId});
		IStatus status = new Status(IStatus.ERROR, pluginId,
			TabbedPropertyViewStatusCodes.TAB_ERROR, message, exception);
		TabbedPropertyViewPlugin.getPlugin().getLog().log(status);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object object) {
		if (this == object)
			return true;

		if (this.getClass() == object.getClass()) {
			TabDescriptor descriptor = (TabDescriptor) object;
			if (this.getCategory().equals(descriptor.getCategory())
				&& this.getId().equals(descriptor.getId())
				&& this.getSectionDescriptors().size() == descriptor
					.getSectionDescriptors().size()) {

				Iterator i = this.getSectionDescriptors().iterator();
				Iterator j = descriptor.getSectionDescriptors().iterator();

				// the order is importent here - so as long as the sizes of the
				// lists are the same and id of the section at the same
				// positions are the same - the lists are the same
				while (i.hasNext()) {
					ISectionDescriptor source = (ISectionDescriptor) i.next();
					ISectionDescriptor target = (ISectionDescriptor) j.next();
					if (!source.getId().equals(target.getId()))
						return false;
				}

				return true;
			}

		}

		return false;
	}

	/**
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

	/**
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

	/**
	 * Set the image for the tab.
	 * 
	 * @param image
	 *            the image for the tab.
	 */
	protected void setImage(Image image) {
		this.image = image;
	}

	/**
	 * Set the indicator to determine if the tab should be displayed as
	 * indented.
	 * 
	 * @param indented
	 *            <code>true</code> if the tab should be displayed as
	 *            indented.
	 */
	protected void setIndented(boolean indented) {
		this.indented = indented;
	}

	/**
	 * Set the indicator to determine if the tab should be the selected tab in
	 * the list.
	 * 
	 * @param indented
	 *            <code>true</code> if the tab should be the selected tab in
	 *            the list.
	 */
	protected void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * Set the text label for the tab.
	 * 
	 * @param label
	 *            the text label for the tab.
	 */
	protected void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Get the image for the tab.
	 * 
	 * @return the image for the tab.
	 */
	public Image getImage() {
		return image;
	}

	/**
	 * Determine if the tab is selected.
	 * 
	 * @return <code>true</code> if the tab is selected.
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Determine if the tab should be displayed as indented.
	 * 
	 * @return <code>true</code> if the tab should be displayed as indented.
	 */
	public boolean isIndented() {
		return indented;
	}

	/**
	 * Get the text label for the tab.
	 * 
	 * @return the text label for the tab.
	 */
	public String getText() {
		return label;
	}
}
