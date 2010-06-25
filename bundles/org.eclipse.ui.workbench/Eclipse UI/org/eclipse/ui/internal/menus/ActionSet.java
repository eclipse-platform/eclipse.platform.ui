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
import java.util.Collection;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MCoreExpression;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.impl.UiFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.services.EContextService;
import org.eclipse.e4.ui.workbench.swt.modeling.MenuServiceFilter;
import org.eclipse.ui.ISources;
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

	private ActiveContextExpression visibleWhen;

	public ActionSet(MApplication application, IEclipseContext appContext,
			IConfigurationElement element) {
		this.application = application;
		this.configElement = element;
	}

	public void addToModel(ArrayList<MMenuContribution> menuContributions,
			ArrayList<MToolBarContribution> toolBarContributions) {

		String idContrib = MenuHelper.getId(configElement);
		visibleWhen = new ActiveContextExpression(idContrib);

		EContextService contextService = application.getContext().get(EContextService.class);
		Context actionSetContext = contextService.getContext(idContrib);
		if (!actionSetContext.isDefined()) {
			actionSetContext.define(MenuHelper.getLabel(configElement),
					MenuHelper.getDescription(configElement), "org.eclipse.ui.contexts.actionSet"); //$NON-NLS-1$
		}

		IConfigurationElement[] menus = configElement
				.getChildren(IWorkbenchRegistryConstants.TAG_MENU);
		for (IConfigurationElement element : menus) {
			addContribution(idContrib, menuContributions, element, true);
		}

		IConfigurationElement[] actions = configElement
				.getChildren(IWorkbenchRegistryConstants.TAG_ACTION);
		for (IConfigurationElement element : actions) {
			addContribution(idContrib, menuContributions, element, false);
			addToolBarContribution(idContrib, toolBarContributions, element);
		}

		// for entertainment purposes only
		// printContributions(contributions);
	}

	static class ActiveContextExpression extends Expression {
		private String id;

		public ActiveContextExpression(String id) {
			this.id = id;
		}

		@Override
		public void collectExpressionInfo(ExpressionInfo info) {
			info.addVariableNameAccess(ISources.ACTIVE_CONTEXT_NAME);
		}

		@Override
		public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
			Object obj = context.getVariable(ISources.ACTIVE_CONTEXT_NAME);
			if (obj instanceof Collection<?>) {
				return EvaluationResult.valueOf(((Collection) obj).contains(id));
			}
			return EvaluationResult.FALSE;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ActiveContextExpression)) {
				return false;
			}
			return id.equals(((ActiveContextExpression) obj).id);
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}
	}

	private MCoreExpression createVisibleWhen() {
		MCoreExpression exp = UiFactoryImpl.eINSTANCE.createCoreExpression();
		exp.setCoreExpressionId("programmatic." + MenuHelper.getId(configElement)); //$NON-NLS-1$
		exp.setCoreExpression(visibleWhen);
		return exp;
	}

	private void addContribution(String idContrib, ArrayList<MMenuContribution> contributions,
			IConfigurationElement element, boolean isMenu) {
		MMenuContribution menuContribution = MenuFactoryImpl.eINSTANCE.createMenuContribution();
		menuContribution.setVisibleWhen(createVisibleWhen());
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
			MMenuElement action = MenuHelper.createLegacyMenuActionAdditions(application, element);
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

	private void addToolBarContribution(String idContrib,
			ArrayList<MToolBarContribution> contributions, IConfigurationElement element) {
		String tpath = MenuHelper.getToolBarPath(element);
		if (tpath == null) {
			return;
		}

		MToolBarElement action = MenuHelper
				.createLegacyToolBarActionAdditions(application, element);
		if (action != null) {
			MToolBarContribution toolBarContribution = MenuFactoryImpl.eINSTANCE
					.createToolBarContribution();
			toolBarContribution.getTags().add(MenuServiceFilter.MC_MENU);
			final String elementId = MenuHelper.getId(element);
			if (idContrib != null && idContrib.length() > 0) {
				toolBarContribution.setElementId(idContrib + "/" + elementId); //$NON-NLS-1$
			} else {
				toolBarContribution.setElementId(elementId);
			}

			// String tgroup = null;
			if (tpath != null) {
				int loc = tpath.lastIndexOf('/');
				if (loc != -1) {
					// tgroup = tpath.substring(loc + 1);
					tpath = tpath.substring(0, loc);
				}
			}
			if ((tpath != null) && tpath.equals("Normal")) { //$NON-NLS-1$
				IConfigurationElement parent = (IConfigurationElement) element.getParent();
				tpath = parent.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
			}
			Path menuPath = new Path(tpath);
			tpath = menuPath.segment(0);

			boolean found = false;
			for (MToolBarContribution contribution : contributions) {
				String contributionId = contribution.getElementId();
				if (contributionId != null && contributionId.equals(tpath)) {
					found = true;
					break;
				}
			}

			if (!found) {
				MToolBarContribution separatorContribution = MenuFactoryImpl.eINSTANCE
						.createToolBarContribution();
				separatorContribution.setElementId(tpath);
				separatorContribution.setParentId("org.eclipse.ui.main.toolbar"); //$NON-NLS-1$
				MToolBarSeparator separator = MenuFactoryImpl.eINSTANCE.createToolBarSeparator();
				separator.setElementId(tpath);
				toolBarContribution.getChildren().add(separator);
				separatorContribution.getChildren().add(separator);
				contributions.add(separatorContribution);
			}

			// String positionInParent =
			// MenuHelper.isSeparatorVisible(configElement) ? null
			//					: "after=" + tpath; //$NON-NLS-1$
			String positionInParent = "after=" + tpath;//$NON-NLS-1$
			toolBarContribution.setParentId("org.eclipse.ui.main.toolbar"); //$NON-NLS-1$

			toolBarContribution.setPositionInParent(positionInParent);
			toolBarContribution.setVisibleWhen(createVisibleWhen());

			toolBarContribution.getChildren().add(action);
			contributions.add(toolBarContribution);
		}
	}

	private void processGroups(String idContrib, ArrayList<MMenuContribution> contributions,
			IConfigurationElement element) {
		MMenuContribution menuContribution = MenuFactoryImpl.eINSTANCE.createMenuContribution();
		menuContribution.setVisibleWhen(createVisibleWhen());
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
			String tag = sepAddition.getName();
			MMenuElement sep = MenuFactoryImpl.eINSTANCE.createMenuSeparator();
			sep.setElementId(name);
			if ("groupMarker".equals(tag)) { //$NON-NLS-1$
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
