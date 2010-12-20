/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.views;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.help.ui.internal.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;

/**
 * Scope dialog for federated search.
 */
public class ScopeSetDialog extends ListDialog {
	private ScopeSetManager manager;
	private EngineDescriptorManager descManager;
	private static final int NEW_ID = IDialogConstants.CLIENT_ID + 1;
	private static final int EDIT_ID = IDialogConstants.CLIENT_ID + 2;
	private static final int RENAME_ID = IDialogConstants.CLIENT_ID +3;
	private static final int REMOVE_ID = IDialogConstants.CLIENT_ID + 4;
	private Button editButton;
	private Button renameButton;
	private Button removeButton;
	private ArrayList sets;
	private ArrayList operations;
	
	private abstract class PendingOperation {
		ScopeSet set;
		public PendingOperation(ScopeSet set) {
			this.set = set;
		}
		public abstract void commit();
		public abstract void cancel();
	}
	
	private class AddOperation extends PendingOperation {
		public AddOperation(ScopeSet set) {
			super(set);
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
		public RenameOperation(ScopeSet set, String newName) {
			super(set);
			this.newName = newName;
		}
		public void commit() {
			this.set.setName(newName);
		}
		public void cancel() {
		}
	}

/*
	private class EditOperation extends PendingOperation {
		public EditOperation(ScopeSet set) {
			super(set);
		}
		public void commit() {
		}
		public void cancel() {
		}
	}
*/

	private class RemoveOperation extends PendingOperation {
		public RemoveOperation(ScopeSet set) {
			super(set);
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
			PendingOperation op = findOperation(set, RenameOperation.class);
			if (op!=null) {
				RenameOperation rop = (RenameOperation)op;
				return rop.newName;
			}
			return null;
		}
		public Image getImage(Object obj) {
			return HelpUIResources.getImage(IHelpUIConstants.IMAGE_SCOPE_SET);
		}
	}

	/**
	 * @param parent
	 */
	public ScopeSetDialog(Shell parent, ScopeSetManager manager, EngineDescriptorManager descManager) {
		super(parent);
		this.manager = manager;
		this.descManager = descManager;
		this.sets = extractSets(manager.getScopeSets(false));
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
    	PlatformUI.getWorkbench().getHelpSystem().setHelp(listContainer,
		     "org.eclipse.help.ui.searchScope"); //$NON-NLS-1$
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
    	createButton(buttonComposite, NEW_ID, Messages.ScopeSetDialog_new, false); 
       	renameButton = createButton(buttonComposite, RENAME_ID, Messages.ScopeSetDialog_rename, false); 
       	editButton = createButton(buttonComposite, EDIT_ID, Messages.ScopeSetDialog_edit, false); 
       	removeButton = createButton(buttonComposite, REMOVE_ID, Messages.ScopeSetDialog_remove, false); 
       	updateButtons();
    }
	
	public ScopeSet getActiveSet() {
		Object [] result = getResult();
		if (result!=null && result.length>0)
			return (ScopeSet)result[0];
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
		case RENAME_ID:
			doRename();
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
		ScopeSet newSet = new ScopeSet(set, getDefaultName());
		String name = getNewName(newSet.getName(), false);
		if (name!=null) {
			newSet.setName(name);
			scheduleOperation(new AddOperation(newSet));
			sets.add(newSet);
			getTableViewer().refresh();
			updateButtons();
		}
	}
	
	private String getDefaultName() {
		Set namesInUse = new HashSet();
		for (int i=0; i<sets.size(); i++) {
		    ScopeSet set = (ScopeSet)sets.get(i);
		    namesInUse.add(set.getName().toLowerCase());
	    }
		for (int i = 1; i < 1000; i++) {
			String name = Messages.ScopeSetDialog_defaultName + i; 
			if (!namesInUse.contains(name.toLowerCase())) {
				return name;
			}
		}
	    return ""; //$NON-NLS-1$
	}


	private void doEdit() {
		IStructuredSelection ssel = (IStructuredSelection)getTableViewer().getSelection();
		ScopeSet set = (ScopeSet)ssel.getFirstElement();
		if (set!=null) {
			PreferenceManager manager = new ScopePreferenceManager(descManager, set);
			PreferenceDialog dialog = new ScopePreferenceDialog(getShell(), manager, descManager, set.isEditable());
			dialog.setPreferenceStore(set.getPreferenceStore());
			dialog.create();
			dialog.getShell().setText(NLS.bind(Messages.ScopePreferenceDialog_wtitle, set.getName()));
			dialog.open();
		}
	}

	private void doRename() {
		IStructuredSelection ssel = (IStructuredSelection)getTableViewer().getSelection();
		ScopeSet set = (ScopeSet)ssel.getFirstElement();
		if (set!=null) {
			RenameOperation rop = (RenameOperation)findOperation(set, RenameOperation.class);
			String oldName = rop!=null?rop.newName:set.getName();
			String newName = getNewName(oldName, true);
			if (newName!=null) {
				if (rop!=null)
					rop.newName = newName;
				else 
					scheduleOperation(new RenameOperation(set, newName));
				getTableViewer().update(set, null);
				updateButtons();
			}
		}
	}

	private String getNewName(String oldName, boolean isRename) {
		RenameDialog dialog = new RenameDialog(getShell(), oldName);
		for (int i=0; i<sets.size(); i++) {
			ScopeSet set = (ScopeSet)sets.get(i);
			dialog.addOldName(set.getName());
		}
		dialog.create();
		String dialogTitle = isRename ?
		  Messages.RenameDialog_wtitle : Messages.NewDialog_wtitle;
	    dialog.getShell().setText(dialogTitle); 
		if (dialog.open()==RenameDialog.OK) {
			return dialog.getNewName();
		}
		return null;
	}
	
	private void doRemove() {
		IStructuredSelection ssel = (IStructuredSelection)getTableViewer().getSelection();
		ScopeSet set = (ScopeSet)ssel.getFirstElement();
		if (set!=null) {
			scheduleOperation(new RemoveOperation(set));
			sets.remove(set);
			getTableViewer().refresh();
			// Set the selection to the first remaining element
			Object element = getTableViewer().getElementAt(0);
			if (element != null) {
				getTableViewer().setSelection(new StructuredSelection(element));
			} 
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
		boolean editableSet = set!=null && set.isEditable() && !set.isImplicit();
		removeButton.setEnabled(editableSet);
		renameButton.setEnabled(editableSet);
		Button okButton = getOkButton();
		if (okButton!=null)
			okButton.setEnabled(set!=null);
	}
	
	private PendingOperation findOperation(ScopeSet set, Class type) {
		if (operations!=null) {
			for (int i=0; i<operations.size(); i++) {
				PendingOperation op = (PendingOperation)operations.get(i);
				if (op.getClass().equals(type)) {
					if (op.set.equals(set))
						return op;
				}
			}
		}
		return null;
	}	
}
