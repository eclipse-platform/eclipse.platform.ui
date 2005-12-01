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
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ui.navigator.internal.filters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.navigator.internal.CommonNavigatorMessages;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;
import org.eclipse.ui.navigator.internal.extensions.RegistryReader;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class ExtensionFilterViewerRegistry extends RegistryReader {

	public static final String NAVIGATOR_FILTER = "commonFilter"; //$NON-NLS-1$

	/* Associates this ExtensionFilterRegistry with a specific Common Navigator instance */
	private final String viewerId;

	/*
	 * Stores the available navigator filters for the viewer with the viewerid below
	 * navigatorExtensionId (String) to FilterDescriptor pairs
	 */
	private final Map navigatorFilters = new HashMap();

	private final ExtensionFilterActivationManager activationManager;

	/*
	 * Provides a coherent view of all filters provided by third party viewers. The registry allows
	 * the Common Navigator instance to absorb the complete set of existing filters from other
	 * viewers.
	 */
	private final ThirdPartyFilterProviderRegistry thirdPartyFilterProviderRegistry = new ThirdPartyFilterProviderRegistry();

	public ExtensionFilterViewerRegistry(String viewerId) {
		super(NavigatorPlugin.PLUGIN_ID, NAVIGATOR_FILTER);   
		this.viewerId = viewerId;
		activationManager = new ExtensionFilterActivationManager(this.viewerId, this);
		readRegistry();
		initializeActivations();
	}

	/**
	 *  
	 */
	private void initializeActivations() {
		String navigatorExtensionId = null;
		for (Iterator keysItr = navigatorFilters.keySet().iterator(); keysItr.hasNext();) {
			navigatorExtensionId = (String) keysItr.next();
			getActivationManager().revertFilterActivations(navigatorExtensionId);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.common.navigator.internal.views.navigator.filters.RegistryReader#readElement(org.eclipse.core.runtime.IConfigurationElement)
	 */
	public boolean readElement(IConfigurationElement element) {
		if (element.getName().equals(NAVIGATOR_FILTER)) {
			ExtensionFilterDescriptor descriptor = new ExtensionFilterDescriptor(element);
			getExtensionFilterDescriptors(descriptor.getNavigatorExtensionId()).add(descriptor);
			return true;
		}
		return false;
	}

	public final ExtensionFilterDescriptor[] getActiveDescriptors(String navigatorExtensionId) {

		List availableDescriptors = getExtensionFilterDescriptors(navigatorExtensionId);
		List activeDescriptors = new ArrayList();
		for (int i = 0; i < availableDescriptors.size(); i++) {
			if (getActivationManager().isFilterActive((ExtensionFilterDescriptor) availableDescriptors.get(i)))
				activeDescriptors.add(availableDescriptors.get(i));
		}
		ExtensionFilterDescriptor[] descriptorsArray = new ExtensionFilterDescriptor[activeDescriptors.size()];
		activeDescriptors.toArray(descriptorsArray);
		return descriptorsArray;
	}

	public final ExtensionFilterDescriptor[] getAllDescriptors(String navigatorExtensionId) {

		List descriptors = getExtensionFilterDescriptors(navigatorExtensionId);
		ExtensionFilterDescriptor[] descriptorsArray = new ExtensionFilterDescriptor[descriptors.size()];
		descriptors.toArray(descriptorsArray);
		return descriptorsArray;
	}

	protected final List getExtensionFilterDescriptors(String navigatorExtensionId) {
		List descriptors = (List) getNavigatorFilters().get(navigatorExtensionId);

		if (descriptors != null)
			return descriptors;
		synchronized (getNavigatorFilters()) {
			descriptors = (List) getNavigatorFilters().get(navigatorExtensionId);
			if (descriptors == null) {
				descriptors = new ArrayList();
				initializeThirdPartyFilterProviders(navigatorExtensionId, descriptors);
				getNavigatorFilters().put(navigatorExtensionId, descriptors);
			}
		}
		return descriptors;
	}

	public void clearCachedNavigatorFilters() {

		printFilters();
		navigatorFilters.clear();
		readRegistry();
		printFilters();
	}

	/**
	 * @return Returns the viewerId.
	 */
	protected String getViewerId() {
		return viewerId;
	}

	/**
	 * @return Returns the navigatorFilters.
	 */
	protected Map getNavigatorFilters() {
		return navigatorFilters;
	}


	/**
	 * @return Returns the thirdPartyFilterProviderRegistry.
	 */
	protected ThirdPartyFilterProviderRegistry getThirdPartyFilterProviderRegistry() {
		return thirdPartyFilterProviderRegistry;
	}

	/**
	 * @return Returns the activationManager.
	 */
	public ExtensionFilterActivationManager getActivationManager() {
		return activationManager;
	}

	/**
	 * @param navigatorExtensionId
	 * @param descriptors
	 */
	private void initializeThirdPartyFilterProviders(String navigatorExtensionId, List descriptors) {

		List thirdPartyExtensionFilterProviders = getThirdPartyFilterProviderRegistry().getThirdPartyFilterProviders(navigatorExtensionId);
		ExtensionFilterProvider provider = null;
		for (int i = 0; i < thirdPartyExtensionFilterProviders.size(); i++) {
			try {
				provider = ((ThirdPartyFilterProviderRegistry.ThirdPartyFilterProviderDescriptor) thirdPartyExtensionFilterProviders.get(i)).createProvider();
				if (provider != null)
					descriptors.addAll(provider.getExtensionFilterDescriptors(navigatorExtensionId, this.viewerId));
			} catch (RuntimeException e) {
				// TODO Log this more appropriately
				System.err.println(CommonNavigatorMessages.ExtensionFilterViewerRegistry_0);
				e.printStackTrace();
			} catch (NoClassDefFoundError ncdfe) {
				System.err.println(CommonNavigatorMessages.ExtensionFilterViewerRegistry_1);
				ncdfe.printStackTrace();
			}
		}

	}

	/**
	 *  
	 */
	private void printFilters() {

		//		System.out.println(getClass().getName());
		//		Object key = null;
		//		Iterator keys = getNavigatorFilters().keySet().iterator();
		//		while (keys.hasNext()) {
		//			key = keys.next();
		//			System.out.println("Key: " + key);
		//			ExtensionFilterDescriptor[] filters = getAllDescriptors(key.toString());
		//			for (int i = 0; i < filters.length; i++)
		//				System.out.println("\t" + filters[i]);
		//		}
	}
}