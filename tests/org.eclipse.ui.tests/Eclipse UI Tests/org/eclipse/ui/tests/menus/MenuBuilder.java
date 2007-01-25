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

package org.eclipse.ui.tests.menus;

import java.util.List;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.menus.IWorkbenchWidget;
import org.eclipse.ui.menus.WidgetContributionItem;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.tests.api.workbenchpart.TextWidget;
import org.eclipse.ui.tests.commands.ActiveActionSetExpression;

/**
 * @since 3.3
 * 
 */
public class MenuBuilder {
	private static AbstractContributionFactory viewMenuAddition = null;

	private static AbstractContributionFactory viewToolbarAddition = null;

	public static void addMenuContribution() {
		if (!PlatformUI.isWorkbenchRunning()) {
			return;
		}
		IMenuService menuService = (IMenuService) PlatformUI.getWorkbench()
				.getService(IMenuService.class);
		viewMenuAddition = new AbstractContributionFactory(
				"menu:org.eclipse.ui.tests.api.MenuTestHarness?after=additions") {
			public void createContributionItems(IMenuService menuService,
					List additions) {
				CommandContributionItem item = new CommandContributionItem(
						"org.eclipse.ui.tests.menus.itemX20",
						"org.eclipse.ui.tests.menus.enabledWorld", null, null,
						null, null, "Item X20", null, null,
						CommandContributionItem.STYLE_PUSH);
				additions.add(item);

				MenuManager submenu = new MenuManager("Menu X21",
						"org.eclipse.ui.tests.menus.menuX21");
				item = new CommandContributionItem(
						"org.eclipse.ui.tests.menus.itemX22",
						"org.eclipse.ui.tests.menus.updateWorld", null, null,
						null, null, "Item X22", null, null,
						CommandContributionItem.STYLE_PUSH);
				submenu.add(item);
				item = new CommandContributionItem(
						"org.eclipse.ui.tests.menus.itemX23",
						"org.eclipse.ui.tests.menus.enabledWorld", null, null,
						null, null, "Item X23", null, null,
						CommandContributionItem.STYLE_PUSH);
				submenu.add(item);

				additions.add(submenu);

				item = new CommandContributionItem(
						"org.eclipse.ui.tests.menus.itemX24",
						"org.eclipse.ui.tests.menus.enabledWorld", null, null,
						null, null, "Item X24", null, null,
						CommandContributionItem.STYLE_PUSH);
				additions.add(item);
			}

			public void releaseContributionItems(IMenuService menuService,
					List items) {
				// for us this is a no-op
			}
		};
		menuService.addContributionFactory(viewMenuAddition);

		viewToolbarAddition = new AbstractContributionFactory(
				"toolbar:org.eclipse.ui.tests.api.MenuTestHarness") {
			public void createContributionItems(IMenuService menuService,
					List additions) {
				CommandContributionItem item = new CommandContributionItem(
						"org.eclipse.ui.tests.menus.itemX25",
						"org.eclipse.ui.tests.menus.updateWorld", null, null,
						null, null, "Item X25", null, null,
						CommandContributionItem.STYLE_PUSH);
				additions.add(item);
				WidgetContributionItem widget = new WidgetContributionItem(
						"org.eclipse.ui.tests.menus.itemX26") {

					public IWorkbenchWidget createWidget() {
						return new TextWidget();
					}
				};
				additions.add(widget);
			}

			public void releaseContributionItems(IMenuService menuService,
					List items) {
				// for us this is a no-op
			}
		};
		menuService.addContributionFactory(viewToolbarAddition);
	}

	public static void removeMenuContribution() {
		if (!PlatformUI.isWorkbenchRunning()) {
			return;
		}
		IMenuService menuService = (IMenuService) PlatformUI.getWorkbench()
				.getService(IMenuService.class);
		menuService.removeContributionFactory(viewMenuAddition);
		viewMenuAddition = null;
		menuService.removeContributionFactory(viewToolbarAddition);
		viewMenuAddition = null;
	}

