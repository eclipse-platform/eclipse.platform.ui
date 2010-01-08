/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

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
		IExtensionPoint point = extensionRegistry.getExtensionPoint("org.eclipse.ui.views"); //$NON-NLS-1$
		for (IConfigurationElement element : point.getConfigurationElements()) {
			MPartDescriptor descriptor = MApplicationFactory.eINSTANCE.createPartDescriptor();
			descriptor.setLabel(element.getAttribute("name")); //$NON-NLS-1$
			descriptor.setId(element.getAttribute("id")); //$NON-NLS-1$
			descriptor
					.setURI("platform:/plugin/org.eclipse.ui.workbench/org.eclipse.ui.internal.e4.compatibility.CompatibilityView"); //$NON-NLS-1$

			application.getDescriptors().add(descriptor);
			descriptors.put(descriptor.getId(), new ViewDescriptor(element));
		}
	}

	public IViewDescriptor find(String id) {
		return descriptors.get(id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.IViewRegistry#getCategories()
	 */
	public IViewCategory[] getCategories() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.IViewRegistry#getViews()
	 */
	public IViewDescriptor[] getViews() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.IViewRegistry#getStickyViews()
	 */
	public IStickyViewDescriptor[] getStickyViews() {
		// TODO Auto-generated method stub
		return null;
	}

}
