/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.examples.rcp.browser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Plug-in class for the browser example.
 */
public class BrowserPlugin extends AbstractUIPlugin {
    private static BrowserPlugin DEFAULT;
    
    public BrowserPlugin() {
        DEFAULT = this;
    }

    public static BrowserPlugin getDefault() {
        return DEFAULT;
    }

    /**
     * Logs the given throwable.
     * 
     * @param t the throwable to log
     */
    public void log(Throwable t) {
        String msg = t.getMessage();
        if (msg == null)
            msg = t.toString();
        IStatus status = new Status(IStatus.ERROR, getBundle().getSymbolicName(), 0, msg, t);
        getLog().log(status);
    }
    
    /**
     * Returns a list of all views and editors in the given page,
     * excluding any secondary views like the History view.
     * 
     * @param page the workbench page
     * @return a list of all non-secondary parts in the page
     */
    public static List getNonSecondaryParts(IWorkbenchPage page) {
        ArrayList list = new ArrayList();
        list.addAll(Arrays.asList(page.getViewReferences()));
        list.addAll(Arrays.asList(page.getEditorReferences()));
        for (Iterator i = list.iterator(); i.hasNext();) {
            IWorkbenchPartReference ref = (IWorkbenchPartReference) i.next();
            if (ref instanceof ISecondaryPart) {
                i.remove();
            }
        }
        return list;
    }
    
}
