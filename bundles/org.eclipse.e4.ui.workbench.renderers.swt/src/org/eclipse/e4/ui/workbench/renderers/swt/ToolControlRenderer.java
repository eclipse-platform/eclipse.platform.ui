/*******************************************************************************
 * Copyright (c) 2010, 2017 IBM Corporation and others.
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
 *     Sopot Cela <sopotcela@gmail.com> - Bug 431868, 472761
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 431868
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 515253
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.State;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.internal.workbench.swt.CSSRenderingUtils;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.e4.ui.workbench.swt.factories.IRendererFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.osgi.service.event.Event;

/**
 * Create a contribute part.
 */
public class ToolControlRenderer extends SWTPartRenderer {

	/**
	 * Will be published or removed in 4.5.
	 */
	private static final String HIDEABLE = "HIDEABLE"; //$NON-NLS-1$
	/**
	 * Will be published or removed in 4.5.
	 */
	private static final String SHOW_RESTORE_MENU = "SHOW_RESTORE_MENU"; //$NON-NLS-1$

	/**
	 * Id for the lock toolbar command
	 */
	private static final String LOCK_TOOLBAR_CMD_ID = "org.eclipse.ui.window.lockToolBar"; //$NON-NLS-1$

	/**
	 * The state ID for a toggle state understood by the system.
	 *
	 * @see RegistryToggleState.STATE_ID
	 */
	public static final String STATE_ID = "org.eclipse.ui.commands.toggleState"; //$NON-NLS-1$

	@Inject
	private MApplication application;
	/**
	 * The context menu for this trim stack's items.
	 */
	private Menu toolControlMenu;

	@Override
	public Object createWidget(final MUIElement element, Object parent) {
		if (!(element instanceof MToolControl)
				|| !(parent instanceof ToolBar || parent instanceof Composite))
			return null;
		Composite parentComp = (Composite) parent;
		MToolControl toolControl = (MToolControl) element;

		if (((Object) toolControl.getParent()) instanceof MToolBar) {
			IRendererFactory factory = context.get(IRendererFactory.class);
			AbstractPartRenderer renderer = factory.getRenderer(
					toolControl.getParent(), parent);
			if (renderer instanceof ToolBarManagerRenderer) {
				return null;
			}
		}

		Widget parentWidget = (Widget) parent;
		IEclipseContext parentContext = getContextForParent(element);

		ToolItem sep = null;
		if (parent instanceof ToolBar) {
			sep = new ToolItem((ToolBar) parentWidget, SWT.SEPARATOR);
		}

		// final Composite newComposite = new Composite((Composite)
		// parentWidget,
		// SWT.NONE);
		// newComposite.setLayout(new FillLayout());
		// bindWidget(element, newComposite);

		// Create a context just to contain the parameters for injection
		IContributionFactory contributionFactory = parentContext
				.get(IContributionFactory.class);

		IEclipseContext localContext = EclipseContextFactory.create();

		localContext.set(Composite.class.getName(), parentComp);
		localContext.set(MToolControl.class.getName(), toolControl);

		Object tcImpl = contributionFactory.create(
				toolControl.getContributionURI(), parentContext, localContext);
		toolControl.setObject(tcImpl);
		Control[] kids = parentComp.getChildren();

		// No kids means that the trim failed curing creation
		if (kids.length == 0)
			return null;

		// The new control is assumed to be the last child created
		// We could safe this up even more by asserting that the
		// number of children should go up by *one* during injection
		Control newCtrl = kids[kids.length - 1];

		if (sep != null && newCtrl != null) {
			sep.setControl(newCtrl);
			newCtrl.pack();
			sep.setWidth(newCtrl.getSize().x);
		}

		bindWidget(toolControl, newCtrl);
		boolean vertical = false;
		MUIElement parentElement = element.getParent();
		if (parentElement instanceof MTrimBar) {
			MTrimBar bar = (MTrimBar) parentElement;
			vertical = bar.getSide() == SideValue.LEFT
					|| bar.getSide() == SideValue.RIGHT;
		}
		CSSRenderingUtils cssUtils = parentContext.get(CSSRenderingUtils.class);
		MUIElement modelElement = (MUIElement) newCtrl.getData(AbstractPartRenderer.OWNING_ME);
		boolean draggable = ((modelElement != null) && (modelElement.getTags().contains(IPresentationEngine.DRAGGABLE)));
		newCtrl = cssUtils.frameMeIfPossible(newCtrl, null, vertical, draggable);

		boolean hideable = isHideable(toolControl);
		boolean showRestoreMenu = isRestoreMenuShowable(toolControl);
		if (showRestoreMenu || hideable) {
			createToolControlMenu(toolControl, newCtrl, hideable);
		}

		return newCtrl;
	}

	private boolean isRestoreMenuShowable(MToolControl toolControl) {
		return toolControl.getTags().contains(SHOW_RESTORE_MENU);
	}

