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
import java.util.Map.Entry;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.databinding.edit.IEMFEditListProperty;
import org.eclipse.emf.databinding.edit.IEMFEditValueProperty;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
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
	private Image image;
	private EMFDataBindingContext context;

	private IListProperty PART__MENUS = EMFProperties.list(MApplicationPackage.Literals.PART__MENUS);
	private IListProperty HANDLER_CONTAINER__HANDLERS = EMFProperties.list(MApplicationPackage.Literals.HANDLER_CONTAINER__HANDLERS);
	private IListProperty BINDING_CONTAINER__BINDINGS = EMFProperties.list(MApplicationPackage.Literals.BINDING_CONTAINER__BINDINGS);

	public PartEditor(EditingDomain editingDomain) {
		super(editingDomain);
	}

	@Override
	public Image getImage(Object element, Display display) {
		if( image == null ) {
			try {
				image = loadSharedImage(display, new URL("platform:/plugin/org.eclipse.e4.ui.model.workbench.edit/icons/full/obj16/Part.gif"));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return image;
	}

	@Override
	public String getLabel(Object element) {
		return "Part";
	}

	@Override
	public String getDescription(Object element) {
		return "Part Bla Bla Bla Bla";
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

	protected Composite createForm(Composite parent, EMFDataBindingContext context, IObservableValue master) {
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
			context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.APPLICATION_ELEMENT__ID).observeDetail(master));			
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Label");

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan=2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.UI_LABEL__LABEL).observeDetail(master));			
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Tooltip");

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan=2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.UI_LABEL__TOOLTIP).observeDetail(master));			
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Icon URI");

			Text t = new Text(parent, SWT.BORDER);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.UI_LABEL__ICON_URI).observeDetail(master));

			Button b = new Button(parent, SWT.PUSH|SWT.FLAT);
			b.setImage(getImage(t.getDisplay(), SEARCH_IMAGE));
			b.setText("Find ...");			
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Class URI");

			Text t = new Text(parent, SWT.BORDER);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.CONTRIBUTION__URI).observeDetail(master));

			Button b = new Button(parent, SWT.PUSH|SWT.FLAT);
			b.setImage(getImage(t.getDisplay(), SEARCH_IMAGE));
			b.setText("Find ...");			
		}
		
		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Closeable");

			Button checkbox = new Button(parent, SWT.CHECK);
			checkbox.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 2, 1));
			
			IEMFEditValueProperty mprop = EMFEditProperties.value(getEditingDomain(), MApplicationPackage.Literals.PART__CLOSEABLE);
			IWidgetValueProperty uiProp = WidgetProperties.selection();
			
			context.bindValue(uiProp.observe(checkbox), mprop.observeDetail(master));
		}

		
		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Persited State");
			l.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));

			TableViewer tableviewer = new TableViewer(parent);
			tableviewer.getTable().setHeaderVisible(true);
			ObservableListContentProvider cp = new ObservableListContentProvider();
			tableviewer.setContentProvider(cp);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.heightHint = 80;
			tableviewer.getControl().setLayoutData(gd);
			
			TableViewerColumn column = new TableViewerColumn(tableviewer, SWT.NONE);
			column.getColumn().setText("Key");
			column.getColumn().setWidth(200);
			column.setLabelProvider(new ColumnLabelProvider() {
				@SuppressWarnings("unchecked")
				@Override
				public String getText(Object element) {
					Entry<String, String> entry = (Entry<String, String>) element;
					return entry.getKey();
				}
			}); 

			//FIXME How can we react upon changes in the Map-Value?
			column = new TableViewerColumn(tableviewer, SWT.NONE);
			column.getColumn().setText("Value");
			column.getColumn().setWidth(200);
			column.setLabelProvider(new ColumnLabelProvider() {
				@SuppressWarnings("unchecked")
				@Override
				public String getText(Object element) {
					Entry<String, String> entry = (Entry<String, String>) element;
					return entry.getValue();
				}
			});
			
			IEMFEditListProperty prop = EMFEditProperties.list(getEditingDomain(), MApplicationPackage.Literals.CONTRIBUTION__PERSISTED_STATE);
			tableviewer.setInput(prop.observeDetail(getMaster()));
			
			Composite buttonComp = new Composite(parent, SWT.NONE);
			buttonComp.setLayoutData(new GridData(GridData.FILL,GridData.END,false,false));
			GridLayout gl = new GridLayout();
			gl.marginLeft=0;
			gl.marginRight=0;
			gl.marginWidth=0;
			gl.marginHeight=0;
			buttonComp.setLayout(gl);

			Button b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText("Add ...");
			b.setImage(getImage(b.getDisplay(), TABLE_ADD_IMAGE));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText("Remove");
			b.setImage(getImage(b.getDisplay(), TABLE_DELETE_IMAGE));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		}


		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Variables");
			l.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));

			ListViewer viewer = new ListViewer(parent);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan=2;
			gd.heightHint = 80;
			viewer.getList().setLayoutData(gd);
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Properties");
			l.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));

			TableViewer tableviewer = new TableViewer(parent);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
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
		}


		ControlFactory.createTagsWidget(parent, this);

//		// ------------------------------------------------------------
//
//		l = new Label(parent, SWT.NONE);
//		l.setText("");
//
//		Composite booleanContainer = new Composite(parent,SWT.NONE);
//		gd = new GridData(GridData.FILL_HORIZONTAL);
//		gd.horizontalSpan=2;
//		booleanContainer.setBackgroundMode(SWT.INHERIT_DEFAULT);
//		booleanContainer.setLayoutData(gd);
//		booleanContainer.setLayout(new GridLayout(4,false));
//
//		Button checkbox = new Button(booleanContainer, SWT.CHECK);
//		checkbox.setText("to render");
//
//		checkbox = new Button(booleanContainer, SWT.CHECK);
//		checkbox.setText("on Top");
//
//		checkbox = new Button(booleanContainer, SWT.CHECK);
//		checkbox.setText("visible");
//
//		checkbox = new Button(booleanContainer, SWT.CHECK);
//		checkbox.setText("closeable");

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

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] {
			FeaturePath.fromList(MApplicationPackage.Literals.UI_LABEL__LABEL)	
		};
	}
}
