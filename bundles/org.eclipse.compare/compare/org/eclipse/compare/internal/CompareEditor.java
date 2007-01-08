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
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.part.*;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * A CompareEditor takes a ICompareEditorInput as input.
 * Most functionality is delegated to the ICompareEditorInput.
 */
public class CompareEditor extends EditorPart implements IReusableEditor, ISaveablesSource, IPropertyChangeListener {

	public final static String CONFIRM_SAVE_PROPERTY= "org.eclipse.compare.internal.CONFIRM_SAVE_PROPERTY"; //$NON-NLS-1$

	private static final int UNINITIALIZED = 0;
	private static final int INITIALIZING = 1;
	private static final int NO_DIFF = 2;
	private static final int CANCELED = 3;
	private static final int INITIALIZED = 4;
	private static final int ERROR = 5;
	private static final int STILL_INITIALIZING = 6;
	private static final int DONE = 7;
	
	private IActionBars fActionBars;
	
	private PageBook fPageBook;
	
	/** the SWT control from the compare editor input*/
	private Control fControl;
	/** the outline page */
	private CompareOutlinePage fOutlinePage;

	private CompareSaveable fSaveable;

	private Control initializingPage;
	
	private int state = UNINITIALIZED;

	private final EditorCompareContainer fContainer = new EditorCompareContainer();

	private class EditorCompareContainer extends CompareContainer {
		
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
		 * @see org.eclipse.compare.internal.CompareContainer#createWorkerJob()
		 */
		protected WorkerJob createWorkerJob() {
			WorkerJob workerJob = new WorkerJob(getWorkerJobName()) {
				public boolean belongsTo(Object family) {
					if (family == CompareEditor.this)
						return true;
					return super.belongsTo(family);
				}
			};
			return workerJob;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.compare.internal.CompareContainer#getWorkerJobName()
		 */
		protected String getWorkerJobName() {
			return NLS.bind(CompareMessages.CompareEditor_11, getTitle());
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.compare.internal.CompareContainer#getWorkbenchPart()
		 */
		public IWorkbenchPart getWorkbenchPart() {
			return CompareEditor.this;
		}
	}
	
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
		
		if (key.equals(IShowInSource.class)) {
			return getEditorInput().getAdapter(key);
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
		if (!(input instanceof CompareEditorInput)) {
			IStatus s= new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.OK, Utilities.getString("CompareEditor.invalidInput"), null); //$NON-NLS-1$
			String title= Utilities.getString("CompareEditor.error.setinput.title"); //$NON-NLS-1$
			String msg= Utilities.getString("CompareEditor.error.setinput.message"); //$NON-NLS-1$
			ErrorDialog.openError(getSite().getShell(), title, msg, s);
			return;
		}
        doSetInput(input);
        // Need to refresh the contributor (see #67888)
        refreshActionBarsContributor();
	}

	private void refreshActionBarsContributor() {
		IEditorSite editorSite= getEditorSite();
        if (editorSite != null) {
	        IEditorActionBarContributor actionBarContributor= editorSite.getActionBarContributor();
	        if (actionBarContributor != null) {
	        		actionBarContributor.setActiveEditor(null);
	        		actionBarContributor.setActiveEditor(this);
	        }
        }
	}
	
	private void doSetInput(IEditorInput input) {
		IEditorInput oldInput= getEditorInput();
		disconnectFromInput(oldInput);
		Point oldSize = null;
		if (oldInput != null) {
			if (fControl != null && !fControl.isDisposed()) {
				oldSize= fControl.getSize();
				fControl.dispose();
				fControl = null;
			}
		}
			
		super.setInput(input);
		
		final CompareEditorInput cei= (CompareEditorInput) input;
		cei.setContainer(fContainer);
		setTitleImage(cei.getTitleImage());
		setPartName(cei.getTitle());
		setTitleToolTip(cei.getToolTipText());
				
		if (input instanceof IPropertyChangeNotifier)
			((IPropertyChangeNotifier)input).addPropertyChangeListener(this);
			
		setState(cei.getCompareResult() == null ? INITIALIZING : INITIALIZED);
		if (fPageBook != null)
			createCompareControl();
		if (fControl != null && oldSize != null)
			fControl.setSize(oldSize);
		
		Job.getJobManager().cancel(this);
		if (cei.getCompareResult() == null) {
			initializeInBackground(cei);
		}
        
        firePropertyChange(IWorkbenchPartConstants.PROP_INPUT);
        
        // We only need to notify of new Saveables if we are changing inputs
        if (oldInput != null) {
        	ISaveablesLifecycleListener lifecycleListener= (ISaveablesLifecycleListener) getSite().getService(ISaveablesLifecycleListener.class);
        	lifecycleListener.handleLifecycleEvent(
        		new SaveablesLifecycleEvent(this, SaveablesLifecycleEvent.POST_OPEN, getSaveables(), false));
        }
	}

