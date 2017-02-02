/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.internal.workbench.swt.Policy;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MCoreExpression;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.impl.UiFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.services.EContextService;
import org.eclipse.e4.ui.workbench.renderers.swt.MenuManagerRenderer;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.e4.compatibility.E4Util;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * @since e4
 *
 */
public class ActionSet {

	public static final String MAIN_TOOLBAR = "org.eclipse.ui.main.toolbar"; //$NON-NLS-1$
	public static final String MAIN_MENU = "org.eclipse.ui.main.menu"; //$NON-NLS-1$

	protected IConfigurationElement configElement;

	protected MApplication application;

	protected Expression visibleWhen;

	private HashSet<String> menuContributionGroupIds = new HashSet<>();
	private HashSet<String> toolbarContributionGroupIds = new HashSet<>();
	private String id;

	public String getId() {
		return id;
	}

	public ActionSet(MApplication application, IEclipseContext appContext,
			IConfigurationElement element) {
		this.application = application;
		this.configElement = element;
		this.id = MenuHelper.getId(configElement);
	}

	public void addToModel(ArrayList<MMenuContribution> menuContributions,
			ArrayList<MToolBarContribution> toolBarContributions,
			ArrayList<MTrimContribution> trimContributions) {

		String idContrib = MenuHelper.getId(configElement);
		visibleWhen = createExpression(configElement);

		EContextService contextService = application.getContext().get(EContextService.class);
		Context actionSetContext = contextService.getContext(idContrib);
		if (!actionSetContext.isDefined()) {
			actionSetContext.define(MenuHelper.getLabel(configElement),
					MenuHelper.getDescription(configElement), "org.eclipse.ui.contexts.actionSet"); //$NON-NLS-1$
		}

		IConfigurationElement[] menus = configElement
				.getChildren(IWorkbenchRegistryConstants.TAG_MENU);
		if (menus.length > 0) {
			for (int i = menus.length; i > 0; i--) {
				IConfigurationElement element = menus[i - 1];
				addContribution(idContrib, menuContributions, element, true, MAIN_MENU);
			}

		}

		IConfigurationElement[] actions = configElement
				.getChildren(IWorkbenchRegistryConstants.TAG_ACTION);
		if (actions.length > 0) {
			for (int i = 0; i < actions.length; i++) {
				IConfigurationElement up = actions[i];
				IConfigurationElement down = actions[actions.length - 1 - i];
				addContribution(idContrib, menuContributions, down, false, MAIN_MENU);
				addToolBarContribution(idContrib, toolBarContributions, trimContributions, up,
						MAIN_TOOLBAR);
			}
		}
	}

	protected Expression createExpression(IConfigurationElement configElement) {
		String actionSetId = MenuHelper.getId(configElement);
		Set<String> associatedPartIds = actionSetPartAssociations(actionSetId);
		return new ActionSetAndPartExpression(actionSetId, associatedPartIds);
	}

	private Set<String> actionSetPartAssociations(String actionSetId) {
		HashSet<String> result = new HashSet<>();
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		final IConfigurationElement[] associations = registry
				.getConfigurationElementsFor(PlatformUI.PLUGIN_ID + '.'
						+ IWorkbenchRegistryConstants.PL_ACTION_SET_PART_ASSOCIATIONS);
		for (IConfigurationElement element : associations) {
			String targetId = element.getAttribute(IWorkbenchRegistryConstants.ATT_TARGET_ID);
			if (!actionSetId.equals(targetId)) {
				continue;
			}
			IConfigurationElement[] children = element
					.getChildren(IWorkbenchRegistryConstants.TAG_PART);
			for (IConfigurationElement part : children) {
				String id = MenuHelper.getId(part);
				if (id != null && id.length() > 0) {
					if (Policy.DEBUG_MENUS) {
						MenuHelper.trace(IWorkbenchRegistryConstants.PL_ACTION_SET_PART_ASSOCIATIONS + ':' + actionSetId
								+ ':' + id, null);
					}
					result.add(id);
				}
			}
		}
		return result;
	}

	static class ActionSetAndPartExpression extends Expression {
		private String id;
		private Set<String> partIds;

