/*******************************************************************************
 * Copyright (c) 2012, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import java.util.ArrayList;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.renderers.swt.ContributionRecord;
import org.eclipse.e4.ui.workbench.renderers.swt.ToolBarContributionRecord;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * @since 3.102.0
 */
public class MenuFactoryGenerator {
	private MApplication application;
	private IConfigurationElement configElement;
	private MenuLocationURI location;

	public MenuFactoryGenerator(MApplication application, IEclipseContext appContext,
			IConfigurationElement configElement, String attribute) {
		this.application = application;
		// this.appContext = appContext;
		assert appContext.equals(this.application.getContext());
		this.configElement = configElement;
		this.location = new MenuLocationURI(attribute);
	}

	private boolean inToolbar() {
		return location.getScheme().startsWith("toolbar"); //$NON-NLS-1$
	}

	public void mergeIntoModel(ArrayList<MMenuContribution> menuContributions,
			ArrayList<MToolBarContribution> toolBarContributions) {
		if (location.getPath() == null || location.getPath().isEmpty()) {
			WorkbenchPlugin.log("MenuFactoryGenerator.mergeIntoModel: Invalid menu URI: " + location); //$NON-NLS-1$
			return;
		}
		if (inToolbar()) {
			if (MenuAdditionCacheEntry.isInWorkbenchTrim(location)) {
				// processTrimChildren(trimContributions, toolBarContributions,
				// configElement);
			} else {
				String query = location.getQuery();
				if (query == null || query.isEmpty()) {
					query = "after=additions"; //$NON-NLS-1$
				}
				processToolbarChildren(toolBarContributions, configElement, location.getPath(), query);
			}
			return;
		}
		MMenuContribution menuContribution = MenuFactoryImpl.eINSTANCE.createMenuContribution();
		menuContribution.getPersistedState().put(IWorkbench.PERSIST_STATE, Boolean.FALSE.toString());
		String idContrib = MenuHelper.getId(configElement);
		if (idContrib != null && idContrib.length() > 0) {
			menuContribution.setElementId(idContrib);
		}
		if ("org.eclipse.ui.popup.any".equals(location.getPath())) { //$NON-NLS-1$
			menuContribution.setParentId("popup"); //$NON-NLS-1$
		} else {
			menuContribution.setParentId(location.getPath());
		}
		String query = location.getQuery();
		if (query == null || query.isEmpty()) {
			query = "after=additions"; //$NON-NLS-1$
		}
		menuContribution.setPositionInParent(query);
		menuContribution.getTags().add("scheme:" + location.getScheme()); //$NON-NLS-1$
		String filter = ContributionsAnalyzer.MC_MENU;
		if ("popup".equals(location.getScheme())) { //$NON-NLS-1$
			filter = ContributionsAnalyzer.MC_POPUP;
		}
		menuContribution.getTags().add(filter);
		menuContribution.setVisibleWhen(MenuHelper.getVisibleWhen(configElement));
		ContextFunction generator = new ContributionFactoryGenerator(configElement, 0);
		menuContribution.getTransientData().put(ContributionRecord.FACTORY, generator);
		menuContributions.add(menuContribution);
	}

	private void processToolbarChildren(ArrayList<MToolBarContribution> contributions, IConfigurationElement toolbar,
			String parentId, String position) {
		MToolBarContribution toolBarContribution = MenuFactoryImpl.eINSTANCE.createToolBarContribution();
		toolBarContribution.getPersistedState().put(IWorkbench.PERSIST_STATE, Boolean.FALSE.toString());
		String idContrib = MenuHelper.getId(toolbar);
		if (idContrib != null && idContrib.length() > 0) {
			toolBarContribution.setElementId(idContrib);
		}
		toolBarContribution.setParentId(parentId);
		toolBarContribution.setPositionInParent(position);
		toolBarContribution.getTags().add("scheme:" + location.getScheme()); //$NON-NLS-1$

		ContextFunction generator = new ContributionFactoryGenerator(configElement, 1);
		toolBarContribution.getTransientData().put(ToolBarContributionRecord.FACTORY, generator);

		contributions.add(toolBarContribution);
	}
}
