/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.subscriber;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.util.IPropertyChangeListener;

/**
 * Interface for registering commit set change listeners with
 * the commit set manager.
 */
public interface ICommitSetChangeListener extends IPropertyChangeListener {

    /**
     * The given set has been added to the set manager.
     * @param set the added set
     */
    void setAdded(CommitSet set);
    
    /**
     * The given set has been removed from the set manager.
     * @param set the removed set
     */
    void setRemoved(CommitSet set);
    
    /**
     * The title of the given set has changed.
     * @param set the set whose title changed
     */
    void titleChanged(CommitSet set);

    /**
     * The state of the given files have change with respect to the
     * given set. This means that the files have either been added 
     * or removed from the set. Callers can use the files contained
     * in the set to determine if each file is an addition or removal.
     * @param set the commit set that has changed
     * @param files the files whose containment state has changed w.r.t the set
     */
    void filesChanged(CommitSet set, IFile[] files);

}
