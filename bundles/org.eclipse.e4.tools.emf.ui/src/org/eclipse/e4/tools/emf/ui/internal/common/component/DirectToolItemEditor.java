/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Steven Spungin <steven@spungin.tv> - Bug 424730
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import javax.inject.Inject;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.common.ContributionURIValidator;
import org.eclipse.e4.tools.emf.ui.common.IContributionClassCreator;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory.TextPasteHandler;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.ContributionClassDialog;
import org.eclipse.e4.tools.emf.ui.internal.common.objectdata.ObjectViewer;
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
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;

public class DirectToolItemEditor extends ToolItemEditor {
	private IEMFValueProperty UI_ELEMENT__VISIBLE_WHEN = EMFProperties.value(UiPackageImpl.Literals.UI_ELEMENT__VISIBLE_WHEN);

	@Inject
	IEclipseContext eclipseContext;

	@Inject
	public DirectToolItemEditor() {
		super();
	}

	@Override
	public Image getImage(Object element, Display display) {
		if (element instanceof MUIElement) {
			MUIElement uiElement = (MUIElement) element;
			if (uiElement.isToBeRendered() && uiElement.isVisible()) {
				return createImage(ResourceProvider.IMG_DirectToolItem);
			} else {
				return createImage(ResourceProvider.IMG_Tbr_DirectToolItem);
			}
		}

		return null;
	}

	@Override
	protected void createSubTypeFormElements(Composite parent, EMFDataBindingContext context, WritableValue master) {
		IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);

		final IContributionClassCreator c = getEditor().getContributionCreator(MenuPackageImpl.Literals.DIRECT_TOOL_ITEM);
		final Link lnk;
		if (project != null && c != null) {
			lnk = new Link(parent, SWT.NONE);
			lnk.setText("<A>" + Messages.DirectMenuItemEditor_ClassURI + "</A>"); //$NON-NLS-1$//$NON-NLS-2$
			lnk.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
			lnk.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					c.createOpen((MContribution) getMaster().getValue(), getEditingDomain(), project, lnk.getShell());
				}
			});
		} else {
			lnk = null;
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.DirectToolItemEditor_ClassURI);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		}

		Text t = new Text(parent, SWT.BORDER);
		TextPasteHandler.createFor(t);
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		t.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				if (lnk != null) {
					lnk.setToolTipText(((Text) (e.getSource())).getText());
				}
			}
		});
		Binding binding = context.bindValue(textProp.observeDelayed(200, t), EMFEditProperties.value(getEditingDomain(), ApplicationPackageImpl.Literals.CONTRIBUTION__CONTRIBUTION_URI).observeDetail(master), new UpdateValueStrategy().setAfterConvertValidator(new ContributionURIValidator()), new UpdateValueStrategy());
		Util.addDecoration(t, binding);

		final Button b = new Button(parent, SWT.PUSH | SWT.FLAT);
		b.setText(Messages.ModelTooling_Common_FindEllipsis);
		b.setImage(createImage(ResourceProvider.IMG_Obj16_zoom));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ContributionClassDialog dialog = new ContributionClassDialog(b.getShell(), eclipseContext, getEditingDomain(), (MContribution) getMaster().getValue(), ApplicationPackageImpl.Literals.CONTRIBUTION__CONTRIBUTION_URI, Messages);
				dialog.open();
			}
		});
	}

	@Override
	protected CTabFolder createForm(Composite parent, EMFDataBindingContext context, WritableValue master, boolean isImport) {
		if (!isImport) {
			CTabFolder folder = super.createForm(parent, context, master, isImport);
			createInstanceInspection(folder);
			return folder;
		} else {
			return super.createForm(parent, context, master, isImport);
		}
	}

	private void createInstanceInspection(CTabFolder folder) {
		CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_RuntimeContributionInstance);
		Composite container = new Composite(folder, SWT.NONE);
		container.setLayout(new GridLayout());
		item.setControl(container);

		ObjectViewer objectViewer = new ObjectViewer();
		TreeViewer viewer = objectViewer.createViewer(container, ApplicationPackageImpl.Literals.CONTRIBUTION__OBJECT, getMaster(), resourcePool, Messages);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

	}

	@Override
	public String getLabel(Object element) {
		return Messages.DirectToolItemEditor_Label;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.DirectToolItemEditor_Description;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IObservableList getChildList(Object element) {
		final IObservableList list = super.getChildList(element);

		if (((MDirectToolItem) element).getVisibleWhen() != null) {
			list.add(0, ((MDirectToolItem) element).getVisibleWhen());
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

		return list;
	}
}