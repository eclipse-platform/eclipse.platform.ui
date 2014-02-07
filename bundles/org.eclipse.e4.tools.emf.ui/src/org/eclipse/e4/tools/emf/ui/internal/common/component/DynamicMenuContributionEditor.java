/*******************************************************************************
 * Copyright (c) 2013 MEDEVIT, FHV and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Descher <marco@descher.at> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import javax.inject.Inject;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.emf.ui.common.ContributionURIValidator;
import org.eclipse.e4.tools.emf.ui.common.EStackLayout;
import org.eclipse.e4.tools.emf.ui.common.IContributionClassCreator;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory.TextPasteHandler;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.ContributionClassDialog;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MDynamicMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;

public class DynamicMenuContributionEditor extends AbstractComponentEditor {

	private Composite composite;
	private EMFDataBindingContext context;

	private EStackLayout stackLayout;

	@Inject
	@Optional
	private IProject project;

	public DynamicMenuContributionEditor() {
		super();
	}

	@Override
	public String getLabel(Object element) {
		return Messages.DynamicMenuContributionEditor_Label;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.DynamicMenuContributionEditor_Description;
	}

	@Override
	public Image getImage(Object element, Display display) {
		if (element instanceof MUIElement) {
			if (((MUIElement) element).isToBeRendered()) {
				return createImage(ResourceProvider.IMG_DynamicMenuContribution);
			} else {
				return createImage(ResourceProvider.IMG_Tbr_DynamicMenuContribution);
			}
		}

		return null;
	}

	@Override
	public String getDetailLabel(Object element) {
		return getLocalizedLabel((MUILabel) element);
	}

	@Override
	protected Composite doGetEditor(Composite parent, Object object) {
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

		if (getEditor().isModelFragment()) {
			Control topControl;
			if (Util.isImport((EObject) object)) {
				topControl = composite.getChildren()[1];
			} else {
				topControl = composite.getChildren()[0];
			}

			if (stackLayout.topControl != topControl) {
				stackLayout.topControl = topControl;
				composite.layout(true, true);
			}
		}

		getMaster().setValue(object);
		return composite;
	}

	private Composite createForm(Composite parent, final EMFDataBindingContext context, WritableValue master, boolean isImport) {
		CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);

		CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabDefault);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		if (getEditor().isShowXMIId() || getEditor().isLiveModel()) {
			ControlFactory.createXMIId(parent, this);
		}

		IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);

		if (isImport) {
			ControlFactory.createFindImport(parent, Messages, this, context);
			folder.setSelection(0);
			return folder;
		}

		ControlFactory.createTextField(parent, Messages.ModelTooling_Common_Id, master, context, textProp, EMFEditProperties.value(getEditingDomain(), ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID));
		ControlFactory.createTranslatedTextField(parent, Messages.DynamicMenuContributionEditor_LabelLabel, getMaster(), context, textProp, EMFEditProperties.value(getEditingDomain(), UiPackageImpl.Literals.UI_LABEL__LABEL), resourcePool, project);

		// ------------------------------------------------------------
		final Link lnk;
		{
			final IContributionClassCreator c = getEditor().getContributionCreator(MenuPackageImpl.Literals.DYNAMIC_MENU_CONTRIBUTION);
			if (project != null && c != null) {
				lnk = new Link(parent, SWT.NONE);
				lnk.setText("<A>" + Messages.DynamicMenuContributionEditor_ClassURI + "</A>"); //$NON-NLS-1$//$NON-NLS-2$
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
				l.setText(Messages.DynamicMenuContributionEditor_ClassURI);
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
			Binding binding = context.bindValue(textProp.observeDelayed(200, t), EMFEditProperties.value(getEditingDomain(), ApplicationPackageImpl.Literals.CONTRIBUTION__CONTRIBUTION_URI).observeDetail(getMaster()), new UpdateValueStrategy().setAfterConvertValidator(new ContributionURIValidator()), new UpdateValueStrategy());
			Util.addDecoration(t, binding);

			final Button b = new Button(parent, SWT.PUSH | SWT.FLAT);
			b.setImage(createImage(ResourceProvider.IMG_Obj16_zoom));
			b.setText(Messages.ModelTooling_Common_FindEllipsis);
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ContributionClassDialog dialog = new ContributionClassDialog(b.getShell(), project, getEditingDomain(), (MContribution) getMaster().getValue(), ApplicationPackageImpl.Literals.CONTRIBUTION__CONTRIBUTION_URI, Messages);
					dialog.open();
				}
			});
		}

		createContributedEditorTabs(folder, context, getMaster(), MDynamicMenuContribution.class);

		folder.setSelection(0);

		return folder;
	}

	@Override
	public IObservableList getChildList(Object element) {
		return null;
	}

}
