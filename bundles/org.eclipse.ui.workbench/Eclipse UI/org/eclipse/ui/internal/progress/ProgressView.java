package org.eclipse.ui.internal.progress;

import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

public class ProgressView extends ViewPart implements IViewPart {

	ProgressTreeViewer viewer;

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
		// XXX Auto-generated method stub

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

		menuMgr.add(new Action(ProgressMessages.getString("ProgressView.CancelAction")) { //$NON-NLS-1$
			/* (non-Javadoc)
			 * @see org.eclipse.jface.action.Action#run()
			 */
			public void run() {
				IStructuredSelection selection = getSelection();
				Iterator items = selection.iterator();
				while (items.hasNext()) {
					JobTreeElement element = (JobTreeElement) items.next();
					if (element.isJobInfo()) {
						JobInfo info = (JobInfo) element;
						int code = info.getStatus().getCode();
						if (code == JobInfo.PENDING_STATUS
							|| code == JobInfo.RUNNING_STATUS)
							info.getJob().cancel();
					}
				}
			}

			/* (non-Javadoc)
			 * @see org.eclipse.jface.action.Action#isEnabled()
			 */
			public boolean isEnabled() {
				return hasSelection();
			}
		});

		menuMgr.add(new Action(ProgressMessages.getString("ProgressView.DeleteAction")) { //$NON-NLS-1$
			/* (non-Javadoc)
			 * @see org.eclipse.jface.action.Action#run()
			 */
			public void run() {
				IStructuredSelection selection = getSelection();
				Iterator items = selection.iterator();
				while (items.hasNext()) {
					JobTreeElement element = (JobTreeElement) items.next();
					if (element.isJobInfo()) {
						JobInfo info = (JobInfo) element;
						if (info.getStatus().getCode() == IStatus.ERROR)
							(
								(ProgressContentProvider) viewer
									.getContentProvider())
									.clearJob(
								info.getJob());
					}
				}
			}

			/* (non-Javadoc)
			 * @see org.eclipse.jface.action.Action#isEnabled()
			 */
			public boolean isEnabled() {
				return hasSelection();
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
	 * Return whether or not there are selected objects. If any of the selections are 
	 * not JobInfos or there is no selection then return false.
	 * @return boolean
	 */
	private boolean hasSelection() {

		//If the provider has not been set yet move on.
		ISelectionProvider provider = getSite().getSelectionProvider();
		ISelection currentSelection = provider.getSelection();
		return currentSelection != null;
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
			public int compare(Viewer viewer, Object e1, Object e2) {
				JobTreeElement element1 = (JobTreeElement) e1;
				JobTreeElement element2 = (JobTreeElement) e2;

				if (element1.isJobInfo() && element2.isJobInfo()) {
					IStatus status1 = ((JobInfo) element1).getStatus();
					IStatus status2 = ((JobInfo) element1).getStatus();
					int difference = status1.getCode() - status2.getCode();
					if (difference != 0)
						return difference;
				}
				return element1.getDisplayString().compareTo(
					element2.getDisplayString());

			}
		};
	}
}