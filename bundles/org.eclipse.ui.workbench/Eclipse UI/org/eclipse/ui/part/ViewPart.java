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

import org.eclipse.ui.*;

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
 *         icon="images/cview.gif"
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
}
}
