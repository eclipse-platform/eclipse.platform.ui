package org.eclipse.jface.action;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import java.util.*;

/**
 * A status line manager is a contribution manager which realizes itself and its items
 * in a status line control.
 * <p>
 * This class may be instantiated; it may also be subclassed if a more
 * sophisticated layout is required.
 * </p>
 */
public class StatusLineManager extends ContributionManager implements IStatusLineManager {

	/**
	 * The status line control; <code>null</code> before
	 * creation and after disposal.
	 */
	private StatusLine statusLine = null;
/**
 * Creates a new status line manager.
 * Use the <code>createControl</code> method to create the 
 * status line control.
 */
public StatusLineManager() {
}
/**
 * Creates and returns this manager's status line control. 
 * Does not create a new control if one already exists.
 *
 * @param parent the parent control
 * @return the status line control
 */
public StatusLine createControl(Composite parent) {
	if (statusLine == null && parent != null) {
		statusLine= new StatusLine(parent);
		update(false);
	}
	return statusLine;
}
/**
 * Disposes of this status line manager and frees all allocated SWT resources.
 * Note that this method does not clean up references between this status line
 * manager and its associated contribution items. 
 * Use <code>removeAll</code> for that purpose.
 */
public void dispose() {
	if (statusLine != null) {
		statusLine.dispose();
		statusLine = null;
	}
}
/**
 * Internal -- returns the StatusLine control.
 * <p>
 * This method is not intended to be used outside of the JFace framework.
 * </p>
 */
public Control getControl() {
	return statusLine;
}
/*
 * (non-Javadoc)
 * Method declared on IStatusLineManager
 */
public IProgressMonitor getProgressMonitor() {
	return statusLine;
}
/* (non-Javadoc)
 * Method declared on IStatueLineManager
 */
public boolean isCancelEnabled() {
	return statusLine != null && statusLine.isCancelEnabled();
}
/* (non-Javadoc)
 * Method declared on IStatueLineManager
 */
public void setCancelEnabled(boolean enabled) {
	if (statusLine != null && !statusLine.isDisposed())
		statusLine.setCancelEnabled(enabled);
}
/* (non-Javadoc)
 * Method declared on IStatusLineManager.
 */
public void setErrorMessage(String message) {
	if (statusLine != null)
		statusLine.setErrorMessage(message);
}
/* (non-Javadoc)
 * Method declared on IStatusLineManager.
 */
public void setErrorMessage(Image image, String message) {
	if (statusLine != null)
		statusLine.setErrorMessage(image, message);
}
/* (non-Javadoc)
 * Method declared on IStatusLineManager.
 */
public void setMessage(String message) {
	if (statusLine != null)
		statusLine.setMessage(message);
}
/* (non-Javadoc)
 * Method declared on IStatusLineManager.
 */
public void setMessage(Image image, String message) {
	if (statusLine != null)
		statusLine.setMessage(image, message);
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 */
public void update(boolean force) {
	
	boolean DEBUG= false;
	
	if (isDirty() || force) {
		
		if (statusLine != null) {

			statusLine.setRedraw(false);
	
//			if (DEBUG) System.out.println("update:");

			if (false) {	// non-incremental update

				Control ws[]= statusLine.getChildren();
				for (int i= 0; i < ws.length; i++) {
					Control w= ws[i];
					Object data= w.getData();
					if (data instanceof IContributionItem) {
//						if (DEBUG) System.out.println("  disposing item: " + data);
						w.dispose();
					}
				}
				
				IContributionItem[] items= getItems();
				for (int i = 0; i < items.length; ++i) {
					IContributionItem ci= items[i];
					if (ci.isVisible()) {						
						ci.fill(statusLine);
//						if (DEBUG) System.out.println("  added item: " + ci);
					}
				}
				
			} else {	// incremental update
				// copy all active items into set
				IContributionItem[] items= getItems();
				HashMap ht= new HashMap(items.length*2);
				for (int i = 0; i < items.length; ++i) {
					IContributionItem ci= items[i];
					if (ci.isVisible())
						ht.put(ci, ci);
				}

				// remove obsolete (removed or non active)
				Control ws[]= statusLine.getChildren();
				for (int i= 0; i < ws.length; i++) {
					Object data= ws[i].getData();
					if (data instanceof IContributionItem) {
						IContributionItem item= (IContributionItem) data;
						if (ht.get(item) == null) {	// not found
						//	if (DEBUG) System.out.println("  disposing item: " + data);
							ws[i].dispose();
						}
					}
				}

				// add new
				IContributionItem src, dest;
				ws= statusLine.getChildren();
				int srcIx= 2;
				int destIx= 0;
				for (int i = 0; i < items.length; ++i) {
					src= (IContributionItem) items[i];
					
					if (! src.isVisible())	// if not active skip this one
						continue;	// we don't bounce the destIx!

					// get corresponding item in SWT widget
					if (srcIx < ws.length)
						dest= (IContributionItem) ws[srcIx].getData();
					else
						dest= null;
						
					if (dest != null && src.equals(dest)) {	// no change
						//if (DEBUG) System.out.println("  no change: ");
						srcIx++;
					} else {
						// src is a new one: insert it at next position
						src.fill(statusLine);
					//	if (DEBUG) System.out.println("  added at " + destIx + ": ");
					}
					destIx++;
				}
			}
			setDirty(false);
			
			statusLine.layout();
			statusLine.setRedraw(true);
		}
	}
}
}
