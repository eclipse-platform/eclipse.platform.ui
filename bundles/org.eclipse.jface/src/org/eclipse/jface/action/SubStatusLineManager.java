package org.eclipse.jface.action;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.*;

/**
 * A <code>SubStatusLineManager</code> is used to define a set of contribution
 * items within a parent manager.  Once defined, the visibility of the entire set can 
 * be changed as a unit.
 */
public class SubStatusLineManager extends SubContributionManager 
	implements IStatusLineManager 
{
	/**
	 * The parent status line manager.
	 */
	private IStatusLineManager parentMgr;

	/**
	 * Current status line message.
	 */
	private String message;

	/**
	 * Current status line error message.
	 */
	private String errorMessage;

	/**
	 * Current status line image.
	 */
	private Image image;
/**
 * Constructs a new manager.
 *
 * @param mgr the parent manager.  All contributions made to the 
 *      <code>SubStatusLineManager</code> are forwarded and appear in the
 *      parent manager.
 */
public SubStatusLineManager(IStatusLineManager mgr) {
	super(mgr);
	parentMgr = mgr;
}
/* (non-Javadoc)
 * Method declared on IStatusLineManager.
 */
public IProgressMonitor getProgressMonitor() {
	return parentMgr.getProgressMonitor();
}
/* (non-Javadoc)
 * Method declared on IStatusLineManager.
 */
public boolean isCancelEnabled() {
	return parentMgr.isCancelEnabled();
}
/* (non-Javadoc)
 * Method declared on IStatusLineManager.
 */
public void setCancelEnabled(boolean enabled) {
	parentMgr.setCancelEnabled(enabled);
}
/* (non-Javadoc)
 * Method declared on IStatusLineManager.
 */
public void setErrorMessage(String message) {
	this.image = null;
	this.errorMessage = message;
	if (isVisible())
		parentMgr.setErrorMessage(errorMessage);
}
/* (non-Javadoc)
 * Method declared on IStatusLineManager.
 */
public void setErrorMessage(Image image, String message) {
	this.image = image;
	this.errorMessage = message;
	if (isVisible())
		parentMgr.setErrorMessage(image, errorMessage);
}
/* (non-Javadoc)
 * Method declared on IStatusLineManager.
 */
public void setMessage(String message) {
	this.image = null;
	this.message = message;
	if (isVisible())
		parentMgr.setMessage(message);
}
/* (non-Javadoc)
 * Method declared on IStatusLineManager.
 */
public void setMessage(Image image, String message) {
	this.image = image;
	this.message = message;
	if (isVisible())
		parentMgr.setMessage(image, message);
}
/* (non-Javadoc)
 * Method declared on SubContributionManager.
 */
public void setVisible(boolean visible) {
	super.setVisible(visible);
	if (visible) {
		parentMgr.setErrorMessage(image, errorMessage);
		parentMgr.setMessage(image, message);
	} else {
		parentMgr.setMessage(null, (String)null);
		parentMgr.setErrorMessage(null, null);
	}
}
/* (non-Javadoc)
 * Method declared on IStatusLineManager.
 */
public void update(boolean force) {
	// This method is not governed by visibility.  The client may
	// call <code>setVisible</code> and then force an update.  At that
	// point we need to update the parent.
	parentMgr.update(force);
}
}
