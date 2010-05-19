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

package org.eclipse.e4.ui.workbench.addons.perspectiveswitcher;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.workbench.modeling.EModelService;
import org.eclipse.e4.workbench.ui.UIEvents;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class PerspectiveSwitcher {
	public static final String PERSPECTIVE_SWITCHER_ID = "org.eclipse.e4.ui.PerspectiveSwitcher"; //$NON-NLS-1$
	@Inject
	protected IEventBroker eventBroker;

	@Inject
	EModelService modelService;

	private ToolBar psTB;
	private Composite comp;
	private Image backgroundImage;

	private EventHandler selectionHandler = new EventHandler() {
		public void handleEvent(Event event) {
			MUIElement changedElement = (MUIElement) event.getProperty(UIEvents.EventTags.ELEMENT);

			if (!(changedElement instanceof MPerspectiveStack))
				return;

			MPerspectiveStack perspStack = (MPerspectiveStack) changedElement;
			if (!perspStack.isToBeRendered())
				return;

			MPerspective selElement = perspStack.getSelectedElement();
			for (ToolItem ti : psTB.getItems()) {
				ti.setSelection(ti.getData() == selElement);
			}
		}
	};

	private EventHandler toBeRenderedHandler = new EventHandler() {
		public void handleEvent(Event event) {
			MUIElement changedElement = (MUIElement) event.getProperty(UIEvents.EventTags.ELEMENT);

			if (!(changedElement instanceof MPerspective))
				return;

			MPerspective persp = (MPerspective) changedElement;
			if (!persp.getParent().isToBeRendered())
				return;

			if (changedElement.isToBeRendered()) {
				addPerspectiveItem(persp);
			} else {
				removePerspectiveItem(persp);
			}
		}
	};

	private EventHandler childrenHandler = new EventHandler() {
		public void handleEvent(Event event) {
			Object changedObj = event.getProperty(UIEvents.EventTags.ELEMENT);
			String eventType = (String) event.getProperty(UIEvents.EventTags.TYPE);

			if (changedObj instanceof MWindow && UIEvents.EventTypes.ADD.equals(eventType)) {
				MUIElement added = (MUIElement) event.getProperty(UIEvents.EventTags.NEW_VALUE);
				if (added instanceof MPerspectiveStack) {
				}
			}
			if (!(changedObj instanceof MPerspectiveStack))
				return;

			if (UIEvents.EventTypes.ADD.equals(eventType)) {
				MPerspective added = (MPerspective) event.getProperty(UIEvents.EventTags.NEW_VALUE);
				// Adding invisible elements is a NO-OP
				if (!added.isToBeRendered())
					return;

				addPerspectiveItem(added);

				// update fix the layout
				psTB.pack();
				psTB.getShell().layout(new Control[] { psTB }, SWT.DEFER);
			} else if (UIEvents.EventTypes.REMOVE.equals(eventType)) {
				MPerspective removed = (MPerspective) event
						.getProperty(UIEvents.EventTags.OLD_VALUE);
				// Removing invisible elements is a NO-OP
				if (!removed.isToBeRendered())
					return;

				removePerspectiveItem(removed);

				// update the layout
				psTB.pack();
				psTB.getShell().layout(new Control[] { psTB }, SWT.DEFER);
			}
		}
	};

	@PostConstruct
	void init(IEclipseContext context) {
		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.ElementContainer.TOPIC,
				UIEvents.ElementContainer.CHILDREN), childrenHandler);
		eventBroker.subscribe(
				UIEvents.buildTopic(UIEvents.UIElement.TOPIC, UIEvents.UIElement.TOBERENDERED),
				toBeRenderedHandler);
		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.ElementContainer.TOPIC,
				UIEvents.ElementContainer.SELECTEDELEMENT), selectionHandler);
	}

	@PostConstruct
	void createWidget(Composite parent, MWindow window) {
		comp = new Composite(parent, SWT.NONE);
		RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		layout.marginLeft = layout.marginRight = 8;
		layout.marginBottom = layout.marginTop = 0;
		comp.setLayout(layout);
		psTB = new ToolBar(comp, SWT.FLAT | SWT.WRAP);
		comp.addControlListener(new ControlListener() {

			public void controlMoved(ControlEvent e) {
				// we don't care
			}

			public void controlResized(ControlEvent e) {
				resize(e);
			}

		});
		comp.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}

		});

		psTB.addMenuDetectListener(new MenuDetectListener() {
			public void menuDetected(MenuDetectEvent e) {
				ToolBar tb = (ToolBar) e.widget;
				System.out.println("Menu Detect"); //$NON-NLS-1$
				Point p = new Point(e.x, e.y);
				p = psTB.getDisplay().map(null, psTB, p);
				ToolItem item = tb.getItem(p);
				if (item == null)
					System.out.println("  ToolBar menu"); //$NON-NLS-1$
				else {
					MPerspective persp = (MPerspective) item.getData();
					if (persp == null)
						System.out.println("  Add button Menu"); //$NON-NLS-1$
					else
						System.out.println("  Perspective menu: " + persp.getElementId()); //$NON-NLS-1$
				}
			}
		});

		ToolItem createItem = new ToolItem(psTB, SWT.PUSH);
		createItem.setText("+"); //$NON-NLS-1$
		new ToolItem(psTB, SWT.SEPARATOR);

		List<MPerspectiveStack> psList = modelService.findElements(window, null,
				MPerspectiveStack.class, null);
		if (psList.size() > 0) {
			MPerspectiveStack perspStack = psList.get(0);

			// Create an item for each perspective that should show up
			for (MPerspective persp : perspStack.getChildren()) {
				if (persp.isToBeRendered()) {
					addPerspectiveItem(persp);
				}
			}
		}
	}

	@PreDestroy
	void removeListeners() {
		eventBroker.unsubscribe(toBeRenderedHandler);
		eventBroker.unsubscribe(childrenHandler);
		eventBroker.unsubscribe(selectionHandler);
	}

	private ToolItem addPerspectiveItem(MPerspective persp) {
		ToolItem psItem = new ToolItem(psTB, SWT.RADIO);
		psItem.setData(persp);
		psItem.setText(persp.getLabel());
		// psItem.setIconURI(persp.getIconURI());
		psItem.setToolTipText(persp.getTooltip());

		psItem.setSelection(persp == persp.getParent().getSelectedElement());

		psItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				MPerspective persp = (MPerspective) e.widget.getData();
				persp.getParent().setSelectedElement(persp);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				MPerspective persp = (MPerspective) e.widget.getData();
				persp.getParent().setSelectedElement(persp);
			}
		});

		psItem.addListener(SWT.MenuDetect, new Listener() {
			public void handleEvent(org.eclipse.swt.widgets.Event event) {
				MPerspective persp = (MPerspective) event.widget.getData();
				System.out.println("Menu for: " + persp.getElementId()); //$NON-NLS-1$
			}
		});
		return psItem;
	}

	private void removePerspectiveItem(MPerspective toRemove) {
		ToolItem psItem = getItemFor(toRemove);
		if (psItem != null) {
			psItem.dispose();
		}
	}

	protected ToolItem getItemFor(MPerspective persp) {
		if (psTB == null)
			return null;

		for (ToolItem ti : psTB.getItems()) {
			if (ti.getData() == persp)
				return ti;
		}

		return null;
	}

	void resize(ControlEvent e) {
		Point size = comp.getSize();
		Image oldBackgroundImage = backgroundImage;
		backgroundImage = new Image(comp.getDisplay(), size.x, size.y);
		GC gc = new GC(backgroundImage);
		comp.getParent().drawBackground(gc, 0, 0, size.x, size.y, 0, 0);
		Color background = comp.getBackground();
		Color border = comp.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
		RGB backgroundRGB = background.getRGB();
		// TODO naive and hard coded, doesn't deal with high contrast, etc.
		Color gradientTop = new Color(comp.getDisplay(), backgroundRGB.red + 12,
				backgroundRGB.green + 10, backgroundRGB.blue + 10);
		int h = size.y;
		int curveStart = 0;
		int curve_width = 5;

		int[] curve = new int[] { 0, h, 1, h, 2, h - 1, 3, h - 2, 3, 2, 4, 1, 5, 0, };
		int[] line1 = new int[curve.length + 4];
		int index = 0;
		int x = curveStart;
		line1[index++] = x + 1;
		line1[index++] = h;
		for (int i = 0; i < curve.length / 2; i++) {
			line1[index++] = x + curve[2 * i];
			line1[index++] = curve[2 * i + 1];
		}
		line1[index++] = x + curve_width;
		line1[index++] = 0;

		int[] line2 = new int[line1.length];
		index = 0;
		for (int i = 0; i < line1.length / 2; i++) {
			line2[index] = line1[index++] - 1;
			line2[index] = line1[index++];
		}

		// custom gradient
		gc.setForeground(gradientTop);
		gc.setBackground(background);
		gc.drawLine(4, 0, size.x, 0);
		gc.drawLine(3, 1, size.x, 1);
		gc.fillGradientRectangle(2, 2, size.x - 2, size.y - 3, true);
		gc.setForeground(background);
		gc.drawLine(2, size.y - 1, size.x, size.y - 1);
		gradientTop.dispose();

		gc.setForeground(border);
		gc.drawPolyline(line2);
		gc.dispose();
		comp.setBackgroundImage(backgroundImage);
		if (oldBackgroundImage != null)
			oldBackgroundImage.dispose();

	}

	void dispose() {
		if (backgroundImage != null) {
			comp.setBackgroundImage(null);
			backgroundImage.dispose();
			backgroundImage = null;
		}
	}
}
