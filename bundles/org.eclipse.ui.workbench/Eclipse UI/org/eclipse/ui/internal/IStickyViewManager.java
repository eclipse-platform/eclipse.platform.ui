/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.util.Set;

import org.eclipse.ui.IMemento;

/**
 * @since 3.3
 *
 */
interface IStickyViewManager {
	
	void remove(String perspectiveId);
	
	void add(String perspectiveId, Set stickyViewSet);
	
	void clear();
	
	void update(Perspective oldPersp, Perspective newPersp);
	
	void save(IMemento memento);
	 
	void restore(IMemento memento);

}
