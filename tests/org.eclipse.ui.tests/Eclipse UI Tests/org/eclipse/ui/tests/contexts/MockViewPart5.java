/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.contexts;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.tests.api.MockWorkbenchPart;

public class MockViewPart5 extends MockWorkbenchPart implements IViewPart {
	/**
	 * 
	 */
	public static final String PART_CONTEXT_ID = "org.eclipse.ui.tests.contexts.ViewPart";

	public static String ID = "org.eclipse.ui.tests.contexts.MockViewPart5";

	public static String NAME = "Context Mock View 5";

	private ContributionItem toolbarItem = new ContributionItem("someId") {

		private DisposeListener disposeListener = new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				toolbarContributionItemWidgetDisposed();
			}

		};

		@Override
		public void fill(ToolBar parent, int index) {
			super.fill(parent, index);

			ToolItem item = new ToolItem(parent, index);

			item.addDisposeListener(disposeListener);
			item.setImage(WorkbenchImages.getImage(ISharedImages.IMG_DEF_VIEW));
		}

		@Override
		public void dispose() {
			toolbarContributionItemDisposed();
			super.dispose();
		}
	};

	public MockViewPart5() {
		super();
	}

	/**
	 * @see IViewPart#getViewSite()
	 */
	@Override
	public IViewSite getViewSite() {
		return (IViewSite) getSite();
	}

	/**
	 * @see IViewPart#init(IViewSite)
	 */
	@Override
	public void init(IViewSite site) throws PartInitException {
		setSite(site);
		callTrace.add("init");
		setSiteInitialized();
		addToolbarContributionItem();
		addContext();
	}

	/**
	 * @see IViewPart#init(IViewSite, IMemento)
	 */
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		setSite(site);
		callTrace.add("init");
		setSiteInitialized();
		addToolbarContributionItem();
		addContext();
	}

	private void addContext() throws PartInitException {
		IContextService contextService = getSite()
				.getService(IContextService.class);
		if (!contextService.getContext(PART_CONTEXT_ID).isDefined()) {
			throw new PartInitException("Failed to find context "
					+ PART_CONTEXT_ID);
		}
		contextService.activateContext(PART_CONTEXT_ID);
	}

	private void addToolbarContributionItem() {
		getViewSite().getActionBars().getToolBarManager().add(toolbarItem);
	}

	public void toolbarContributionItemWidgetDisposed() {
		callTrace.add("toolbarContributionItemWidgetDisposed");
	}

	public void toolbarContributionItemDisposed() {
		callTrace.add("toolbarContributionItemDisposed");
	}

	/**
	 * @see IViewPart#saveState(IMemento)
	 */
	@Override
	public void saveState(IMemento memento) {
		// how's this for a comment, filthy human compiler
	}

	@Override
	protected IActionBars getActionBars() {
		return getViewSite().getActionBars();
	}
}
