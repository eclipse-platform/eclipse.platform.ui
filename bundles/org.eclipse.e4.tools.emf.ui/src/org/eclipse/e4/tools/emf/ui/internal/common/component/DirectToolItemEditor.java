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
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.ContributionClassDialog;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.emf.databinding.EMFDataBindingContext;
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
import org.eclipse.swt.widgets.Text;

public class DirectToolItemEditor extends ToolItemEditor {
	private Image image;

	public DirectToolItemEditor(EditingDomain editingDomain, IProject project) {
		super(editingDomain,project);
	}

	@Override
	public Image getImage(Object element, Display display) {
		if (image == null) {
			try {
				image = loadSharedImage(display, new URL("platform:/plugin/org.eclipse.e4.tools.emf.ui/icons/full/modelelements/DirectToolItem.gif")); //$NON-NLS-1$
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return image;
	}


	@Override
	protected void createSubTypeFormElements(Composite parent, EMFDataBindingContext context, WritableValue master) {
		IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);

		Label l = new Label(parent, SWT.NONE);
		l.setText(Messages.DirectToolItemEditor_ClassURI);

		Text t = new Text(parent, SWT.BORDER);
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value( getEditingDomain(), ApplicationPackageImpl.Literals.CONTRIBUTION__CONTRIBUTION_URI).observeDetail(master));

		final Button b = new Button(parent, SWT.PUSH|SWT.FLAT);
		b.setText(Messages.DirectToolItemEditor_Find);
		b.setImage(getImage(b.getDisplay(), SEARCH_IMAGE));
		b.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ContributionClassDialog dialog = new ContributionClassDialog(b.getShell(),project,getEditingDomain(),(MContribution) getMaster().getValue(), ApplicationPackageImpl.Literals.CONTRIBUTION__CONTRIBUTION_URI);
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
}