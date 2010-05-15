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
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.ContributionClassDialog;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.MenuIconDialogEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.PartDescriptorIconDialogEditor;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.descriptor.basic.util.BasicAdapterFactory;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
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

public class PartDescriptorEditor extends AbstractComponentEditor {

	private Composite composite;
	private Image image;
	private EMFDataBindingContext context;
	private IProject project;
	
	private IListProperty PART__MENUS = EMFProperties.list(BasicPackageImpl.Literals.PART_DESCRIPTOR__MENUS);
	private IListProperty HANDLER_CONTAINER__HANDLERS = EMFProperties.list(CommandsPackageImpl.Literals.HANDLER_CONTAINER__HANDLERS);

	public PartDescriptorEditor(EditingDomain editingDomain, IProject project) {
		super(editingDomain);
		this.project = project;
	}

	@Override
	public Image getImage(Object element, Display display) {
		if( image == null ) {
			try {
				image = loadSharedImage(display, new URL("platform:/plugin/org.eclipse.e4.ui.model.workbench.edit/icons/full/obj16/Part.gif")); //$NON-NLS-1$
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return image;
	}

	@Override
	public String getLabel(Object element) {
		return Messages.PartDescriptorEditor_Label;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.PartDescriptorEditor_Descriptor;
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
			l.setText(Messages.PartDescriptorEditor_Id);

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan=2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID).observeDetail(master));			
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.PartDescriptorEditor_LabelLabel);

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan=2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_LABEL__LABEL).observeDetail(master));			
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.PartDescriptorEditor_Tooltip);

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan=2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_LABEL__TOOLTIP).observeDetail(master));			
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.PartDescriptorEditor_IconURI);

			Text t = new Text(parent, SWT.BORDER);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_LABEL__ICON_URI).observeDetail(master));

			final Button b = new Button(parent, SWT.PUSH|SWT.FLAT);
			b.setImage(getImage(t.getDisplay(), SEARCH_IMAGE));
			b.setText(Messages.PartDescriptorEditor_Find);
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					PartDescriptorIconDialogEditor dialog = new PartDescriptorIconDialogEditor(b.getShell(), project, getEditingDomain(),(MPartDescriptor) getMaster().getValue());
					dialog.open();
				}
			});	
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.PartDescriptorEditor_ClassURI);

			Text t = new Text(parent, SWT.BORDER);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), BasicPackageImpl.Literals.PART_DESCRIPTOR__CONTRIBUTION_URI).observeDetail(master));

			final Button b = new Button(parent, SWT.PUSH|SWT.FLAT);
			b.setImage(getImage(t.getDisplay(), SEARCH_IMAGE));
			b.setText(Messages.PartDescriptorEditor_Find);
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ContributionClassDialog dialog = new ContributionClassDialog(b.getShell(),project,getEditingDomain(),(MPartDescriptor) getMaster().getValue(), BasicPackageImpl.Literals.PART_DESCRIPTOR__CONTRIBUTION_URI);
					dialog.open();
				}
			});
		}
		
		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.PartDescriptorEditor_ContainerData);

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan=2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_ELEMENT__CONTAINER_DATA).observeDetail(master));
		}
		
		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.PartDescriptorEditor_Dirtyable);

			Button checkbox = new Button(parent, SWT.CHECK);
			checkbox.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 2, 1));
			
			IEMFEditValueProperty mprop = EMFEditProperties.value(getEditingDomain(), BasicPackageImpl.Literals.PART_DESCRIPTOR__DIRTYABLE);
			IWidgetValueProperty uiProp = WidgetProperties.selection();
			
			context.bindValue(uiProp.observe(checkbox), mprop.observeDetail(master));
		}

		
		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.PartDescriptorEditor_Closeable);

			Button checkbox = new Button(parent, SWT.CHECK);
			checkbox.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 2, 1));
			
			IEMFEditValueProperty mprop = EMFEditProperties.value(getEditingDomain(), BasicPackageImpl.Literals.PART_DESCRIPTOR__CLOSEABLE);
			IWidgetValueProperty uiProp = WidgetProperties.selection();
			
			context.bindValue(uiProp.observe(checkbox), mprop.observeDetail(master));
		}
		
		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.PartDescriptorEditor_Multiple);

			Button checkbox = new Button(parent, SWT.CHECK);
			checkbox.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 2, 1));
			
			IEMFEditValueProperty mprop = EMFEditProperties.value(getEditingDomain(), BasicPackageImpl.Literals.PART_DESCRIPTOR__ALLOW_MULTIPLE);
			IWidgetValueProperty uiProp = WidgetProperties.selection();
			
			context.bindValue(uiProp.observe(checkbox), mprop.observeDetail(master));
		}
		
		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.PartDescriptorEditor_Category);
			
			Text t = new Text(parent, SWT.BORDER);
			t.setLayoutData(new GridData(GridData.FILL,GridData.BEGINNING, true, false, 2, 1));
			context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), BasicPackageImpl.Literals.PART_DESCRIPTOR__CATEGORY).observeDetail(master));
		}
		
		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.PartDescriptorEditor_PersitedState);
			l.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));

			TableViewer tableviewer = new TableViewer(parent);
			tableviewer.getTable().setHeaderVisible(true);
			ObservableListContentProvider cp = new ObservableListContentProvider();
			tableviewer.setContentProvider(cp);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.heightHint = 80;
			tableviewer.getControl().setLayoutData(gd);
			
			TableViewerColumn column = new TableViewerColumn(tableviewer, SWT.NONE);
			column.getColumn().setText(Messages.PartDescriptorEditor_PersitedStateKey);
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
			column.getColumn().setText(Messages.PartDescriptorEditor_PersitedStateValue);
			column.getColumn().setWidth(200);
			column.setLabelProvider(new ColumnLabelProvider() {
				@SuppressWarnings("unchecked")
				@Override
				public String getText(Object element) {
					Entry<String, String> entry = (Entry<String, String>) element;
					return entry.getValue();
				}
			});
			
			IEMFEditListProperty prop = EMFEditProperties.list(getEditingDomain(), ApplicationPackageImpl.Literals.CONTRIBUTION__PERSISTED_STATE);
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
			b.setText(Messages.PartDescriptorEditor_Add);
			b.setImage(getImage(b.getDisplay(), TABLE_ADD_IMAGE));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.PartDescriptorEditor_Remove);
			b.setImage(getImage(b.getDisplay(), TABLE_DELETE_IMAGE));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		}
		
		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.PartDescriptorEditor_Variables);
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
			l.setText(Messages.PartDescriptorEditor_Properties);
			l.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));

			TableViewer tableviewer = new TableViewer(parent);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan=2;
			gd.heightHint = 80;
			tableviewer.getTable().setHeaderVisible(true);
			tableviewer.getControl().setLayoutData(gd);

			TableViewerColumn column = new TableViewerColumn(tableviewer, SWT.NONE);
			column.getColumn().setText(Messages.PartDescriptorEditor_PropertiesKey);
			column.getColumn().setWidth(200);

			column = new TableViewerColumn(tableviewer, SWT.NONE);
			column.getColumn().setText(Messages.PartDescriptorEditor_PropertiesValue);
			column.getColumn().setWidth(200);
		}


		ControlFactory.createTagsWidget(parent, this);

		return parent;
	}

	@Override
	public IObservableList getChildList(Object element) {
		WritableList list = new WritableList();
		list.add(new VirtualEntry<Object>( ModelEditor.VIRTUAL_PARTDESCRIPTOR_MENU, PART__MENUS, element, Messages.PartDescriptorEditor_Menus) {

			@Override
			protected boolean accepted(Object o) {
				return true;
			}

		});

		list.add(new VirtualEntry<Object>( ModelEditor.VIRTUAL_HANDLER, HANDLER_CONTAINER__HANDLERS, element, Messages.PartDescriptorEditor_Handlers) {

			@Override
			protected boolean accepted(Object o) {
				return true;
			}

		});

		return list;
	}

	@Override
	public String getDetailLabel(Object element) {
		MPartDescriptor o = (MPartDescriptor) element;
		return o.getLabel();
	}

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] {
			FeaturePath.fromList(UiPackageImpl.Literals.UI_LABEL__LABEL)	
		};
	}
}