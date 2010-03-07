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
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class ApplicationEditor extends AbstractComponentEditor {

	private Composite composite;
	private WritableValue master = new WritableValue();
	private Image image;
	private DataBindingContext context;

	private IListProperty HANDLER_CONTAINER__HANDLERS = EMFProperties.list(MApplicationPackage.Literals.HANDLER_CONTAINER__HANDLERS);
	private IListProperty BINDING_CONTAINER__BINDINGS = EMFProperties.list(MApplicationPackage.Literals.BINDING_CONTAINER__BINDINGS);
	private IListProperty APPLICATION__COMMANDS = EMFProperties.list(MApplicationPackage.Literals.APPLICATION__COMMANDS);
	private IListProperty ELEMENT_CONTAINER__CHILDREN = EMFProperties.list(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN);

	public ApplicationEditor(EditingDomain editingDomain) {
		super(editingDomain);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Image getImage(Object element, Display display) {
		if( image == null ) {
			try {
				image = loadSharedImage(display, new URL("platform:/plugin/org.eclipse.e4.ui.model.workbench.edit/icons/full/obj16/Application.gif"));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return image;
	}

	@Override
	public String getLabel(Object element) {
		return "Application";
	}

	@Override
	public String getDescription(Object element) {
		return "Application bla, bla, bla";
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

	protected Composite createForm(Composite parent, DataBindingContext context, IObservableValue master) {
		parent = new Composite(parent,SWT.NONE);

		return parent;
	}

	@Override
	public IObservableList getChildList(Object element) {
		WritableList list = new WritableList();
		list.add(new VirtualEntry<Object>( ModelEditor.VIRTUAL_HANDLER, HANDLER_CONTAINER__HANDLERS, element, "Handlers") {

			@Override
			protected boolean accepted(Object o) {
				return true;
			}

		});

		list.add(new VirtualEntry<Object>( ModelEditor.VIRTUAL_BINDING, BINDING_CONTAINER__BINDINGS, element, "Bindings") {

			@Override
			protected boolean accepted(Object o) {
				return true;
			}

		});

		list.add(new VirtualEntry<Object>( ModelEditor.VIRTUAL_COMMAND, APPLICATION__COMMANDS, element, "Commands") {

			@Override
			protected boolean accepted(Object o) {
				return true;
			}

		});

		list.add(new VirtualEntry<Object>( ModelEditor.VIRTUAL_WINDOWS, ELEMENT_CONTAINER__CHILDREN, element, "Windows") {

			@Override
			protected boolean accepted(Object o) {
				return true;
			}

		});

		return list;
	}

	@Override
	public String getDetailLabel(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

}
