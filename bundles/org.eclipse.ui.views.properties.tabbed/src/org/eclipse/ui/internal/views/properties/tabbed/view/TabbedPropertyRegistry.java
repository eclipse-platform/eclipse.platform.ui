/*******************************************************************************
 * Copyright (c) 2001, 2018 IBM Corporation and others.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.views.properties.tabbed.TabbedPropertyViewStatusCodes;
import org.eclipse.ui.internal.views.properties.tabbed.l10n.TabbedPropertyMessages;
import org.eclipse.ui.views.properties.tabbed.AbstractTabDescriptor;
import org.eclipse.ui.views.properties.tabbed.IActionProvider;
import org.eclipse.ui.views.properties.tabbed.ISectionDescriptor;
import org.eclipse.ui.views.properties.tabbed.ISectionDescriptorProvider;
import org.eclipse.ui.views.properties.tabbed.ITabDescriptor;
import org.eclipse.ui.views.properties.tabbed.ITabDescriptorProvider;
import org.eclipse.ui.views.properties.tabbed.ITypeMapper;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.ibm.icu.text.MessageFormat;

/**
 * Provides information about the tabbed property extension points. Each tabbed
 * property registry is associated with a unique contributor ID.
 *
 * @author Anthony Hunter
 */
public class TabbedPropertyRegistry {

	private static final String NO_TAB_ERROR = TabbedPropertyMessages.TabbedPropertyRegistry_Non_existing_tab;

	private static final String CONTRIBUTOR_ERROR = TabbedPropertyMessages.TabbedPropertyRegistry_contributor_error;

	private static final String TAB_ERROR = TabbedPropertyMessages.TabDescriptor_Tab_unknown_category;

	// extension point constants
	private static final String EXTPT_CONTRIBUTOR = "propertyContributor"; //$NON-NLS-1$

	private static final String EXTPT_TABS = "propertyTabs"; //$NON-NLS-1$

	private static final String EXTPT_SECTIONS = "propertySections"; //$NON-NLS-1$

	private static final String ELEMENT_TAB = "propertyTab"; //$NON-NLS-1$

	private static final String ELEMENT_SECTION = "propertySection"; //$NON-NLS-1$

	private static final String ELEMENT_PROPERTY_CATEGORY = "propertyCategory"; //$NON-NLS-1$

	private static final String ATT_CATEGORY = "category"; //$NON-NLS-1$

	private static final String ATT_CONTRIBUTOR_ID = "contributorId"; //$NON-NLS-1$

	private static final String ATT_TYPE_MAPPER = "typeMapper"; //$NON-NLS-1$

	private static final String ATT_LABEL_PROVIDER = "labelProvider"; //$NON-NLS-1$

	private static final String ATT_ACTION_PROVIDER = "actionProvider"; //$NON-NLS-1$

	private static final String ATT_SECTION_DESCRIPTOR_PROVIDER = "sectionDescriptorProvider"; //$NON-NLS-1$

	private static final String ATT_TAB_DESCRIPTOR_PROVIDER = "tabDescriptorProvider"; //$NON-NLS-1$

	private static final String ATT_OVERRIDABLE_TAB_LIST_CONTENT_PROVIDER = "overridableTabListContentProvider"; //$NON-NLS-1$

	private static final String TOP = "top"; //$NON-NLS-1$

	protected String contributorId;

	protected IConfigurationElement contributorConfigurationElement;

	protected List<String> propertyCategories;

	protected ILabelProvider labelProvider;

	protected IActionProvider actionProvider;

	protected ITypeMapper typeMapper;

	protected ISectionDescriptorProvider sectionDescriptorProvider;

	protected ITabDescriptorProvider tabDescriptorProvider;

	protected ITabDescriptor[] tabDescriptors;

	protected static final AbstractTabDescriptor[] EMPTY_DESCRIPTOR_ARRAY = new TabDescriptor[0];

	protected boolean overridableTabListContentProvider = false;

