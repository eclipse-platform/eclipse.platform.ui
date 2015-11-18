/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jan-Hendrik Diederich, Bredex GmbH - bug 201052
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 430616, 441267, 441282, 445609, 441280, 472654
 *     Simon Scholz <scholzsimon@vogella.com> - Bug 473845
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityPart;
import org.eclipse.ui.internal.menus.MenuHelper;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.IStickyViewDescriptor;
import org.eclipse.ui.views.IViewCategory;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;
import org.osgi.framework.Bundle;

public class ViewRegistry implements IViewRegistry {

	public static final String VIEW_TAG = "View"; //$NON-NLS-1$

	/**
	 * This constant is used as key for persisting the original class for a
	 * legacy {@link ViewPart} in the persisted state of a
	 * {@link MPartDescriptor}.
	 */
	public static final String ORIGINAL_COMPATIBILITY_VIEW_CLASS = "originalCompatibilityViewClass"; //$NON-NLS-1$

	/**
	 * This constant is used as key for persisting the original bundle for a
	 * legacy {@link ViewPart} in the persisted state of a
	 * {@link MPartDescriptor}.
	 */
	public static final String ORIGINAL_COMPATIBILITY_VIEW_BUNDLE = "originalCompatibilityViewBundle"; //$NON-NLS-1$

	@Inject
	private MApplication application;

	@Inject
	private EModelService modelService;

	@Inject
	private IExtensionRegistry extensionRegistry;

	@Inject
	private IWorkbench workbench;

	@Inject
	Logger logger;

	private Map<String, IViewDescriptor> descriptors = new HashMap<>();

	private List<IStickyViewDescriptor> stickyDescriptors = new ArrayList<>();

	private HashMap<String, ViewCategory> categories = new HashMap<>();

	private Category miscCategory = new Category();

	@PostConstruct
	void postConstruct() {
		IExtensionPoint point = extensionRegistry.getExtensionPoint("org.eclipse.ui.views"); //$NON-NLS-1$
		for (IExtension extension : point.getExtensions()) {
			// find the category first
			for (IConfigurationElement element : extension.getConfigurationElements()) {
				if (element.getName().equals(IWorkbenchRegistryConstants.TAG_CATEGORY)) {
					ViewCategory category = new ViewCategory(
							element.getAttribute(IWorkbenchRegistryConstants.ATT_ID),
							element.getAttribute(IWorkbenchRegistryConstants.ATT_NAME));
					categories.put(category.getId(), category);
				} else if (element.getName().equals(IWorkbenchRegistryConstants.TAG_STICKYVIEW)) {
					try {
						stickyDescriptors.add(new StickyViewDescriptor(element));
					} catch (CoreException e) {
						// log an error since its not safe to open a dialog here
						logger.error("Unable to create sticky view descriptor.", e.getStatus()); //$NON-NLS-1$
					}
				}
			}
		}
		if (!categories.containsKey(miscCategory.getId())) {
			categories.put(miscCategory.getId(), new ViewCategory(miscCategory.getId(),
					miscCategory.getLabel()));
		}

		for (IExtension extension : point.getExtensions()) {
			for (IConfigurationElement element : extension.getConfigurationElements()) {
				if (element.getName().equals(IWorkbenchRegistryConstants.TAG_VIEW)) {
					createDescriptor(element, false);
				}
				if (element.getName().equals(IWorkbenchRegistryConstants.TAG_E4VIEW)) {
					createDescriptor(element, true);
				}
			}
		}
	}