	private boolean isHideable(MToolControl toolControl) {
		return toolControl.getTags().contains(HIDEABLE);
	}

	@Inject
	@Optional
	private void subscribeTopicTagsChanged(
			@UIEventTopic(UIEvents.ApplicationElement.TOPIC_TAGS) Event event) {

		Object changedObj = event.getProperty(EventTags.ELEMENT);

		if (!(changedObj instanceof MToolControl))
			return;

		final MToolControl changedElement = (MToolControl) changedObj;

		if (UIEvents.isADD(event)) {
			if (UIEvents.contains(event, UIEvents.EventTags.NEW_VALUE,
					IPresentationEngine.HIDDEN_EXPLICITLY)) {
				changedElement.setVisible(false);
			} else {
				boolean hideable = UIEvents.contains(event,
						UIEvents.EventTags.NEW_VALUE, HIDEABLE);
				if (UIEvents.contains(event, UIEvents.EventTags.NEW_VALUE,
						SHOW_RESTORE_MENU) || hideable) {
					Object obj = changedElement.getWidget();
					if (obj instanceof Control) {
						if (((Control) obj).getMenu() == null) {
							createToolControlMenu(changedElement,
									(Control) obj, hideable);
						}
					}
				}
			}
		} else if (UIEvents.isREMOVE(event)) {
			if (UIEvents.contains(event, UIEvents.EventTags.OLD_VALUE,
					IPresentationEngine.HIDDEN_EXPLICITLY)) {
				changedElement.setVisible(true);
			}
		}
	}

	@Inject
	@Optional
	private void subscribeTopicAppStartup(
			@UIEventTopic(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE) Event event) {
		List<MToolControl> toolControls = modelService.findElements(
				application, null, MToolControl.class, null);
		for (MToolControl toolControl : toolControls) {
			if (toolControl.getTags().contains(
					IPresentationEngine.HIDDEN_EXPLICITLY)) {
				toolControl.setVisible(false);
			}
		}
	}

	private void createToolControlMenu(final MToolControl toolControl,
			Control renderedCtrl, boolean hideable) {
		toolControlMenu = new Menu(renderedCtrl);

		if (hideable) {
			MenuItem hideItem = new MenuItem(toolControlMenu, SWT.NONE);
			hideItem.setText(Messages.ToolBarManagerRenderer_MenuCloseText);
			hideItem.addListener(SWT.Selection, event -> toolControl.getTags().add(
					IPresentationEngine.HIDDEN_EXPLICITLY));

			new MenuItem(toolControlMenu, SWT.SEPARATOR);
		}

		MenuItem restoreHiddenItems = new MenuItem(toolControlMenu, SWT.NONE);
		restoreHiddenItems
				.setText(Messages.ToolBarManagerRenderer_MenuRestoreText);
		restoreHiddenItems.addListener(SWT.Selection, event -> removeHiddenTags(toolControl));

		// lock the toolbars
		MenuItem toggleLockToolbars = new MenuItem(toolControlMenu, SWT.NONE);
		toggleLockToolbars.setText(getLockToolbarsText());
		toggleLockToolbars.addListener(SWT.Selection, event -> {
			// execute command
			EHandlerService handlerService = context.get(EHandlerService.class);
			ECommandService commandService = context.get(ECommandService.class);
			ParameterizedCommand pCommand = commandService.createCommand(LOCK_TOOLBAR_CMD_ID, Collections.emptyMap());
			handlerService.executeHandler(pCommand);
			toggleLockToolbars.setText(getLockToolbarsText());
		});
		renderedCtrl.setMenu(toolControlMenu);
	}

	/* get the toggle toolbar text depending on the command state */
	private String getLockToolbarsText() {
		ECommandService commandService = context.get(ECommandService.class);
		Command command = commandService.getCommand(LOCK_TOOLBAR_CMD_ID);
		State state = command.getState(STATE_ID);
		if ((state != null) && (state.getValue() instanceof Boolean)) {
			boolean enabled = ((Boolean) state.getValue()).booleanValue();
			return (enabled) ? Messages.ToolBarManagerRenderer_UnlockToolbars
					: Messages.ToolBarManagerRenderer_LockToolbars;
		}
		return Messages.ToolBarManagerRenderer_ToggleLockToolbars;
	}

	/**
	 * Removes the IPresentationEngine.HIDDEN_EXPLICITLY from the trimbar
	 * entries. Having a separate logic for toolbars and toolcontrols would be
	 * confusing for the user, hence we remove this tag for both these types
	 *
	 * @param toolbarModel
	 */
	private void removeHiddenTags(MToolControl toolControl) {
		MWindow mWindow = modelService.getTopLevelWindowFor(toolControl);
		List<MTrimElement> trimElements = modelService.findElements(mWindow,
				null, MTrimElement.class, null);
		for (MTrimElement trimElement : trimElements) {
			trimElement.getTags().remove(IPresentationEngine.HIDDEN_EXPLICITLY);
		}
	}

}
