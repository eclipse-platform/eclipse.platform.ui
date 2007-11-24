/*******************************************************************************
 * Copyright (c) 2007 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 207844)
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.internal.swt;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.internal.databinding.provisional.swt.AbstractSWTObservableValue;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.3
 * 
 */
public class ShellObservableValue extends AbstractSWTObservableValue {

	private final Shell shell;

	/**
	 * @param shell
	 */
	public ShellObservableValue(Shell shell) {
		super(shell);
		this.shell = shell;
	}
	
	/**
	 * @param realm
	 * @param shell
	 */
	public ShellObservableValue(Realm realm, Shell shell) {
		super(realm, shell);
		this.shell = shell;
	}

	public void doSetValue(final Object value) {
		String oldValue = shell.getText();
		String newValue = value == null ? "" : value.toString(); //$NON-NLS-1$
		shell.setText(newValue);
		
		if (!newValue.equals(oldValue)) {
			fireValueChange(Diffs.createValueDiff(oldValue, newValue));
		}
	}

	public Object doGetValue() {
		return shell.getText();
	}

	public Object getValueType() {
		return String.class;
	}
}
