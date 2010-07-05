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
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.common.ComponentLabelProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.HandledMenuItemCommandSelectionDialog;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.databinding.edit.IEMFEditListProperty;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.MoveCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
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

public class HandledMenuItemEditor extends MenuItemEditor {
	private Image image;
	private IModelResource resource;

	private IEMFEditListProperty HANDLED_ITEM__PARAMETERS = EMFEditProperties.list(getEditingDomain(), MenuPackageImpl.Literals.HANDLED_ITEM__PARAMETERS);

	public HandledMenuItemEditor(EditingDomain editingDomain, ModelEditor editor, IProject project, IModelResource resource) {
		super(editingDomain, editor, project);
		this.resource = resource;
	}

	@Override
	public Image getImage(Object element, Display display) {
		if (image == null) {
			try {
				image = loadSharedImage(display, new URL("platform:/plugin/org.eclipse.e4.tools.emf.ui/icons/full/modelelements/HandledMenuItem.gif")); //$NON-NLS-1$
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
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

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
			l.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, false, false));

			final TableViewer viewer = new TableViewer(parent);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.heightHint = 120;
			viewer.getControl().setLayoutData(gd);

			ObservableListContentProvider cp = new ObservableListContentProvider();
			viewer.setContentProvider(cp);
			viewer.setLabelProvider(new ComponentLabelProvider(getEditor()));

			IEMFEditListProperty prop = EMFEditProperties.list(getEditingDomain(), MenuPackageImpl.Literals.HANDLED_ITEM__PARAMETERS);
			viewer.setInput(prop.observeDetail(master));

			Composite buttonComp = new Composite(parent, SWT.NONE);
			buttonComp.setLayoutData(new GridData(GridData.FILL, GridData.END, false, false));
			GridLayout gl = new GridLayout();
			gl.marginLeft = 0;
			gl.marginRight = 0;
			gl.marginWidth = 0;
			gl.marginHeight = 0;
			buttonComp.setLayout(gl);

			Button b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.ModelTooling_Common_Up);
			b.setImage(getImage(b.getDisplay(), ARROW_UP));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!viewer.getSelection().isEmpty()) {
						IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
						if (s.size() == 1) {
							Object obj = s.getFirstElement();
							MHandledItem container = (MHandledItem) getMaster().getValue();
							int idx = container.getParameters().indexOf(obj) - 1;
							if (idx >= 0) {
								Command cmd = MoveCommand.create(getEditingDomain(), getMaster().getValue(), MenuPackageImpl.Literals.HANDLED_ITEM__PARAMETERS, obj, idx);

								if (cmd.canExecute()) {
									getEditingDomain().getCommandStack().execute(cmd);
									viewer.setSelection(new StructuredSelection(obj));
								}
							}

						}
					}
				}
			});

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.ModelTooling_Common_Down);
			b.setImage(getImage(b.getDisplay(), ARROW_DOWN));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!viewer.getSelection().isEmpty()) {
						IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
						if (s.size() == 1) {
							Object obj = s.getFirstElement();
							MHandledItem container = (MHandledItem) getMaster().getValue();
							int idx = container.getParameters().indexOf(obj) + 1;
							if (idx < container.getParameters().size()) {
								Command cmd = MoveCommand.create(getEditingDomain(), getMaster().getValue(), MenuPackageImpl.Literals.HANDLED_ITEM__PARAMETERS, obj, idx);

								if (cmd.canExecute()) {
									getEditingDomain().getCommandStack().execute(cmd);
									viewer.setSelection(new StructuredSelection(obj));
								}
							}

						}
					}
				}
			});

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.ModelTooling_Common_AddEllipsis);
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
						getEditor().setSelection(param);
					}
				}
			});

			b = new Button(buttonComp, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.ModelTooling_Common_Remove);
			b.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent e) {
					IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
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

	@Override
	public IObservableList getChildList(Object element) {
		return HANDLED_ITEM__PARAMETERS.observe(element);
	}
}
