/*******************************************************************************
 * Copyright (c) 2009 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.dom;

import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
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

		public void shellActivated(ShellEvent e) {
			ShellElement.this.isActive = true;
			doApplyStyles();
		}

		public void shellDeactivated(ShellEvent e) {
			ShellElement.this.isActive = false;
			doApplyStyles();
		}

		public void shellDeiconified(ShellEvent e) {
		}

		public void shellIconified(ShellEvent e) {
		}

		public void shellClosed(ShellEvent e) {
			ShellElement.this.dispose();
		}
	};

	public ShellElement(Shell shell, CSSEngine engine) {
		super(shell, engine);
	}

	public void initialize() {
		super.initialize();

		if (!dynamicEnabled) return; 
		
		
		Shell shell = getShell();
		// Add Shell listener
		shell.addShellListener(shellListener);
    }

    public Node getParentNode() {
        // Shells are considered as root notes; see bug 375069 
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=375069
        return null;
    }

	private Shell getShell() {
		return (Shell) getNativeWidget();
	}

	public void dispose() {
		super.dispose();
		
		if (!dynamicEnabled) return; 
		
		Shell shell = getShell();
		// Remove Shell listener
		shell.removeShellListener(shellListener);
	}

	public boolean isPseudoInstanceOf(String s) {
		if ("active".equals(s)) {
			return this.isActive;
		}
		return super.isPseudoInstanceOf(s);
	}
	
}
