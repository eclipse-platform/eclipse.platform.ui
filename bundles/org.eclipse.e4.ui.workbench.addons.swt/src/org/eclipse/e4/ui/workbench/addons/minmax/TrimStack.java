/*******************************************************************************
 * Copyright (c) 2010, 2014 IBM Corporation and others.
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
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MCompositePart;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.IResourceUtilities;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.renderers.swt.TrimmedPartLayout;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
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

	public static final String USE_OVERLAYS_KEY = "UseOverlays"; //$NON-NLS-1$

	static final String STATE_XSIZE = "XSize"; //$NON-NLS-1$

	static final String STATE_YSIZE = "YSize"; //$NON-NLS-1$

	public static final String MINIMIZED_AND_SHOWING = "MinimizedAndShowing"; //$NON-NLS-1$

	private Image layoutImage;

	private Image restoreImage;

	private ToolBar trimStackTB;

	/**
	 * The context menu for this trim stack's items.
	 */
	private Menu trimStackMenu;

	private boolean cachedUseOverlays = true;
	private boolean isShowing = false;
	private MUIElement minimizedElement;
	// private Composite clientAreaComposite;
	private Composite hostPane;

	@Inject
	@Named("org.eclipse.e4.ui.workbench.IResourceUtilities")
	private IResourceUtilities<ImageDescriptor> resUtils;

	/**
	 * A map of created images from a part's icon URI path.
	 */
	private Map<String, Image> imageMap = new HashMap<String, Image>();

	ControlListener caResizeListener = new ControlListener() {
		@Override
		public void controlResized(ControlEvent e) {
			if (hostPane != null && hostPane.isVisible())
				setPaneLocation();
		}

		@Override
		public void controlMoved(ControlEvent e) {
		}
	};

	// Listens to ESC and closes the active fast view
	private Listener escapeListener = new Listener() {
		@Override
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

	@Inject
	@Optional
	private void subscribeTopicTagsChanged(
			@UIEventTopic(UIEvents.ApplicationElement.TOPIC_TAGS) org.osgi.service.event.Event event) {
		Object changedObj = event.getProperty(EventTags.ELEMENT);

		if (!(changedObj instanceof MToolControl))
			return;

		final MToolControl changedElement = (MToolControl) changedObj;
		if (changedElement.getObject() != this)
			return;

		if (UIEvents.isREMOVE(event)) {
			if (UIEvents.contains(event, UIEvents.EventTags.OLD_VALUE, MINIMIZED_AND_SHOWING)) {
				showStack(false);
			}
		}
	}

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

		if (result == null || result.length() == 0)
			return null;

		if (element instanceof MUILabel) {
			String label = ((MUILabel)element).getLocalizedLabel();
			if (label != null && label.length() > 0) {
				result = label + ' ' + '(' + result + ')';
			}
		}

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

		Object changedElement = event.getProperty(UIEvents.EventTags.ELEMENT);
		if (!(changedElement instanceof MUIElement)) {
			return;
		}

		String key;
		if (UIEvents.isREMOVE(event)) {
			key = ((Entry<String, Object>) event.getProperty(UIEvents.EventTags.OLD_VALUE))
					.getKey();
		} else {
			key = ((Entry<String, Object>) event.getProperty(UIEvents.EventTags.NEW_VALUE))
					.getKey();
		}

		if (key.equals(IPresentationEngine.OVERRIDE_ICON_IMAGE_KEY)) {
			ToolItem toolItem = getChangedToolItem((MUIElement) changedElement);
			if (toolItem != null)
				toolItem.setImage(getImage((MUILabel) toolItem.getData()));
		} else if (key.equals(IPresentationEngine.OVERRIDE_TITLE_TOOL_TIP_KEY)) {
			ToolItem toolItem = getChangedToolItem((MUIElement) changedElement);
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
		@Override
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

			if (changedElement instanceof MCompositePart) {
				MPart innerPart = getLeafPart(changedElement);
				if (innerPart != null) {
					fixToolItemSelection();
					return;
				}
			}

			if (changedElement == getLeafPart(minimizedElement)) {
				fixToolItemSelection();
				return;
			}

			showStack(false);
		}
	};
	
	// Close any open stacks before shutting down
	private EventHandler shutdownHandler = new EventHandler() {
		@Override
		public void handleEvent(org.osgi.service.event.Event event) {
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
			if (isEditorStack() || minimizedElement instanceof MPlaceholder) {
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
		if (!(minimizedElement instanceof MPlaceholder))
			return false;

		MPlaceholder ph = (MPlaceholder) minimizedElement;
		return ph.getRef() instanceof MArea;
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
		@Override
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
		@Override
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
					@Override
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
		@Override
		public void handleEvent(org.osgi.service.event.Event event) {
			if (minimizedElement == null || trimStackTB == null)
				return;

			Object changedObj = event.getProperty(UIEvents.EventTags.ELEMENT);

			// if a child has been added or removed, re-scape the CTF
			if (changedObj == minimizedElement) {
				trimStackTB.getDisplay().asyncExec(new Runnable() {
					@Override
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
		@Override
		public void handleEvent(org.osgi.service.event.Event event) {
			Object changedObj = event.getProperty(UIEvents.EventTags.ELEMENT);
			if (changedObj != minimizedElement)
				return;

			if (minimizedElement.getWidget() != null) {
				trimStackTB.getDisplay().asyncExec(new Runnable() {
					@Override
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
		@Override
		public void widgetSelected(SelectionEvent e) {
			ToolItem toolItem = (ToolItem) e.widget;
			MUIElement uiElement = (MUIElement) toolItem.getData();

			// Clicking on the already showing item ? NOTE: the selection will already have been
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

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}
	};

	// private MTrimBar bar;

	private int fixedSides;

	private Composite originalParent;

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
		eventBroker.subscribe(UIEvents.UILifeCycle.APP_SHUTDOWN_STARTED, shutdownHandler);
	}

	private Composite getCAComposite() {
		if (trimStackTB == null)
			return null;

		// Get the shell's client area composite
		Shell theShell = trimStackTB.getShell();
		if (theShell.getLayout() instanceof TrimmedPartLayout) {
			TrimmedPartLayout tpl = (TrimmedPartLayout) theShell.getLayout();
			if (!tpl.clientArea.isDisposed())
				return tpl.clientArea;
		}
		return null;
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
			MTrimBar bar = (MTrimBar) meParent;
			if (bar.getSide() == SideValue.RIGHT || bar.getSide() == SideValue.LEFT)
				orientation = SWT.VERTICAL;
		}
		trimStackTB = new ToolBar(parent, orientation | SWT.FLAT | SWT.WRAP);
		trimStackTB.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				showStack(false);

				trimStackTB = null;
				trimStackMenu = null;
			}
		});

		trimStackTB.addListener(SWT.MenuDetect, new Listener() {
			@Override
			public void handleEvent(Event event) {
				// Clear any existing menus
				while (trimStackMenu.getItemCount() > 0)
					trimStackMenu.getItem(0).dispose();

				// Only open the menu if a tool item is selected
				Point point = trimStackTB.getDisplay().map(null, trimStackTB,
						new Point(event.x, event.y));
				ToolItem selectedToolItem = trimStackTB.getItem(point);
				if (selectedToolItem == null) {
					return;
				}

				// Are we hovering over a valid tool item (vs restore button)
				Object data = selectedToolItem.getData();
				if (data instanceof MPart) {
					// A part on a stack or editor area
					createPartMenu((MPart) data);
				} else if (data instanceof MPerspective) {
					// A perspective in a perspective stack (for now we just support restore)
					createEmtpyEditorAreaMenu();
				} else if (isEditorStack()) {
					// An empty editor area
					createEmtpyEditorAreaMenu();
				} else {
					createUseOverlaysMenu();
				}
			}
		});

		trimStackMenu = new Menu(trimStackTB);
		trimStackTB.setMenu(trimStackMenu);

		ToolItem restoreBtn = new ToolItem(trimStackTB, SWT.PUSH);
		restoreBtn.setToolTipText(Messages.TrimStack_RestoreText);
		restoreBtn.setImage(getRestoreImage());
		restoreBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				minimizedElement.getTags().remove(IPresentationEngine.MINIMIZED);
			}
		});

		updateTrimStackItems();
	}

	/**
	 * Creates a restore menu item that removes the minimized tag from the {@link #minimizedElement}
	 */
	private void createEmtpyEditorAreaMenu() {
		MenuItem restoreItem = new MenuItem(trimStackMenu, SWT.NONE);
		restoreItem.setText(Messages.TrimStack_RestoreText);
		restoreItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				minimizedElement.getTags().remove(IPresentationEngine.MINIMIZED);
			}
		});
	}

	/**
	 * Creates a restore menu item that removes the minimized tag from the {@link #minimizedElement}
	 */
	private void createUseOverlaysMenu() {
		MenuItem useOverlaysItem = new MenuItem(trimStackMenu, SWT.CHECK);
		useOverlaysItem.setText(Messages.TrimStack_Show_In_Original_Location);
		useOverlaysItem.setSelection(!useOverlays());
		useOverlaysItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (toolControl != null) {
					toolControl.getPersistedState().put(USE_OVERLAYS_KEY,
							Boolean.toString(!useOverlays()));
				}
			}
		});
	}

	/**
	 * Creates a series of menu items when a part is selected. The orientation submenu changes the
	 * layout tags on the {@link #minimizedElement}. The restore item will remove the minimized tag.
	 * The close item is not available on the editor stack, but will ask the part service to hide
	 * the part.
	 * 
	 * @param selectedPart
	 *            the part from the data of the selected tool item
	 */
	private void createPartMenu(final MPart selectedPart) {
		MenuItem orientationItem = new MenuItem(trimStackMenu, SWT.CASCADE);
		orientationItem.setText(Messages.TrimStack_OrientationMenu);
		Menu orientationMenu = new Menu(orientationItem);
		orientationItem.setMenu(orientationMenu);

		MenuItem defaultItem = new MenuItem(orientationMenu, SWT.RADIO);
		defaultItem.setText(Messages.TrimStack_DefaultOrientationItem);
		defaultItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				boolean doRefresh = minimizedElement.getTags().remove(
						IPresentationEngine.ORIENTATION_HORIZONTAL);
				doRefresh |= minimizedElement.getTags().remove(
						IPresentationEngine.ORIENTATION_VERTICAL);
				if (isShowing && doRefresh) {
					setPaneLocation();
				}
			}
		});

		MenuItem horizontalItem = new MenuItem(orientationMenu, SWT.RADIO);
		horizontalItem.setText(Messages.TrimStack_Horizontal);
		horizontalItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (!minimizedElement.getTags()
						.contains(IPresentationEngine.ORIENTATION_HORIZONTAL)) {
					minimizedElement.getTags().remove(IPresentationEngine.ORIENTATION_VERTICAL);
					minimizedElement.getTags().add(IPresentationEngine.ORIENTATION_HORIZONTAL);
					if (isShowing) {
						setPaneLocation();
					}
				}
			}
		});

		MenuItem verticalItem = new MenuItem(orientationMenu, SWT.RADIO);
		verticalItem.setText(Messages.TrimStack_Vertical);
		verticalItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (!minimizedElement.getTags().contains(IPresentationEngine.ORIENTATION_VERTICAL)) {
					minimizedElement.getTags().remove(IPresentationEngine.ORIENTATION_HORIZONTAL);
					minimizedElement.getTags().add(IPresentationEngine.ORIENTATION_VERTICAL);
					if (isShowing) {
						setPaneLocation();
					}
				}
			}
		});

		// Set initial orientation selection
		if (minimizedElement.getTags().contains(IPresentationEngine.ORIENTATION_HORIZONTAL)) {
			horizontalItem.setSelection(true);
		} else if (minimizedElement.getTags().contains(IPresentationEngine.ORIENTATION_VERTICAL)) {
			verticalItem.setSelection(true);
		} else {
			defaultItem.setSelection(true);
		}

		MenuItem restoreItem = new MenuItem(trimStackMenu, SWT.NONE);
		restoreItem.setText(Messages.TrimStack_RestoreText);
		restoreItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				minimizedElement.getTags().remove(IPresentationEngine.MINIMIZED);
				partService.activate(selectedPart);
			}
		});

		// Do not allow the shared editor area to be closed
		if (!isEditorStack()) {
			MenuItem closeItem = new MenuItem(trimStackMenu, SWT.NONE);
			closeItem.setText(Messages.TrimStack_CloseText);
			closeItem.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					partService.hidePart(selectedPart);
				}
			});
		}
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

	public MUIElement getMinimizedElement() {
		return minimizedElement;
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
			if (text != null && text.length() > 0)
				return text;
		}

		String string = label.getLocalizedLabel();
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
			ToolItem ti = new ToolItem(trimStackTB, SWT.CHECK);
			ti.setToolTipText(Messages.TrimStack_SharedAreaTooltip);
			ti.setImage(getLayoutImage());
			ti.addSelectionListener(toolItemSelectionListener);
		} else if (minimizedElement instanceof MPlaceholder) {
			MPlaceholder ph = (MPlaceholder) minimizedElement;
			if (ph.getRef() instanceof MPart) {
				MPart part = (MPart) ph.getRef();
				ToolItem ti = new ToolItem(trimStackTB, SWT.CHECK);
				ti.setData(part);
				ti.setImage(getImage(part));
				ti.setToolTipText(getLabelText(part));
				ti.addSelectionListener(toolItemSelectionListener);
			}
		} else if (minimizedElement instanceof MGenericStack<?>) {
			// Handle *both* PartStacks and PerspectiveStacks here...
			MGenericStack<?> theStack = (MGenericStack<?>) minimizedElement;

			// check to see if this stack has any valid elements
			boolean hasRenderedElements = false;
			for (MUIElement stackElement : theStack.getChildren()) {
				if (stackElement.isToBeRendered()) {
					hasRenderedElements = true;
					break;
				}
			}

			if (hasRenderedElements) {
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
			} else if (theStack.getTags().contains(IPresentationEngine.NO_AUTO_COLLAPSE)) {
				// OK to be empty and still minimized
				ToolItem ti = new ToolItem(trimStackTB, SWT.CHECK);
				ti.setToolTipText(Messages.TrimStack_EmptyStackTooltip);
				ti.setImage(getLayoutImage());
				ti.addSelectionListener(toolItemSelectionListener);
			} else {
				// doesn't have any children that's showing, place it back in the presentation
				restoreStack();
				return;
			}
		}

		trimStackTB.pack();
		trimStackTB.getShell().layout(new Control[] { trimStackTB }, SWT.DEFER);
	}

	void restoreStack() {
		showStack(false);

		minimizedElement.setVisible(true);
		minimizedElement.getTags().remove(IPresentationEngine.MINIMIZED);

		// Activate the part that is being brought up...
		if (minimizedElement instanceof MPartStack) {
			MPartStack theStack = (MPartStack) minimizedElement;
			MStackElement curSel = theStack.getSelectedElement();
			Control ctrl = (Control) minimizedElement.getWidget();

			// Hack for elems that are lazy initialized
			if (ctrl instanceof CTabFolder && ((CTabFolder) ctrl).getSelection() == null) {
				theStack.setSelectedElement(null);
				theStack.setSelectedElement(curSel);
			}
		}

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
		Control ctrl = (Control) minimizedElement.getWidget();
		CTabFolder ctf = ctrl instanceof CTabFolder? (CTabFolder) ctrl: null;

		Composite clientAreaComposite = getCAComposite();
		if (clientAreaComposite == null || clientAreaComposite.isDisposed())
			return;

		if (show && !isShowing) {
			if (useOverlays()) {
				hostPane = getHostPane();
				originalParent = ctrl.getParent();
				ctrl.setParent(hostPane);

				// Hack ! Force a resize of the CTF to make sure the hosted
				// view is the correct size...see bug 434062 for details
				if (ctf != null) {
					Rectangle bb = ctf.getBounds();
					bb.width--;
					ctf.setBounds(bb);
				}

				clientAreaComposite.addControlListener(caResizeListener);

				// Set the initial location
				setPaneLocation();

				hostPane.addListener(SWT.Traverse, escapeListener);

				hostPane.layout(true);
				hostPane.moveAbove(null);
				hostPane.setVisible(true);

				// Cache the value to ensure that a stack is hidden using the same mode it was
				// opened in
				cachedUseOverlays = true;
			} else {
				minimizedElement.setVisible(true);
				ctrl.addListener(SWT.Traverse, escapeListener);

				// Cache the value to ensure that a stack is hidden using the same mode it was
				// opened in
				cachedUseOverlays = false;
			}

			isShowing = true;
			toolControl.getTags().add(MINIMIZED_AND_SHOWING);

			// Activate the part that is being brought up...
			if (minimizedElement instanceof MPartStack) {
				MPartStack theStack = (MPartStack) minimizedElement;
				MStackElement curSel = theStack.getSelectedElement();

				// Hack for elems that are lazy initialized
				if (ctf != null && ctf.getSelection() == null) {
					theStack.setSelectedElement(null);
					theStack.setSelectedElement(curSel);
				}

				if (curSel instanceof MPart) {
					partService.activate((MPart) curSel);
				} else if (curSel instanceof MPlaceholder) {
					MPlaceholder ph = (MPlaceholder) curSel;
					if (ph.getRef() instanceof MPart) {
						partService.activate((MPart) ph.getRef());
					}
				}
			} else if (isEditorStack()) {
				MArea area = (MArea) ((MPlaceholder) minimizedElement).getRef();

				// See if we can find an element to activate...
				MPart partToActivate = null;
				MUIElement selectedElement = area.getSelectedElement();
				while (partToActivate == null && selectedElement != null) {
					if (selectedElement instanceof MPart) {
						partToActivate = (MPart) selectedElement;
					} else if (selectedElement instanceof MPlaceholder) {
						MPlaceholder ph = (MPlaceholder) selectedElement;
						if (ph.getRef() instanceof MPart) {
							partToActivate = (MPart) ph.getRef();
						} else {
							selectedElement = null;
						}
					} else if (selectedElement instanceof MElementContainer<?>) {
						MElementContainer<?> container = (MElementContainer<?>) selectedElement;
						selectedElement = (MElementContainer<?>) container.getSelectedElement();
					}
				}

				// If we haven't found one then use the first
				if (partToActivate == null) {
					List<MPart> parts = modelService.findElements(area, null, MPart.class, null);
					if (parts.size() > 0)
						partToActivate = parts.get(0);
				}

				if (partToActivate != null) {
					partService.activate(partToActivate);
				}
			} else if (minimizedElement instanceof MPlaceholder) {
				MPlaceholder ph = (MPlaceholder) minimizedElement;
				if (ph.getRef() instanceof MPart) {
					MPart part = (MPart) ph.getRef();
					partService.activate(part);
				}
			}

			fixToolItemSelection();
		} else if (!show && isShowing) {
			if (cachedUseOverlays) {
				// Check to ensure that the client area is non-null since the
				// trimstack may be currently hosted in the limbo shell
				if (clientAreaComposite != null) {
					clientAreaComposite.removeControlListener(caResizeListener);
				}

				ctrl.setParent(originalParent);

				hostPane.dispose();
				hostPane = null;
			} else {
				if (ctrl != null && !ctrl.isDisposed())
					ctrl.removeListener(SWT.Traverse, escapeListener);
				minimizedElement.setVisible(false);
			}

			isShowing = false;
			toolControl.getTags().remove(MINIMIZED_AND_SHOWING);

			fixToolItemSelection();
		}
	}

	/**
	 * @return 'true' iff the minimized stack should overlay the current presentation, 'false' means
	 *         to temporarily restore the stack into the current presentation.
	 */
	private boolean useOverlays() {
		if (toolControl == null)
			return true;

		String useOverlays = toolControl.getPersistedState().get(USE_OVERLAYS_KEY);
		if (useOverlays == null)
			useOverlays = "true"; //$NON-NLS-1$
		return Boolean.parseBoolean(useOverlays);
	}

	private void setPaneLocation() {
		Composite clientAreaComposite = getCAComposite();
		if (clientAreaComposite == null || clientAreaComposite.isDisposed())
			return;

		Rectangle caRect = clientAreaComposite.getBounds();

		// NOTE: always starts in the persisted (or default) size
		Point paneSize = hostPane.getSize();

		// Ensure it's not clipped
		if (paneSize.x > caRect.width)
			paneSize.x = caRect.width;
		if (paneSize.y > caRect.height)
			paneSize.y = caRect.height;

		if (minimizedElement.getTags().contains(IPresentationEngine.ORIENTATION_HORIZONTAL))
			paneSize.x = caRect.width;
		if (minimizedElement.getTags().contains(IPresentationEngine.ORIENTATION_VERTICAL))
			paneSize.y = caRect.height;

		Point loc = new Point(0, 0);
		if (isFixed(SWT.LEFT))
			loc.x = caRect.x;
		else
			loc.x = (caRect.x + caRect.width) - paneSize.x;

		if (isFixed(SWT.TOP))
			loc.y = caRect.y;
		else
			loc.y = (caRect.y + caRect.height) - paneSize.y;

		hostPane.setSize(paneSize);
		hostPane.setLocation(loc);
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
		// Create one
		hostPane = new Composite(trimStackTB.getShell(), SWT.NONE);
		hostPane.setData(ShellActivationListener.DIALOG_IGNORE_KEY, Boolean.TRUE);

		hostPane.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				hostPane = null;
			}
		});

		setHostSize();

		// Set a special layout that allows resizing
		fixedSides = getFixedSides();
		hostPane.setLayout(new TrimPaneLayout(toolControl, fixedSides));

		return hostPane;
	}

	private int getFixedSides() {
		MUIElement tcParent = toolControl.getParent();
		if (!(tcParent instanceof MTrimBar))
			return 0;
		MTrimBar bar = (MTrimBar) tcParent;
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
