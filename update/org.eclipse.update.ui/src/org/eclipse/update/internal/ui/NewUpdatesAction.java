package org.eclipse.update.internal.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.update.internal.ui.search.DefaultUpdatesSearchObject;
import org.eclipse.update.internal.ui.search.ISearchCategory;
import org.eclipse.update.internal.ui.search.SearchCategoryDescriptor;
import org.eclipse.update.internal.ui.search.SearchCategoryRegistryReader;
import org.eclipse.update.internal.ui.search.SearchObject;

/**
 * Insert the type's description here.
 * @see IWorkbenchWindowActionDelegate
 */
public class NewUpdatesAction implements IWorkbenchWindowActionDelegate {
	IWorkbenchWindow window;
	SearchObject searchObject;
	ISearchCategory category;
	/**
	 * The constructor.
	 */
	public NewUpdatesAction() {
		initialize();
	}

	private void initialize() {
		searchObject = new DefaultUpdatesSearchObject();
		String categoryId = searchObject.getCategoryId();
		SearchCategoryDescriptor desc =
			SearchCategoryRegistryReader.getDefault().getDescriptor(categoryId);
		category = desc.createCategory();
	}

	/**
	 * Insert the method's description here.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		ProgressMonitorDialog pmd =
			new ProgressMonitorDialog(window.getShell());
		try {
			pmd.run(true, true, getOperation());
		} catch (InterruptedException e) {
			UpdateUIPlugin.logException(e);
		} catch (InvocationTargetException e) {
			UpdateUIPlugin.logException(e);
		}
	}

	private IRunnableWithProgress getOperation() {
		return searchObject.getSearchOperation(window.getShell().getDisplay(), category.getQueries());
	}

	/**
	 * Insert the method's description here.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * Insert the method's description here.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * Insert the method's description here.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}