/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations.newapi;

import org.eclipse.ui.presentations.IPresentablePart;

/**
 * @since 3.0
 */
public class LeftToRightTabOrder extends TabOrder {
	
	public LeftToRightTabOrder() {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.presentations.newapi.TabOrder#add(org.eclipse.ui.presentations.IPresentablePart)
	 */
	public void add(IPresentablePart newPart, IPresentablePartList list) {
		list.insert(newPart, list.size());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.presentations.newapi.TabOrder#addInitial(org.eclipse.ui.presentations.IPresentablePart)
	 */
	public void addInitial(IPresentablePart newPart, IPresentablePartList list) {
		add(newPart, list);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.presentations.newapi.TabOrder#insert(org.eclipse.ui.presentations.IPresentablePart, int)
	 */
	public void insert(IPresentablePart newPart, int index, IPresentablePartList list) {
		list.insert(newPart, index);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.presentations.newapi.TabOrder#remove(org.eclipse.ui.presentations.IPresentablePart)
	 */
	public void remove(IPresentablePart removed, IPresentablePartList list) {
		list.remove(removed);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.presentations.newapi.TabOrder#select(org.eclipse.ui.presentations.IPresentablePart)
	 */
	public void select(IPresentablePart selected, IPresentablePartList list) {
		list.select(selected);
	}

}
