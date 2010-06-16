/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import java.util.ArrayList;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.workbench.swt.modeling.MenuServiceFilter;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.internal.e4.compatibility.E4Util;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * @since e4
 * 
 */
public class ActionSet {

	private IConfigurationElement configElement;

	private MApplication application;

	public ActionSet(MApplication application, IEclipseContext appContext,
			IConfigurationElement element) {
		this.application = application;
		this.configElement = element;
	}

	public void addToModel(ArrayList<MMenuContribution> contributions) {

		String idContrib = MenuHelper.getId(configElement);
		IConfigurationElement[] menus = configElement
				.getChildren(IWorkbenchRegistryConstants.TAG_MENU);
		for (IConfigurationElement element : menus) {
			addContribution(idContrib, contributions, element, true);
		}

		IConfigurationElement[] actions = configElement
				.getChildren(IWorkbenchRegistryConstants.TAG_ACTION);
		for (IConfigurationElement element : actions) {
			addContribution(idContrib, contributions, element, false);
		}

		// for entertainment purposes only
		// printContributions(contributions);
	}

	private void addContribution(String idContrib, ArrayList<MMenuContribution> contributions,
			IConfigurationElement element, boolean isMenu) {
		MMenuContribution menuContribution = MenuFactoryImpl.eINSTANCE.createMenuContribution();
		menuContribution.getTags().add(MenuServiceFilter.MC_MENU);
		final String elementId = MenuHelper.getId(element);
		if (idContrib != null && idContrib.length() > 0) {
			menuContribution.setElementId(idContrib + "/" + elementId); //$NON-NLS-1$
		} else {
			menuContribution.setElementId(elementId);
		}

		String path = isMenu ? MenuHelper.getPath(element) : MenuHelper.getMenuBarPath(element);
		if (path == null || path.length() == 0) {
			if (!isMenu) {
				return;
			}
			path = IWorkbenchActionConstants.MB_ADDITIONS;
		}
		Path menuPath = new Path(path);
		String parentId = "org.eclipse.ui.main.menu"; //$NON-NLS-1$
		String positionInParent = "after=" + menuPath.segment(0); //$NON-NLS-1$
		int segmentCount = menuPath.segmentCount();
		if (segmentCount > 1) {
			parentId = menuPath.segment(segmentCount - 2);
			positionInParent = "after=" + menuPath.segment(segmentCount - 1); //$NON-NLS-1$
		}
		menuContribution.setParentID(parentId);
		menuContribution.setPositionInParent(positionInParent);
		if (isMenu) {
			MMenu menu = MenuHelper.createMenuAddition(element);
			menuContribution.getChildren().add(menu);
		} else {
			if (parentId.equals("org.eclipse.ui.main.menu")) { //$NON-NLS-1$
				E4Util.unsupported("****MC: bad pie: " + menuPath); //$NON-NLS-1$
				parentId = IWorkbenchActionConstants.M_WINDOW;
				menuContribution.setParentID(parentId);
			}
			MMenuElement action = MenuHelper.createLegacyActionAdditions(application, element);
			if (action != null) {
				menuContribution.getChildren().add(action);
			}
		}
		if (menuContribution.getChildren().size() > 0) {
			contributions.add(menuContribution);
		}
		if (isMenu) {
			processGroups(idContrib, contributions, element);
		}
	}

	private void processGroups(String idContrib, ArrayList<MMenuContribution> contributions,
			IConfigurationElement element) {
		MMenuContribution menuContribution = MenuFactoryImpl.eINSTANCE.createMenuContribution();
		menuContribution.getTags().add(MenuServiceFilter.MC_MENU);
		final String elementId = MenuHelper.getId(element);
		if (idContrib != null && idContrib.length() > 0) {
			menuContribution.setElementId(idContrib + "/" + elementId + ".groups"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			menuContribution.setElementId(elementId + ".groups"); //$NON-NLS-1$
		}
		menuContribution.setParentID(elementId);
		menuContribution.setPositionInParent("after=additions"); //$NON-NLS-1$
		IConfigurationElement[] children = element.getChildren();
		for (IConfigurationElement sepAddition : children) {
			String name = sepAddition.getAttribute(IWorkbenchRegistryConstants.ATT_NAME);
			MMenuElement sep = MenuFactoryImpl.eINSTANCE.createMenuSeparator();
			sep.setElementId(name);
			if (!MenuHelper.isSeparatorVisible(sepAddition)) {
				sep.setVisible(false);
			}
			menuContribution.getChildren().add(sep);
		}
		if (menuContribution.getChildren().size() > 0) {
			contributions.add(menuContribution);
		}
	}

	MElementContainer<MMenuElement> findMenuFromPath(MElementContainer<MMenuElement> menu,
			Path menuPath, int segment) {
		int idx = MenuHelper.indexForId(menu, menuPath.segment(segment));
		if (idx == -1) {
			if (segment + 1 < menuPath.segmentCount() || !menuPath.hasTrailingSeparator()) {
				return null;
			}
			return menu;
		}
		MElementContainer<MMenuElement> item = (MElementContainer<MMenuElement>) menu.getChildren()
				.get(idx);
		if (item.getChildren().size() == 0) {
			if (segment + 1 == menuPath.segmentCount()) {
				return menu;
			} else {
				return null;
			}
		}
		return findMenuFromPath(item, menuPath, segment + 1);
	}
}
