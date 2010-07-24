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
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.internal.workbench.swt.ShellActivationListener;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.widgets.CTabFolder;
import org.eclipse.e4.ui.widgets.CTabItem;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.renderers.swt.TrimmedPartLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
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

	private ToolBar trimStackTB;
	private boolean isShowing = false;
	private MPartStack theStack;
	private Composite hostPane;

	ControlListener caResizeListener = new ControlListener() {
		public void controlResized(ControlEvent e) {
			if (hostPane != null && hostPane.isVisible())
				setPaneLocation(hostPane);
		}

		public void controlMoved(ControlEvent e) {
		}
	};

	private Listener mouseDownListener = new Listener() {
		public void handleEvent(Event event) {
			if (event.widget instanceof Control) {
				Control control = (Control) event.widget;
				if (control.getShell() != hostPane.getParent())
					return;

				if (control == trimStackTB)
					return;

				// Is this control contained in the hosted stack ?
				while (!(control instanceof Shell)) {
					if (control == hostPane) {
						return;
					}
					control = control.getParent();
				}

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

			if (hostPane != null && hostPane.isVisible())
				updateSelection(theStack.getSelectedElement());
		}
	};

	private EventHandler toBeRenderedHandler = new EventHandler() {
		public void handleEvent(org.osgi.service.event.Event event) {
			if (theStack == null || trimStackTB == null)
				return;

			MUIElement changedElement = (MUIElement) event.getProperty(UIEvents.EventTags.ELEMENT);

			// if our stack is going away, so should we
			if (changedElement == theStack && !theStack.isToBeRendered()) {
				restoreStack();
				return;
			}

			// if one of the kids changes state, re-scrape the CTF
			MUIElement parentElement = changedElement.getParent();
			if (parentElement == theStack) {
				trimStackTB.getDisplay().asyncExec(new Runnable() {
					public void run() {
						updateTrimStackItems();
					}
				});
			}
		}
	};

	private EventHandler childrenHandler = new EventHandler() {
		public void handleEvent(org.osgi.service.event.Event event) {
			if (theStack == null || trimStackTB == null)
				return;

			Object changedObj = event.getProperty(UIEvents.EventTags.ELEMENT);

			// if a child has been added or removed, re-scape the CTF
			if (changedObj == theStack) {
				trimStackTB.getDisplay().asyncExec(new Runnable() {
					public void run() {
						updateTrimStackItems();
					}
				});
			}
		}
	};

	private EventHandler widgetHandler = new EventHandler() {
		@SuppressWarnings("restriction")
		public void handleEvent(org.osgi.service.event.Event event) {
			Object changedObj = event.getProperty(UIEvents.EventTags.ELEMENT);
			if (changedObj != theStack)
				return;

			if (theStack.getWidget() != null) {
				trimStackTB.getDisplay().asyncExec(new Runnable() {
					public void run() {
						updateTrimStackItems();
					}
				});
			}
		}
	};

	private MTrimBar bar;

	private int fixedSides;

	@PostConstruct
	void addListeners() {
		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.ElementContainer.TOPIC,
				UIEvents.ElementContainer.CHILDREN), childrenHandler);
		eventBroker.subscribe(
				UIEvents.buildTopic(UIEvents.UIElement.TOPIC, UIEvents.UIElement.TOBERENDERED),
				toBeRenderedHandler);
		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.ElementContainer.TOPIC,
				UIEvents.ElementContainer.SELECTEDELEMENT), selectionHandler);
		eventBroker.subscribe(
				UIEvents.buildTopic(UIEvents.UIElement.TOPIC, UIEvents.UIElement.WIDGET),
				widgetHandler);
	}

	@PostConstruct
	void createWidget(Composite parent, MToolControl me) {
		if (theStack == null) {
			theStack = findStack();
		}

		MUIElement meParent = me.getParent();
		int orientation = SWT.HORIZONTAL;
		if (meParent instanceof MTrimBar) {
			bar = (MTrimBar) meParent;
			if (bar.getSide() == SideValue.RIGHT || bar.getSide() == SideValue.LEFT)
				orientation = SWT.VERTICAL;
		}
		trimStackTB = new ToolBar(parent, orientation | SWT.FLAT | SWT.WRAP);

		ToolItem restoreBtn = new ToolItem(trimStackTB, SWT.PUSH);
		restoreBtn.setImage(getRestoreImage(parent.getDisplay()));
		restoreBtn.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				theStack.getTags().remove(MinMaxAddon.MINIMIZED);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				theStack.getTags().remove(MinMaxAddon.MINIMIZED);
			}
		});

		updateTrimStackItems();
	}

	/**
	 * @return
	 */
	private MPartStack findStack() {
		List<MPerspectiveStack> ps = modelService.findElements(window, null,
				MPerspectiveStack.class, null);
		if (ps.size() == 0) {
			String toolControlId = toolControl.getElementId();
			int index = toolControlId.indexOf('(');
			String stackId = toolControlId.substring(0, index);
			theStack = (MPartStack) modelService.find(stackId, window);
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

		return theStack;
	}

	private void updateTrimStackItems() {
		// Prevent exceptions on shutdown
		if (trimStackTB == null || trimStackTB.isDisposed())
			return;

		CTabFolder ctf = (CTabFolder) theStack.getWidget();
		if (ctf == null)
			return;

		if (ctf.getItemCount() == 0) {
			restoreStack();
			return;
		}

		// Remove any current items except the 'restore' button
		while (trimStackTB.getItemCount() > 1) {
			trimStackTB.getItem(trimStackTB.getItemCount() - 1).dispose();
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
					boolean show = item.getSelection();
					partService.activate((MPart) me);
					showStack(show);
				}

				public void widgetDefaultSelected(SelectionEvent e) {
					ToolItem item = (ToolItem) e.widget;
					CTabItem cti = (CTabItem) item.getData();
					MUIElement me = (MUIElement) cti.getData(AbstractPartRenderer.OWNING_ME);
					if (me instanceof MPlaceholder)
						me = ((MPlaceholder) me).getRef();
					boolean show = item.getSelection();
					partService.activate((MPart) me);
					showStack(show);
				}
			});
		}

		if (hostPane != null && hostPane.isVisible())
			updateSelection(theStack.getSelectedElement());

		trimStackTB.pack();
		trimStackTB.getShell().layout(new Control[] { trimStackTB }, SWT.DEFER);
	}

	void restoreStack() {
		showStack(false);

		// ensure the filter is removed
		Display.getCurrent().removeFilter(SWT.MouseDown, mouseDownListener);

		theStack.setVisible(true);
		theStack.getTags().remove(MinMaxAddon.MINIMIZED);
		toolControl.setToBeRendered(false);

		eventBroker.unsubscribe(toBeRenderedHandler);
		eventBroker.unsubscribe(childrenHandler);
		eventBroker.unsubscribe(selectionHandler);
		eventBroker.unsubscribe(widgetHandler);

		if (hostPane != null && !hostPane.isDisposed())
			hostPane.dispose();
		hostPane = null;

		if (trimStackTB != null && !trimStackTB.isDisposed())
			trimStackTB.dispose();
		trimStackTB = null;
	}

	@Execute
	public void showStack(@Named("show") boolean show) {
		CTabFolder ctf = (CTabFolder) theStack.getWidget();
		if (show && !isShowing) {
			hostPane = getHostPane();
			ctf.setParent(hostPane);

			hostPane.getDisplay().addFilter(SWT.MouseDown, mouseDownListener);
			getShellClientComposite().addControlListener(caResizeListener);

			updateSelection(theStack.getSelectedElement());

			// Set the initial location
			setPaneLocation(hostPane);

			hostPane.layout(true);
			hostPane.moveAbove(null);
			hostPane.setVisible(true);

			isShowing = true;
		} else if (!show && isShowing) {
			Display.getCurrent().removeFilter(SWT.MouseDown, mouseDownListener);

			// Check to ensure that the client area is non-null since the
			// trimstack may be currently hosted in the limbo shell
			Composite clientArea = getShellClientComposite();
			if (clientArea != null)
				clientArea.removeControlListener(caResizeListener);

			if (hostPane != null && hostPane.isVisible()) {
				hostPane.setVisible(false);

				// clear any selected item
				updateSelection(null);

				// capture the current shell's bounds
				Point size = hostPane.getSize();
				toolControl.getPersistedState().put("XSize", Integer.toString(size.x));
				toolControl.getPersistedState().put("YSize", Integer.toString(size.y));
			}

			isShowing = false;
		}
	}

	Composite getShellClientComposite() {
		if (trimStackTB.isDisposed()) {
			return null;
		}
		Shell theShell = trimStackTB.getShell();
		if (!(theShell.getLayout() instanceof TrimmedPartLayout))
			return null;

		TrimmedPartLayout tpl = (TrimmedPartLayout) theShell.getLayout();
		return tpl.clientArea;
	}

	/**
	 * @param showShell2
	 */
	private void setPaneLocation(Composite someShell) {
		Composite clientComp = getShellClientComposite();
		if (clientComp == null || clientComp.isDisposed())
			return;

		Rectangle caRect = getShellClientComposite().getBounds();

		Point paneSize = hostPane.getSize();
		Point loc = new Point(0, 0);

		if (isFixed(SWT.LEFT))
			loc.x = caRect.x;
		else
			loc.x = (caRect.x + caRect.width) - paneSize.x;

		if (isFixed(SWT.TOP))
			loc.y = caRect.y;
		else
			loc.y = (caRect.y + caRect.height) - paneSize.y;

		someShell.setLocation(loc);
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

	private Composite getHostPane() {
		if (hostPane != null)
			return hostPane;

		// Create one
		hostPane = new Composite(trimStackTB.getShell(), SWT.NONE);
		hostPane.setData(ShellActivationListener.DIALOG_IGNORE_KEY, Boolean.TRUE);

		int xSize = 600;
		String xSizeStr = toolControl.getPersistedState().get("XSize");
		if (xSizeStr != null)
			xSize = Integer.parseInt(xSizeStr);
		int ySize = 400;
		String ySizeStr = toolControl.getPersistedState().get("YSize");
		if (ySizeStr != null)
			ySize = Integer.parseInt(ySizeStr);
		hostPane.setSize(xSize, ySize);
		hostPane.addListener(SWT.Traverse, escapeListener);

		// Set a special layout that allows resizing
		fixedSides = getFixedSides();
		hostPane.setLayout(new TrimPaneLayout(fixedSides));

		return hostPane;
	}

	private int getFixedSides() {
		int verticalValue = SWT.TOP; // should be based on the center of the TB
		if (bar.getSide() == SideValue.LEFT)
			return SWT.LEFT | verticalValue;
		else if (bar.getSide() == SideValue.RIGHT)
			return SWT.RIGHT | verticalValue;

		return SWT.TOP | SWT.LEFT;
	}

	private Image getRestoreImage(Display display) {
		if (restoreImage == null) {
			restoreImage = WorkbenchImages
					.getImage(IWorkbenchGraphicConstants.IMG_ETOOL_RESTORE_TRIMPART);
		}
		return restoreImage;
	}

	private boolean isFixed(int swtSide) {
		return (fixedSides & swtSide) != 0;
	}
}
