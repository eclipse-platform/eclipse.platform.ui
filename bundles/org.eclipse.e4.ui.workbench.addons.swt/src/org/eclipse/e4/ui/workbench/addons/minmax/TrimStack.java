/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.addons.minmax;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.internal.workbench.swt.ShellActivationListener;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
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
import org.eclipse.e4.ui.workbench.IResourceUtilities;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.renderers.swt.TrimmedPartLayout;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.osgi.service.event.EventHandler;

/**
 * Class for representing window trim containing minimized views and shared areas
 */
public class TrimStack {

	/**
	 * Contribution URI for this class
	 */
	public static String CONTRIBUTION_URI = "platform:/plugin/org.eclipse.e4.ui.workbench.addons.swt/org.eclipse.e4.ui.workbench.addons.minmax.TrimStack"; //$NON-NLS-1$

	private static final String LAYOUT_ICON_URI = "platform:/plugin/org.eclipse.e4.ui.workbench.addons.swt/icons/full/obj16/layout_co.gif"; //$NON-NLS-1$

	private static final String RESTORE_ICON_URI = "platform:/plugin/org.eclipse.e4.ui.workbench.addons.swt/icons/full/etool16/fastview_restore.gif"; //$NON-NLS-1$

	private static final String STATE_XSIZE = "XSize"; //$NON-NLS-1$

	private static final String STATE_YSIZE = "YSize"; //$NON-NLS-1$

	private Image layoutImage;

	private Image restoreImage;

	private ToolBar trimStackTB;

	/**
	 * The context menu for this trim stack's items.
	 */
	private Menu trimStackMenu;

	/**
	 * The tool item that the cursor is currently hovering over.
	 */
	private ToolItem selectedToolItem;
	private boolean isShowing = false;
	private MUIElement minimizedElement;
	private Composite hostPane;

	@Inject
	@Named("org.eclipse.e4.ui.workbench.IResourceUtilities")
	private IResourceUtilities<ImageDescriptor> resUtils;

	/**
	 * A map of created images from a part's icon URI path.
	 */
	private Map<String, Image> imageMap = new HashMap<String, Image>();

	ControlListener caResizeListener = new ControlListener() {
		public void controlResized(ControlEvent e) {
			if (hostPane != null && hostPane.isVisible())
				setPaneLocation(hostPane);
		}

		public void controlMoved(ControlEvent e) {
		}
	};

