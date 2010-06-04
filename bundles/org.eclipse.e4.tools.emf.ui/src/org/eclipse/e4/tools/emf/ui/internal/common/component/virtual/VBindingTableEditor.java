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
package org.eclipse.e4.tools.emf.ui.internal.common.component.virtual;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.ObservableColumnLabelProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.ComponentLabelProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MBindingTableContainer;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

public class VBindingTableEditor extends AbstractComponentEditor {
	private Composite composite;
	private EMFDataBindingContext context;
	private ModelEditor editor;
	private TableViewer bindingViewer;
	private TreeViewer contextsViewer;

	public VBindingTableEditor(EditingDomain editingDomain, ModelEditor editor) {
		super(editingDomain);
		this.editor = editor;
	}

	@Override
	public Image getImage(Object element, Display display) {
		return null;
	}

	@Override
	public String getLabel(Object element) {
		return Messages.VBindingTableEditor_Label;
	}

	@Override
	public String getDetailLabel(Object element) {
		return null;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.VBindingTableEditor_Description;
	}

	@Override
	public Composite getEditor(Composite parent, Object object) {
		if (composite == null) {
			context = new EMFDataBindingContext();
			composite = createForm(parent, context, getMaster());
		}
		VirtualEntry<?> o = (VirtualEntry<?>) object;
		bindingViewer.setInput(o.getList());
		getMaster().setValue(o.getOriginalParent());
		return composite;
	}

	private Composite createForm(Composite parent, EMFDataBindingContext context, WritableValue master) {
		parent = new Composite(parent, SWT.NONE);
		parent.setLayout(new GridLayout(3, false));

		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.VBindingTableEditor_Contexts);
			l.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

			contextsViewer = new TreeViewer(parent);
			ObservableListTreeContentProvider pv = new ObservableListTreeContentProvider(new ObservableFactoryImpl(), new TreeStructureAdvisorImpl());
			contextsViewer.setContentProvider(pv);
			contextsViewer.getTree().setHeaderVisible(true);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.heightHint = 200;
			contextsViewer.getControl().setLayoutData(gd);
			
			final WritableList list = new WritableList();
			