	private void disconnectFromInput(IEditorInput oldInput) {
		if (oldInput != null) {
			
			if (oldInput instanceof IPropertyChangeNotifier)
				((IPropertyChangeNotifier)oldInput).removePropertyChangeListener(this);
			
			// Let the workbench know that the old input's saveables are no longer needed
			ISaveablesLifecycleListener lifecycleListener= (ISaveablesLifecycleListener) getSite().getService(ISaveablesLifecycleListener.class);
			lifecycleListener.handleLifecycleEvent(
					new SaveablesLifecycleEvent(this, SaveablesLifecycleEvent.POST_CLOSE, getSaveables(oldInput), false));
		}
	}
	
	protected void initializeInBackground(final CompareEditorInput cei) {
		// Need to cancel any running jobs associated with the oldInput
		Job job = new Job(CompareMessages.CompareEditor_0) {
			protected IStatus run(IProgressMonitor monitor) {
				IStatus status;
				try {
					status = CompareUIPlugin.getDefault().prepareInput(cei, monitor);
					if (status.isOK()) {
						// We need to update the saveables list
						setState(INITIALIZED);
						Saveable[] saveables = getSaveables();
						if (saveables.length > 0) {
							ISaveablesLifecycleListener listener= (ISaveablesLifecycleListener) getSite().getService(ISaveablesLifecycleListener.class);
							if (listener != null) {
								listener.handleLifecycleEvent(
										new SaveablesLifecycleEvent(CompareEditor.this, SaveablesLifecycleEvent.POST_OPEN, saveables, false));
							}
						}
						return Status.OK_STATUS;
					}
					if (status.getCode() == CompareUIPlugin.NO_DIFFERENCE) {
						setState(NO_DIFF);
						return Status.OK_STATUS;
					}
					setState(ERROR);
				} catch (OperationCanceledException e) {
					setState(CANCELED);
					status = Status.CANCEL_STATUS;
				} finally {
					if (monitor.isCanceled())
						setState(CANCELED);
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							createCompareControl();
						}
					});
				}
				return status;
			}
			public boolean belongsTo(Object family) {
				if (family == CompareEditor.this || family == cei)
					return true;
				return cei.belongsTo(family);
			}
		};
		job.setUser(true);
		Utilities.schedule(job, getSite());
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
		fPageBook = new PageBook(parent, SWT.NONE);
		createCompareControl();
		getSite().getKeyBindingService().setScopes(new String[] { "org.eclipse.compare.compareEditorScope" }); //$NON-NLS-1$
	}

	private void createCompareControl() {
		if (fPageBook.isDisposed())
			return;
		IEditorInput input= getEditorInput();
		if (input instanceof CompareEditorInput) {
			CompareEditorInput ci = (CompareEditorInput) input;
			if (ci.getCompareResult() == null) {
				if (getState() == INITIALIZING) {
					setPageLater();
				} else if (getState() == STILL_INITIALIZING) {
					if (initializingPage == null) {
						initializingPage = getInitializingMessagePane(fPageBook);
					}
					fPageBook.showPage(initializingPage);
				} else if (getState() == CANCELED) {
					// Close the editor when we are canceled
					closeEditor();
				} else if (getState() == NO_DIFF) {
					// Prompt and close the editor as well
					setState(DONE);
					closeEditor();
					CompareUIPlugin.getDefault().handleNoDifference();
				} else if (getState() == ERROR) {
					// If an error occurred, close the editor 
					// (the message would be displayed by the progress view)
					closeEditor();
				}
			} else if (fControl == null) {
				fControl= (ci).createContents(fPageBook);
				fPageBook.showPage(fControl);
				PlatformUI.getWorkbench().getHelpSystem().setHelp(fControl, ICompareContextIds.COMPARE_EDITOR);
			}
		}
	}
	
	private void setPageLater() {
		Display.getCurrent().timerExec(1000, new Runnable() {
			public void run() {
				synchronized(CompareEditor.this) {
					if (getState() == INITIALIZING)
						setState(STILL_INITIALIZING);
				}
				createCompareControl();
			}
		});
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
		return getSaveables(input);
	}

	private Saveable[] getSaveables(IEditorInput input) {
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
	
	private Composite getInitializingMessagePane(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBackground(getBackgroundColor(parent));
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);
		
		createDescriptionLabel(composite, CompareMessages.CompareEditor_1);
		return composite;
	}
	
	private Color getBackgroundColor(Composite parent) {
		return parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
	}
	
	private Label createDescriptionLabel(Composite parent, String text) {
		Label description = new Label(parent, SWT.WRAP);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		description.setLayoutData(data);
		description.setText(text);
		description.setBackground(getBackgroundColor(parent));
		return description;
	}
	
	private void closeEditor() {
		getSite().getPage().closeEditor(CompareEditor.this, false);
	}

	private synchronized void setState(int state) {
		this.state = state;
	}

	private int getState() {
		return state;
	}
	
}

