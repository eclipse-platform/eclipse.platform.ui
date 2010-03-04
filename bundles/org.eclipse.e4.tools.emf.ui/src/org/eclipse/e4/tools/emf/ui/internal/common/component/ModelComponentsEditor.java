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

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class ModelComponentsEditor extends AbstractComponentEditor {
	private IListProperty MODEL_COMPONENTS__COMPONENTS = EMFProperties.list(MApplicationPackage.Literals.MODEL_COMPONENTS__COMPONENTS);

	private Composite composite;
	private WritableValue master = new WritableValue();
	private Image image;
	private DataBindingContext context;

	@Override
	public Image getImage(Object element, Display display) {
		if( image == null ) {
			image = new Image(display, getClass().getClassLoader().getResourceAsStream("/icons/application_view_icons.png"));
		}
		return image;
	}

	@Override
	public String getLabel(Object element) {
		return "Model Components";
	}

	@Override
	public String getDescription(Object element) {
		return "Some bla bla bla bla";
	}

	@Override
	public Composite getEditor(Composite parent, Object object) {
		if( composite == null ) {
			context = new DataBindingContext();
			composite = createForm(parent);
		}
		master.setValue(object);
		return composite;
	}

	private Composite createForm(Composite parent) {
		parent = new Composite(parent, SWT.NONE);

		return parent;
	}

	@Override
	public IObservableList getChildList(Object element) {
		return MODEL_COMPONENTS__COMPONENTS.observe(element);
	}

	@Override
	public String getDetailLabel(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

}
