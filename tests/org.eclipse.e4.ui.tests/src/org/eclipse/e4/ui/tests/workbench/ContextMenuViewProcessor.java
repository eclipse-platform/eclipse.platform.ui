package org.eclipse.e4.ui.tests.workbench;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsFactoryImpl;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.MCoreExpression;
import org.eclipse.e4.ui.model.application.ui.impl.UiFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.workbench.modeling.IModelExtension;
import org.eclipse.emf.ecore.EObject;

public class ContextMenuViewProcessor implements IModelExtension {

	public static final String OPEN_DIALOG_COMMAND = "org.eclipse.e4.ui.tests.contextDialog";

	public void processElement(EObject parent) {
		if (!(parent instanceof MApplication)) {
			return;
		}
		MApplication app = (MApplication) parent;

		createCommand(app);
		MPartDescriptor desc = BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		desc.setLabel("Context Menu View");
		desc.setElementId("org.eclipse.e4.ui.tests.workbench.ContextMenuView");
		desc.setCategory("org.eclipse.e4.secondaryDataStack");
		desc.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.ContextMenuView");

		MMenu menu = MenuFactoryImpl.eINSTANCE.createMenu();
		menu.getTags().add("ViewMenu");
		menu.setElementId("org.eclipse.e4.ui.tests.workbench.ContextMenuView");

		MHandledMenuItem item = MenuFactoryImpl.eINSTANCE
				.createHandledMenuItem();
		item.setElementId("e4.showView");
		item.setLabel("Show View");
		item.setCommand(findCommand(app, "org.eclipse.ui.views.showView"));
		menu.getChildren().add(item);

		// add it to the part
		desc.getMenus().add(menu);

		// context menu
		menu = createPopupMenu(app, ContextMenuView.ITEMS_MENU, "Items");
		// add it to the part
		desc.getMenus().add(menu);

		// context menu
		menu = createPopupMenu(app, ContextMenuView.TAGS_MENU, "Tags");
		// add it to the part
		desc.getMenus().add(menu);

		// context menu
		menu = createPopupMenu(app, ContextMenuView.INFO_MENU, "Info");
		// add it to the part
		desc.getMenus().add(menu);

		app.getDescriptors().add(desc);
	}

	private void createCommand(MApplication app) {
		MCommand cmd = CommandsFactoryImpl.eINSTANCE.createCommand();
		cmd.setElementId(OPEN_DIALOG_COMMAND);
		cmd.setCommandName("Show Info");
		MHandler handler = CommandsFactoryImpl.eINSTANCE.createHandler();
		handler.setCommand(cmd);
		handler.setElementId("open.dialog.on.selection");
		handler.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.OpenDialogHandler");
		app.getCommands().add(cmd);
		app.getHandlers().add(handler);
	}

	private MMenu createPopupMenu(MApplication app, String id, String name) {
		MMenu menu = MenuFactoryImpl.eINSTANCE.createMenu();
		menu.getTags().add("popup");
		menu.setElementId(id);

		MHandledMenuItem item = MenuFactoryImpl.eINSTANCE
				.createHandledMenuItem();
		item.setElementId("e4.showView");
		item.setLabel("Show View " + name);
		item.setCommand(findCommand(app, "org.eclipse.ui.views.showView"));
		menu.getChildren().add(item);

		MMenuSeparator sep = MenuFactoryImpl.eINSTANCE.createMenuSeparator();
		sep.setElementId("additions");
		menu.getChildren().add(sep);

		item = MenuFactoryImpl.eINSTANCE.createHandledMenuItem();
		item.setElementId("copy");
		item.setLabel("Copy");
		item.setCommand(findCommand(app, "org.eclipse.ui.edit.copy"));
		menu.getChildren().add(item);

		MCoreExpression exp = UiFactoryImpl.eINSTANCE.createCoreExpression();
		exp.setCoreExpressionId("org.eclipse.e4.ui.tests.selection.Entry");
		item = MenuFactoryImpl.eINSTANCE.createHandledMenuItem();
		MCommand cmd = findCommand(app, OPEN_DIALOG_COMMAND);
		item.setElementId(cmd.getElementId());
		item.setLabel(cmd.getCommandName());
		item.setCommand(cmd);
		item.setVisibleWhen(exp);
		menu.getChildren().add(item);

		return menu;
	}

	private MCommand findCommand(MApplication app, String id) {
		for (MCommand cmd : app.getCommands()) {
			if (id.equals(cmd.getElementId())) {
				return cmd;
			}
		}
		return null;
	}

}
