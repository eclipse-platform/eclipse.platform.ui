/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.part;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchPartSite;
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

    /**
     * Listens to PROP_TITLE property changes in this object until the first call to
     * setContentDescription. Used for compatibility with old parts that call setTitle
     * or overload getTitle instead of using setContentDescription. 
     */
    private IPropertyListener compatibilityTitleListener = new IPropertyListener() {
        /* (non-Javadoc)
         * @see org.eclipse.ui.IPropertyListener#propertyChanged(java.lang.Object, int)
         */
        public void propertyChanged(Object source, int propId) {
            if (propId == IWorkbenchPartConstants.PROP_TITLE) {
                setDefaultContentDescription();
            }
        }
    };

    /**
     * Creates a new view.
     */
    protected ViewPart() {
        super();

        addPropertyListener(compatibilityTitleListener);
    }

    /* (non-Javadoc)
     * Method declared on IViewPart.
     */
    public IViewSite getViewSite() {
        return (IViewSite) getSite();
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite)
     */
    public void init(IViewSite site) throws PartInitException {
        setSite(site);

        setDefaultContentDescription();
    }

    /* 
     * (non-Javadoc)
     * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
     */
    public void init(IViewSite site, IMemento memento) throws PartInitException {
    	/*
    	* Initializes this view with the given view site.  A memento is passed to
        * the view which contains a snapshot of the views state from a previous
        * session.  Where possible, the view should try to recreate that state
        * within the part controls.
        * <p>
        * This implementation will ignore the memento and initialize the view in
        * a fresh state.  Subclasses may override the implementation to perform any
        * state restoration as needed.
        */
        init(site);
    }

  
    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewPart#saveState(org.eclipse.ui.IMemento)
     */
    public void saveState(IMemento memento) {
        // do nothing
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setPartName(java.lang.String)
     */
    protected void setPartName(String partName) {
        if (compatibilityTitleListener != null) {
            removePropertyListener(compatibilityTitleListener);
            compatibilityTitleListener = null;
        }

        super.setPartName(partName);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setContentDescription(java.lang.String)
     */
    protected void setContentDescription(String description) {
        if (compatibilityTitleListener != null) {
            removePropertyListener(compatibilityTitleListener);
            compatibilityTitleListener = null;
        }

        super.setContentDescription(description);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    public void setInitializationData(IConfigurationElement cfig,
            String propertyName, Object data) {
        super.setInitializationData(cfig, propertyName, data);

        setDefaultContentDescription();
    }

    private void setDefaultContentDescription() {
        if (compatibilityTitleListener == null) {
            return;
        }

        String partName = getPartName();
        String title = getTitle();

        if (Util.equals(partName, title)) {
            internalSetContentDescription(""); //$NON-NLS-1$
        } else {
            internalSetContentDescription(title);
        }
    }

    /**
     * Checks that the given site is valid for this type of part.
     * The site for a view must be an <code>IViewSite</code>.
     * 
     * @param site the site to check
     * @since 3.1
     */
    protected final void checkSite(IWorkbenchPartSite site) {
        super.checkSite(site);
        Assert.isTrue(site instanceof IViewSite, "The site for a view must be an IViewSite"); //$NON-NLS-1$
    }    
}
