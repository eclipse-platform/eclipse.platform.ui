/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPart2;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.misc.UIStats;
import org.eclipse.ui.internal.registry.IViewDescriptor;
import org.eclipse.ui.internal.registry.IViewRegistry;
import org.eclipse.ui.internal.registry.ViewDescriptor;
import org.eclipse.ui.internal.util.Util;

/**
 * The ViewFactory is used to control the creation and disposal of views.  
 * It implements a reference counting strategy so that one view can be shared
 * by more than one client.
 */
/*package*/ class ViewFactory {

	private class ViewReference extends WorkbenchPartReference implements IViewReference {

		private String secondaryId;
		private boolean create = true;

		public ViewReference(String id, IMemento memento) {
			this(id, null, memento);
		}
		
		public ViewReference(String id, String secondaryId, IMemento memento) {
			ViewDescriptor desc = (ViewDescriptor) viewReg.find(id);
			ImageDescriptor iDesc = null;
			String title = null;
			if (desc != null) {
				iDesc = desc.getImageDescriptor();
				title = desc.getLabel();
			}
			
			String name = null;
			
			if (memento != null) {
				name = memento.getString(IWorkbenchConstants.TAG_PART_NAME);
			}
			if (name == null) {
				name = title;
			}
			
			init(id, title, null, iDesc, name, null);
			this.secondaryId = secondaryId;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.WorkbenchPartReference#dispose()
		 */
		public void dispose() {
			super.dispose();
			create = false;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ui.IWorkbenchPartReference#getPage()
		 */
		public IWorkbenchPage getPage() {
			return page;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ui.IWorkbenchPartReference#getPart(boolean)
		 */
		public IWorkbenchPart getPart(boolean restore) {
			if (part != null)
				return part;
			if (!create)
				return null;
			if (restore) {
				IStatus status = restoreView(this);
				if (status.getSeverity() == IStatus.ERROR) {
					create = false;
					Workbench workbench = (Workbench) PlatformUI.getWorkbench();
					if (!workbench.isStarting()) {
						ErrorDialog.openError(page.getWorkbenchWindow().getShell(), WorkbenchMessages.getString("ViewFactory.unableToRestoreViewTitle"), //$NON-NLS-1$
						WorkbenchMessages.format("ViewFactory.unableToRestoreViewMessage", new String[] { getTitle()}), //$NON-NLS-1$
						status, IStatus.WARNING | IStatus.ERROR);
					}
				} else {
					releaseReferences();
				}
			}
			return part;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.WorkbenchPartReference#getRegisteredName()
		 */
		public String getRegisteredName() {
			if (part != null && part.getSite() != null) {
				return part.getSite().getRegisteredName();
			}

			IViewRegistry reg = viewReg;
			IViewDescriptor desc = reg.find(getId());
			if (desc != null)
				return desc.getLabel();
			return getTitle();
		}
		
		protected String computePartName() {
			if (part instanceof IWorkbenchPart2) {
				return super.computePartName();
			} else {
				return getRegisteredName();
			}
		}
		
		protected String computeContentDescription() {
			if (part instanceof IWorkbenchPart2) {
				return super.computeContentDescription();
			} else {
				String rawTitle = getRawTitle();
				
				if (!Util.equals(rawTitle, getRegisteredName())) { 
					return rawTitle;
				}
				
				return ""; //$NON-NLS-1$
			}
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ui.IViewReference
		 */
		public String getSecondaryId() {
			return secondaryId;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IViewReference#getView(boolean)
		 */
		public IViewPart getView(boolean restore) {
			return (IViewPart) getPart(restore);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IViewReference#isFastView()
		 */
		public boolean isFastView() {
			return page.isFastView(this);
		}
		
	}
	
	private ReferenceCounter counter;
	private HashMap mementoTable = new HashMap();
	private WorkbenchPage page;
	private IViewRegistry viewReg;

	
	/**
	 * Separates a view's primary id from its secondary id in view key strings.
	 */
	static final String ID_SEP = ":"; //$NON-NLS-1$
	
	/**
     * Returns a string representing a view with the given id and (optional) secondary id,
     * suitable for use as a key in a map.  
     * 
     * @param id primary id of the view
     * @param secondaryId secondary id of the view or <code>null</code>
     * @return the key
     */
    static String getKey(String id, String secondaryId) {
        return secondaryId == null ? id : id + ID_SEP + secondaryId;
    }

	/**
     * Returns a string representing the given view reference, suitable for use as a key in a map.  
     * 
     * @param viewRef the view reference
     * @return the key
     */
    static String getKey(IViewReference viewRef) {
        return getKey(viewRef.getId(), viewRef.getSecondaryId());
    }
	
    /**
     * Extracts ths primary id portion of a compound id.
     * @param compoundId a compound id of the form: primaryId [':' secondaryId]
     * @return the primary id
     */
    static String extractPrimaryId(String compoundId) {
        int i = compoundId.lastIndexOf(ID_SEP);
        if (i == -1)
            return compoundId;
        return compoundId.substring(0, i);
    }
    
    /**
     * Extracts ths secondary id portion of a compound id.
     * @param compoundId a compound id of the form: primaryId [':' secondaryId]
     * @return the secondary id, or <code>null</code> if none
     */
    static String extractSecondaryId(String compoundId) {
        int i = compoundId.lastIndexOf(ID_SEP);
        if (i == -1)
            return null;
        return compoundId.substring(i+1);
    }

    /**
     * Returns whether the given view id contains a wildcard. Wildcards cannot
     * be used in regular view ids, only placeholders.
     * 
     * @param viewId the view id
     * @return <code>true</code> if the given view id contains a wildcard,
     *         <code>false</code> otherwise
     * 
     * @since 3.0.1
     */
    static boolean hasWildcard(String viewId) {
        return viewId.indexOf(PartPlaceholder.WILD_CARD) >= 0;
    }
    
    /**
	 * Constructs a new view factory.
	 */
	public ViewFactory(WorkbenchPage page, IViewRegistry reg) {
		super();
		this.page = page;
		this.viewReg = reg;
		counter = new ReferenceCounter();
	}
	
	/**
	 * @param ref the <code>IViewReference</code> to restore.
	 * @return <code>IStatus</code>
	 */
	public IStatus busyRestoreView(final IViewReference ref) {
		if (ref.getPart(false) != null)
			return new Status(IStatus.OK, PlatformUI.PLUGIN_ID, 0, "", null); //$NON-NLS-1$

		final String key = getKey(ref);
		final IMemento stateMem = getViewState(key);
		mementoTable.remove(key);

		final boolean resetPart[] = { true };
		final IStatus result[] = new IStatus[] { new Status(IStatus.OK, PlatformUI.PLUGIN_ID, 0, "", null)}; //$NON-NLS-1$
		Platform.run(new SafeRunnable() {
			public void handleException(Throwable e) {
				if (resetPart[0]) {
					ViewReference viewRef = ((ViewReference) ref); 
					viewRef.setPart(null);
					if (viewRef.getPane() != null) {
						page.hideView(ref);						
					}
				}
				//Execption is already logged.
				result[0] = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, WorkbenchMessages.format("Perspective.exceptionRestoringView", new String[] { key }), //$NON-NLS-1$
				e);

			}
			public void run() {
				IViewDescriptor desc = viewReg.find(ref.getId());
				if (desc == null) {
					result[0] = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, WorkbenchMessages.format("ViewFactory.couldNotCreate", new Object[] { key }), //$NON-NLS-1$
					null);
					return;
				}

				// Create the view.
				IViewPart view = null;
				String label = desc.getLabel();
				try {
					try {
						UIStats.start(UIStats.CREATE_PART, label);
						view = desc.createView();
					} finally {
						UIStats.end(UIStats.CREATE_PART, label);
					}
					((ViewReference) ref).setPart(view);
				} catch (CoreException e) {
					PartPane pane = ((ViewReference) ref).getPane();
					if (pane != null) {
						page.getPerspectivePresentation().removePart(pane);
						pane.dispose();
					}
					result[0] = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, WorkbenchMessages.format("ViewFactory.initException", new Object[] { desc.getID()}), //$NON-NLS-1$
					e);
					return;
				}

				// Create site
				ViewSite site = new ViewSite(ref, view, page, desc);
				PartPane pane = ((ViewReference) ref).getPane();				
				if (pane == null) {
					pane = new ViewPane(ref, page);
					((ViewReference) ref).setPane(pane);
				}
				site.setPane(pane);
				site.setActionBars(new ViewActionBars(page.getActionBars(), (ViewPane) pane));
				try {
					try {
						UIStats.start(UIStats.INIT_PART, label);
						view.init(site, stateMem);
					} finally {
						UIStats.end(UIStats.INIT_PART, label);
					}
				} catch (PartInitException e) {
					releaseView(ref);
					result[0] = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, WorkbenchMessages.format("Perspective.exceptionRestoringView", new String[] { key }), //$NON-NLS-1$
					e);
					return;
				}
				if (view.getSite() != site) {
					releaseView(ref);
					result[0] = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, WorkbenchMessages.format("ViewFactory.siteException", new Object[] { desc.getID()}), //$NON-NLS-1$
					null);
					return;
				}

				resetPart[0] = false;
				Control ctrl = pane.getControl();
				if (ctrl == null)
					pane.createControl(page.getClientComposite());
				else 
				    pane.createChildControl();
				result[0] = new Status(IStatus.OK, PlatformUI.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
			}
		});
		return result[0];
	}

	/**
	 * Creates an instance of a view defined by id.
	 * 
	 * This factory implements reference counting.  The first call to this
	 * method will return a new view.  Subsequent calls will return the
	 * first view with an additional reference count.  The view is
	 * disposed when releaseView is called an equal number of times
	 * to getView.
	 */
	public IViewReference createView(final String id) throws PartInitException {
	    return createView(id, null);
	}

	/**
	 * Creates an instance of a view defined by id and secondary id.
	 * 
	 * This factory implements reference counting.  The first call to this
	 * method will return a new view.  Subsequent calls will return the
	 * first view with an additional reference count.  The view is
	 * disposed when releaseView is called an equal number of times
	 * to createView.
	 */
	public IViewReference createView(String id, String secondaryId) throws PartInitException {
		IViewDescriptor desc = viewReg.find(id);
		// ensure that the view id is valid
		if (desc == null)
			throw new PartInitException(WorkbenchMessages.format("ViewFactory.couldNotCreate", new Object[] { id })); //$NON-NLS-1$
		// ensure that multiple instances are allowed if a secondary id is given
		if (secondaryId != null) {
		    if (!desc.getAllowMultiple()) {
				throw new PartInitException(WorkbenchMessages.format("ViewFactory.noMultiple", new Object[] { id })); //$NON-NLS-1$
		    }
		}
		String key = getKey(id, secondaryId);
		IViewReference ref = (IViewReference) counter.get(key);
		if (ref == null) {
			IMemento memento = (IMemento)mementoTable.get(key); 
			ref = new ViewReference(id, secondaryId, memento);
			counter.put(key, ref);
		} else {
			counter.addRef(key);
		}
		return ref;
	}

    /**
	 * Remove a view rec from the manager.
	 *
	 * The IViewPart.dispose method must be called at a higher level.
	 */
	private void destroyView(IViewPart view) {
		// Free action bars, pane, etc.
		PartSite site = (PartSite) view.getSite();
		ViewActionBars actionBars = (ViewActionBars) site.getActionBars();
		actionBars.dispose();
		PartPane pane = site.getPane();
		pane.dispose();

		// Free the site.
		site.dispose();
	}
	
	/**
	 * Returns the view with the given id, or <code>null</code> if not found.
	 */
	public IViewReference getView(String id) {
	    return getView(id, null);
	}

	/**
	 * Returns the view with the given id and secondary id, or <code>null</code> if not found.
	 */
	public IViewReference getView(String id, String secondaryId) {
	    String key = getKey(id, secondaryId);
		return (IViewReference) counter.get(key);
	}

	/**
	 * @return the <code>IViewRegistry</code> used by this factory.
	 * @since 3.0
	 */
	public IViewRegistry getViewRegistry() {
		return viewReg;
	}
	
	/**
	 * Returns a list of views which are open.
	 */
	public IViewReference[] getViews() {
		List list = counter.values();
		IViewReference[] array = new IViewReference[list.size()];
		list.toArray(array);
		return array;
	}
	
	/**
	 * @return the <code>WorkbenchPage</code> used by this factory.
	 * @since 3.0
	 */
	public WorkbenchPage getWorkbenchPage() {
		return page;
	}

	/**
	 * Returns whether a view with the same id(s) as the
     * given view reference exists.
	 */
	public boolean hasView(IViewReference viewRef) {
	    return hasView(viewRef.getId(), viewRef.getSecondaryId());
	}
	
	/**
	 * Returns whether a view with the given id exists.
	 */
	public boolean hasView(String id) {
	    return hasView(id, null);
	}
	
	/**
	 * Returns whether a view with the given ids exists.
	 */
	public boolean hasView(String id, String secondaryId) {
	    return getView(id, secondaryId) != null;
	}
	
	/**
	 * Releases an instance of a view.
	 *
	 * This factory does reference counting.  For more info see
	 * getView.
	 */
	public void releaseView(IViewReference viewRef) {
	    String key = getKey(viewRef);
		IViewReference ref = (IViewReference) counter.get(key);
		if (ref == null)
			return;
		int count = counter.removeRef(key);
		if (count <= 0) {
			IViewPart view = (IViewPart) ref.getPart(false);
			if (view != null)
				destroyView(view);
		}
	}
	
	/**
	 * Restore view states.
	 *  
	 * @param memento the <code>IMemento</code> to restore from.
	 * @return <code>IStatus</code>
	 */
	public IStatus restoreState(IMemento memento) {
		IMemento mem[] = memento.getChildren(IWorkbenchConstants.TAG_VIEW);
		for (int i = 0; i < mem.length; i++) {
			//for dynamic UI - add the next line to replace subsequent code that is commented out
			restoreViewState(mem[i]);
		}
		return new Status(IStatus.OK, PlatformUI.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
	}
	
	/**
	 * Creates an instance of a view defined by id.
	 * 
	 * This factory implements reference counting.  The first call to this
	 * method will return a new view.  Subsequent calls will return the
	 * first view with an additional reference count.  The view is
	 * disposed when releaseView is called an equal number of times
	 * to getView.
	 */
	public IStatus restoreView(final IViewReference ref) {
		final IStatus result[] = new IStatus[1];
		BusyIndicator.showWhile(page.getWorkbenchWindow().getShell().getDisplay(), new Runnable() {
			public void run() {
				result[0] = busyRestoreView(ref);
			}
		});
		return result[0];
	}

	/**
	 * Save view states.
	 * 
	 * @param memento the <code>IMemento</code> to save to.
	 * @return <code>IStatus</code>
	 */
	public IStatus saveState(IMemento memento) {
		final MultiStatus result = new MultiStatus(PlatformUI.PLUGIN_ID, IStatus.OK, WorkbenchMessages.getString("ViewFactory.problemsSavingViews"), null); //$NON-NLS-1$

		final IViewReference refs[] = getViews();
		for (int i = 0; i < refs.length; i++) {
			//for dynamic UI - add the following line to replace subsequent code which is commented out
			saveViewState(memento, refs[i], result);
		}
		return result;
	}
	
	//	for dynamic UI
	public IMemento saveViewState(IMemento memento, IViewReference ref, MultiStatus res) {
		final MultiStatus result = res;
		final IMemento viewMemento = memento.createChild(IWorkbenchConstants.TAG_VIEW);
		viewMemento.putString(IWorkbenchConstants.TAG_ID, ViewFactory.getKey(ref));
		if (ref instanceof ViewReference) {
			viewMemento.putString(IWorkbenchConstants.TAG_PART_NAME, ((ViewReference)ref).getPartName());
		}
		final IViewReference viewRef = ref;
		final IViewPart view = (IViewPart)ref.getPart(false);
		if(view != null) {
			Platform.run(new SafeRunnable() {
				public void run() {
					view.saveState(viewMemento.createChild(IWorkbenchConstants.TAG_VIEW_STATE));
				}
				public void handleException(Throwable e) {
					result.add(new Status(
							IStatus.ERROR,PlatformUI.PLUGIN_ID,0,
							WorkbenchMessages.format("ViewFactory.couldNotSave",new String[]{viewRef.getTitle()}), //$NON-NLS-1$
							e));
				}
			});
		} else {
			IMemento mem = getViewState(ViewFactory.getKey(ref));
			if(mem != null) {
				IMemento child = viewMemento.createChild(IWorkbenchConstants.TAG_VIEW_STATE);
				child.putMemento(mem);
			}
		}
		return viewMemento;
	}

//	for dynamic UI
	public void restoreViewState(IMemento memento){
		String compoundId = memento.getString(IWorkbenchConstants.TAG_ID);
		mementoTable.put(compoundId, memento);
	}
	
	private IMemento getViewState(String key) {
		IMemento memento = (IMemento) mementoTable.get(key);
		
		if (memento == null) {
			return null;
		}
		
		return memento.getChild(IWorkbenchConstants.TAG_VIEW_STATE);
	}
}
	
