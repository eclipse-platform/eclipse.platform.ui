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

package org.eclipse.ui.internal.menus;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.swt.IFocusService;

/**
 * @since 3.3
 * 
 */
public class FocusControlSourceProvider extends AbstractSourceProvider
		implements IFocusService {

	/**
	 * The names of the sources supported by this source provider.
	 */
	private static final String[] PROVIDED_SOURCE_NAMES = new String[] {
			ISources.ACTIVE_FOCUS_CONTROL_ID_NAME,
			ISources.ACTIVE_FOCUS_CONTROL_NAME };

	Map controlToId = new HashMap();
	private FocusListener focusListener;

	private String currentId;

	private Control currentControl;

	private DisposeListener disposeListener;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.menus.IFocusService#addTrackerFor(org.eclipse.swt.widgets.Control,
	 *      java.lang.String)
	 */
	public void addFocusTracker(Control control, String id) {
		if (control.isDisposed()) {
			return;
		}
		controlToId.put(control, id);
		control.addFocusListener(getFocusListener());
		control.addDisposeListener(getDisposeListener());
	}

	/**
	 * @return
	 */
	private DisposeListener getDisposeListener() {
		if (disposeListener == null) {
			disposeListener = new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					controlToId.remove(e.widget);
				}
			};
		}
		return disposeListener;
	}

	/**
	 * @return
	 */
	private FocusListener getFocusListener() {
		if (focusListener == null) {
			focusListener = new FocusListener() {
				public void focusGained(FocusEvent e) {
					focusIn(e.widget);
				}

				public void focusLost(FocusEvent e) {
					focusIn(null);
				}
			};
		}
		return focusListener;
	}

	/**
	 * @param widget
	 */
	private void focusIn(Widget widget) {
		String id = (String) controlToId.get(widget);
		if (currentId != id) {
			if (id == null) {
				currentId = null;
				currentControl = null;
			} else {
				currentId = id;
				currentControl = (Control) widget;
			}
			Map m = new HashMap();
			m.put(ISources.ACTIVE_FOCUS_CONTROL_ID_NAME, currentId);
			m.put(ISources.ACTIVE_FOCUS_CONTROL_NAME, currentControl);
			fireSourceChanged(ISources.ACTIVE_MENU, m);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.menus.IFocusService#removeTrackerFor(org.eclipse.swt.widgets.Control)
	 */
	public void removeFocusTracker(Control control) {
		controlToId.remove(control);
		if (control.isDisposed()) {
			return;
		}
		control.removeFocusListener(getFocusListener());
		control.removeDisposeListener(getDisposeListener());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISourceProvider#dispose()
	 */
	public void dispose() {
		Iterator i = controlToId.keySet().iterator();
		while (i.hasNext()) {
			Control c = (Control) i.next();
			if (!c.isDisposed()) {
				c.removeFocusListener(getFocusListener());
				c.removeDisposeListener(getDisposeListener());
			}
		}
		controlToId.clear();
		controlToId = null;
		focusListener = null;
		disposeListener = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISourceProvider#getCurrentState()
	 */
	public Map getCurrentState() {
		Map m = new HashMap();
		m.put(ISources.ACTIVE_FOCUS_CONTROL_ID_NAME, currentId);
		m.put(ISources.ACTIVE_FOCUS_CONTROL_NAME, currentControl);
		return m;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISourceProvider#getProvidedSourceNames()
	 */
	public String[] getProvidedSourceNames() {
		return PROVIDED_SOURCE_NAMES;
	}
}
