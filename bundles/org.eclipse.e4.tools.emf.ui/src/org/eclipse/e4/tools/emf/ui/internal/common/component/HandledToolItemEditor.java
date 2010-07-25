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
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.tools.emf.ui.common.CommandToStringConverter;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.HandledToolItemCommandSelectionDialog;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.databinding.IEMFValueProperty;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.databinding.edit.IEMFEditListProperty;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class HandledToolItemEditor extends ToolItemEditor {
	private IModelResource resource;

	private IEMFEditListProperty HANDLED_ITEM__PARAMETERS = EMFEditProperties.list(getEditingDomain(), MenuPackageImpl.Literals.HANDLED_ITEM__PARAMETERS);
	private IEMFValueProperty UI_ELEMENT__VISIBLE_WHEN = EMFProperties.value(UiPackageImpl.Literals.UI_ELEMENT__VISIBLE_WHEN);

	public HandledToolItemEditor(EditingDomain editingDomain, ModelEditor editor, IProject project, IModelResource resource) {
		super(editingDomain, editor, project);
		this.resource = resource;
	}

	@Override
	public Image getImage(Object element, Display display) {
		if (element instanceof MUIElement) {
			MUIElement uiElement = (MUIElement) element;
			if (uiElement.isToBeRendered() && uiElement.isVisible()) {
				try {
					return loadSharedImage(display, new URL("platform:/plugin/org.eclipse.e4.tools.emf.ui/icons/full/modelelements/HandledToolItem.gif")); //$NON-NLS-1$
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				try {
					return loadSharedImage(display, new URL("platform:/plugin/org.eclipse.e4.tools.emf.ui/icons/full/modelelements/tbr/HandledToolItem.gif"));//$NON-NLS-1$
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return null;
	}

	@Override
	protected void createSubTypeFormElements(Composite parent, EMFDataBindingContext context, final WritableValue master) {
		IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);

		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.HandledToolItemEditor_Command);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

			Text t = new Text(parent, SWT.BORDER);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			t.setEditable(false);
			context.bindValue(textProp.observeDelayed(200, t), EMFEditProperties.value(getEditingDomain(), MenuPackageImpl.Literals.HANDLED_ITEM__COMMAND).observeDetail(master), new UpdateValueStrategy(), new UpdateValueStrategy().setConverter(new CommandToStringConverter()));

			final Button b = new Button(parent, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.ModelTooling_Common_FindEllipsis);
			b.setImage(getImage(b.getDisplay(), SEARCH_IMAGE));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					HandledToolItemCommandSelectionDialog dialog = new HandledToolItemCommandSelectionDialog(b.getShell(), (MHandledItem) getMaster().getValue(), resource);
					dialog.open();
				}
			});
		}
	}

	@Override
	public String getLabel(Object element) {
		return Messages.HandledToolItemEditor_Label;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.HandledToolItemEditor_Description;
	}

	@Override
	public IObservableList getChildList(Object element) {
		final WritableList list = new WritableList();

		if (((MHandledToolItem) element).getVisibleWhen() != null) {
			list.add(0, ((MHandledToolItem) element).getVisibleWhen());
		}

		UI_ELEMENT__VISIBLE_WHEN.observe(element).addValueChangeListener(new IValueChangeListener() {

			public void handleValueChange(ValueChangeEvent event) {
				if (event.diff.getOldValue() != null) {
					list.remove(event.diff.getOldValue());
				}

				if (event.diff.getNewValue() != null) {
					list.add(0, event.diff.getNewValue());
				}
			}
		});

		list.add(new VirtualEntry<MParameter>(ModelEditor.VIRTUAL_PARAMETERS, HANDLED_ITEM__PARAMETERS, element, Messages.HandledToolItemEditor_Parameters) {
			@Override
			protected boolean accepted(MParameter o) {
				return true;
			}
		});

		return list;
	}

}