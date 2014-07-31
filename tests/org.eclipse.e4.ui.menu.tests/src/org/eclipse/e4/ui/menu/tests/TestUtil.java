/*******************************************************************************
 * Copyright (c) 2011, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.menu.tests;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.commands.contexts.Context;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.UIEventPublisher;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.internal.workbench.swt.CSSRenderingUtils;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsFactoryImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.services.EContextService;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.IResourceUtilities;
import org.eclipse.e4.ui.workbench.renderers.swt.MenuManagerRenderer;
import org.eclipse.e4.ui.workbench.renderers.swt.ToolBarManagerRenderer;
import org.eclipse.e4.ui.workbench.swt.factories.IRendererFactory;
import org.eclipse.e4.ui.workbench.swt.util.ISWTResourceUtilities;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.equinox.log.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.internal.commands.CommandImageManager;
import org.eclipse.ui.internal.commands.CommandImageService;
import org.osgi.framework.ServiceReference;

class TestUtil {
	public static final String ORG_ECLIPSE_UI_CONTEXTS_ACTION_SET = "org.eclipse.ui.contexts.actionSet";

	public static void printContributions(MApplication application) {
		for (MMenuContribution mc : application.getMenuContributions()) {
			System.out.print("\n\nMC: " + mc.getParentId() + "?"
					+ mc.getPositionInParent());
			printMenuOut(1, mc);
		}

		for (MToolBarContribution tbc : application.getToolBarContributions()) {
			System.out.print("\n\nTC: " + tbc.getParentId() + "?"
					+ tbc.getPositionInParent());
			printToolOut(1, tbc);
		}
	}

	public static void printIcon(int level, MUILabel item) {
		printTabs(level + 2);
		System.out.print("icon: " + item.getIconURI());
	}

	public static void printMenuOut(int level,
			MElementContainer<MMenuElement> container) {
		for (MMenuElement child : container.getChildren()) {
			printTabs(level);
			System.out.print(child.getClass().getSimpleName() + ": "
					+ child.getElementId());
			if (child instanceof MMenu) {
				printMenuOut(level + 1, (MElementContainer<MMenuElement>) child);
			} else if (child instanceof MHandledMenuItem) {
				System.out.print(": cmd "
						+ ((MHandledMenuItem) child).getCommand()
								.getElementId());
				printIcon(level, child);
			} else if (child instanceof MDirectMenuItem) {
				System.out.print(": cmd "
						+ ((MDirectMenuItem) child).getContributionURI());
				printIcon(level, child);
			} else if (child instanceof MMenuSeparator) {
				System.out.print(": label "
						+ ((MMenuSeparator) child).getLabel());
			}
		}
	}

	public static void printTabs(int level) {
		System.out.print("\n");
		for (int i = 0; i < level; i++) {
			System.out.print("   ");
		}
	}

	public static void printToolOut(int level, MToolBarContribution tbc) {
		for (MToolBarElement child : tbc.getChildren()) {
			printTabs(level);
			System.out.print(child.getClass().getSimpleName() + ": "
					+ child.getElementId());
			if (child instanceof MDirectToolItem) {
				System.out.print(": cmd "
						+ ((MDirectToolItem) child).getContributionURI());
				printIcon(level, (MUILabel) child);
			} else if (child instanceof MHandledToolItem) {
				System.out.print(": cmd "
						+ ((MHandledToolItem) child).getCommand()
								.getElementId());
				printIcon(level, (MUILabel) child);
			} else if (child instanceof MToolBarSeparator) {
				System.out.print(": separator ");
			}
		}
	}

	public static void setupActionBuilderStructure(MMenu menuBar) {
		MMenu file = MenuFactoryImpl.eINSTANCE.createMenu();
		file.setElementId("file");
		file.setLabel("&File");
		menuBar.getChildren().add(file);

		MDirectMenuItem item = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();
		item.setElementId("refresh");
		item.setLabel("Re&fresh");
		file.getChildren().add(item);

		item = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();
		item.setElementId("exit");
		item.setLabel("&Exit");
		file.getChildren().add(item);

		MMenu edit = MenuFactoryImpl.eINSTANCE.createMenu();
		edit.setElementId("edit");
		edit.setLabel("&Edit");
		menuBar.getChildren().add(edit);

		item = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();
		item.setElementId("cut");
		item.setLabel("Cu&t");
		edit.getChildren().add(item);

		item = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();
		item.setElementId("copy");
		item.setLabel("&Copy");
		edit.getChildren().add(item);

		item = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();
		item.setElementId("paste");
		item.setLabel("&Paste");
		edit.getChildren().add(item);

		MMenuSeparator sep = MenuFactoryImpl.eINSTANCE.createMenuSeparator();
		sep.setElementId("copy.ext");
		sep.setVisible(false);
		edit.getChildren().add(sep);

		sep = MenuFactoryImpl.eINSTANCE.createMenuSeparator();
		sep.setElementId("additions");
		sep.setVisible(false);
		menuBar.getChildren().add(sep);

		MMenu window = MenuFactoryImpl.eINSTANCE.createMenu();
		window.setElementId("window");
		window.setLabel("&Window");
		menuBar.getChildren().add(window);

		item = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();
		item.setElementId("newWindow");
		item.setLabel("&New Window");
		window.getChildren().add(item);

		item = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();
		item.setElementId("preferences");
		item.setLabel("&Preferences");
		window.getChildren().add(item);
	}

	public static void setupActionBuilderStructure(MTrimBar coolbar) {
		MToolBar groupFile = MenuFactoryImpl.eINSTANCE.createToolBar();
		groupFile.setElementId("group.file");
		groupFile.setVisible(false);

		coolbar.getChildren().add(groupFile);

		MToolBar fileToolBar = MenuFactoryImpl.eINSTANCE.createToolBar();
		fileToolBar.setElementId(IWorkbenchActionConstants.TOOLBAR_FILE);

		MToolBarSeparator sep = MenuFactoryImpl.eINSTANCE
				.createToolBarSeparator();
		sep.setElementId(IWorkbenchActionConstants.NEW_GROUP);
		fileToolBar.getChildren().add(sep);
		// fileToolBar.add(newWizardDropDownAction);
		sep = MenuFactoryImpl.eINSTANCE.createToolBarSeparator();
		sep.setElementId(IWorkbenchActionConstants.SAVE_GROUP);
		sep.setVisible(false);
		fileToolBar.getChildren().add(sep);
		sep = MenuFactoryImpl.eINSTANCE.createToolBarSeparator();
		sep.setElementId(IWorkbenchActionConstants.NEW_EXT);
		sep.setVisible(false);
		fileToolBar.getChildren().add(sep);

		MDirectToolItem item = MenuFactoryImpl.eINSTANCE.createDirectToolItem();
		item.setElementId("save");
		item.setLabel("S&ave");
		fileToolBar.getChildren().add(item);

		item = MenuFactoryImpl.eINSTANCE.createDirectToolItem();
		item.setElementId("saveAll");
		item.setLabel("Sa&ve All");
		fileToolBar.getChildren().add(item);

		sep = MenuFactoryImpl.eINSTANCE.createToolBarSeparator();
		sep.setElementId(IWorkbenchActionConstants.SAVE_EXT);
		sep.setVisible(false);
		fileToolBar.getChildren().add(sep);

		item = MenuFactoryImpl.eINSTANCE.createDirectToolItem();
		item.setElementId("print");
		item.setLabel("&Print");
		fileToolBar.getChildren().add(item);

		sep = MenuFactoryImpl.eINSTANCE.createToolBarSeparator();
		sep.setElementId(IWorkbenchActionConstants.PRINT_EXT);
		sep.setVisible(false);
		fileToolBar.getChildren().add(sep);
		sep = MenuFactoryImpl.eINSTANCE.createToolBarSeparator();
		sep.setElementId(IWorkbenchActionConstants.BUILD_GROUP);
		fileToolBar.getChildren().add(sep);
		sep = MenuFactoryImpl.eINSTANCE.createToolBarSeparator();
		sep.setElementId(IWorkbenchActionConstants.BUILD_EXT);
		sep.setVisible(false);
		fileToolBar.getChildren().add(sep);
		sep = MenuFactoryImpl.eINSTANCE.createToolBarSeparator();
		sep.setElementId(IWorkbenchActionConstants.MB_ADDITIONS);
		fileToolBar.getChildren().add(sep);

		coolbar.getChildren().add(fileToolBar);

		MToolBar add = MenuFactoryImpl.eINSTANCE.createToolBar();
		add.setElementId(IWorkbenchActionConstants.MB_ADDITIONS);
		add.setVisible(false);

		coolbar.getChildren().add(add);

		MToolBar groupNav = MenuFactoryImpl.eINSTANCE.createToolBar();
		groupNav.setElementId("group.nav");
		groupNav.setVisible(false);

		coolbar.getChildren().add(groupNav);

		MToolBar navToolBar = MenuFactoryImpl.eINSTANCE.createToolBar();
		navToolBar.setElementId(IWorkbenchActionConstants.TOOLBAR_NAVIGATE);

		sep = MenuFactoryImpl.eINSTANCE.createToolBarSeparator();
		sep.setElementId(IWorkbenchActionConstants.HISTORY_GROUP);
		navToolBar.getChildren().add(sep);

		sep = MenuFactoryImpl.eINSTANCE.createToolBarSeparator();
		sep.setElementId(IWorkbenchActionConstants.GROUP_APP);
		sep.setVisible(false);
		navToolBar.getChildren().add(sep);

		item = MenuFactoryImpl.eINSTANCE.createDirectToolItem();
		item.setElementId("backwardHistory");
		item.setLabel("Backward");
		navToolBar.getChildren().add(item);

		item = MenuFactoryImpl.eINSTANCE.createDirectToolItem();
		item.setElementId("forwardHistory");
		item.setLabel("Forward");
		navToolBar.getChildren().add(item);

		sep = MenuFactoryImpl.eINSTANCE.createToolBarSeparator();
		sep.setElementId(IWorkbenchActionConstants.PIN_GROUP);
		navToolBar.getChildren().add(sep);

		coolbar.getChildren().add(navToolBar);
	}

	public static void setupCommandImageService(IEclipseContext ctx) {
		CmdService cs = ContextInjectionFactory.make(CmdService.class, ctx);
		CommandImageService service = new CommandImageService(
				new CommandImageManager(), cs);
		service.readRegistry();
		ctx.set(ICommandImageService.class, service);
	}

	public static MApplication setupRenderer(IEclipseContext appContext) {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.setContext(appContext);
		appContext.set(MApplication.class, application);

		MBindingContext rootContext = CommandsFactoryImpl.eINSTANCE
				.createBindingContext();
		rootContext.setElementId(ORG_ECLIPSE_UI_CONTEXTS_ACTION_SET);
		rootContext.setName("ActionSets");
		application.getRootContext().add(rootContext);
		EContextService ecs = appContext.get(EContextService.class);
		Context actionSet = ecs.getContext(ORG_ECLIPSE_UI_CONTEXTS_ACTION_SET);
		if (!actionSet.isDefined()) {
			actionSet.define("ActionSets", null, null);
		}

		TestUtil.setupRendererServices(appContext);

		TestUtil.setupCommandImageService(appContext);
		MTrimmedWindow window = BasicFactoryImpl.eINSTANCE
				.createTrimmedWindow();
		window.setContext(appContext.createChild("MWindowContext"));
		MMenu menuBar = MenuFactoryImpl.eINSTANCE.createMenu();
		menuBar.setElementId("org.eclipse.ui.main.menu");
		window.setMainMenu(menuBar);

		MTrimBar coolbar = BasicFactoryImpl.eINSTANCE.createTrimBar();
		coolbar.setElementId("org.eclipse.ui.main.toolbar");
		coolbar.setSide(SideValue.TOP);
		window.getTrimBars().add(coolbar);

		application.getChildren().add(window);
		application.setSelectedElement(window);
		window.getContext().activate();

		Display display = Display.getDefault();
		appContext.set(Display.class, display);

		final MenuManagerRenderer menuRenderer = new MenuManagerRenderer();
		final ToolBarManagerRenderer tbRenderer = new ToolBarManagerRenderer();
		appContext.set(IRendererFactory.class, new IRendererFactory() {
			@Override
			public AbstractPartRenderer getRenderer(MUIElement uiElement,
					Object parent) {
				if (uiElement instanceof MMenu) {
					return menuRenderer;
				} else if (uiElement instanceof MToolBar) {
					return tbRenderer;
				}
				return null;
			}
		});
		menuRenderer.init(appContext);
		ContextInjectionFactory.inject(menuRenderer, appContext);
		tbRenderer.init(appContext);
		ContextInjectionFactory.inject(tbRenderer, appContext);

		((Notifier) application).eAdapters().add(
				new UIEventPublisher(appContext));
		return application;
	}

	public static void setupRendererServices(IEclipseContext appContext) {
		appContext.set(Logger.class, new Logger() {

			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return "Fake Logger";
			}

			@Override
			public boolean isLoggable(int level) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void log(int level, String message) {
				// TODO Auto-generated method stub

			}

			@Override
			public void log(int level, String message, Throwable exception) {
				// TODO Auto-generated method stub

			}

			@Override
			public void log(Object context, int level, String message) {
				// TODO Auto-generated method stub

			}

			@Override
			public void log(Object context, int level, String message,
					Throwable exception) {
				// TODO Auto-generated method stub

			}

			@Override
			public void log(ServiceReference<?> sr, int level, String message) {
				// TODO Auto-generated method stub

			}

			@Override
			public void log(ServiceReference<?> sr, int level, String message,
					Throwable exception) {

			}
		});
		appContext.set(IResourceUtilities.class, new ISWTResourceUtilities() {

			@Override
			public Image adornImage(Image toAdorn, Image adornment) {
				return null;
			}

			@Override
			public ImageDescriptor imageDescriptorFromURI(URI iconPath) {
				try {
					return ImageDescriptor.createFromURL(new URL(iconPath
							.toString()));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
		});
		appContext.set(IPresentationEngine.class, new IPresentationEngine() {

			@Override
			public Object createGui(MUIElement element) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Object createGui(MUIElement element, Object parentWidget,
					IEclipseContext parentContext) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void focusGui(MUIElement element) {
				// TODO Auto-generated method stub

			}

			@Override
			public void removeGui(MUIElement element) {
				// TODO Auto-generated method stub

			}

			@Override
			public Object run(MApplicationElement uiRoot,
					IEclipseContext appContext) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void stop() {
				// TODO Auto-generated method stub

			}
		});
		appContext.set(CSSRenderingUtils.class, ContextInjectionFactory.make(
				CSSRenderingUtils.class, appContext));
	}

	private TestUtil() {
		// not able tp construct
	}
}
