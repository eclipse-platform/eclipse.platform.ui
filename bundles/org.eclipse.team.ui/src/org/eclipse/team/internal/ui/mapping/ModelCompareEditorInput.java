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
package org.eclipse.team.internal.ui.mapping;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.*;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.ICache;
import org.eclipse.team.core.ICacheListener;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.synchronize.SynchronizeView;
import org.eclipse.team.ui.mapping.*;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ModelSynchronizeParticipant;
import org.eclipse.ui.*;

public class ModelCompareEditorInput extends CompareEditorInput implements ISaveablesSource, IPropertyListener {

	private final ModelSynchronizeParticipant participant;
	private ICompareInput input;
	private Saveable saveable;
	private ICacheListener contextListener;
	private final IWorkbenchPage page;
	private final Object modelObject;
	private ICompareInputChangeNotifier changeNotifier;
	private ICompareInputChangeListener changeListener;

	public ModelCompareEditorInput(ModelSynchronizeParticipant participant, Object modelObject, ICompareInput input, IWorkbenchPage page) {
		super(new CompareConfiguration());
		Assert.isNotNull(page);
		Assert.isNotNull(participant);
		Assert.isNotNull(input);
		this.page = page;
		this.participant = participant;
		this.input = input;
		this.modelObject = modelObject;
		this.saveable = asSaveable(this.input);
		setDirty(saveable.isDirty());
	}

