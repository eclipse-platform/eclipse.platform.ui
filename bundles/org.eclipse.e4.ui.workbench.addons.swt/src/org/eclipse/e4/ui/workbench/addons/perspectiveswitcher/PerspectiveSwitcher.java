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
import org.eclipse.e4.core.contexts.IContextConstants;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.PerspectiveExtensionReader;
import org.eclipse.ui.internal.PerspectiveTagger;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.dialogs.SelectPerspectiveDialog;
import org.eclipse.ui.internal.e4.compatibility.E4Util;
import org.eclipse.ui.internal.e4.compatibility.ModeledPageLayout;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;
import org.eclipse.ui.internal.util.PrefUtil;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class PerspectiveSwitcher {
	public static final String PERSPECTIVE_SWITCHER_ID = "org.eclipse.e4.ui.PerspectiveSwitcher"; //$NON-NLS-1$
	@Inject
	protected IEventBroker eventBroker;

	@Inject
	EModelService modelService;

	@Inject
	private EPartService partService;

	@Inject
	private MWindow window;

	private MToolControl psME;
	private ToolBar psTB;
	private Composite comp;
	private Image backgroundImage;

	Color borderColor;
	Control toolParent;

	private EventHandler selectionHandler = new EventHandler() {
		public void handleEvent(Event event) {
			if (psTB.isDisposed()) {
				return;
			}

			MUIElement changedElement = (MUIElement) event.getProperty(UIEvents.EventTags.ELEMENT);

			if (psME == null || !(changedElement instanceof MPerspectiveStack))
				return;

			MWindow perspWin = modelService.getTopLevelWindowFor(changedElement);
			MWindow switcherWin = modelService.getTopLevelWindowFor(psME);
			if (perspWin != switcherWin)
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
			if (psTB.isDisposed()) {
				return;
			}

			MUIElement changedElement = (MUIElement) event.getProperty(UIEvents.EventTags.ELEMENT);

			if (psME == null || !(changedElement instanceof MPerspective))
				return;

			MWindow perspWin = modelService.getTopLevelWindowFor(changedElement);
			MWindow switcherWin = modelService.getTopLevelWindowFor(psME);
			if (perspWin != switcherWin)
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
			if (psTB.isDisposed()) {
				return;
			}

			Object changedObj = event.getProperty(UIEvents.EventTags.ELEMENT);
			String eventType = (String) event.getProperty(UIEvents.EventTags.TYPE);

			if (changedObj instanceof MWindow && UIEvents.EventTypes.ADD.equals(eventType)) {
				MUIElement added = (MUIElement) event.getProperty(UIEvents.EventTags.NEW_VALUE);
				if (added instanceof MPerspectiveStack) {
				}
			}

			if (psME == null || !(changedObj instanceof MPerspectiveStack))
				return;

			MWindow perspWin = modelService.getTopLevelWindowFor((MUIElement) changedObj);
			MWindow switcherWin = modelService.getTopLevelWindowFor(psME);
			if (perspWin != switcherWin)
				return;

			if (UIEvents.EventTypes.ADD.equals(eventType)) {
				MPerspective added = (MPerspective) event.getProperty(UIEvents.EventTags.NEW_VALUE);
				// Adding invisible elements is a NO-OP
				if (!added.isToBeRendered())
					return;

				addPerspectiveItem(added);
			} else if (UIEvents.EventTypes.REMOVE.equals(eventType)) {
				MPerspective removed = (MPerspective) event
						.getProperty(UIEvents.EventTags.OLD_VALUE);
				// Removing invisible elements is a NO-OP
				if (!removed.isToBeRendered())
					return;

				removePerspectiveItem(removed);
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
	void createWidget(Composite parent, MToolControl toolControl) {
		psME = toolControl;
		borderColor = new Color(parent.getDisplay(), 170, 176, 191);
		comp = new Composite(parent, SWT.NONE);
		RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		layout.marginLeft = layout.marginRight = 8;
		layout.marginBottom = 4;
		layout.marginTop = 6;
		comp.setLayout(layout);
		psTB = new ToolBar(comp, SWT.FLAT | SWT.WRAP | SWT.RIGHT);
		comp.addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				paint(e);
			}
		});
		toolParent = ((Control) toolControl.getParent().getWidget());
		toolParent.addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				e.gc.setForeground(borderColor);
				Rectangle bounds = ((Control) e.widget).getBounds();
				e.gc.drawLine(0, bounds.height - 1, bounds.width, bounds.height - 1);
			}
		});

		toolParent.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				borderColor.dispose();
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
						openMenuFor(item, persp);
				}
			}
		});

		psTB.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				disposeTBImages();
			}

		});

		final ToolItem createItem = new ToolItem(psTB, SWT.PUSH);
		createItem.setText("+"); //$NON-NLS-1$
		createItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				selectPerspective();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				selectPerspective();
			}
		});
		new ToolItem(psTB, SWT.SEPARATOR);

		MPerspectiveStack stack = getPerspectiveStack();
		if (stack != null) {
			// Create an item for each perspective that should show up
			for (MPerspective persp : stack.getChildren()) {
				if (persp.isToBeRendered()) {
					addPerspectiveItem(persp);
				}
			}
		}
	}

	MPerspectiveStack getPerspectiveStack() {
		List<MPerspectiveStack> psList = modelService.findElements(window, null,
				MPerspectiveStack.class, null);
		if (psList.size() > 0)
			return psList.get(0);
		return null;
	}

	@PreDestroy
	void removeListeners() {
		eventBroker.unsubscribe(toBeRenderedHandler);
		eventBroker.unsubscribe(childrenHandler);
		eventBroker.unsubscribe(selectionHandler);
	}

	private ToolItem addPerspectiveItem(MPerspective persp) {
		final ToolItem psItem = new ToolItem(psTB, SWT.RADIO);
		psItem.setData(persp);
		IPerspectiveDescriptor descriptor = getDescriptorFor(persp.getElementId());
		boolean foundImage = false;
		if (descriptor != null) {
			ImageDescriptor desc = descriptor.getImageDescriptor();
			if (desc != null) {
				psItem.setImage(desc.createImage(false));
				foundImage = true;
				psItem.setToolTipText(persp.getLabel());
			}
		}
		if (!foundImage
				|| PrefUtil.getAPIPreferenceStore().getBoolean(
						IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR)) {
			psItem.setText(persp.getLabel());
			psItem.setToolTipText(persp.getTooltip());
		}

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
				openMenuFor(psItem, persp);
			}
		});

		// update the layout
		psTB.pack();
		psTB.getShell().layout(new Control[] { psTB }, SWT.DEFER);

		return psItem;
	}

	// FIXME see https://bugs.eclipse.org/bugs/show_bug.cgi?id=313771
	private IPerspectiveDescriptor getDescriptorFor(String id) {
		return PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(id);
	}

	private MPerspective getPerspectiveFor(IPerspectiveDescriptor desc) {
		MPerspectiveStack stack = getPerspectiveStack();
		if (stack != null) {
			// Create an item for each perspective that should show up
			for (MPerspective persp : stack.getChildren()) {
				if (persp.getElementId().equals(desc.getId())) {
					return persp;
				}
			}
		}
		return null;
	}

	// FIXME singletons, singletons, everywhere!!
	// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=313771
	private void openPerspective(PerspectiveDescriptor desc) {
		if (desc == null) {
			// something bad happened
			E4Util.unsupported("Couldn't open perspective");
			return;
		}

		MPerspective persp = AdvancedFactoryImpl.eINSTANCE.createPerspective();
		// tag it with the same id
		persp.setElementId(desc.getId());
		persp.setLabel(desc.getLabel());

		List<MPerspective> children = getPerspectiveStack().getChildren();

		// instantiate the perspective
		IPerspectiveFactory factory = (desc).createFactory();
		ModeledPageLayout modelLayout = new ModeledPageLayout(window, modelService, partService,
				persp, desc, (WorkbenchPage) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage(), true);
		factory.createInitialLayout(modelLayout);
		PerspectiveTagger.tagPerspective(persp, modelService);
		PerspectiveExtensionReader reader = new PerspectiveExtensionReader();
		reader.extendLayout(null, desc.getId(), modelLayout);

		// add it to the stack
		children.add(persp);

		// activate it
		getPerspectiveStack().setSelectedElement(persp);
		window.getContext().set(IContextConstants.ACTIVE_CHILD, persp.getContext());

	}

	private void selectPerspective() {
		SelectPerspectiveDialog dialog = new SelectPerspectiveDialog(psTB.getShell(), PlatformUI
				.getWorkbench().getPerspectiveRegistry());
		dialog.open();
		if (dialog.getReturnCode() == Window.CANCEL) {
			return;
		}
		IPerspectiveDescriptor descriptor = dialog.getSelection();
		if (descriptor != null) {
			MPerspective persp = getPerspectiveFor(descriptor);
			if (persp != null) {
				if (!persp.isToBeRendered())
					persp.setToBeRendered(true);
				persp.getParent().setSelectedElement(persp);
			} else {
				openPerspective((PerspectiveDescriptor) descriptor);
			}
		}
	}

	private void openMenuFor(ToolItem item, MPerspective persp) {
		final Menu menu = new Menu(psTB);
		menu.setData(persp);
		if (persp.isVisible()) {
			addItem(menu, "Customize...");
			addItem(menu, "Save As...");
			addCloseItem(menu);
		}
		if (persp.getParent().getSelectedElement() == persp) {
			addResetItem(menu);
		}

		new MenuItem(menu, SWT.SEPARATOR);
		// addDockOnSubMenu(menu);
		addShowTextItem(menu);

		Rectangle bounds = item.getBounds();
		Point point = psTB.toDisplay(bounds.x, bounds.y + bounds.height);
		menu.setLocation(point.x, point.y);
		menu.setVisible(true);
		menu.addMenuListener(new MenuListener() {

			public void menuHidden(MenuEvent e) {
				psTB.getDisplay().asyncExec(new Runnable() {

					public void run() {
						menu.dispose();
					}

				});
			}

			public void menuShown(MenuEvent e) {
				// Nothing to do
			}

		});
	}

	private void addCloseItem(final Menu menu) {
		MenuItem menuItem = new MenuItem(menu, SWT.NONE);
		menuItem.setText("&Close");
		menuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				MPerspective persp = (MPerspective) menu.getData();
				if (persp != null)
					closePerspective(persp);
			}
		});
	}

	private void closePerspective(MPerspective persp) {
		IWorkbenchPage page = persp.getContext().get(IWorkbenchPage.class);
		IPerspectiveDescriptor desc = getDescriptorFor(persp.getElementId());
		page.closePerspective(desc, true, false);

		removePerspectiveItem(persp);
	}

	private void addResetItem(final Menu menu) {
		MenuItem menuItem = new MenuItem(menu, SWT.NONE);
		menuItem.setText("&Reset");
		menuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				final MPerspective persp = (MPerspective) menu.getData();
				if (persp == null)
					return;
				IWorkbenchPage page = persp.getContext().get(IWorkbenchPage.class);
				page.resetPerspective();
			}
		});
	}

	private void addShowTextItem(final Menu menu) {
		final MenuItem showtextMenuItem = new MenuItem(menu, SWT.CHECK);
		showtextMenuItem.setText("&Show Text");
		showtextMenuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				boolean preference = showtextMenuItem.getSelection();
				if (preference != PrefUtil.getAPIPreferenceStore().getDefaultBoolean(
						IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR)) {
					PrefUtil.getInternalPreferenceStore().setValue(
							IPreferenceConstants.OVERRIDE_PRESENTATION, true);
				}
				PrefUtil.getAPIPreferenceStore().setValue(
						IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR, preference);
				changeShowText(preference);
			}
		});
		showtextMenuItem.setSelection(PrefUtil.getAPIPreferenceStore().getBoolean(
				IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR));
	}

	private void changeShowText(boolean showText) {
		ToolItem[] items = psTB.getItems();
		for (int i = 0; i < items.length; i++) {
			MPerspective persp = (MPerspective) items[i].getData();
			if (persp != null)
				if (showText) {
					if (persp.getLabel() != null)
						items[i].setText(persp.getLabel());
					items[i].setToolTipText(persp.getTooltip());
				} else {
					Image image = items[i].getImage();
					if (image != null) {
						items[i].setText("");
						items[i].setToolTipText(persp.getLabel());
					}
				}
		}
		// update fix the layout
		psTB.pack();
		psTB.getShell().layout(new Control[] { psTB }, SWT.DEFER);
	}

	private void addItem(final Menu menu, final String label) {
		MenuItem menuItem = new MenuItem(menu, SWT.NONE);
		menuItem.setText(label);
		menuItem.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				MPerspective persp = (MPerspective) menu.getData();
				E4Util.unsupported(label + " " + persp.getLabel());
			}
		});

	}

	private void removePerspectiveItem(MPerspective toRemove) {
		ToolItem psItem = getItemFor(toRemove);
		if (psItem != null) {
			psItem.dispose();
		}

		// update the layout
		psTB.pack();
		psTB.getShell().layout(new Control[] { psTB }, SWT.DEFER);
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

	void paint(PaintEvent e) {
		GC gc = e.gc;
		Point size = comp.getSize();
		Color border1 = borderColor;
		int h = size.y;
		int[] simpleCurve = new int[] { 0, h - 1, 1, h - 1, 2, h - 2, 2, 1, 3, 0 };
		// draw border
		gc.setForeground(border1);
		gc.setAntialias(SWT.ON);
		gc.drawPolyline(simpleCurve);

		Rectangle bounds = ((Control) e.widget).getBounds();
		bounds.x = bounds.y = 0;
		Region r = new Region();
		r.add(bounds);
		int[] simpleCurveClose = new int[simpleCurve.length + 4];
		System.arraycopy(simpleCurve, 0, simpleCurveClose, 0, simpleCurve.length);
		int index = simpleCurve.length;
		simpleCurveClose[index++] = bounds.width;
		simpleCurveClose[index++] = 0;
		simpleCurveClose[index++] = bounds.width;
		simpleCurveClose[index++] = bounds.height;
		r.subtract(simpleCurveClose);
		Region clipping = new Region();
		gc.getClipping(clipping);
		r.intersect(clipping);
		gc.setClipping(r);
		Image b = toolParent.getBackgroundImage();
		if (b != null && !b.isDisposed())
			gc.drawImage(b, 0, 0);

		r.dispose();
		clipping.dispose();
		// // gc.fillRectangle(bounds);
		// Rectangle mappedBounds = e.display.map(comp, comp.getParent(), bounds);
		// ((Composite) toolParent).drawBackground(gc, bounds.x, bounds.y, bounds.width,
		// bounds.height, mappedBounds.x, mappedBounds.y);

	}

	void resize() {
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

	void disposeTBImages() {
		ToolItem[] items = psTB.getItems();
		for (int i = 0; i < items.length; i++) {
			Image image = items[i].getImage();
			if (image != null) {
				items[i].setImage(null);
				image.dispose();
			}
		}
	}
}
