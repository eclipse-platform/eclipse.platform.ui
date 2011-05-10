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
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.ExpressionContext;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

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
	static final String TRIM_BAR_MANAGER_RENDERER_DRAG_HANDLE = "TrimBarRenderer.dragHandle"; //$NON-NLS-1$

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
		if (!(parentComp.getLayout() instanceof TrimmedPartLayout))
			return null;

		final MTrimBar trimModel = (MTrimBar) element;
		TrimmedPartLayout tpl = (TrimmedPartLayout) parentComp.getLayout();

		Composite result = null;
		switch (trimModel.getSide().getValue()) {
		case SideValue.TOP_VALUE:
			result = tpl.getTrimComposite(parentComp, SWT.TOP);
			break;
		case SideValue.BOTTOM_VALUE:
			result = tpl.getTrimComposite(parentComp, SWT.BOTTOM);
			break;
		case SideValue.LEFT_VALUE:
			result = tpl.getTrimComposite(parentComp, SWT.LEFT);
			break;
		case SideValue.RIGHT_VALUE:
			result = tpl.getTrimComposite(parentComp, SWT.RIGHT);
			break;
		default:
			return null;
		}
		processContents(trimModel);
		result.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				cleanUp(trimModel);
			}
		});
		return result;
	}

	/**
	 * @param trimModel
	 */
	private void processContents(MTrimBar trimModel) {
		IEclipseContext ctx = getContext(trimModel);
		ExpressionContext eContext = new ExpressionContext(ctx);
		ArrayList<MTrimContribution> toContribute = new ArrayList<MTrimContribution>();
		ContributionsAnalyzer.gatherTrimContributions(trimModel,
				application.getTrimContributions(), trimModel.getElementId(),
				toContribute, eContext);
		addTrimContributions(trimModel, toContribute, ctx, eContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.renderers.swt.SWTPartRenderer#childRendered
	 * (org.eclipse.e4.ui.model.application.ui.MElementContainer,
	 * org.eclipse.e4.ui.model.application.ui.MUIElement)
	 */
	@Override
	public void childRendered(MElementContainer<MUIElement> parentElement,
			MUIElement element) {
		super.childRendered(parentElement, element);
		Object downCast = parentElement;
		layoutTrim((MTrimBar) downCast);
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
	public void processContents(MElementContainer<MUIElement> container) {
		if (!(((MUIElement) container) instanceof MTrimBar))
			return;

		// Process any contents of the newly created ME
		List<MUIElement> parts = container.getChildren();
		final Composite parent = (Composite) container.getWidget();
		if (parts != null) {
			// loading a legacy app will add children to the window while it is
			// being rendered.
			// this is *not* the correct place for this
			// hope that the ADD event will pick up the new part.
			IPresentationEngine renderer = (IPresentationEngine) context
					.get(IPresentationEngine.class.getName());
			MUIElement[] plist = parts.toArray(new MUIElement[parts.size()]);
			for (int i = 0; i < plist.length; i++) {
				MUIElement childME = plist[i];
				final Composite intermediate = createIntermediate(childME,
						parent);
				final Object newObj = renderer.createGui(childME, intermediate,
						null);
				if (newObj == null) {
					intermediate.dispose();
				} else {
					((Widget) newObj).addListener(SWT.Dispose, new Listener() {
						public void handleEvent(Event event) {
							intermediate.dispose();
						}
					});
				}
			}
		}
	}

	private Composite createIntermediate(MUIElement toolbarModel,
			Composite parent) {
		Composite intermediate = new Composite((Composite) parent, SWT.NONE);
		intermediate.setData(AbstractPartRenderer.OWNING_ME, toolbarModel);
		int orientation = getOrientation(toolbarModel);
		RowLayout layout = RowLayoutFactory.fillDefaults().wrap(false)
				.spacing(0).type(orientation).create();
		layout.marginLeft = 3;
		layout.center = true;
		intermediate.setLayout(layout);
		ToolBar separatorToolBar = new ToolBar(intermediate, orientation
				| SWT.WRAP | SWT.FLAT | SWT.RIGHT);
		separatorToolBar.setData(TRIM_BAR_MANAGER_RENDERER_DRAG_HANDLE);
		new ToolItem(separatorToolBar, SWT.SEPARATOR);
		return intermediate;
	}

	private int getOrientation(final MUIElement element) {
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

	private void addTrimContributions(final MTrimBar trimModel,
			ArrayList<MTrimContribution> toContribute, IEclipseContext ctx,
			final ExpressionContext eContext) {
		HashSet<String> existingToolbarIds = new HashSet<String>();
		for (MTrimElement item : trimModel.getChildren()) {
			String id = item.getElementId();
			if (item instanceof MToolBar && id != null) {
				existingToolbarIds.add(id);
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
								layoutTrim(trimModel);
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
