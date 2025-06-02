/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 422040
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.preferences.ViewPreferencesAction;

/**
 * The ProgressView is the class that shows the details of the current workbench
 * progress.
 */
public class ProgressView extends ViewPart {

	DetailedProgressViewer viewer;

	Action cancelAction;

	Action clearAllAction;

	private IPartListener2 partListener;

	boolean visible;

	ProgressViewerContentProvider provider;

	class ActivationListener implements IPartListener2 {

		@Override
		public void partActivated(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partBroughtToTop(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partClosed(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partDeactivated(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partOpened(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partHidden(IWorkbenchPartReference partRef) {
			if (isMyPart(partRef)) {
				visible = false;
				provider.stopListening();
			}
		}

		@Override
		public void partVisible(IWorkbenchPartReference partRef) {
			if (isMyPart(partRef)) {
				visible = true;
				provider.startListening();
			}
		}

		@Override
		public void partInputChanged(IWorkbenchPartReference partRef) {
		}

		boolean isMyPart(IWorkbenchPartReference partRef) {
			return ProgressView.this == partRef.getPart(false);
		}

	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new DetailedProgressViewer(parent, SWT.MULTI);
		viewer.setComparator(ProgressManagerUtil.getProgressViewerComparator());

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IWorkbenchHelpContextIds.RESPONSIVE_UI);

		initContentProvider();
		createClearAllAction();
		createCancelAction();
		initContextMenu();
		initPulldownMenu();
		initToolBar();
		getSite().setSelectionProvider(viewer);
		partListener = new ActivationListener();
		getViewSite().getPage().addPartListener(partListener);
	}

	@Override
	public void dispose() {
		IViewSite site = getViewSite();
		if (partListener != null && site != null) {
			site.getPage().removePartListener(partListener);
			partListener = null;
		}
		super.dispose();
	}

	@Override
	public void setFocus() {
		if (viewer != null) {
			viewer.setFocus();
		}
	}

	/**
	 * Sets the content provider for the viewer.
	 */
	protected void initContentProvider() {
		provider = new ProgressViewerContentProvider(viewer, true, true);
		viewer.setContentProvider(provider);
		viewer.setInput(ProgressManager.getInstance());
	}

	/**
	 * Initialize the context menu for the receiver.
	 */
	private void initContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		menuMgr.add(cancelAction);
		menuMgr.addMenuListener(manager -> {
			JobInfo info = getSelectedInfo();
			if (info == null) {
				return;
			}
		});
		menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		getSite().registerContextMenu(menuMgr, viewer);
		viewer.getControl().setMenu(menu);
	}

	private void initPulldownMenu() {
		IMenuManager menuMgr = getViewSite().getActionBars().getMenuManager();
		menuMgr.add(clearAllAction);
		menuMgr.add(new ViewPreferencesAction() {
			@Override
			public void openViewPreferencesDialog() {
				new JobsViewPreferenceDialog(viewer.getControl().getShell()).open();

			}
		});

	}

	private void initToolBar() {
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager tm = bars.getToolBarManager();
		tm.add(clearAllAction);
	}

	/**
	 * Return the selected objects. If any of the selections are not JobInfos or
	 * there is no selection then return null.
	 *
	 * @return JobInfo[] or <code>null</code>.
	 */
	private IStructuredSelection getSelection() {
		// If the provider has not been set yet move on.
		ISelectionProvider provider = getSite().getSelectionProvider();
		if (provider == null) {
			return null;
		}
		ISelection currentSelection = provider.getSelection();
		if (currentSelection instanceof IStructuredSelection) {
			return (IStructuredSelection) currentSelection;
		}
		return null;
	}

	/**
	 * Get the currently selected job info. Only return it if it is the only item
	 * selected and it is a JobInfo.
	 *
	 * @return JobInfo
	 */
	JobInfo getSelectedInfo() {
		IStructuredSelection selection = getSelection();
		if (selection != null && selection.size() == 1) {
			JobTreeElement element = (JobTreeElement) selection.getFirstElement();
			if (element.isJobInfo()) {
				return (JobInfo) element;
			}
		}
		return null;
	}

	/**
	 * Create the cancel action for the receiver.
	 */
	private void createCancelAction() {
		cancelAction = new Action(ProgressMessages.ProgressView_CancelAction) {
			@Override
			public void run() {
				viewer.cancelSelection();
			}
		};

	}

	/**
	 * Create the clear all action for the receiver.
	 */
	private void createClearAllAction() {
		clearAllAction = new Action(ProgressMessages.ProgressView_ClearAllAction) {
			@Override
			public void run() {
				FinishedJobs.getInstance().clearAll();
			}
		};
		clearAllAction.setToolTipText(ProgressMessages.NewProgressView_RemoveAllJobsToolTip);
		ImageDescriptor id = WorkbenchImages.getWorkbenchImageDescriptor("/elcl16/progress_remall.svg"); //$NON-NLS-1$
		if (id != null) {
			clearAllAction.setImageDescriptor(id);
		}
	}

	/**
	 * @return Returns the viewer.
	 */
	public DetailedProgressViewer getViewer() {
		return viewer;
	}

}
