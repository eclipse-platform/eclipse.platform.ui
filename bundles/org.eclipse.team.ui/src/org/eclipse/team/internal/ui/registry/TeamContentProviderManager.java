/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.registry;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.team.internal.ui.TeamUIPlugin;

/**
 * Manages the team content provider extension point
 */
public class TeamContentProviderManager {
	
	public static final String PT_TEAM_CONTENT_PROVIDERS = "teamContentProviders"; //$NON-NLS-1$

	private static TeamContentProviderManager instance;
	
	List descriptors;
	
	public static TeamContentProviderManager getInstance() {
		if (instance == null)
			instance = new TeamContentProviderManager();
		return instance;
	}
	
	public ITeamContentProviderDescriptor[] getDescriptors() {
		lazyInitialize();
		return (ITeamContentProviderDescriptor[]) descriptors.toArray(new ITeamContentProviderDescriptor[descriptors.size()]);
	}
	
	public String[] getContentProviderIds() {
		List result = new ArrayList();
		ITeamContentProviderDescriptor[] descriptors = getDescriptors();
		for (int i = 0; i < descriptors.length; i++) {
			ITeamContentProviderDescriptor descriptor = descriptors[i];
			result.add(descriptor.getContentExtensionId());
		}
		// TODO: Is this still required?
		result.add("org.eclipse.team.ui.navigatorContent"); //$NON-NLS-1$
		return (String[]) result.toArray(new String[result.size()]);
	}
	
	protected void lazyInitialize() {
		if (descriptors != null)
			return;
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(TeamUIPlugin.ID, PT_TEAM_CONTENT_PROVIDERS);
		IExtension[] extensions = point.getExtensions();
		descriptors = new ArrayList(extensions.length);
		for (int i = 0, imax = extensions.length; i < imax; i++) {
			ITeamContentProviderDescriptor desc = null;
			try {
				desc = new TeamContentProviderDescriptor(extensions[i]);
			} catch (CoreException e) {
				TeamUIPlugin.log(e);
			}
			if (desc != null)
				descriptors.add(desc);
		}
	}
}
