/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.provisional.views.markers;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * FiltersConfigurationDialog is the dialog for configuring the filters for the
 * 
 * @since 3.3
 * 
 */
public class FiltersConfigurationDialog extends Dialog {

	private class ScopeArea extends FilterConfigurationArea {

		/**
		 * Create a new instance of the receiver.
		 */
		public ScopeArea() {
			super();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.internal.provisional.views.markers.FilterConfigurationArea#createContents(org.eclipse.swt.widgets.Composite)
		 */
		public void createContents(Composite parent) {
			Label label = new Label(parent, SWT.NONE);
			label.setText("Test"); //$NON-NLS-1$
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.internal.provisional.views.markers.FilterConfigurationArea#getTitle()
		 */
		public String getTitle() {
			return MarkerMessages.severity_description;
		}

	}

	private FilterConfigurationArea scopeArea = new ScopeArea();

	private Collection filterGroups;

	/**
	 * Create a new instance of the receiver on group.
	 * 
	 * @param parentShell
	 * @param groups
	 *            Collection of MarkerFieldFilterGroup
	 */
	public FiltersConfigurationDialog(IShellProvider parentShell,
			Collection groups) {
		super(parentShell);
		filterGroups = groups;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {

		Composite top = (Composite) super.createDialogArea(parent);
		final FormToolkit toolkit = new FormToolkit(top.getDisplay());
		parent.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				toolkit.dispose();

			}
		});
		final ScrolledForm form = toolkit.createScrolledForm(top);
		form.setBackground(parent.getBackground());
		form.getBody().setLayout(new GridLayout());

		createFieldArea(toolkit, form, scopeArea);
		Iterator areas = getFilterGroup().getFieldFilterAreas().iterator();
		while (areas.hasNext()) {
			createFieldArea(toolkit, form, (FilterConfigurationArea) areas
					.next());

		}

		return top;
	}

	/**
	 * Return a MarkerFieldFilterGroup
	 * 
	 * @return MarkerFieldFilterGroup
	 */
	private MarkerFieldFilterGroup getFilterGroup() {
		// TODO use all of the groups
		return (MarkerFieldFilterGroup) filterGroups.iterator().next();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
	 */
	protected Point getInitialSize() {
		Rectangle bounds = getShell().getDisplay().getBounds();
		return new Point(Math.min(500, bounds.width / 3), Math.min(400,
				bounds.height / 2));
	}

	/**
	 * Create a field area in the form for the FilterConfigurationArea
	 * 
	 * @param toolkit
	 * @param form
	 * @param area
	 */
	private void createFieldArea(final FormToolkit toolkit,
			final ScrolledForm form, final FilterConfigurationArea area) {
		final ExpandableComposite expandable = toolkit
				.createExpandableComposite(form.getBody(),
						ExpandableComposite.TWISTIE);
		expandable.setText(area.getTitle());
		expandable.setBackground(form.getBackground());
		expandable.setLayout(new GridLayout());
		expandable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Composite sectionClient = toolkit.createComposite(expandable);
		sectionClient.setLayout(new GridLayout());
		sectionClient.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		sectionClient.setBackground(form.getBackground());
		area.createContents(sectionClient);
		expandable.setClient(sectionClient);

		expandable.addExpansionListener(new IExpansionListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.forms.events.IExpansionListener#expansionStateChanged(org.eclipse.ui.forms.events.ExpansionEvent)
			 */
			public void expansionStateChanged(ExpansionEvent e) {
				
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.forms.events.IExpansionListener#expansionStateChanging(org.eclipse.ui.forms.events.ExpansionEvent)
			 */
			public void expansionStateChanging(ExpansionEvent e) {

			}
		});
	}

}