	protected void contentsCreated() {
		super.contentsCreated();
		contextListener = new ICacheListener() {
			public void cacheDisposed(ICache cache) {
				closeEditor();
			}
		};
		participant.getContext().getCache().addCacheListener(contextListener);
		registerForInputStateChanges();
		if (saveable instanceof SaveableComparison) {
			SaveableComparison scm = (SaveableComparison) saveable;
			scm.addPropertyListener(this);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#handleDispose()
	 */
	protected void handleDispose() {
		super.handleDispose();
		participant.getContext().getCache().removeCacheListener(contextListener);
		deregisterForInputStateChanges();
		if (saveable instanceof SaveableComparison) {
			SaveableComparison scm = (SaveableComparison) saveable;
			scm.removePropertyListener(ModelCompareEditorInput.this);
		}
	}
	
	private void registerForInputStateChanges() {
		ISynchronizationCompareAdapter adapter = Utils.getCompareAdapter(modelObject);
		if (adapter != null) {
			changeNotifier = adapter.getChangeNotifier(participant.getContext(), input);
			if (changeNotifier != null) {
				changeListener = new ICompareInputChangeListener() {
					public void compareInputsChanged(ICompareInputChangeEvent event) {
						if (event.isInSync(input)) {
							closeEditor();
						} else if (event.hasChanged(input)) {
							reset();
						}
					}
				};
				changeNotifier.addChangeListener(changeListener);
				changeNotifier.connect(input);
			}
		}
	}
	
	protected void reset() {
		ISynchronizationCompareAdapter adapter = Utils.getCompareAdapter(modelObject);
		ICompareInput newInput = adapter.asCompareInput(participant.getContext(), modelObject);
		if (newInput != null) {
			deregisterForInputStateChanges();
			input = newInput;
			updateSaveable();
			registerForInputStateChanges();
			Display display = page.getWorkbenchWindow().getShell().getDisplay();
			display.asyncExec(new Runnable() {
				public void run() {
					try {
						getRunnableContext().run(true, true, new IRunnableWithProgress() {
							public void run(IProgressMonitor monitor) throws InvocationTargetException,
									InterruptedException {
								refresh(monitor);
							}
						});
					} catch (InvocationTargetException e) {
						handleError("An error occurred while updating the comparison", e);
					} catch (InterruptedException e) {
						// Ignore
					}
				}
			});
		}
		
	}

	private void updateSaveable() {
		if (this.saveable instanceof ResourceSaveableComparison) {
			ResourceSaveableComparison rsc = (ResourceSaveableComparison) this.saveable;
			rsc.setInput(input);
		}
	}

	protected void handleError(final String message, Throwable throwable) {
		if (throwable instanceof InvocationTargetException) {
			InvocationTargetException ite = (InvocationTargetException) throwable;
			throwable = ite.getTargetException();
		}
		final IStatus status;
		if (throwable instanceof CoreException) {
			CoreException ce = (CoreException) throwable;
			status = ce.getStatus();
		} else {
			status = new Status(IStatus.ERROR, TeamUIPlugin.ID, 0, message, throwable);
		}
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				ErrorDialog.openError(getShell(), null, message, status);
			}
		});
	}

	protected Shell getShell() {
		return Utils.getShell(null);
	}

	protected IRunnableContext getRunnableContext() {
		return PlatformUI.getWorkbench().getProgressService();
	}

	private void deregisterForInputStateChanges() {
		if (changeNotifier != null) {
			changeNotifier.removeChangeListener(changeListener);
			changeNotifier.disconnect(input);
		}
	}
	
	protected void closeEditor() {
		if (isSaveNeeded()) {
			// TODO: Need to indicate to the user that the editor is stale
		} else {
			Display display = page.getWorkbenchWindow().getShell().getDisplay();
			display.asyncExec(new Runnable() {
				public void run() {
					IEditorPart part = page.findEditor(ModelCompareEditorInput.this);
					page.closeEditor(part, false);
				}
			});
		}
	}
	
	private Saveable asSaveable(ICompareInput input) {
		if (input instanceof ISynchronizationCompareInput) {
			ISynchronizationCompareInput mci = (ISynchronizationCompareInput) input;
			SaveableComparison compareModel = mci.getSaveable();
			if (compareModel != null)
				return compareModel;
		}
		return new ResourceSaveableComparison(input, participant, this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#prepareInput(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected Object prepareInput(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		// update the title now that the remote revision number as been fetched
		// from the server
		setTitle(getTitle());
        monitor.beginTask(TeamUIMessages.SyncInfoCompareInput_3, 100);
        monitor.setTaskName(TeamUIMessages.SyncInfoCompareInput_3);
		try {
			ISynchronizationCompareInput adapter = asModelCompareInput(input);
			if (adapter != null) {
				adapter.prepareInput(getCompareConfiguration(), Policy.subMonitorFor(monitor, 100));
			}
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		} finally {
            monitor.done();
        }
		return input;
	}

	private ISynchronizationCompareInput asModelCompareInput(ICompareInput input) {
		return (ISynchronizationCompareInput)Utils.getAdapter(input, ISynchronizationCompareInput.class);
	}

	/**
	 * Return whether the compare input of this editor input matches the
	 * given object.
	 * @param object the object
	 * @param participant the participant associated with the given object
	 * @return whether the compare input of this editor input matches the
	 * given object
	 */
	public boolean matches(Object object, ISynchronizeParticipant participant) {
		if (participant == this.participant && input instanceof ISynchronizationCompareInput) {
			ISynchronizationCompareInput mci = (ISynchronizationCompareInput) input;
			return mci.isCompareInputFor(object);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPropertyListener#propertyChanged(java.lang.Object, int)
	 */
	public void propertyChanged(Object source, int propId) {
		if (propId == SaveableComparison.PROP_DIRTY) {
			setDirty(saveable.isDirty());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (IFile.class.equals(adapter)) {
			IResource resource = Utils.getResource(input);
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
		return NLS.bind(TeamUIMessages.SyncInfoCompareInput_title, new String[] { input.getName() }); 
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return TeamUIPlugin.getImageDescriptor(ITeamUIImages.IMG_SYNC_VIEW);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
	 */
	public String getToolTipText() {
		String fullPath;
		ISynchronizationCompareInput adapter = asModelCompareInput(input);
		if (adapter != null) {
			fullPath = adapter.getFullPath();
		} else {
			fullPath = getName();
		}
		return NLS.bind(TeamUIMessages.SyncInfoCompareInput_tooltip, new String[] { Utils.shortenText(SynchronizeView.MAX_NAME_LENGTH, participant.getName()), fullPath });
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

	/**
	 * {@inheritDoc}
	 */
	public Saveable[] getActiveSaveables() {
		return new Saveable[] { saveable };
	}

	/**
	 * {@inheritDoc}
	 */
	public Saveable[] getSaveables() {
		return getActiveSaveables();
	}

}
