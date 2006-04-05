/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding;

import org.eclipse.jface.examples.databinding.nestedselection.BindingFactory;
import org.eclipse.jface.examples.databinding.nestedselection.Person;
import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;
import org.eclipse.jface.internal.databinding.provisional.description.Property;
import org.eclipse.jface.internal.databinding.provisional.observable.list.WritableList;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @since 3.2
 * 
 */
public class HelloWorld {

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		DataBindingContext dbc = BindingFactory.createContext(shell);
		Combo combo = new Combo(shell, SWT.READ_ONLY);
		WritableList list = new WritableList();
		list.add("Hello");
		list.add("Bonjour");
		list.add("Guten Tag");
		dbc.bind(combo, list, null);
		combo.select(0);
		Text text = new Text(shell, SWT.BORDER);
		Person person = new Person("Boris", "1234 Carling Ave", "Ottawa",
				"Canada");
		dbc.bind(text, new Property(person, "name"), null);
		// shell.setLayout(new GridLayout(2, false));
		GridLayoutFactory.swtDefaults().numColumns(2).generateLayout(shell);
		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}