			IEMFValueProperty listProp = EMFProperties.value(CommandsPackageImpl.Literals.BINDING_TABLE_CONTAINER__ROOT_CONTEXT);
			IObservableValue val = listProp.observeDetail(getMaster());
			val.addValueChangeListener(new IValueChangeListener() {
				
				public void handleValueChange(ValueChangeEvent event) {
					list.clear();
					MBindingContext ctx = (MBindingContext) event.getObservableValue().getValue();
					if( ctx != null ) {
						list.add(ctx);	
					}
				}
			});
			
			
			contextsViewer.setInput(list);
			
			
			{
				IEMFValueProperty prop = EMFProperties.value(CommandsPackageImpl.Literals.BINDING_CONTEXT__NAME);
				
				TreeViewerColumn column = new TreeViewerColumn(contextsViewer, SWT.NONE);
				column.getColumn().setText(Messages.VBindingTableEditor_Name);
				column.getColumn().setWidth(200);				
				column.setLabelProvider(new ObservableColumnLabelProvider<MBindingContext>(prop.observeDetail(pv.getKnownElements())));
				column.setEditingSupport(new EditingSupport(contextsViewer) {
					private TextCellEditor editor = new TextCellEditor(contextsViewer.getTree());
					
					@Override
					protected void setValue(Object element, Object value) {
						Command cmd = SetCommand.create(getEditingDomain(), element, CommandsPackageImpl.Literals.BINDING_CONTEXT__NAME, value);
						if( cmd.canExecute() ) {
							getEditingDomain().getCommandStack().execute(cmd);
						}
					}
					
					@Override
					protected Object getValue(Object element) {
						MBindingContext ctx = (MBindingContext) element; 
						return ctx.getName() != null ? ctx.getName() : ""; //$NON-NLS-1$
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
				IEMFValueProperty prop = EMFProperties.value(CommandsPackageImpl.Literals.BINDING_CONTEXT__DESCRIPTION);
				
				TreeViewerColumn column = new TreeViewerColumn(contextsViewer, SWT.NONE);
				column.getColumn().setText(Messages.VBindingTableEditor_LabelDescription);
				column.getColumn().setWidth(200);				
				column.setLabelProvider(new ObservableColumnLabelProvider<MBindingContext>(prop.observeDetail(pv.getKnownElements())));				
				column.setEditingSupport(new EditingSupport(contextsViewer) {
					private TextCellEditor editor = new TextCellEditor(contextsViewer.getTree());
					
					@Override
					protected void setValue(Object element, Object value) {
						Command cmd = SetCommand.create(getEditingDomain(), element, CommandsPackageImpl.Literals.BINDING_CONTEXT__DESCRIPTION, value);
						if( cmd.canExecute() ) {
							getEditingDomain().getCommandStack().execute(cmd);
						}
					}
					
					@Override
					protected Object getValue(Object element) {
						MBindingContext ctx = (MBindingContext) element; 
						return ctx.getDescription() != null ? ctx.getDescription() : ""; //$NON-NLS-1$
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
				IEMFValueProperty prop = EMFProperties.value(ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID);
				
				TreeViewerColumn column = new TreeViewerColumn(contextsViewer, SWT.NONE);
				column.getColumn().setText(Messages.VBindingTableEditor_Id);
				column.getColumn().setWidth(200);
				column.setLabelProvider(new ObservableColumnLabelProvider<MBindingContext>(prop.observeDetail(pv.getKnownElements())));				
				column.setEditingSupport(new EditingSupport(contextsViewer) {
					private TextCellEditor editor = new TextCellEditor(contextsViewer.getTree());
					
					@Override
					protected void setValue(Object element, Object value) {
						Command cmd = SetCommand.create(getEditingDomain(), element, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID, value);
						if( cmd.canExecute() ) {
							getEditingDomain().getCommandStack().execute(cmd);
						}
					}
					
					@Override
					protected Object getValue(Object element) {
						MBindingContext ctx = (MBindingContext) element; 
						return ctx.getElementId() != null ? ctx.getElementId() : ""; //$NON-NLS-1$
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
			
			ColumnViewerEditorActivationStrategy editorActivationStrategy = new ColumnViewerEditorActivationStrategy(contextsViewer) {
				@Override
				protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
					boolean singleSelect = ((IStructuredSelection)contextsViewer.getSelection()).size() == 1;
					boolean isLeftDoubleMouseSelect = event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION && ((MouseEvent)event.sourceEvent).button == 1;

					return singleSelect && (isLeftDoubleMouseSelect
							|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC
							|| event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL);
				}
			};
			TreeViewerEditor.create(contextsViewer, editorActivationStrategy, ColumnViewerEditor.TABBING_HORIZONTAL | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR | ColumnViewerEditor.KEEP_EDITOR_ON_DOUBLE_CLICK);

			
			Composite buttonComp = new Composite(parent, SWT.NONE);
			buttonComp.setLayoutData(new GridData(GridData.FILL, GridData.END, false, false));
			GridLayout gl = new GridLayout();
			gl.marginLeft = 0;
			gl.marginRight = 0;
			gl.marginWidth = 0;
			gl.marginHeight = 0;
			buttonComp.setLayout(gl);
			
			Button b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.VBindingTableEditor_Up);
			b.setImage(getImage(b.getDisplay(), ARROW_UP));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
			
			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.VBindingTableEditor_Down);
			b.setImage(getImage(b.getDisplay(), ARROW_DOWN));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
			
			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.VBindingTableEditor_Add);
			b.setImage(getImage(b.getDisplay(), TABLE_ADD_IMAGE));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					IStructuredSelection s = (IStructuredSelection) contextsViewer.getSelection();
					MBindingContext context = MCommandsFactory.INSTANCE.createBindingContext();
					MBindingContext parentContext = null;
					
					if( ! s.isEmpty() ) {
						parentContext = (MBindingContext) s.getFirstElement();
						Command cmd = AddCommand.create(getEditingDomain(), parentContext, CommandsPackageImpl.Literals.BINDING_CONTEXT__CHILDREN, context);
						
						if( cmd.canExecute() ) {
							getEditingDomain().getCommandStack().execute(cmd);
							contextsViewer.setSelection(new StructuredSelection(context));
						}
					} else if( s.isEmpty() && ((MBindingTableContainer)getMaster().getValue()).getRootContext() == null ) {
						Command cmd = SetCommand.create(getEditingDomain(), getMaster().getValue(), CommandsPackageImpl.Literals.BINDING_TABLE_CONTAINER__ROOT_CONTEXT, context);
						if( cmd.canExecute() ) {
							getEditingDomain().getCommandStack().execute(cmd);
							contextsViewer.setSelection(new StructuredSelection(context));
						}
					}
					
					
				}
			});
			
			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.VBindingTableEditor_Remove);
			b.setImage(getImage(b.getDisplay(), TABLE_DELETE_IMAGE));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					IStructuredSelection s = (IStructuredSelection) contextsViewer.getSelection();
					if( ! s.isEmpty() ) {
						List<Command> commands = new ArrayList<Command>();
						
						for( Object o : s.toArray() ) {
							MBindingContext ctx = (MBindingContext) o;
							EObject owner = ((EObject)ctx).eContainer();
							if( owner instanceof MBindingTableContainer ) {
								Command cmd = SetCommand.create(getEditingDomain(), owner, CommandsPackageImpl.Literals.BINDING_TABLE_CONTAINER__ROOT_CONTEXT, null);
								if( cmd.canExecute() ) {
									getEditingDomain().getCommandStack().execute(cmd);
									return;
								}
							} else {
								commands.add(RemoveCommand.create(getEditingDomain(), owner, CommandsPackageImpl.Literals.BINDING_CONTEXT__CHILDREN, ctx));
							}
						}
						
						CompoundCommand cmd = new CompoundCommand(commands);
						if( cmd.canExecute() ) {
							getEditingDomain().getCommandStack().execute(cmd);
						}
					}
				}
			});
		}

		{
			Label l = new Label(parent, SWT.NONE);
			l.setText("Binding Tables");
			l.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

			bindingViewer = new TableViewer(parent);
			ObservableListContentProvider cp = new ObservableListContentProvider();
			bindingViewer.setContentProvider(cp);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.heightHint = 180;
			bindingViewer.getControl().setLayoutData(gd);
			bindingViewer.setLabelProvider(new ComponentLabelProvider(editor));

			Composite buttonComp = new Composite(parent, SWT.NONE);
			buttonComp.setLayoutData(new GridData(GridData.FILL, GridData.END, false, false));
			GridLayout gl = new GridLayout();
			gl.marginLeft = 0;
			gl.marginRight = 0;
			gl.marginWidth = 0;
			gl.marginHeight = 0;
			buttonComp.setLayout(gl);

			Button b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText("Up");
			b.setImage(getImage(b.getDisplay(), ARROW_UP));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!bindingViewer.getSelection().isEmpty()) {
						IStructuredSelection s = (IStructuredSelection) bindingViewer.getSelection();
						if (s.size() == 1) {
							Object obj = s.getFirstElement();
							MBindingTableContainer container = (MBindingTableContainer) getMaster().getValue();
							int idx = container.getBindingTables().indexOf(obj) - 1;
							if (idx >= 0) {
								Command cmd = MoveCommand.create(getEditingDomain(), getMaster().getValue(), CommandsPackageImpl.Literals.BINDING_TABLE_CONTAINER__BINDING_TABLES, obj, idx);

								if (cmd.canExecute()) {
									getEditingDomain().getCommandStack().execute(cmd);
									bindingViewer.setSelection(new StructuredSelection(obj));
								}
							}

						}
					}
				}
			});

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText("Down");
			b.setImage(getImage(b.getDisplay(), ARROW_DOWN));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!bindingViewer.getSelection().isEmpty()) {
						IStructuredSelection s = (IStructuredSelection) bindingViewer.getSelection();
						if (s.size() == 1) {
							Object obj = s.getFirstElement();
							MBindingTableContainer container = (MBindingTableContainer) getMaster().getValue();
							int idx = container.getBindingTables().indexOf(obj) + 1;
							if (idx < container.getBindingTables().size()) {
								Command cmd = MoveCommand.create(getEditingDomain(), getMaster().getValue(), CommandsPackageImpl.Literals.BINDING_TABLE_CONTAINER__BINDING_TABLES, obj, idx);

								if (cmd.canExecute()) {
									getEditingDomain().getCommandStack().execute(cmd);
									bindingViewer.setSelection(new StructuredSelection(obj));
								}
							}

						}
					}
				}
			});

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText("Add ...");
			b.setImage(getImage(b.getDisplay(), TABLE_ADD_IMAGE));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					MBindingTable command = MCommandsFactory.INSTANCE.createBindingTable();
					Command cmd = AddCommand.create(getEditingDomain(), getMaster().getValue(), CommandsPackageImpl.Literals.BINDING_TABLE_CONTAINER__BINDING_TABLES, command);

					if (cmd.canExecute()) {
						getEditingDomain().getCommandStack().execute(cmd);
						editor.setSelection(command);
					}
				}
			});

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText("Remove");
			b.setImage(getImage(b.getDisplay(), TABLE_DELETE_IMAGE));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!bindingViewer.getSelection().isEmpty()) {
						List<?> commands = ((IStructuredSelection) bindingViewer.getSelection()).toList();
						Command cmd = RemoveCommand.create(getEditingDomain(), getMaster().getValue(), CommandsPackageImpl.Literals.BINDING_TABLE_CONTAINER__BINDING_TABLES, commands);
						if (cmd.canExecute()) {
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
		// TODO Auto-generated method stub
		return null;
	}

	private static class ObservableFactoryImpl implements IObservableFactory {
		private IEMFListProperty prop = EMFProperties.list(CommandsPackageImpl.Literals.BINDING_CONTEXT__CHILDREN);
		
		public IObservable createObservable(Object target) {
			if( target instanceof IObservableList ) {
				return (IObservable) target;
			}
			return prop.observe(target);
		}
	}
	
	private static class TreeStructureAdvisorImpl extends TreeStructureAdvisor {
		
	}
}
