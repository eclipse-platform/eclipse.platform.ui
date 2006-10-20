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
import org.eclipse.team.core.ICache;
import org.eclipse.team.core.ICacheListener;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.synchronize.LocalResourceSaveableComparison;
import org.eclipse.team.internal.ui.synchronize.SynchronizeView;
import org.eclipse.team.ui.mapping.ISynchronizationCompareInput;
import org.eclipse.team.ui.mapping.SaveableComparison;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.*;
import org.eclipse.ui.services.IDisposable;

public class ModelCompareEditorInput extends CompareEditorInput implements ISaveablesSource, IPropertyListener {

	private final ModelSynchronizeParticipant participant;
	private final ICompareInput input;
	private final Saveable saveable;
	private final ICacheListener contextListener;
	private final IWorkbenchPage page;
	private final ICompareInputChangeListener compareInputChangeListener;
	private final ListenerList inputChangeListeners = new ListenerList(ListenerList.IDENTITY);
	
	private final class ResourceDiffSaveableComparison extends
	LocalResourceSaveableComparison {
		private ResourceDiffSaveableComparison(
				ICompareInput compareInput, CompareEditorInput compareEditorInput) {
			super(compareInput, compareEditorInput);
		}
		
		protected void fireInputChange() {
			if (ModelCompareEditorInput.this.input instanceof ResourceDiffCompareInput) {
				ResourceDiffCompareInput rdci = (ResourceDiffCompareInput) ModelCompareEditorInput.this.input;
				rdci.fireChange();
			}
		}
	}

	public ModelCompareEditorInput(ModelSynchronizeParticipant participant, ICompareInput input, IWorkbenchPage page) {
		super(new CompareConfiguration());
		Assert.isNotNull(page);
		Assert.isNotNull(participant);
		Assert.isNotNull(input);
		this.page = page;
		this.participant = participant;
		this.input = input;
		this.saveable = asSaveable(this.input);
		setDirty(saveable.isDirty());
		compareInputChangeListener = new ICompareInputChangeListener() {
			public void compareInputChanged(ICompareInput source) {
				if (source == ModelCompareEditorInput.this.input) {
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
		contextListener = new ICacheListener() {
			public void cacheDisposed(ICache cache) {
				closeEditor(true);
			}
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#contentsCreated()
	 */
	protected void contentsCreated() {
		super.contentsCreated();
		participant.getContext().getCache().addCacheListener(contextListener);
		input.addCompareInputChangeListener(compareInputChangeListener);
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
		input.removeCompareInputChangeListener(compareInputChangeListener);
		if (saveable instanceof SaveableComparison) {
			SaveableComparison scm = (SaveableComparison) saveable;
			scm.removePropertyListener(ModelCompareEditorInput.this);
		}
		if (saveable instanceof LocalResourceSaveableComparison) {
			LocalResourceSaveableComparison rsc = (LocalResourceSaveableComparison) saveable;
			rsc.dispose();
		}
		if (input instanceof IDisposable) {
			((IDisposable) input).dispose();
		}
	}
	
	/**
	 * Close the editor if it is not dirty. If it is still dirty, let the 
	 * content merge viewer handle the compare input change.
	 */
	/* package */ boolean closeEditor(boolean checkForUnsavedChanges) {
		if (isSaveNeeded() && checkForUnsavedChanges) {
			return false;
		} else {
			Runnable runnable = new Runnable() {
				public void run() {
					IEditorPart part = page.findEditor(ModelCompareEditorInput.this);
					page.closeEditor(part, false);
				}
			};
			if (Display.getCurrent() != null) {
				runnable.run();
			} else {
				Display display = page.getWorkbenchWindow().getShell().getDisplay();
				display.asyncExec(runnable);
			}
			return true;
		}
	}
	
	private Saveable asSaveable(ICompareInput input) {
		if (input instanceof ISynchronizationCompareInput) {
			ISynchronizationCompareInput mci = (ISynchronizationCompareInput) input;
			SaveableComparison compareModel = mci.getSaveable();
			if (compareModel != null)
				return compareModel;
		}
		return new ResourceDiffSaveableComparison(input, this);
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

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablesSource#getActiveSaveables()
	 */
	public Saveable[] getActiveSaveables() {
		return new Saveable[] { saveable };
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
		if (input == this.input) {
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
		if (input == this.input) {
			inputChangeListeners.remove(listener);
		} else {
			super.removeCompareInputChangeListener(input, listener);
		}
	}
	
	/* package */ void propogateInputChange() {
		if (!inputChangeListeners.isEmpty()) {
			Object[] allListeners = inputChangeListeners.getListeners();
			for (int i = 0; i < allListeners.length; i++) {
				final ICompareInputChangeListener listener = (ICompareInputChangeListener)allListeners[i];
				SafeRunner.run(new ISafeRunnable() {
					public void run() throws Exception {
						listener.compareInputChanged(input);
					}
					public void handleException(Throwable exception) {
						// Logged by the safe runner
					}
				});
			}
		}
	}

}
