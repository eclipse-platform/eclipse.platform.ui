/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jan-Hendrik Diederich, Bredex GmbH - bug 201052
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.services.annotations.PostConstruct;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MPartDescriptor;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.internal.e4.compatibility.E4Util;
import org.eclipse.ui.views.IStickyViewDescriptor;
import org.eclipse.ui.views.IViewCategory;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

public class ViewRegistry implements IViewRegistry {

	@Inject
	private MApplication application;

	@Inject
	private IExtensionRegistry extensionRegistry;

	private Map<String, IViewDescriptor> descriptors = new HashMap<String, IViewDescriptor>();

	@PostConstruct
	void postConstruct() {
		for (MPartDescriptor descriptor : application.getDescriptors()) {
			descriptors.put(descriptor.getId(), new ViewDescriptor(descriptor, null));
		}

		IExtensionPoint point = extensionRegistry.getExtensionPoint("org.eclipse.ui.views"); //$NON-NLS-1$
		for (IConfigurationElement element : point.getConfigurationElements()) {
			if (element.getName().equals(IWorkbenchRegistryConstants.TAG_VIEW)) {
				MPartDescriptor descriptor = MApplicationFactory.eINSTANCE.createPartDescriptor();
				descriptor.setLabel(element.getAttribute(IWorkbenchRegistryConstants.ATT_NAME));
				String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
				descriptor.setId(id);
				if (id.equals(IPageLayout.ID_RES_NAV) || id.equals(IPageLayout.ID_PROJECT_EXPLORER)) {
					descriptor.setCategory("org.eclipse.e4.primaryNavigationStack"); //$NON-NLS-1$
				} else if (id.equals(IPageLayout.ID_OUTLINE)) {
					descriptor.setCategory("org.eclipse.e4.secondaryNavigationStack"); //$NON-NLS-1$
				} else {
					descriptor.setCategory("org.eclipse.e4.secondaryDataStack"); //$NON-NLS-1$
				}

				descriptor.setCloseable(true);
				descriptor.setAllowMultiple(Boolean.parseBoolean(element
						.getAttribute(IWorkbenchRegistryConstants.ATT_ALLOW_MULTIPLE)));
				descriptor
						.setURI("platform:/plugin/org.eclipse.ui.workbench/org.eclipse.ui.internal.e4.compatibility.CompatibilityView"); //$NON-NLS-1$

				String iconURI = element.getAttribute(IWorkbenchRegistryConstants.ATT_ICON);
				if (iconURI != null) {
					StringBuilder builder = new StringBuilder("platform:/plugin/"); //$NON-NLS-1$
					builder.append(element.getNamespaceIdentifier()).append('/');

					// FIXME: need to get rid of $nl$ properly
					if (iconURI.startsWith("$nl$")) { //$NON-NLS-1$
						iconURI = iconURI.substring(4);
					}

					builder.append(iconURI);
					descriptor.setIconURI(builder.toString());
				}

				application.getDescriptors().add(descriptor);
				descriptors.put(descriptor.getId(), new ViewDescriptor(descriptor, element));
			}
		}
	}

	public IViewDescriptor find(String id) {
		return descriptors.get(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.IViewRegistry#getCategories()
	 */
	public IViewCategory[] getCategories() {
		// FIXME: compat getCategories
		E4Util.unsupported("getCategories"); //$NON-NLS-1$
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.IViewRegistry#getViews()
	 */
	public IViewDescriptor[] getViews() {
		return descriptors.values().toArray(new IViewDescriptor[descriptors.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.IViewRegistry#getStickyViews()
	 */
	public IStickyViewDescriptor[] getStickyViews() {
		// FIXME: compat getStickyViews
		E4Util.unsupported("getStickyViews"); //$NON-NLS-1$
		return null;
	}

	/**
	 * 
	 */
	public void dispose() {

	}

	/**
	 * @param string
	 * @return
	 */
	public IViewCategory findCategory(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return
	 */
	public Category getMiscCategory() {
		// TODO Auto-generated method stub
		return null;
	}

}
