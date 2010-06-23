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

import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;

import org.eclipse.e4.tools.emf.ui.common.IModelResource;

import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.SharedElementsDialog;

import org.eclipse.swt.events.SelectionEvent;

import org.eclipse.swt.events.SelectionAdapter;

import org.eclipse.swt.events.SelectionAdapter;

import org.eclipse.swt.widgets.Button;

import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.emf.databinding.EMFDataBindingContext;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class PlaceholderEditor extends AbstractComponentEditor {
	private Composite composite;
	private Image image;
	private EMFDataBindingContext context;
	private IModelResource resource;
	private ModelEditor editor;
	
	public PlaceholderEditor(EditingDomain editingDomain, ModelEditor editor, IModelResource resource) {
		super(editingDomain);
		this.resource = resource;
		this.editor = editor;
	}

	@Override
	public Image getImage(Object element, Display display) {
		if (image == null) {
			try {
				image = loadSharedImage(display, new URL("platform:/plugin/org.eclipse.e4.tools.emf.ui/icons/full/modelelements/Placeholder.gif")); //$NON-NLS-1$
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return image;
	}

	@Override
	public String getLabel(Object element) {
		return Messages.PlaceholderEditor_Label;
	}

	@Override
	public String getDetailLabel(Object element) {
		MPlaceholder pl = (MPlaceholder) element;
		if( pl.getRef() != null ) {
			StringBuilder b = new StringBuilder();

			b.append(((EObject)pl.getRef()).eClass().getName());
			if( pl.getRef() instanceof MUILabel ) {
				MUILabel label = (MUILabel) pl.getRef();
				if( label.getLabel() != null && label.getLabel().trim().length() > 0 ) {
					b.append(" (" + label.getLabel() + ")");  //$NON-NLS-1$//$NON-NLS-2$
				} else if( label.getTooltip() != null && label.getTooltip().trim().length() > 0 ) {
					b.append(" (" + label.getTooltip() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					if( pl.getRef().getElementId() != null && pl.getRef().getElementId().trim().length() > 0 ) {
						b.append(pl.getRef().getElementId());
					}
				}
			} else {
				if( pl.getRef().getElementId() != null && pl.getRef().getElementId().trim().length() > 0 ) {
					b.append(" (" + pl.getRef().getElementId() + ")");  //$NON-NLS-1$//$NON-NLS-2$
				}
			}

			return b.toString();
		}

		return null;
	}

	@Override
	public String getDescription(Object element) {
		return Messages.PlaceholderEditor_Descriptor;
	}

	@Override
	public Composite getEditor(Composite parent, Object object) {
		if (composite == null) {
			context = new EMFDataBindingContext();
			composite = createForm(parent, context, getMaster());
		}
		getMaster().setValue(object);
		return composite;
	}

	private Composite createForm(Composite parent, final EMFDataBindingContext context, WritableValue master) {
		parent = new Composite(parent, SWT.NONE);
		parent.setLayout(new GridLayout(3, false));

		IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);

		// ------------------------------------------------------------
		{
			Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.PlaceholderEditor_Id);

			Text t = new Text(parent, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			t.setLayoutData(gd);
			context.bindValue(textProp.observeDelayed(200,t), EMFEditProperties.value(getEditingDomain(), ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID).observeDetail(getMaster()));
			
			final Button b = new Button(parent, SWT.PUSH);
			b.setText(Messages.PlaceholderEditor_FindReference);
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					SharedElementsDialog dialog = new SharedElementsDialog(b.getShell(),editor,(MPlaceholder) getMaster().getValue(), resource);
					dialog.open();
				}
			});
		}

		return parent;
	}

	@Override
	public IObservableList getChildList(Object element) {
		return null;
	}

}
