/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.commands.operations.ObjectUndoContext;

/**
 * @since 3.1
 */
public class WorkbenchUndoContext extends ObjectUndoContext {
    
    private String fLabel = ""; //$NON-NLS-1$
    
    /**
     * Create an operation context for the workbench using the specified label.
     * @param label - the label for the context
     */
    public WorkbenchUndoContext(String label) {
        super(Workbench.getInstance());
        fLabel = label;
    }

}
