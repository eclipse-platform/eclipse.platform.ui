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
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.menu.MRenderedToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.workbench.modeling.ExpressionContext;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Widget;

/**
 * Create a contribute part.
 */
public class RenderedToolBarRenderer extends SWTPartRenderer {
	private MApplication application;

	HashMap<MToolBar, ArrayList<ArrayList<MToolBarElement>>> pendingCleanup = new HashMap<MToolBar, ArrayList<ArrayList<MToolBarElement>>>();

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
		MRenderedToolBar toolbarModel = (MRenderedToolBar) toolbarObj;
		if (toolbarModel.getContributionManager() instanceof ToolBarManager) {
			final ArrayList<MToolBarElement> toRemove = new ArrayList<MToolBarElement>();
			IContributionItem[] items = ((ToolBarManager) toolbarModel
					.getContributionManager()).getItems();
			if (items.length > 0) {
				for (IContributionItem item : items) {
					if (item.isGroupMarker() || item.isSeparator()) {
						MToolBarSeparator sep = MenuFactoryImpl.eINSTANCE
								.createToolBarSeparator();
						sep.setElementId(item.getId());
						sep.setVisible(false);
						toolbarModel.getChildren().add(sep);
						toRemove.add(sep);
					}
				}
				if (!toRemove.isEmpty()) {
					ArrayList<ArrayList<MToolBarElement>> lists = pendingCleanup
							.get(toolbarModel);
					if (lists == null) {
						lists = new ArrayList<ArrayList<MToolBarElement>>();
						pendingCleanup.put(toolbarModel, lists);
					}
					lists.add(toRemove);
				}
			}
		}
		ContributionsAnalyzer.gatherToolBarContributions(toolbarModel,
				application.getToolBarContributions(),
				toolbarModel.getElementId(), toContribute, eContext);
		ToolBarRenderer.addToolBarContributions(toolbarModel, toContribute,
				ctx, eContext, pendingCleanup);
	}
}
