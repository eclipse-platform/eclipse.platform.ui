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

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.services.IDisposable;

/**
 * A part service which delegates all responsibility to the parent service. The
 * slave service is only responsible for disposing any locally activated
 * listeners when it is disposed.
 * <p>
 * This class is not intended for use outside of the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 * 
 * @since 3.4
 * 
 */
public class SlavePartService implements IPartService, IDisposable {

	/**
	 * The parent part service to which all listeners are routed. This value is
	 * never <code>null</code>.
	 */
	private IPartService parent;

	private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

	/**
	 * Constructs a new instance.
	 * 
	 * @param parentPartService
	 *            The parent part service for this slave. Never
	 *            <code>null</code>.
	 */
	public SlavePartService(IPartService parentPartService) {
		if (parentPartService == null) {
			throw new IllegalArgumentException(
					"The parent part service cannot be null"); //$NON-NLS-1$
		}
		this.parent = parentPartService;
	}

	public void addPartListener(IPartListener listener) {
		listeners.add(listener);
		parent.addPartListener(listener);
	}

	public void addPartListener(IPartListener2 listener) {
		listeners.add(listener);
		parent.addPartListener(listener);
	}

	public IWorkbenchPart getActivePart() {
		return parent.getActivePart();
	}

	public IWorkbenchPartReference getActivePartReference() {
		return parent.getActivePartReference();
	}

	public void removePartListener(IPartListener listener) {
		listeners.remove(listener);
		parent.removePartListener(listener);
	}

	public void removePartListener(IPartListener2 listener) {
		listeners.remove(listener);
		parent.removePartListener(listener);
	}

	public void dispose() {
		Object list[] = listeners.getListeners();
		for (int i = 0; i < list.length; i++) {
			Object obj = list[i];
			if (obj instanceof IPartListener) {
				parent.removePartListener((IPartListener) obj);
			}
			if (obj instanceof IPartListener2) {
				parent.removePartListener((IPartListener2) obj);
			}
		}
		listeners.clear();
	}

}
