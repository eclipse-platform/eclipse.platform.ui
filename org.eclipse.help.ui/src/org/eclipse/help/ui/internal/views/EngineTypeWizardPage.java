/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.views;

import org.eclipse.help.ui.internal.Messages;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

public class EngineTypeWizardPage extends WizardPage {
	private TableViewer tableViewer;
	private EngineTypeDescriptor [] engineTypes;
	private EngineTypeDescriptor selection;

	class EngineContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			return engineTypes;
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	class EngineLabelProvider extends LabelProvider implements ITableLabelProvider {

		@Override
		public String getText(Object obj) {
			EngineTypeDescriptor desc = (EngineTypeDescriptor)obj;
			return desc.getLabel();
		}

		@Override
		public Image getImage(Object obj) {
			EngineTypeDescriptor desc = (EngineTypeDescriptor)obj;
			return desc.getIconImage();
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return getImage(element);
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			return getText(element);
		}
	}

	public EngineTypeWizardPage(EngineTypeDescriptor[] engineTypes) {
		super("engineType"); //$NON-NLS-1$
		setTitle(Messages.EngineTypeWizardPage_title);
		setDescription(Messages.EngineTypeWizardPage_desc);
		this.engineTypes = engineTypes;
	}

	@Override
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
	     "org.eclipse.help.ui.searchScope"); //$NON-NLS-1$
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		Label label = new Label(container, SWT.NULL);
		label.setText(Messages.EngineTypeWizardPage_label);
		label.setFont(font);
		tableViewer = new TableViewer(container);
		tableViewer.setContentProvider(new EngineContentProvider());
		tableViewer.setLabelProvider(new EngineLabelProvider());
		tableViewer.addSelectionChangedListener(event -> {
			setPageComplete(!event.getSelection().isEmpty());
			selection = (EngineTypeDescriptor) ((IStructuredSelection) event.getSelection())
					.getFirstElement();
		});
		tableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		tableViewer.setInput(engineTypes);
		tableViewer.getTable().setFont(font);
		setControl(container);
		setPageComplete(false);
	}
	public EngineTypeDescriptor getSelectedEngineType() {
		return selection;
	}
}
