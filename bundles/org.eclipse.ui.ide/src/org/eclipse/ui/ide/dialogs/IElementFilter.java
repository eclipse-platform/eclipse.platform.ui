/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.ide.dialogs;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * The {@link IElementFilter} is a interface that defines the API for filtering the current selection of 
 * a {@link ResourceTreeAndListGroup} in order to find a subset to update as the result of a type filtering.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @since 3.10
 */
public interface IElementFilter {

    /**
     * Callback to filter the given collection of elements
     * 
     * @param elements the raw list of elements to filter
     * @param monitor the progress monitor
     * @throws InterruptedException thrown if the monitor is cancelled
     */
    public void filterElements(Collection elements, IProgressMonitor monitor) throws InterruptedException;

    /**
     * Callback to filter the given array of objects
     * 
     * @param elements the raw array of elements to filter
     * @param monitor the progress monitor
     * @throws InterruptedException thrown if the monitor is cancelled
     */
    public void filterElements(Object[] elements, IProgressMonitor monitor) throws InterruptedException;

}
