/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
	 * @param parentPartService The parent part service for this slave. Never
	 *                          <code>null</code>.
	 */
	public SlavePartService(IPartService parentPartService) {
		if (parentPartService == null) {
			throw new IllegalArgumentException("The parent part service cannot be null"); //$NON-NLS-1$
		}
		this.parent = parentPartService;
	}

	@Override
	public void addPartListener(IPartListener listener) {
		listeners.add(listener);
		parent.addPartListener(listener);
	}

	@Override
	public void addPartListener(IPartListener2 listener) {
		listeners.add(listener);
		parent.addPartListener(listener);
	}

	@Override
	public IWorkbenchPart getActivePart() {
		return parent.getActivePart();
	}

	@Override
	public IWorkbenchPartReference getActivePartReference() {
		return parent.getActivePartReference();
	}

	@Override
	public void removePartListener(IPartListener listener) {
		listeners.remove(listener);
		parent.removePartListener(listener);
	}

	@Override
	public void removePartListener(IPartListener2 listener) {
		listeners.remove(listener);
		parent.removePartListener(listener);
	}

	@Override
	public void dispose() {
		for (Object listener : listeners.getListeners()) {
			if (listener instanceof IPartListener) {
				parent.removePartListener((IPartListener) listener);
			}
			if (listener instanceof IPartListener2) {
				parent.removePartListener((IPartListener2) listener);
			}
		}
		listeners.clear();
	}

}
