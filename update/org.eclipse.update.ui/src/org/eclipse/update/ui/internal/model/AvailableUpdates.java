package org.eclipse.update.ui.internal.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.net.URL;
import org.eclipse.update.core.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.views.properties.*;
import org.eclipse.ui.model.*;
import java.util.*;
import org.eclipse.update.internal.ui.UpdateUIPluginImages;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import java.lang.reflect.InvocationTargetException;

public class AvailableUpdates extends ModelObject implements IWorkbenchAdapter {

	private Vector updates = new Vector();
	private boolean searchInProgress;
	private BackgroundProgressMonitor backgroundProgress;
	private BackgroundThread searchThread;
	private boolean debug = false;

class SearchAdapter extends MonitorAdapter {
	public void done() {
		searchInProgress = false;
	}
}
	
	public AvailableUpdates() {
		backgroundProgress = new BackgroundProgressMonitor();
		backgroundProgress.addProgressMonitor(new SearchAdapter());
	}
	
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IWorkbenchAdapter.class)) {
			return this;
		}
		return super.getAdapter(adapter);
	}
	
	public String getName() {
		return "Available Updates";
	}
	
	public String toString() {
		return getName();
	}
	
	/**
	 * @see IWorkbenchAdapter#getChildren(Object)
	 */
	public Object[] getChildren(Object parent) {
		return updates.toArray();
	}

	/**
	 * @see IWorkbenchAdapter#getImageDescriptor(Object)
	 */
	public ImageDescriptor getImageDescriptor(Object obj) {
		return UpdateUIPluginImages.DESC_UPDATES_OBJ;
	}

	/**
	 * @see IWorkbenchAdapter#getLabel(Object)
	 */
	public String getLabel(Object obj) {
		return getName();
	}

	/**
	 * @see IWorkbenchAdapter#getParent(Object)
	 */
	public Object getParent(Object arg0) {
		return getModel();
	}
	
	public void attachProgressMonitor(IProgressMonitor monitor) {
		backgroundProgress.addProgressMonitor(monitor);
	}
	public void detachProgressMonitor(IProgressMonitor monitor) {
		backgroundProgress.removeProgressMonitor(monitor);
	}
	
	public void startSearch(Display display) throws InvocationTargetException, InterruptedException {
		if (searchInProgress) return;
		backgroundProgress.setDisplay(display);
		IRunnableWithProgress operation = getSearchOperation();
		searchThread = new BackgroundThread(operation, backgroundProgress, Display.getDefault());
		searchInProgress = true;
		searchThread.start();
		Throwable throwable= searchThread.getThrowable();
		if (throwable != null) {
			if (debug) {
				System.err.println("Exception in search background thread:");//$NON-NLS-1$
				throwable.printStackTrace();
				System.err.println("Called from:");//$NON-NLS-1$
				// Don't create the InvocationTargetException on the throwable,
				// otherwise it will print its stack trace (from the other thread).
				new InvocationTargetException(null).printStackTrace();
			}
			if (throwable instanceof InvocationTargetException) {
				throw (InvocationTargetException) throwable;
			} else if (throwable instanceof InterruptedException) {
				throw (InterruptedException) throwable;
			} else if (throwable instanceof OperationCanceledException) {
				// See 1GAN3L5: ITPUI:WIN2000 - ModalContext converts OperationCancelException into InvocationTargetException
				throw new InterruptedException(throwable.getMessage());
			} else {
				throw new InvocationTargetException(throwable);
			}	
		}
	}
	
	public boolean isSearchInProgress() {
		return searchInProgress;
	}
	
	public void stopSearch() {
		if (!searchInProgress || searchThread==null) return;
		backgroundProgress.setCanceled(true);
	}
	
	public IRunnableWithProgress getSearchOperation() {
		return new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				doSearch(monitor);
			}
		};
	}
	
	private void doSearch(IProgressMonitor monitor) {
		backgroundProgress.beginTask("Searching...", 5);
		for (int i=0; i<5; i++) {
			if (monitor.isCanceled()) {
				break;
			}
			try {
				Thread.currentThread().sleep(2000);
				monitor.worked(1);
			}
			catch (InterruptedException e) {
			}
		}
		monitor.done();
	}
}