/*******************************************************************************
 * Copyright (c) 2009, 2014 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.dom;

import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTImageHelper;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

/**
 * {@link CSSStylableElement} implementation which wrap SWT {@link Shell}.
 *
 */
public class ShellElement extends CompositeElement {

	protected boolean isActive;

	// Create SWT Shell Listener
	private ShellListener shellListener = new ShellListener() {

		@Override
		public void shellActivated(ShellEvent e) {
			ShellElement.this.isActive = true;
			doApplyStyles();
		}

		@Override
		public void shellDeactivated(ShellEvent e) {
			ShellElement.this.isActive = false;
			doApplyStyles();
		}

		@Override
		public void shellDeiconified(ShellEvent e) {
		}

		@Override
		public void shellIconified(ShellEvent e) {
		}

		@Override
		public void shellClosed(ShellEvent e) {
			ShellElement.this.dispose();
		}
	};

	public ShellElement(Shell shell, CSSEngine engine) {
		super(shell, engine);
	}

	@Override
	public void initialize() {
		super.initialize();

		Shell shell = getShell();

		if (!dynamicEnabled) {
			return;
		}

		// Add Shell listener
		shell.addShellListener(shellListener);
	}

	@Override
	public Node getParentNode() {
		// Shells are considered as root notes; see bug 375069
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375069
		return null;
	}

	private Shell getShell() {
		return (Shell) getNativeWidget();
	}

	@Override
	public void dispose() {
		super.dispose();

		if (!dynamicEnabled) {
			return;
		}

		Shell shell = getShell();
		if (!shell.isDisposed()) {
			shell.removeShellListener(shellListener);
		}
	}

	@Override
	public boolean isPseudoInstanceOf(String s) {
		if ("active".equals(s)) {
			return this.isActive;
		}
		if ("swt-parented".equals(s)) {
			return getShell().getParent() != null;
		}
		if ("swt-unparented".equals(s)) {
			return getShell().getParent() == null;
		}
		return super.isPseudoInstanceOf(s);
	}

	@Override
	public String getAttribute(String attr) {
		if("title".equals(attr)) {
			String title = getShell().getText();
			return title != null ? title : "";
		}
		if ("parentage".equals(attr)) {
			Shell shell = getShell();
			Composite parent = shell.getParent();
			if (parent == null) {
				return "";
			}
			StringBuilder sb = new StringBuilder();
			do {
				String id = WidgetElement.getID(parent);
				if (id != null && id.length() > 0) {
					sb.append(id).append(' ');
				}
				parent = parent.getParent();
			} while (parent != null);
			return sb.toString().trim();
		}
		return super.getAttribute(attr);
	}

	@Override
	public void reset() {
		CSSSWTImageHelper.restoreDefaultImage(getShell());
		super.reset();
	}
}
