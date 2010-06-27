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
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarSeparator;
import org.eclipse.e4.ui.workbench.modeling.ExpressionContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Widget;

/**
 * Create a contribute part.
 */
public class ToolBarRenderer extends SWTPartRenderer {
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
		if (!(element instanceof MToolBar) || !(parent instanceof Composite))
			return null;

		int orientation = SWT.HORIZONTAL;

		MUIElement theParent = element.getParent();
		if (theParent instanceof MTrimBar) {
			MTrimBar trimContainer = (MTrimBar) theParent;
			SideValue side = trimContainer.getSide();
			if (side.getValue() == SideValue.LEFT_VALUE
					|| side.getValue() == SideValue.RIGHT_VALUE)
				orientation = SWT.VERTICAL;
		}

		// HACK!! This should be done using a separate renderer
		ToolBar tb = null;
		tb = new ToolBar((Composite) parent, orientation | SWT.WRAP | SWT.FLAT
				| SWT.RIGHT);
		tb.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				cleanUp((MToolBar) element);
			}
		});

		return tb;
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
		super.processContents(container);
		IEclipseContext ctx = getContext(container);
		ExpressionContext eContext = new ExpressionContext(ctx);
		ArrayList<MToolBarContribution> toContribute = new ArrayList<MToolBarContribution>();
		MElementContainer<?> toolbarObj = container;
		MToolBar toolbarModel = (MToolBar) toolbarObj;
		ContributionsAnalyzer.gatherToolBarContributions(toolbarModel,
				application.getToolBarContributions(),
				toolbarModel.getElementId(), toContribute, eContext);
		addToolBarContributions(toolbarModel, toContribute, ctx, eContext);
	}

	private void addToolBarContributions(final MToolBar toolbarModel,
			ArrayList<MToolBarContribution> toContribute, IEclipseContext ctx,
			final ExpressionContext eContext) {
		HashSet<String> existingSeparatorNames = new HashSet<String>();
		for (MToolBarElement child : toolbarModel.getChildren()) {
			String elementId = child.getElementId();
			if (child instanceof MToolBarSeparator && elementId != null) {
				existingSeparatorNames.add(elementId);
			}
		}
		boolean done = toContribute.size() == 0;
		while (!done) {
			ArrayList<MToolBarContribution> curList = new ArrayList<MToolBarContribution>(
					toContribute);
			int retryCount = toContribute.size();
			toContribute.clear();

			for (final MToolBarContribution contribution : curList) {
				final ArrayList<MToolBarElement> toRemove = new ArrayList<MToolBarElement>();
				if (!ContributionsAnalyzer.processAddition(toolbarModel,
						contribution, toRemove, existingSeparatorNames)) {
					toContribute.add(contribution);
				} else {
					if (contribution.getVisibleWhen() != null) {
						ctx.runAndTrack(new RunAndTrack() {
							@Override
							public boolean changed(IEclipseContext context) {
								if (!toolbarModel.isToBeRendered()
										|| !toolbarModel.isVisible()
										|| toolbarModel.getWidget() == null) {
									return false;
								}
								boolean rc = ContributionsAnalyzer.isVisible(
										contribution, eContext);
								for (MToolBarElement child : toRemove) {
									child.setToBeRendered(rc);
								}
								return true;
							}
						});
					}
					ArrayList<ArrayList<MToolBarElement>> lists = pendingCleanup
							.get(toolbarModel);
					if (lists == null) {
						lists = new ArrayList<ArrayList<MToolBarElement>>();
						pendingCleanup.put(toolbarModel, lists);
					}
					lists.add(toRemove);
				}
			}
			// We're done if the retryList is now empty (everything done) or
			// if the list hasn't changed at all (no hope)
			done = (toContribute.size() == 0)
					|| (toContribute.size() == retryCount);
		}
	}

	protected void cleanUp(MToolBar element) {
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

}
