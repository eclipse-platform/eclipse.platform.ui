/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.core.subscribers;

import org.eclipse.team.core.diff.IDiffChangeListener;
import org.eclipse.team.core.mapping.IResourceDiffTree;

/**
 * A change set collector for change sets whose underlying representation is a diff tree
 */
public abstract class DiffTreeChangeSetCollector extends ChangeSetCollector {

    /**
     * Return the Change Set whose sync info set is the
     * one given.
     * @param tree a diff tree
     * @return the change set for the given diff tree
     */
    protected ChangeSet getChangeSet(IResourceDiffTree tree) {
        ChangeSet[] sets = getSets();
        for (int i = 0; i < sets.length; i++) {
			ChangeSet changeSet = sets[i];
            if (((DiffChangeSet)changeSet).getDiffTree() == tree) {
                return changeSet;
            }
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.internal.core.subscribers.AbstractChangeSetCollector#handleSetAdded(org.eclipse.team.internal.core.subscribers.ChangeSet)
     */
    protected void handleSetAdded(ChangeSet set) {
    	((DiffChangeSet)set).getDiffTree().addDiffChangeListener(getDiffTreeListener());
    	super.handleSetAdded(set);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.internal.core.subscribers.AbstractChangeSetCollector#handleSetRemoved(org.eclipse.team.internal.core.subscribers.ChangeSet)
     */
    protected void handleSetRemoved(ChangeSet set) {
    	((DiffChangeSet)set).getDiffTree().removeDiffChangeListener(getDiffTreeListener());
    	super.handleSetRemoved(set);
    }
    
    protected abstract IDiffChangeListener getDiffTreeListener();
    
}
