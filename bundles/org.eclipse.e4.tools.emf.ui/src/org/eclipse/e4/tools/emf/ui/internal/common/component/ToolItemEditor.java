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
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.ui.model.application.ItemType;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MToolItem;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ToolItemEditor extends AbstractComponentEditor {
	private Composite composite;
	private WritableValue master = new WritableValue();
	private Image image;
	private DataBindingContext context;

	public ToolItemEditor(EditingDomain editingDomain) {
		super(editingDomain);
	}

	@Override
	public Image getImage(Object element, Display display) {
		if (image == null) {
			try {
				image = loadSharedImage(display, new URL("platform:/plugin/org.eclipse.e4.ui.model.workbench.edit/icons/full/obj16/ToolItem_separator.gif"));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return image;
	}

	@Override
	public String getLabel(Object element) {
		return "Separator";
	}

	@Override
	public String getDescription(Object element) {
		return "Separator bla bla bla";
	}

	@Override
	public Composite getEditor(Composite parent, Object object) {
		if (composite == null) {
			context = new DataBindingContext();
			composite = createForm(parent, context, master);
		}
		master.setValue(object);
		return composite;
	}

	private Composite createForm(Composite parent, DataBindingContext context, WritableValue master) {
		parent = new Composite(parent, SWT.NONE);
		parent.setLayout(new GridLayout(3, false));

		IValueProperty textProp = WidgetProperties.text();

		Label l = new Label(parent, SWT.NONE);
		l.setText("Id");

		Text t = new Text(parent, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		t.setLayoutData(gd);
		context.bindValue(textProp.observe(t), EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.APPLICATION_ELEMENT__ID).observeDetail(master));

		if( this.getClass() != ToolItemEditor.class ) {
			createFormSubTypeForm(parent, context, master);
		}

		return parent;
	}

	private void createFormSubTypeForm(Composite parent, DataBindingContext context, WritableValue master) {
		IValueProperty textProp = WidgetProperties.text();

		// ------------------------------------------------------------

		Label l = new Label(parent, SWT.NONE);
		l.setText("Type");

		StructuredViewer viewer = new ComboViewer(parent);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setInput(new ItemType[] { ItemType.CHECK, ItemType.PUSH, ItemType.RADIO });
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		viewer.getControl().setLayoutData(gd);
		IObservableValue itemTypeObs = EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.ITEM__TYPE).observeDetail(master);
		context.bindValue(ViewerProperties.singleSelection().observe(viewer), itemTypeObs);

		// ------------------------------------------------------------

		l = new Label(parent, SWT.NONE);
		l.setText("Label");

		Text t = new Text(parent, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		t.setLayoutData(gd);
		context.bindValue(textProp.observe(t), EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.UI_LABEL__LABEL).observeDetail(master));

		// ------------------------------------------------------------

		l = new Label(parent, SWT.NONE);
		l.setText("Tooltip");

		t = new Text(parent, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		t.setLayoutData(gd);
		context.bindValue(textProp.observe(t), EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.UI_LABEL__TOOLTIP).observeDetail(master));

		// ------------------------------------------------------------

		l = new Label(parent, SWT.NONE);
		l.setText("Icon URI");

		t = new Text(parent, SWT.BORDER);
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		context.bindValue(textProp.observe(t), EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.UI_LABEL__ICON_URI).observeDetail(master));

		Button b = new Button(parent, SWT.PUSH | SWT.FLAT);
		b.setText("Find ...");
		// ------------------------------------------------------------
		createSubTypeFormElements(parent, context, master);
		// ------------------------------------------------------------

		l = new Label(parent, SWT.NONE);
		l.setText("Tags");
		l.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));

		viewer = new ListViewer(parent);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.heightHint = 80;
		viewer.getControl().setLayoutData(gd);
	}

	protected void createSubTypeFormElements(Composite parent, DataBindingContext context, WritableValue master) {

	}

	@Override
	public IObservableList getChildList(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDetailLabel(Object element) {
		MToolItem item = (MToolItem) element;
		if (item.getType() == ItemType.SEPARATOR) {
			return null;
		} else {
			if (item.getLabel() != null && item.getLabel().trim().length() > 0) {
				return item.getLabel();
			} else if (item.getTooltip() != null && item.getTooltip().trim().length() > 0) {
				return item.getTooltip();
			}
		}
		return null;
	}

}
