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
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.tools.emf.ui.common.EStackLayout;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.ContributionClassDialog;
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
import org.eclipse.swt.widgets.Text;

public class AddonsEditor extends AbstractComponentEditor {
	private Composite composite;
	private EMFDataBindingContext context;
	private IProject project;
	private Image image;
	private EStackLayout stackLayout;
	
	public AddonsEditor(EditingDomain editingDomain, ModelEditor editor, IProject project) {
		super(editingDomain,editor);
		this.project = project;
	}

	@Override
	public Image getImage(Object element, Display display) {
		if( image == null ) {
			try {
				image = loadSharedImage(display, new URL("platform:/plugin/org.eclipse.e4.tools.emf.ui/icons/full/modelelements/Addons.png")); //$NON-NLS-1$
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return image;
	}

	@Override
	public String getLabel(Object element) {
		return "Addon";
	}

	@Override
	public String getDetailLabel(Object element) {
		MContribution contrib = (MContribution) element;
		if( contrib.getContributionURI() != null && contrib.getContributionURI().trim().length() > 0 ) {
			return contrib.getContributionURI().substring(contrib.getContributionURI().lastIndexOf('/')+1);
		}
		return null;
	}

	@Override
	public String getDescription(Object element) {
		return "Addon Bla Bla Bla";
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
		
		if( getEditor().isModelFragment() ) {
			Control topControl;
			if( Util.isImport((EObject) object) ) {
				topControl = composite.getChildren()[1];
			} else {
				topControl = composite.getChildren()[0];				
			}
			
			if( stackLayout.topControl != topControl ) {
				stackLayout.topControl = topControl;
				composite.layout(true, true);
			}
		}
		
		getMaster().setValue(object);
		return composite;
	}
	
	protected Composite createForm(Composite parent, EMFDataBindingContext context, IObservableValue master, boolean isImport) {
		parent = new Composite(parent, SWT.NONE);
		parent.setLayout(new GridLayout(3, false));

		IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);

		
		if( getEditor().isModelFragment() && isImport ) {
			ControlFactory.createFindImport(parent, this, context);
			return parent;
		}

		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.AddonsEditor_Id);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID).observeDetail(getMaster()));
		}
		
		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.AddonsEditor_ClassURI);
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

			Text t = new Text(parent, SWT.BORDER);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), ApplicationPackageImpl.Literals.CONTRIBUTION__CONTRIBUTION_URI).observeDetail(getMaster()));

			final Button b = new Button(parent, SWT.PUSH|SWT.FLAT);
			b.setImage(getImage(t.getDisplay(), SEARCH_IMAGE));
			b.setText(Messages.AddonsEditor_Find);
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ContributionClassDialog dialog = new ContributionClassDialog(b.getShell(),project,getEditingDomain(),(MContribution) getMaster().getValue(), ApplicationPackageImpl.Literals.CONTRIBUTION__CONTRIBUTION_URI);
					dialog.open();
				}
			});
		}
		
//		ControlFactory.createBindingsWidget(parent, this);

		return parent;
	}

	@Override
	public IObservableList getChildList(Object element) {
		return null;
	}

}