	private void createDescriptor(IConfigurationElement element, boolean e4View) {
		String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
		MPartDescriptor descriptor = null;
		List<MPartDescriptor> currentDescriptors = application.getDescriptors();
		for (MPartDescriptor desc : currentDescriptors) {
			// do we have a matching descriptor?
			if (desc.getElementId().equals(id)) {
				descriptor = desc;
				break;
			}
		}
		if (descriptor == null) { // create a new descriptor
			descriptor = modelService.createModelElement(MPartDescriptor.class);
			descriptor.setElementId(id);
			application.getDescriptors().add(descriptor);
		}
		// ==> Update descriptor
		descriptor.setLabel(element.getAttribute(IWorkbenchRegistryConstants.ATT_NAME));

		List<String> tags = descriptor.getTags();
		tags.add(VIEW_TAG);

		descriptor.setCloseable(true);
		descriptor.setAllowMultiple(Boolean.parseBoolean(element
				.getAttribute(IWorkbenchRegistryConstants.ATT_ALLOW_MULTIPLE)));

		// make view description available as tooltip
		String viewDescription = RegistryReader.getDescription(element);
		descriptor.setTooltip(viewDescription);

		// Is this an E4 part or a legacy IViewPart ?
		String clsSpec = element.getAttribute(IWorkbenchConstants.TAG_CLASS);
		String implementationURI = CompatibilityPart.COMPATIBILITY_VIEW_URI;
		if (e4View) {
			implementationURI = "bundleclass://" + element.getContributor().getName() + "/" + clsSpec; //$NON-NLS-1$//$NON-NLS-2$
		} else {
			IExtension declaringExtension = element.getDeclaringExtension();
			String name = declaringExtension.getContributor().getName();

			Bundle bundle = Platform.getBundle(name);
			// the indexOf operation removes potential additional information
			// from the qualified classname
			int colonIndex = clsSpec.indexOf(':');
			String viewClass = colonIndex == -1 ? clsSpec : clsSpec.substring(0, colonIndex);
			descriptor.getPersistedState().put(ORIGINAL_COMPATIBILITY_VIEW_CLASS, viewClass);
			descriptor.getPersistedState().put(ORIGINAL_COMPATIBILITY_VIEW_BUNDLE, bundle.getSymbolicName());
		}
		descriptor.setContributionURI(implementationURI);

		String iconURI = MenuHelper.getIconURI(element, IWorkbenchRegistryConstants.ATT_ICON);
		if (iconURI == null) {
			descriptor.setIconURI(MenuHelper.getImageUrl(workbench.getSharedImages()
					.getImageDescriptor(ISharedImages.IMG_DEF_VIEW)));
		} else {
			descriptor.setIconURI(iconURI);
		}

		String categoryId = element.getAttribute(IWorkbenchRegistryConstants.ATT_CATEGORY);
		ViewCategory category = findCategory(categoryId);
		if (category == null) {
			category = findCategory(miscCategory.getId());
		}
		if (category != null) {
			tags.add("categoryTag:" + category.getLabel()); //$NON-NLS-1$
			descriptor.setCategory(category.getLabel());
		}
		// ==> End of update descriptor

		ViewDescriptor viewDescriptor = new ViewDescriptor(application, descriptor, element);
		descriptors.put(descriptor.getElementId(), viewDescriptor);
		if (category != null) {
			category.addDescriptor(viewDescriptor);
		}
	}

	@Override
	public IViewDescriptor find(String id) {
		IViewDescriptor candidate = descriptors.get(id);
		if (WorkbenchActivityHelper.restrictUseOf(candidate)) {
			return null;
		}
		return candidate;
	}

	@Override
	public IViewCategory[] getCategories() {
		return categories.values().toArray(new IViewCategory[categories.size()]);
	}

	@Override
	public IViewDescriptor[] getViews() {
		Collection<?> allowedViews = WorkbenchActivityHelper.restrictCollection(
				descriptors.values(), new ArrayList<>());
		return allowedViews.toArray(new IViewDescriptor[allowedViews.size()]);
	}

	@Override
	public IStickyViewDescriptor[] getStickyViews() {
		Collection<?> allowedViews = WorkbenchActivityHelper.restrictCollection(stickyDescriptors,
				new ArrayList<>());
		return allowedViews.toArray(new IStickyViewDescriptor[allowedViews.size()]);
	}

	/**
	 * Returns the {@link ViewCategory} for the given id or <code>null</code> if
	 * one cannot be found or the id is <code>null</code>
	 *
	 * @param id
	 *            the {@link ViewCategory} id
	 * @return the {@link ViewCategory} with the given id or <code>null</code>
	 */
	public ViewCategory findCategory(String id) {
		if (id == null) {
			return categories.get(miscCategory.getId());
		}
		return categories.get(id);
	}

	public Category getMiscCategory() {
		return miscCategory;
	}

}
