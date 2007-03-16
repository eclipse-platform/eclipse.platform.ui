/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.menus;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.tests.TestPlugin;
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
		if (menuService==null) {
			return;
		}
		viewMenuAddition = new AbstractContributionFactory(
				"menu:org.eclipse.ui.tests.api.MenuTestHarness?after=additions", TestPlugin.PLUGIN_ID) {
			public void createContributionItems(IServiceLocator serviceLocator,
					IContributionRoot additions) {
				CommandContributionItem item = new CommandContributionItem(serviceLocator,
						"org.eclipse.ui.tests.menus.itemX20",
						"org.eclipse.ui.tests.menus.enabledWorld", null, null,
						null, null, "Item X20", null, null,
						CommandContributionItem.STYLE_PUSH);
				additions.addContributionItem(item, null);

				MenuManager submenu = new MenuManager("Menu X21",
						"org.eclipse.ui.tests.menus.menuX21");
				item = new CommandContributionItem(serviceLocator,
						"org.eclipse.ui.tests.menus.itemX22",
						"org.eclipse.ui.tests.menus.updateWorld", null, null,
						null, null, "Item X22", null, null,
						CommandContributionItem.STYLE_PUSH);
				submenu.add(item);
				item = new CommandContributionItem(serviceLocator,
						"org.eclipse.ui.tests.menus.itemX23",
						"org.eclipse.ui.tests.menus.enabledWorld", null, null,
						null, null, "Item X23", null, null,
						CommandContributionItem.STYLE_PUSH);
				submenu.add(item);

				additions.addContributionItem(submenu, null);

				item = new CommandContributionItem(serviceLocator,
						"org.eclipse.ui.tests.menus.itemX24",
						"org.eclipse.ui.tests.menus.enabledWorld", null, null,
						null, null, "Item X24", null, null,
						CommandContributionItem.STYLE_PUSH);
				additions.addContributionItem(item, null);
			}
		};
		menuService.addContributionFactory(viewMenuAddition);

		viewToolbarAddition = new AbstractContributionFactory(
				"toolbar:org.eclipse.ui.tests.api.MenuTestHarness", TestPlugin.PLUGIN_ID) {
			public void createContributionItems(IServiceLocator serviceLocator,
					IContributionRoot additions) {
				CommandContributionItem item = new CommandContributionItem(serviceLocator,
						"org.eclipse.ui.tests.menus.itemX25",
						"org.eclipse.ui.tests.menus.updateWorld", null, null,
						null, null, "Item X25", null, null,
						CommandContributionItem.STYLE_PUSH);
				additions.addContributionItem(item, null);
				WorkbenchWindowControlContribution widget = new WorkbenchWindowControlContribution(
						"org.eclipse.ui.tests.menus.itemX26") {
					protected Control createControl(Composite parent) {
						Text textCtrl = new Text(parent, SWT.BORDER);
						textCtrl.setText("ABCDEFGHI");
						return textCtrl;
					}
				};
				additions.addContributionItem(widget, null);
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
		if (menuService==null) {
			return;
		}
		menuService.removeContributionFactory(viewMenuAddition);
		viewMenuAddition = null;
		menuService.removeContributionFactory(viewToolbarAddition);
		viewMenuAddition = null;
	}

	public static void addSearchMenu() {
		IMenuService menuService = (IMenuService) PlatformUI.getWorkbench()
				.getService(IMenuService.class);

		AbstractContributionFactory searchContribution = new AbstractContributionFactory(
				"menu:org.eclipse.ui.main.menu?after=navigate", TestPlugin.PLUGIN_ID) {
			public void createContributionItems(IServiceLocator menuService,
					IContributionRoot additions) {
				MenuManager search = new MenuManager("Se&arch",
						"org.eclipse.search.menu");

				search.add(new GroupMarker("internalDialogGroup"));
				search.add(new GroupMarker("dialogGroup"));
				search.add(new Separator("fileSearchContextMenuActionsGroup"));
				search.add(new Separator("contextMenuActionsGroup"));
				search.add(new Separator("occurencesActionsGroup"));
				search.add(new Separator("extraSearchGroup"));

				additions.addContributionItem(search, null);
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
				"menu:org.eclipse.search.menu?after=dialogGroup", TestPlugin.PLUGIN_ID) {
			public void createContributionItems(IServiceLocator serviceLocator,
					IContributionRoot additions) {
				CommandContributionItem item = new CommandContributionItem(serviceLocator,
						"org.eclipse.jdt.internal.ui.search.openJavaSearchPage",
						"org.eclipse.jdt.internal.ui.search.openJavaSearchPage",
						null, searchIcon, null, null, null, null, null,
						CommandContributionItem.STYLE_PUSH);
				additions.addContributionItem(item, activeSearchActionSet);
			}
		};
		menuService.addContributionFactory(factory);

		factory = new AbstractContributionFactory(
				"menu:org.eclipse.search.menu?after=contextMenuActionsGroup", TestPlugin.PLUGIN_ID) {
			public void createContributionItems(IServiceLocator serviceLocator
					,
					IContributionRoot additions) {
				MenuManager readMenu = new MenuManager("&Read Access",
						"readAccessSubMenu");
				additions.addContributionItem(readMenu, activeSearchActionSet);

				readMenu.add(new GroupMarker("group1"));

				CommandContributionItem item = new CommandContributionItem(serviceLocator,
						"org.eclipse.jdt.ui.edit.text.java.search.read.access.in.workspace",
						"org.eclipse.jdt.ui.edit.text.java.search.read.access.in.workspace",
						null, null, null, null, null, "W", null,
						CommandContributionItem.STYLE_PUSH);
				readMenu.add(item);
				item = new CommandContributionItem(serviceLocator,
						"org.eclipse.jdt.ui.edit.text.java.search.read.access.in.project",
						"org.eclipse.jdt.ui.edit.text.java.search.read.access.in.project",
						null, null, null, null, null, "P", null,
						CommandContributionItem.STYLE_PUSH);
				readMenu.add(item);
				item = new CommandContributionItem(serviceLocator,
						"org.eclipse.jdt.ui.edit.text.java.search.read.access.in.hierarchy",
						"org.eclipse.jdt.ui.edit.text.java.search.read.access.in.hierarchy",
						null, null, null, null, null, "H", null,
						CommandContributionItem.STYLE_PUSH);
				readMenu.add(item);
				item = new CommandContributionItem(serviceLocator,
						"org.eclipse.jdt.ui.edit.text.java.search.read.access.in.working.set",
						"org.eclipse.jdt.ui.edit.text.java.search.read.access.in.working.set",
						null, null, null, null, null, "S", null,
						CommandContributionItem.STYLE_PUSH);
				readMenu.add(item);

				MenuManager writeMenu = new MenuManager("&Write Access",
						"writeAccessSubMenu");
				additions.addContributionItem(writeMenu, activeSearchActionSet);

				writeMenu.add(new GroupMarker("group1"));

				item = new CommandContributionItem(serviceLocator,
						"org.eclipse.jdt.ui.edit.text.java.search.write.access.in.workspace",
						"org.eclipse.jdt.ui.edit.text.java.search.write.access.in.workspace",
						null, null, null, null, null, "W", null,
						CommandContributionItem.STYLE_PUSH);
				writeMenu.add(item);
				item = new CommandContributionItem(serviceLocator,
						"org.eclipse.jdt.ui.edit.text.java.search.write.access.in.project",
						"org.eclipse.jdt.ui.edit.text.java.search.write.access.in.project",
						null, null, null, null, null, "P", null,
						CommandContributionItem.STYLE_PUSH);
				writeMenu.add(item);
				item = new CommandContributionItem(serviceLocator,
						"org.eclipse.jdt.ui.edit.text.java.search.write.access.in.hierarchy",
						"org.eclipse.jdt.ui.edit.text.java.search.write.access.in.hierarchy",
						null, null, null, null, null, "H", null,
						CommandContributionItem.STYLE_PUSH);
				writeMenu.add(item);
				item = new CommandContributionItem(serviceLocator,
						"org.eclipse.jdt.ui.edit.text.java.search.write.access.in.working.set",
						"org.eclipse.jdt.ui.edit.text.java.search.write.access.in.working.set",
						null, null, null, null, null, "S", null,
						CommandContributionItem.STYLE_PUSH);
				writeMenu.add(item);
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
				"popup:org.eclipse.ui.menus.popup.any?after=additions", TestPlugin.PLUGIN_ID) {
			public void createContributionItems(IServiceLocator serviceLocator,
					IContributionRoot additions) {
				CommandContributionItem item = new CommandContributionItem(serviceLocator,
						"org.eclipse.ui.examples.wiki.post",
						"org.eclipse.ui.examples.wiki.post", null, postIcon,
						null, null, null, "P", null,
						CommandContributionItem.STYLE_PUSH);
				additions.addContributionItem(item, ifileExpression);

				item = new CommandContributionItem(serviceLocator,
						"org.eclipse.ui.examples.wiki.load",
						"org.eclipse.ui.examples.wiki.load", null, loadIcon,
						null, null, null, "L", null,
						CommandContributionItem.STYLE_PUSH);
				additions.addContributionItem(item, ifileExpression);
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
				"popup:#TextEditorContext?after=additions", TestPlugin.PLUGIN_ID) {
			public void createContributionItems(IServiceLocator serviceLocator,
					IContributionRoot additions) {
				CommandContributionItem item = new CommandContributionItem(serviceLocator,
						"org.eclipse.ui.examples.menus.scramble.text",
						"org.eclipse.ui.examples.menus.scramble.text", null,
						scrambleIcon, null, null, null, "c", null,
						CommandContributionItem.STYLE_PUSH);
				additions.addContributionItem(item, null);
			}
		};
		menuService.addContributionFactory(factory);
	}
}
