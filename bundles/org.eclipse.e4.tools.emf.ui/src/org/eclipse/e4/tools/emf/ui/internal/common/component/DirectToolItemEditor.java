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
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.tools.emf.ui.common.ContributionURIValidator;
import org.eclipse.e4.tools.emf.ui.common.IContributionClassCreator;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.ContributionClassDialog;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.databinding.IEMFValueProperty;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
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
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;

public class DirectToolItemEditor extends ToolItemEditor {
	private IEMFValueProperty UI_ELEMENT__VISIBLE_WHEN = EMFProperties.value(UiPackageImpl.Literals.UI_ELEMENT__VISIBLE_WHEN);

	public DirectToolItemEditor(EditingDomain editingDomain, ModelEditor editor, IProject project) {
		super(editingDomain, editor, project);
	}

	@Override
	public Image getImage(Object element, Display display) {
		if (element instanceof MUIElement) {
			MUIElement uiElement = (MUIElement) element;
			if (uiElement.isToBeRendered() && uiElement.isVisible()) {
				try {
					return loadSharedImage(display, new URL("platform:/plugin/org.eclipse.e4.tools.emf.ui/icons/full/modelelements/DirectToolItem.gif"));//$NON-NLS-1$
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				try {
					return loadSharedImage(display, new URL("platform:/plugin/org.eclipse.e4.tools.emf.ui/icons/full/modelelements/tbr/DirectToolItem.gif"));//$NON-NLS-1$
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return null;
	}

	@Override
	protected void createSubTypeFormElements(Composite parent, EMFDataBindingContext context, WritableValue master) {
		IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);

		final IContributionClassCreator c = getEditor().getContributionCreator(MenuPackageImpl.Literals.DIRECT_TOOL_ITEM);
		if (project != null && c != null) {
			final Link l = new Link(parent, SWT.NONE);
			l.setText("<A>" + Messages.DirectMenuItemEditor_ClassURI + "</A>"); //$NON-NLS-1$//$NON-NLS-2$
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
			l.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					c.createOpen((MContribution) getMaster().getValue(), getEditingDomain(), project, l.getShell());
				}
			});
		} else {
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.DirectToolItemEditor_ClassURI);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		}

		Text t = new Text(parent, SWT.BORDER);
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Binding binding = context.bindValue(textProp.observeDelayed(200, t), EMFEditProperties.value(getEditingDomain(), ApplicationPackageImpl.Literals.CONTRIBUTION__CONTRIBUTION_URI).observeDetail(master), new UpdateValueStrategy().setAfterConvertValidator(new ContributionURIValidator()), new UpdateValueStrategy());
		Util.addDecoration(t, binding);

		final Button b = new Button(parent, SWT.PUSH | SWT.FLAT);
		b.setText(Messages.ModelTooling_Common_FindEllipsis);
		b.setImage(getImage(b.getDisplay(), SEARCH_IMAGE));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ContributionClassDialog dialog = new ContributionClassDialog(b.getShell(), project, getEditingDomain(), (MContribution) getMaster().getValue(), ApplicationPackageImpl.Literals.CONTRIBUTION__CONTRIBUTION_URI);
				dialog.open();
			}
		});
	}

	@Override
	public String getLabel(Object element) {
		return Messages.DirectToolItemEditor_Label;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.DirectToolItemEditor_Description;
	}

	@Override
	public IObservableList getChildList(Object element) {
		final WritableList list = new WritableList();

		if (((MDirectToolItem) element).getVisibleWhen() != null) {
			list.add(0, ((MDirectToolItem) element).getVisibleWhen());
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

		return list;
	}
}