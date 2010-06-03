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

package org.eclipse.e4.ui.workbench.addons.minmax;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.swt.internal.AbstractPartRenderer;
import org.eclipse.e4.workbench.modeling.EModelService;
import org.eclipse.e4.workbench.modeling.EPartService;
import org.eclipse.e4.workbench.modeling.EPartService.PartState;
import org.eclipse.e4.workbench.ui.UIEvents;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.osgi.service.event.EventHandler;

/**
 *
 */
public class TrimStack {
	private static Image restoreImage;

	private static int SHOW_SHELL = 1;
	private static int HOVER_SHELL = 2;

	private ToolBar trimStackTB;

	MPartStack theStack;
	private Shell showShell;
	private Shell hoverShell;
	private boolean inhibitHover = false;

	private Listener mouseDownListener = new Listener() {
		public void handleEvent(Event event) {
			if (event.widget instanceof Control) {
				Control control = (Control) event.widget;
				if (control == null)
					return;
				if (control.getShell() != showShell.getParent())
					return;
				if (control == trimStackTB)
					return;

				showStack(false);
			}
		}
	};

	// Traverse listener -- listens to ESC and closes the active fastview
	private Listener escapeListener = new Listener() {
		public void handleEvent(Event event) {
			if (event.character == SWT.ESC) {
				showStack(false);
			}
		}
	};

	@Inject
	EModelService modelService;

	@Inject
	EPartService partService;

	@Inject
	MWindow window;

	@Inject
	MToolControl toolControl;

	@Inject
	protected IEventBroker eventBroker;

	private EventHandler selectionHandler = new EventHandler() {
		public void handleEvent(org.osgi.service.event.Event event) {
			MUIElement changedElement = (MUIElement) event.getProperty(UIEvents.EventTags.ELEMENT);
			if (changedElement != theStack)
				return;

			if (showShell != null && showShell.isVisible())
				updateSelection(theStack.getSelectedElement());
		}
	};

	private EventHandler toBeRenderedHandler = new EventHandler() {
		public void handleEvent(org.osgi.service.event.Event event) {
			if (theStack == null || trimStackTB == null)
				return;

			MUIElement changedElement = (MUIElement) event.getProperty(UIEvents.EventTags.ELEMENT);

			MUIElement parentElement = changedElement.getParent();
			if (theStack == null || trimStackTB == null || parentElement != theStack)
				return;

			trimStackTB.getDisplay().asyncExec(new Runnable() {
				public void run() {
					updateTrimStackItems();
				}
			});
		}
	};

	private EventHandler childrenHandler = new EventHandler() {
		public void handleEvent(org.osgi.service.event.Event event) {
			if (theStack == null || trimStackTB == null)
				return;

			Object changedObj = event.getProperty(UIEvents.EventTags.ELEMENT);
			if (changedObj != theStack)
				return;

			trimStackTB.getDisplay().asyncExec(new Runnable() {
				public void run() {
					updateTrimStackItems();
				}
			});
		}
	};

