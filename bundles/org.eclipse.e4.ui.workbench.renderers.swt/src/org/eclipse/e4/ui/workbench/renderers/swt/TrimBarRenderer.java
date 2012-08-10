/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution;
import org.eclipse.e4.ui.workbench.modeling.ExpressionContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

/**
 *
 */
public class TrimBarRenderer extends SWTPartRenderer {
	private MApplication application;

	private class LayoutJob implements Runnable {
		public List<MTrimBar> barsToLayout = new ArrayList<MTrimBar>();

		public void run() {
			layoutJob = null;
			if (barsToLayout.size() == 0)
				return;
			for (MTrimBar bar : barsToLayout) {
				Composite trimCtrl = (Composite) bar.getWidget();
				if (trimCtrl != null && !trimCtrl.isDisposed())
					trimCtrl.layout();
			}
		}
	}

	private LayoutJob layoutJob = null;

	synchronized private void layoutTrim(MTrimBar trimBar) {
		Composite comp = (Composite) trimBar.getWidget();
		if (comp == null || comp.isDisposed())
			return;

		if (layoutJob == null) {
			layoutJob = new LayoutJob();
			layoutJob.barsToLayout.add(trimBar);
			comp.getDisplay().asyncExec(layoutJob);
		} else if (!layoutJob.barsToLayout.contains(trimBar)) {
			layoutJob.barsToLayout.add(trimBar);
		}
	}

	private HashMap<MTrimBar, ArrayList<ArrayList<MTrimElement>>> pendingCleanup = new HashMap<MTrimBar, ArrayList<ArrayList<MTrimElement>>>();

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.renderers.PartFactory#createWidget(org.eclipse
	 * .e4.ui.model.application.MPart)
	 */
	@Override
	public Object createWidget(MUIElement element, Object parent) {
		if (!(element instanceof MTrimBar) || !(parent instanceof Composite))
			return null;

		Composite parentComp = (Composite) parent;

		Composite trimComposite = null;
		final MTrimBar trimModel = (MTrimBar) element;
		if (parentComp.getLayout() instanceof TrimmedPartLayout) {
			TrimmedPartLayout tpl = (TrimmedPartLayout) parentComp.getLayout();

			switch (trimModel.getSide().getValue()) {
			case SideValue.TOP_VALUE:
				trimComposite = tpl.getTrimComposite(parentComp, SWT.TOP);
				break;
			case SideValue.BOTTOM_VALUE:
				trimComposite = tpl.getTrimComposite(parentComp, SWT.BOTTOM);
				break;
			case SideValue.LEFT_VALUE:
				trimComposite = tpl.getTrimComposite(parentComp, SWT.LEFT);
				break;
			case SideValue.RIGHT_VALUE:
				trimComposite = tpl.getTrimComposite(parentComp, SWT.RIGHT);
				break;
			default:
				return null;
			}
			trimComposite.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					cleanUp(trimModel);
				}
			});
		} else {
			trimComposite = new Composite(parentComp, SWT.NONE);
			trimComposite.setLayout(new TrimBarLayout(true));
		}
		return trimComposite;
	}

	@Override
	public void hideChild(MElementContainer<MUIElement> parentElement,
			MUIElement child) {
		super.hideChild(parentElement, child);

		Object downCast = parentElement;
		layoutTrim((MTrimBar) downCast);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.renderers.swt.SWTPartFactory#processContents
	 * (org.eclipse.e4.ui.model.application.MPart)
	 */
	@Override
	public void processContents(MElementContainer<MUIElement> me) {
		if (!(((MUIElement) me) instanceof MTrimBar))
			return;
		super.processContents(me);
		IEclipseContext ctx = getContext(me);
		ExpressionContext eContext = new ExpressionContext(ctx);
		MElementContainer<?> trimObj = me;
		MTrimBar trimModel = (MTrimBar) trimObj;
		ArrayList<MTrimContribution> toContribute = new ArrayList<MTrimContribution>();
		ContributionsAnalyzer.gatherTrimContributions(trimModel,
				application.getTrimContributions(), trimModel.getElementId(),
				toContribute, eContext);
		addTrimContributions(trimModel, toContribute, ctx, eContext);
	}

	private void addTrimContributions(final MTrimBar trimModel,
			ArrayList<MTrimContribution> toContribute, IEclipseContext ctx,
			final ExpressionContext eContext) {
		HashSet<String> existingToolbarIds = new HashSet<String>();

		MTrimmedWindow topWin = (MTrimmedWindow) modelService
				.getTopLevelWindowFor(trimModel);
		for (MTrimBar bar : topWin.getTrimBars()) {
			for (MTrimElement item : bar.getChildren()) {
				String id = item.getElementId();
				if (id != null) {
					existingToolbarIds.add(id);
				}
			}
		}

		boolean done = toContribute.size() == 0;
		while (!done) {
			ArrayList<MTrimContribution> curList = new ArrayList<MTrimContribution>(
					toContribute);
			int retryCount = toContribute.size();
			toContribute.clear();

			for (final MTrimContribution contribution : curList) {
				final ArrayList<MTrimElement> toRemove = new ArrayList<MTrimElement>();
				if (!ContributionsAnalyzer.processAddition(trimModel,
						contribution, toRemove, existingToolbarIds)) {
					toContribute.add(contribution);
				} else {
					if (contribution.getVisibleWhen() != null) {
						ctx.runAndTrack(new RunAndTrack() {
							@Override
							public boolean changed(IEclipseContext context) {
								if (!trimModel.isToBeRendered()
										|| !trimModel.isVisible()
										|| trimModel.getWidget() == null) {
									return false;
								}
								boolean rc = ContributionsAnalyzer.isVisible(
										contribution, eContext);
								for (MTrimElement child : toRemove) {
									child.setToBeRendered(rc);
								}
								return true;
							}
						});
					}
					ArrayList<ArrayList<MTrimElement>> lists = pendingCleanup
							.get(trimModel);
					if (lists == null) {
						lists = new ArrayList<ArrayList<MTrimElement>>();
						pendingCleanup.put(trimModel, lists);
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

	protected void cleanUp(MTrimBar element) {
		ArrayList<ArrayList<MTrimElement>> lists = pendingCleanup
				.remove(element);
		if (lists == null) {
			return;
		}
		for (ArrayList<MTrimElement> list : lists) {
			for (MTrimElement child : list) {
				element.getChildren().remove(child);
			}
		}
	}
}
