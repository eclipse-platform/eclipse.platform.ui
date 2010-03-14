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
import java.util.List;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.ComponentLabelProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MPartSashContainer;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.databinding.IEMFListProperty;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.databinding.edit.IEMFEditListProperty;
import org.eclipse.emf.databinding.edit.IEMFEditValueProperty;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.IViewerValueProperty;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class PartSashContainerEditor extends AbstractComponentEditor {

	private Composite composite;
	private Image vImage;
	private Image hImage;
	private EMFDataBindingContext context;
	private ModelEditor editor;

	private IListProperty ELEMENT_CONTAINER__CHILDREN = EMFProperties.list(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN);

	public PartSashContainerEditor(EditingDomain editingDomain, ModelEditor editor) {
		super(editingDomain);
		this.editor = editor;
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

	private Composite createForm(Composite parent, final EMFDataBindingContext context,
			WritableValue master) {
		parent = new Composite(parent,SWT.NONE);
		parent.setLayout(new GridLayout(3, false));

		IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Id");

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan=2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.APPLICATION_ELEMENT__ID).observeDetail(getMaster()));
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Orientation");

			ComboViewer viewer = new ComboViewer(parent);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan=2;
			viewer.getControl().setLayoutData(gd);
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

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Selected Element");

			ComboViewer viewer = new ComboViewer(parent);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan=2;
			viewer.getControl().setLayoutData(gd);
			IEMFEditListProperty listProp = EMFEditProperties.list(getEditingDomain(), MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN);
			IEMFEditValueProperty valProp = EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.APPLICATION_ELEMENT__ID);
			IViewerValueProperty vProp = ViewerProperties.singleSelection();

			final Binding[] binding = new Binding[1];
			final IObservableValue uiObs = vProp.observe(viewer);
			final IObservableValue mObs = EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.ELEMENT_CONTAINER__SELECTED_ELEMENT).observeDetail(getMaster());
			getMaster().addValueChangeListener(new IValueChangeListener() {

				public void handleValueChange(ValueChangeEvent event) {
					if( binding[0] != null ) {
						binding[0].dispose();
					}

				}
			});

			ViewerSupport.bind(viewer, listProp.observeDetail(getMaster()), valProp);

			getMaster().addValueChangeListener(new IValueChangeListener() {

				public void handleValueChange(ValueChangeEvent event) {
					binding[0] = context.bindValue(uiObs, mObs);
				}
			});
		}

		ControlFactory.createTagsWidget(parent, this);

		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Controls");
			l.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
			
			final TableViewer viewer = new TableViewer(parent);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.heightHint = 200;
			viewer.getControl().setLayoutData(gd);
			ObservableListContentProvider cp = new ObservableListContentProvider();
			viewer.setContentProvider(cp);
			viewer.setLabelProvider(new ComponentLabelProvider(editor));
			
			IEMFListProperty prop = EMFProperties.list(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN);
			viewer.setInput(prop.observeDetail(getMaster()));
			
			Composite buttonComp = new Composite(parent, SWT.NONE);
			buttonComp.setLayoutData(new GridData(GridData.FILL,GridData.END,false,false));
			GridLayout gl = new GridLayout(2,false);
			gl.marginLeft=0;
			gl.marginRight=0;
			gl.marginWidth=0;
			gl.marginHeight=0;
			buttonComp.setLayout(gl);

			Button b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText("Up");
			b.setImage(getImage(b.getDisplay(), ARROW_UP));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false,2,1));

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText("Down");
			b.setImage(getImage(b.getDisplay(), ARROW_DOWN));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false,2,1));
			
			final ComboViewer childrenDropDown = new ComboViewer(buttonComp);
			childrenDropDown.getControl().setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
			childrenDropDown.setContentProvider(new ArrayContentProvider());
			childrenDropDown.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element) {
					EClass eclass = (EClass) element;
					return eclass.getName();
				}
			});
			childrenDropDown.setInput(new EClass[] {
					MApplicationPackage.Literals.PART_SASH_CONTAINER,
					MApplicationPackage.Literals.PART_STACK,
					MApplicationPackage.Literals.PART
			});
			childrenDropDown.setSelection(new StructuredSelection(MApplicationPackage.Literals.PART_SASH_CONTAINER));
			
			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setImage(getImage(b.getDisplay(), TABLE_ADD_IMAGE));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if( ! childrenDropDown.getSelection().isEmpty() ) {
						EClass eClass = (EClass) ((IStructuredSelection)childrenDropDown.getSelection()).getFirstElement();
						EObject eObject = MApplicationFactory.eINSTANCE.create(eClass);
						
						Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(), MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN, eObject);
						
						if( cmd.canExecute() ) {
							getEditingDomain().getCommandStack().execute(cmd);
							editor.setSelection(eObject);
						}
					}
				}
			});
			
			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText("Remove");
			b.setImage(getImage(b.getDisplay(), TABLE_DELETE_IMAGE));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false,2,1));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if( ! viewer.getSelection().isEmpty() ) {
						List<?> elements = ((IStructuredSelection)viewer.getSelection()).toList();
						
						Command cmd = RemoveCommand.create(getEditingDomain(), getMaster().getValue(), MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN, elements);
						if( cmd.canExecute() ) {
							getEditingDomain().getCommandStack().execute(cmd);
						}
					}
				}
			});
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

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] {
			FeaturePath.fromList(MApplicationPackage.Literals.GENERIC_TILE__HORIZONTAL)	
		};
	}
}
