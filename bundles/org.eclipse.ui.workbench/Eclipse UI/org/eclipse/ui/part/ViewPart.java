/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.part;

import org.eclipse.jface.util.Assert;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.util.Util;

/**
 * Abstract base implementation of all workbench views.
 * <p>
 * This class should be subclassed by clients wishing to define new views.
 * The name of the subclass should be given as the <code>"class"</code> 
 * attribute in a <code>view</code> extension contributed to the workbench's
 * view extension point (named <code>"org.eclipse.ui.views"</code>).
 * For example, the plug-in's XML markup might contain:
 * <pre>
 * &LT;extension point="org.eclipse.ui.views"&GT;
 *      &LT;view id="com.example.myplugin.view"
 *         name="My View"
 *         class="com.example.myplugin.MyView"
 *         icon="images/eview.gif"
 *      /&GT;
 * &LT;/extension&GT;
 * </pre>
 * where <code>com.example.myplugin.MyView</code> is the name of the
 * <code>ViewPart</code> subclass.
 * </p>
 * <p>
 * Subclasses must implement the following methods:
 * <ul>
 *   <li><code>createPartControl</code> - to create the view's controls </li>
 *   <li><code>setFocus</code> - to accept focus</li>
 * </ul>
 * </p>
 * <p>
 * Subclasses may extend or reimplement the following methods as required:
 * <ul>
 *   <li><code>setInitializationData</code> - extend to provide additional 
 *       initialization when view extension is instantiated</li>
 *   <li><code>init(IWorkbenchPartSite)</code> - extend to provide additional
 *       initialization when view is assigned its site</li>
 *   <li><code>dispose</code> - extend to provide additional cleanup</li>
 *   <li><code>getAdapter</code> - reimplement to make their view adaptable</li>
 * </ul>
 * </p>
 */
public abstract class ViewPart extends WorkbenchPart implements IViewPart {

private boolean automaticContentDescription = true;
	
/**
 * Creates a new view.
 */
protected ViewPart() {
	super();
}
/* (non-Javadoc)
 * Method declared on IViewPart.
 */
public IViewSite getViewSite() {
	return (IViewSite)getSite();
}
/* (non-Javadoc)
 * Initializes this view at the given view site.
 */
public void init(IViewSite site) throws PartInitException {
	setSite(site);
}
/* (non-Javadoc)
 * Initializes this view with the given view site.  A memento is passed to
 * the view which contains a snapshot of the views state from a previous
 * session.  Where possible, the view should try to recreate that state
 * within the part controls.
 * <p>
 * This implementation will ignore the memento and initialize the view in
 * a fresh state.  Subclasses may override the implementation to perform any
 * state restoration as needed.
 */
public void init(IViewSite site,IMemento memento) throws PartInitException {
	init(site);
}
/* (non-Javadoc)
 * Method declared on IViewPart.
 */
public void saveState(IMemento memento){
    // do nothing
}

/**
 * Sets the content description for this part. The content description is typically
 * a short string describing the current contents of the part. This will be included
 * in the part's title unless the part explicitly calls setTitle to override this
 * behavior.
 * <p>
 * For compatibility with old views, if the view uses setTitle to change the view title
 * and the view never calls setContentDescription then the view title will be used as
 * the content description. 
 * </p>
 * 
 * @param description the content description. New views should not use setTitle, in which
 * case calling this with the empty string indicates that no content description should be
 * used. For old views (that use setTitle), the empty string indicates that the content
 * description should match the view title.
 * 
 * @since 3.0
 */
protected void setContentDescription(String description) {
	Assert.isNotNull(description);
	
	automaticContentDescription = Util.equals(description, "");  //$NON-NLS-1$
	
	if (automaticContentDescription) {
		String title = getTitle();
		
		if (!automaticTitle) { 
			description = title;
		}
	}
	
	super.setContentDescription(description);
}

/* (non-Javadoc)
 * @see org.eclipse.ui.part.WorkbenchPart#setTitle(java.lang.String)
 */
protected void setTitle(String title) {
	title = Util.safeString(title);
	super.setTitle(title);
	
	if (automaticContentDescription) {
		setContentDescription(""); //$NON-NLS-1$
	}
}
}
