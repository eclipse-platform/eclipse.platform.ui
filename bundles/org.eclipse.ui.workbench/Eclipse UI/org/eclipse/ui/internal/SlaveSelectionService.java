/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.services.IDisposable;

/**
 * @since 3.4
 *
 */
public class SlaveSelectionService implements ISelectionService, IDisposable {

	private ListenerList<ISelectionListener> postListeners = new ListenerList<>(ListenerList.IDENTITY);
	private ListenerList<ISelectionListener> listeners = new ListenerList<>(ListenerList.IDENTITY);
	private Map<ISelectionListener, String> listenersToPartId = new HashMap<>();
	private Map<ISelectionListener, String> postListenersToPartId = new HashMap<>();

	private ISelectionService parentSelectionService;

	/**
	 * @param parentSelectionService
	 */
	public SlaveSelectionService(ISelectionService parentSelectionService) {
		if (parentSelectionService == null) {
			throw new IllegalArgumentException(
					"The parent selection service cannot be null"); //$NON-NLS-1$
		}
		this.parentSelectionService = parentSelectionService;
	}

	@Override
	public void addPostSelectionListener(ISelectionListener listener) {
		postListeners.add(listener);
		parentSelectionService.addPostSelectionListener(listener);
	}

	@Override
	public void addPostSelectionListener(String partId,
			ISelectionListener listener) {
		listenersToPartId.put(listener, partId);
		parentSelectionService.addPostSelectionListener(partId, listener);
	}

	@Override
	public void addSelectionListener(ISelectionListener listener) {
		listeners.add(listener);
		parentSelectionService.addSelectionListener(listener);
	}

	@Override
	public void addSelectionListener(String partId, ISelectionListener listener) {
		postListenersToPartId.put(listener, partId);
		parentSelectionService.addPostSelectionListener(partId, listener);
	}

	@Override
	public ISelection getSelection() {
		return parentSelectionService.getSelection();
	}

	@Override
	public ISelection getSelection(String partId) {
		return parentSelectionService.getSelection(partId);
	}

	@Override
	public void removePostSelectionListener(ISelectionListener listener) {
		postListeners.remove(listener);
		parentSelectionService.removePostSelectionListener(listener);
	}

	@Override
	public void removePostSelectionListener(String partId,
			ISelectionListener listener) {
		postListenersToPartId.remove(listener);
		parentSelectionService.removePostSelectionListener(partId, listener);
	}

	@Override
	public void removeSelectionListener(ISelectionListener listener) {
		listeners.remove(listener);
		parentSelectionService.removeSelectionListener(listener);
	}

	@Override
	public void removeSelectionListener(String partId,
			ISelectionListener listener) {
		listenersToPartId.remove(listener);
		parentSelectionService.removeSelectionListener(partId, listener);
	}

	@Override
	public void dispose() {
		for (Object listener : listeners.getListeners()) {
			parentSelectionService.removeSelectionListener((ISelectionListener) listener);
		}
		listeners.clear();

		for (Object listener : postListeners.getListeners()) {
			parentSelectionService.removePostSelectionListener((ISelectionListener) listener);
		}
		postListeners.clear();

		for (Entry<ISelectionListener, String> entry : listenersToPartId.entrySet()) {
			parentSelectionService.removeSelectionListener(entry.getValue(), entry.getKey());
		}
		listenersToPartId.clear();

		for (Entry<ISelectionListener, String> entry : postListenersToPartId.entrySet()) {
			parentSelectionService.removePostSelectionListener(entry.getValue(), entry.getKey());
		}
		postListenersToPartId.clear();
	}
}