	/**
	 * There is one details registry for each contributor type.
	 */
	protected TabbedPropertyRegistry(String id) {
		this.contributorId = id;
		this.propertyCategories = new ArrayList<>();
		IConfigurationElement[] extensions = getConfigurationElements(EXTPT_CONTRIBUTOR);
		for (IConfigurationElement configurationElement : extensions) {
			String contributor = configurationElement
					.getAttribute(ATT_CONTRIBUTOR_ID);
			if (contributor == null || !id.equals(contributor)) {
				continue;
			}
			this.contributorConfigurationElement = configurationElement;
			try {
				if (configurationElement.getAttribute(ATT_LABEL_PROVIDER) != null) {
					labelProvider = (ILabelProvider) configurationElement
							.createExecutableExtension(ATT_LABEL_PROVIDER);
				}
				if (configurationElement.getAttribute(ATT_ACTION_PROVIDER) != null) {
					actionProvider = (IActionProvider) configurationElement
							.createExecutableExtension(ATT_ACTION_PROVIDER);
				}
				if (configurationElement.getAttribute(ATT_TYPE_MAPPER) != null) {
					typeMapper = (ITypeMapper) configurationElement
							.createExecutableExtension(ATT_TYPE_MAPPER);
				}
				if (configurationElement
						.getAttribute(ATT_SECTION_DESCRIPTOR_PROVIDER) != null) {
					sectionDescriptorProvider = (ISectionDescriptorProvider) configurationElement
							.createExecutableExtension(ATT_SECTION_DESCRIPTOR_PROVIDER);
				}
				if (configurationElement
						.getAttribute(ATT_TAB_DESCRIPTOR_PROVIDER) != null) {
					tabDescriptorProvider = (ITabDescriptorProvider) configurationElement
							.createExecutableExtension(ATT_TAB_DESCRIPTOR_PROVIDER);
				}
				if (configurationElement
						.getAttribute(ATT_OVERRIDABLE_TAB_LIST_CONTENT_PROVIDER) != null) {
					String attributeBoolean = configurationElement
							.getAttribute(ATT_OVERRIDABLE_TAB_LIST_CONTENT_PROVIDER);
					overridableTabListContentProvider = attributeBoolean
							.equals("true");//$NON-NLS-1$
				}
			} catch (CoreException exception) {
				handleConfigurationError(id, exception);
			}
			addPropertyCategories(configurationElement);
		}
		if (propertyCategories == null || contributorId == null ||
				contributorConfigurationElement == null) {
			handleConfigurationError(id, null);
			this.contributorId = null;
		}
	}

	/**
	 * Gets the categories that are valid for this contributor.
	 *
	 * @param configurationElement
	 *            the configuration element for this contributor.
	 */
	private void addPropertyCategories(
			IConfigurationElement configurationElement) {
		IConfigurationElement[] elements = configurationElement
				.getChildren(ELEMENT_PROPERTY_CATEGORY);
		for (IConfigurationElement element : elements) {
			propertyCategories.add(element.getAttribute(ATT_CATEGORY));
		}
	}

	/**
	 * Handle the error when an issue is found loading from the configuration
	 * element.
	 *
	 * @param id
	 *            the configuration id.
	 * @param exception
	 *            an optional CoreException
	 */
	private void handleConfigurationError(String id, CoreException exception) {
		String message = MessageFormat.format(CONTRIBUTOR_ERROR, id);
		Bundle bundle = FrameworkUtil.getBundle(TabbedPropertyRegistry.class);
		IStatus status = new Status(IStatus.ERROR, bundle.getSymbolicName(),
				TabbedPropertyViewStatusCodes.CONTRIBUTOR_ERROR, message,
				exception);
		Platform.getLog(bundle).log(status);
	}

	/**
	 * Reads property section extensions. Returns all section descriptors for
	 * the current contributor id or an empty array if none is found.
	 */
	protected ISectionDescriptor[] readSectionDescriptors() {
		List result = new ArrayList();
		IConfigurationElement[] extensions = getConfigurationElements(EXTPT_SECTIONS);
		for (IConfigurationElement extension : extensions) {
			IConfigurationElement[] sections = extension
					.getChildren(ELEMENT_SECTION);
			for (IConfigurationElement section : sections) {
				ISectionDescriptor descriptor = new SectionDescriptor(section,
						typeMapper);
				result.add(descriptor);
			}
		}
		return (ISectionDescriptor[]) result
				.toArray(new ISectionDescriptor[result.size()]);
	}

	/**
	 * Returns the configuration elements targeted for the given extension point
	 * and the current contributor id. The elements are also sorted by plugin
	 * prerequisite order.
	 */
	protected IConfigurationElement[] getConfigurationElements(
			String extensionPointId) {
		if (contributorId == null) {
			return new IConfigurationElement[0];
		}
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(FrameworkUtil.getBundle(TabbedPropertyRegistry.class).getSymbolicName(),
						extensionPointId);
		IConfigurationElement[] extensions = point.getConfigurationElements();
		List unordered = new ArrayList(extensions.length);
		for (IConfigurationElement extension : extensions) {
			if (!extension.getName().equals(extensionPointId)) {
				continue;
			}
			String contributor = extension.getAttribute(ATT_CONTRIBUTOR_ID);
			if (!contributorId.equals(contributor)) {
				continue;
			}
			unordered.add(extension);
		}
		return (IConfigurationElement[]) unordered
				.toArray(new IConfigurationElement[unordered.size()]);
	}

