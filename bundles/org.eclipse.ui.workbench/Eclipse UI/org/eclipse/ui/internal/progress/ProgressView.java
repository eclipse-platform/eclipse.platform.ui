package org.eclipse.ui.internal.progress;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

public class ProgressView extends ViewPart implements IViewPart {

	ProgressTreeViewer viewer;
	private Action cancelAction;
	private Action deleteAction;
	private Action showErrorAction;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		viewer =
			new ProgressTreeViewer(
				parent,
				SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setUseHashlookup(true);
		viewer.setSorter(getViewerSorter());

		initContentProvider();
		initLabelProvider();
		initContextMenu();
		getSite().setSelectionProvider(viewer);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {

	}
	/**
	 * Sets the content provider for the viewer.
	 */
	protected void initContentProvider() {
		IContentProvider provider = new ProgressContentProvider(viewer);
		viewer.setContentProvider(provider);
		viewer.setInput(provider);
	}

	/**
	 * Sets the label provider for the viewer.
	 */
	protected void initLabelProvider() {
		viewer.setLabelProvider(new ProgressLabelProvider());

	}

	/**
	 * Initialize the context menu for the receiver.
	 */

	private void initContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$

		Menu menu = menuMgr.createContextMenu(viewer.getTree());

		createCancelAction();
		createDeleteAction();
		createShowErrorAction();
		menuMgr.add(cancelAction);
		menuMgr.add(deleteAction);
		menuMgr.add(showErrorAction);

		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				cancelAction.setEnabled(false);
				deleteAction.setEnabled(false);
				showErrorAction.setEnabled(false);
				JobInfo info = getSelectedInfo();
				if (info == null) {
					return;
				}
				int code = info.getStatus().getCode();
				if (code == JobInfo.PENDING_STATUS
					|| code == JobInfo.RUNNING_STATUS)
					cancelAction.setEnabled(true);
				else if (code == IStatus.ERROR) {
					deleteAction.setEnabled(true);
					showErrorAction.setEnabled(true);
				}

			}
		});

		menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		getSite().registerContextMenu(menuMgr, viewer);
		viewer.getTree().setMenu(menu);

	}

	/**
	 * Return the selected objects. If any of the selections are 
	 * not JobInfos or there is no selection then return null.
	 * @return JobInfo[] or <code>null</code>.
	 */
	private IStructuredSelection getSelection() {

		//If the provider has not been set yet move on.
		ISelectionProvider provider = getSite().getSelectionProvider();
		if (provider == null)
			return null;
		ISelection currentSelection = provider.getSelection();
		if (currentSelection instanceof IStructuredSelection) {
			return (IStructuredSelection) currentSelection;
		}
		return null;
	}

	/**
	 * Get the currently selected job info. Only return 
	 * it if it is the only item selected and it is a
	 * JobInfo.
	 * @return
	 */
	private JobInfo getSelectedInfo() {
		IStructuredSelection selection = getSelection();
		if (selection != null && selection.size() == 1) {
			JobTreeElement element =
				(JobTreeElement) selection.getFirstElement();
			if (element.isJobInfo())
				return (JobInfo) element;
		}
		return null;

	}

	/**
	 * Return a viewer sorter for looking at the jobs.
	 * @return
	 */
	private ViewerSorter getViewerSorter() {
		return new ViewerSorter() {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
			 */
			public int compare(Viewer testViewer, Object e1, Object e2) {
				return ((JobTreeElement) e1).compareTo((JobTreeElement) e2);
			}
		};
	}

	/**
	 * Create the cancel action for the receiver.
	 * @return Action
	 */
	private void createCancelAction() {
			cancelAction = new Action(ProgressMessages.getString("ProgressView.CancelAction")) {//$NON-NLS-1$
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
			public void run() {
				JobInfo element = getSelectedInfo();
				element.getJob().cancel();

			}

		};
	}

	/**
	 * Create the delete action for the receiver.
	 * @return Action
	 */
	private void createDeleteAction() {
			deleteAction = new Action(ProgressMessages.getString("ProgressView.DeleteAction")) {//$NON-NLS-1$
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
			public void run() {
				JobInfo element = getSelectedInfo();
				(
					(ProgressContentProvider) viewer
						.getContentProvider())
						.clearJob(
					element.getJob());
			}
		};
	}

	/**
	 * Create the show error action for the receiver.
	 * @return Action
	 */
	private void createShowErrorAction() {
			showErrorAction = new Action(ProgressMessages.getString("ProgressView.ShowErrorAction")) {//$NON-NLS-1$
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
			public void run() {
				JobInfo element = getSelectedInfo();
				ErrorDialog.openError(
					viewer.getControl().getShell(),
					element.getDisplayString(),
					element.getStatus().getMessage(),
					element.getStatus());
			}

		};
	}
}