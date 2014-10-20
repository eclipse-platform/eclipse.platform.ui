/*******************************************************************************
 * Copyright (c) 2009, 2014 IBM Corporation and others.
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
import org.eclipse.e4.core.commands.ExpressionContext;
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
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

/**
 * Create a toolbar
 */
public class ToolBarRenderer extends SWTPartRenderer {
	private MApplication application;

	HashMap<MToolBar, ArrayList<ArrayList<MToolBarElement>>> pendingCleanup = new HashMap<MToolBar, ArrayList<ArrayList<MToolBarElement>>>();

	@Override
	public void init(IEclipseContext context) {
		super.init(context);
		application = context.get(MApplication.class);
	}

	@Override
	public Object createWidget(final MUIElement element, Object parent) {
		if (!(element instanceof MToolBar) || !(parent instanceof Composite))
			return null;

		// HACK!! This should be done using a separate renderer
		Composite intermediate = new Composite((Composite) parent, SWT.NONE);
		createToolbar(element, intermediate);
		intermediate.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				cleanUp((MToolBar) element);
			}
		});

		return intermediate;
	}

	ToolBar createToolbar(final MUIElement element, Composite intermediate) {
		int orientation = getOrientation(element);
		RowLayout layout = RowLayoutFactory.fillDefaults().wrap(false)
				.spacing(0).type(orientation).create();
		layout.marginLeft = 3;
		layout.center = true;
		intermediate.setLayout(layout);
		ToolBar separatorToolBar = new ToolBar(intermediate, orientation
				| SWT.WRAP | SWT.FLAT | SWT.RIGHT);
		new ToolItem(separatorToolBar, SWT.SEPARATOR);
		return new ToolBar(intermediate, orientation | SWT.WRAP | SWT.FLAT
				| SWT.RIGHT);
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

	@Override
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

	ToolBar findToolbar(Composite intermediate) {
		if (!intermediate.isDisposed()) {
			Control[] children = intermediate.getChildren();
			int length = children.length;
			if (length > 0 && children[length - 1] instanceof ToolBar) {
				return (ToolBar) children[length - 1];
			}
		}
		return null;
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
		disposeToolbarIfNecessary(parentElement);
	}

	private void disposeToolbarIfNecessary(MUIElement element) {
		Composite composite = (Composite) element.getWidget();
		ToolBar toolbar = findToolbar(composite);
		if (toolbar != null && hasOnlySeparators(toolbar)) {
			toolbar.dispose();
			if (composite.getChildren().length > 0) {
				composite.getChildren()[0].dispose();
			}
		}
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
	public void childRendered(MElementContainer<MUIElement> parentElement,
			MUIElement element) {
		super.childRendered(parentElement, element);
		ToolBar toolbar = (ToolBar) getUIContainer(element);
		if (toolbar != null && !toolbar.isDisposed()) {
			toolbar.getShell().layout(new Control[] { toolbar }, SWT.DEFER);
		}
	}

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
		addToolBarContributions(toolbarModel, toContribute, ctx, eContext,
				pendingCleanup);
	}

	@Override
	public void postProcess(MUIElement element) {
		super.postProcess(element);
		disposeToolbarIfNecessary(element);
	}

	public static void addToolBarContributions(
			final MToolBar toolbarModel,
			ArrayList<MToolBarContribution> toContribute,
			IEclipseContext ctx,
			final ExpressionContext eContext,
			HashMap<MToolBar, ArrayList<ArrayList<MToolBarElement>>> pendingCleanup) {
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
