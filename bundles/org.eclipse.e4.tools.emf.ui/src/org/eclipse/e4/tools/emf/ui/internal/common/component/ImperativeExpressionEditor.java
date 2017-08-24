/*******************************************************************************
 * Copyright (c) 2016, 2017 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Simon Scholz <simon.scholz@vogella.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component;

import javax.inject.Inject;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.common.ContributionURIValidator;
import org.eclipse.e4.tools.emf.ui.common.IContributionClassCreator;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory.TextPasteHandler;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.ContributionClassDialog;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MImperativeExpression;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;

public class ImperativeExpressionEditor extends AbstractComponentEditor {
	private Composite composite;
	private EMFDataBindingContext context;

	@Inject
	IEclipseContext eclipseContext;

	@Inject
	public ImperativeExpressionEditor() {
		super();
	}

	@Override
	public Image getImage(Object element) {
		return getImage(element, ResourceProvider.IMG_Obj16_class_obj);
	}

	@Override
	public String getLabel(Object element) {
		return Messages.ImperativeExpressionEditor_TreeLabel;
	}

	@Override
	public String getDetailLabel(Object element) {
		String contributionURI = ((MImperativeExpression) element).getContributionURI();
		if (contributionURI != null && contributionURI.trim().length() > 0) {
			int lastIndexOf = contributionURI.lastIndexOf("."); //$NON-NLS-1$
			if (contributionURI.length() > lastIndexOf + 1) {
				return contributionURI.substring(lastIndexOf + 1);
			}
			return contributionURI;
		}
		return null;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.CoreExpressionEditor_TreeLabelDescription;
	}

	@Override
	public Composite doGetEditor(Composite parent, Object object) {
		if (composite == null) {
			context = new EMFDataBindingContext();
			composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new FillLayout());
			createForm(composite, context, getMaster());
		}
		getMaster().setValue(object);
		return composite;
	}

	private Composite createForm(Composite parent, EMFDataBindingContext context, WritableValue master) {
		final CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);

		CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabDefault);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		if (getEditor().isShowXMIId() || getEditor().isLiveModel()) {
			ControlFactory.createXMIId(parent, this);
		}

		final IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);

		final Link lnk;
		{
			final IContributionClassCreator c = getEditor()
					.getContributionCreator(UiPackageImpl.Literals.IMPERATIVE_EXPRESSION);
			if (project != null && c != null) {
				lnk = new Link(parent, SWT.NONE);
				lnk.setText("<A>" + Messages.HandlerEditor_ClassURI + "</A>"); //$NON-NLS-1$//$NON-NLS-2$
				lnk.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
				lnk.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						c.createOpen((MContribution) getMaster().getValue(), getEditingDomain(), project,
								lnk.getShell());
					}
				});
			} else {
				lnk = null;
				final Label l = new Label(parent, SWT.NONE);
				l.setText(Messages.HandlerEditor_ClassURI);
				l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
			}

			final Text t = new Text(parent, SWT.BORDER);
			TextPasteHandler.createFor(t);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			t.addModifyListener(e -> {
				if (lnk != null) {
					lnk.setToolTipText(((Text) e.getSource()).getText());
				}
			});
			final Binding binding = context.bindValue(textProp.observeDelayed(200, t),
					EMFEditProperties
					.value(getEditingDomain(), ApplicationPackageImpl.Literals.CONTRIBUTION__CONTRIBUTION_URI)
					.observeDetail(getMaster()),
					new UpdateValueStrategy().setAfterConvertValidator(new ContributionURIValidator()),
					new UpdateValueStrategy());
			Util.addDecoration(t, binding);

			final Button b = new Button(parent, SWT.PUSH | SWT.FLAT);
			b.setImage(createImage(ResourceProvider.IMG_Obj16_zoom));
			b.setText(Messages.ModelTooling_Common_FindEllipsis);
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final ContributionClassDialog dialog = new ContributionClassDialog(b.getShell(), eclipseContext,
							getEditingDomain(), (MContribution) getMaster().getValue(),
							ApplicationPackageImpl.Literals.CONTRIBUTION__CONTRIBUTION_URI, Messages);
					dialog.open();
				}
			});

			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.ImperativeExpressionEditor_TrackingLabel);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

			Button trackingButton = new Button(parent, SWT.CHECK);
			ISWTObservableValue observeTrackingSelection = WidgetProperties.selection().observe(trackingButton);
			context.bindValue(observeTrackingSelection,
					EMFEditProperties
					.value(getEditingDomain(), UiPackageImpl.Literals.IMPERATIVE_EXPRESSION__TRACKING)
					.observeDetail(getMaster()));
		}

		item = new CTabItem(folder, SWT.NONE);
		item.setText(Messages.ModelTooling_Common_TabSupplementary);

		parent = createScrollableContainer(folder);
		item.setControl(parent.getParent());

		ControlFactory.createStringListWidget(parent, Messages, this, Messages.CategoryEditor_Tags,
				ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__TAGS, VERTICAL_LIST_WIDGET_INDENT);
		ControlFactory.createMapProperties(parent, Messages, this, Messages.ModelTooling_Contribution_PersistedState,
				ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__PERSISTED_STATE, VERTICAL_LIST_WIDGET_INDENT);

		createContributedEditorTabs(folder, context, getMaster(), MImperativeExpression.class);

		folder.setSelection(0);

		return folder;
	}

	@Override
	public IObservableList getChildList(Object element) {
		return null;
	}

}
