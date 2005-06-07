/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.*;

import org.eclipse.ui.*;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.*;

import org.eclipse.compare.*;


/**
 * A CompareEditor takes a ICompareEditorInput as input.
 * Most functionality is delegated to the ICompareEditorInput.
 */
public class CompareEditor extends EditorPart implements IReusableEditor {
	
	/**
	 * Internal property change listener for handling changes in the editor's input.
	 */
	class PropertyChangeListener implements IPropertyChangeListener {
		/*
		 * @see IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent event) {
			CompareEditor.this.propertyChange(event);
		}
	}

	public final static String CONFIRM_SAVE_PROPERTY= "org.eclipse.compare.internal.CONFIRM_SAVE_PROPERTY"; //$NON-NLS-1$
	
	private IActionBars fActionBars;
	/** The editor's property change listener. */
	private IPropertyChangeListener fPropertyChangeListener= new PropertyChangeListener();
	/** the SWT control */
	private Control fControl;
	/** the outline page */
	private CompareOutlinePage fOutlinePage;
	/** enable outline */
	
	
	public CompareEditor() {
		// empty default implementation
	}
	
	/* (non-Javadoc)
	 * Method declared on IAdaptable
	 */
	public Object getAdapter(Class key) {
		
		if (key.equals(IContentOutlinePage.class)) {
			Object object= getCompareConfiguration().getProperty(CompareConfiguration.USE_OUTLINE_VIEW);
			if (object instanceof Boolean && ((Boolean)object).booleanValue()) {
				IEditorInput input= getEditorInput();
				if (input instanceof CompareEditorInput) {
					fOutlinePage= new CompareOutlinePage((CompareEditorInput) input);
					return fOutlinePage;
				}
			}
		}
		return super.getAdapter(key);
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
				
		setSite(site);
		setInput(input);
	}
	
	public void setInput(IEditorInput input) {
		try {
	        doSetInput(input);
	        // Need to refresh the contributor (see #67888)
	        IEditorSite editorSite= getEditorSite();
	        if (editorSite != null) {
		        IEditorActionBarContributor actionBarContributor= editorSite.getActionBarContributor();
		        if (actionBarContributor != null) {
		        		actionBarContributor.setActiveEditor(null);
		        		actionBarContributor.setActiveEditor(this);
		        }
	        }
		} catch (CoreException x) {
			String title= Utilities.getString("CompareEditor.error.setinput.title"); //$NON-NLS-1$
			String msg= Utilities.getString("CompareEditor.error.setinput.message"); //$NON-NLS-1$
			ErrorDialog.openError(getSite().getShell(), title, msg, x.getStatus());
		}				
	}
	
	public void doSetInput(IEditorInput input) throws CoreException {
	
		if (!(input instanceof CompareEditorInput)) {
			IStatus s= new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.OK, Utilities.getString("CompareEditor.invalidInput"), null); //$NON-NLS-1$
			throw new CoreException(s);
		}

		IEditorInput oldInput= getEditorInput();
		if (oldInput instanceof IPropertyChangeNotifier)
			((IPropertyChangeNotifier)input).removePropertyChangeListener(fPropertyChangeListener);
			
		super.setInput(input);
		
		CompareEditorInput cei= (CompareEditorInput) input;

		setTitleImage(cei.getTitleImage());
		setPartName(cei.getTitle());	// was setTitle(cei.getTitle());
		setTitleToolTip(cei.getToolTipText());
				
		if (input instanceof IPropertyChangeNotifier)
			((IPropertyChangeNotifier)input).addPropertyChangeListener(fPropertyChangeListener);
			
		if (oldInput != null) {
			if (fControl != null && !fControl.isDisposed()) {
				Point oldSize= fControl.getSize();
				Composite parent= fControl.getParent();
				fControl.dispose();
				createPartControl(parent);
				if (fControl != null)
					fControl.setSize(oldSize);
			}
		}
        
        firePropertyChange(IWorkbenchPartConstants.PROP_INPUT);
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
			fControl= ((CompareEditorInput) input).createContents(parent);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(fControl, ICompareContextIds.COMPARE_EDITOR);
		}
	}
	
	/*
	 * @see DesktopPart#dispose
	 */
	public void dispose() {
	
		IEditorInput input= getEditorInput();
		if (input instanceof IPropertyChangeNotifier)
			((IPropertyChangeNotifier)input).removePropertyChangeListener(fPropertyChangeListener);
								
		super.dispose();
		
		fPropertyChangeListener= null;
	}
			
	/*
	 * @see IDesktopPart#setFocus
	 */
	public void setFocus() {
		IEditorInput input= getEditorInput();
		if (input instanceof CompareEditorInput)
			((CompareEditorInput)input).setFocus();
	}
	
	/*
	 * @see IEditorPart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	public void gotoMarker(IMarker marker) {
		// empty default implemenatation
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
			// NeedWork
		} catch (OperationCanceledException x) {
			// NeedWork
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
		Object old_value= event.getOldValue();
		Object new_value= event.getNewValue();
		if (old_value == null || new_value == null || !old_value.equals(new_value))
			firePropertyChange(PROP_DIRTY);
	}
}

