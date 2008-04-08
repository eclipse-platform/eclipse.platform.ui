/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.part;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * A pagebook is a composite control where only a single control is visible
 * at a time. It is similar to a notebook, but without tabs.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * Note that although this class is a subclass of <code>Composite</code>,
 * it does not make sense to set a layout on it.
 * </p>
 *
 * @see PageBookView
 * @noextend This class is not intended to be subclassed by clients.
 */
public class PageBook extends Composite {

    /**
     * <p>
     * [Issue: This class should be declared private.]
     * </p>
     */
    public class PageBookLayout extends Layout {

        protected Point computeSize(Composite composite, int wHint, int hHint,
                boolean flushCache) {
            if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT) {
				return new Point(wHint, hHint);
			}

            Point result = null;
            if (currentPage != null) {
                result = currentPage.computeSize(wHint, hHint, flushCache);
            } else {
                //Rectangle rect= composite.getClientArea();
                //result= new Point(rect.width, rect.height);
                result = new Point(0, 0);
            }
            if (wHint != SWT.DEFAULT) {
				result.x = wHint;
			}
            if (hHint != SWT.DEFAULT) {
				result.y = hHint;
			}
            return result;
        }

        protected void layout(Composite composite, boolean flushCache) {
            if (currentPage != null) {
                currentPage.setBounds(composite.getClientArea());
            }
        }
    }

    /**
     * The current control; <code>null</code> if none.
     */
    private Control currentPage = null;

    /**
     * Creates a new empty pagebook.
     *
     * @param parent the parent composite
     * @param style the SWT style bits
     */
    public PageBook(Composite parent, int style) {
        super(parent, style);
        setLayout(new PageBookLayout());
    }

    /**
     * Shows the given page. This method has no effect if the given page is not
     * contained in this pagebook.
     *
     * @param page the page to show
     */
    public void showPage(Control page) {

        if (page == currentPage) {
			return;
		}
        if (page.getParent() != this) {
			return;
		}

        Control oldPage = currentPage;
        currentPage = page;

        // show new page
        if (page != null) {
            if (!page.isDisposed()) {
                page.setVisible(true);
                layout(true);
                //				if (fRequestFocusOnShowPage)
                //					page.setFocus();
            }
        }

        // hide old *after* new page has been made visible in order to avoid flashing
        if (oldPage != null && !oldPage.isDisposed()) {
			oldPage.setVisible(false);
		}
    }
}