	/**
	 * Returns the index of the given element in the array.
	 */
	private int getIndex(Object[] array, Object target) {
		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(target)) {
				return i;
			}
		}
		return -1; // should never happen
	}

	/**
	 * Returns all section descriptors for the provided selection.
	 *
	 * @param part
	 *            the workbench part containing the selection
	 * @param selection
	 *            the current selection.
	 * @return all section descriptors.
	 */
	public ITabDescriptor[] getTabDescriptors(IWorkbenchPart part,
			ISelection selection) {
		if (selection == null || selection.isEmpty()) {
			return EMPTY_DESCRIPTOR_ARRAY;
		}

		ITabDescriptor[] allDescriptors = null;
		if (tabDescriptorProvider == null) {
			allDescriptors = getAllTabDescriptors();
		} else {
			allDescriptors = tabDescriptorProvider.getTabDescriptors(part,
					selection);
		}

		ITabDescriptor[] result = filterTabDescriptors(allDescriptors, part,
				selection);
		return result;
	}

	/**
	 * Filters out the tab descriptors that do not have any sections for the
	 * given input.
	 */
	protected ITabDescriptor[] filterTabDescriptors(
			ITabDescriptor[] descriptors, IWorkbenchPart part,
			ISelection selection) {
		List result = new ArrayList();
		for (ITabDescriptor descriptor : descriptors) {
			ITabDescriptor filteredDescriptor = adaptDescriptorFor(descriptor,
					part, selection);
			if (!filteredDescriptor.getSectionDescriptors().isEmpty()) {
				result.add(filteredDescriptor);
			}
		}
		if (result.isEmpty()) {
			return EMPTY_DESCRIPTOR_ARRAY;
		}
		return (ITabDescriptor[]) result.toArray(new ITabDescriptor[result
				.size()]);
	}

	/**
	 * Given a property tab descriptor remove all its section descriptors that
	 * do not apply to the given input object.
	 */
	protected ITabDescriptor adaptDescriptorFor(ITabDescriptor target,
			IWorkbenchPart part, ISelection selection) {
		List filteredSectionDescriptors = new ArrayList();
		List descriptors = target.getSectionDescriptors();
		for (Iterator iter = descriptors.iterator(); iter.hasNext();) {
			ISectionDescriptor descriptor = (ISectionDescriptor) iter.next();
			if (descriptor.appliesTo(part, selection)) {
				filteredSectionDescriptors.add(descriptor);
			}
		}
		AbstractTabDescriptor result = (AbstractTabDescriptor) ((AbstractTabDescriptor) target)
				.clone();
		result.setSectionDescriptors(filteredSectionDescriptors);
		return result;
	}

	/**
	 * Reads property tab extensions. Returns all tab descriptors for the
	 * current contributor id or an empty array if none is found.
	 */
	protected ITabDescriptor[] getAllTabDescriptors() {
		if (tabDescriptors == null) {
			List temp = readTabDescriptors();
			populateWithSectionDescriptors(temp);
			temp = sortTabDescriptorsByCategory(temp);
			temp = sortTabDescriptorsByAfterTab(temp);
			tabDescriptors = (TabDescriptor[]) temp
					.toArray(new TabDescriptor[temp.size()]);
		}
		return tabDescriptors;
	}

	/**
	 * Reads property tab extensions. Returns all tab descriptors for the
	 * current contributor id or an empty list if none is found.
	 */
	protected List readTabDescriptors() {
		List result = new ArrayList();
		IConfigurationElement[] extensions = getConfigurationElements(EXTPT_TABS);
		for (IConfigurationElement extension : extensions) {
			IConfigurationElement[] tabs = extension.getChildren(ELEMENT_TAB);
			for (IConfigurationElement tab : tabs) {
				TabDescriptor descriptor = new TabDescriptor(tab);
				if (getIndex(propertyCategories.toArray(), descriptor
						.getCategory()) == -1) {
					/* tab descriptor has unknown category */
					handleTabError(tab, descriptor.getCategory() == null ? "" //$NON-NLS-1$
							: descriptor.getCategory());
				} else {
					result.add(descriptor);
				}
			}
		}
		return result;
	}

	/**
	 * Populates the given tab descriptors with section descriptors.
	 */
	protected void populateWithSectionDescriptors(List aTabDescriptors) {
		ISectionDescriptor[] sections = null;
		if (sectionDescriptorProvider != null) {
			sections = sectionDescriptorProvider.getSectionDescriptors();
		} else {
			sections = readSectionDescriptors();
		}
		for (ISectionDescriptor section : sections) {
			appendToTabDescriptor(section, aTabDescriptors);
		}
	}

	/**
	 * Appends the given section to a tab from the list.
	 */
	protected void appendToTabDescriptor(ISectionDescriptor section,
			List aTabDescriptors) {
		for (Iterator i = aTabDescriptors.iterator(); i.hasNext();) {
			TabDescriptor tab = (TabDescriptor) i.next();
			if (tab.append(section)) {
				return;
			}
		}
		// could not append the section to any of the existing tabs - log error
		String message = MessageFormat.format(NO_TAB_ERROR, section.getId(), section.getTargetTab());
		Bundle bundle = FrameworkUtil.getBundle(TabbedPropertyRegistry.class);
		IStatus status = new Status(IStatus.ERROR, bundle.getSymbolicName(),
				TabbedPropertyViewStatusCodes.NO_TAB_ERROR, message, null);
		Platform.getLog(bundle).log(status);
	}

	/**
	 * Sorts the tab descriptors in the given list according to category.
	 */
	protected List<TabDescriptor> sortTabDescriptorsByCategory(List<TabDescriptor> descriptors) {
		Collections.sort(descriptors, (one, two) -> {
			String categoryOne = one.getCategory();
			String categoryTwo = two.getCategory();
			int categoryOnePosition = getIndex(propertyCategories.toArray(), categoryOne);
			int categoryTwoPosition = getIndex(propertyCategories.toArray(), categoryTwo);
			return categoryOnePosition - categoryTwoPosition;
		});
		return descriptors;
	}

	/**
	 * Sorts the tab descriptors in the given list according to afterTab.
	 */
	protected List<TabDescriptor> sortTabDescriptorsByAfterTab(List<TabDescriptor> tabs) {
		if (tabs.isEmpty() || propertyCategories == null) {
			return tabs;
		}
		List<TabDescriptor> sorted = new ArrayList<>();
		int categoryIndex = 0;
		for (int i = 0; i < propertyCategories.size(); i++) {
			List<TabDescriptor> categoryList = new ArrayList<>();
			String category = propertyCategories.get(i);
			int topOfCategory = categoryIndex;
			int endOfCategory = categoryIndex;
			while (endOfCategory < tabs.size() &&
					tabs.get(endOfCategory).getCategory()
							.equals(category)) {
				endOfCategory++;
			}
			for (int j = topOfCategory; j < endOfCategory; j++) {
				TabDescriptor tab = tabs.get(j);
				if (tab.getAfterTab().equals(TOP)) {
					categoryList.add(0, tabs.get(j));
				} else {
					categoryList.add(tabs.get(j));
				}
			}
			Collections.sort(categoryList, (one, two) -> {
				if (two.getAfterTab().equals(one.getId())) {
					return -1;
				} else if (one.getAfterTab().equals(two.getId())) {
					return 1;
				} else {
					return 0;
				}
			});
			for (int j = 0; j < categoryList.size(); j++) {
				sorted.add(categoryList.get(j));
			}
			categoryIndex = endOfCategory;
		}
		return sorted;
	}

	/**
	 * Gets the type mapper for the contributor.
	 *
	 * @return the type mapper for the contributor.
	 */
	public ITypeMapper getTypeMapper() {
		return typeMapper;
	}

	/**
	 * Gets the label provider for the contributor.
	 *
	 * @return the label provider for the contributor.
	 */
	public ILabelProvider getLabelProvider() {
		return labelProvider;
	}

	/**
	 * Gets the action provider for the contributor.
	 *
	 * @return the action provider for the contributor.
	 */
	public IActionProvider getActionProvider() {
		return actionProvider;
	}

	/**
	 * Gets the tab list content provider for the contributor.
	 *
	 * @return the tab list content provider for the contributor.
	 */
	public IStructuredContentProvider getTabListContentProvider() {
		if (overridableTabListContentProvider) {
			return new OverridableTabListContentProvider(this);
		}
		return new TabListContentProvider(this);
	}

	/**
	 * Handle the tab error when an issue is found loading from the
	 * configuration element.
	 *
	 * @param configurationElement
	 *            the configuration element
	 */
	private void handleTabError(IConfigurationElement configurationElement,
			String category) {
		String pluginId = configurationElement.getDeclaringExtension()
				.getContributor().getName();
		String message = MessageFormat.format(TAB_ERROR, pluginId, category );
		IStatus status = new Status(IStatus.ERROR, pluginId,
				TabbedPropertyViewStatusCodes.TAB_ERROR, message, null);
		Bundle bundle = FrameworkUtil.getBundle(TabbedPropertyRegistry.class);
		Platform.getLog(bundle).log(status);
	}

	/**
	 * Disposes this registry.
	 *
	 * @since 3.7
	 */
	public void dispose() {
		if (labelProvider != null) {
			labelProvider.dispose();
			labelProvider = null;
		}

		if (tabDescriptors != null) {
			for (ITabDescriptor tabDescriptor : tabDescriptors) {
				if (tabDescriptor instanceof TabDescriptor)
					((TabDescriptor)tabDescriptor).dispose();
			}
		}
	}
}
