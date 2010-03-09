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

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.ui.model.application.ItemType;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MToolItem;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
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
	private Image image;
	private EMFDataBindingContext context;

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
			context = new EMFDataBindingContext();
			composite = createForm(parent, context, getMaster());
		}
		getMaster().setValue(object);
		return composite;
	}

	private Composite createForm(Composite parent, EMFDataBindingContext context, WritableValue master) {
		parent = new Composite(parent, SWT.NONE);
		parent.setLayout(new GridLayout(3, false));

		IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);

		Label l = new Label(parent, SWT.NONE);
		l.setText("Id");

		Text t = new Text(parent, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		t.setLayoutData(gd);
		context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.APPLICATION_ELEMENT__ID).observeDetail(master));

		if (this.getClass() != ToolItemEditor.class) {
			createFormSubTypeForm(parent, context, master);
		}

		return parent;
	}

	private void createFormSubTypeForm(Composite parent, EMFDataBindingContext context, final WritableValue master) {
		IValueProperty textProp = WidgetProperties.text(SWT.Modify);

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Type");

			ComboViewer viewer = new ComboViewer(parent);
			viewer.setContentProvider(new ArrayContentProvider());
			viewer.setInput(new ItemType[] { ItemType.CHECK, ItemType.PUSH, ItemType.RADIO });
			GridData gd = new GridData();
			gd.horizontalSpan = 2;
			viewer.getControl().setLayoutData(gd);
			IObservableValue itemTypeObs = EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.ITEM__TYPE).observeDetail(master);
			context.bindValue(ViewerProperties.singleSelection().observe(viewer), itemTypeObs);
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Label");

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observe(t), EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.UI_LABEL__LABEL).observeDetail(master));
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Tooltip");

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observe(t), EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.UI_LABEL__TOOLTIP).observeDetail(master));
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Icon URI");

			Text t = new Text(parent, SWT.BORDER);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			context.bindValue(textProp.observe(t), EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.UI_LABEL__ICON_URI).observeDetail(master));

			Button b = new Button(parent, SWT.PUSH | SWT.FLAT);
			b.setText("Find ...");
			b.setImage(getImage(b.getDisplay(), SEARCH_IMAGE));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
		}

		// ------------------------------------------------------------

		createSubTypeFormElements(parent, context, master);

		ControlFactory.createTagsWidget(parent, this);
	}

	protected void createSubTypeFormElements(Composite parent, EMFDataBindingContext context, WritableValue master) {

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
