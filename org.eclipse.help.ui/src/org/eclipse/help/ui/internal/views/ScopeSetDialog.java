/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.views;

import java.util.ArrayList;

import org.eclipse.help.ui.internal.*;
import org.eclipse.help.ui.internal.HelpUIResources;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListDialog;

/**
 * Scope dialog for federated search.
 */
public class ScopeSetDialog extends ListDialog {
	private ScopeSetManager manager;
	private EngineDescriptorManager descManager;
	private static final int NEW_ID = IDialogConstants.CLIENT_ID + 1;
	private static final int EDIT_ID = IDialogConstants.CLIENT_ID + 2;
	private static final int REMOVE_ID = IDialogConstants.CLIENT_ID + 3;
	private Button newButton;
	private Button editButton;
	private Button removeButton;
	private ArrayList sets;
	private ArrayList operations;
	
	private abstract class PendingOperation {
		public abstract void commit();
		public abstract void cancel();
	}
	
	private class AddOperation extends PendingOperation {
		private ScopeSet set;
		public AddOperation(ScopeSet set) {
			this.set = set;
		}
		public void commit() {
			manager.add(set);
		}
		public void cancel() {
			set.dispose();
		}
	}

	private class RenameOperation extends PendingOperation {
		private String newName;
		private ScopeSet set;
		public RenameOperation(ScopeSet set, String newName) {
			this.set = set;
			this.newName = newName;
		}
		public void commit() {
			this.set.setName(newName);
		}
		public void cancel() {
		}
	}

	private class EditOperation extends PendingOperation {
		private ScopeSet set;
	
		public EditOperation(ScopeSet set) {
			this.set = set;
		}
		public void commit() {
		}
		public void cancel() {
		}
	}

	private class RemoveOperation extends PendingOperation {
		private ScopeSet set;
		
		public RemoveOperation(ScopeSet set) {
			this.set = set;
		}
		public void commit() {
			manager.remove(set);
		}
		public void cancel() {
		}
	}
	
	private class ScopeContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return sets.toArray();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	private class ScopeLabelProvider extends LabelProvider {
		public String getText(Object obj) {
			String name = findNewName((ScopeSet)obj);
			if (name!=null)
				return name;
			return ((ScopeSet)obj).getName();
		}
		private String findNewName(ScopeSet set) {
			if (operations!=null) {
				for (int i=0; i<operations.size(); i++) {
					PendingOperation op = (PendingOperation)operations.get(i);
					if (op instanceof RenameOperation) {
						RenameOperation rop = (RenameOperation)op;
						if (rop.set.equals(set))
							return rop.newName;
					}
				}
			}
			return null;
		}
		public Image getImage(Object obj) {
			return HelpUIResources.getImage(IHelpUIConstants.IMAGE_CONTAINER);
		}
	}

	/**
	 * @param parent
	 */
	public ScopeSetDialog(Shell parent, ScopeSetManager manager, EngineDescriptorManager descManager) {
		super(parent);
		this.manager = manager;
		this.descManager = descManager;
		this.sets = extractSets(manager.getScopeSets());
		setContentProvider(new ScopeContentProvider());
		setLabelProvider(new ScopeLabelProvider());
		setInitialSelections(new Object[] { manager.getActiveSet() });
	}
	
	private ArrayList extractSets(ScopeSet[] array) {
		ArrayList list = new ArrayList();
		for (int i=0; i<array.length; i++) {
			list.add(array[i]);
		}
		return list;
	}
	
    protected Control createDialogArea(Composite container) {
    	Composite listContainer = (Composite)super.createDialogArea(container);
    	createEditingButtons(listContainer);
    	getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons();
			}
		});
    	return listContainer;
    }
    
    private void createEditingButtons(Composite composite) {
		Composite buttonComposite= new Composite(composite, SWT.RIGHT);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		buttonComposite.setLayout(layout);
		GridData data= new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL);
		data.grabExcessHorizontalSpace= true;
		composite.setData(data);
    	newButton = createButton(buttonComposite, NEW_ID, HelpUIResources.getString("ScopeSetDialog.new"), false); //$NON-NLS-1$
       	editButton = createButton(buttonComposite, EDIT_ID, HelpUIResources.getString("ScopeSetDialog.edit"), false); //$NON-NLS-1$
       	removeButton = createButton(buttonComposite, REMOVE_ID, HelpUIResources.getString("ScopeSetDialog.remove"), false); //$NON-NLS-1$
       	updateButtons();
    }
	
	public ScopeSet getActiveSet() {
		Object [] result = getResult();
		if (result!=null && result.length>0)
			return (ScopeSet)result[0];
		else
			return null;
	}
	protected void okPressed() {
    	if (operations!=null) {
    		for (int i=0; i<operations.size(); i++) {
    			PendingOperation operation = (PendingOperation)operations.get(i);
    			operation.commit();
    		}
    		operations = null;
    	}
    	super.okPressed();
    }
	
	protected void cancelPressed() {
    	if (operations!=null) {
    		for (int i=0; i<operations.size(); i++) {
    			PendingOperation operation = (PendingOperation)operations.get(i);
    			operation.cancel();
    		}
    		operations = null;
    	}
		super.cancelPressed();
	}
	
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case NEW_ID:
			doNew();
			break;
		case EDIT_ID:
			doEdit();
			break;
		case REMOVE_ID:
			doRemove();
			break;
		}
		super.buttonPressed(buttonId);
	}
	
	private void doNew() {
		IStructuredSelection ssel = (IStructuredSelection)getTableViewer().getSelection();
		ScopeSet set = (ScopeSet)ssel.getFirstElement();
		ScopeSet newSet = new ScopeSet(set);
		scheduleOperation(new AddOperation(newSet));
		sets.add(newSet);
		getTableViewer().refresh();
		updateButtons();
	}

	private void doEdit() {
		IStructuredSelection ssel = (IStructuredSelection)getTableViewer().getSelection();
		ScopeSet set = (ScopeSet)ssel.getFirstElement();
		if (set!=null) {
			PreferenceManager manager = new ScopePreferenceManager(descManager, set);
			PreferenceDialog dialog = new ScopePreferenceDialog(getShell(), manager, descManager);
			dialog.setPreferenceStore(set.getPreferenceStore());
			dialog.open();
		}
	}
	
	private void doRemove() {
		IStructuredSelection ssel = (IStructuredSelection)getTableViewer().getSelection();
		ScopeSet set = (ScopeSet)ssel.getFirstElement();
		if (set!=null) {
			scheduleOperation(new RemoveOperation(set));
			sets.remove(set);
			getTableViewer().refresh();
			updateButtons();
		}
	}
	
	private void scheduleOperation(PendingOperation op) {
		if (operations==null)
			operations = new ArrayList();
		operations.add(op);
	}
	
	private void updateButtons() {
		IStructuredSelection ssel = (IStructuredSelection)getTableViewer().getSelection();
		editButton.setEnabled(ssel.isEmpty()==false);
		ScopeSet set = (ScopeSet)ssel.getFirstElement();
		removeButton.setEnabled(set!=null && !set.isDefault());
		Button okButton = getOkButton();
		if (okButton!=null)
			okButton.setEnabled(set!=null);
	}
}