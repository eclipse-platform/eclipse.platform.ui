/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import javax.inject.Inject;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.e4.tools.emf.ui.common.CommandToStringConverter;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory.TextPasteHandler;
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

	private final IEMFEditListProperty HANDLED_ITEM__PARAMETERS = EMFEditProperties.list(getEditingDomain(),
		MenuPackageImpl.Literals.HANDLED_ITEM__PARAMETERS);
	private final IEMFValueProperty UI_ELEMENT__VISIBLE_WHEN = EMFProperties
		.value(UiPackageImpl.Literals.UI_ELEMENT__VISIBLE_WHEN);

	@Inject
	private IModelResource resource;

	@Inject
	public HandledToolItemEditor() {
		super();
	}

	@Override
	public Image getImage(Object element, Display display) {
		if (element instanceof MUIElement) {
			final MUIElement uiElement = (MUIElement) element;
			if (uiElement.isToBeRendered() && uiElement.isVisible()) {
				return createImage(ResourceProvider.IMG_HandledToolItem);
			}
			return createImage(ResourceProvider.IMG_Tbr_HandledToolItem);
		}

		return null;
	}

	@Override
	protected void createSubTypeFormElements(Composite parent, EMFDataBindingContext context, final WritableValue master) {
		final IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);

		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.HandledToolItemEditor_Command);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

			final Text t = new Text(parent, SWT.BORDER);
			TextPasteHandler.createFor(t);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			t.setEditable(false);
			context.bindValue(textProp.observeDelayed(200, t),
				EMFEditProperties.value(getEditingDomain(), MenuPackageImpl.Literals.HANDLED_ITEM__COMMAND)
					.observeDetail(master), new UpdateValueStrategy(), new UpdateValueStrategy()
					.setConverter(new CommandToStringConverter(Messages)));

			final Button b = new Button(parent, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.ModelTooling_Common_FindEllipsis);
			b.setImage(createImage(ResourceProvider.IMG_Obj16_zoom));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final HandledToolItemCommandSelectionDialog dialog = new HandledToolItemCommandSelectionDialog(b
						.getShell(), (MHandledItem) getMaster().getValue(), resource, Messages);
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

	@SuppressWarnings("unchecked")
	@Override
	public IObservableList getChildList(Object element) {
		final IObservableList list = super.getChildList(element);

		if (((MHandledToolItem) element).getVisibleWhen() != null) {
			list.add(0, ((MHandledToolItem) element).getVisibleWhen());
		}

		UI_ELEMENT__VISIBLE_WHEN.observe(element).addValueChangeListener(new IValueChangeListener() {

			@Override
			public void handleValueChange(ValueChangeEvent event) {
				if (event.diff.getOldValue() != null) {
					list.remove(event.diff.getOldValue());
				}

				if (event.diff.getNewValue() != null) {
					list.add(0, event.diff.getNewValue());
				}
			}
		});

		list.add(new VirtualEntry<MParameter>(ModelEditor.VIRTUAL_PARAMETERS, HANDLED_ITEM__PARAMETERS, element,
			Messages.HandledToolItemEditor_Parameters) {
			@Override
			protected boolean accepted(MParameter o) {
				return true;
			}
		});

		return list;
	}

}