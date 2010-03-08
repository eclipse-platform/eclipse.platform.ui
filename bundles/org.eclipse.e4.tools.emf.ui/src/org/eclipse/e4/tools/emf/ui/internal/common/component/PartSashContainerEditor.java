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
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MPartSashContainer;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.IViewerValueProperty;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class PartSashContainerEditor extends AbstractComponentEditor {

	private Composite composite;
	private Image vImage;
	private Image hImage;
	private EMFDataBindingContext context;

	private IListProperty ELEMENT_CONTAINER__CHILDREN = EMFProperties.list(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN);

	public PartSashContainerEditor(EditingDomain editingDomain) {
		super(editingDomain);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Image getImage(Object element, Display display) {
		boolean horizontal = ((MPartSashContainer)element).isHorizontal();

		if( vImage == null && ! horizontal ) {
			try {
				vImage = loadSharedImage(display, new URL("platform:/plugin/org.eclipse.e4.ui.model.workbench.edit/icons/full/obj16/PartSashContainer_vertical.gif"));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if( hImage == null && horizontal ) {
			try {
				hImage = loadSharedImage(display, new URL("platform:/plugin/org.eclipse.e4.ui.model.workbench.edit/icons/full/obj16/PartSashContainer.gif"));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if( horizontal ) {
			return hImage;
		} else {
			return vImage;
		}
	}

	@Override
	public String getLabel(Object element) {
		return "Sash";
	}

	@Override
	public String getDescription(Object element) {
		return "Sash bla bla bla";
	}

	@Override
	public Composite getEditor(Composite parent, Object object) {
		if( composite == null ) {
			context = new EMFDataBindingContext();
			composite = createForm(parent,context, getMaster());
		}
		getMaster().setValue(object);
		return composite;
	}

	private Composite createForm(Composite parent, EMFDataBindingContext context,
			WritableValue master) {
		parent = new Composite(parent,SWT.NONE);
		parent.setLayout(new GridLayout(3, false));

		IValueProperty textProp = WidgetProperties.text();

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Id");

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan=2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observe(t), EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.APPLICATION_ELEMENT__ID).observeDetail(getMaster()));
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Orientation");

			ComboViewer viewer = new ComboViewer(parent);
			viewer.setContentProvider(new ArrayContentProvider());
			viewer.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element) {
					return ((Boolean)element).booleanValue() ? "Horizontal" : "Vertical";
				}
			});
			viewer.setInput(new Boolean[] { Boolean.TRUE, Boolean.FALSE });
			IViewerValueProperty vProp = ViewerProperties.singleSelection();
			context.bindValue(vProp.observe(viewer), EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.GENERIC_TILE__HORIZONTAL).observeDetail(getMaster()));
		}


		return parent;
	}

	@Override
	public IObservableList getChildList(Object element) {
		return ELEMENT_CONTAINER__CHILDREN.observe(element);
	}

	@Override
	public String getDetailLabel(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

}
