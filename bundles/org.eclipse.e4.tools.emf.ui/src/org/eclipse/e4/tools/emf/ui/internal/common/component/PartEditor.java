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
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class PartEditor extends AbstractComponentEditor {
	private Composite composite;
	private WritableValue master = new WritableValue();
	private Image image;
	private DataBindingContext context;

	private IListProperty PART__MENUS = EMFProperties.list(MApplicationPackage.Literals.PART__MENUS);
	private IListProperty HANDLER_CONTAINER__HANDLERS = EMFProperties.list(MApplicationPackage.Literals.HANDLER_CONTAINER__HANDLERS);
	private IListProperty BINDING_CONTAINER__BINDINGS = EMFProperties.list(MApplicationPackage.Literals.BINDING_CONTAINER__BINDINGS);


	@Override
	public Image getImage(Display display) {
		if( image == null ) {
			image = new Image(display, getClass().getClassLoader().getResourceAsStream("/icons/application_form.png"));
		}

		return image;
	}

	@Override
	public String getLabel(Object element) {
		return "Part Descriptor";
	}

	@Override
	public String getDescription(Object element) {
		return "Part Descriptor Bla Bla Bla Bla";
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
		parent.setLayout(new GridLayout(3, false));

		IValueProperty textProp = WidgetProperties.text();

		Label l = new Label(parent, SWT.NONE);
		l.setText("Id");

		Text t = new Text(parent, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=2;
		t.setLayoutData(gd);
		context.bindValue(textProp.observe(t), EMFProperties.value(MApplicationPackage.Literals.APPLICATION_ELEMENT__ID).observeDetail(master));

		// ------------------------------------------------------------

		l = new Label(parent, SWT.NONE);
		l.setText("Tags");

		t = new Text(parent, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=2;
		t.setLayoutData(gd);

		l = new Label(parent, SWT.NONE);
		ListViewer viewer = new ListViewer(parent);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=2;
		gd.heightHint = 80;
		viewer.getList().setLayoutData(gd);

		// ------------------------------------------------------------

		l = new Label(parent, SWT.NONE);
		l.setText("Label");

		t = new Text(parent, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=2;
		t.setLayoutData(gd);
		context.bindValue(textProp.observe(t), EMFProperties.value(MApplicationPackage.Literals.UI_LABEL__LABEL).observeDetail(master));

		// ------------------------------------------------------------

		l = new Label(parent, SWT.NONE);
		l.setText("Tooltip");

		t = new Text(parent, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=2;
		t.setLayoutData(gd);
		context.bindValue(textProp.observe(t), EMFProperties.value(MApplicationPackage.Literals.UI_LABEL__TOOLTIP).observeDetail(master));

		// ------------------------------------------------------------

		l = new Label(parent, SWT.NONE);
		l.setText("Icon URI");

		t = new Text(parent, SWT.BORDER);
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		context.bindValue(textProp.observe(t), EMFProperties.value(MApplicationPackage.Literals.UI_LABEL__ICON_URI).observeDetail(master));

		Button b = new Button(parent, SWT.PUSH|SWT.FLAT);
		b.setText("Find ...");

		// ------------------------------------------------------------

		l = new Label(parent, SWT.NONE);
		l.setText("Class URI");

		t = new Text(parent, SWT.BORDER);
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		context.bindValue(textProp.observe(t), EMFProperties.value(MApplicationPackage.Literals.CONTRIBUTION__URI).observeDetail(master));

		b = new Button(parent, SWT.PUSH|SWT.FLAT);
		b.setText("Find ...");

		// ------------------------------------------------------------

		l = new Label(parent, SWT.NONE);
		l.setText("Variables");

		t = new Text(parent, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=2;
		t.setLayoutData(gd);

		l = new Label(parent, SWT.NONE);
		viewer = new ListViewer(parent);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=2;
		gd.heightHint = 80;
		viewer.getList().setLayoutData(gd);

		// ------------------------------------------------------------

		l = new Label(parent, SWT.NONE);
		l.setText("Properties");

		t = new Text(parent, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=2;
		t.setLayoutData(gd);

		l = new Label(parent, SWT.NONE);
		TableViewer tableviewer = new TableViewer(parent);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=2;
		gd.heightHint = 80;
		tableviewer.getTable().setHeaderVisible(true);
		tableviewer.getControl().setLayoutData(gd);

		TableViewerColumn column = new TableViewerColumn(tableviewer, SWT.NONE);
		column.getColumn().setText("Key");
		column.getColumn().setWidth(200);

		column = new TableViewerColumn(tableviewer, SWT.NONE);
		column.getColumn().setText("Value");
		column.getColumn().setWidth(200);

		// ------------------------------------------------------------

		l = new Label(parent, SWT.NONE);
		l.setText("");

		Composite booleanContainer = new Composite(parent,SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=2;
		booleanContainer.setBackgroundMode(SWT.INHERIT_DEFAULT);
		booleanContainer.setLayoutData(gd);
		booleanContainer.setLayout(new GridLayout(4,false));

		Button checkbox = new Button(booleanContainer, SWT.CHECK);
		checkbox.setText("to render");

		checkbox = new Button(booleanContainer, SWT.CHECK);
		checkbox.setText("on Top");

		checkbox = new Button(booleanContainer, SWT.CHECK);
		checkbox.setText("visible");

		checkbox = new Button(booleanContainer, SWT.CHECK);
		checkbox.setText("closeable");

		return parent;
	}

	@Override
	public IObservableList getChildList(Object element) {
		WritableList list = new WritableList();
		list.add(new VirtualEntry<Object>( ModelEditor.VIRTUAL_MENU, PART__MENUS, element, "Menus") {

			@Override
			protected boolean accepted(Object o) {
				return true;
			}

		});

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

		return list;
	}

	@Override
	public String getDetailLabel(Object element) {
		MPart o = (MPart) element;
		return o.getLabel();
	}

}
