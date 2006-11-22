/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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

import org.eclipse.compare.*;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * A CompareEditor takes a ICompareEditorInput as input.
 * Most functionality is delegated to the ICompareEditorInput.
 */
public class CompareEditor extends EditorPart implements IReusableEditor, ISaveablesSource, ICompareContainer, IPropertyChangeListener {

	public final static String CONFIRM_SAVE_PROPERTY= "org.eclipse.compare.internal.CONFIRM_SAVE_PROPERTY"; //$NON-NLS-1$
	
	private IActionBars fActionBars;
	/** the SWT control */
	private Control fControl;
	/** the outline page */
	private CompareOutlinePage fOutlinePage;

	private CompareSaveable fSaveable;
	
	/**
	 * No-argument constructor required for extension points.
	 */
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
	
	/*
	 * Helper method used by ComapreEditorConfiguration to get at the compare configuration of the editor
	 */
	/* package */ CompareConfiguration getCompareConfiguration() {
		IEditorInput input= getEditorInput();
		if (input instanceof CompareEditorInput)
			return ((CompareEditorInput)input).getCompareConfiguration();
		return null;
	}
				
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
	 */
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		
		if (!(input instanceof CompareEditorInput))
			throw new PartInitException(Utilities.getString("CompareEditor.invalidInput")); //$NON-NLS-1$
				
		setSite(site);
		setInput(input);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#setInput(org.eclipse.ui.IEditorInput)
	 */
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
	
	private void doSetInput(IEditorInput input) throws CoreException {
	
		if (!(input instanceof CompareEditorInput)) {
			IStatus s= new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.OK, Utilities.getString("CompareEditor.invalidInput"), null); //$NON-NLS-1$
			throw new CoreException(s);
		}

		IEditorInput oldInput= getEditorInput();
		if (oldInput instanceof IPropertyChangeNotifier)
			((IPropertyChangeNotifier)input).removePropertyChangeListener(this);

		ISaveablesLifecycleListener lifecycleListener= null;
		if (oldInput != null) {
			lifecycleListener= (ISaveablesLifecycleListener) getSite().getService(ISaveablesLifecycleListener.class);
			lifecycleListener.handleLifecycleEvent(
				new SaveablesLifecycleEvent(this, SaveablesLifecycleEvent.POST_CLOSE, getSaveables(), false));
		}
			
		super.setInput(input);
		
		CompareEditorInput cei= (CompareEditorInput) input;
		cei.setContainer(this);

		setTitleImage(cei.getTitleImage());
		setPartName(cei.getTitle());
		setTitleToolTip(cei.getToolTipText());
				
		if (input instanceof IPropertyChangeNotifier)
			((IPropertyChangeNotifier)input).addPropertyChangeListener(this);
			
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
        
