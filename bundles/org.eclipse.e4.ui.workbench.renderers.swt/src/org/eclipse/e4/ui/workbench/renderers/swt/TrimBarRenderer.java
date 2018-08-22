/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tristan Hume - <trishume@gmail.com> - Bug 2369
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution;
import org.eclipse.e4.ui.model.application.ui.menu.impl.ToolBarImpl;
import org.eclipse.e4.ui.workbench.swt.factories.IRendererFactory;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 *
 */
public class TrimBarRenderer extends SWTPartRenderer {
	private MApplication application;

	private class LayoutJob implements Runnable {
		public List<MTrimBar> barsToLayout = new ArrayList<>();

		@Override
		public void run() {
			layoutJob = null;
			if (barsToLayout.isEmpty())
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

	@Override
	public void init(IEclipseContext context) {
		super.init(context);
		application = context.get(MApplication.class);
	}

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
			trimComposite.addDisposeListener(e -> cleanUp(trimModel));
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

	@Override
	public void processContents(MElementContainer<MUIElement> me) {
		if (!(((MUIElement) me) instanceof MTrimBar))
			return;
		super.processContents(me);
		IEclipseContext ctx = getContext(me);
		ExpressionContext eContext = new ExpressionContext(ctx);
		MElementContainer<?> trimObj = me;
		MTrimBar trimModel = (MTrimBar) trimObj;
		ArrayList<MTrimContribution> toContribute = new ArrayList<>();
		ContributionsAnalyzer.gatherTrimContributions(trimModel,
				application.getTrimContributions(), trimModel.getElementId(),
				toContribute, eContext);
		addTrimContributions(trimModel, toContribute, ctx, eContext);
	}

	private void addTrimContributions(final MTrimBar trimModel,
			ArrayList<MTrimContribution> toContribute, IEclipseContext ctx,
			final ExpressionContext eContext) {
		HashSet<String> existingToolbarIds = new HashSet<>();

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

		boolean done = toContribute.isEmpty();
		while (!done) {
			ArrayList<MTrimContribution> curList = new ArrayList<>(
					toContribute);
			int retryCount = toContribute.size();
			toContribute.clear();

			for (final MTrimContribution contribution : curList) {
				final ArrayList<MTrimElement> toRemove = new ArrayList<>();
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
					trimModel.getPendingCleanup().addAll(toRemove);
				}
			}
			// We're done if the retryList is now empty (everything done) or
			// if the list hasn't changed at all (no hope)
			done = (toContribute.isEmpty())
					|| (toContribute.size() == retryCount);
		}
	}

	/**
	 * @param element
	 *            the trimBar to be cleaned up
	 */
	protected void cleanUp(MTrimBar element) {
		IRendererFactory rendererFactory = context.get(IRendererFactory.class);
		for (MTrimElement child : element.getPendingCleanup()) {
			element.getChildren().remove(child);
			if (child instanceof ToolBarImpl) {
				ToolBarImpl tb = (ToolBarImpl) child;
				releaseToolbar(rendererFactory, tb);
			}
		}
		element.getPendingCleanup().clear();
		List<MTrimElement> children = element.getChildren();
		for (MTrimElement mTrimElement : children) {
			if (mTrimElement instanceof ToolBarImpl) {
				ToolBarImpl tb = (ToolBarImpl) mTrimElement;
				releaseToolbar(rendererFactory, tb);
			}
		}
	}

	private void releaseToolbar(IRendererFactory rendererFactory, ToolBarImpl tb) {
		AbstractPartRenderer apr = rendererFactory.getRenderer(tb, null);
		if (apr instanceof ToolBarManagerRenderer) {
			ToolBarManagerRenderer tbmr = (ToolBarManagerRenderer) apr;
			ToolBarManager tbm = tbmr.getManager(tb);
			tbmr.clearModelToManager(tb, null);
			if (tbm != null) {
				tbm.dispose();
				tbm.removeAll();
			}
		}
	}
}
