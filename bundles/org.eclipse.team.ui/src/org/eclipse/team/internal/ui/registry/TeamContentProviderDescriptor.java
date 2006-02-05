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

import org.eclipse.core.runtime.*;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.mapping.ITeamContentProviderDescriptor;

/**
 * A team content provider descriptor associates a model provider
 * with a navigator content extension
 */
public class TeamContentProviderDescriptor implements ITeamContentProviderDescriptor {

	private static final String TAG_TEAM_CONTENT_PROVIDER = "teamContentProvider"; //$NON-NLS-1$
	
	private static final String ATT_MODEL_PROVIDER_ID = "modelProviderId"; //$NON-NLS-1$
	private static final String ATT_CONTENT_EXTENSION_ID = "contentExtensionId"; //$NON-NLS-1$
	private static final String ATT_ICON = "icon"; //$NON-NLS-1$
	private static final String ATT_PREFERENCE_PAGE = "preferencePage"; //$NON-NLS-1$

	private String modelProviderId;
	private String contentExtensionId;

	private ImageDescriptor imageDescriptor;

	private IConfigurationElement configElement;

	public TeamContentProviderDescriptor(IExtension extension) throws CoreException {
		readExtension(extension);
	}

	/**
	 * Initialize this descriptor based on the provided extension point.
	 */
	protected void readExtension(IExtension extension) throws CoreException {
		//read the extension
		String id = extension.getUniqueIdentifier(); // id not required
		IConfigurationElement[] elements = extension.getConfigurationElements();
		int count = elements.length;
		for (int i = 0; i < count; i++) {
			IConfigurationElement element = elements[i];
			configElement = element;
			String name = element.getName();
			if (name.equalsIgnoreCase(TAG_TEAM_CONTENT_PROVIDER)) {
				modelProviderId = element.getAttribute(ATT_MODEL_PROVIDER_ID);
				contentExtensionId = element.getAttribute(ATT_CONTENT_EXTENSION_ID);
			}
			break;
		}
		if (modelProviderId == null)
			fail(NLS.bind(TeamUIMessages.TeamContentProviderDescriptor_1, new String[] { ATT_MODEL_PROVIDER_ID, TAG_TEAM_CONTENT_PROVIDER, id == null ? "" : id})); //$NON-NLS-1$
		if (contentExtensionId == null)
			fail(NLS.bind(TeamUIMessages.TeamContentProviderDescriptor_1, new String[] { ATT_CONTENT_EXTENSION_ID, TAG_TEAM_CONTENT_PROVIDER, id == null ? "" : id})); //$NON-NLS-1$
	}
	
	protected void fail(String reason) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, TeamUIPlugin.ID, 0, reason, null));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.registry.ITeamContentProviderDescriptor#getContentExtensionId()
	 */
	public String getContentExtensionId() {
		return contentExtensionId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.registry.ITeamContentProviderDescriptor#getModelProviderId()
	 */
	public String getModelProviderId() {
		return modelProviderId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.registry.ITeamContentProviderDescriptor#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		if (imageDescriptor != null)
			return imageDescriptor;
		String iconName = configElement.getAttribute(ATT_ICON);
		if (iconName == null)
			return null;
		imageDescriptor = TeamUIPlugin.getImageDescriptorFromExtension(configElement.getDeclaringExtension(), iconName);
		return imageDescriptor;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.registry.ITeamContentProviderDescriptor#createPreferencePage()
	 */
	public IPreferencePage createPreferencePage() throws CoreException {
		Object obj = RegistryReader.createExtension(configElement, ATT_PREFERENCE_PAGE);
		return (IPreferencePage) obj;
	}
}
