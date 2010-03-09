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

import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class DirectToolItemEditor extends ToolItemEditor {
	private Image image;

	public DirectToolItemEditor(EditingDomain editingDomain) {
		super(editingDomain);
	}

	@Override
	public Image getImage(Object element, Display display) {
		if (image == null) {
			try {
				image = loadSharedImage(display, new URL("platform:/plugin/org.eclipse.e4.ui.model.workbench.edit/icons/full/obj16/DirectToolItem.gif"));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return image;
	}


	@Override
	protected void createSubTypeFormElements(Composite parent, EMFDataBindingContext context, WritableValue master) {
		IWidgetValueProperty textProp = WidgetProperties.text();

		Label l = new Label(parent, SWT.NONE);
		l.setText("Class URI");

		Text t = new Text(parent, SWT.BORDER);
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value( getEditingDomain(), MApplicationPackage.Literals.CONTRIBUTION__URI).observeDetail(master));

		Button b = new Button(parent, SWT.PUSH|SWT.FLAT);
		b.setText("Find ...");
		b.setImage(getImage(b.getDisplay(), SEARCH_IMAGE));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
	}

	@Override
	public String getLabel(Object element) {
		return "Direct Tool Item";
	}

	@Override
	public String getDescription(Object element) {
		return "Direct Tool Item bla bla bla";
	}
}