		public ActionSetAndPartExpression(String id, Set<String> associatedPartIds) {
			this.id = id;
			this.partIds = associatedPartIds;
		}

		@Override
		public void collectExpressionInfo(ExpressionInfo info) {
			info.addVariableNameAccess(ISources.ACTIVE_CONTEXT_NAME);
			info.addVariableNameAccess(ISources.ACTIVE_PART_ID_NAME);
		}

		@Override
		public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
			Object obj = context.getVariable(ISources.ACTIVE_CONTEXT_NAME);
			if (obj instanceof Collection<?>) {
				boolean rc = ((Collection) obj).contains(id);
				if (rc) {
					return EvaluationResult.TRUE;
				}
			}
			if (!partIds.isEmpty()) {
				return EvaluationResult.valueOf(partIds.contains(context
						.getVariable(ISources.ACTIVE_PART_ID_NAME)));
			}
			return EvaluationResult.FALSE;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ActionSetAndPartExpression)) {
				return false;
			}
			ActionSetAndPartExpression exp = (ActionSetAndPartExpression) obj;
			return id.equals(exp.id) && partIds.equals(exp.partIds);
		}

		@Override
		public int hashCode() {
			return id.hashCode() + partIds.hashCode();
		}
	}

	private MCoreExpression createVisibleWhen() {
		if (visibleWhen == null) {
			return null;
		}
		MCoreExpression exp = UiFactoryImpl.eINSTANCE.createCoreExpression();
		exp.setCoreExpressionId("programmatic." + MenuHelper.getId(configElement)); //$NON-NLS-1$
		exp.setCoreExpression(visibleWhen);
		return exp;
	}

	protected void addContribution(String idContrib, ArrayList<MMenuContribution> contributions,
			IConfigurationElement element, boolean isMenu, String parentId) {
		MMenuContribution menuContribution = MenuFactoryImpl.eINSTANCE.createMenuContribution();
		menuContribution.setVisibleWhen(createVisibleWhen());
		menuContribution.getTags().add(ContributionsAnalyzer.MC_MENU);
		menuContribution.getTags().add("scheme:menu"); //$NON-NLS-1$
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
		if (path.endsWith("/")) { //$NON-NLS-1$
			path += IWorkbenchActionConstants.MB_ADDITIONS;
		}
		Path menuPath = new Path(path);
		String positionInParent = "after=" + menuPath.segment(0); //$NON-NLS-1$
		int segmentCount = menuPath.segmentCount();
		if (segmentCount > 1) {
			parentId = menuPath.segment(segmentCount - 2);
			positionInParent = "after=" + menuPath.segment(segmentCount - 1); //$NON-NLS-1$
		}
		menuContribution.setParentId(parentId);
		menuContribution.setPositionInParent(positionInParent);
		if (isMenu) {
			MMenu menu = MenuHelper.createMenuAddition(element);
			if (menu != null) {
				contributeMenuGroup(contributions, parentId, positionInParent);
				menuContribution.getChildren().add(menu);
				menu.getTransientData().put("ActionSet", id); //$NON-NLS-1$
			}
		} else {
			if (parentId.equals(MAIN_MENU)) {
				E4Util.unsupported("****MC: bad pie: " + menuPath); //$NON-NLS-1$
				parentId = IWorkbenchActionConstants.M_WINDOW;
				menuContribution.setParentId(parentId);
			}
			MMenuElement action = MenuHelper.createLegacyMenuActionAdditions(application, element);
			if (action != null) {
				contributeMenuGroup(contributions, parentId, positionInParent);
				menuContribution.getChildren().add(action);
				action.getTransientData().put("ActionSet", id); //$NON-NLS-1$
			}
		}
		if (menuContribution.getChildren().size() > 0) {
			contributions.add(menuContribution);
		}
		if (isMenu) {
			processGroups(idContrib, contributions, element);
		}
	}

	private void contributeMenuGroup(ArrayList<MMenuContribution> contributions, String parentId,
			String positionInParent) {
		String group = positionInParent.substring(6);
		if (menuContributionGroupIds.contains(parentId + group)) {
			return;
		}
		menuContributionGroupIds.add(parentId + group);
		MMenuContribution menuContribution = MenuFactoryImpl.eINSTANCE.createMenuContribution();
		menuContribution.setVisibleWhen(createVisibleWhen());
		menuContribution.getTags().add(ContributionsAnalyzer.MC_MENU);
		menuContribution.getTags().add("scheme:menu"); //$NON-NLS-1$
		menuContribution.setParentId(parentId);
		menuContribution.setPositionInParent("after=additions"); //$NON-NLS-1$
		MMenuElement sep = MenuFactoryImpl.eINSTANCE.createMenuSeparator();
		sep.getTags().add(MenuManagerRenderer.GROUP_MARKER);
		sep.setVisible(false);
		sep.setElementId(group);
		menuContribution.getChildren().add(sep);
		contributions.add(menuContribution);
	}

	private void contributeToolBarGroup(ArrayList<MToolBarContribution> contributions,
			String parentId, String group) {
		if (toolbarContributionGroupIds.contains(parentId + group)) {
			return;
		}
		toolbarContributionGroupIds.add(parentId + group);
		MToolBarContribution toolBarContribution = MenuFactoryImpl.eINSTANCE
				.createToolBarContribution();
		toolBarContribution.getTags().add(ContributionsAnalyzer.MC_MENU);
		toolBarContribution.getTags().add("scheme:toolbar"); //$NON-NLS-1$
		toolBarContribution.setParentId(parentId);
		toolBarContribution.setPositionInParent("after=additions"); //$NON-NLS-1$
		MToolBarSeparator sep = MenuFactoryImpl.eINSTANCE.createToolBarSeparator();
		sep.setElementId(group);
		sep.setVisible(false);
		toolBarContribution.getChildren().add(sep);
		contributions.add(toolBarContribution);
	}

	protected void addToolBarContribution(String idContrib,
			ArrayList<MToolBarContribution> contributions,
			ArrayList<MTrimContribution> trimContributions, IConfigurationElement element,
			String parentId) {
		String tpath = MenuHelper.getToolBarPath(element);
		if (tpath == null || isEditorAction(element)) {
			return;
		}

		if (tpath.endsWith("/")) { //$NON-NLS-1$
			tpath += IWorkbenchActionConstants.MB_ADDITIONS;
		}

		MToolBarElement action = MenuHelper
				.createLegacyToolBarActionAdditions(application, element);
		if (action == null) {
			return;
		}

		action.getTransientData().put("Name", MenuHelper.getLabel(element)); //$NON-NLS-1$
		action.getTransientData().put("ActionSet", id); //$NON-NLS-1$

		MToolBarContribution toolBarContribution = MenuFactoryImpl.eINSTANCE
				.createToolBarContribution();
		toolBarContribution.getTags().add(ContributionsAnalyzer.MC_MENU);
		toolBarContribution.getTags().add("scheme:toolbar"); //$NON-NLS-1$
		final String elementId = MenuHelper.getId(element);
		if (idContrib != null && idContrib.length() > 0) {
			toolBarContribution.setElementId(idContrib + "/" + elementId); //$NON-NLS-1$
			toolBarContribution.getTags().add("ActionSet::" + idContrib); //$NON-NLS-1$
		} else {
			toolBarContribution.setElementId(elementId);
		}

		String tgroup = null;
		if (tpath != null) {
			int loc = tpath.lastIndexOf('/');
			if (loc != -1) {
				tgroup = tpath.substring(loc + 1);
				tpath = tpath.substring(0, loc);
			} else {
				tgroup = tpath;
				tpath = null;
			}
		}
		if (tpath == null || tpath.equals("Normal")) { //$NON-NLS-1$
			IConfigurationElement parent = (IConfigurationElement) element.getParent();
			tpath = parent.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
		}
		Path menuPath = new Path(tpath);
		tpath = menuPath.segment(0);

		if (MAIN_TOOLBAR.equals(parentId)) {
			addTrimContribution(idContrib, contributions, trimContributions, element, parentId,
					tpath, tgroup);
		} else {
			tpath = parentId;
		}

		// String positionInParent =
		// MenuHelper.isSeparatorVisible(configElement) ? null
		//					: "after=" + tpath; //$NON-NLS-1$
		String positionInParent = "after=" + tgroup;//$NON-NLS-1$
		contributeToolBarGroup(contributions, tpath, tgroup);
		toolBarContribution.setParentId(tpath);

		toolBarContribution.setPositionInParent(positionInParent);
		toolBarContribution.setVisibleWhen(createVisibleWhen());
		toolBarContribution.getChildren().add(action);

		contributions.add(toolBarContribution);
	}

	private boolean isEditorAction(IConfigurationElement element) {
		return IWorkbenchRegistryConstants.EXTENSION_EDITOR_ACTIONS.equals(element
				.getDeclaringExtension().getExtensionPointUniqueIdentifier());
	}

	private void addTrimContribution(String idContrib,
			ArrayList<MToolBarContribution> contributions,
			ArrayList<MTrimContribution> trimContributions, IConfigurationElement element,
			String parentId, String tpath, String tgroup) {

		final String elementId = MenuHelper.getId(element);
		MTrimContribution trimContribution = MenuFactoryImpl.eINSTANCE.createTrimContribution();
		trimContribution.getTags().add(ContributionsAnalyzer.MC_TOOLBAR);
		trimContribution.getTags().add("scheme:toolbar"); //$NON-NLS-1$
		if (idContrib != null && idContrib.length() > 0) {
			trimContribution.setElementId(idContrib + "/" + elementId); //$NON-NLS-1$
		} else {
			trimContribution.setElementId(elementId);
		}
		trimContribution.setParentId(parentId);
		trimContribution.setPositionInParent("after=additions"); //$NON-NLS-1$		trimContribution.setVisibleWhen(createVisibleWhen());
		MToolBar tb = MenuFactoryImpl.eINSTANCE.createToolBar();
		tb.setElementId(tpath);
		tb.getTransientData().put("Name", MenuHelper.getLabel(this.configElement)); //$NON-NLS-1$
		tb.getTransientData().put("ActionSet", id); //$NON-NLS-1$
		trimContribution.getChildren().add(tb);
		trimContributions.add(trimContribution);
	}

	private void processGroups(String idContrib, ArrayList<MMenuContribution> contributions,
			IConfigurationElement element) {
		MMenuContribution menuContribution = MenuFactoryImpl.eINSTANCE.createMenuContribution();
		menuContribution.setVisibleWhen(createVisibleWhen());
		menuContribution.getTags().add(ContributionsAnalyzer.MC_MENU);
		menuContribution.getTags().add("scheme:menu"); //$NON-NLS-1$
		final String elementId = MenuHelper.getId(element);
		if (idContrib != null && idContrib.length() > 0) {
			menuContribution.setElementId(idContrib + "/" + elementId + ".groups"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			menuContribution.setElementId(elementId + ".groups"); //$NON-NLS-1$
		}
		menuContribution.setParentId(elementId);
		menuContribution.setPositionInParent("after=additions"); //$NON-NLS-1$
		IConfigurationElement[] children = element.getChildren();
		for (IConfigurationElement sepAddition : children) {
			String name = sepAddition.getAttribute(IWorkbenchRegistryConstants.ATT_NAME);
			String tag = sepAddition.getName();
			MMenuElement sep = MenuFactoryImpl.eINSTANCE.createMenuSeparator();
			sep.setElementId(name);
			if ("groupMarker".equals(tag)) { //$NON-NLS-1$
				sep.setVisible(false);
				sep.getTags().add(MenuManagerRenderer.GROUP_MARKER);
			}
			menuContribution.getChildren().add(sep);
		}
		if (menuContribution.getChildren().size() > 0) {
			contributions.add(menuContribution);
		}
	}

	MElementContainer<MMenuElement> findMenuFromPath(MElementContainer<MMenuElement> menu,
			Path menuPath, int segment) {
		int idx = ContributionsAnalyzer.indexForId(menu, menuPath.segment(segment));
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
