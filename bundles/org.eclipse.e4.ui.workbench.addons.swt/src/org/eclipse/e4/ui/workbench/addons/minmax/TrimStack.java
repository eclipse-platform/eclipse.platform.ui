/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
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
import java.util.Map.Entry;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.internal.workbench.swt.CSSRenderingUtils;
import org.eclipse.e4.ui.internal.workbench.swt.ShellActivationListener;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MGenericStack;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
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
import org.eclipse.swt.events.SelectionAdapter;
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
	public static String CONTRIBUTION_URI = "bundleclass://org.eclipse.e4.ui.workbench.addons.swt/org.eclipse.e4.ui.workbench.addons.minmax.TrimStack"; //$NON-NLS-1$

	private static final String LAYOUT_ICON_URI = "platform:/plugin/org.eclipse.e4.ui.workbench.addons.swt/icons/full/obj16/layout_co.gif"; //$NON-NLS-1$

	private static final String RESTORE_ICON_URI = "platform:/plugin/org.eclipse.e4.ui.workbench.addons.swt/icons/full/etool16/fastview_restore.gif"; //$NON-NLS-1$

	static final String STATE_XSIZE = "XSize"; //$NON-NLS-1$

	static final String STATE_YSIZE = "YSize"; //$NON-NLS-1$

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
				showStack(false);
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

	private Image getOverrideImage(MUIElement element) {
		Image result = null;

		Object imageObject = element.getTransientData().get(
				IPresentationEngine.OVERRIDE_ICON_IMAGE_KEY);
		if (imageObject != null && imageObject instanceof Image
				&& !((Image) imageObject).isDisposed())
			result = (Image) imageObject;
		return result;
	}

	private String getOverrideTitleToolTip(MUIElement element) {
		String result = null;

		Object stringObject = element.getTransientData().get(
				IPresentationEngine.OVERRIDE_TITLE_TOOL_TIP_KEY);
		if (stringObject != null && stringObject instanceof String)
			result = (String) stringObject;

		return result;
	}

	/**
	 * This is the new way to handle UIEvents (as opposed to subscring and unsubscribing them with
	 * the event broker.
	 * 
	 * The method is described in detail at http://wiki.eclipse.org/Eclipse4/RCP/Event_Model
	 */
	@SuppressWarnings("unchecked")
	@Inject
	@Optional
	private void handleTransientDataEvents(
			@UIEventTopic(UIEvents.ApplicationElement.TOPIC_TRANSIENTDATA) org.osgi.service.event.Event event) {
		// Prevent exceptions on shutdown
		if (trimStackTB == null || trimStackTB.isDisposed() || minimizedElement.getWidget() == null)
			return;

		MUIElement changedElement = (MUIElement) event.getProperty(UIEvents.EventTags.ELEMENT);

		String key;
		if (UIEvents.isREMOVE(event)) {
			key = ((Entry<String, Object>) event.getProperty(UIEvents.EventTags.OLD_VALUE))
					.getKey();
		} else {
			key = ((Entry<String, Object>) event.getProperty(UIEvents.EventTags.NEW_VALUE))
					.getKey();
		}

		if (key.equals(IPresentationEngine.OVERRIDE_ICON_IMAGE_KEY)) {
			ToolItem toolItem = getChangedToolItem(changedElement);
			if (toolItem != null)
				toolItem.setImage(getImage((MUILabel) toolItem.getData()));
		} else if (key.equals(IPresentationEngine.OVERRIDE_TITLE_TOOL_TIP_KEY)) {
			ToolItem toolItem = getChangedToolItem(changedElement);
			if (toolItem != null)
				toolItem.setToolTipText(getLabelText((MUILabel) toolItem.getData()));
		}
	}

	private ToolItem getChangedToolItem(MUIElement changedElement) {
		ToolItem[] toolItems = trimStackTB.getItems();
		for (ToolItem toolItem : toolItems) {
			if (changedElement.equals(toolItem.getData())) {
				return toolItem;
			}
		}
		return null;
	}

	/**
	 * This is the old way to subscribe to UIEvents. You should consider using the new way as shown
	 * by handleTransientDataEvents() and described in the article at
	 * http://wiki.eclipse.org/Eclipse4/RCP/Event_Model
	 */
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
				fixToolItemSelection();
				return;
			}

			showStack(false);
		}
	};

	private void fixToolItemSelection() {
		if (trimStackTB == null || trimStackTB.isDisposed())
			return;

		if (!isShowing) {
			// Not open...no selection
			for (ToolItem item : trimStackTB.getItems()) {
				item.setSelection(false);
			}
		} else {
			if (isEditorStack()) {
				trimStackTB.getItem(1).setSelection(true);
			} else if (isPerspectiveStack()) {
				MPerspectiveStack pStack = (MPerspectiveStack) minimizedElement;
				MUIElement selElement = pStack.getSelectedElement();
				for (ToolItem item : trimStackTB.getItems()) {
					item.setSelection(item.getData() == selElement);
				}
			} else {
				MPartStack partStack = (MPartStack) minimizedElement;
				MUIElement selElement = partStack.getSelectedElement();
				if (selElement instanceof MPlaceholder)
					selElement = ((MPlaceholder) selElement).getRef();

				for (ToolItem item : trimStackTB.getItems()) {
					boolean isSel = item.getData() == selElement;
					item.setSelection(isSel);
				}
			}
		}
	}

	private boolean isEditorStack() {
		return minimizedElement instanceof MPlaceholder;
	}

	private boolean isPerspectiveStack() {
		return minimizedElement instanceof MPerspectiveStack;
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

	/**
	 * This is the old way to subscribe to UIEvents. You should consider using the new way as shown
	 * by handleTransientDataEvents() and described in the article at
	 * http://wiki.eclipse.org/Eclipse4/RCP/Event_Model
	 */
	private EventHandler openHandler = new EventHandler() {
		public void handleEvent(org.osgi.service.event.Event event) {
			if (isShowing)
				return;

			MUIElement changedElement = (MUIElement) event.getProperty(UIEvents.EventTags.ELEMENT);

			// Open if shared area
			if (getLeafPart(minimizedElement) == changedElement
					&& !(minimizedElement instanceof MPerspectiveStack)) {
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

	/**
	 * This is the old way to subscribe to UIEvents. You should consider using the new way as shown
	 * by handleTransientDataEvents() and described in the article at
	 * http://wiki.eclipse.org/Eclipse4/RCP/Event_Model
	 */
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

	/**
	 * This is the old way to subscribe to UIEvents. You should consider using the new way as shown
	 * by handleTransientDataEvents() and described in the article at
	 * http://wiki.eclipse.org/Eclipse4/RCP/Event_Model
	 */
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

	/**
	 * This is the old way to subscribe to UIEvents. You should consider using the new way as shown
	 * by handleTransientDataEvents() and described in the article at
	 * http://wiki.eclipse.org/Eclipse4/RCP/Event_Model
	 */
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

	/**
	 * This is the old way to subscribe to UIEvents. You should consider using the new way as shown
	 * by handleTransientDataEvents() and described in the article at
	 * http://wiki.eclipse.org/Eclipse4/RCP/Event_Model
	 */
	// Listener attached to every ToolItem in a TrimStack. Responsible for activating the
	// appropriate part.
	private SelectionListener toolItemSelectionListener = new SelectionListener() {
		public void widgetSelected(SelectionEvent e) {
			ToolItem toolItem = (ToolItem) e.widget;
			MUIElement uiElement = (MUIElement) toolItem.getData();

			// Clicking on the already showing item ? NOTE: teh Selection will already have been
			// turned off by the time the event arrives
			if (!toolItem.getSelection()) {
				partService.requestActivation();
				showStack(false);
				return;
			}

			if (uiElement instanceof MPart) {
				partService.activate((MPart) uiElement);
			} else if (uiElement instanceof MPerspective) {
				uiElement.getParent().setSelectedElement(uiElement);
			}
			showStack(true);
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}
	};

	private MTrimBar bar;

	private int fixedSides;

	/**
	 * This is the old way to subscribe to UIEvents. You should consider using the new way as shown
	 * by handleTransientDataEvents() and described in the article at
	 * http://wiki.eclipse.org/Eclipse4/RCP/Event_Model
	 */
	@PostConstruct
	void addListeners() {
		eventBroker.subscribe(UIEvents.ElementContainer.TOPIC_CHILDREN, childrenHandler);
		eventBroker.subscribe(UIEvents.UIElement.TOPIC_TOBERENDERED, toBeRenderedHandler);
		eventBroker.subscribe(UIEvents.UIElement.TOPIC_WIDGET, widgetHandler);
		eventBroker.subscribe(UIEvents.UILifeCycle.BRINGTOTOP, openHandler);
		eventBroker.subscribe(UIEvents.UILifeCycle.ACTIVATE, closeHandler);
	}

	/**
	 * This is the old way to subscribe to UIEvents. You should consider using the new way as shown
	 * by handleTransientDataEvents() and described in the article at
	 * http://wiki.eclipse.org/Eclipse4/RCP/Event_Model
	 */
	@PreDestroy
	void removeListeners() {
		eventBroker.unsubscribe(toBeRenderedHandler);
		eventBroker.unsubscribe(childrenHandler);
		eventBroker.unsubscribe(widgetHandler);
		eventBroker.unsubscribe(openHandler);
		eventBroker.unsubscribe(closeHandler);
	}

	@PostConstruct
	void createWidget(Composite parent, MToolControl me, CSSRenderingUtils cssUtils) {
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
		trimStackTB.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				trimStackTB = null;
				trimStackMenu = null;
			}
		});

		trimStackTB.addListener(SWT.MenuDetect, new Listener() {
			public void handleEvent(Event event) {
				// Clear any existing items
				while (trimStackMenu.getItemCount() > 0)
					trimStackMenu.getItem(0).dispose();

				// remap the coordinate relative to the control
				Point point = trimStackTB.getDisplay().map(null, trimStackTB,
						new Point(event.x, event.y));
				// get the selected item in question
				selectedToolItem = trimStackTB.getItem(point);

				if (selectedToolItem == null)
					return;

				final MPart menuPart = selectedToolItem.getData() instanceof MPart ? (MPart) selectedToolItem
						.getData() : null;
				if (menuPart == null)
					return;

				MenuItem orientationItem = new MenuItem(trimStackMenu, SWT.CASCADE);
				orientationItem.setText(Messages.TrimStack_OrientationMenu);
				Menu orientationMenu = new Menu(orientationItem);
				orientationItem.setMenu(orientationMenu);

				MenuItem defaultItem = new MenuItem(orientationMenu, SWT.RADIO);
				defaultItem.setText(Messages.TrimStack_DefaultOrientationItem);
				defaultItem.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						menuPart.getTags().remove(IPresentationEngine.ORIENTATION_HORIZONTAL);
						menuPart.getTags().remove(IPresentationEngine.ORIENTATION_VERTICAL);
						if (isShowing) {
							setPaneLocation(hostPane);
						}
					}
				});

				MenuItem horizontalItem = new MenuItem(orientationMenu, SWT.RADIO);
				horizontalItem.setText(Messages.TrimStack_Horizontal);
				horizontalItem.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						if (menuPart.getTags().contains(IPresentationEngine.ORIENTATION_HORIZONTAL)) {
							menuPart.getTags().remove(IPresentationEngine.ORIENTATION_HORIZONTAL);
						} else {
							menuPart.getTags().remove(IPresentationEngine.ORIENTATION_VERTICAL);
							menuPart.getTags().add(IPresentationEngine.ORIENTATION_HORIZONTAL);
						}
						if (isShowing) {
							setPaneLocation(hostPane);
						}
					}
				});

				MenuItem verticalItem = new MenuItem(orientationMenu, SWT.RADIO);
				verticalItem.setText(Messages.TrimStack_Vertical);

				verticalItem.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						if (menuPart.getTags().contains(IPresentationEngine.ORIENTATION_VERTICAL)) {
							menuPart.getTags().remove(IPresentationEngine.ORIENTATION_VERTICAL);
						} else {
							menuPart.getTags().remove(IPresentationEngine.ORIENTATION_HORIZONTAL);
							menuPart.getTags().add(IPresentationEngine.ORIENTATION_VERTICAL);
						}
						if (isShowing) {
							setPaneLocation(hostPane);
						}
					}
				});

				// Set initial orientation selection
				if (menuPart.getTags().contains(IPresentationEngine.ORIENTATION_HORIZONTAL)) {
					horizontalItem.setSelection(true);
				} else if (menuPart.getTags().contains(IPresentationEngine.ORIENTATION_VERTICAL)) {
					verticalItem.setSelection(true);
				} else {
					defaultItem.setSelection(true);
				}

				MenuItem restoreItem = new MenuItem(trimStackMenu, SWT.NONE);
				restoreItem.setText(Messages.TrimStack_RestoreText);
				restoreItem.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						minimizedElement.getTags().remove(IPresentationEngine.MINIMIZED);
						partService.activate(menuPart);
					}
				});

				MenuItem closeItem = new MenuItem(trimStackMenu, SWT.NONE);
				closeItem.setText(Messages.TrimStack_CloseText);
				closeItem.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						partService.hidePart(menuPart);
					}
				});

			}
		});

		if (minimizedElement instanceof MPartStack) {
			trimStackMenu = new Menu(trimStackTB);
			trimStackTB.setMenu(trimStackMenu);
		}

		ToolItem restoreBtn = new ToolItem(trimStackTB, SWT.PUSH);
		restoreBtn.setToolTipText(Messages.TrimStack_RestoreText);
		restoreBtn.setImage(getRestoreImage());
		restoreBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				minimizedElement.getTags().remove(IPresentationEngine.MINIMIZED);
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

	private String getLabelText(MUILabel label) {
		// Use override text if available
		if (label instanceof MUIElement) {
			String text = getOverrideTitleToolTip((MUIElement) label);
			if (text != null)
				return text;
		}

		String string = label.getLabel();
		return string == null ? "" : string; //$NON-NLS-1$
	}

	private Image getImage(MUILabel element) {
		// Use override image if available
		if (element instanceof MUIElement) {
			Image image = getOverrideImage((MUIElement) element);
			if (image != null)
				return image;
		}

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

	private MUILabel getLabelElement(MUIElement element) {
		if (element instanceof MPlaceholder)
			element = ((MPlaceholder) element).getRef();

		return (MUILabel) (element instanceof MUILabel ? element : null);
	}

	private void updateTrimStackItems() {
		// Prevent exceptions on shutdown
		if (trimStackTB == null || trimStackTB.isDisposed() || minimizedElement.getWidget() == null)
			return;

		// Remove any current items except the 'restore' button
		while (trimStackTB.getItemCount() > 1) {
			trimStackTB.getItem(trimStackTB.getItemCount() - 1).dispose();
		}

		if (isEditorStack() && trimStackTB.getItemCount() == 1) {
			MUIElement data = getLeafPart(minimizedElement);
			ToolItem ti = new ToolItem(trimStackTB, SWT.CHECK);
			ti.setToolTipText(Messages.TrimStack_SharedAreaTooltip);
			ti.setImage(getLayoutImage());
			ti.setData(data);
			ti.addSelectionListener(toolItemSelectionListener);
		} else if (minimizedElement instanceof MGenericStack<?>) {
			// Handle *both* PartStacks and PerspectiveStacks here...
			MGenericStack<?> theStack = (MGenericStack<?>) minimizedElement;

			// check to see if this stack has any valid elements
			boolean check = false;
			for (MUIElement stackElement : theStack.getChildren()) {
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

			for (MUIElement stackElement : theStack.getChildren()) {
				if (!stackElement.isToBeRendered()) {
					continue;
				}

				MUILabel labelElement = getLabelElement(stackElement);
				ToolItem newItem = new ToolItem(trimStackTB, SWT.CHECK);
				newItem.setData(labelElement);
				newItem.setImage(getImage(labelElement));
				newItem.setToolTipText(getLabelText(labelElement));
				newItem.addSelectionListener(toolItemSelectionListener);
			}
		}

		trimStackTB.pack();
		trimStackTB.getShell().layout(new Control[] { trimStackTB }, SWT.DEFER);
	}

	void restoreStack() {
		minimizedElement.setVisible(true);
		minimizedElement.getTags().remove(IPresentationEngine.MINIMIZED);
		toolControl.setToBeRendered(false);

		if (hostPane != null && !hostPane.isDisposed())
			hostPane.dispose();
		hostPane = null;
	}

	/**
	 * Sets whether this stack should be visible or hidden
	 * 
	 * @param show
	 *            whether the stack should be visible
	 */
	public void showStack(boolean show) {
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
			fixToolItemSelection();
		} else if (!show && isShowing) {
			// Check to ensure that the client area is non-null since the
			// trimstack may be currently hosted in the limbo shell
			if (clientArea != null)
				clientArea.removeControlListener(caResizeListener);

			if (hostPane != null && hostPane.isVisible()) {
				hostPane.setVisible(false);
			}

			isShowing = false;
			fixToolItemSelection();
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

		// NOTE: always starts in the persisted (or default) size
		Point paneSize = getHostPane().getSize();

		// Ensure it's not clipped
		if (paneSize.x > caRect.width)
			paneSize.x = caRect.width;
		if (paneSize.y > caRect.height)
			paneSize.y = caRect.height;

		if (minimizedElement instanceof MPartStack) {
			MPartStack stack = (MPartStack) minimizedElement;
			MUIElement stackSel = stack.getSelectedElement();
			if (stackSel instanceof MPlaceholder)
				stackSel = ((MPlaceholder) stackSel).getRef();
			if (stackSel instanceof MPart) {
				if (stackSel.getTags().contains(IPresentationEngine.ORIENTATION_HORIZONTAL))
					paneSize.x = caRect.width;
				if (stackSel.getTags().contains(IPresentationEngine.ORIENTATION_VERTICAL))
					paneSize.y = caRect.height;
			}
		}
		Point loc = new Point(0, 0);

		if (isFixed(SWT.LEFT))
			loc.x = caRect.x;
		else
			loc.x = (caRect.x + caRect.width) - paneSize.x;

		if (isFixed(SWT.TOP))
			loc.y = caRect.y;
		else
			loc.y = (caRect.y + caRect.height) - paneSize.y;

		someShell.setSize(paneSize);
		someShell.setLocation(loc);
	}

	private void setHostSize() {
		if (hostPane == null || hostPane.isDisposed())
			return;

		int xSize = 600;
		String xSizeStr = toolControl.getPersistedState().get(STATE_XSIZE);
		if (xSizeStr != null)
			xSize = Integer.parseInt(xSizeStr);
		int ySize = 400;
		String ySizeStr = toolControl.getPersistedState().get(STATE_YSIZE);
		if (ySizeStr != null)
			ySize = Integer.parseInt(ySizeStr);
		hostPane.setSize(xSize, ySize);
	}

	private Composite getHostPane() {
		if (hostPane != null) {
			setHostSize(); // Always start with the persisted size
			return hostPane;
		}

		// Create one
		hostPane = new Composite(trimStackTB.getShell(), SWT.NONE);
		hostPane.setData(ShellActivationListener.DIALOG_IGNORE_KEY, Boolean.TRUE);
		setHostSize();

		hostPane.addListener(SWT.Traverse, escapeListener);

		// Set a special layout that allows resizing
		fixedSides = getFixedSides();
		hostPane.setLayout(new TrimPaneLayout(toolControl, fixedSides));

		return hostPane;
	}

	private int getFixedSides() {
		Composite trimComp = (Composite) bar.getWidget();
		Rectangle trimBounds = trimComp.getBounds();
		Point trimCenter = new Point(trimBounds.width / 2, trimBounds.height / 2); // adjusted to
																					// (0,0)

		Control trimCtrl = (Control) toolControl.getWidget();
		Rectangle ctrlBounds = trimCtrl.getBounds();
		Point ctrlCenter = new Point(ctrlBounds.x + (ctrlBounds.width / 2), ctrlBounds.y
				+ (ctrlBounds.height / 2));

		if (bar.getSide() == SideValue.LEFT) {
			int verticalValue = ctrlCenter.y < trimCenter.y ? SWT.TOP : SWT.BOTTOM;
			return SWT.LEFT | verticalValue;
		} else if (bar.getSide() == SideValue.RIGHT) {
			int verticalValue = ctrlCenter.y < trimCenter.y ? SWT.TOP : SWT.BOTTOM;
			return SWT.RIGHT | verticalValue;
		} else if (bar.getSide() == SideValue.TOP) {
			int horizontalValue = ctrlCenter.x < trimCenter.x ? SWT.LEFT : SWT.RIGHT;
			return SWT.TOP | horizontalValue;
		} else if (bar.getSide() == SideValue.BOTTOM) {
			int horizontalValue = ctrlCenter.x < trimCenter.x ? SWT.LEFT : SWT.RIGHT;
			return SWT.BOTTOM | horizontalValue;
		}

		return SWT.BOTTOM | SWT.RIGHT;
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
