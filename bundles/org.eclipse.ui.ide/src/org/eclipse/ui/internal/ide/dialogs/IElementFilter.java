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
package org.eclipse.ui.internal.ide.dialogs;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * The IElementFilter is a interface that defines 
 * the api for filtering the current selection of 
 * a ResourceTreeAndListGroup in order to find a 
 * subset to update as the result of a type filtering.
 * This is meant as an internal class and is used exlcusively
 * by the import dialog.
 */

public interface IElementFilter {

    public void filterElements(Collection elements, IProgressMonitor monitor)
            throws InterruptedException;

    public void filterElements(Object[] elements, IProgressMonitor monitor)
            throws InterruptedException;

}
