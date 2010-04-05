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

package org.eclipse.e4.ui.workbench.trim;

import java.util.List;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.annotations.PostConstruct;
import org.eclipse.e4.core.services.annotations.PreDestroy;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ItemType;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MDirectToolItem;
import org.eclipse.e4.ui.model.application.MPerspective;
import org.eclipse.e4.ui.model.application.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.MToolBar;
import org.eclipse.e4.ui.model.application.MToolItem;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MWindowTrim;
import org.eclipse.e4.ui.model.application.SideValue;
import org.eclipse.e4.workbench.modeling.EModelService;
import org.eclipse.e4.workbench.ui.UIEvents;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class PerspectiveSwitcher {
	public static final String PERSPECTIVE_SWITCHER_ID = "org.eclipse.e4.ui.PerspectiveSwitcher";
	@Inject
	protected IEventBroker eventBroker;

	@Inject
	EModelService modelService;

	private MToolBar perspectiveSwitcherTB;

	private EventHandler selectionHandler = new EventHandler() {
		public void handleEvent(Event event) {
			if (perspectiveSwitcherTB == null)
				return;

			MUIElement changedElement = (MUIElement) event
					.getProperty(UIEvents.EventTags.ELEMENT);

			if (!(changedElement instanceof MPerspectiveStack))
				return;

			MPerspectiveStack perspStack = (MPerspectiveStack) changedElement;
			if (!perspStack.isToBeRendered())
				return;

			// Clear the previous selected item (if any)
			clearCurSelection();

			MToolItem psItem = getItemFor(perspStack.getSelectedElement());
			if (psItem != null)
				psItem.setSelected(true);

			if (event.getProperty(UIEvents.EventTags.OLD_VALUE) instanceof MPerspective) {
				MPerspective oldSel = (MPerspective) event
						.getProperty(UIEvents.EventTags.OLD_VALUE);
				MToolItem oldSelItem = getItemFor(oldSel);
				if (oldSelItem != null)
					oldSelItem.setSelected(false);
			}
		}

		private void clearCurSelection() {
			for (MToolItem ti : perspectiveSwitcherTB.getChildren()) {
				if (ti.isSelected())
					ti.setSelected(false);
			}
		}
	};

	private EventHandler toBeRenderedHandler = new EventHandler() {
		public void handleEvent(Event event) {
			if (perspectiveSwitcherTB == null)
				return;

			MUIElement changedElement = (MUIElement) event
					.getProperty(UIEvents.EventTags.ELEMENT);

			if (!(changedElement instanceof MPerspective))
				return;

			MPerspective persp = (MPerspective) changedElement;
			if (!persp.getParent().isToBeRendered())
				return;

			if (changedElement.isToBeRendered()) {
				MToolItem psItem = addPerspectiveItem(persp);
			} else {
				removePerspectiveItem(persp);
			}
		}
	};

	private EventHandler childrenHandler = new EventHandler() {
		public void handleEvent(Event event) {
			if (perspectiveSwitcherTB == null)
				return;

			Object changedObj = event.getProperty(UIEvents.EventTags.ELEMENT);
			if (!(changedObj instanceof MPerspectiveStack))
				return;

			MPerspectiveStack perspStack = (MPerspectiveStack) changedObj;

			// if we aren't in the UI who cares?
			if (!perspStack.isToBeRendered())
				return;

			String eventType = (String) event
					.getProperty(UIEvents.EventTags.TYPE);
			if (UIEvents.EventTypes.ADD.equals(eventType)) {
				MPerspective added = (MPerspective) event
						.getProperty(UIEvents.EventTags.NEW_VALUE);
				// Adding invisible elements is a NO-OP
				if (!added.isToBeRendered())
					return;

				MToolItem psItem = addPerspectiveItem(added);
				perspectiveSwitcherTB.getChildren().add(psItem);

				// Hack!! fix the layout
				ToolBar tb = (ToolBar) perspectiveSwitcherTB.getWidget();
				tb.pack();
				tb.getParent().layout(new Control[] { tb }, SWT.DEFER);
			} else if (UIEvents.EventTypes.REMOVE.equals(eventType)) {
				MPerspective removed = (MPerspective) event
						.getProperty(UIEvents.EventTags.OLD_VALUE);
				// Removing invisible elements is a NO-OP
				if (!removed.isToBeRendered())
					return;

				removePerspectiveItem(removed);

				// Hack!! fix the layout
				ToolBar tb = (ToolBar) perspectiveSwitcherTB.getWidget();
				tb.pack();
				tb.getParent().layout(new Control[] { tb }, SWT.DEFER);
			}
		}
	};

	@PostConstruct
	void init(IEclipseContext context) {
		System.out.println("init");

		eventBroker.subscribe(UIEvents.buildTopic(
				UIEvents.ElementContainer.TOPIC,
				UIEvents.ElementContainer.CHILDREN), childrenHandler);
		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.UIElement.TOPIC,
				UIEvents.UIElement.TOBERENDERED), toBeRenderedHandler);
		eventBroker.subscribe(UIEvents.buildTopic(
				UIEvents.ElementContainer.TOPIC,
				UIEvents.ElementContainer.SELECTEDELEMENT), selectionHandler);
	}

	@PreDestroy
	private void removeListeners() {
		eventBroker.unsubscribe(toBeRenderedHandler);
		eventBroker.unsubscribe(childrenHandler);
		eventBroker.unsubscribe(selectionHandler);
	}

	public void postContextCreate(IEclipseContext context) {
		System.out.println("postContextCreate");
	}

	public void processAdditions(MApplication appModel) {
		System.out.println("processAdditions");
	}

	public void processRemovals(MApplication appModel) {
		System.out.println("processRemovals");

		perspectiveSwitcherTB = (MToolBar) modelService.find(
				PERSPECTIVE_SWITCHER_ID, appModel);
		if (perspectiveSwitcherTB != null)
			return;

		// OK, we don't already have one so create one...
		List<MPerspectiveStack> psList = modelService.findElements(appModel,
				null, MPerspectiveStack.class, null);
		for (MPerspectiveStack ps : psList) {
			if (ps.isToBeRendered()) {
				addSwitcher(appModel, ps);
			}
		}
	}

	private void addSwitcher(MApplication appModel, MPerspectiveStack ps) {
		List<MWindowTrim> trimList = modelService.findElements(appModel, null,
				MWindowTrim.class, null);
		for (MWindowTrim trim : trimList) {
			if (trim.getSide() == SideValue.TOP) {
				perspectiveSwitcherTB = MApplicationFactory.eINSTANCE
						.createToolBar();
				perspectiveSwitcherTB.setId(PERSPECTIVE_SWITCHER_ID);

				// Create an item for each perspective that should show up
				for (MPerspective persp : ps.getChildren()) {
					if (persp.isToBeRendered()) {
						MToolItem psItem = addPerspectiveItem(persp);
						psItem.setSelected(persp == ps.getSelectedElement());
					}
				}
				trim.getChildren().add(perspectiveSwitcherTB);
			}
		}
	}

	private MToolItem addPerspectiveItem(MPerspective persp) {
		MDirectToolItem psItem = MApplicationFactory.eINSTANCE
				.createDirectToolItem();
		psItem.setId(persp.getId());
		psItem.setLabel(persp.getLabel());
		psItem.setIconURI(persp.getIconURI());
		psItem.setTooltip(persp.getTooltip());
		psItem.setType(ItemType.CHECK);
		psItem.setURI("platform:/plugin/org.eclipse.e4.ui.workbench.swt/org.eclipse.e4.ui.workbench.trim.SwitchPerspective");

		perspectiveSwitcherTB.getChildren().add(psItem);
		return psItem;
	}

	protected MToolItem getItemFor(MPerspective persp) {
		return (MToolItem) modelService.find(persp.getId(),
				perspectiveSwitcherTB);
	}

	private void removePerspectiveItem(MPerspective toRemove) {
		MToolItem psItem = getItemFor(toRemove);
		if (psItem != null) {
			perspectiveSwitcherTB.getChildren().remove(psItem);
		}
	}
}