	public static void addSearchMenu() {
		IMenuService menuService = (IMenuService) PlatformUI.getWorkbench()
				.getService(IMenuService.class);

		AbstractContributionFactory searchContribution = new AbstractContributionFactory(
				"menu:org.eclipse.ui.main.menu?after=navigate") {
			public void createContributionItems(IMenuService menuService,
					List additions) {
				MenuManager search = new MenuManager("Se&arch",
						"org.eclipse.search.menu");

				search.add(new GroupMarker("internalDialogGroup"));
				search.add(new GroupMarker("dialogGroup"));
				search.add(new Separator("fileSearchContextMenuActionsGroup"));
				search.add(new Separator("contextMenuActionsGroup"));
				search.add(new Separator("occurencesActionsGroup"));
				search.add(new Separator("extraSearchGroup"));

				additions.add(search);
			}

			public void releaseContributionItems(IMenuService menuService,
					List items) {
				// nothing to do here
			}
		};

		menuService.addContributionFactory(searchContribution);
	}

	public static void addToSearchMenu() {
		final IMenuService menuService = (IMenuService) PlatformUI
				.getWorkbench().getService(IMenuService.class);
		final ActiveActionSetExpression activeSearchActionSet = new ActiveActionSetExpression(
				"org.eclipse.jdt.ui.SearchActionSet");

		final ImageDescriptor searchIcon = AbstractUIPlugin
				.imageDescriptorFromPlugin("org.eclise.ui.tests",
						"icons/full/obj16/jsearch_obj.gif");
		AbstractContributionFactory factory = new AbstractContributionFactory(
				"menu:org.eclipse.search.menu?after=dialogGroup") {
			public void createContributionItems(IMenuService menuService,
					List additions) {
				CommandContributionItem item = new CommandContributionItem(
						"org.eclipse.jdt.internal.ui.search.openJavaSearchPage",
						"org.eclipse.jdt.internal.ui.search.openJavaSearchPage",
						null, searchIcon, null, null, null, null, null,
						CommandContributionItem.STYLE_PUSH);
				menuService.registerVisibleWhen(item, activeSearchActionSet);
				additions.add(item);
			}

			public void releaseContributionItems(IMenuService menuService,
					List items) {
			}
		};
		menuService.addContributionFactory(factory);

		factory = new AbstractContributionFactory(
				"menu:org.eclipse.search.menu?after=contextMenuActionsGroup") {
			public void createContributionItems(IMenuService menuService,
					List additions) {
				MenuManager readMenu = new MenuManager("&Read Access",
						"readAccessSubMenu");
				menuService
						.registerVisibleWhen(readMenu, activeSearchActionSet);
				additions.add(readMenu);

				readMenu.add(new GroupMarker("group1"));

				CommandContributionItem item = new CommandContributionItem(
						"org.eclipse.jdt.ui.edit.text.java.search.read.access.in.workspace",
						"org.eclipse.jdt.ui.edit.text.java.search.read.access.in.workspace",
						null, null, null, null, null, "W", null,
						CommandContributionItem.STYLE_PUSH);
				readMenu.add(item);
				item = new CommandContributionItem(
						"org.eclipse.jdt.ui.edit.text.java.search.read.access.in.project",
						"org.eclipse.jdt.ui.edit.text.java.search.read.access.in.project",
						null, null, null, null, null, "P", null,
						CommandContributionItem.STYLE_PUSH);
				readMenu.add(item);
				item = new CommandContributionItem(
						"org.eclipse.jdt.ui.edit.text.java.search.read.access.in.hierarchy",
						"org.eclipse.jdt.ui.edit.text.java.search.read.access.in.hierarchy",
						null, null, null, null, null, "H", null,
						CommandContributionItem.STYLE_PUSH);
				readMenu.add(item);
				item = new CommandContributionItem(
						"org.eclipse.jdt.ui.edit.text.java.search.read.access.in.working.set",
						"org.eclipse.jdt.ui.edit.text.java.search.read.access.in.working.set",
						null, null, null, null, null, "S", null,
						CommandContributionItem.STYLE_PUSH);
				readMenu.add(item);

				MenuManager writeMenu = new MenuManager("&Write Access",
						"writeAccessSubMenu");
				menuService.registerVisibleWhen(writeMenu,
						activeSearchActionSet);
				additions.add(writeMenu);

				writeMenu.add(new GroupMarker("group1"));

				item = new CommandContributionItem(
						"org.eclipse.jdt.ui.edit.text.java.search.write.access.in.workspace",
						"org.eclipse.jdt.ui.edit.text.java.search.write.access.in.workspace",
						null, null, null, null, null, "W", null,
						CommandContributionItem.STYLE_PUSH);
				writeMenu.add(item);
				item = new CommandContributionItem(
						"org.eclipse.jdt.ui.edit.text.java.search.write.access.in.project",
						"org.eclipse.jdt.ui.edit.text.java.search.write.access.in.project",
						null, null, null, null, null, "P", null,
						CommandContributionItem.STYLE_PUSH);
				writeMenu.add(item);
				item = new CommandContributionItem(
						"org.eclipse.jdt.ui.edit.text.java.search.write.access.in.hierarchy",
						"org.eclipse.jdt.ui.edit.text.java.search.write.access.in.hierarchy",
						null, null, null, null, null, "H", null,
						CommandContributionItem.STYLE_PUSH);
				writeMenu.add(item);
				item = new CommandContributionItem(
						"org.eclipse.jdt.ui.edit.text.java.search.write.access.in.working.set",
						"org.eclipse.jdt.ui.edit.text.java.search.write.access.in.working.set",
						null, null, null, null, null, "S", null,
						CommandContributionItem.STYLE_PUSH);
				writeMenu.add(item);
			}

			public void releaseContributionItems(IMenuService menuService,
					List items) {
			}
		};
		menuService.addContributionFactory(factory);
	}