	// Listens to ESC and closes the active fast view
	private Listener escapeListener = new Listener() {
		public void handleEvent(Event event) {
			if (event.character == SWT.ESC) {
				partService.requestActivation();
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

	private EventHandler closeHandler = new EventHandler() {
		public void handleEvent(org.osgi.service.event.Event event) {
			if (!isShowing)
				return;

			// The only time we don't close is if I've selected my tab.
			MUIElement changedElement = (MUIElement) event.getProperty(UIEvents.EventTags.ELEMENT);

			// Perspective changed, close the visible stacks
			if (changedElement instanceof MPerspectiveStack) {
				showStack(false);
				return;
			}

			if (changedElement == getLeafPart(minimizedElement)) {
				fixToolItemSelection((MPart) changedElement);
				return;
			}

			showStack(false);
		}
	};

	private void fixToolItemSelection(MPart part) {
		if (trimStackTB == null || trimStackTB.isDisposed())
			return;

		if (isEditorStack()) {
			trimStackTB.getItem(1).setSelection(part != null);
			if (part != null)
				trimStackTB.getItem(1).setData(part);
		} else {
			for (ToolItem item : trimStackTB.getItems()) {
				boolean result = item.getData() == null ? false : item.getData() == part;
				item.setSelection(result);
			}
		}

	}

	private boolean isEditorStack() {
		return minimizedElement instanceof MPlaceholder;
	}

	private MPart getLeafPart(MUIElement element) {
		if (element instanceof MPlaceholder)
			return getLeafPart(((MPlaceholder) element).getRef());

		if (element instanceof MElementContainer<?>)
			return getLeafPart(((MElementContainer<?>) element).getSelectedElement());

		if (element instanceof MPart)
			return (MPart) element;

		return null;
	}

	private EventHandler openHandler = new EventHandler() {
		public void handleEvent(org.osgi.service.event.Event event) {
			if (isShowing)
				return;

			MUIElement changedElement = (MUIElement) event.getProperty(UIEvents.EventTags.ELEMENT);

			// Open if shared area
			if (getLeafPart(minimizedElement) == changedElement) {
				showStack(true);
				return;
			}

			MUIElement selectedElement = null;

			if (minimizedElement instanceof MPlaceholder) {
				selectedElement = ((MPlaceholder) minimizedElement).getRef();
			} else if (minimizedElement instanceof MPartStack) {
				selectedElement = ((MPartStack) minimizedElement).getSelectedElement();
			}

			if (selectedElement == null)
				return;

			if (selectedElement instanceof MPlaceholder)
				selectedElement = ((MPlaceholder) selectedElement).getRef();

			if (changedElement != selectedElement)
				return;

			showStack(true);
		}
	};

	private EventHandler toBeRenderedHandler = new EventHandler() {
		public void handleEvent(org.osgi.service.event.Event event) {
			if (minimizedElement == null || trimStackTB == null)
				return;

			MUIElement changedElement = (MUIElement) event.getProperty(UIEvents.EventTags.ELEMENT);

			// if our stack is going away, so should we
			if (changedElement == minimizedElement && !minimizedElement.isToBeRendered()) {
				restoreStack();
				return;
			}

			// if one of the kids changes state, re-scrape the CTF
			MUIElement parentElement = changedElement.getParent();
			if (parentElement == minimizedElement) {
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
			if (minimizedElement == null || trimStackTB == null)
				return;

			Object changedObj = event.getProperty(UIEvents.EventTags.ELEMENT);

			// if a child has been added or removed, re-scape the CTF
			if (changedObj == minimizedElement) {
				trimStackTB.getDisplay().asyncExec(new Runnable() {
					public void run() {
						updateTrimStackItems();
					}
				});
			}
		}
	};

	private EventHandler widgetHandler = new EventHandler() {
		public void handleEvent(org.osgi.service.event.Event event) {
			Object changedObj = event.getProperty(UIEvents.EventTags.ELEMENT);
			if (changedObj != minimizedElement)
				return;

			if (minimizedElement.getWidget() != null) {
				trimStackTB.getDisplay().asyncExec(new Runnable() {
					public void run() {
						updateTrimStackItems();
					}
				});
			}
		}
	};

	// Listener attached to every ToolItem in a TrimStack. Responsible for activating the
	// appropriate part.
	private SelectionListener toolItemSelectionListener = new SelectionListener() {
		public void widgetSelected(SelectionEvent e) {
			ToolItem toolItem = (ToolItem) e.widget;
			MUIElement uiElement = (MUIElement) toolItem.getData();
			if (toolItem.getSelection()) {
				partService.activate((MPart) uiElement);
			} else {
				// Get partService to activate a part visible in the presentation
				partService.requestActivation();
			}
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
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
		eventBroker.subscribe(
				UIEvents.buildTopic(UIEvents.UIElement.TOPIC, UIEvents.UIElement.WIDGET),
				widgetHandler);
		eventBroker.subscribe(UIEvents.UILifeCycle.BRINGTOTOP, openHandler);
		eventBroker.subscribe(UIEvents.UILifeCycle.ACTIVATE, closeHandler);
	}

	@PreDestroy
	void removeListeners() {
		eventBroker.unsubscribe(toBeRenderedHandler);
		eventBroker.unsubscribe(childrenHandler);
		eventBroker.unsubscribe(widgetHandler);
		eventBroker.unsubscribe(openHandler);
		eventBroker.unsubscribe(closeHandler);
	}

	@PostConstruct
	void createWidget(Composite parent, MToolControl me) {
		if (minimizedElement == null) {
			minimizedElement = findElement();
		}

		MUIElement meParent = me.getParent();
		int orientation = SWT.HORIZONTAL;
		if (meParent instanceof MTrimBar) {
			bar = (MTrimBar) meParent;
			if (bar.getSide() == SideValue.RIGHT || bar.getSide() == SideValue.LEFT)
				orientation = SWT.VERTICAL;
		}
		trimStackTB = new ToolBar(parent, orientation | SWT.FLAT | SWT.WRAP);
		trimStackTB.addListener(SWT.MenuDetect, new Listener() {
			public void handleEvent(Event event) {
				// remap the coordinate relative to the control
				Point point = trimStackTB.getDisplay().map(null, trimStackTB,
						new Point(event.x, event.y));
				// get the selected item in question
				selectedToolItem = trimStackTB.getItem(point);
			}
		});

		if (minimizedElement instanceof MPartStack)
			createPopupMenu();

		ToolItem restoreBtn = new ToolItem(trimStackTB, SWT.PUSH);
		restoreBtn.setToolTipText(Messages.TrimStack_RestoreText);
		restoreBtn.setImage(getRestoreImage());
		restoreBtn.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				minimizedElement.getTags().remove(MinMaxAddon.MINIMIZED);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				minimizedElement.getTags().remove(MinMaxAddon.MINIMIZED);
			}
		});

		updateTrimStackItems();
	}

	@PreDestroy
	void destroy() {
		for (Image image : imageMap.values()) {
			image.dispose();
		}

		if (layoutImage != null) {
			layoutImage.dispose();
			layoutImage = null;
		}

		if (restoreImage != null) {
			restoreImage.dispose();
			restoreImage = null;
		}
	}

	private MUIElement findElement() {
		MUIElement result;
		List<MPerspectiveStack> ps = modelService.findElements(window, null,
				MPerspectiveStack.class, null);
		if (ps.size() == 0) {
			String toolControlId = toolControl.getElementId();
			int index = toolControlId.indexOf('(');
			String stackId = toolControlId.substring(0, index);
			result = modelService.find(stackId, window);
		} else {
			String toolControlId = toolControl.getElementId();
			int index = toolControlId.indexOf('(');
			String stackId = toolControlId.substring(0, index);
			String perspId = toolControlId.substring(index + 1, toolControlId.length() - 1);
			MPerspective persp = (MPerspective) modelService.find(perspId, ps.get(0));
			if (persp != null) {
				result = modelService.find(stackId, persp);
			} else {
				result = modelService.find(stackId, window);
			}
		}

		return result;
	}

	private String getLabel(MUILabel label) {
		String string = label.getLabel();
		return string == null ? "" : string; //$NON-NLS-1$
	}

	private Image getImage(MUILabel element) {
		String iconURI = element.getIconURI();
		if (iconURI != null && iconURI.length() > 0) {
			Image image = imageMap.get(iconURI);
			if (image == null) {
				image = resUtils.imageDescriptorFromURI(URI.createURI(iconURI)).createImage();
				imageMap.put(iconURI, image);
			}
			return image;
		}
		return null;
	}

	private MPart getPart(MStackElement element) {
		if (element instanceof MPart) {
			return (MPart) element;
		}
		return (MPart) ((MPlaceholder) element).getRef();
	}

	private void updateTrimStackItems() {
		// Prevent exceptions on shutdown
		if (trimStackTB == null || trimStackTB.isDisposed())
			return;

		if (isEditorStack()) {
			if (trimStackTB.getItemCount() == 1) {
				MUIElement data = getLeafPart(minimizedElement);
				if (data != null) {
					ToolItem ti = new ToolItem(trimStackTB, SWT.CHECK);
					ti.setToolTipText(Messages.TrimStack_SharedAreaTooltip);
					ti.setImage(getLayoutImage());
					ti.setData(data);
					ti.addSelectionListener(toolItemSelectionListener);
				}
			}
		} else if (minimizedElement instanceof MPartStack) {
			MPartStack theStack = (MPartStack) minimizedElement;
			if (theStack.getWidget() == null) {
				return;
			}

			// check to see if this stack has any valid elements
			boolean check = false;
			for (MStackElement stackElement : theStack.getChildren()) {
				if (stackElement.isToBeRendered()) {
					check = true;
					break;
				}
			}

			if (!check) {
				// doesn't have any children that's showing, place it back in the presentation
				restoreStack();
				return;
			}

			// Remove any current items except the 'restore' button
			while (trimStackTB.getItemCount() > 1) {
				trimStackTB.getItem(trimStackTB.getItemCount() - 1).dispose();
			}

			for (MStackElement stackElement : theStack.getChildren()) {
				if (!stackElement.isToBeRendered()) {
					continue;
				}

				MPart part = getPart(stackElement);
				ToolItem newItem = new ToolItem(trimStackTB, SWT.CHECK);
				newItem.setData(part);
				newItem.setImage(getImage(part));
				newItem.setToolTipText(getLabel(part));
				newItem.addSelectionListener(toolItemSelectionListener);
			}
		}

		trimStackTB.pack();
		trimStackTB.getShell().layout(new Control[] { trimStackTB }, SWT.DEFER);
		trimStackTB.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				trimStackTB = null;
			}
		});
	}

	void restoreStack() {
		minimizedElement.setVisible(true);
		minimizedElement.getTags().remove(MinMaxAddon.MINIMIZED);
		toolControl.setToBeRendered(false);

		if (hostPane != null && !hostPane.isDisposed())
			hostPane.dispose();
		hostPane = null;
	}

	/**
	 * Create the popup menu that will appear when a minimized part has been selected by the cursor.
	 */
	private void createPopupMenu() {
		trimStackMenu = new Menu(trimStackTB);
		trimStackTB.setMenu(trimStackMenu);

		MenuItem closeItem = new MenuItem(trimStackMenu, SWT.NONE);
		closeItem.setText(Messages.TrimStack_CloseText);
		closeItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				partService.hidePart((MPart) selectedToolItem.getData());
			}
		});
	}

	protected void showStack(boolean show) {
		Control ctf = (Control) minimizedElement.getWidget();
		Composite clientArea = getShellClientComposite();
		if (clientArea == null)
			return;

		if (show && !isShowing) {
			hostPane = getHostPane();
			ctf.setParent(hostPane);

			clientArea.addControlListener(caResizeListener);

			// Set the initial location
			setPaneLocation(hostPane);

			hostPane.layout(true);
			hostPane.moveAbove(null);
			hostPane.setVisible(true);

			isShowing = true;
		} else if (!show && isShowing) {
			// Check to ensure that the client area is non-null since the
			// trimstack may be currently hosted in the limbo shell
			if (clientArea != null)
				clientArea.removeControlListener(caResizeListener);

			if (hostPane != null && hostPane.isVisible()) {
				hostPane.setVisible(false);

				// capture the current shell's bounds
				Point size = hostPane.getSize();
				toolControl.getPersistedState().put(STATE_XSIZE, Integer.toString(size.x));
				toolControl.getPersistedState().put(STATE_YSIZE, Integer.toString(size.y));
			}

			fixToolItemSelection(null);
			isShowing = false;
		}
	}

	Composite getShellClientComposite() {
		if (trimStackTB == null || trimStackTB.isDisposed()) {
			return null;
		}
		Shell theShell = trimStackTB.getShell();
		if (!(theShell.getLayout() instanceof TrimmedPartLayout))
			return null;

		TrimmedPartLayout tpl = (TrimmedPartLayout) theShell.getLayout();
		return tpl.clientArea;
	}

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

	private Composite getHostPane() {
		if (hostPane != null)
			return hostPane;

		// Create one
		hostPane = new Composite(trimStackTB.getShell(), SWT.NONE);
		hostPane.setData(ShellActivationListener.DIALOG_IGNORE_KEY, Boolean.TRUE);

		int xSize = 600;
		String xSizeStr = toolControl.getPersistedState().get(STATE_XSIZE);
		if (xSizeStr != null)
			xSize = Integer.parseInt(xSizeStr);
		int ySize = 400;
		String ySizeStr = toolControl.getPersistedState().get(STATE_YSIZE);
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

	private Image getLayoutImage() {
		if (layoutImage == null) {
			layoutImage = resUtils.imageDescriptorFromURI(URI.createURI(LAYOUT_ICON_URI))
					.createImage();
		}
		return layoutImage;
	}

	private Image getRestoreImage() {
		if (restoreImage == null) {
			restoreImage = resUtils.imageDescriptorFromURI(URI.createURI(RESTORE_ICON_URI))
					.createImage();
		}
		return restoreImage;
	}

	private boolean isFixed(int swtSide) {
		return (fixedSides & swtSide) != 0;
	}
}
