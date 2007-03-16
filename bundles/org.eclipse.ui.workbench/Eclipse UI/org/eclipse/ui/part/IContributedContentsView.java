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

import org.eclipse.ui.IWorkbenchPart;

/**
 * This interface is used to identify workbench views which
 * allow other parts (typically the active part) to supply
 * their contents.
 * The interface allows access to the part which contributed the current
 * contents.
 */
public interface IContributedContentsView {
    /**
     * Returns the workbench part which contributed the
     * current contents of this view.
     *
     * @return the part which contributed the current contents
     */
    public IWorkbenchPart getContributingPart();
}
