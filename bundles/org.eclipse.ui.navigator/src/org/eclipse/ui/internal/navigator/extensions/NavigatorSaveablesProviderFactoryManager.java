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
package org.eclipse.ui.internal.navigator.extensions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.navigator.SaveablesProviderFactory;
import org.osgi.framework.Bundle;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class NavigatorSaveablesProviderFactoryManager {

	private static final NavigatorSaveablesProviderFactoryManager INSTANCE = new NavigatorSaveablesProviderFactoryManager();

	/**
	 * Maps from bundle object to (saveableModelProviderService class name OR saveableModelProviderService object)
	 */
	private final Map saveableModelProviderFactories = new HashMap();

	/**
	 * @return The intialized singleton instance of the viewer descriptor
	 *         registry.
	 */
	public static NavigatorSaveablesProviderFactoryManager getInstance() {
		return INSTANCE;
	}

	protected NavigatorSaveablesProviderFactoryManager() {
		new NavigatorSaveableModelProviderFactoryRegistry().readRegistry();
	}

	/**
	 * 
	 * @param contentExtensionId 
	 * @return The current list of SaveablesProviderFactory objects
	 */
	public SaveablesProviderFactory[] getSaveablesProviderFactories(String contentExtensionId) {
		// TODO use contentExtensionId
		synchronized (saveableModelProviderFactories) {
			List result = new ArrayList();
			for (Iterator it = saveableModelProviderFactories.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				Bundle bundle = (Bundle) entry.getKey();
				// TODO only return objects from active bundles (needs bundle listener to keep list current)
				if(true || bundle.getState() == Bundle.ACTIVE) {
					Object classnameOrObject = entry.getValue();
					if (classnameOrObject instanceof String) {
						Class clazz;
						try {
							clazz = bundle.loadClass((String) classnameOrObject);
							SaveablesProviderFactory saveablesProviderFactory = (SaveablesProviderFactory) clazz.newInstance();
							classnameOrObject = saveablesProviderFactory;
							entry.setValue(classnameOrObject);
						} catch (ClassNotFoundException ex) {
							NavigatorPlugin.logError(0, "could not load subclass of SaveablesProviderFactory", ex); //$NON-NLS-1$
						} catch (InstantiationException ex) {
							NavigatorPlugin.logError(0, "could not instantiate SaveablesProviderFactory", ex); //$NON-NLS-1$
						} catch (IllegalAccessException ex) {
							NavigatorPlugin.logError(0, "could not access subclass of SaveablesProviderFactory", ex); //$NON-NLS-1$
						}
					}
					if (classnameOrObject instanceof SaveablesProviderFactory) {
						result.add(classnameOrObject);
					}
				}
			}
			return (SaveablesProviderFactory[]) result.toArray(new SaveablesProviderFactory[result.size()]);
		}

	}

	private class NavigatorSaveableModelProviderFactoryRegistry extends RegistryReader
			implements IViewerExtPtConstants {

		protected NavigatorSaveableModelProviderFactoryRegistry() {
			super(NavigatorPlugin.PLUGIN_ID, TAG_SAVEABLES_PROVIDER_FACTORY);
		}

		protected boolean readElement(IConfigurationElement element) {
			if (TAG_SAVEABLES_PROVIDER_FACTORY.equals(element.getName())) {
				String className = element.getAttribute(ATT_CLASS);
				String bundleName = element.getDeclaringExtension().getContributor().getName();
				Bundle bundle = Platform.getBundle(bundleName);
				saveableModelProviderFactories.put(bundle, className);
				return true;
			}
			return false;
		}
	}

}
