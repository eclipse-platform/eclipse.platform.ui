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
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MMenu;
import org.eclipse.e4.ui.model.application.MModelComponent;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MPartDescriptor;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ModelComponentEditor extends AbstractComponentEditor {

	private Composite composite;
	private Image image;
	private EMFDataBindingContext context;

	private IListProperty MODEL_COMPONENT__CHILDREN = EMFProperties.list( MApplicationPackage.Literals.MODEL_COMPONENT__CHILDREN);
	private IListProperty PART_DESCRIPTOR_CONTAINER__DESCRIPTORS = EMFProperties.list( MApplicationPackage.Literals.PART_DESCRIPTOR_CONTAINER__DESCRIPTORS);
	
	public ModelComponentEditor(EditingDomain editingDomain) {
		super(editingDomain);
	}

	@Override
	public Composite getEditor(Composite parent, Object object) {
		if( composite == null ) {
			context = new EMFDataBindingContext();
			composite = createForm(parent);
		}
		getMaster().setValue(object);
		return composite;
	}

	public void dispose() {
		if( image != null ) {
			image.dispose();
			image = null;
		}

		if( composite != null ) {
			composite.dispose();
			composite = null;
		}

		if( context != null ) {
			context.dispose();
			context = null;
		}
	}

	private Composite createForm(Composite parent) {
		parent = new Composite(parent,SWT.NONE);
		parent.setLayout(new GridLayout(3, false));

		IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);

		Label l = new Label(parent, SWT.NONE);
		l.setText("Id");

		Text t = new Text(parent, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=2;
		t.setLayoutData(gd);
		context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.APPLICATION_ELEMENT__ID).observeDetail(getMaster()));

		// ------------------------------------------------------------

		l = new Label(parent, SWT.NONE);
		l.setText("Parent-Id");

		t = new Text(parent, SWT.BORDER);
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.MODEL_COMPONENT__PARENT_ID).observeDetail(getMaster()));

		Button b = new Button(parent, SWT.PUSH|SWT.FLAT);
		b.setText("Find ...");

		// ------------------------------------------------------------

		l = new Label(parent, SWT.NONE);
		l.setText("Position in Parent");

		t = new Text(parent, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=2;
		t.setLayoutData(gd);
		context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.MODEL_COMPONENT__POSITION_IN_PARENT).observeDetail(getMaster()));

		// ------------------------------------------------------------

		l = new Label(parent, SWT.NONE);
		l.setText("Processor");

		t = new Text(parent, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=2;
		t.setLayoutData(gd);
		context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.MODEL_COMPONENT__PROCESSOR).observeDetail(getMaster()));
		
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
		gd.heightHint = 130;
		viewer.getList().setLayoutData(gd);

		return parent;
	}

	@Override
	public Image getImage(Object element, Display display) {
		if( image == null ) {
			try {
				image = loadSharedImage(display, new URL("platform:/plugin/org.eclipse.e4.ui.model.workbench.edit/icons/full/obj16/ModelComponent.gif"));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return image;
	}

	@Override
	public String getLabel(Object element) {
		return "Model Component";
	}

	@Override
	public String getDescription(Object element) {
		return "The model component ... bla bla bla";
	}

	@Override
	public IObservableList getChildList(Object element) {
		WritableList list = new WritableList();
		list.add(new VirtualEntry<Object>( ModelEditor.VIRTUAL_MENU, MODEL_COMPONENT__CHILDREN, element, "Menus") {

			@Override
			protected boolean accepted(Object o) {
				return o instanceof MMenu;
			}

		});
		list.add(new VirtualEntry<Object>( ModelEditor.VIRTUAL_PART, MODEL_COMPONENT__CHILDREN, element, "Parts") {

			@Override
			protected boolean accepted(Object o) {
				return o instanceof MPart;
			}

		});
		list.add(new VirtualEntry<Object>( ModelEditor.VIRTUAL_PART_DESCRIPTORS, PART_DESCRIPTOR_CONTAINER__DESCRIPTORS, element, "PartDescriptors") {

			@Override
			protected boolean accepted(Object o) {
				return o instanceof MPartDescriptor;
			}

		});
		return list;
	}

	@Override
	public String getDetailLabel(Object element) {
		MModelComponent o = (MModelComponent) element;
		if( o.getParentID() != null ) {
			return "parentId: " + o.getParentID();
		}
		return null;
	}

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] {
			FeaturePath.fromList(MApplicationPackage.Literals.MODEL_COMPONENT__PARENT_ID)	
		};
	}
}
