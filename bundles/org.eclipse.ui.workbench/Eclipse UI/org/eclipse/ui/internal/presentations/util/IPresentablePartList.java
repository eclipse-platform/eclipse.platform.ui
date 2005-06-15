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

import org.eclipse.ui.presentations.IPresentablePart;

/**
 * @since 3.0
 */
public interface IPresentablePartList {
    public void insert(IPresentablePart part, int idx);

    public void remove(IPresentablePart part);

    public void move(IPresentablePart part, int newIndex);

    public int size();

    public void select(IPresentablePart part);
    
    public IPresentablePart[] getPartList();
}