	@PostConstruct
	void addListeners() {
		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.ElementContainer.TOPIC,
				UIEvents.ElementContainer.CHILDREN), childrenHandler);
		eventBroker.subscribe(
				UIEvents.buildTopic(UIEvents.UIElement.TOPIC, UIEvents.UIElement.TOBERENDERED),
				toBeRenderedHandler);
		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.ElementContainer.TOPIC,
				UIEvents.ElementContainer.SELECTEDELEMENT), selectionHandler);
	}

	@PreDestroy
	void removeListeners() {
		eventBroker.unsubscribe(toBeRenderedHandler);
		eventBroker.unsubscribe(childrenHandler);
		eventBroker.unsubscribe(selectionHandler);
	}

	@PostConstruct
	void createWidget(Composite parent, MWindow window) {
		if (theStack == null) {
			List<MPerspectiveStack> ps = modelService.findElements(window, null,
					MPerspectiveStack.class, null);
			if (ps.size() == 0) {
				theStack = (MPartStack) modelService.find(toolControl.getElementId(), window);
			} else {
				String toolControlId = toolControl.getElementId();
				int index = toolControlId.indexOf('(');
				String stackId = toolControlId.substring(0, index);
				String perspId = toolControlId.substring(index + 1, toolControlId.length() - 1);
				MPerspective persp = (MPerspective) modelService.find(perspId, ps.get(0));
				if (persp != null) {
					theStack = (MPartStack) modelService.find(stackId, persp);
				}
			}
		}

		trimStackTB = new ToolBar(parent, SWT.FLAT | SWT.WRAP);

		ToolItem restoreBtn = new ToolItem(trimStackTB, SWT.PUSH);
		restoreBtn.setImage(getRestoreImage(parent.getDisplay()));
		restoreBtn.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				restoreStack();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				restoreStack();
			}
		});

		updateTrimStackItems();
		// addTransparentTooltips(newTB);
	}

	private void updateTrimStackItems() {
		// Remove any current items except the 'restore' button
		while (trimStackTB.getItemCount() > 1) {
			trimStackTB.getItem(trimStackTB.getItemCount() - 1).dispose();
		}

		CTabFolder ctf = (CTabFolder) theStack.getWidget();
		if (ctf == null) {
			// Try again...this is *really* dangerous since it'll chase its tail forever if the
			// stack never gets rendered
			trimStackTB.getDisplay().asyncExec(new Runnable() {
				public void run() {
					updateTrimStackItems();
				}
			});
			return;
		}
		CTabItem[] items = ctf.getItems();
		for (CTabItem item : items) {
			ToolItem newItem = new ToolItem(trimStackTB, SWT.CHECK);
			newItem.setData(item);
			newItem.setImage(item.getImage());
			newItem.setToolTipText(item.getText());
			newItem.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					ToolItem item = (ToolItem) e.widget;
					CTabItem cti = (CTabItem) item.getData();
					MUIElement me = (MUIElement) cti.getData(AbstractPartRenderer.OWNING_ME);
					if (me instanceof MPlaceholder)
						me = ((MPlaceholder) me).getRef();
					partService.activate((MPart) me);
					showStack(item.getSelection());
				}

				public void widgetDefaultSelected(SelectionEvent e) {
					ToolItem item = (ToolItem) e.widget;
					CTabItem cti = (CTabItem) item.getData();
					theStack.setSelectedElement((MStackElement) cti
							.getData(AbstractPartRenderer.OWNING_ME));
					showStack(true);
				}
			});
		}

		if (showShell != null && showShell.isVisible())
			updateSelection(theStack.getSelectedElement());

		trimStackTB.pack();
		trimStackTB.getShell().layout(new Control[] { trimStackTB }, SWT.DEFER);
	}

	/**
	 * @param ti
	 */
	protected void showHover(boolean show, ToolItem ti) {
		CTabFolder ctf = (CTabFolder) theStack.getWidget();
		if (show) {
			Shell hoverShell = getShell(HOVER_SHELL, ti.getParent().getShell());
			ctf.setParent(hoverShell);

			hoverShell.layout(true);
			CTabItem cti = (CTabItem) ti.getData();
			MUIElement element = (MUIElement) cti.getData(AbstractPartRenderer.OWNING_ME);
			if (element instanceof MPlaceholder)
				element = ((MPlaceholder) element).getRef();
			if (element instanceof MPart) {
				MPart part = (MPart) element;
				partService.showPart(part, PartState.VISIBLE);
			}

			// Set the initial location
			Rectangle itemBounds = ti.getBounds();
			Point shellSize = hoverShell.getSize();
			Point loc = new Point(itemBounds.x - (shellSize.x / 2), itemBounds.y
					+ itemBounds.height);
			loc = ti.getDisplay().map(ti.getParent(), null, loc);
			hoverShell.setLocation(loc);

			hoverShell.setVisible(true);
		} else {
			if (hoverShell != null)
				hoverShell.setVisible(false);
		}
	}

	void restoreStack() {
		showStack(false);

		theStack.setVisible(true);
		toolControl.setVisible(false);
	}

	@Execute
	public void showStack(@Named("show") boolean show) {
		CTabFolder ctf = (CTabFolder) theStack.getWidget();
		if (show) {
			showHover(false, null);
			inhibitHover = true;

			Composite showShell = getShell(SHOW_SHELL, trimStackTB.getShell());
			ctf.setParent(showShell);

			showShell.getDisplay().addFilter(SWT.MouseDown, mouseDownListener);

			// Button Hack to show a 'restore' button while avoiding the 'minimized' layout
			ctf.setMinimizeVisible(false);
			ctf.setMaximizeVisible(true);
			ctf.setMaximized(true);

			updateSelection(theStack.getSelectedElement());

			// Set the initial location
			Rectangle itemBounds = trimStackTB.getBounds();
			Point shellSize = showShell.getSize();
			Point loc = new Point(itemBounds.x - (shellSize.x / 2), itemBounds.y
					+ itemBounds.height);
			loc = trimStackTB.getDisplay().map(trimStackTB.getParent(), null, loc);
			showShell.setLocation(loc);

			showShell.layout(true);
			showShell.moveAbove(null);
			showShell.setVisible(true);
		} else {
			if (showShell != null) {
				showShell.setVisible(false);

				// clear any selected item
				updateSelection(null);

				showShell.getDisplay().removeFilter(SWT.MouseDown, mouseDownListener);

				// capture the current shell's bounds
				// Point size = showShell.getSize();
				// String sizeStr = size.toString();
				// toolControl.getPersistedState().put("Size", sizeStr);
			}

			// Button Hack to show a 'restore' button while avoiding the 'minimized' layout
			ctf.setMinimizeVisible(true);
			ctf.setMaximizeVisible(false);
			ctf.setMaximized(false);

			inhibitHover = false;
		}
	}

	private void updateSelection(MStackElement selectedElement) {
		// Show which view is up on the TB
		ToolItem[] items = trimStackTB.getItems();
		for (ToolItem item : items) {
			CTabItem cti = (CTabItem) item.getData();
			if (cti == null || cti.isDisposed())
				continue;

			item.setSelection(cti.getData(AbstractPartRenderer.OWNING_ME) == selectedElement);
		}
	}

	private Shell getShell(int shellType, Shell mainShell) {
		if (shellType == SHOW_SHELL && showShell != null)
			return showShell;
		if (shellType == HOVER_SHELL && hoverShell != null)
			return hoverShell;

		Shell theShell;
		if (shellType == HOVER_SHELL) {
			hoverShell = new Shell(mainShell, SWT.NONE);
			// hoverShell.setAlpha(128);
			hoverShell.setSize(300, 200);
			theShell = hoverShell;
		} else {
			showShell = new Shell(mainShell, SWT.RESIZE);
			showShell.setSize(600, 400);
			theShell = showShell;

			theShell.addListener(SWT.Traverse, escapeListener);
		}

		// Set a layout guaranteed not to affect the control's LayoutData
		theShell.setLayout(new Layout() {
			@Override
			protected void layout(Composite composite, boolean flushCache) {
				Rectangle bounds = ((Shell) composite).getClientArea();
				Control[] kids = composite.getChildren();
				if (kids.length == 1) {
					kids[0].setBounds(0, 0, bounds.width, bounds.height);
				}
			}

			@Override
			protected Point computeSize(Composite composite, int wHint, int hHint,
					boolean flushCache) {
				return new Point(600, 400);
			}
		});

		return theShell;

	}

	private Image getRestoreImage(Display display) {
		if (restoreImage == null) {
			restoreImage = WorkbenchImages
					.getImage(IWorkbenchGraphicConstants.IMG_ETOOL_RESTORE_TRIMPART);
			// restoreImage = new Image(display, 16, 16);
			// GC gc = new GC(restoreImage);
			// gc.setBackground(display.getSystemColor(SWT.COLOR_BLUE));
			// gc.fillRectangle(0, 0, 16, 16);
			// gc.setBackground(display.getSystemColor(SWT.COLOR_YELLOW));
			// gc.fillRectangle(3, 3, 10, 10);
			// gc.dispose();
		}
		return restoreImage;
	}

	void addTransparentTooltips(ToolBar newTB) {
		newTB.addMouseTrackListener(new MouseTrackListener() {
			public void mouseHover(MouseEvent e) {
				if (inhibitHover)
					return;
				ToolBar tb = (ToolBar) e.widget;
				Point loc = new Point(e.x, e.y);
				ToolItem ti = tb.getItem(loc);
				if (ti.getData() != null)
					showHover(true, ti);
			}

			public void mouseExit(MouseEvent e) {
				showHover(false, null);
			}

			public void mouseEnter(MouseEvent e) {
				showHover(false, null);
			}
		});

		newTB.addMouseListener(new MouseListener() {
			public void mouseUp(MouseEvent e) {
				showHover(false, null);
			}

			public void mouseDown(MouseEvent e) {
				showHover(false, null);
			}

			public void mouseDoubleClick(MouseEvent e) {
				showHover(false, null);
			}
		});

		newTB.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				showHover(false, null);
			}
		});
	}
}
