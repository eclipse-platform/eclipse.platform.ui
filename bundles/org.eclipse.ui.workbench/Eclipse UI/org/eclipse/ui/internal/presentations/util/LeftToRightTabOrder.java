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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IPresentationSerializer;

/**
 * @since 3.0
 */
public class LeftToRightTabOrder extends TabOrder {

    private IPresentablePartList list;
    
    public LeftToRightTabOrder(IPresentablePartList list) {
        this.list = list;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.TabOrder#add(org.eclipse.ui.presentations.IPresentablePart)
     */
    public void add(IPresentablePart newPart) {
        list.insert(newPart, list.size());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.TabOrder#addInitial(org.eclipse.ui.presentations.IPresentablePart)
     */
    public void addInitial(IPresentablePart newPart) {
        add(newPart);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.TabOrder#insert(org.eclipse.ui.presentations.IPresentablePart, int)
     */
    public void insert(IPresentablePart newPart, int index) {
        list.insert(newPart, index);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.TabOrder#remove(org.eclipse.ui.presentations.IPresentablePart)
     */
    public void remove(IPresentablePart removed) {
        list.remove(removed);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.TabOrder#select(org.eclipse.ui.presentations.IPresentablePart)
     */
    public void select(IPresentablePart selected) {
        list.select(selected);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.TabOrder#move(org.eclipse.ui.presentations.IPresentablePart, int)
     */
    public void move(IPresentablePart toMove, int newIndex) {
        list.move(toMove, newIndex);
    }
    
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.TabOrder#getPartList()
     */
    public IPresentablePart[] getPartList() {
        return list.getPartList();
    }
    
    /**
     * Restores a presentation from a previously stored state
     * 
     * @param serializer (not null)
     * @param savedState (not null)
     */
    public void restoreState(IPresentationSerializer serializer,
            IMemento savedState) {
        IMemento[] parts = savedState.getChildren(IWorkbenchConstants.TAG_PART);

        for (int idx = 0; idx < parts.length; idx++) {
            String id = parts[idx].getString(IWorkbenchConstants.TAG_ID);

            if (id != null) {
                IPresentablePart part = serializer.getPart(id);

                if (part != null) {
                    addInitial(part);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.StackPresentation#saveState(org.eclipse.ui.presentations.IPresentationSerializer, org.eclipse.ui.IMemento)
     */
    public void saveState(IPresentationSerializer context, IMemento memento) {

        List parts = Arrays.asList(list.getPartList());

        Iterator iter = parts.iterator();
        while (iter.hasNext()) {
            IPresentablePart next = (IPresentablePart) iter.next();

            IMemento childMem = memento
                    .createChild(IWorkbenchConstants.TAG_PART);
            childMem.putString(IWorkbenchConstants.TAG_ID, context.getId(next));
        }
    }
}
