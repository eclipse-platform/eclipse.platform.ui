/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 475860
 *******************************************************************************/
package org.eclipse.jface.viewers;

/**
 * A listener which is notified when a viewer's selection changes.
 *
 * @see ISelection
 * @see ISelectionProvider
 * @see SelectionChangedEvent
 */
@FunctionalInterface
public interface ISelectionChangedListener {
    /**
     * Notifies that the selection has changed.
     *
     * @param event event object describing the change
     */
    public void selectionChanged(SelectionChangedEvent event);
}
