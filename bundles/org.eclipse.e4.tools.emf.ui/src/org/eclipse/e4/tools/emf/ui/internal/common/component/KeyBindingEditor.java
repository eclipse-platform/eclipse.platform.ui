/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class KeyBindingEditor extends AbstractComponentEditor {
	private Composite composite;
	private WritableValue master = new WritableValue();
	private Image image;
	private DataBindingContext context;

	@Override
	public Image getImage(Object element, Display display) {
		if( image == null ) {
			try {
				image = loadSharedImage(display, new URL("platform:/plugin/org.eclipse.e4.ui.model.workbench.edit/icons/full/obj16/KeyBinding.gif"));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return image;
	}

	@Override
	public String getLabel(Object element) {
		return "Binding";
	}

	@Override
	public String getDescription(Object element) {
		return "Binding bla bla bla";
	}

	@Override
	public Composite getEditor(Composite parent, Object object) {
		if( composite == null ) {
			context = new DataBindingContext();
			composite = createForm(parent,context, master);
		}
		master.setValue(object);
		return composite;
	}

	private Composite createForm(Composite parent, DataBindingContext context2,
			WritableValue master) {
		parent = new Composite(parent,SWT.NONE);
		return parent;
	}

	@Override
	public IObservableList getChildList(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDetailLabel(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

}
