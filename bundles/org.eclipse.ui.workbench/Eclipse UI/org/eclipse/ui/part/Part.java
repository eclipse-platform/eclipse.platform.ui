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
package org.eclipse.ui.part;

import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.components.IServiceProvider;
import org.eclipse.ui.part.interfaces.IPersistable;

/**
 * A part is a high-level object that manages an SWT control. The lifecycle
 * of a part matches that of its control. The part creates its
 * control in its constructor, and it performs any cleanup by hooking
 * a dispose listener on the control.
 * 
 * <p>
 * Not intended to be subclassed by clients. 
 * </p>
 * 
 * @since 3.1
 */
public abstract class Part implements IPersistable, IServiceProvider 
{
    
    /**
     * Returns the part's control. The owner of a part may attach
     * layout data to this control, and may give the part focus by
     * calling getControl().setFocus().
     *
     * @return the part's control
     */
    public abstract Control getControl();

}
