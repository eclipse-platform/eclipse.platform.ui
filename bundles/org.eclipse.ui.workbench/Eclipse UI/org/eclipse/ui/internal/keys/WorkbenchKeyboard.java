/*******************************************************************************
 * Copyright (c) 2011, 2014 IBM Corporation and others.
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

package org.eclipse.ui.internal.keys;

import java.util.List;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.bindings.keys.KeyBindingDispatcher;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * @since 3.5
 *
 */
public class WorkbenchKeyboard {
	private KeyBindingDispatcher delegate;

	static class KeyDownFilter implements Listener {
		private KeyBindingDispatcher.KeyDownFilter delegate;

		/**
		 * @param event
		 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
		 */
		@Override
		public void handleEvent(Event event) {
			delegate.handleEvent(event);
		}

		@Override
		public String toString() {
			return delegate.toString();
		}

		public KeyDownFilter(KeyBindingDispatcher.KeyDownFilter filter) {
			this.delegate = filter;
		}

	}

	@Override
	public boolean equals(Object o) {
		return delegate.equals(o);
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	/**
	 * @return
	 * @see org.eclipse.e4.ui.bindings.keys.KeyBindingDispatcher#getKeyDownFilter()
	 */
	public KeyDownFilter getKeyDownFilter() {
		return new KeyDownFilter(delegate.getKeyDownFilter());
	}

	/**
	 * @param potentialKeyStrokes
	 * @param event
	 * @return
	 * @see org.eclipse.e4.ui.bindings.keys.KeyBindingDispatcher#press(java.util.List,
	 *      org.eclipse.swt.widgets.Event)
	 */
	public boolean press(List potentialKeyStrokes, Event event) {
		return delegate.press(potentialKeyStrokes, event);
	}

	/**
	 * @param context
	 * @see org.eclipse.e4.ui.bindings.keys.KeyBindingDispatcher#setContext(org.eclipse.e4.core.contexts.IEclipseContext)
	 */
	public void setContext(IEclipseContext context) {
		delegate.setContext(context);
	}

	public WorkbenchKeyboard(KeyBindingDispatcher kbd) {
		delegate = kbd;
	}

	/**
	 * @param ctrlShiftT
	 * @return
	 */
	public static List generatePossibleKeyStrokes(Event event) {
		return KeyBindingDispatcher.generatePossibleKeyStrokes(event);
	}
}
