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
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.ContributionClassDialog;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.HandlerCommandSelectionDialog;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.FeaturePath;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class HandlerEditor extends AbstractComponentEditor {
	private Composite composite;
	private Image image;
	private EMFDataBindingContext context;
	private IModelResource resource;
	private IProject project;

	public HandlerEditor(EditingDomain editingDomain, IModelResource resource, IProject project) {
		super(editingDomain);
		this.resource = resource;
		this.project = project;
	}

	@Override
	public Image getImage(Object element, Display display) {
		if (image == null) {
			try {
				image = loadSharedImage(display, new URL("platform:/plugin/org.eclipse.e4.ui.model.workbench.edit/icons/full/obj16/Handler.gif")); //$NON-NLS-1$
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return image;
	}

	@Override
	public String getLabel(Object element) {
		return Messages.HandlerEditor_Label;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.HandlerEditor_Description;
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
			l.setText(Messages.HandlerEditor_Id);

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200, t), EMFEditProperties.value(getEditingDomain(), ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID).observeDetail(getMaster()));
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.HandlerEditor_Command);

			Text t = new Text(parent, SWT.BORDER);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			t.setEnabled(false);
			context.bindValue(textProp.observeDelayed(200, t), EMFEditProperties.value(getEditingDomain(), FeaturePath.fromList(CommandsPackageImpl.Literals.HANDLER__COMMAND, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID)).observeDetail(getMaster()));

			final Button b = new Button(parent, SWT.PUSH | SWT.FLAT);
			b.setText(Messages.HandlerEditor_Find);
			b.setImage(getImage(b.getDisplay(), SEARCH_IMAGE));
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					HandlerCommandSelectionDialog dialog = new HandlerCommandSelectionDialog(b.getShell(), (MHandler) getMaster().getValue(), resource);
					dialog.open();
				}
			});
		}

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.HandlerEditor_ClassURI);

			Text t = new Text(parent, SWT.BORDER);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			context.bindValue(textProp.observeDelayed(200, t), EMFEditProperties.value(getEditingDomain(), ApplicationPackageImpl.Literals.CONTRIBUTION__CONTRIBUTION_URI).observeDetail(getMaster()));

			final Button b = new Button(parent, SWT.PUSH | SWT.FLAT);
			b.setImage(getImage(b.getDisplay(), SEARCH_IMAGE));
			b.setText(Messages.HandlerEditor_Find);
			b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ContributionClassDialog dialog = new ContributionClassDialog(b.getShell(),project,getEditingDomain(),(MContribution) getMaster().getValue(), ApplicationPackageImpl.Literals.CONTRIBUTION__CONTRIBUTION_URI);
					dialog.open();
				}
			});
		}

		ControlFactory.createTagsWidget(parent, this);

		return parent;
	}

	@Override
	public IObservableList getChildList(Object element) {
		return null;
	}

	@Override
	public String getDetailLabel(Object element) {
		MHandler handler = (MHandler) element;
		if (handler.getCommand() != null && handler.getCommand().getCommandName() != null && handler.getCommand().getCommandName().trim().length() > 0) {
			return handler.getCommand().getCommandName();
		}

		return null;
	}

	@Override
	public FeaturePath[] getLabelProperties() {
		return new FeaturePath[] { FeaturePath.fromList(CommandsPackageImpl.Literals.HANDLER__COMMAND, CommandsPackageImpl.Literals.COMMAND__COMMAND_NAME) };
	}
}
