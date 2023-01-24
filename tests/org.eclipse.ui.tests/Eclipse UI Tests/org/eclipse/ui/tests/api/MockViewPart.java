/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 444070
 *******************************************************************************/
package org.eclipse.ui.tests.api;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.internal.WorkbenchImages;
import org.junit.Assert;

public class MockViewPart extends MockWorkbenchPart implements IViewPart {
	public static String ID = "org.eclipse.ui.tests.api.MockViewPart";

	public static String ID2 = ID + "2";

	public static String ID3 = ID + "3";

	public static String ID4 = ID + "4";

	public static String IDMULT = ID + "Mult";

	public static String NAME = "Mock View 1";

	private ContributionItem toolbarItem = new ContributionItem("someId") {

		private DisposeListener disposeListener = e -> toolbarContributionItemWidgetDisposed();

		@Override
		public void fill(ToolBar parent, int index) {
			super.fill(parent, index);

			ToolItem item = new ToolItem(parent, SWT.NONE, index);

			item.addDisposeListener(disposeListener);
			item.setImage(WorkbenchImages.getImage(ISharedImages.IMG_DEF_VIEW));
		}

		@Override
		public void dispose() {
			toolbarContributionItemDisposed();
			super.dispose();
		}
	};

	private class DummyAction extends Action {
		public DummyAction() {
			setText("Monkey");
			setImageDescriptor(getViewSite().getWorkbenchWindow().getWorkbench().getSharedImages()
					.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		}
	}

	public MockViewPart() {
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
	public void init(IViewSite site) {
		setSite(site);
		callTrace.add("init");
		setSiteInitialized();
		addToolbarContributionItem();
	}

	/**
	 * @see IViewPart#init(IViewSite, IMemento)
	 */
	@Override
	public void init(IViewSite site, IMemento memento) {
		setSite(site);
		callTrace.add("init");
		setSiteInitialized();
		addToolbarContributionItem();
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		Button addAction = new Button(parent, SWT.PUSH);
		addAction.setText("Add Action to Toolbar");
		addAction.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IActionBars bars = getViewSite().getActionBars();
				bars.getToolBarManager().add(new DummyAction());
				bars.updateActionBars();
			}
		});

		Button removeAction = new Button(parent, SWT.PUSH);
		removeAction.setText("Remove Action from Toolbar");
		removeAction.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IActionBars bars = getViewSite().getActionBars();
				IToolBarManager tbm = bars.getToolBarManager();
				IContributionItem[] items = tbm.getItems();
				if (items.length > 0) {
					IContributionItem item = items[items.length - 1];
					if (item instanceof ActionContributionItem aci) {
						if (aci.getAction() instanceof DummyAction) {
							tbm.remove(item);
							bars.updateActionBars();
						}
					}
				}
			}
		});
	}

	@Override
	public void dispose() {
		// Test for bug 94457: The contribution items must still be in the
		// toolbar manager at the
		// time the part is disposed. (Changing this behavior would be a
		// breaking change for some
		// clients).
		Assert.assertTrue("Contribution items should not be removed from the site until after the part is disposed",
				getViewSite().getActionBars().getToolBarManager().find(toolbarItem.getId()) == toolbarItem);
		super.dispose();
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
		// do nothing
	}

	@Override
	protected IActionBars getActionBars() {
		return getViewSite().getActionBars();
	}
}
