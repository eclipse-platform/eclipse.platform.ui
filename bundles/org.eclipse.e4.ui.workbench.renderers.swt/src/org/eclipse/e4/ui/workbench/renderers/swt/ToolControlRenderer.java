/*******************************************************************************
 * Copyright (c) 2010, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sopot Cela <sopotcela@gmail.com> - Bug 431868
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 431868
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.List;
import javax.inject.Inject;
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
import org.eclipse.swt.widgets.Listener;
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

		setCSSInfo(toolControl, newCtrl);

		boolean vertical = false;
		MUIElement parentElement = element.getParent();
		if (parentElement instanceof MTrimBar) {
			MTrimBar bar = (MTrimBar) parentElement;
			vertical = bar.getSide() == SideValue.LEFT
					|| bar.getSide() == SideValue.RIGHT;
		}
		CSSRenderingUtils cssUtils = parentContext.get(CSSRenderingUtils.class);
		newCtrl = cssUtils.frameMeIfPossible(newCtrl, null, vertical, true);
		createToolControlMenu(toolControl, newCtrl);
		return newCtrl;
	}

	@Inject
	@Optional
	private void subscribeTopicTagsChanged(
			@UIEventTopic(UIEvents.ApplicationElement.TOPIC_TAGS) Event event) {

		Object changedObj = event.getProperty(EventTags.ELEMENT);

		if (!(changedObj instanceof MToolControl))
			return;

		final MUIElement changedElement = (MUIElement) changedObj;

		if (UIEvents.isADD(event)) {
			if (UIEvents.contains(event, UIEvents.EventTags.NEW_VALUE,
					IPresentationEngine.HIDDEN_EXPLICITLY)) {
				changedElement.setVisible(false);
				changedElement.setToBeRendered(false);
			}
		} else if (UIEvents.isREMOVE(event)) {
			if (UIEvents.contains(event, UIEvents.EventTags.OLD_VALUE,
					IPresentationEngine.HIDDEN_EXPLICITLY)) {
				changedElement.setVisible(true);
				changedElement.setToBeRendered(true);
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
				toolControl.setToBeRendered(false);
			}
		}
	}

	private void createToolControlMenu(final MToolControl toolControl,
			Control renderedCtrl) {
		toolControlMenu = new Menu(renderedCtrl);
		MenuItem hideItem = new MenuItem(toolControlMenu, SWT.NONE);
		hideItem.setText(Messages.ToolBarManagerRenderer_MenuCloseText);
		hideItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(org.eclipse.swt.widgets.Event event) {
				toolControl.getTags()
						.add(IPresentationEngine.HIDDEN_EXPLICITLY);
			}
		});

		new MenuItem(toolControlMenu, SWT.SEPARATOR);

		MenuItem restoreHiddenItems = new MenuItem(toolControlMenu, SWT.NONE);
		restoreHiddenItems
				.setText(Messages.ToolBarManagerRenderer_MenuRestoreText);
		restoreHiddenItems.addListener(SWT.Selection, new Listener() {
			public void handleEvent(org.eclipse.swt.widgets.Event event) {
				removeHiddenTags(toolControl);
			}
		});
		renderedCtrl.setMenu(toolControlMenu);

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
