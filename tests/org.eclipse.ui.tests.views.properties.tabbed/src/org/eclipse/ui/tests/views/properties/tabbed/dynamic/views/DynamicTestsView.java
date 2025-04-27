/*******************************************************************************
 * Copyright (c) 2007, 2019 IBM Corporation and others.
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
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 548799
 *******************************************************************************/
package org.eclipse.ui.tests.views.properties.tabbed.dynamic.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.tests.views.properties.tabbed.dynamic.model.DynamicTestsElement;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * A view to test dynamic contributions to the tabbed properties view. The view
 * has three modes of providing tabs and sections to the tabbed properties view:
 * use static contributions from plugin.xml, use dynamic section contributions
 * from code, or use dynamic tab (and section) contributions from code.
 *
 * @author Anthony Hunter
 */
public class DynamicTestsView extends ViewPart implements
		ITabbedPropertySheetPageContributor {

	static class DynamicTestsViewLabelProvider extends LabelProvider {

		@Override
		public Image getImage(Object obj) {
			DynamicTestsElement element = ((DynamicTestsTreeNode) obj)
					.getDynamicTestsElement();
			return element.getImage();
		}

		@Override
		public String getText(Object obj) {
			DynamicTestsElement element = ((DynamicTestsTreeNode) obj)
					.getDynamicTestsElement();
			return element.getName();
		}
	}

	public static final String DYNAMIC_TESTS_VIEW_ID = "org.eclipse.ui.tests.views.properties.tabbed.dynamic.views.DynamicTestsView"; //$NON-NLS-1$

	public static final String DYNAMIC_TESTS_VIEW_STATIC = "org.eclipse.ui.tests.views.properties.tabbed.static"; //$NON-NLS-1$

	public static final String DYNAMIC_TESTS_VIEW_DYNAMIC_TABS = "org.eclipse.ui.tests.views.properties.tabbed.dynamic.tab"; //$NON-NLS-1$

	public static final String DYNAMIC_TESTS_VIEW_DYNAMIC_SECTIONS = "org.eclipse.ui.tests.views.properties.tabbed.dynamic.section"; //$NON-NLS-1$
	private Action dynamicSectionsAction;

	private Action dynamicTabsAction;

	private Action staticAction;

	private TabbedPropertySheetPage tabbedPropertySheetPage;

	private TreeViewer viewer;

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new DynamicTestsViewContentProvider(this));
		viewer.setLabelProvider(new DynamicTestsViewLabelProvider());
		viewer.setInput(getViewSite());
		initToolBar();
		getSite().setSelectionProvider(viewer);
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == IPropertySheetPage.class) {
			if (tabbedPropertySheetPage == null) {
				tabbedPropertySheetPage = new TabbedPropertySheetPage(this);
			}
			return adapter.cast(tabbedPropertySheetPage);
		}
		return super.getAdapter(adapter);
	}

	@Override
	public String getContributorId() {
		if (staticAction.isChecked()) {
			return DYNAMIC_TESTS_VIEW_STATIC;
		} else if (dynamicSectionsAction.isChecked()) {
			return DYNAMIC_TESTS_VIEW_DYNAMIC_SECTIONS;
		} else if (dynamicTabsAction.isChecked()) {
			return DYNAMIC_TESTS_VIEW_DYNAMIC_TABS;
		} else {
			return null;
		}
	}

	public void setContributorId(String contributorId) {
		if (DYNAMIC_TESTS_VIEW_STATIC.equals(contributorId)) {
			staticAction.setChecked(true);
			dynamicSectionsAction.setChecked(false);
			dynamicTabsAction.setChecked(false);
		} else if (DYNAMIC_TESTS_VIEW_DYNAMIC_SECTIONS.equals(contributorId)) {
			staticAction.setChecked(false);
			dynamicSectionsAction.setChecked(true);
			dynamicTabsAction.setChecked(false);
		} else if (DYNAMIC_TESTS_VIEW_DYNAMIC_TABS.equals(contributorId)) {
			staticAction.setChecked(false);
			dynamicSectionsAction.setChecked(false);
			dynamicTabsAction.setChecked(true);
		}
	}

	public TabbedPropertySheetPage getTabbedPropertySheetPage() {
		return tabbedPropertySheetPage;
	}

	public TreeViewer getViewer() {
		return viewer;
	}

	/**
	 * Create the tool bar for the view. The tool bar has three toggle buttons
	 * which enable one of the three property contributors.
	 */
	private void initToolBar() {
		final String staticText = "Use static contributions from plugin.xml"; //$NON-NLS-1$
		final String dynamicSectionsText = "Use dynamic section contributions from code"; //$NON-NLS-1$
		final String dynamicTabsText = "Use dynamic tab (and section) contributions from code"; //$NON-NLS-1$
		String path = "icons/sample.gif"; //$NON-NLS-1$
		ImageDescriptor imageDescriptor = ResourceLocator.imageDescriptorFromBundle(getClass(), path).orElse(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_ERROR_TSK));
		staticAction = new Action(staticText, IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				if (isChecked()) {
					getViewSite().getActionBars().getStatusLineManager()
							.setMessage(staticText);
					dynamicSectionsAction.setChecked(false);
					dynamicTabsAction.setChecked(false);
					viewer.setSelection(StructuredSelection.EMPTY);
				}
			}
		};
		staticAction.setToolTipText(staticText);
		staticAction.setImageDescriptor(imageDescriptor);

		dynamicSectionsAction = new Action(dynamicSectionsText,
				IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				if (isChecked()) {
					getViewSite().getActionBars().getStatusLineManager()
							.setMessage(dynamicSectionsText);
					staticAction.setChecked(false);
					dynamicTabsAction.setChecked(false);
					viewer.setSelection(StructuredSelection.EMPTY);
				}
			}
		};
		dynamicSectionsAction.setToolTipText(dynamicSectionsText);
		dynamicSectionsAction.setImageDescriptor(imageDescriptor);

		dynamicTabsAction = new Action(dynamicTabsText, IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				if (isChecked()) {
					getViewSite().getActionBars().getStatusLineManager()
							.setMessage(dynamicTabsText);
					staticAction.setChecked(false);
					dynamicSectionsAction.setChecked(false);
					viewer.setSelection(StructuredSelection.EMPTY);
				}
			}
		};
		dynamicTabsAction.setToolTipText(dynamicTabsText);
		dynamicTabsAction.setImageDescriptor(imageDescriptor);
		dynamicTabsAction.setDisabledImageDescriptor(imageDescriptor);
		staticAction.setChecked(true);

		IToolBarManager toolBarManager = getViewSite().getActionBars()
				.getToolBarManager();
		toolBarManager.add(staticAction);
		toolBarManager.add(dynamicSectionsAction);
		toolBarManager.add(dynamicTabsAction);

		getViewSite().getActionBars().getStatusLineManager().setMessage(
				staticText);
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}