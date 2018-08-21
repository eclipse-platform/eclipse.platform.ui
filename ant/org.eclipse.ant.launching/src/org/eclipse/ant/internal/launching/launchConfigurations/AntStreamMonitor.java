/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ant.internal.launching.launchConfigurations;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IFlushableStreamMonitor;

/**
 * Stream monitor implementation for an Ant build process.
 */
public class AntStreamMonitor implements IFlushableStreamMonitor {

	private StringBuffer fContents = new StringBuffer();
	private ListenerList<IStreamListener> fListeners = new ListenerList<>(1);
	private boolean fBuffered = true;

	/**
	 * @see org.eclipse.debug.core.model.IStreamMonitor#addListener(org.eclipse.debug.core.IStreamListener)
	 */
	@Override
	public void addListener(IStreamListener listener) {
		fListeners.add(listener);
	}

	/**
	 * @see org.eclipse.debug.core.model.IStreamMonitor#getContents()
	 */
	@Override
	public String getContents() {
		return fContents.toString();
	}

	/**
	 * @see org.eclipse.debug.core.model.IStreamMonitor#removeListener(org.eclipse.debug.core.IStreamListener)
	 */
	@Override
	public void removeListener(IStreamListener listener) {
		fListeners.remove(listener);
	}

	/**
	 * Appends the given message to this stream, and notifies listeners.
	 * 
	 * @param message
	 */
	public void append(String message) {
		if (isBuffered()) {
			fContents.append(message);
		}
		for (IStreamListener iStreamListener : fListeners) {
			iStreamListener.streamAppended(message, this);
		}
	}

	/**
	 * @see org.eclipse.debug.core.model.IFlushableStreamMonitor#flushContents()
	 */
	@Override
	public void flushContents() {
		fContents.setLength(0);
	}

	/**
	 * @see org.eclipse.debug.core.model.IFlushableStreamMonitor#isBuffered()
	 */
	@Override
	public boolean isBuffered() {
		return fBuffered;
	}

	/**
	 * @see org.eclipse.debug.core.model.IFlushableStreamMonitor#setBuffered(boolean)
	 */
	@Override
	public void setBuffered(boolean buffer) {
		fBuffered = buffer;
	}
}
