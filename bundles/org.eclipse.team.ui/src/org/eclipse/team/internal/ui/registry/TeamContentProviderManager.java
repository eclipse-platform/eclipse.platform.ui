/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.registry;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.ui.navigator.NavigatorActivationService;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentDescriptor;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentDescriptorRegistry;

/**
 * Manages the team content provider extension point
 */
public class TeamContentProviderManager {
	
	public static final String PT_TEAM_CONTENT_PROVIDERS = "teamContentProviders"; //$NON-NLS-1$

	private static TeamContentProviderManager instance;
	
	Map descriptors;

	public static void enableTeamContentProvider(String viewerId) {
		NavigatorContentDescriptor[] descriptors = NavigatorContentDescriptorRegistry.getInstance().getAllContentDescriptors();
		for (int i = 0; i < descriptors.length; i++) {
			NavigatorContentDescriptor descriptor = descriptors[i];
			boolean enable = TeamContentProviderManager.getInstance().isTeamContentProvider(descriptor.getId());
			NavigatorActivationService.getInstance().activateNavigatorExtension(viewerId, descriptor.getId(), enable);
		}
		NavigatorActivationService.getInstance().activateNavigatorExtension(viewerId, "org.eclipse.team.ui.navigatorContent", true);
	}
	
	public static TeamContentProviderManager getInstance() {
		if (instance == null)
			instance = new TeamContentProviderManager();
		return instance;
	}
	
	public boolean isTeamContentProvider(String id) {
		TeamContentProviderDescriptor[] descs = getDescriptors();
		for (int i = 0; i < descs.length; i++) {
			TeamContentProviderDescriptor descriptor = descs[i];
			if (descriptor.getContentExtensionId().equals(id)) {
				return true;
			}
		}
		return false;
	}
	
	public TeamContentProviderDescriptor[] getDescriptors() {
		lazyInitialize();
		return (TeamContentProviderDescriptor[]) descriptors.values().toArray(new TeamContentProviderDescriptor[descriptors.size()]);
	}
	
	public String[] getContentProviderIds() {
		List result = new ArrayList();
		TeamContentProviderDescriptor[] descriptors = getDescriptors();
		for (int i = 0; i < descriptors.length; i++) {
			TeamContentProviderDescriptor descriptor = descriptors[i];
			result.add(descriptor.getContentExtensionId());
		}
		result.add("org.eclipse.team.ui.navigatorContent");
		return (String[]) result.toArray(new String[result.size()]);
	}
	
	protected void lazyInitialize() {
		if (descriptors != null)
			return;
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(TeamUIPlugin.ID, PT_TEAM_CONTENT_PROVIDERS);
		IExtension[] extensions = point.getExtensions();
		descriptors = new HashMap(extensions.length * 2 + 1);
		for (int i = 0, imax = extensions.length; i < imax; i++) {
			TeamContentProviderDescriptor desc = null;
			try {
				desc = new TeamContentProviderDescriptor(extensions[i]);
			} catch (CoreException e) {
				TeamUIPlugin.log(e);
			}
			if (desc != null)
				descriptors.put(desc.getId(), desc);
		}
	}
}
