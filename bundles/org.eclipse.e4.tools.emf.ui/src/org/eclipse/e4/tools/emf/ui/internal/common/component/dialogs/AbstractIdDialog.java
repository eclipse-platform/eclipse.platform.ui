/*******************************************************************************
 * Copyright (c) 2013 - 2014 fhv.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nicolaj Hoess <nicohoess@gmail.com> - initial implementation (Bug 396975)
 *     Andrej Brummelhuis <andrejbrummelhuis@gmail.com> - Bug 396975, 395283
 *     Adrian Alcaide - initial implementation (Bug 396975)
 *******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs;

import java.util.List;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.PatternFilter;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public abstract class AbstractIdDialog<ContributionClass, ElementClass extends MApplicationElement> extends SaveDialogBoundsSettingsDialog {

	protected EModelService modelService;

	protected TableViewer viewer;
	protected EditingDomain domain;
	protected IModelResource resource;
	protected ContributionClass contribution;
	protected Messages messages;

	public AbstractIdDialog(Shell parentShell, IModelResource resource, ContributionClass toolbarContribution, EditingDomain domain, EModelService modelService, Messages Messages) {
		super(parentShell);
		this.resource = resource;
		this.modelService = modelService;
		this.messages = Messages;
		this.domain = domain;
		this.contribution = toolbarContribution;
	}

	protected abstract String getShellTitle();

	protected abstract String getDialogTitle();

	protected abstract String getDialogMessage();

	protected abstract String getLabelText();

	protected abstract List<ElementClass> getViewerInput();

	protected abstract EAttribute getFeatureLiteral();

	protected abstract String getListItemInformation(ElementClass listItem);

	@Override
	protected boolean isResizable() {
		return true;
	}

	protected IBaseLabelProvider getLabelProvider() {
		return new StyledCellLabelProvider() {

			@Override
			public void update(ViewerCell cell) {
				ElementClass el = (ElementClass) cell.getElement();
				String elementId = (el.getElementId() != null && el.getElementId().trim().length() > 0) ? el.getElementId() : "(Id missing)"; //$NON-NLS-1$
				StyledString str = new StyledString(elementId);

				String infoString = getListItemInformation(el);
				if (infoString != null && infoString.trim().length() > 0)
					str.append(" - " + getListItemInformation(el), StyledString.DECORATIONS_STYLER); //$NON-NLS-1$

				cell.setText(str.getString());
				cell.setStyleRanges(str.getStyleRanges());
			}
		};
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText(getShellTitle());
		setTitle(getDialogTitle());
		setMessage(getDialogMessage());
		Composite comp = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(comp, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayout(new GridLayout(2, false));

		Label l = new Label(container, SWT.NONE);
		l.setText(getLabelText());

		Text idField = new Text(container, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH);
		idField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final PatternFilter filter = new PatternFilter() {
			@Override
			protected boolean isParentMatch(Viewer viewer, Object element) {
				return viewer instanceof AbstractTreeViewer && super.isParentMatch(viewer, element);
			}
		};

		l = new Label(container, SWT.NONE);
		viewer = new TableViewer(container);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(getLabelProvider());
		viewer.addFilter(filter);
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		});

		ControlFactory.attachFiltering(idField, viewer, filter);

		viewer.setInput(getViewerInput());

		return comp;
	}

	@Override
	protected void okPressed() {
		if (!viewer.getSelection().isEmpty()) {
			ElementClass el = (ElementClass) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
			Command cmd = SetCommand.create(domain, contribution, getFeatureLiteral(), el.getElementId());
			if (cmd.canExecute()) {
				domain.getCommandStack().execute(cmd);
				super.okPressed();
			}
		}
	}

}