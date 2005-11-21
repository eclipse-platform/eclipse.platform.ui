/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.resources.internal.workbench;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.navigator.internal.filters.ExtensionFilterDescriptor;
import org.eclipse.ui.navigator.internal.filters.ExtensionFilterProvider;
import org.eclipse.ui.navigator.resources.internal.plugin.WorkbenchNavigatorMessages;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.navigator.ResourcePatternFilter;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * @since 3.2
 *  
 */
public class ResourceExtensionFilterProvider implements ExtensionFilterProvider {

	private static final String HIDE = WorkbenchNavigatorMessages.ResourceExtensionFilterProvider_Hides ;
	/**
	 *  
	 */
	public ResourceExtensionFilterProvider() {
		super();
	}

	/*
	 * (non-Javadoc) @return a List of ExtensionFilterDescriptor (s)
	 * 
	 * @see org.eclipse.wst.common.navigator.internal.views.navigator.filters.ExtensionFilterProvider#getExtensionFilterDescriptors(java.lang.String)
	 */
	public List getExtensionFilterDescriptors(String extensionId, String viewerId) {
		List extFilterDescriptors = new ArrayList();
		ExtensionFilterDescriptor extFilterDescriptor = null;
		ViewerFilter vf = null;

		AbstractUIPlugin plugin = (AbstractUIPlugin) Platform.getPlugin("org.eclipse.ui.ide"); //$NON-NLS-1$
		if (plugin != null) {
			IExtensionPoint extension = plugin.getDescriptor().getExtensionPoint("resourceFilters"); //$NON-NLS-1$
			if (extension != null) {
				IExtension[] extensions = extension.getExtensions();
				for (int i = 0; i < extensions.length; i++) {
					IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
					for (int j = 0; j < configElements.length; j++) {
						String pattern = configElements[j].getAttribute("pattern");//$NON-NLS-1$
						if (pattern != null) {
							vf = new ResourcePatternFilter();
							((ResourcePatternFilter) vf).setPatterns(new String[]{pattern});
							String selected = configElements[j].getAttribute("selected");//$NON-NLS-1$

							// override the enablement of .* or use the Resource setting
							boolean isDotStar = (pattern != null && ".*".equals(pattern)); //$NON-NLS-1$
							boolean enabledByDefault = (isDotStar || (selected != null && selected.equalsIgnoreCase("true"))); //$NON-NLS-1$ 
							String description = HIDE + pattern;
							extFilterDescriptor = new ExtensionFilterDescriptor(extensionId + "." + pattern, extensionId, //$NON-NLS-1$
										pattern, description, viewerId, enabledByDefault, vf);

							if (!extFilterDescriptors.contains(extFilterDescriptor))
								extFilterDescriptors.add(extFilterDescriptor);
						}
					}
				}
			}
		}


		//		  if (pattensDefined.size() > 0) {
		//		  	NamePatternFilter patternFilter = new NamePatternFilter();
		//		  	patternFilter.setPatterns(patternsDefined);
		//		  	extFilterDescriptor = new ExtensionFilterDescriptor(descriptors[i].getId(), extensionId,
		// descriptors[i].getName(), descriptors[i].getDescription(), viewerId, false, vf);
		//		  	extFilterDescriptors.add(extFilterDescriptor);
		//		  }

		return extFilterDescriptors;
	}
}
