/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Nov 21, 2004
 * 
 * TODO To change the template for this generated file go to Window - Preferences - Java - Code
 * Style - Code Templates
 */
package org.eclipse.ui.navigator.internal.filters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.navigator.internal.NavigatorMessages;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;
import org.eclipse.ui.navigator.internal.extensions.RegistryReader;


/**
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2 
 *
 */
class ThirdPartyFilterProviderRegistry extends RegistryReader {

	protected static final String THIRD_PARTY_FILTER_PROVIDER = "thirdPartyFilterProvider"; //$NON-NLS-1$

	protected static final String ATT_NAVIGATOR_EXTENSION_ID = "navigatorExtensionId"; //$NON-NLS-1$

	protected static final String ATT_CLASS = "class"; //$NON-NLS-1$

	protected static final String ATT_VIEWER_ID = "viewerId"; //$NON-NLS-1$

	class ThirdPartyFilterProviderDescriptor {

		public final String navigatorExtensionId;

		public final String viewerId;

		public final IConfigurationElement element;

		protected ExtensionFilterProvider provider;

		public ThirdPartyFilterProviderDescriptor(String navigatorExtensionId, IConfigurationElement element) {
			this.navigatorExtensionId = navigatorExtensionId;
			this.element = element;
			this.viewerId = this.element.getAttribute(ATT_VIEWER_ID);
		}

		public ExtensionFilterProvider createProvider() {
			try {
				if (provider == null && element != null)
					provider = (ExtensionFilterProvider) element.createExecutableExtension(ATT_CLASS);

			} catch (CoreException e) {
				e.printStackTrace();
			}
			return provider;
		}
	}

	private final Map thirdPartyFilterProviders = new HashMap();

	public ThirdPartyFilterProviderRegistry() {
		super(NavigatorPlugin.PLUGIN_ID, THIRD_PARTY_FILTER_PROVIDER);
		readRegistry();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.common.navigator.internal.views.RegistryReader#readElement(org.eclipse.core.runtime.IConfigurationElement)
	 */
	public boolean readElement(IConfigurationElement element) {
		if (THIRD_PARTY_FILTER_PROVIDER.equals(element.getName())) {
			String navigatorExtensionId = element.getAttribute(ATT_NAVIGATOR_EXTENSION_ID);

			if (navigatorExtensionId != null) {
				addThirdPartyFilterProviders(new ThirdPartyFilterProviderDescriptor(navigatorExtensionId, element));
				return true;
			}
			NavigatorPlugin.log(NavigatorMessages.getString("ExtensionFilterRegistry.16")); //$NON-NLS-1$

		}
		return false;
	}

	protected void addThirdPartyFilterProviders(ThirdPartyFilterProviderDescriptor descriptor) {
		getThirdPartyFilterProviders(descriptor.navigatorExtensionId).add(descriptor);
	}

	public List getThirdPartyFilterProviders(String navigatorExtensionId) {
		List result = (List) getThirdPartyFilterProviders().get(navigatorExtensionId);
		if (result != null)
			return result;
		synchronized (getThirdPartyFilterProviders()) {
			result = (List) getThirdPartyFilterProviders().get(navigatorExtensionId);
			if (result == null)
				getThirdPartyFilterProviders().put(navigatorExtensionId, (result = new ArrayList()));
		}
		return result;
	}

	/**
	 * @return Returns the thirdPartyFilterProviders.
	 */
	protected Map getThirdPartyFilterProviders() {
		return thirdPartyFilterProviders;
	}
}