/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.layout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * This class wraps a control with a complex computeSize method. It uses caching
 * to reduce the number of times the control's computeSize method is called. This
 * allows controls (such as Coolbars and wrapping text) with slow computeSize
 * operations to be used inside layouts and composites that use inefficient caching.
 * <p>
 * For example, you want to use Coolbar A inside composite B. Rather than making A
 * a direct child of B, place it inside a CacheWrapper and insert the CacheWrapper
 * into B. Any layout data that would normally be attached to the control itself
 * should be attached to the wrapper instead:
 * </p>
 * <code>
 * 
 *   // Unoptimized code
 *   Toolbar myToolbar = new Toolbar(someParent, SWT.WRAP);
 *   myToolbar.setLayoutData(someLayoutData);
 * </code>
 * <code>
 * 
 *   // Optimized code
 *   CacheWrapper myWrapper = new CacheWrapper(someParent);
 *   Toolbar myToolbar = new Toolbar(myWrapper.getControl(), SWT.WRAP);
 *   myWrapper.getControl().setLayoutData(someLayoutData);
 * </code>
 * <p>
 * CacheWrapper creates a Composite which should have exactly one child: the control
 * whose size should be cached. Note that CacheWrapper does NOT respect the flushCache
 * argument to layout() and computeSize(). This is intentional, since the whole point of
 * this class is to workaround layouts with poor caching, and such layouts will typically
 * be too eager about flushing the caches of their children. However, this means that you
 * MUST manually call flushCache() whenver the child's preferred size changes (and before
 * the parent is layed out).  
 * </p>
 * 
 * @since 3.0
 */
public class CacheWrapper {
    private Composite proxy;

    private SizeCache cache = new SizeCache();

    private Rectangle lastBounds = new Rectangle(0, 0, 0, 0);

    private class WrapperLayout extends Layout implements ICachingLayout {
        protected Point computeSize(Composite composite, int wHint, int hHint,
                boolean flushCache) {
            Control[] children = composite.getChildren();
            if (children.length != 1) {
                return new Point(0, 0);
            }

            cache.setControl(children[0]);

            return cache.computeSize(wHint, hHint);
        }

        protected void layout(Composite composite, boolean flushCache) {
            Control[] children = composite.getChildren();
            if (children.length != 1) {
                return;
            }

            Control child = children[0];
            Rectangle newBounds = composite.getClientArea();
            if (!newBounds.equals(lastBounds)) {
                child.setBounds(newBounds);
                lastBounds = newBounds;
            }

        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.internal.layout.ICachingLayout#flush(org.eclipse.swt.widgets.Control)
         */
        public void flush(Control dirtyControl) {
            CacheWrapper.this.flushCache();
        }
    }

    /**
     * Creates a <code>CacheWrapper</code> with the given parent
     * 
     * @param parent
     */
    public CacheWrapper(Composite parent) {
        proxy = new Composite(parent, SWT.NONE);

        proxy.setLayout(new WrapperLayout());
    }

    /**
     * Flush the cache. Call this when the child has changed in order to force
     * the size to be recomputed in the next resize event.
     */
    public void flushCache() {
        cache.flush();
    }

    /**
     * Use this as the parent of the real control.
     * 
     * @return the proxy contol. It should be given exactly one child.
     */
    public Composite getControl() {
        return proxy;
    }

    /**
     * Dispose of any widgets created by this wrapper.
     */
    public void dispose() {
        if (proxy != null) {
            proxy.dispose();
            proxy = null;
        }
    }
}
