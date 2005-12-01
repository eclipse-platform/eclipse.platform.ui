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
/*
 * Created on Apr 12, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.navigator.internal.extensions;

import org.eclipse.ui.IPluginContribution;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class NavigatorActivityHelper {

	public static boolean isActivityEnabled(NavigatorContentExtension extension) {
		// TODO FIXUP MDE
		return true;
		//		final String localID = extension.getNavigatorExtensionId();
		//		IConfigurationElement element = extension.getDescriptor().getConfigurationElement();
		//		String tempPluginID = null;
		//		try {
		//			tempPluginID = ((ConfigurationElement)
		// element).getParentExtension().getDeclaringPluginDescriptor().getUniqueIdentifier();
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//		}
		//		boolean enabled = true;
		//		if (null != tempPluginID) {
		//			final String pluginID = tempPluginID;
		//			IPluginContribution contribution = new IPluginContribution() {
		//				public String getLocalId() {
		//					return localID;
		//				}
		//
		//				public String getPluginId() {
		//					return pluginID;
		//				}
		//			};
		//			IWorkbenchActivitySupport workbenchActivitySupport =
		// PlatformUI.getWorkbench().getActivitySupport();
		//			IIdentifier identifier =
		// workbenchActivitySupport.getActivityManager().getIdentifier(createUnifiedId(contribution));
		//			enabled = identifier.isEnabled();
		//		}
		//		return enabled;
	}

	public static final String createUnifiedId(IPluginContribution contribution) {
		if (contribution.getPluginId() != null)
			return contribution.getPluginId() + '/' + contribution.getLocalId();
		return contribution.getLocalId();
	}
}
