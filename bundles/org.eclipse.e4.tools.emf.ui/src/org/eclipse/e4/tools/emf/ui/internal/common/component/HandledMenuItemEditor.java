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
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.ObservableColumnLabelProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.HandledMenuItemCommandSelectionDialog;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class HandledMenuItemEditor extends MenuItemEditor {
	private Image image;
	private IModelResource resource;

	public HandledMenuItemEditor(EditingDomain editingDomain, IProject project, IModelResource resource) {
		super(editingDomain,project);
		this.resource = resource;
	}

	@Override
	public Image getImage(Object element, Display display) {
		if (image == null) {
			try {
				image = loadSharedImage(display, new URL("platform:/plugin/org.eclipse.e4.ui.model.workbench.edit/icons/full/obj16/HandledMenuItem.gif")); //$NON-NLS-1$
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return image;
	}

	@Override
	public String getLabel(Object element) {
		return Messages.HandledMenuItemEditor_Label;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.HandledMenuItemEditor_Description;
	}

	@Override
	protected void createFormSubTypeForm(Composite parent, EMFDataBindingContext context, final WritableValue master) {
		IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.HandledMenuItemEditor_Command);

			Text t = new Text(parent, SWT.BORDER);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			t.setEnabled(false);
			context.bindValue(textProp.observeDelayed(200, t), EMFEditProperties.value(getEditingDomain(), FeaturePath.fromList(MenuPackageImpl.Literals.HANDLED_ITEM__COMMAND, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID)).observeDetail(master));

			final Button b = new Button(parent, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.HandledMenuItemEditor_Find);
			b.setImage(getImage(b.getDisplay(), SEARCH_IMAGE));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					HandledMenuItemCommandSelectionDialog dialog = new HandledMenuItemCommandSelectionDialog(b.getShell(), (MHandledItem) getMaster().getValue(), resource);
					dialog.open();
				}
			});
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.HandledMenuItemEditor_Parameters);
			l.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));

			final TableViewer tableviewer = new TableViewer(parent);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.heightHint = 120;
			tableviewer.getTable().setHeaderVisible(true);
			tableviewer.getControl().setLayoutData(gd);

			ObservableListContentProvider cp = new ObservableListContentProvider();
			tableviewer.setContentProvider(cp);

			{
				IEMFValueProperty prop = EMFEditProperties.value(getEditingDomain(), CommandsPackageImpl.Literals.PARAMETER__NAME);

				TableViewerColumn column = new TableViewerColumn(tableviewer, SWT.NONE);
				column.getColumn().setText(Messages.HandledMenuItemEditor_Tag);
				column.getColumn().setWidth(200);
				column.setLabelProvider(new ObservableColumnLabelProvider<MParameter>(prop.observeDetail(cp.getKnownElements())));
				column.setEditingSupport(new EditingSupport(tableviewer) {
					private TextCellEditor cellEditor = new TextCellEditor(tableviewer.getTable());

					@Override
					protected void setValue(Object element, Object value) {
						Command cmd = SetCommand.create(getEditingDomain(), element, CommandsPackageImpl.Literals.PARAMETER__NAME, value);
						if (cmd.canExecute()) {
							getEditingDomain().getCommandStack().execute(cmd);
						}
					}

					@Override
					protected Object getValue(Object element) {
						String val = ((MParameter) element).getName();
						return val == null ? "" : val; //$NON-NLS-1$
					}

					@Override
					protected CellEditor getCellEditor(Object element) {
						return cellEditor;
					}

					@Override
					protected boolean canEdit(Object element) {
						return true;
					}
				});
			}

			{
				IEMFValueProperty prop = EMFEditProperties.value(getEditingDomain(), CommandsPackageImpl.Literals.PARAMETER__VALUE);

				TableViewerColumn column = new TableViewerColumn(tableviewer, SWT.NONE);
				column.getColumn().setText(Messages.HandledMenuItemEditor_Value);
				column.getColumn().setWidth(200);
				column.setLabelProvider(new ObservableColumnLabelProvider<MParameter>(prop.observeDetail(cp.getKnownElements())));
				column.setEditingSupport(new EditingSupport(tableviewer) {
					private TextCellEditor cellEditor = new TextCellEditor(tableviewer.getTable());

					@Override
					protected void setValue(Object element, Object value) {
						Command cmd = SetCommand.create(getEditingDomain(), element, CommandsPackageImpl.Literals.PARAMETER__VALUE, value);
						if (cmd.canExecute()) {
							getEditingDomain().getCommandStack().execute(cmd);
						}
					}

					@Override
					protected Object getValue(Object element) {
						String val = ((MParameter) element).getValue();
						return val == null ? "" : val; //$NON-NLS-1$
					}

					@Override
					protected CellEditor getCellEditor(Object element) {
						return cellEditor;
					}

					@Override
					protected boolean canEdit(Object element) {
						return true;
					}
				});
			}

			ColumnViewerEditorActivationStrategy editorActivationStrategy = new ColumnViewerEditorActivationStrategy(tableviewer) {
				@Override
				protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
					boolean singleSelect = ((IStructuredSelection) tableviewer.getSelection()).size() == 1;
					boolean isLeftDoubleMouseSelect = event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION && ((MouseEvent) event.sourceEvent).button == 1;

					return singleSelect && (isLeftDoubleMouseSelect || event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC || event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL);
				}
			};
			TableViewerEditor.create(tableviewer, editorActivationStrategy, ColumnViewerEditor.TABBING_HORIZONTAL | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR);

			IEMFEditListProperty prop = EMFEditProperties.list(getEditingDomain(), MenuPackageImpl.Literals.HANDLED_ITEM__PARAMETERS);
			tableviewer.setInput(prop.observeDetail(master));

			Composite buttonComp = new Composite(parent, SWT.NONE);
			buttonComp.setLayoutData(new GridData(GridData.FILL, GridData.END, false, false));
			GridLayout gl = new GridLayout();
			gl.marginLeft = 0;
			gl.marginRight = 0;
			gl.marginWidth = 0;
			gl.marginHeight = 0;
			buttonComp.setLayout(gl);

			Button b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.HandledMenuItemEditor_Up);
			b.setImage(getImage(b.getDisplay(), ARROW_UP));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.HandledMenuItemEditor_Down);
			b.setImage(getImage(b.getDisplay(), ARROW_DOWN));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.HandledMenuItemEditor_Add);
			b.setImage(getImage(b.getDisplay(), TABLE_ADD_IMAGE));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					MHandledItem item = (MHandledItem) master.getValue();
					MParameter param = MCommandsFactory.INSTANCE.createParameter();
					Command cmd = AddCommand.create(getEditingDomain(), item, MenuPackageImpl.Literals.HANDLED_ITEM__PARAMETERS, param);
					if (cmd.canExecute()) {
						getEditingDomain().getCommandStack().execute(cmd);
						tableviewer.editElement(param, 0);
					}
				}
			});

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.HandledMenuItemEditor_Remove);
			b.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent e) {
					IStructuredSelection s = (IStructuredSelection) tableviewer.getSelection();
					if (!s.isEmpty()) {
						MHandledItem item = (MHandledItem) master.getValue();
						Command cmd = RemoveCommand.create(getEditingDomain(), item, MenuPackageImpl.Literals.HANDLED_ITEM__PARAMETERS, s.toList());
						if (cmd.canExecute()) {
							getEditingDomain().getCommandStack().execute(cmd);
						}
					}
				}

			});
			b.setImage(getImage(b.getDisplay(), TABLE_DELETE_IMAGE));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		}
	}
}
