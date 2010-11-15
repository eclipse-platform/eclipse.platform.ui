/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
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
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MRenderedToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.ExpressionContext;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.action.ToolBarManager;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

/**
 * Create a contribute part.
 */
public class RenderedToolBarRenderer extends SWTPartRenderer {

	public static final String TAG_VIEW_MENU = "ViewMenu"; //$NON-NLS-1$

	private MApplication application;

	@Inject
	IPresentationEngine renderer;

	HashMap<MToolBar, ArrayList<ArrayList<MToolBarElement>>> pendingCleanup = new HashMap<MToolBar, ArrayList<ArrayList<MToolBarElement>>>();

	private Image viewMenuImage;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.renderers.swt.SWTPartRenderer#init(org.eclipse
	 * .e4.core.contexts.IEclipseContext)
	 */
	@Override
	public void init(IEclipseContext context) {
		super.init(context);
		application = context.get(MApplication.class);
	}

	public Object createWidget(final MUIElement element, Object parent) {
		if (!(element instanceof MRenderedToolBar)
				|| !(parent instanceof Composite))
			return null;

		final MRenderedToolBar toolBar = (MRenderedToolBar) element;
		if (!(toolBar.getContributionManager() instanceof ToolBarManager)) {
			return null;
		}

		ToolBarManager tbm = (ToolBarManager) toolBar.getContributionManager();
		ToolBar tb = tbm.createControl((Composite) parent);
		if (tb.getParent() != parent) {
			tb.setParent((Composite) parent);
			tb.setVisible(true);
		}

		tbm.update(true);
		tb.setData(ToolBarManager.class.getName(), tbm);
		tb.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				cleanUp(toolBar);
			}
		});

		tb.getParent().layout(true);

		return tb;
	}

	/**
	 * @param toolBar
	 */
	protected void cleanUp(MRenderedToolBar element) {
		ArrayList<ArrayList<MToolBarElement>> lists = pendingCleanup
				.remove(element);
		if (lists == null) {
			return;
		}
		for (ArrayList<MToolBarElement> list : lists) {
			for (MToolBarElement child : list) {
				element.getChildren().remove(child);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer#hideChild
	 * (org.eclipse.e4.ui.model.application.MElementContainer,
	 * org.eclipse.e4.ui.model.application.MUIElement)
	 */
	@Override
	public void hideChild(MElementContainer<MUIElement> parentElement,
			MUIElement child) {
		super.hideChild(parentElement, child);

		// Since there's no place to 'store' a child that's not in a menu
		// we'll blow it away and re-create on an add
		Widget widget = (Widget) child.getWidget();
		if (widget != null && !widget.isDisposed())
			widget.dispose();
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
		// We've delegated further rendering to the ContributionManager
		// it's their fault the menu items don't show up!
		IEclipseContext ctx = getContext(container);
		ExpressionContext eContext = new ExpressionContext(ctx);
		ArrayList<MToolBarContribution> toContribute = new ArrayList<MToolBarContribution>();
		MElementContainer<?> toolbarObj = container;
		MToolBar toolbarModel = (MToolBar) toolbarObj;
		ContributionsAnalyzer.gatherToolBarContributions(toolbarModel,
				application.getToolBarContributions(),
				toolbarModel.getElementId(), toContribute, eContext);
		ToolBarRenderer.addToolBarContributions(toolbarModel, toContribute,
				ctx, eContext, pendingCleanup);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer#postProcess
	 * (org.eclipse.e4.ui.model.application.ui.MUIElement)
	 */
	@Override
	public void postProcess(MUIElement element) {
		ToolBar tb = (ToolBar) element.getWidget();
		if (((EObject) element).eContainer() instanceof MPart) {
			MPart part = (MPart) ((EObject) element).eContainer();
			MMenu viewMenu = getViewMenu(part);

			// View menu (if any)
			if (viewMenu != null) {
				addMenuButton(part, tb, viewMenu);
			}
		}

		tb.pack();
		tb.setVisible(true);
		super.postProcess(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.renderers.swt.SWTPartRenderer#disposeWidget
	 * (org.eclipse.e4.ui.model.application.ui.MUIElement)
	 */
	@Override
	public void disposeWidget(MUIElement element) {
		ToolBar tb = (ToolBar) element.getWidget();
		tb.setVisible(false);
		unbindWidget(element);
		tb.setData(AbstractPartRenderer.OWNING_ME, element);
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
			if (menu.getTags().contains(TAG_VIEW_MENU)) {
				return menu;
			}
		}
		return null;
	}
}
