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
 *******************************************************************************/

package org.eclipse.ui.internal.menus;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.core.expressions.IEvaluationContext;
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
 */
public class FocusControlSourceProvider extends AbstractSourceProvider implements IFocusService {

	/**
	 * The names of the sources supported by this source provider.
	 */
	private static final String[] PROVIDED_SOURCE_NAMES = new String[] { ISources.ACTIVE_FOCUS_CONTROL_ID_NAME,
			ISources.ACTIVE_FOCUS_CONTROL_NAME };

	Map<Control, String> controlToId = new HashMap<>();
	private FocusListener focusListener;

	private String currentId;

	private Control currentControl;

	private DisposeListener disposeListener;

	@Override
	public void addFocusTracker(Control control, String id) {
		if (control.isDisposed()) {
			return;
		}
		controlToId.put(control, id);
		control.addFocusListener(getFocusListener());
		control.addDisposeListener(getDisposeListener());
	}

	private DisposeListener getDisposeListener() {
		if (disposeListener == null) {
			disposeListener = e -> {
				controlToId.remove(e.widget);
				if (currentControl == e.widget) {
					focusIn(null);

				}
			};
		}
		return disposeListener;
	}

	private FocusListener getFocusListener() {
		if (focusListener == null) {
			focusListener = new FocusListener() {
				@Override
				public void focusGained(FocusEvent e) {
					focusIn(e.widget);
				}

				@Override
				public void focusLost(FocusEvent e) {
					focusIn(null);
				}
			};
		}
		return focusListener;
	}

	private void focusIn(Widget widget) {
		String id = controlToId.get(widget);
		if (currentId != id) {
			Map<String, Object> m = new HashMap<>();
			if (id == null) {
				currentId = null;
				currentControl = null;
				m.put(ISources.ACTIVE_FOCUS_CONTROL_ID_NAME, IEvaluationContext.UNDEFINED_VARIABLE);
				m.put(ISources.ACTIVE_FOCUS_CONTROL_NAME, IEvaluationContext.UNDEFINED_VARIABLE);
			} else {
				currentId = id;
				currentControl = (Control) widget;
				m.put(ISources.ACTIVE_FOCUS_CONTROL_ID_NAME, currentId);
				m.put(ISources.ACTIVE_FOCUS_CONTROL_NAME, currentControl);
			}
			fireSourceChanged(ISources.ACTIVE_MENU, m);
		}
	}

	@Override
	public void removeFocusTracker(Control control) {
		if (controlToId == null) {
			// bug 396909: avoid NPEs if the service has already been disposed
			return;
		}
		controlToId.remove(control);
		if (control.isDisposed()) {
			return;
		}
		control.removeFocusListener(getFocusListener());
		control.removeDisposeListener(getDisposeListener());
	}

	@Override
	public void dispose() {
		Iterator<Control> i = controlToId.keySet().iterator();
		while (i.hasNext()) {
			Control c = i.next();
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

	@Override
	public Map getCurrentState() {
		Map<String, Object> m = new HashMap<>();
		if (currentId == null) {
			m.put(ISources.ACTIVE_FOCUS_CONTROL_ID_NAME, IEvaluationContext.UNDEFINED_VARIABLE);
			m.put(ISources.ACTIVE_FOCUS_CONTROL_NAME, IEvaluationContext.UNDEFINED_VARIABLE);

		} else {
			m.put(ISources.ACTIVE_FOCUS_CONTROL_ID_NAME, currentId);
			m.put(ISources.ACTIVE_FOCUS_CONTROL_NAME, currentControl);
		}
		return m;
	}

	@Override
	public String[] getProvidedSourceNames() {
		return PROVIDED_SOURCE_NAMES;
	}
}
