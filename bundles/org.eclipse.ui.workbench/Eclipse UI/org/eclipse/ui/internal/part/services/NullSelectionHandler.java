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
package org.eclipse.ui.internal.part.services;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.internal.part.components.services.ISelectionHandler;

/**
 * @since 3.1
 */
public class NullSelectionHandler implements ISelectionHandler {

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.components.interfaces.ISelectionHandler#setSelection(org.eclipse.jface.viewers.ISelection)
     */
    public void setSelection(ISelection newSelection) {

    }

}
