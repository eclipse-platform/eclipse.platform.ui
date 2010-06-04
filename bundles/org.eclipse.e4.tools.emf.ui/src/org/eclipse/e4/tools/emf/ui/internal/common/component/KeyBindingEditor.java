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
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.KeyBindingCommandSelectionDialog;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.e4.ui.model.application.commands.MKeySequence;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
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

public class KeyBindingEditor extends AbstractComponentEditor {

	private Composite composite;
	private Image image;
	private EMFDataBindingContext context;
	private IModelResource resource;

	public KeyBindingEditor(EditingDomain editingDomain, IModelResource resource) {
		super(editingDomain);
		this.resource = resource;
	}

	@Override
	public Image getImage(Object element, Display display) {
		if (image == null) {
			try {
				image = loadSharedImage(display, new URL("platform:/plugin/org.eclipse.e4.ui.model.workbench.edit/icons/full/obj16/KeyBinding.gif")); //$NON-NLS-1$
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return image;
	}

	@Override
	public String getLabel(Object element) {
		return Messages.KeyBindingEditor_Label;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.KeyBindingEditor_Description;
	}

	@Override
	public Composite getEditor(Composite parent, Object object) {
		if (composite == null) {
			context = new EMFDataBindingContext();
			composite = createForm(parent, context);
		}
		getMaster().setValue(object);
		return composite;
	}

	private Composite createForm(Composite parent, EMFDataBindingContext context) {
		parent = new Composite(parent, SWT.NONE);
		parent.setLayout(new GridLayout(3, false));

		IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.KeyBindingEditor_Id);

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200, t), EMFEditProperties.value(getEditingDomain(), ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID).observeDetail(getMaster()));
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.KeyBindingEditor_Sequence);

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200, t), EMFEditProperties.value(getEditingDomain(), CommandsPackageImpl.Literals.KEY_SEQUENCE__KEY_SEQUENCE).observeDetail(getMaster()));
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.KeyBindingEditor_Command);

			Text t = new Text(parent, SWT.BORDER);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			t.setEnabled(false);
			context.bindValue(textProp.observeDelayed(200, t), EMFEditProperties.value(getEditingDomain(), FeaturePath.fromList(CommandsPackageImpl.Literals.KEY_BINDING__COMMAND, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID)).observeDetail(getMaster()));

			final Button b = new Button(parent, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.KeyBindingEditor_Find);
			b.setImage(getImage(b.getDisplay(), SEARCH_IMAGE));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					KeyBindingCommandSelectionDialog dialog = new KeyBindingCommandSelectionDialog(b.getShell(), (MKeyBinding) getMaster().getValue(), resource);
					dialog.open();
				}
			});
		}

		Label l = new Label(parent, SWT.NONE);
		l.setText(Messages.KeyBindingEditor_Parameters);
		l.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));

		final TableViewer tableviewer = new TableViewer(parent);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 120;
		tableviewer.getTable().setHeaderVisible(true);
		tableviewer.getControl().setLayoutData(gd);
		IEMFEditListProperty prop = EMFEditProperties.list(getEditingDomain(), CommandsPackageImpl.Literals.KEY_BINDING__PARAMETERS);

		TableViewerColumn column = new TableViewerColumn(tableviewer, SWT.NONE);
		column.getColumn().setText(Messages.KeyBindingEditor_ParametersKey);
		column.getColumn().setWidth(200);
		column.setEditingSupport(new EditingSupport(tableviewer) {
			private TextCellEditor cellEditor = new TextCellEditor(tableviewer.getTable());

			@Override
			protected void setValue(Object element, Object value) {
				((MParameter) element).setName((String) value);
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

		column = new TableViewerColumn(tableviewer, SWT.NONE);
		column.getColumn().setText(Messages.KeyBindingEditor_ParametersValue);
		column.getColumn().setWidth(200);
		column.setEditingSupport(new EditingSupport(tableviewer) {
			private TextCellEditor cellEditor = new TextCellEditor(tableviewer.getTable());

			@Override
			protected void setValue(Object element, Object value) {
				((MParameter) element).setValue((String) value);
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

		ColumnViewerEditorActivationStrategy editorActivationStrategy = new ColumnViewerEditorActivationStrategy(tableviewer) {
			@Override
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
				boolean singleSelect = ((IStructuredSelection) tableviewer.getSelection()).size() == 1;
				boolean isLeftDoubleMouseSelect = event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION && ((MouseEvent) event.sourceEvent).button == 1;

				return singleSelect && (isLeftDoubleMouseSelect || event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC || event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL);
			}
		};
		TableViewerEditor.create(tableviewer, editorActivationStrategy, ColumnViewerEditor.TABBING_HORIZONTAL | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR);

		ViewerSupport.bind(tableviewer, prop.observeDetail(getMaster()), new IValueProperty[] { EMFEditProperties.value(getEditingDomain(), CommandsPackageImpl.Literals.PARAMETER__NAME), EMFEditProperties.value(getEditingDomain(), CommandsPackageImpl.Literals.PARAMETER__VALUE) });

		Composite buttonComp = new Composite(parent, SWT.NONE);
		buttonComp.setLayoutData(new GridData(GridData.FILL, GridData.END, false, false));
		GridLayout gl = new GridLayout();
		gl.marginLeft = 0;
		gl.marginRight = 0;
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		buttonComp.setLayout(gl);

		Button b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
		b.setText(Messages.KeyBindingEditor_Up);
		b.setImage(getImage(b.getDisplay(), ARROW_UP));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));

		b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
		b.setText(Messages.KeyBindingEditor_Down);
		b.setImage(getImage(b.getDisplay(), ARROW_DOWN));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));

		b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
		b.setText(Messages.KeyBindingEditor_Add);
		b.setImage(getImage(b.getDisplay(), TABLE_ADD_IMAGE));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MKeyBinding item = (MKeyBinding) getMaster().getValue();
				MParameter param = MCommandsFactory.INSTANCE.createParameter();
				Command cmd = AddCommand.create(getEditingDomain(), item, CommandsPackageImpl.Literals.KEY_BINDING__PARAMETERS, param);
				if (cmd.canExecute()) {
					getEditingDomain().getCommandStack().execute(cmd);
				}
				tableviewer.editElement(param, 0);
			}
		});

		b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
		b.setText(Messages.KeyBindingEditor_Remove);
		b.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection s = (IStructuredSelection) tableviewer.getSelection();
				if (!s.isEmpty()) {
					MKeyBinding item = (MKeyBinding) getMaster().getValue();
					Command cmd = RemoveCommand.create(getEditingDomain(), item, CommandsPackageImpl.Literals.KEY_BINDING__PARAMETERS, s.toList());
					if (cmd.canExecute()) {
						getEditingDomain().getCommandStack().execute(cmd);
					}
				}
			}

		});
		b.setImage(getImage(b.getDisplay(), TABLE_DELETE_IMAGE));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));

		ControlFactory.createTagsWidget(parent, this);

		return parent;
	}

	@Override
	public IObservableList getChildList(Object element) {
		return null;
	}

	@Override
	public String getDetailLabel(Object element) {
		MKeySequence seq = (MKeySequence) element;
		if (seq.getKeySequence() != null && seq.getKeySequence().trim().length() > 0) {
			return seq.getKeySequence();
		}
		return null;
	}

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] { FeaturePath.fromList(CommandsPackageImpl.Literals.KEY_SEQUENCE__KEY_SEQUENCE) };
	}
}
