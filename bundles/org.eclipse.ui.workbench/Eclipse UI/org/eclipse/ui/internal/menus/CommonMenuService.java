/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.internal.provisional.action.ToolBarManager2;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.services.RegistryPersistence;
import org.eclipse.ui.internal.util.Util;


/**
 * Test harness for new menu story.
 * 
 * <b>NOTE:</b> This is -NOT- meant to be production code but is just a testing
 * scaffold...
 * 
 * @since 3.3
 *
 */
public class CommonMenuService extends RegistryPersistence {

	/**
	 * The name of the <code>org.eclipse.ui.menus2</code> extension point.
	 */
	public static String EXTENSION_MENUS = PlatformUI.PLUGIN_ID + '.'
			+ PL_MENUS;

	private static Map URIToManager = new HashMap();

	public static void readAdditions() {
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		final IConfigurationElement[] menusExtensionPoint = registry
								.getConfigurationElementsFor(COMMON_MENU_ADDITIONS);

		for (int i = 0; i < menusExtensionPoint.length; i++) {
			System.out.println("EPH:" + menusExtensionPoint[i].getName()); //$NON-NLS-1$
			if (PL_MENU_ADDITION.equals(menusExtensionPoint[i].getName())) {
				readMenuAddition(menusExtensionPoint[i]);
			}
		}
	}

	/**
	 * @param configurationElement
	 */
	private static void readMenuAddition(IConfigurationElement addition) {
		// Determine the insertio location by parsing the URI
		String locationURI = addition.getAttribute(TAG_LOCATION_URI);
		URI uri = createURI(locationURI);
		
		if (uri != null) {
			ContributionManager mgr = getManagerForURI(uri);
			int insertionIndex = getInsertionIndexForURI(mgr, uri);			

			// Read teh child additions
			readAdditions(mgr, addition, insertionIndex);
		}
	}

	/**
	 * @param configurationElement
	 */
	private static void readAdditions(ContributionManager mgr, IConfigurationElement addition, 
			int insertionIndex) {
		IConfigurationElement[] items = addition.getChildren();
		for (int i = 0; i < items.length; i++) {
			String itemType = items[i].getName();
			System.out.println("Type: " + itemType); //$NON-NLS-1$
			
			if (TAG_ITEM.equals(itemType)) {
				mgr.insert(insertionIndex++, new MenuItemContribution(items[i]));
			}
			else if (TAG_WIDGET.equals(itemType)) {
				mgr.insert(insertionIndex++, new MenuWidgetContribution(items[i]));
			}
			else if (TAG_MENU.equals(itemType)) {
				MenuMenuContribution subMenu = new MenuMenuContribution(items[i]);
				mgr.insert(insertionIndex++, new MenuMenuContribution(items[i]));

				// Read the sub-structure
				readAdditions(subMenu, items[i], 0);
			}
			else if (TAG_SEPARATOR.equals(itemType)) {
				mgr.insert(insertionIndex++, new MenuSeparatorContribution(items[i]));
			}
		}
	}

	/**
	 * Wraps a URI constructor in a standard exception handler.
	 * 
	 * @param uriDef The string to create the URI from
	 * @return The URI or <code>null</code> if the string is
	 * badly formed.
	 */
	public static URI createURI(String uriDef) {
		URI uri = null;
		try {
			uri = new URI(uriDef);
		} catch (URISyntaxException e) {
			// [TBD] Log it
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return uri;
	}
	
	/**
	 * @param mgr 
	 * @param uri
	 * @return
	 */
	private static int getInsertionIndexForURI(ContributionManager mgr, URI uri) {
		String query = uri.getQuery();
		if (query == null)
			return 0;
		
		// Should be in the form "[before|after]=id"
		String[] queryParts = Util.split(query, '=');
		if (queryParts[1].length() > 0) {
			int indexOfId = mgr.indexOf(queryParts[1]);
			
			// Increment if we're 'after' this id
			if (queryParts[0].equals("after")) //$NON-NLS-1$
				indexOfId++;
		}
		
		return 0;
	}

	/**
	 * @param uri
	 * @return
	 */
	public static ContributionManager getManagerForURI(URI uri) {
		if (uri == null)
			return null;
		
		String mgrId = uri.getScheme() + ":" + uri.getHost(); //$NON-NLS-1$
		ContributionManager mgr = (ContributionManager) URIToManager.get(mgrId);
		if (mgr == null) {
			mgr = createContributionManager(uri);
			URIToManager.put(mgrId, mgr);
		}
		
		return mgr;
	}

	public static ContributionManager getManagerForURI(String uriDef) {
		return getManagerForURI(createURI(uriDef));
	}
	/**
	 * @param locationURI
	 * @return
	 */
	private static ContributionManager createContributionManager(URI uri) {
		String type = uri.getScheme();
		if (type.equals("menu") || type.equals("popup")) { //$NON-NLS-1$ //$NON-NLS-2$
			return new MenuManager(uri.getPath(), uri.getPath());
		}
		else if (type.equals("toolbar")) { //$NON-NLS-1$
			return new ToolBarManager2(SWT.HORIZONTAL);
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.services.RegistryPersistence#isChangeImportant(org.eclipse.core.runtime.IRegistryChangeEvent)
	 */
	protected boolean isChangeImportant(IRegistryChangeEvent event) {
		return false;
	}
}
