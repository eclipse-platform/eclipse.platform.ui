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

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.tools.emf.ui.common.ContributionURIValidator;
import org.eclipse.e4.tools.emf.ui.common.EStackLayout;
import org.eclipse.e4.tools.emf.ui.common.IContributionClassCreator;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.ContributionClassDialog;
import org.eclipse.e4.tools.services.IResourcePool;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;

public class AddonsEditor extends AbstractComponentEditor {
	private Composite composite;
	private EMFDataBindingContext context;
	private IProject project;
	private EStackLayout stackLayout;

	public AddonsEditor(EditingDomain editingDomain, ModelEditor editor, IProject project, IResourcePool resourcePool) {
		super(editingDomain, editor, resourcePool);
		this.project = project;
	}

	@Override
	public Image getImage(Object element, Display display) {
		return createImage(ResourceProvider.IMG_Addons);
	}

	@Override
	public String getLabel(Object element) {
		return Messages.AddonsEditor_TreeLabel;
	}

	@Override
	public String getDetailLabel(Object element) {
		MContribution contrib = (MContribution) element;
		if (contrib.getContributionURI() != null && contrib.getContributionURI().trim().length() > 0) {
			return contrib.getContributionURI().substring(contrib.getContributionURI().lastIndexOf('/') + 1);
		}
		return null;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.AddonsEditor_TreeLabelDescription;
	}

	@Override
	public Composite getEditor(Composite parent, Object object) {
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

	protected Composite createForm(Composite parent, EMFDataBindingContext context, IObservableValue master, boolean isImport) {
		parent = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout(3, false);
		gl.horizontalSpacing = 10;
		parent.setLayout(gl);

		IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);

		if (getEditor().isShowXMIId() || getEditor().isLiveModel()) {
			ControlFactory.createXMIId(parent, this);
		}

		if (getEditor().isModelFragment() && isImport) {
			ControlFactory.createFindImport(parent, this, context);
			return parent;
		}

		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.ModelTooling_Common_Id);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200, t), EMFEditProperties.value(getEditingDomain(), ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID).observeDetail(getMaster()));
		}

		// ------------------------------------------------------------
		{
			final IContributionClassCreator c = getEditor().getContributionCreator(ApplicationPackageImpl.Literals.ADDON);
			if (project != null && c != null) {
				final Link l = new Link(parent, SWT.NONE);
				l.setText("<A>" + Messages.PartEditor_ClassURI + "</A>"); //$NON-NLS-1$//$NON-NLS-2$
				l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
				l.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						c.createOpen((MContribution) getMaster().getValue(), getEditingDomain(), project, l.getShell());
					}
				});
			} else {
				Label l = new Label(parent, SWT.NONE);
				l.setText(Messages.AddonsEditor_ClassURI);
				l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
			}

			Text t = new Text(parent, SWT.BORDER);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			Binding binding = context.bindValue(textProp.observeDelayed(200, t), EMFEditProperties.value(getEditingDomain(), ApplicationPackageImpl.Literals.CONTRIBUTION__CONTRIBUTION_URI).observeDetail(getMaster()), new UpdateValueStrategy().setAfterConvertValidator(new ContributionURIValidator()), new UpdateValueStrategy());
			Util.addDecoration(t, binding);

			final Button b = new Button(parent, SWT.PUSH | SWT.FLAT);
			b.setImage(createImage(ResourceProvider.IMG_Obj16_zoom));
			b.setText(Messages.ModelTooling_Common_FindEllipsis);
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ContributionClassDialog dialog = new ContributionClassDialog(b.getShell(), project, getEditingDomain(), (MContribution) getMaster().getValue(), ApplicationPackageImpl.Literals.CONTRIBUTION__CONTRIBUTION_URI);
					dialog.open();
				}
			});
		}

		ControlFactory.createMapProperties(parent, this, Messages.ModelTooling_Contribution_PersistedState, ApplicationPackageImpl.Literals.CONTRIBUTION__PERSISTED_STATE, VERTICAL_LIST_WIDGET_INDENT);
		ControlFactory.createStringListWidget(parent, this, Messages.AddonsEditor_Tags, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__TAGS, VERTICAL_LIST_WIDGET_INDENT);

		return parent;
	}

	@Override
	public IObservableList getChildList(Object element) {
		return null;
	}

}
