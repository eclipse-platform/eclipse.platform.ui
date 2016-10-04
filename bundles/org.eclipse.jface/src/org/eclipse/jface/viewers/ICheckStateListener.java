/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 503316
 *******************************************************************************/
package org.eclipse.jface.viewers;

/**
 * A listener which is notified of changes to the checked
 * state of items in checkbox viewers.
 *
 * @see CheckStateChangedEvent
 */
@FunctionalInterface
public interface ICheckStateListener {
    /**
     * Notifies of a change to the checked state of an element.
     *
     * @param event event object describing the change
     */
    void checkStateChanged(CheckStateChangedEvent event);
}
