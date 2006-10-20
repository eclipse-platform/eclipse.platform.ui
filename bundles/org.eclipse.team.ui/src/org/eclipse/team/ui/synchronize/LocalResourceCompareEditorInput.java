/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.synchronize;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.*;
import org.eclipse.compare.structuremergeviewer.*;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.synchronize.LocalResourceSaveableComparison;
import org.eclipse.team.internal.ui.synchronize.LocalResourceTypedElement;
import org.eclipse.team.ui.mapping.SaveableComparison;
import org.eclipse.ui.*;
import org.eclipse.ui.services.IDisposable;

/**
 * A compare editor input that makes use of {@link LocalResourceTypedElement} and
 * {@link LocalResourceSaveableComparison} in order to simplify the creation of
 * compare editors on a single file situated in the left pane. Furthermore, all
 * save operations do not buffer contents but instead write them to disk.
 * 
 * @since 3.3
 */
public abstract class LocalResourceCompareEditorInput extends CompareEditorInput implements ISaveablesSource {

	private class InternalResourceSaveableComparison extends LocalResourceSaveableComparison {
		public InternalResourceSaveableComparison(
				ICompareInput input, CompareEditorInput editorInput) {
			super(input, editorInput);
		}

		protected void fireInputChange() {
			LocalResourceCompareEditorInput.this.fireInputChange();
		}
	}

	private ICompareInputChangeListener compareInputChangeListener;
	private final IWorkbenchPage page;
	private final ListenerList inputChangeListeners = new ListenerList(ListenerList.IDENTITY);
	private Saveable saveable;
	private IPropertyListener propertyListener;
	
	/**
	 * Return a typed element that represents a local file.
	 * @param file the file
	 * @return a typed element that represents a local file.
	 */
	public static ITypedElement createFileElement(IFile file) {
		return new LocalResourceTypedElement(file);
	}
	
