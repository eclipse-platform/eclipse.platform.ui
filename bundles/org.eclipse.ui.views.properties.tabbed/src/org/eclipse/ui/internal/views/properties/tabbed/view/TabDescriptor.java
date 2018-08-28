/*******************************************************************************
 * Copyright (c) 2001, 2016 IBM Corporation and others.
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
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 489250
 *******************************************************************************/
package org.eclipse.ui.internal.views.properties.tabbed.view;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.views.properties.tabbed.TabbedPropertyViewStatusCodes;
import org.eclipse.ui.internal.views.properties.tabbed.l10n.TabbedPropertyMessages;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.properties.tabbed.AbstractTabDescriptor;
import org.eclipse.ui.views.properties.tabbed.ISectionDescriptor;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.ibm.icu.text.MessageFormat;

/**
 * Represents the default implementation of a tab descriptor on the tabbed
 * property tabs extensions.
 *
 * @author Anthony Hunter
 */
public class TabDescriptor extends AbstractTabDescriptor {

	private static final String ATT_ID = "id"; //$NON-NLS-1$

	private static final String ATT_LABEL = "label"; //$NON-NLS-1$

	private static final String ATT_IMAGE = "image"; //$NON-NLS-1$

	private static final String ATT_INDENTED = "indented"; //$NON-NLS-1$

	private static final String ATT_CATEGORY = "category"; //$NON-NLS-1$

	private static final String ATT_AFTER_TAB = "afterTab"; //$NON-NLS-1$

	private static final String TAB_ERROR = TabbedPropertyMessages.TabDescriptor_Tab_error;

	private String id;

	private String label;

	private Image image;

	private boolean selected;

	private boolean indented;

	private String category;

	private String afterTab;

	/**
	 * Constructor for TabDescriptor.
	 *
	 * @param configurationElement
	 *            the configuration element for the tab descriptor.
	 */
	public TabDescriptor(IConfigurationElement configurationElement) {
		super();
		if (configurationElement != null) {
			id = configurationElement.getAttribute(ATT_ID);
			label = configurationElement.getAttribute(ATT_LABEL);
			String imageString = configurationElement.getAttribute(ATT_IMAGE);
			if (imageString != null) {
				image = AbstractUIPlugin.imageDescriptorFromPlugin(
						configurationElement.getDeclaringExtension()
								.getContributor().getName(), imageString)
						.createImage();
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
		selected = false;
	}

	/**
	 * Get the unique identifier for the tab.
	 *
	 * @return the unique identifier for the tab.
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * Get the text label for the tab.
	 *
	 * @return the text label for the tab.
	 */
	@Override
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
	@Override
	public String getAfterTab() {
		if (afterTab == null) {
			return super.getAfterTab();
		}
		return afterTab;
	}

	/**
	 * Get the category this tab belongs to.
	 *
	 * @return Get the category this tab belongs to.
	 */
	@Override
	public String getCategory() {
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

		getSectionDescriptors().add(target);
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
			getSectionDescriptors().add(0, target);
			return true;
		}
		for (int i = 0; i < getSectionDescriptors().size(); i++) {
			ISectionDescriptor descriptor = (ISectionDescriptor) getSectionDescriptors()
					.get(i);
			if (target.getAfterSection().equals(descriptor.getId())) {
				getSectionDescriptors().add(i + 1, target);
				return true;
			} else if (descriptor.getAfterSection().equals(target.getId())) {
				getSectionDescriptors().add(i, target);
				return true;
			}
		}
		return false;
	}

	@Override
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
				.getContributor().getName();
		String message = MessageFormat.format(TAB_ERROR, pluginId);
		IStatus status = new Status(IStatus.ERROR, pluginId,
				TabbedPropertyViewStatusCodes.TAB_ERROR, message, exception);
		Bundle bundle = FrameworkUtil.getBundle(TabDescriptor.class);
		Platform.getLog(bundle).log(status);
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
	 * @param selected
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
	@Override
	public Image getImage() {
		return image;
	}

	/**
	 * Determine if the tab is selected.
	 *
	 * @return <code>true</code> if the tab is selected.
	 */
	@Override
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Determine if the tab should be displayed as indented.
	 *
	 * @return <code>true</code> if the tab should be displayed as indented.
	 */
	@Override
	public boolean isIndented() {
		return indented;
	}

	/**
	 * Get the text label for the tab.
	 *
	 * @return the text label for the tab.
	 */
	@Override
	public String getText() {
		return label;
	}

	/**
	 * Disposes this descriptor.
	 *
	 * @since 3.7
	 */
	public void dispose() {
		if (image != null) {
			image.dispose();
			image = null;
		}
	}
}
