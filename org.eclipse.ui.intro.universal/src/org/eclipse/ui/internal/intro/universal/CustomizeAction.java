/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.intro.universal;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.intro.impl.IntroPlugin;
import org.eclipse.ui.intro.IIntroSite;


public class CustomizeAction extends Action {

	public static final String P_PAGE_ID = "pageId"; //$NON-NLS-1$
	private IIntroSite site;
	private IConfigurationElement element;
	
	public CustomizeAction(IIntroSite site, IConfigurationElement element) {
		this.site = site;
		this.element = element;
	}
	
	public void run() {
		String pageId = IntroPlugin.getDefault().getIntroModelRoot().getCurrentPageId();
		run(pageId);
	}

	public static IConfigurationElement getPageElement() {
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
				"org.eclipse.ui.preferencePages"); //$NON-NLS-1$
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			if (element.getName().equals("page")) { //$NON-NLS-1$
				String att = element.getAttribute("class"); //$NON-NLS-1$
				if (att != null
						&& att.equals("org.eclipse.ui.intro.universal.ExtensionFactory:welcomeCustomization")) { //$NON-NLS-1$
					return element;
				}
			}
		}
		return null;
	}

	private void run(String pageId) {
		PreferenceManager pm = new PreferenceManager();
		IPreferenceNode node = createPreferenceNode(pageId);
		pm.addToRoot(node);
		IWorkbenchWindow window = site.getWorkbenchWindow();
		CustomizationDialog dlg = new CustomizationDialog(window.getShell(), pageId);
		dlg.open();
	}

	private IPreferenceNode createPreferenceNode(final String pageId) {
		if (element == null)
			return null;
		String id = element.getAttribute("id"); //$NON-NLS-1$
		String label = element.getAttribute("name"); //$NON-NLS-1$
		String className = "org.eclipse.ui.internal.intro.shared.WelcomeCustomizationPreferencePage"; //$NON-NLS-1$
		if (id == null || label == null || className == null)
			return null;
		return new PreferenceNode(id, label, null, className) {

			public void createPage() {
				WelcomeCustomizationPreferencePage page = new WelcomeCustomizationPreferencePage();
		        page.setTitle(getLabelText());
				page.setCurrentPage(pageId);
				setPage(page);
			}
		};
	}
}