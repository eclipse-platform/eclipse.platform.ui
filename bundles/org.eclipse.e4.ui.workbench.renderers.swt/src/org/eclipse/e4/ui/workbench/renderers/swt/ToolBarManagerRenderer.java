/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.action.AbstractGroupMarker;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Create a contribute part.
 */
public class ToolBarManagerRenderer extends SWTPartRenderer {

	private static final String TOOL_BAR_MANAGER_RENDERER_VIEW_MENU = "ToolBarManagerRenderer.viewMenu"; //$NON-NLS-1$
	private static final String TOOL_BAR_MANAGER_RENDERER_DRAG_HANDLE = "ToolBarManagerRenderer.dragHandle"; //$NON-NLS-1$
	private Map<MToolBar, ToolBarManager> modelToManager = new HashMap<MToolBar, ToolBarManager>();
	private Map<ToolBarManager, MToolBar> managerToModel = new HashMap<ToolBarManager, MToolBar>();

	private Map<MToolBarElement, IContributionItem> modelToContribution = new HashMap<MToolBarElement, IContributionItem>();
	private Map<IContributionItem, MToolBarElement> contributionToModel = new HashMap<IContributionItem, MToolBarElement>();

	private Map<MToolBarElement, ToolBarContributionRecord> modelContributionToRecord = new HashMap<MToolBarElement, ToolBarContributionRecord>();

	private Map<MToolBarElement, ArrayList<ToolBarContributionRecord>> sharedElementToRecord = new HashMap<MToolBarElement, ArrayList<ToolBarContributionRecord>>();

	// @Inject
	// private Logger logger;

	@Inject
	private IPresentationEngine renderer;

	@Inject
	private MApplication application;
	@Inject
	private EModelService modelService;

	@Inject
	IEventBroker eventBroker;
	private EventHandler itemUpdater = new EventHandler() {
		public void handleEvent(Event event) {
			// Ensure that this event is for a MMenuItem
			if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MToolBarElement))
				return;

			MToolBarElement itemModel = (MToolBarElement) event
					.getProperty(UIEvents.EventTags.ELEMENT);

			IContributionItem ici = getContribution(itemModel);
			if (ici == null) {
				return;
			}

