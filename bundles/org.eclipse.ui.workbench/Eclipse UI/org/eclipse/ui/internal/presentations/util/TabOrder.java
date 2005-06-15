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
package org.eclipse.ui.internal.presentations.util;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IPresentationSerializer;

/**
 * @since 3.0
 */
public abstract class TabOrder {
    /**
     * Adds a part due to a user action that opened a part
     * 
     * @param newPart part being added 
     */
    public abstract void add(IPresentablePart newPart);

    /**
     * Adds a part at initialization-time (the part was added as
     * part of a perspective, rather than by a user action)
     * 
     * @param newPart the part being added
     */
    public abstract void addInitial(IPresentablePart newPart);

    public abstract void restoreState(IPresentationSerializer serializer,
            IMemento savedState);
    
    public abstract void saveState(IPresentationSerializer serializer, IMemento memento);
    
    /**
     * Adds a part at a particular index due to a drag/drop operation. 
     * 
     * @param added part being added
     * @param index index where the part is added at
     */
    public abstract void insert(IPresentablePart added, int index);

    public abstract void move(IPresentablePart toMove, int newIndex);
    
    /**
     * Removes a part
     * 
     * @param removed part being removed
     */
    public abstract void remove(IPresentablePart removed);

    /**
     * Selects a part
     * 
     * @param selected part being selected
     */
    public abstract void select(IPresentablePart selected);
    
    public abstract IPresentablePart[] getPartList();
}
