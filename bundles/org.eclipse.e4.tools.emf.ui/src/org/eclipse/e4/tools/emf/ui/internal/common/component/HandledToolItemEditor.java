/*******************************************************************************
 * Copyright (c) 2010, 2017 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import javax.inject.Inject;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.e4.tools.emf.ui.common.CommandToStringConverter;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.internal.E4Properties;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.VirtualEntry;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory.TextPasteHandler;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.HandledToolItemCommandSelectionDialog;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class HandledToolItemEditor extends ToolItemEditor<MHandledToolItem> {
	@Inject
	private IModelResource resource;

	@Inject
	public HandledToolItemEditor() {
		super();
	}

	@Override
	public Image getImage(Object element) {
		return getImage(element, ResourceProvider.IMG_HandledToolItem);
	}

	@Override
	protected void createSubTypeFormElements(Composite parent, EMFDataBindingContext context,
			final WritableValue<MHandledToolItem> master) {
		final IWidgetValueProperty<Text, String> textProp = WidgetProperties.text(SWT.Modify);

		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.HandledToolItemEditor_Command);
			l.setLayoutData(new GridData());

			final Text t = new Text(parent, SWT.BORDER);
			TextPasteHandler.createFor(t);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			t.setEditable(false);
			context.bindValue(textProp.observeDelayed(200, t),
					E4Properties.itemCommand(getEditingDomain()).observeDetail(master),
					new UpdateValueStrategy<>(), UpdateValueStrategy.create(new CommandToStringConverter(Messages)));

			Button b = ControlFactory.createFindButton(parent, resourcePool);
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final HandledToolItemCommandSelectionDialog dialog = new HandledToolItemCommandSelectionDialog(b
							.getShell(), getMaster().getValue(), resource, Messages);
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
	public IObservableList<Object> getChildList(Object element) {
		final IObservableList<Object> list = super.getChildList(element);
		MHandledToolItem item = (MHandledToolItem) element;

		if (item.getVisibleWhen() != null) {
			list.add(0, item.getVisibleWhen());
		}

		E4Properties.visibleWhen().observe(item).addValueChangeListener(event -> {
			if (event.diff.getOldValue() != null) {
				list.remove(event.diff.getOldValue());
			}

			if (event.diff.getNewValue() != null) {
				list.add(0, event.diff.getNewValue());
			}
		});

		list.add(new VirtualEntry<>(ModelEditor.VIRTUAL_PARAMETERS, E4Properties.itemParameters(getEditingDomain()), item,
				Messages.HandledToolItemEditor_Parameters));

		return list;
	}

}