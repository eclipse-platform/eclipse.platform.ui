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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;


/**
 * Applies styles to the widgets of a {@link Display} when they are skinned, and
 * to a {@link CTabFolder} page the engine skipped once it becomes the selected tab's page.
 */
public class CSSSWTApplyStylesListener {
	// marks a control this listener already watches, since every re-skin repeats the skin event
	private final String pendingPageKey = getClass().getName() + '@' + Integer.toHexString(hashCode());

	CSSEngine engine;
	public CSSSWTApplyStylesListener(Display display, final CSSEngine engine) {
		this.engine = engine;
		display.addListener(SWT.Skin, event -> {
			if (engine == null) {
				return;
			}
			engine.applyStyles(event.widget, false);
			if (event.widget instanceof Control control && control.getParent() instanceof CTabFolder folder
					&& control != folder.getTopRight() && !isPageOfSelectedTab(folder, control)) {
				// the folder hides this page from the engine, so it was skipped
				styleWhenItBecomesThePage(folder, control);
			}
		});
	}

	/**
	 * Styles the control once it is the selected tab's page, which attaching it
	 * resizes it to or selecting its tab shows it.
	 */
	private void styleWhenItBecomesThePage(CTabFolder folder, Control control) {
		if (control.getData(pendingPageKey) != null) {
			return;
		}
		Listener listener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				// the control may have been reparented, and a new skin event watches it again
				boolean inFolder = control.getParent() == folder && !folder.isDisposed();
				if (inFolder && !isPageOfSelectedTab(folder, control)) {
					return;
				}
				control.removeListener(SWT.Show, this);
				control.removeListener(SWT.Resize, this);
				control.setData(pendingPageKey, null);
				if (inFolder) {
					engine.applyStyles(control, true);
				}
			}
		};
		control.setData(pendingPageKey, listener);
		control.addListener(SWT.Show, listener);
		control.addListener(SWT.Resize, listener);
	}

	private static boolean isPageOfSelectedTab(CTabFolder folder, Control control) {
		int selected = folder.getSelectionIndex();
		return selected >= 0 && folder.getItem(selected).getControl() == control;
	}

}