	private static class ObjectClassExpression extends Expression {
		public ObjectClassExpression(String c) {

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.expressions.Expression#evaluate(org.eclipse.core.expressions.IEvaluationContext)
		 */
		public EvaluationResult evaluate(IEvaluationContext context)
				throws CoreException {
			// TODO Auto-generated method stub
			return null;
		}

	}

	public static void addFileContribution() {
		final IMenuService menuService = (IMenuService) PlatformUI
				.getWorkbench().getService(IMenuService.class);
		final ObjectClassExpression ifileExpression = new ObjectClassExpression(
				"org.eclipse.core.resources.IFile");

		final ImageDescriptor postIcon = AbstractUIPlugin
				.imageDescriptorFromPlugin("org.eclise.ui.tests",
						"icons/full/elcl16/post_wiki.gif");
		final ImageDescriptor loadIcon = AbstractUIPlugin
				.imageDescriptorFromPlugin("org.eclise.ui.tests",
						"icons/full/elcl16/load_wiki.gif");
		AbstractContributionFactory factory = new AbstractContributionFactory(
				"popup:org.eclipse.ui.menus.popup.any?after=additions") {
			public void createContributionItems(IMenuService menuService,
					List additions) {
				CommandContributionItem item = new CommandContributionItem(
						"org.eclipse.ui.examples.wiki.post",
						"org.eclipse.ui.examples.wiki.post", null, postIcon,
						null, null, null, "P", null,
						CommandContributionItem.STYLE_PUSH);
				menuService.registerVisibleWhen(item, ifileExpression);
				additions.add(item);

				item = new CommandContributionItem(
						"org.eclipse.ui.examples.wiki.load",
						"org.eclipse.ui.examples.wiki.load", null, loadIcon,
						null, null, null, "L", null,
						CommandContributionItem.STYLE_PUSH);
				menuService.registerVisibleWhen(item, ifileExpression);
				additions.add(item);
			}

			public void releaseContributionItems(IMenuService menuService,
					List items) {
			}
		};
		menuService.addContributionFactory(factory);
	}

	public static void addTextMenuContribition() {
		final IMenuService menuService = (IMenuService) PlatformUI
				.getWorkbench().getService(IMenuService.class);

		final ImageDescriptor scrambleIcon = AbstractUIPlugin
				.imageDescriptorFromPlugin("org.eclise.ui.tests",
						"icons/full/eobj16/scramble.gif");
		AbstractContributionFactory factory = new AbstractContributionFactory(
				"popup:#TextEditorContext?after=additions") {
			public void createContributionItems(IMenuService menuService,
					List additions) {
				CommandContributionItem item = new CommandContributionItem(
						"org.eclipse.ui.examples.menus.scramble.text",
						"org.eclipse.ui.examples.menus.scramble.text", null,
						scrambleIcon, null, null, null, "c", null,
						CommandContributionItem.STYLE_PUSH);
				additions.add(item);
			}

			public void releaseContributionItems(IMenuService menuService,
					List items) {
			}
		};
		menuService.addContributionFactory(factory);
	}
}
