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
import java.util.HashMap;
import java.util.HashSet;
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
import org.eclipse.e4.ui.workbench.modeling.ExpressionContext;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.action.AbstractGroupMarker;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
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

	private Map<MToolBar, ToolBarManager> modelToManager = new HashMap<MToolBar, ToolBarManager>();
	private Map<ToolBarManager, MToolBar> managerToModel = new HashMap<ToolBarManager, MToolBar>();

	private Map<MToolBarElement, IContributionItem> modelToContribution = new HashMap<MToolBarElement, IContributionItem>();

	private ArrayList<ContributionRecord> contributionRecords = new ArrayList<ContributionRecord>();

	// @Inject
	// private Logger logger;

	@Inject
	IPresentationEngine renderer;
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
		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.UIElement.TOPIC,
				UIEvents.UIElement.TOBERENDERED), toBeRenderedUpdater);

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

		// HACK!! This should be done using a separate renderer
		Composite intermediate = new Composite((Composite) parent, SWT.NONE);
		createToolbar(element, intermediate);
		processContribution((MToolBar) element);

		return intermediate;
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
		HashSet<String> existingSeparatorNames = new HashSet<String>();
		for (MToolBarElement child : toolbarModel.getChildren()) {
			String elementId = child.getElementId();
			if (child instanceof MToolBarSeparator && elementId != null) {
				existingSeparatorNames.add(elementId);
			}
		}

		ToolBarManager manager = getManager(toolbarModel);
		boolean done = toContribute.size() == 0;
		while (!done) {
			ArrayList<MToolBarContribution> curList = new ArrayList<MToolBarContribution>(
					toContribute);
			int retryCount = toContribute.size();
			toContribute.clear();

			for (final MToolBarContribution contribution : curList) {
				if (!processAddition(toolbarModel, manager, contribution,
						existingSeparatorNames)) {
					toContribute.add(contribution);
				}
			}
			// We're done if the retryList is now empty (everything done) or
			// if the list hasn't changed at all (no hope)
			done = (toContribute.size() == 0)
					|| (toContribute.size() == retryCount);
		}
	}

	static class ContributionRecord {
		public ContributionRecord(MToolBar toolbarModel,
				MToolBarContribution contribution, ToolBarManager manager) {
			this.toolbarModel = toolbarModel;
			this.contribution = contribution;
			this.manager = manager;
		}

		MToolBar toolbarModel;
		MToolBarContribution contribution;
		ToolBarManager manager;
		ArrayList<MToolBarElement> generatedElements = new ArrayList<MToolBarElement>();

		public void generate() {
			for (MToolBarElement element : contribution.getChildren()) {
				MToolBarElement copy = (MToolBarElement) EcoreUtil
						.copy((EObject) element);
				generatedElements.add(copy);
			}
		}

		public void updateVisibility(IEclipseContext context) {
			ExpressionContext exprContext = new ExpressionContext(context);
			boolean isVisible = ContributionsAnalyzer.isVisible(contribution,
					exprContext);
			for (MToolBarElement item : generatedElements) {
				item.setVisible(isVisible);
			}
			manager.markDirty();
		}

		public void dispose() {
			for (MToolBarElement copy : generatedElements) {
				toolbarModel.getChildren().remove(copy);
			}
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
			final ToolBarManager manager, MToolBarContribution contribution,
			HashSet<String> existingSeparatorNames) {
		int idx = getIndex(toolbarModel, contribution.getPositionInParent());
		if (idx == -1) {
			return false;
		}
		final ContributionRecord record = new ContributionRecord(toolbarModel,
				contribution, manager);
		contributionRecords.add(record);
		record.generate();
		for (MToolBarElement copy : record.generatedElements) {
			if (copy instanceof MToolBarSeparator
					&& existingSeparatorNames.contains(copy.getElementId())) {
				// skip this, it's already there
				continue;
			}
			toolbarModel.getChildren().add(idx++, copy);
			if (copy instanceof MToolBarSeparator
					&& copy.getElementId() != null) {
				existingSeparatorNames.add(copy.getElementId());
			}
		}
		if (contribution.getVisibleWhen() != null) {
			final IEclipseContext parentContext = modelService
					.getContainingContext(toolbarModel);
			parentContext.runAndTrack(new RunAndTrack() {
				@Override
				public boolean changed(IEclipseContext context) {
					record.updateVisibility(parentContext.getActiveLeaf());
					manager.update(false);
					return true;
				}
			});
		}

		return true;
	}

	private static int getIndex(MElementContainer<?> menuModel,
			String positionInParent) {
		String id = null;
		String modifier = null;
		if (positionInParent != null && positionInParent.length() > 0) {
			String[] array = positionInParent.split("="); //$NON-NLS-1$
			modifier = array[0];
			id = array[1];
		}
		if (id == null) {
			return menuModel.getChildren().size();
		}

		int idx = 0;
		int size = menuModel.getChildren().size();
		while (idx < size) {
			if (id.equals(menuModel.getChildren().get(idx).getElementId())) {
				if ("after".equals(modifier)) { //$NON-NLS-1$
					idx++;
				}
				return idx;
			}
			idx++;
		}
		return id.equals("additions") ? menuModel.getChildren().size() : -1; //$NON-NLS-1$
	}

	private ToolBar createToolbar(final MUIElement element,
			Composite intermediate) {
		int orientation = getOrientation(element);
		RowLayout layout = RowLayoutFactory.fillDefaults().wrap(false)
				.spacing(0).type(orientation).create();
		layout.marginLeft = 3;
		layout.center = true;
		intermediate.setLayout(layout);
		// new Label(intermediate, (orientation == SWT.HORIZONTAL ? SWT.VERTICAL
		// : SWT.HORIZONTAL) | SWT.SEPARATOR);

		ToolBar separatorToolBar = new ToolBar(intermediate, orientation
				| SWT.WRAP | SWT.FLAT | SWT.RIGHT);
		new ToolItem(separatorToolBar, SWT.SEPARATOR);
		ToolBarManager manager = getManager((MToolBar) element);
		if (manager == null) {
			manager = new ToolBarManager(orientation | SWT.WRAP | SWT.FLAT
					| SWT.RIGHT);
			linkModelToManager((MToolBar) element, manager);
		}
		ToolBar bar = manager.createControl(intermediate);
		bar.setData(manager);
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
		for (ContributionRecord record : contributionRecords
				.toArray(new ContributionRecord[contributionRecords.size()])) {
			if (record.toolbarModel == toolbarModel) {
				record.dispose();
				contributionRecords.remove(record);
				for (MToolBarElement copy : record.generatedElements) {
					IContributionItem ici = modelToContribution.remove(copy);
					if (ici != null) {
						record.manager.remove(ici);
					}
				}
				record.generatedElements.clear();
			}
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
		parentManager.update(false);

		ToolBar tb = getToolbarFrom(container.getWidget());
		if (tb != null && ((EObject) container).eContainer() instanceof MPart) {
			MPart part = (MPart) ((EObject) container).eContainer();
			MMenu viewMenu = getViewMenu(part);

			// View menu (if any)
			if (viewMenu != null) {
				addMenuButton(part, tb, viewMenu);
			}
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
				int length = children.length;
				if (length > 0 && children[length - 1] instanceof ToolBar) {
					return (ToolBar) children[length - 1];
				}
			}
		}
		return null;
	}

	/**
	 * @param tb
	 */
	private void addMenuButton(MPart part, ToolBar tb, MMenu menu) {
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
		MPart part = (MPart) item.getData("thePart"); //$NON-NLS-1$
		Control ctrl = (Control) part.getWidget();
		Menu menu = (Menu) renderer.createGui(menuModel, ctrl.getShell(),
				part.getContext());

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
		menu.dispose();
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

	private MMenu getViewMenu(MPart part) {
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
		// disposeToolbarIfNecessary(element);
		ToolBar tb = getToolbarFrom(element.getWidget());
		if (tb != null && !tb.isDisposed()) {
			tb.getShell().layout(new Control[] { tb }, SWT.DEFER);
			tb.setVisible(true);
		}
	}

	public Object getUIContainer(MUIElement childElement) {
		if (childElement.getWidget() instanceof ToolBar) {
			return childElement.getWidget();
		}

		Object obj = super.getUIContainer(childElement);
		if (obj instanceof ToolBar) {
			return obj;
		}

		if (obj instanceof Composite) {
			Composite intermediate = (Composite) obj;
			if (intermediate == null || intermediate.isDisposed()) {
				return null;
			}
			ToolBar toolbar = getToolbarFrom(intermediate);
			if (toolbar == null) {
				toolbar = createToolbar(childElement.getParent(), intermediate);
			}
			return toolbar;
		}
		return null;
	}

	@Override
	public void disposeWidget(MUIElement element) {
		ToolBar tb = getToolbarFrom(element.getWidget());
		tb.setVisible(false);
		unbindWidget(element);
		tb.setData(AbstractPartRenderer.OWNING_ME, element);
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
		parentManager.add(marker);
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
		parentManager.add(ci);
		modelToContribution.put(itemModel, ci);
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
		parentManager.add(ci);
		modelToContribution.put(itemModel, ci);
	}

	/**
	 * @param parentManager
	 * @param itemModel
	 */
	private void processHandledItem(ToolBarManager parentManager,
			MHandledToolItem itemModel) {
		final IEclipseContext lclContext = getContext(itemModel);
		HandledContributionItem ci = ContextInjectionFactory.make(
				HandledContributionItem.class, lclContext);
		ci.setModel(itemModel);
		parentManager.add(ci);
		modelToContribution.put(itemModel, ci);
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
}
