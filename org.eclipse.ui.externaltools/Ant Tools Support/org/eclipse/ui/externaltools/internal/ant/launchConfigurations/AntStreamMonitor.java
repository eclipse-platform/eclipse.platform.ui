package org.eclipse.ui.externaltools.internal.ant.launchConfigurations;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.internal.core.ListenerList;

/**
 *
 */
public class AntStreamMonitor implements IStreamMonitor {

	private StringBuffer fContents = new StringBuffer();
	private ListenerList fListeners = new ListenerList(1);
	/**
	 * @see org.eclipse.debug.core.model.IStreamMonitor#addListener(org.eclipse.debug.core.IStreamListener)
	 */
	public void addListener(IStreamListener listener) {
		fListeners.add(listener);
	}

	/**
	 * @see org.eclipse.debug.core.model.IStreamMonitor#getContents()
	 */
	public String getContents() {
		return fContents.toString();
	}

	/**
	 * @see org.eclipse.debug.core.model.IStreamMonitor#removeListener(org.eclipse.debug.core.IStreamListener)
	 */
	public void removeListener(IStreamListener listener) {
		fListeners.remove(listener);
	}

	/**
	 * Appends the given message to this stream, and notifies listeners.
	 * 
	 * @param message
	 */
	public void append(String message) {
		fContents.append(message);
		Object[] listeners = fListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			IStreamListener listener = (IStreamListener)listeners[i];
			listener.streamAppended(message, this);
		}
	}
}