        if (lifecycleListener != null) {
        	lifecycleListener.handleLifecycleEvent(
        		new SaveablesLifecycleEvent(this, SaveablesLifecycleEvent.POST_OPEN, getSaveables(), false));
        }
	}
	
	/*
	 * Helper method used to find an action bars using the Utilities#findActionsBars(Control)
	 */
	public IActionBars getActionBars() {
		return fActionBars;
	}
	
	/*
	 * Set the action bars so the Utilities class can access it.
	 */
	/* package */ void setActionBars(IActionBars actionBars) {
		fActionBars= actionBars;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		parent.setData(this);
		
		IEditorInput input= getEditorInput();
		if (input instanceof CompareEditorInput) {
			fControl= ((CompareEditorInput) input).createContents(parent);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(fControl, ICompareContextIds.COMPARE_EDITOR);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	public void dispose() {
	
		IEditorInput input= getEditorInput();
		if (input instanceof IPropertyChangeNotifier)
			((IPropertyChangeNotifier)input).removePropertyChangeListener(this);
								
		super.dispose();
	}
			
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
		IEditorInput input= getEditorInput();
		if (input instanceof CompareEditorInput)
			((CompareEditorInput)input).setFocus();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	/* (non-Javadoc)
	 * Always throws an AssertionFailedException.
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	public void doSaveAs() {
		Assert.isTrue(false); // Save As not supported for CompareEditor
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
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
		
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#isDirty()
	 */
	public boolean isDirty() {
		IEditorInput input= getEditorInput();
		if (input instanceof ISaveablesSource) {
			ISaveablesSource sms= (ISaveablesSource) input;
			Saveable[] models= sms.getSaveables();
			for (int i= 0; i < models.length; i++) {
				Saveable model= models[i];
				if (model.isDirty())
					return true;
			}
		}
		if (input instanceof CompareEditorInput)
			return ((CompareEditorInput)input).isSaveNeeded();
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(CompareEditorInput.DIRTY_STATE)) {
			Object old_value= event.getOldValue();
			Object new_value= event.getNewValue();
			if (old_value == null || new_value == null || !old_value.equals(new_value))
				firePropertyChange(PROP_DIRTY);
		} else if (event.getProperty().equals(CompareEditorInput.PROP_TITLE)) {
			setPartName(((CompareEditorInput)getEditorInput()).getTitle());
			setTitleToolTip(((CompareEditorInput)getEditorInput()).getToolTipText());
		} else if (event.getProperty().equals(CompareEditorInput.PROP_TITLE_IMAGE)) {
			setTitleImage(((CompareEditorInput)getEditorInput()).getTitleImage());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablesSource#getModels()
	 */
	public Saveable[] getSaveables() {
		IEditorInput input= getEditorInput();
		if (input instanceof ISaveablesSource) {
			ISaveablesSource source= (ISaveablesSource) input;
			return source.getSaveables();
		}
		return new Saveable[] { getSaveable() };
	}

	private Saveable getSaveable() {
		if (fSaveable == null) {
			fSaveable= new CompareSaveable();
		}
		return fSaveable;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablesSource#getActiveModels()
	 */
	public Saveable[] getActiveSaveables() {
		IEditorInput input= getEditorInput();
		if (input instanceof ISaveablesSource) {
			ISaveablesSource source= (ISaveablesSource) input;
			return source.getActiveSaveables();
		}
		return new Saveable[] { getSaveable() };
	}
	
	private class CompareSaveable extends Saveable {

		public String getName() {
			return CompareEditor.this.getPartName();
		}

		public String getToolTipText() {
			return CompareEditor.this.getTitleToolTip();
		}

		public ImageDescriptor getImageDescriptor() {
			return ImageDescriptor.createFromImage(CompareEditor.this.getTitleImage());
		}

		public void doSave(IProgressMonitor monitor) throws CoreException {
			CompareEditor.this.doSave(monitor);
		}

		public boolean isDirty() {
			return CompareEditor.this.isDirty();
		}

		public boolean equals(Object object) {
			return object == this;
		}

		public int hashCode() {
			return CompareEditor.this.hashCode();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.ICompareContainer#removeCompareInputChangeListener(org.eclipse.compare.structuremergeviewer.ICompareInput, org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener)
	 */
	public void removeCompareInputChangeListener(ICompareInput input,
			ICompareInputChangeListener listener) {
		input.removeCompareInputChangeListener(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.ICompareContainer#addCompareInputChangeListener(org.eclipse.compare.structuremergeviewer.ICompareInput, org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener)
	 */
	public void addCompareInputChangeListener(ICompareInput input,
			ICompareInputChangeListener listener) {
		input.addCompareInputChangeListener(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.ICompareContainer#registerContextMenu(org.eclipse.jface.action.MenuManager, org.eclipse.jface.viewers.ISelectionProvider)
	 */
	public void registerContextMenu(MenuManager menu, ISelectionProvider provider) {
		getSite().registerContextMenu(menu, provider);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.ICompareContainer#setStatusMessage(java.lang.String)
	 */
	public void setStatusMessage(String message) {
		if (fActionBars != null) {
			IStatusLineManager slm= fActionBars.getStatusLineManager();
			if (slm != null) {
				slm.setMessage(message);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.ICompareContainer#getServiceLocator()
	 */
	public IServiceLocator getServiceLocator() {
		return getSite();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.operation.IRunnableContext#run(boolean, boolean, org.eclipse.jface.operation.IRunnableWithProgress)
	 */
	public void run(boolean fork, boolean cancelable,
			IRunnableWithProgress runnable) throws InvocationTargetException,
			InterruptedException {
		PlatformUI.getWorkbench().getProgressService().run(fork, cancelable, runnable);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.ICompareContainer#getNavigator()
	 */
	public ICompareNavigator getNavigator() {
		return null;
	}
	
}