	/**
	 * Creates a <code>LocalResourceCompareEditorInput</code> which is initialized with the given
	 * compare configuration.
	 * The compare configuration is passed to subsequently created viewers.
	 *
	 * @param configuration the compare configuration 
	 * @param page the workbench page that will contain the editor
	 */
	public LocalResourceCompareEditorInput(CompareConfiguration configuration, IWorkbenchPage page) {
		super(configuration);
		this.page = page;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#contentsCreated()
	 */
	protected void contentsCreated() {
		super.contentsCreated();
		compareInputChangeListener = new ICompareInputChangeListener() {
			public void compareInputChanged(ICompareInput source) {
				if (source == getCompareResult()) {
					boolean closed = false;
					if (source.getKind() == Differencer.NO_CHANGE) {
						closed = closeEditor(true);
					}
					if (!closed) {
						// The editor was closed either because the compare input still has changes
						// or because the editor input is dirty. In either case, fire the changes
						// to the registered listeners
						propogateInputChange();
					}
				}
			}
		};
		getCompareInput().addCompareInputChangeListener(compareInputChangeListener);
		if (saveable instanceof SaveableComparison) {
			SaveableComparison scm = (SaveableComparison) saveable;
			propertyListener = new IPropertyListener() {
							public void propertyChanged(Object source, int propId) {
								if (propId == SaveableComparison.PROP_DIRTY) {
									setDirty(saveable.isDirty());
								}
							}
						};
			scm.addPropertyListener(propertyListener);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#handleDispose()
	 */
	protected void handleDispose() {
		super.handleDispose();
		getCompareInput().removeCompareInputChangeListener(compareInputChangeListener);
		if (saveable instanceof SaveableComparison) {
			SaveableComparison scm = (SaveableComparison) saveable;
			scm.removePropertyListener(propertyListener);
		}
		if (saveable instanceof LocalResourceSaveableComparison) {
			LocalResourceSaveableComparison rsc = (LocalResourceSaveableComparison) saveable;
			rsc.dispose();
		}
		if (getCompareResult() instanceof IDisposable) {
			((IDisposable) getCompareResult()).dispose();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#prepareInput(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected final Object prepareInput(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		final Object input = internalPrepareInput(monitor);
		return input;
	}

	/**
	 * Method called from {@link #prepareInput(IProgressMonitor)} to obtain the input.
	 * @param monitor a progress monitor
	 * @return the compare input
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 */
	protected abstract ICompareInput internalPrepareInput(IProgressMonitor monitor) 
		throws InvocationTargetException, InterruptedException;
	
	/**
	 * Callback from the resource saveable that is invoked when the resource is
	 * saved so that this input can fire a change event for its input.
	 */
	protected abstract void fireInputChange();
	
	/**
	 * Close the editor if it is not dirty. If it is still dirty, let the 
	 * content merge viewer handle the compare input change.
	 * @param checkForUnsavedChanges whether to check for unsaved changes
	 * @return <code>true</code> if the editor was closed (note that the 
	 * close may be asynchronous)
	 */
	protected boolean closeEditor(boolean checkForUnsavedChanges) {
		if (isSaveNeeded() && checkForUnsavedChanges) {
			return false;
		} else {
			Runnable runnable = new Runnable() {
				public void run() {
					IEditorPart part = getPage().findEditor(LocalResourceCompareEditorInput.this);
					getPage().closeEditor(part, false);
				}
			};
			if (Display.getCurrent() != null) {
				runnable.run();
			} else {
				Display display = getPage().getWorkbenchWindow().getShell().getDisplay();
				display.asyncExec(runnable);
			}
			return true;
		}
	}
	
	private IWorkbenchPage getPage() {
		if (page == null)
			return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		return page;
	}
	
	/* package */ void propogateInputChange() {
		if (!inputChangeListeners.isEmpty()) {
			Object[] allListeners = inputChangeListeners.getListeners();
			for (int i = 0; i < allListeners.length; i++) {
				final ICompareInputChangeListener listener = (ICompareInputChangeListener)allListeners[i];
				SafeRunner.run(new ISafeRunnable() {
					public void run() throws Exception {
						listener.compareInputChanged((ICompareInput)LocalResourceCompareEditorInput.this.getCompareResult());
					}
					public void handleException(Throwable exception) {
						// Logged by the safe runner
					}
				});
			}
		}
	}

	/**
	 * Get the saveable that provides the save behavior for this compare editor input.
	 * The {@link #createSaveable()} is called to create the saveable if it does not yet exist.
	 * @return saveable that provides the save behavior for this compare editor input.
	 */
	protected Saveable getSaveable() {
		if (saveable == null) {
			saveable = createSaveable();
		}
		return saveable;
	}
	
	/**
	 * Create the saveable that provides the save behavior for this compare editor input.
	 * By default, an instance of {@link LocalResourceSaveableComparison} is returned.
	 * @return the saveable that provides the save behavior for this compare editor input
	 */
	protected Saveable createSaveable() {
		Object compareResult = getCompareResult();
		Assert.isNotNull(compareResult, "This method cannot be caled until after prepareInput is called"); //$NON-NLS-1$
		return new InternalResourceSaveableComparison((ICompareInput)compareResult, this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablesSource#getActiveSaveables()
	 */
	public Saveable[] getActiveSaveables() {
		return new Saveable[] { getSaveable() };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablesSource#getSaveables()
	 */
	public Saveable[] getSaveables() {
		return getActiveSaveables();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#addCompareInputChangeListener(org.eclipse.compare.structuremergeviewer.ICompareInput, org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener)
	 */
	public void addCompareInputChangeListener(ICompareInput input,
			ICompareInputChangeListener listener) {
		if (input == getCompareResult()) {
			inputChangeListeners.add(listener);
		} else {
			super.addCompareInputChangeListener(input, listener);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#removeCompareInputChangeListener(org.eclipse.compare.structuremergeviewer.ICompareInput, org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener)
	 */
	public void removeCompareInputChangeListener(ICompareInput input,
			ICompareInputChangeListener listener) {
		if (input == getCompareResult()) {
			inputChangeListeners.remove(listener);
		} else {
			super.removeCompareInputChangeListener(input, listener);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (IFile.class.equals(adapter)) {
			IResource resource = Utils.getResource(getCompareResult());
			if (resource instanceof IFile) {
				return resource;
			}
		}
		return super.getAdapter(adapter);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#getTitleImage()
	 */
	public Image getTitleImage() {
		ImageRegistry reg = TeamUIPlugin.getPlugin().getImageRegistry();
		Image image = reg.get(ITeamUIImages.IMG_SYNC_VIEW);
		if (image == null) {
			image = getImageDescriptor().createImage();
			reg.put(ITeamUIImages.IMG_SYNC_VIEW, image);
		}
		return image;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#getTitle()
	 */
	public String getTitle() {
		return NLS.bind(TeamUIMessages.SyncInfoCompareInput_title, new String[] { getCompareInput().getName() }); 
	}
	
	private ICompareInput getCompareInput() {
		return (ICompareInput)getCompareResult();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return TeamUIPlugin.getImageDescriptor(ITeamUIImages.IMG_SYNC_VIEW);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#findContentViewer(org.eclipse.jface.viewers.Viewer, org.eclipse.compare.structuremergeviewer.ICompareInput, org.eclipse.swt.widgets.Composite)
	 */
	public Viewer findContentViewer(Viewer oldViewer, ICompareInput input, Composite parent) {
		Viewer newViewer = super.findContentViewer(oldViewer, input, parent);
		boolean isNewViewer= newViewer != oldViewer;
		if (isNewViewer && newViewer instanceof IPropertyChangeNotifier && saveable instanceof IPropertyChangeListener) {
			// Register the model for change events if appropriate
			final IPropertyChangeNotifier dsp= (IPropertyChangeNotifier) newViewer;
			final IPropertyChangeListener pcl = (IPropertyChangeListener) saveable;
			dsp.addPropertyChangeListener(pcl);
			Control c= newViewer.getControl();
			c.addDisposeListener(
				new DisposeListener() {
					public void widgetDisposed(DisposeEvent e) {
						dsp.removePropertyChangeListener(pcl);
					}
				}
			);
		}
		return newViewer;
	}

}