			String attName = (String) event
					.getProperty(UIEvents.EventTags.ATTNAME);
			if (UIEvents.UILabel.LABEL.equals(attName)) {
				ici.update();
			} else if (UIEvents.UILabel.ICONURI.equals(attName)) {
				ici.update();
			} else if (UIEvents.UILabel.TOOLTIP.equals(attName)) {
				ici.update();
			}
		}
	};

	private EventHandler toBeRenderedUpdater = new EventHandler() {
		public void handleEvent(Event event) {
			// Ensure that this event is for a MMenuItem
			if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MToolBarElement))
				return;

			MToolBarElement itemModel = (MToolBarElement) event
					.getProperty(UIEvents.EventTags.ELEMENT);
			String attName = (String) event
					.getProperty(UIEvents.EventTags.ATTNAME);
			if (UIEvents.UIElement.TOBERENDERED.equals(attName)) {
				Object obj = itemModel.getParent();
				if (!(obj instanceof MToolBar)) {
					return;
				}
				ToolBarManager parent = getManager((MToolBar) obj);
				if (itemModel.isToBeRendered()) {
					if (parent != null) {
						modelProcessSwitch(parent, itemModel);
						parent.update(true);
						ToolBar tb = parent.getControl();
						if (tb != null && !tb.isDisposed()) {
							tb.getShell().layout(new Control[] { tb },
									SWT.DEFER);
						}
					}
				} else {
					IContributionItem ici = modelToContribution
							.remove(itemModel);
					if (ici != null && parent != null) {
						parent.remove(ici);
					}
					if (ici != null) {
						ici.dispose();
					}
				}
			} else if (UIEvents.UIElement.VISIBLE.equals(attName)) {
				IContributionItem ici = getContribution(itemModel);
				if (ici == null) {
					return;
				}
				ici.setVisible(itemModel.isVisible());
				ToolBarManager parent = (ToolBarManager) ((ContributionItem) ici)
						.getParent();
				if (parent != null) {
					parent.markDirty();
					ToolBar tb = parent.getControl();
					if (tb != null && !tb.isDisposed()) {
						tb.getShell().layout(new Control[] { tb }, SWT.DEFER);
					}
				}
			}
		}
	};

	private EventHandler selectionUpdater = new EventHandler() {
		public void handleEvent(Event event) {
			// Ensure that this event is for a MToolItem
			if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MToolBarElement))
				return;

			MToolBarElement itemModel = (MToolBarElement) event
					.getProperty(UIEvents.EventTags.ELEMENT);
			IContributionItem ici = getContribution(itemModel);
			if (ici != null) {
				ici.update();
			}
		}
	};

	private EventHandler enabledUpdater = new EventHandler() {
		public void handleEvent(Event event) {
			// Ensure that this event is for a MMenuItem
			if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MToolBarElement))
				return;

			MToolBarElement itemModel = (MToolBarElement) event
					.getProperty(UIEvents.EventTags.ELEMENT);
			IContributionItem ici = getContribution(itemModel);
			if (ici != null) {
				ici.update();
			}
		}
	};
	private Image viewMenuImage;

	@PostConstruct
	public void init() {
		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.UILabel.TOPIC),
				itemUpdater);
		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.Item.TOPIC,
				UIEvents.Item.SELECTED), selectionUpdater);
		eventBroker
				.subscribe(UIEvents.buildTopic(UIEvents.Item.TOPIC,
						UIEvents.Item.ENABLED), enabledUpdater);
		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.UIElement.TOPIC),
				toBeRenderedUpdater);

		context.set(ToolBarManagerRenderer.class, this);

	}

	@PreDestroy
	public void contextDisposed() {
		eventBroker.unsubscribe(itemUpdater);
		eventBroker.unsubscribe(selectionUpdater);
		eventBroker.unsubscribe(enabledUpdater);
		eventBroker.unsubscribe(toBeRenderedUpdater);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer#createWidget
	 * (org.eclipse.e4.ui.model.application.ui.MUIElement, java.lang.Object)
	 */
	@Override
	public Object createWidget(final MUIElement element, Object parent) {
		if (!(element instanceof MToolBar) || !(parent instanceof Composite))
			return null;

		final MToolBar toolbarModel = (MToolBar) element;
		Composite intermediate = createIntermediate(toolbarModel,
				(Composite) parent);
		createToolbar(toolbarModel, intermediate);
		setupMenuButton(toolbarModel, intermediate);
		processContribution(toolbarModel);
		return intermediate;
	}

	/**
	 * @param toolbarModel
	 * @param intermediate
	 */
	private void setupMenuButton(MToolBar toolbarModel, Composite intermediate) {
		if (needsViewMenu(toolbarModel)) {
			MPart part = (MPart) ((EObject) toolbarModel).eContainer();
			MMenu viewMenu = getViewMenu(part);

			// View menu (if any)
			if (viewMenu != null) {
				addMenuButton(part, intermediate, viewMenu);
			}
		}
	}

	/**
	 * @param toolbarModel
	 * @param parent
	 * @return an intermediate composite or simply the parent.
	 */
	private Composite createIntermediate(MToolBar toolbarModel, Composite parent) {
		Composite intermediate = new Composite((Composite) parent, SWT.NONE);
		intermediate.setData(AbstractPartRenderer.OWNING_ME, toolbarModel);
		int orientation = getOrientation(toolbarModel);
		RowLayout layout = RowLayoutFactory.fillDefaults().wrap(false)
				.spacing(0).type(orientation).create();
		layout.marginLeft = 3;
		layout.center = true;
		intermediate.setLayout(layout);
		if (needsDragHandle(toolbarModel)) {
			ToolBar separatorToolBar = new ToolBar(intermediate, orientation
					| SWT.WRAP | SWT.FLAT | SWT.RIGHT);
			separatorToolBar.setData(TOOL_BAR_MANAGER_RENDERER_DRAG_HANDLE);
			new ToolItem(separatorToolBar, SWT.SEPARATOR);
		}
		return intermediate;
	}

	private boolean needsDragHandle(MToolBar toolbarModel) {
		return toolbarModel != null
				&& ((EObject) toolbarModel).eContainer() instanceof MTrimBar;
	}

	private boolean needsViewMenu(MToolBar toolbarModel) {
		return toolbarModel != null
				&& ((EObject) toolbarModel).eContainer() instanceof MPart;
	}

	/**
	 * @param element
	 */
	private void processContribution(MToolBar toolbarModel) {
		final ArrayList<MToolBarContribution> toContribute = new ArrayList<MToolBarContribution>();
		ContributionsAnalyzer.XXXgatherToolBarContributions(toolbarModel,
				application.getToolBarContributions(),
				toolbarModel.getElementId(), toContribute);
		generateContributions(toolbarModel, toContribute);
	}

	/**
	 * @param toolbarModel
	 * @param toContribute
	 */
	private void generateContributions(MToolBar toolbarModel,
			ArrayList<MToolBarContribution> toContribute) {

		ToolBarManager manager = getManager(toolbarModel);
		boolean done = toContribute.size() == 0;
		while (!done) {
			ArrayList<MToolBarContribution> curList = new ArrayList<MToolBarContribution>(
					toContribute);
			int retryCount = toContribute.size();
			toContribute.clear();

			for (final MToolBarContribution contribution : curList) {
				if (!processAddition(toolbarModel, manager, contribution)) {
					toContribute.add(contribution);
				}
			}
			// We're done if the retryList is now empty (everything done) or
			// if the list hasn't changed at all (no hope)
			done = (toContribute.size() == 0)
					|| (toContribute.size() == retryCount);
		}
	}

	/**
	 * @param toolbarModel
	 * @param manager
	 * @param contribution
	 * @param existingSeparatorNames
	 * @return <code>true</code> if the contribution was successfuly processed
	 */
	private boolean processAddition(MToolBar toolbarModel,
			final ToolBarManager manager, MToolBarContribution contribution) {
		final ToolBarContributionRecord record = new ToolBarContributionRecord(
				toolbarModel, contribution, this);
		if (!record.mergeIntoModel()) {
			return false;
		}
		if (record.anyVisibleWhen()) {
			final IEclipseContext parentContext = modelService
					.getContainingContext(toolbarModel);
			parentContext.runAndTrack(new RunAndTrack() {
				@Override
				public boolean changed(IEclipseContext context) {
					record.updateVisibility(parentContext.getActiveLeaf());
					manager.update(true);
					return true;
				}
			});
		}

		return true;
	}

	private ToolBar createToolbar(final MUIElement element, Composite parent) {
		int orientation = getOrientation(element);

		ToolBarManager manager = getManager((MToolBar) element);
		if (manager == null) {
			manager = new ToolBarManager(orientation | SWT.WRAP | SWT.FLAT
					| SWT.RIGHT);
			linkModelToManager((MToolBar) element, manager);
		}
		ToolBar bar = manager.createControl(parent);
		if (bar.getParent() != parent) {
			Thread.dumpStack();
		}
		bar.setData(manager);
		bar.setData(AbstractPartRenderer.OWNING_ME, element);
		bar.getShell().layout(new Control[] { bar }, SWT.DEFER);
		bar.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				cleanUp((MToolBar) element);
			}
		});
		return bar;
	}

	/**
	 * @param element
	 */
	protected void cleanUp(MToolBar toolbarModel) {
		Collection<ToolBarContributionRecord> vals = modelContributionToRecord
				.values();
		for (ToolBarContributionRecord record : vals
				.toArray(new ToolBarContributionRecord[vals.size()])) {
			if (record.toolbarModel == toolbarModel) {
				record.dispose();
				for (MToolBarElement copy : record.generatedElements) {
					cleanUpCopy(record, copy);
				}
				for (MToolBarElement copy : record.sharedElements) {
					cleanUpCopy(record, copy);
				}
				record.generatedElements.clear();
				record.sharedElements.clear();
			}
		}
	}

	public void cleanUpCopy(ToolBarContributionRecord record,
			MToolBarElement copy) {
		modelContributionToRecord.remove(copy);
		IContributionItem ici = getContribution(copy);
		clearModelToContribution(copy, ici);
		if (ici != null) {
			record.getManagerForModel().remove(ici);
		}
	}

	int getOrientation(final MUIElement element) {
		MUIElement theParent = element.getParent();
		if (theParent instanceof MTrimBar) {
			MTrimBar trimContainer = (MTrimBar) theParent;
			SideValue side = trimContainer.getSide();
			if (side.getValue() == SideValue.LEFT_VALUE
					|| side.getValue() == SideValue.RIGHT_VALUE)
				return SWT.VERTICAL;
		}
		return SWT.HORIZONTAL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.renderers.swt.SWTPartRenderer#processContents
	 * (org.eclipse.e4.ui.model.application.ui.MElementContainer)
	 */
	@Override
	public void processContents(MElementContainer<MUIElement> container) {
		// I can either simply stop processing, or we can walk the model
		// ourselves like the "old" days
		// EMF gives us null lists if empty
		if (container == null)
			return;

		Object obj = container;
		ToolBarManager parentManager = getManager((MToolBar) obj);
		if (parentManager == null) {
			return;
		}
		// Process any contents of the newly created ME
		List<MUIElement> parts = container.getChildren();
		if (parts != null) {
			MUIElement[] plist = parts.toArray(new MUIElement[parts.size()]);
			for (int i = 0; i < plist.length; i++) {
				MUIElement childME = plist[i];
				modelProcessSwitch(parentManager, (MToolBarElement) childME);
			}
		}
		parentManager.update(true);

		ToolBar tb = getToolbarFrom(container.getWidget());
		if (tb != null) {
			tb.getShell().layout(new Control[] { tb }, SWT.DEFER);
		}
	}

	/**
	 * @param widget
	 * @return
	 */
	private ToolBar getToolbarFrom(Object widget) {
		if (widget instanceof ToolBar) {
			return (ToolBar) widget;
		}
		if (widget instanceof Composite) {
			Composite intermediate = (Composite) widget;
			if (!intermediate.isDisposed()) {
				Control[] children = intermediate.getChildren();
				for (Control control : children) {
					if (control.getData() instanceof ToolBarManager) {
						return (ToolBar) control;
					}
				}
			}
		}
		return null;
	}

	private void addMenuButton(MPart part, Composite intermediate, MMenu menu) {
		ToolBar tb = new ToolBar(intermediate, SWT.FLAT | SWT.RIGHT);
		tb.setData(TOOL_BAR_MANAGER_RENDERER_VIEW_MENU);
		ToolItem ti = new ToolItem(tb, SWT.PUSH);
		ti.setImage(getViewMenuImage());
		ti.setHotImage(null);
		ti.setToolTipText("View Menu"); //$NON-NLS-1$
		ti.setData("theMenu", menu); //$NON-NLS-1$
		ti.setData("thePart", part); //$NON-NLS-1$

		ti.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				showMenu((ToolItem) e.widget);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				showMenu((ToolItem) e.widget);
			}
		});
	}

	/**
	 * @param item
	 */
	protected void showMenu(ToolItem item) {
		// Create the UI for the menu
		final MMenu menuModel = (MMenu) item.getData("theMenu"); //$NON-NLS-1$
		Menu menu = null;
		Object obj = menuModel.getWidget();
		if (obj instanceof Menu) {
			menu = (Menu) obj;
		}
		if (menu == null || menu.isDisposed()) {
			MPart part = (MPart) item.getData("thePart"); //$NON-NLS-1$
			Control ctrl = (Control) part.getWidget();
			final Menu tmpMenu = (Menu) renderer.createGui(menuModel,
					ctrl.getShell(), part.getContext());
			menu = tmpMenu;
			if (tmpMenu != null) {
				ctrl.addDisposeListener(new DisposeListener() {
					public void widgetDisposed(DisposeEvent e) {
						if (!tmpMenu.isDisposed()) {
							tmpMenu.dispose();
						}
					}
				});
			}
		}
		if (menu == null) {
			return;
		}

		// ...and Show it...
		Rectangle ib = item.getBounds();
		Point displayAt = item.getParent().toDisplay(ib.x, ib.y + ib.height);
		menu.setLocation(displayAt);
		menu.setVisible(true);

		Display display = Display.getCurrent();
		while (!menu.isDisposed() && menu.isVisible()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		if (menu.getData() instanceof MenuManager) {
			MenuManager manager = (MenuManager) menu.getData();
			manager.dispose();
		} else {
			menu.dispose();
		}
	}

	private Image getViewMenuImage() {
		if (viewMenuImage == null) {
			Display d = Display.getCurrent();

			Image viewMenu = new Image(d, 16, 16);
			Image viewMenuMask = new Image(d, 16, 16);

			Display display = Display.getCurrent();
			GC gc = new GC(viewMenu);
			GC maskgc = new GC(viewMenuMask);
			gc.setForeground(display
					.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
			gc.setBackground(display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));

			int[] shapeArray = new int[] { 6, 1, 15, 1, 11, 5, 10, 5 };
			gc.fillPolygon(shapeArray);
			gc.drawPolygon(shapeArray);

			Color black = display.getSystemColor(SWT.COLOR_BLACK);
			Color white = display.getSystemColor(SWT.COLOR_WHITE);

			maskgc.setBackground(black);
			maskgc.fillRectangle(0, 0, 16, 16);

			maskgc.setBackground(white);
			maskgc.setForeground(white);
			maskgc.fillPolygon(shapeArray);
			maskgc.drawPolygon(shapeArray);
			gc.dispose();
			maskgc.dispose();

			ImageData data = viewMenu.getImageData();
			data.transparentPixel = data.getPixel(0, 0);

			viewMenuImage = new Image(d, viewMenu.getImageData(),
					viewMenuMask.getImageData());
			viewMenu.dispose();
			viewMenuMask.dispose();
		}
		return viewMenuImage;
	}

	MMenu getViewMenu(MPart part) {
		if (part.getMenus() == null) {
			return null;
		}
		for (MMenu menu : part.getMenus()) {
			if (menu.getTags().contains(StackRenderer.TAG_VIEW_MENU)) {
				return menu;
			}
		}
		return null;
	}

	boolean hasOnlySeparators(ToolBar toolbar) {
		ToolItem[] children = toolbar.getItems();
		for (ToolItem toolItem : children) {
			if ((toolItem.getStyle() & SWT.SEPARATOR) == 0) {
				return false;
			} else if (toolItem.getControl() != null
					&& toolItem.getControl().getData(OWNING_ME) instanceof MToolControl) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void postProcess(MUIElement element) {
		super.postProcess(element);
		disposeToolbarIfNecessary((MToolBar) element);
		ToolBar tb = getToolbarFrom(element.getWidget());
		if (tb != null && !tb.isDisposed()) {
			tb.setVisible(true);
			tb.getShell().layout(new Control[] { tb }, SWT.DEFER);
		}
	}

	/**
	 * @param element
	 */
	private void disposeToolbarIfNecessary(MToolBar element) {
		ToolBar tb = getToolbarFrom(element.getWidget());
		Composite parent = null;
		if (tb != null) {
			parent = tb.getParent();
		}
		boolean cleanUp = tb == null || tb.isDisposed();
		if (!cleanUp) {
			cleanUp = tb.getItemCount() == 0 || hasOnlySeparators(tb);
		}
		if (cleanUp) {
			if (tb != null && !tb.isDisposed()) {
				tb.dispose();
			}
			if (parent != null) {
				for (Control child : parent.getChildren()) {
					if (TOOL_BAR_MANAGER_RENDERER_DRAG_HANDLE.equals(child
							.getData())) {
						child.dispose();
					} else if (TOOL_BAR_MANAGER_RENDERER_VIEW_MENU.equals(child
							.getData()) && !needsViewMenu(element)) {
						child.dispose();
					}
				}
				if (parent.getChildren().length == 0) {
					parent.dispose();
				}
			}
		}
	}

	@Override
	public void disposeWidget(MUIElement element) {
		ToolBar tb = getToolbarFrom(element.getWidget());
		if (tb != null) {
			unbindWidget(element);
			tb.setData(AbstractPartRenderer.OWNING_ME, element);
		}
		ToolBarManager manager = getManager((MToolBar) element);
		if (manager != null) {
			manager.dispose();
		}
	}

	@Override
	public void hideChild(MElementContainer<MUIElement> parentElement,
			MUIElement child) {
		super.hideChild(parentElement, child);

		// Since there's no place to 'store' a child that's not in a menu
		// we'll blow it away and re-create on an add
		Widget widget = (Widget) child.getWidget();
		if (widget != null && !widget.isDisposed()) {
			widget.dispose();
		}
		ToolBar toolbar = (ToolBar) getUIContainer(child);
		if (toolbar != null && !toolbar.isDisposed()) {
			toolbar.getShell().layout(new Control[] { toolbar }, SWT.DEFER);
		}
		// disposeToolbarIfNecessary(parentElement);
	}

	@Override
	public void childRendered(MElementContainer<MUIElement> parentElement,
			MUIElement element) {
		super.childRendered(parentElement, element);
		ToolBar toolbar = (ToolBar) getUIContainer(element);
		if (toolbar != null && !toolbar.isDisposed()) {
			toolbar.getShell().layout(new Control[] { toolbar }, SWT.DEFER);
		}
	}

	public Object getUIContainer(MUIElement childElement) {
		Composite intermediate = (Composite) super.getUIContainer(childElement);
		if (intermediate == null || intermediate.isDisposed()) {
			return null;
		}
		ToolBar toolbar = findToolbar(intermediate);
		if (toolbar == null) {
			toolbar = createToolbar(childElement.getParent(), intermediate);
		}
		return toolbar;
	}

	private ToolBar findToolbar(Composite intermediate) {
		for (Control child : intermediate.getChildren()) {
			if (child.getData() instanceof ToolBarManager) {
				return (ToolBar) child;
			}
		}
		return null;
	}

	/**
	 * @param parentManager
	 * @param childME
	 */
	private void modelProcessSwitch(ToolBarManager parentManager,
			MToolBarElement childME) {
		if (childME instanceof MHandledToolItem) {
			MHandledToolItem itemModel = (MHandledToolItem) childME;
			processHandledItem(parentManager, itemModel);
		} else if (childME instanceof MDirectToolItem) {
			MDirectToolItem itemModel = (MDirectToolItem) childME;
			processDirectItem(parentManager, itemModel);
		} else if (childME instanceof MToolBarSeparator) {
			MToolBarSeparator itemModel = (MToolBarSeparator) childME;
			processSeparator(parentManager, itemModel);
		} else if (childME instanceof MToolControl) {
			MToolControl itemModel = (MToolControl) childME;
			processToolControl(parentManager, itemModel);
		}
	}

	/**
	 * @param parentManager
	 * @param itemModel
	 */
	private void processSeparator(ToolBarManager parentManager,
			MToolBarSeparator itemModel) {
		AbstractGroupMarker marker = null;
		if (itemModel.isVisible()) {
			marker = new Separator();
			marker.setId(itemModel.getElementId());
		} else {
			if (itemModel.getElementId() != null) {
				marker = new GroupMarker(itemModel.getElementId());
			}
		}
		addToManager(parentManager, itemModel, marker);
		linkModelToContribution(itemModel, marker);
	}

	/**
	 * @param parentManager
	 * @param itemModel
	 */
	private void processToolControl(ToolBarManager parentManager,
			MToolControl itemModel) {
		final IEclipseContext lclContext = getContext(itemModel);
		ToolControlContribution ci = ContextInjectionFactory.make(
				ToolControlContribution.class, lclContext);
		ci.setModel(itemModel);
		ci.setVisible(itemModel.isVisible());
		addToManager(parentManager, itemModel, ci);
		linkModelToContribution(itemModel, ci);
	}

	/**
	 * @param parentManager
	 * @param itemModel
	 */
	private void processDirectItem(ToolBarManager parentManager,
			MDirectToolItem itemModel) {
		final IEclipseContext lclContext = getContext(itemModel);
		DirectContributionItem ci = ContextInjectionFactory.make(
				DirectContributionItem.class, lclContext);
		ci.setModel(itemModel);
		ci.setVisible(itemModel.isVisible());
		addToManager(parentManager, itemModel, ci);
		linkModelToContribution(itemModel, ci);
	}

	/**
	 * @param parentManager
	 * @param itemModel
	 */
	private void processHandledItem(ToolBarManager parentManager,
			MHandledToolItem itemModel) {
		IContributionItem ici = getContribution(itemModel);
		if (ici != null) {
			return;
		}
		final IEclipseContext lclContext = getContext(itemModel);
		HandledContributionItem ci = ContextInjectionFactory.make(
				HandledContributionItem.class, lclContext);
		ci.setModel(itemModel);
		ci.setVisible(itemModel.isVisible());
		addToManager(parentManager, itemModel, ci);
		linkModelToContribution(itemModel, ci);
	}

	/**
	 * @param parentManager
	 * @param itemModel
	 * @param ci
	 */
	private void addToManager(ToolBarManager parentManager,
			MToolBarElement model, IContributionItem ci) {
		MElementContainer<MUIElement> parent = model.getParent();
		// technically this shouldn't happen
		if (parent == null) {
			parentManager.add(ci);
		} else {
			int index = parent.getChildren().indexOf(model);
			// shouldn't be -1, but better safe than sorry
			if (index > parentManager.getSize() || index == -1) {
				parentManager.add(ci);
			} else {
				parentManager.insert(index, ci);
			}
		}
	}

	public ToolBarManager getManager(MToolBar model) {
		return modelToManager.get(model);
	}

	public MToolBar getToolBarModel(ToolBarManager manager) {
		return managerToModel.get(manager);
	}

	public void linkModelToManager(MToolBar model, ToolBarManager manager) {
		modelToManager.put(model, manager);
		managerToModel.put(manager, model);
	}

	public void clearModelToManager(MToolBar model, ToolBarManager manager) {
		modelToManager.remove(model);
		managerToModel.remove(manager);
	}

	public IContributionItem getContribution(MToolBarElement element) {
		return modelToContribution.get(element);
	}

	public MToolBarElement getToolElement(IContributionItem item) {
		return contributionToModel.get(item);
	}

	public void linkModelToContribution(MToolBarElement model,
			IContributionItem item) {
		modelToContribution.put(model, item);
		contributionToModel.put(item, model);
	}

	public void clearModelToContribution(MToolBarElement model,
			IContributionItem item) {
		modelToContribution.remove(model);
		contributionToModel.remove(item);
	}

	public ArrayList<ToolBarContributionRecord> getList(MToolBarElement item) {
		ArrayList<ToolBarContributionRecord> tmp = sharedElementToRecord
				.get(item);
		if (tmp == null) {
			tmp = new ArrayList<ToolBarContributionRecord>();
			sharedElementToRecord.put(item, tmp);
		}
		return tmp;
	}

	public void linkElementToContributionRecord(MToolBarElement element,
			ToolBarContributionRecord record) {
		modelContributionToRecord.put(element, record);
	}

	public ToolBarContributionRecord getContributionRecord(
			MToolBarElement element) {
		return modelContributionToRecord.get(element);
	}

}
