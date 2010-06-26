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
import org.eclipse.e4.tools.emf.ui.common.EStackLayout;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.ObservableColumnLabelProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandParameter;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.databinding.IEMFValueProperty;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.databinding.edit.IEMFEditListProperty;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class CommandEditor extends AbstractComponentEditor {

	private Composite composite;
	private Image image;
	private EMFDataBindingContext context;
	private EStackLayout stackLayout;

	public CommandEditor(EditingDomain editingDomain, ModelEditor editor) {
		super(editingDomain,editor);
	}

	@Override
	public Image getImage(Object element, Display display) {
		if (image == null) {
			try {
				image = loadSharedImage(display, new URL("platform:/plugin/org.eclipse.e4.tools.emf.ui/icons/full/modelelements/Command.gif")); //$NON-NLS-1$
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return image;
	}

	@Override
	public String getLabel(Object element) {
		return Messages.CommandEditor_Label;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.CommandEditor_Description;
	}

	@Override
	public Composite getEditor(Composite parent, Object object) {
		if (composite == null) {
			context = new EMFDataBindingContext();
			if (getEditor().isModelFragment()) {
				composite = new Composite(parent, SWT.NONE);
				stackLayout = new EStackLayout();
				composite.setLayout(stackLayout);
				createForm(composite, context, getMaster(), false);
				createForm(composite, context, getMaster(), true);
			} else {
				composite = createForm(parent, context, getMaster(), false);
			}
		}
		
		if( getEditor().isModelFragment() ) {
			Control topControl;
			if( Util.isImport((EObject) object) ) {
				topControl = composite.getChildren()[1];
			} else {
				topControl = composite.getChildren()[0];				
			}
			
			if( stackLayout.topControl != topControl ) {
				stackLayout.topControl = topControl;
				composite.layout(true, true);
			}
		}
		
		getMaster().setValue(object);
		return composite;
	}

	private Composite createForm(Composite parent, EMFDataBindingContext context, IObservableValue master, boolean isImport) {
		parent = new Composite(parent, SWT.NONE);
		parent.setLayout(new GridLayout(3, false));

		IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);
		
		if( isImport ) {
			ControlFactory.createFindImport(parent, this, context);
			
			return parent;
		}

		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.CommandEditor_Id);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID).observeDetail(getMaster()));
		}
		
		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.CommandEditor_Name);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), CommandsPackageImpl.Literals.COMMAND__COMMAND_NAME).observeDetail(getMaster()));
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.CommandEditor_LabelDescription);
			l.setLayoutData(new GridData(GridData.END,GridData.BEGINNING,false,false));

			Text t = new Text(parent, SWT.BORDER|SWT.H_SCROLL|SWT.V_SCROLL);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			gd.heightHint=100;
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), CommandsPackageImpl.Literals.COMMAND__DESCRIPTION).observeDetail(getMaster()));
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.CommandEditor_Parameters);
			l.setLayoutData(new GridData(GridData.END,GridData.BEGINNING,false,false));

			final TableViewer viewer = new TableViewer(parent,SWT.FULL_SELECTION|SWT.MULTI|SWT.BORDER);
			ObservableListContentProvider cp = new ObservableListContentProvider();
			viewer.setContentProvider(cp);
			viewer.getTable().setHeaderVisible(true);

			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.heightHint = 120;
			viewer.getControl().setLayoutData(gd);

			{
				IEMFValueProperty prop = EMFEditProperties.value(getEditingDomain(), ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID);
				
				TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
				column.getColumn().setText(Messages.CommandEditor_ParameterId);
				column.getColumn().setWidth(200);
				column.setLabelProvider(new ObservableColumnLabelProvider<MCommandParameter>(prop.observeDetail(cp.getKnownElements())));
				column.setEditingSupport(new EditingSupport(viewer) {
					private TextCellEditor editor = new TextCellEditor(viewer.getTable());
					
					@Override
					protected void setValue(Object element, Object value) {
						if( value.toString().trim().length() == 0 ) {
							value = null;
						}
						
						Command cmd = SetCommand.create(getEditingDomain(), element, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID, value);
						if( cmd.canExecute() ) {
							getEditingDomain().getCommandStack().execute(cmd);
						}
					}
					
					@Override
					protected Object getValue(Object element) {
						MCommandParameter obj = (MCommandParameter) element;
						return obj.getElementId() != null ? obj.getElementId() : ""; //$NON-NLS-1$
					}
					
					@Override
					protected CellEditor getCellEditor(Object element) {
						return editor;
					}
					
					@Override
					protected boolean canEdit(Object element) {
						return true;
					}
				});
			}
			
			{
				IEMFValueProperty prop = EMFEditProperties.value(getEditingDomain(), CommandsPackageImpl.Literals.COMMAND_PARAMETER__NAME);
				
				TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
				column.getColumn().setText(Messages.CommandEditor_ParameterName);
				column.getColumn().setWidth(200);
				column.setLabelProvider(new ObservableColumnLabelProvider<MCommandParameter>(prop.observeDetail(cp.getKnownElements())));
				column.setEditingSupport(new EditingSupport(viewer) {
					private TextCellEditor editor = new TextCellEditor(viewer.getTable());
					
					@Override
					protected void setValue(Object element, Object value) {
						Command cmd = SetCommand.create(getEditingDomain(), element, CommandsPackageImpl.Literals.COMMAND_PARAMETER__NAME, value);
						if( cmd.canExecute() ) {
							getEditingDomain().getCommandStack().execute(cmd);
						}
					}
					
					@Override
					protected Object getValue(Object element) {
						MCommandParameter obj = (MCommandParameter) element;
						return obj.getName() != null ? obj.getName() : ""; //$NON-NLS-1$
					}
					
					@Override
					protected CellEditor getCellEditor(Object element) {
						return editor;
					}
					
					@Override
					protected boolean canEdit(Object element) {
						return true;
					}
				});
			}

			{
				IEMFValueProperty prop = EMFEditProperties.value(getEditingDomain(), CommandsPackageImpl.Literals.COMMAND_PARAMETER__TYPE_ID);
				
				TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
				column.getColumn().setText(Messages.CommandEditor_ParameterTypeId);
				column.getColumn().setWidth(200);				
				column.setLabelProvider(new ObservableColumnLabelProvider<MCommandParameter>(prop.observeDetail(cp.getKnownElements())));
				column.setEditingSupport(new EditingSupport(viewer) {
					private TextCellEditor editor = new TextCellEditor(viewer.getTable());
					
					@Override
					protected void setValue(Object element, Object value) {
						Command cmd = SetCommand.create(getEditingDomain(), element, CommandsPackageImpl.Literals.COMMAND_PARAMETER__TYPE_ID, value);
						if( cmd.canExecute() ) {
							getEditingDomain().getCommandStack().execute(cmd);
						}
					}
					
					@Override
					protected Object getValue(Object element) {
						MCommandParameter obj = (MCommandParameter) element;
						return obj.getTypeId() != null ? obj.getTypeId() : ""; //$NON-NLS-1$
					}
					
					@Override
					protected CellEditor getCellEditor(Object element) {
						return editor;
					}
					
					@Override
					protected boolean canEdit(Object element) {
						return true;
					}
				});
			}

			{
				IEMFValueProperty prop = EMFEditProperties.value(getEditingDomain(), CommandsPackageImpl.Literals.COMMAND_PARAMETER__OPTIONAL);
				
				TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
				column.getColumn().setText(Messages.CommandEditor_ParameterOptional);
				column.getColumn().setWidth(200);				
				column.setLabelProvider(new ObservableColumnLabelProvider<MCommandParameter>(prop.observeDetail(cp.getKnownElements())) {
					@Override
					public String getText(MCommandParameter element) {
						return element.isOptional() ? Messages.CommandEditor_ParameterOptional_Yes : Messages.CommandEditor_ParameterOptional_No;
					}
				});
				column.setEditingSupport(new EditingSupport(viewer) {
					private ComboBoxCellEditor editor = new ComboBoxCellEditor(viewer.getTable(), new String[] { Messages.CommandEditor_ParameterOptional_Yes, Messages.CommandEditor_ParameterOptional_No });
					
					@Override
					protected void setValue(Object element, Object value) {
						int idx = ((Number)value).intValue();
						Command cmd = SetCommand.create(getEditingDomain(), element, CommandsPackageImpl.Literals.COMMAND_PARAMETER__OPTIONAL, idx == 0);
						if( cmd.canExecute() ) {
							getEditingDomain().getCommandStack().execute(cmd);
						}
					}
					
					@Override
					protected Object getValue(Object element) {
						MCommandParameter obj = (MCommandParameter) element;
						return obj.isOptional() ? 0 : 1;
					}
					
					@Override
					protected CellEditor getCellEditor(Object element) {
						return editor;
					}
					
					@Override
					protected boolean canEdit(Object element) {
						return true;
					}
				});
			}

			ColumnViewerEditorActivationStrategy editorActivationStrategy = new ColumnViewerEditorActivationStrategy(viewer) {
				@Override
				protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
					boolean singleSelect = ((IStructuredSelection)viewer.getSelection()).size() == 1;
					boolean isLeftDoubleMouseSelect = event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION && ((MouseEvent)event.sourceEvent).button == 1;

					return singleSelect && (isLeftDoubleMouseSelect
							|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC
							|| event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL);
				}
			};
			TableViewerEditor.create(viewer, editorActivationStrategy, ColumnViewerEditor.TABBING_HORIZONTAL | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR);

			
			IEMFEditListProperty mProp = EMFEditProperties.list(getEditingDomain(), CommandsPackageImpl.Literals.COMMAND__PARAMETERS);
			viewer.setInput(mProp.observeDetail(getMaster()));

			Composite buttonComp = new Composite(parent, SWT.NONE);
			buttonComp.setLayoutData(new GridData(GridData.FILL,GridData.END,false,false));
			GridLayout gl = new GridLayout();
			gl.marginLeft=0;
			gl.marginRight=0;
			gl.marginWidth=0;
			gl.marginHeight=0;
			buttonComp.setLayout(gl);

			Button b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.CommandEditor_Up);
			b.setImage(getImage(b.getDisplay(), ARROW_UP));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.CommandEditor_Down);
			b.setImage(getImage(b.getDisplay(), ARROW_DOWN));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.CommandEditor_Add);
			b.setImage(getImage(b.getDisplay(), TABLE_ADD_IMAGE));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					MCommandParameter param = MCommandsFactory.INSTANCE.createCommandParameter();
					Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(), CommandsPackageImpl.Literals.COMMAND__PARAMETERS, param);
					
					if( cmd.canExecute() ) {
						getEditingDomain().getCommandStack().execute(cmd);
						viewer.editElement(param, 0);
					}
				}
			});

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.CommandEditor_Remove);
			b.setImage(getImage(b.getDisplay(), TABLE_DELETE_IMAGE));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		}

		ControlFactory.createTagsWidget(parent, this);

		return parent;
	}

	@Override
	public IObservableList getChildList(Object element) {
		return null;
	}

	@Override
	public String getDetailLabel(Object element) {
		MCommand cmd = (MCommand) element;
		if (cmd.getCommandName() != null && cmd.getCommandName().trim().length() > 0) {
			return cmd.getCommandName();
		}

		return null;
	}
	
	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] {
			FeaturePath.fromList(CommandsPackageImpl.Literals.COMMAND__COMMAND_NAME)	
		};
	}

}
