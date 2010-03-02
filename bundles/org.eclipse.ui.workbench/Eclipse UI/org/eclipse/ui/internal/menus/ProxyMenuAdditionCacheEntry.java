/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Proxy cache entry that manages access to contributed instances of
 * {@link ExtensionContributionFactory}.
 * 
 * @since 3.5
 * 
 */
public class ProxyMenuAdditionCacheEntry extends AbstractMenuAdditionCacheEntry {

	private AbstractContributionFactory factory;
	private boolean createFactory = true;

	/**
     * Create a new instance of this class.
     *
	 * @param location the location URI
	 * @param namespace the namespace
	 * @param element the defining element
	 */
	public ProxyMenuAdditionCacheEntry(String location, String namespace,
			IConfigurationElement element) {
		super(location, namespace, element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.menus.AbstractContributionFactory#createContributionItems
	 * (org.eclipse.ui.services.IServiceLocator,
	 * org.eclipse.ui.menus.IContributionRoot)
	 */
	public void createContributionItems(IServiceLocator serviceLocator,
			IContributionRoot additions) {
		AbstractContributionFactory factory = getFactory();
		if (factory != null)
			factory.createContributionItems(serviceLocator, additions);
	}

	/**
	 * Return the factory or <code>null</code> if it could not be obtained.
	 * 
	 * @return the factory
	 */
	private AbstractContributionFactory getFactory() {
		if (createFactory)
			factory = createFactory();
		return factory;
	}

	/**
	 * Return the factory or <code>null</code> if it could not be obtained.
	 * 
	 * @return the factory
	 */
	private AbstractContributionFactory createFactory() {
		final AbstractContributionFactory[] factory = new AbstractContributionFactory[1];

		SafeRunner.run(new SafeRunnable() {

			public void run() throws Exception {
				factory[0] = (AbstractContributionFactory) WorkbenchPlugin
						.createExtension(getConfigElement(),
								IWorkbenchRegistryConstants.ATT_CLASS);

			}
		});
		createFactory = false;
		return factory[0];
	}
}
