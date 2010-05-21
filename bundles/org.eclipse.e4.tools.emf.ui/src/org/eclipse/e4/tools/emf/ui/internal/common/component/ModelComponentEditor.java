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
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.ui.model.application.MModelComponent;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
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

	private IListProperty MODEL_COMPONENT__CHILDREN = EMFProperties.list( ApplicationPackageImpl.Literals.MODEL_COMPONENT__CHILDREN);
	private IListProperty PART_DESCRIPTOR_CONTAINER__DESCRIPTORS = EMFProperties.list( BasicPackageImpl.Literals.PART_DESCRIPTOR_CONTAINER__DESCRIPTORS);
	private IListProperty HANDLER_CONTAINER__HANDLERS = EMFProperties.list(CommandsPackageImpl.Literals.HANDLER_CONTAINER__HANDLERS);
	private IListProperty BINDING_CONTAINER__BINDINGS = EMFProperties.list(CommandsPackageImpl.Literals.BINDING_TABLE_CONTAINER__BINDING_TABLES);
	private IListProperty MODEL_COMPONENT__COMMANDS = EMFProperties.list(ApplicationPackageImpl.Literals.MODEL_COMPONENT__COMMANDS);
	private IListProperty MODEL_COMPONENT__BINDINGS = EMFProperties.list(ApplicationPackageImpl.Literals.MODEL_COMPONENT__BINDINGS);
	
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
		l.setText(Messages.ModelComponentEditor_Id);

		Text t = new Text(parent, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=2;
		t.setLayoutData(gd);
		context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID).observeDetail(getMaster()));

		// ------------------------------------------------------------

		l = new Label(parent, SWT.NONE);
		l.setText(Messages.ModelComponentEditor_ParentId);

		t = new Text(parent, SWT.BORDER);
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), ApplicationPackageImpl.Literals.MODEL_COMPONENT__PARENT_ID).observeDetail(getMaster()));

		Button b = new Button(parent, SWT.PUSH|SWT.FLAT);
		b.setText(Messages.ModelComponentEditor_Find);

		// ------------------------------------------------------------

		l = new Label(parent, SWT.NONE);
		l.setText(Messages.ModelComponentEditor_PositionInParent);

		t = new Text(parent, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=2;
		t.setLayoutData(gd);
		context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), ApplicationPackageImpl.Literals.MODEL_COMPONENT__POSITION_IN_PARENT).observeDetail(getMaster()));

		// ------------------------------------------------------------

		l = new Label(parent, SWT.NONE);
		l.setText(Messages.ModelComponentEditor_Processor);

		t = new Text(parent, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=2;
		t.setLayoutData(gd);
		context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), ApplicationPackageImpl.Literals.MODEL_COMPONENT__PROCESSOR).observeDetail(getMaster()));
		
		// ------------------------------------------------------------

		l = new Label(parent, SWT.NONE);
		l.setText(Messages.ModelComponentEditor_Tags);

		t = new Text(parent, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=2;
		t.setLayoutData(gd);

		new Label(parent, SWT.NONE);
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
				image = loadSharedImage(display, new URL("platform:/plugin/org.eclipse.e4.tools.emf.ui/icons/full/modelelements/ModelComponent.png")); //$NON-NLS-1$
//				image = loadSharedImage(display, new URL("platform:/plugin/org.eclipse.e4.ui.model.workbench.edit/icons/full/obj16/ModelComponent.gif")); //$NON-NLS-1$
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return image;
	}

	@Override
	public String getLabel(Object element) {
		return Messages.ModelComponentEditor_Label;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.ModelComponentEditor_Description;
	}

	@Override
	public IObservableList getChildList(Object element) {
		WritableList list = new WritableList();
		
		list.add(new VirtualEntry<Object>( ModelEditor.VIRTUAL_HANDLER, HANDLER_CONTAINER__HANDLERS, element, Messages.ModelComponentEditor_Handlers) {

			@Override
			protected boolean accepted(Object o) {
				return true;
			}

		});

		list.add(new VirtualEntry<Object>( ModelEditor.VIRTUAL_BINDING_TABLE, BINDING_CONTAINER__BINDINGS, element, Messages.ModelComponentEditor_BindingTables) {

			@Override
			protected boolean accepted(Object o) {
				return true;
			}

		});
		
		list.add(new VirtualEntry<Object>( ModelEditor.VIRTUAL_MODEL_COMP_BINDINGS, MODEL_COMPONENT__BINDINGS, element, Messages.ModelComponentEditor_KeyBindings) {

			@Override
			protected boolean accepted(Object o) {
				return true;
			}

		});
		
		
		list.add(new VirtualEntry<Object>( ModelEditor.VIRTUAL_MODEL_COMP_COMMANDS, MODEL_COMPONENT__COMMANDS, element, Messages.ModelComponentEditor_Commands) {

			@Override
			protected boolean accepted(Object o) {
				return true;
			}

		});
		
//		list.add(new VirtualEntry<Object>( ModelEditor.VIRTUAL_PART_MENU, MODEL_COMPONENT__CHILDREN, element, Messages.ModelComponentEditor_Menus) {
//
//			@Override
//			protected boolean accepted(Object o) {
//				return o instanceof MMenu;
//			}
//
//		});
		list.add(new VirtualEntry<Object>( ModelEditor.VIRTUAL_MODEL_COMPONENT_CHILDREN, MODEL_COMPONENT__CHILDREN, element, Messages.ModelComponentEditor_UiChildren) {

			@Override
			protected boolean accepted(Object o) {
				return o instanceof MUIElement;
			}

		});
		list.add(new VirtualEntry<Object>( ModelEditor.VIRTUAL_PART_DESCRIPTORS, PART_DESCRIPTOR_CONTAINER__DESCRIPTORS, element, Messages.ModelComponentEditor_PartDescriptors) {

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
			return Messages.ModelComponentEditor_DetailParentId + ": " + o.getParentID(); //$NON-NLS-1$
		}
		return null;
	}

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] {
			FeaturePath.fromList(ApplicationPackageImpl.Literals.MODEL_COMPONENT__PARENT_ID)	
		};
	}
}
