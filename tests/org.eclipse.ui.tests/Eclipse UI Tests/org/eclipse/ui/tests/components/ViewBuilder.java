/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.components;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.components.framework.ComponentException;
import org.eclipse.ui.internal.components.framework.ServiceFactory;
import org.eclipse.ui.internal.part.Part;

/**
 * @since 3.1
 */
public class ViewBuilder implements IPartBuilder {

    private String viewId;
    private Shell testShell;
    private IWorkbenchPage page; 
    
    public ViewBuilder(String viewId) {
        this.viewId = viewId;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.components.IPartBuilder#createPart()
     */
    public Part createPart(Composite parent, ServiceFactory context, IMemento savedState) throws ComponentException {
        page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        
        return ((WorkbenchPage)page).getPartFactory().createView(viewId, parent, savedState, context);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "View " + viewId;
    }
}
