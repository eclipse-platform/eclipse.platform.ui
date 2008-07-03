/*******************************************************************************
 * Copyright (c) 2001, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.views.properties.tabbed.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.ui.internal.views.properties.tabbed.TabbedPropertyViewPlugin;
import org.eclipse.ui.internal.views.properties.tabbed.TabbedPropertyViewStatusCodes;
import org.eclipse.ui.internal.views.properties.tabbed.l10n.TabbedPropertyMessages;
import org.eclipse.ui.views.properties.tabbed.AbstractSectionDescriptor;
import org.eclipse.ui.views.properties.tabbed.ISection;
import org.eclipse.ui.views.properties.tabbed.ITypeMapper;

import com.ibm.icu.text.MessageFormat;

/**
 * Represents the default implementation of a section descriptor on the tabbed
 * property sections extensions. This implementation assumes that we are
 * interested in selected objects in an IStructuredSelection.
 * 
 * @author Anthony Hunter
 */
public class SectionDescriptor extends AbstractSectionDescriptor {

	private static final String ATT_ID = "id"; //$NON-NLS-1$

	private static final String ATT_TARGET_TAB = "tab"; //$NON-NLS-1$

	private static final String ATT_AFTER_SECTION = "afterSection"; //$NON-NLS-1$

	private static final String ATT_CLASS = "class"; //$NON-NLS-1$

	private static final String ATT_SECTION_FILTER = "filter"; //$NON-NLS-1$	

	private static final String ATT_SECTION_ENABLES_FOR = "enablesFor"; //$NON-NLS-1$	

	private static final String ATT_INPUT_TYPE = "type"; //$NON-NLS-1$

	private static final String ELEMENT_INPUT = "input"; //$NON-NLS-1$

	private String id;

	private String targetTab;

	private String afterSection;

	private ArrayList inputTypes;

	private IFilter filter;

	private int enablesFor = ENABLES_FOR_ANY;

	private IConfigurationElement configurationElement;

	/**
	 * Constructor for the section descriptor.
	 * 
	 * @param configurationElement
	 *            the configuration element for the section descriptor.
	 * @param typeMapper
	 *            The type mapper.
	 */
	protected SectionDescriptor(IConfigurationElement configurationElement,
			ITypeMapper typeMapper) {
		super(typeMapper);
		this.configurationElement = configurationElement;

		id = getConfigurationElement().getAttribute(ATT_ID);
		targetTab = getConfigurationElement().getAttribute(ATT_TARGET_TAB);
		afterSection = getConfigurationElement()
				.getAttribute(ATT_AFTER_SECTION);
		if (getConfigurationElement().getAttribute(ATT_SECTION_ENABLES_FOR) != null) {
			String enablesForStr = getConfigurationElement().getAttribute(
					ATT_SECTION_ENABLES_FOR);
			int enablesForTest = Integer.parseInt(enablesForStr);
			if (enablesForTest > 0) {
				enablesFor = enablesForTest;
			}
		}

		if (id == null || targetTab == null) {
			// the section id and tab are mandatory - log error
			handleSectionError(null);
		}
	}

	/**
	 * Handle the section error when an issue is found loading from the
	 * configuration element.
	 * 
	 * @param exception
	 *            an optional CoreException
	 */
	private void handleSectionError(CoreException exception) {
		String pluginId = getConfigurationElement().getDeclaringExtension()
				.getNamespaceIdentifier();
		String message = TabbedPropertyMessages.SectionDescriptor_Section_error;
		if (exception == null) {
			message = MessageFormat.format(
					TabbedPropertyMessages.SectionDescriptor_Section_error,
					new Object[] { pluginId });
		} else {
			message = MessageFormat
					.format(
							TabbedPropertyMessages.SectionDescriptor_class_not_found_error,
							new Object[] { pluginId });
		}
		IStatus status = new Status(IStatus.ERROR, pluginId,
				TabbedPropertyViewStatusCodes.SECTION_ERROR, message, exception);
		TabbedPropertyViewPlugin.getPlugin().getLog().log(status);
	}

	/**
	 * @see org.eclipse.ui.views.properties.tabbed.ISectionDescriptor#getId()
	 */
	public String getId() {
		return id;
	}

	/**
	 * @see org.eclipse.ui.views.properties.tabbed.ISectionDescriptor#getFilter()
	 */
	public IFilter getFilter() {
		if (filter == null) {
			try {
				if (getConfigurationElement().getAttribute(ATT_SECTION_FILTER) != null) {
					filter = (IFilter) configurationElement
							.createExecutableExtension(ATT_SECTION_FILTER);
				}
			} catch (CoreException exception) {
				handleSectionError(exception);
			}
		}
		return filter;
	}

	/**
	 * Retrieves the value for section enablement which is a precise number of
	 * items selected. For example: enablesFor=" 4" enables the action only when
	 * 4 items are selected. If not specified, enable for all selections.
	 * 
	 * @return the value for section enablement.
	 */
	public int getEnablesFor() {
		return enablesFor;
	}

	/**
	 * @see org.eclipse.ui.views.properties.tabbed.ISectionDescriptor#getTargetTab()
	 */
	public String getTargetTab() {
		return targetTab;
	}

	/**
	 * @see org.eclipse.ui.views.properties.tabbed.ISectionDescriptor#getAfterSection()
	 */
	public String getAfterSection() {
		if (afterSection == null) {
			return super.getAfterSection();
		}
		return afterSection;
	}

	/**
	 * Creates an instance of a section described by this descriptor
	 * 
	 * @see org.eclipse.ui.views.properties.tabbed.ISectionDescriptor#getSectionClass()
	 */
	public ISection getSectionClass() {
		ISection section = null;
		try {
			section = (ISection) getConfigurationElement()
					.createExecutableExtension(ATT_CLASS);
		} catch (CoreException exception) {
			handleSectionError(exception);
		}

		return section;
	}

	/**
	 * Gets the input types that are valid for this section.
	 * 
	 * @see org.eclipse.ui.views.properties.tabbed.ISectionDescriptor#getInputTypes()
	 */
	public List getInputTypes() {
		if (inputTypes == null) {
			inputTypes = new ArrayList();
			IConfigurationElement[] elements = getConfigurationElement()
					.getChildren(ELEMENT_INPUT);
			for (int i = 0; i < elements.length; i++) {
				IConfigurationElement element = elements[i];
				inputTypes.add(element.getAttribute(ATT_INPUT_TYPE));
			}
		}

		return inputTypes;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getId();
	}

	/**
	 * @return Returns the configurationElement.
	 */
	private IConfigurationElement getConfigurationElement() {
		return configurationElement;
	}
}
