/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.widgets.*;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.*;

import org.eclipse.ui.*;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.EditorPart;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.*;

import org.eclipse.compare.*;


/**
 * A CompareEditor takes a ICompareEditorInput as input.
 * Most functionality is delegated to the ICompareEditorInput.
 */
public class CompareEditor extends EditorPart implements IPropertyChangeListener {
	
	public final static String CONFIRM_SAVE_PROPERTY= "org.eclipse.compare.internal.CONFIRM_SAVE_PROPERTY"; //$NON-NLS-1$
	
	private IActionBars fActionBars;
	
	
	public CompareEditor() {
	}
		
	/* package */ CompareConfiguration getCompareConfiguration() {
		IEditorInput input= getEditorInput();
		if (input instanceof CompareEditorInput)
			return ((CompareEditorInput)input).getCompareConfiguration();
		return null;
	}
				
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		
		if (!(input instanceof CompareEditorInput))
			throw new PartInitException(Utilities.getString("CompareEditor.invalidInput")); //$NON-NLS-1$
				
		CompareEditorInput cei= (CompareEditorInput) input;
			
		setSite(site);
		setInput(input);
		
		setTitleImage(cei.getTitleImage());
		setTitle(cei.getTitle());
				
		if (input instanceof IPropertyChangeNotifier)
			((IPropertyChangeNotifier)input).addPropertyChangeListener(this);
	}

	public IActionBars getActionBars() {
		return fActionBars;
	}
	
	public void setActionBars(IActionBars actionBars) {
		fActionBars= actionBars;
	}
	
	/*
	 * @see IDesktopPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {
		parent.setData(this);
		
		IEditorInput input= getEditorInput();
		if (input instanceof CompareEditorInput) {
			Control c= ((CompareEditorInput) input).createContents(parent);
			WorkbenchHelp.setHelp(c, ICompareContextIds.COMPARE_EDITOR);
		}
	}
	
	/*
	 * @see DesktopPart#dispose
	 */
	public void dispose() {
	
		IEditorInput input= getEditorInput();
		if (input instanceof IPropertyChangeNotifier)
			((IPropertyChangeNotifier)input).removePropertyChangeListener(this);
								
		super.dispose();
	}
			
	/*
	 * @see IDesktopPart#setFocus
	 */
	public void setFocus() {
		IEditorInput input= getEditorInput();
		if (input instanceof CompareEditorInput)
			((CompareEditorInput)input).setFocus();
	}
	
	/**
	 * Returns false because the editor doesn't support "Save As...".
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	public void gotoMarker(IMarker marker) {
	}
	
	/**
	 * Always throws an AssertionFailedException.
	 */
	/*
	 * @see IEditorPart#doSaveAs()
	 */
	public void doSaveAs() {
		Assert.isTrue(false); // Save As not supported for CompareEditor
	}
	
	/*
	 * @see IEditorPart#doSave()
	 */
	public void doSave(IProgressMonitor progressMonitor) {
		
		final IEditorInput input= getEditorInput();
		
		WorkspaceModifyOperation operation= new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor pm) throws CoreException {
				if (input instanceof CompareEditorInput)
					((CompareEditorInput)input).saveChanges(pm);
			}
		};

		Shell shell= getSite().getShell();
		
		try {
			
			operation.run(progressMonitor);
									
			firePropertyChange(PROP_DIRTY);
			
		} catch (InterruptedException x) {
		} catch (OperationCanceledException x) {
		} catch (InvocationTargetException x) {
			String title= Utilities.getString("CompareEditor.saveError.title"); //$NON-NLS-1$
			String reason= x.getTargetException().getMessage();
			MessageDialog.openError(shell, title, Utilities.getFormattedString("CompareEditor.cantSaveError", reason));	//$NON-NLS-1$
		}
	}	
		
	/*
	 * @see IEditorPart#isDirty()
	 */
	public boolean isDirty() {
		IEditorInput input= getEditorInput();
		if (input instanceof CompareEditorInput)
			return ((CompareEditorInput)input).isSaveNeeded();
		return false;
	}
	
	public void propertyChange(PropertyChangeEvent event) {
		if (isDirty())
			firePropertyChange(PROP_DIRTY);
	}
}

