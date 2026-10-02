/*******************************************************************************
 * Copyright (c) 2008, 2018 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.engine;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;


/**
 * Applies styles to the widgets of a {@link Display} when they are skinned, and
 * to the page of an unselected {@link CTabFolder} tab once it is shown.
 */
public class CSSSWTApplyStylesListener {
	CSSEngine engine;
	// marks a control this listener already watches, since every re-skin repeats the skin event
	private final String pendingPageKey = getClass().getName() + '@' + Integer.toHexString(hashCode());

	public CSSSWTApplyStylesListener(Display display, final CSSEngine engine) {
		this.engine = engine;
		display.addListener(SWT.Skin, event -> {
			if (engine == null) {
				return;
			}
			engine.applyStyles(event.widget, false);
			if (event.widget instanceof Control control && control.getParent() instanceof CTabFolder folder
					&& isPageOfUnselectedTab(folder, control)) {
				// the folder hides this page from the engine, so the engine skipped it
				styleWhenItBecomesThePage(control);
			}
		});
	}

	/**
	 * Styles the control once it is shown, which selecting its tab does.
	 */
	private void styleWhenItBecomesThePage(Control control) {
		if (control.getData(pendingPageKey) != null) {
			return;
		}
		Listener listener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				// read the parent each time, since setParent may have moved the control out of its folder
				CTabFolder folder = control.getParent() instanceof CTabFolder f ? f : null;
				if (folder != null && isPageOfUnselectedTab(folder, control)) {
					return;
				}
				control.removeListener(SWT.Show, this);
				control.setData(pendingPageKey, null);
				if (folder != null) {
					engine.applyStyles(control, true);
				}
			}
		};
		control.setData(pendingPageKey, listener);
		control.addListener(SWT.Show, listener);
	}

	private static boolean isPageOfUnselectedTab(CTabFolder folder, Control control) {
		int selected = folder.getSelectionIndex();
		if (selected >= 0 && folder.getItem(selected).getControl() == control) {
			return false;
		}
		for (CTabItem item : folder.getItems()) {
			if (item.getControl() == control) {
				return true;
			}
		}
		return false;
	}

}
