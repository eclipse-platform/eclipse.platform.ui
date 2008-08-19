/*******************************************************************************
 * Copyright (c) 2008 Aleksandra Wozniak and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aleksandra Wozniak (aleksandra.k.wozniak@gmail.com) - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.part.ResourceTransfer;

/**
 * This is a dialog that can invoke the compare editor on chosen files.
 * 
 * @since 3.4
 */
public class CompareWithOtherResourceDialog extends TitleAreaDialog {

	private int CLEAR_RETURN_CODE = 150; // any number != 0
	private int MIN_WIDTH = 300;
	private int MIN_HEIGHT = 175;

	private class FileTextDragListener implements DragSourceListener {

		private InternalSection section;

		public FileTextDragListener(InternalSection section) {
			this.section = section;
		}

		public void dragFinished(DragSourceEvent event) {
			section.fileText.setText(""); //$NON-NLS-1$
		}

		public void dragSetData(DragSourceEvent event) {
			if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
				event.data = section.fileText.getText();
			} else
				event.data = section.fileText.getText();
		}

		public void dragStart(DragSourceEvent event) {
			if (section.fileText.getText() == null)
				event.doit = false;
		}
	}

	private class FileTextDropListener implements DropTargetListener {

		private InternalSection section;
		private ResourceTransfer resourceTransfer;
		private TextTransfer textTransfer;

		public FileTextDropListener(InternalSection section) {
			this.section = section;
			resourceTransfer = ResourceTransfer.getInstance();
			textTransfer = TextTransfer.getInstance();
		}

		public void dragEnter(DropTargetEvent event) {

			if (event.detail == DND.DROP_DEFAULT) {
				if ((event.operations & DND.DROP_COPY) != 0)
					event.detail = DND.DROP_COPY;
				else
					event.detail = DND.DROP_NONE;
			}

			for (int i = 0; i < event.dataTypes.length; i++) {
				if (resourceTransfer.isSupportedType(event.dataTypes[i])
						|| textTransfer.isSupportedType(event.dataTypes[i])) {
					event.currentDataType = event.dataTypes[i];
					if (event.detail != DND.DROP_COPY)
						event.detail = DND.DROP_NONE;
					break;
				}
			}
		}

		public void dragLeave(DropTargetEvent event) {
			// intentionally empty
		}

		public void dragOperationChanged(DropTargetEvent event) {

			if (event.detail == DND.DROP_DEFAULT) {
				if ((event.operations & DND.DROP_COPY) != 0)
					event.detail = DND.DROP_COPY;
				else
					event.detail = DND.DROP_NONE;
			} else if (resourceTransfer.isSupportedType(event.currentDataType)) {
				if (event.detail != DND.DROP_COPY)
					event.detail = DND.DROP_NONE;
			}
		}

		public void dragOver(DropTargetEvent event) {
			// intentionally empty
		}

		public void drop(DropTargetEvent event) {

			if (textTransfer.isSupportedType(event.currentDataType)) {
				String txt = (String) event.data;
				section.setResource(ResourcesPlugin.getWorkspace().getRoot()
						.findMember(txt));
			} else if (resourceTransfer.isSupportedType(event.currentDataType)) {
				IResource[] files = (IResource[]) event.data;
				section.setResource(files[0]);
			}

			updateErrorInfo();
		}

		public void dropAccept(DropTargetEvent event) {
			// intentionally empty
		}

	}

	private abstract class InternalSection {

		protected Group group;
		protected Text fileText;
		private IResource resource;

		public InternalSection(Composite parent) {
			createContents(parent);
		}

		private InternalSection() {
			// not to instantiate
		}

		public void createContents(Composite parent) {
			createGroup(parent);
			createFileLabel();
			createFileCombo();
			initDrag();
			initDrop();
		}

		public IResource getResource() {
			return resource;
		}

		public void setResource(IResource resource) {
			this.resource = resource;
			String txt = resource.getFullPath().toString();
			fileText.setText(txt);
		}

		public void setResource(String s) {
			IResource tmp = ResourcesPlugin.getWorkspace().getRoot()
					.findMember(s);
			if (tmp instanceof IWorkspaceRoot)
				resource = null;
			else
				resource = tmp;

		}

		protected void clearResource() {
			resource = null;
			fileText.setText(""); //$NON-NLS-1$
			updateErrorInfo();
		}

		protected void initDrag() {
			DragSource source = new DragSource(fileText, DND.DROP_MOVE
					| DND.DROP_COPY | DND.DROP_DEFAULT);
			Transfer[] types = new Transfer[] { TextTransfer.getInstance(),
					ResourceTransfer.getInstance() };
			source.setTransfer(types);
			source.addDragListener(new FileTextDragListener(this));
		}

		protected void initDrop() {
			DropTarget target = new DropTarget(fileText, DND.DROP_MOVE
					| DND.DROP_COPY | DND.DROP_DEFAULT);
			Transfer[] types = new Transfer[] { TextTransfer.getInstance(),
					ResourceTransfer.getInstance() };
			target.setTransfer(types);
			target.addDropListener(new FileTextDropListener(this));
		}

		protected void createGroup(Composite parent) {
			group = new Group(parent, SWT.NONE);
			group.setLayout(new GridLayout(3, false));
			group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}

		protected void createFileCombo() {
			fileText = new Text(group, SWT.BORDER);
			fileText
					.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			fileText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					setResource(fileText.getText());
					updateErrorInfo();
				}
			});

			fileText.addSelectionListener(new SelectionListener() {

				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}

				public void widgetSelected(SelectionEvent e) {
					setResource(fileText.getText());
					updateErrorInfo();
				}

			});
		}

		protected void createFileLabel() {
			final Label fileLabel = new Label(group, SWT.NONE);
			fileLabel.setText(CompareMessages.CompareWithOther_fileLabel);
		}
	}

	private class InternalGroup extends InternalSection {

		public InternalGroup(Composite parent) {
			createContents(parent);
		}

		public void setText(String text) {
			group.setText(text);
		}

		public void setLayoutData(GridData layoutData) {
			group.setLayoutData(layoutData);
		}
	}

	private class InternalExpandable extends InternalSection {

		private ExpandableComposite expandable;
		private Button clearButton;

		public InternalExpandable(Composite parent) {
			createContents(parent);
		}

		public void createContents(Composite parent) {
			createGroup(parent);
			createFileLabel();
			createFileCombo();
			createClearButton(group);
			initDrag();
			initDrop();
		}

		public void createGroup(Composite parent) {
			final Composite p = parent;
			expandable = new ExpandableComposite(parent, SWT.NONE,
					ExpandableComposite.TREE_NODE | ExpandableComposite.TWISTIE);
			super.createGroup(expandable);
			expandable.setClient(group);
			expandable.addExpansionListener(new ExpansionAdapter() {
				public void expansionStateChanged(ExpansionEvent e) {
					p.layout();
				}
			});
		}

		protected void createClearButton(Composite parent) {
			clearButton = createButton(parent, CLEAR_RETURN_CODE,
					CompareMessages.CompareWithOther_clear, false);
			clearButton.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}

				public void widgetSelected(SelectionEvent e) {
					clearResource();
				}
			});
		}

		public void setText(String text) {
			expandable.setText(text);
			group.setText(text);
		}

		public void setLayoutData(GridData layoutData) {
			expandable.setLayoutData(layoutData);
		}
	}

	private Button okButton;
	private InternalGroup rightPanel, leftPanel;
	private InternalExpandable ancestorPanel;
	private ISelection fselection;

	/**
	 * Creates the dialog.
	 * 
	 * @param shell
	 *            a shell
	 * @param selection
	 *            if the selection is not null, it will be set as initial files
	 *            for comparison
	 * @since 3.4
	 */
	protected CompareWithOtherResourceDialog(Shell shell, ISelection selection) {
		super(shell);
		setShellStyle(SWT.MODELESS | SWT.RESIZE | SWT.MAX);
		fselection = selection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets
	 * .Composite)
	 */
	protected Control createDialogArea(Composite parent) {

		Composite mainPanel = new Composite(parent, SWT.NULL);
		mainPanel.setLayout(new GridLayout(2, true));
		mainPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		ancestorPanel = new InternalExpandable(mainPanel);
		ancestorPanel.setText(CompareMessages.CompareWithOther_ancestor);
		GridData ancestorGD = new GridData(SWT.FILL, SWT.FILL, true, false);
		ancestorGD.horizontalSpan = 2;
		ancestorPanel.setLayoutData(ancestorGD);

		leftPanel = new InternalGroup(mainPanel);
		leftPanel.setText(CompareMessages.CompareWithOther_leftPanel);
		leftPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		rightPanel = new InternalGroup(mainPanel);
		rightPanel.setText(CompareMessages.CompareWithOther_rightPanel);
		rightPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		setSelection(fselection);
		getShell().setText(CompareMessages.CompareWithOther_dialogTitle);
		setTitle(CompareMessages.CompareWithOther_dialogMessage);
		getShell().setMinimumSize(convertHorizontalDLUsToPixels(MIN_WIDTH),
				convertVerticalDLUsToPixels(MIN_HEIGHT));

		return mainPanel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse
	 * .swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		okButton = getButton(IDialogConstants.OK_ID);
		updateErrorInfo();
		setMessage(CompareMessages.CompareWithOther_info);
	}

	private void setSelection(ISelection selection) {
		IResource[] selectedResources = Utilities.getResources(selection);
		switch (selectedResources.length) {
		case 1:
			leftPanel.setResource(selectedResources[0]);
			break;
		case 2:
			leftPanel.setResource(selectedResources[0]);
			rightPanel.setResource(selectedResources[1]);
			break;
		case 3:
			ancestorPanel.setResource(selectedResources[0]);
			ancestorPanel.expandable.setExpanded(true);
			leftPanel.setResource(selectedResources[1]);
			rightPanel.setResource(selectedResources[2]);
			break;
		}
	}

	private boolean isComparePossible() {
		IResource[] resources;
		if (ancestorPanel.getResource() == null) {
			resources = new IResource[] { leftPanel.getResource(),
					rightPanel.getResource() };
		} else {
			resources = new IResource[] { ancestorPanel.getResource(),
					leftPanel.getResource(), rightPanel.getResource() };
		}

		ResourceCompareInput r = new ResourceCompareInput(
				new CompareConfiguration());
		return r.isEnabled(new StructuredSelection(resources));
	}

	private void updateErrorInfo() {
		if (okButton != null) {
			if (leftPanel.getResource() == null
					|| rightPanel.getResource() == null) {
				setMessage(CompareMessages.CompareWithOther_error_empty,
						IMessageProvider.ERROR);
				okButton.setEnabled(false);
			} else if (ancestorPanel.getResource() == null
					&& ancestorPanel.fileText.getText() != "") { //$NON-NLS-1$
				setMessage(CompareMessages.CompareWithOther_warning_two_way,
						IMessageProvider.WARNING);
			} else if (!isComparePossible()) {
				setMessage(
						CompareMessages.CompareWithOther_error_not_comparable,
						IMessageProvider.ERROR);
				okButton.setEnabled(false);
			} else {
				setMessage(CompareMessages.CompareWithOther_info);
				okButton.setEnabled(true);
			}
		}
	}

	/**
	 * Returns table with selected resources. If any resource wasn't chosen in
	 * the ancestor panel, table has only two elements -- resources chosen in
	 * left and right panel. In the other case table contains all three
	 * resources.
	 * 
	 * @return table with selected resources
	 */
	public IResource[] getResult() {
		IResource[] resources;
		IResource rightResource = rightPanel.getResource();
		IResource leftResource = leftPanel.getResource();
		IResource ancestorResource = ancestorPanel.getResource();
		if (ancestorResource == null)
			resources = new IResource[] { leftResource, rightResource };
		else
			resources = new IResource[] { ancestorResource, leftResource,
					rightResource };
		return resources;
	}
}
