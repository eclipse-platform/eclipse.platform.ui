/*******************************************************************************
 * Copyright (c) 2009, 2017 Angelo Zerr and others.
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
 *     IBM Corporation - initial API and implementation
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 513300
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.dom;

import java.util.Objects;
import java.util.function.Supplier;
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
		if ("active".equalsIgnoreCase(s)) {
			return this.isActive;
		}
		if ("swt-parented".equalsIgnoreCase(s)) {
			return getShell().getParent() != null;
		}
		if ("swt-unparented".equalsIgnoreCase(s)) {
			return getShell().getParent() == null;
		}
		return super.isPseudoInstanceOf(s);
	}

	@Override
	protected Supplier<String> internalGetAttribute(String attr) {
		if("title".equals(attr)) {
			return () -> Objects.toString(getShell().getText(), "");
		}
		if ("parentage".equals(attr)) {
			return () -> {
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
			};
		}
		return super.internalGetAttribute(attr);
	}

	@Override
	public void reset() {
		CSSSWTImageHelper.restoreDefaultImage(getShell());
		super.reset();
	}
}
