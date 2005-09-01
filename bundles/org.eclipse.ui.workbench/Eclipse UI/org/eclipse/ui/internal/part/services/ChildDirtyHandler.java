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
package org.eclipse.ui.internal.part.services;

import org.eclipse.ui.internal.components.framework.ComponentException;
import org.eclipse.ui.internal.components.framework.IServiceProvider;
import org.eclipse.ui.internal.part.Part;
import org.eclipse.ui.internal.part.components.services.IDirtyHandler;
import org.eclipse.ui.internal.part.multiplexer.INestedComponent;
import org.eclipse.ui.internal.part.multiplexer.ISharedContext;

/**
 * @since 3.1
 */
public class ChildDirtyHandler implements IDirtyHandler, INestedComponent {

	private IDirtyHandler parent;

	private boolean isActive = false;

	private boolean currentDirty;

	/**
	 * Component constructor. Do not invoke directly.
	 * 
	 * @param shared
	 * @throws ComponentException
	 */
	public ChildDirtyHandler(ISharedContext shared) throws ComponentException {
		IServiceProvider sharedContainer = shared.getSharedComponents();
		this.parent = (IDirtyHandler) sharedContainer
				.getService(IDirtyHandler.class);
	}

	public void setDirty(boolean newDirty) {
		if (newDirty != currentDirty) {
			currentDirty = newDirty;
			if (isActive) {
				parent.setDirty(newDirty);
			}
		}
	}

	public void activate(Part newActivePart) {
		if (isActive) {
			return;
		}

		if (parent != null) {
			parent.setDirty(currentDirty);
		}

		isActive = true;
	}

	public void deactivate(Object newActive) {
		isActive = false;
	}
}
