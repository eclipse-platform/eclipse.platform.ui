/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.debug.model;

import org.eclipse.ant.internal.ui.debug.IAntDebugConstants;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

public class AntValue extends AntDebugElement implements IValue  {

    private String fValueString;
    
    /**
     * @param target
     */
    public AntValue(AntDebugTarget target, String value) {
        super(target);
        fValueString= value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IValue#getReferenceTypeName()
     */
    public String getReferenceTypeName() {
        return ""; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IValue#getValueString()
     */
    public String getValueString() {
        return fValueString;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IValue#isAllocated()
     */
    public boolean isAllocated() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IValue#getVariables()
     */
    public IVariable[] getVariables() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IValue#hasVariables()
     */
    public boolean hasVariables() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IDebugElement#getModelIdentifier()
     */
    public String getModelIdentifier() {
        return IAntDebugConstants.ID_ANT_DEBUG_MODEL;
    }
}