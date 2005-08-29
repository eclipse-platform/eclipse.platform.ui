/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brock Janiczak - bug 78494
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.breakpoints;

 
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManagerListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.LazyModelPresentation;
import org.eclipse.debug.internal.ui.actions.OpenBreakpointMarkerAction;
import org.eclipse.debug.internal.ui.actions.ShowSupportedBreakpointsAction;
import org.eclipse.debug.internal.ui.actions.SkipAllBreakpointsAction;
import org.eclipse.debug.internal.ui.actions.breakpointGroups.CopyBreakpointsAction;
import org.eclipse.debug.internal.ui.actions.breakpointGroups.PasteBreakpointsAction;
import org.eclipse.debug.internal.ui.actions.breakpointGroups.RemoveFromWorkingSetAction;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.SelectionListenerAction;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

/**
 * This view shows the breakpoints registered with the breakpoint manager
 */
public class BreakpointsView extends AbstractDebugView implements ISelectionListener, IBreakpointManagerListener, IPerspectiveListener2 {

    private BreakpointsViewEventHandler fEventHandler;
	private ICheckStateListener fCheckListener= new ICheckStateListener() {
		public void checkStateChanged(CheckStateChangedEvent event) {
			handleCheckStateChanged(event);
		}
	};
	private boolean fIsTrackingSelection= false;
	// Persistance constants
	private static String KEY_IS_TRACKING_SELECTION= "isTrackingSelection"; //$NON-NLS-1$
	private static String KEY_VALUE="value"; //$NON-NLS-1$
    private static final String ACTION_REMOVE_FROM_GROUP = "RemoveFromGroup"; //$NON-NLS-1$
	private BreakpointsContentProvider fContentProvider;
    private Clipboard fClipboard;
    
	/**
	 * This memento allows the Breakpoints view to save and restore state
	 * when it is closed and opened within a session. A different
	 * memento is supplied by the platform for persistance at
	 * workbench shutdown.
	 */
	private static IMemento fgMemento;
	
	/**
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointManagerListener(this);
		getSite().getWorkbenchWindow().addPerspectiveListener(this);
	}

	/**
	 * @see AbstractDebugView#createViewer(Composite)
	 */
	protected Viewer createViewer(Composite parent) {
		fContentProvider= new BreakpointsContentProvider();
		CheckboxTreeViewer viewer = new BreakpointsViewer(new Tree(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CHECK));
        setViewer(viewer);
		viewer.setContentProvider(fContentProvider);
		viewer.setSorter(new BreakpointsSorter());
		viewer.setInput(DebugPlugin.getDefault().getBreakpointManager());
		viewer.addCheckStateListener(fCheckListener);
		viewer.addTreeListener(new ITreeViewerListener() {
			public void treeExpanded(TreeExpansionEvent event) {
				((BreakpointsViewer)getViewer()).updateCheckedState(event.getElement());
			}
			public void treeCollapsed(TreeExpansionEvent event) {
			}
		});
	    viewer.setLabelProvider(new BreakpointsLabelProvider());
		
		// Necessary so that the PropertySheetView hears about selections in this view
		getSite().setSelectionProvider(viewer);
		initIsTrackingSelection();
		initBreakpointOrganizers();
		setEventHandler(new BreakpointsViewEventHandler(this));
        initDragAndDrop();
		return viewer;
	}
    
    private void initDragAndDrop() {
        StructuredViewer viewer = (StructuredViewer)getViewer();
        int ops= DND.DROP_MOVE | DND.DROP_COPY;
        // drop
        Transfer[] dropTransfers= new Transfer[] {
            LocalSelectionTransfer.getInstance()
        };
        viewer.addDropSupport(ops, dropTransfers, new BreakpointsDropAdapter(this, viewer));
        
        // Drag 
        Transfer[] dragTransfers= new Transfer[] {
            LocalSelectionTransfer.getInstance()
        };
        viewer.addDragSupport(ops, dragTransfers, new BreakpointsDragAdapter(this, viewer));
    }    
	
	/**
	 * Initializes whether this view tracks selection in the
	 * debug view from the persisted state.
	 */
	private void initIsTrackingSelection() {
		IMemento memento= getMemento();
		if (memento != null) {
			IMemento node= memento.getChild(KEY_IS_TRACKING_SELECTION);
			if (node != null) {
				setTrackSelection(Boolean.valueOf(node.getString(KEY_VALUE)).booleanValue());
				return;
			}
		}
		setTrackSelection(false);
	}
	
	private void initBreakpointOrganizers() {
		IMemento memento = getMemento();
		if (memento != null) {
			IMemento node = memento.getChild(IDebugUIConstants.EXTENSION_POINT_BREAKPOINT_ORGANIZERS);
			if (node == null) {
                fContentProvider.setOrganizers(null);
            } else {
				String value = node.getString(KEY_VALUE);
                if (value != null) {
                    String[] ids = value.split(","); //$NON-NLS-1$
    				BreakpointOrganizerManager manager = BreakpointOrganizerManager.getDefault();
    				List organziers= new ArrayList();
                    for (int i = 0; i < ids.length; i++) {
                        IBreakpointOrganizer organizer = manager.getOrganizer(ids[i]);
                        if (organizer != null) {
                            organziers.add(organizer);
                        }
                    }
    				fContentProvider.setOrganizers((IBreakpointOrganizer[]) organziers.toArray(new IBreakpointOrganizer[organziers.size()]));
                }
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#getMemento()
	 */
	protected IMemento getMemento() {
		if (fgMemento != null) {
		    return fgMemento;
		}
		return super.getMemento();
	}
	
    /**
     * Update the checked state up the given element and all of its children.
     * 
     * @param element
     */
	public void updateCheckedState(Object element) {
        ((BreakpointsViewer)getViewer()).updateCheckedState(element);
	}
		
	/**
	 * Returns this view's viewer as a checkbox tree viewer.
	 * @return this view's viewer as a checkbox tree viewer
	 */
	public CheckboxTreeViewer getCheckboxViewer() {
		return (CheckboxTreeViewer) getViewer();
	}
	
	/**
	 * Returns this view's content provider as a tree content provider.
	 * @return this view's content provider as a tree content provider
	 */
	public ITreeContentProvider getTreeContentProvider() {
	    return fContentProvider;
	}

	/**
	 * Responds to the user checking and unchecking breakpoints by enabling
	 * and disabling them.
	 * 
	 * @param event the check state change event
	 */
	private void handleCheckStateChanged(CheckStateChangedEvent event) {
		Object source= event.getElement();
		if (source instanceof BreakpointContainer) {
			handleContainerChecked(event, (BreakpointContainer) source);
		} else if (source instanceof IBreakpoint) {
			handleBreakpointChecked(event, (IBreakpoint) source);
		}
	}
	/**
	 * A breakpoint has been checked/unchecked. Update the group
	 * element's checked/grayed state as appropriate.
	 */
	private void handleBreakpointChecked(final CheckStateChangedEvent event, final IBreakpoint breakpoint) {
		final boolean enable= event.getChecked();
        String jobName = enable ? DebugUIViewsMessages.BreakpointsView_0 : DebugUIViewsMessages.BreakpointsView_1; // 
        new Job(jobName) {
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    breakpoint.setEnabled(enable);
                    return Status.OK_STATUS;
                } catch (final CoreException e) {
                    Display.getDefault().asyncExec(new Runnable() {
                        public void run() {
                            String titleState= enable ? DebugUIViewsMessages.BreakpointsView_6 : DebugUIViewsMessages.BreakpointsView_7; // 
                            String messageState= enable ? DebugUIViewsMessages.BreakpointsView_8 : DebugUIViewsMessages.BreakpointsView_9;  // 
                            DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(), MessageFormat.format(DebugUIViewsMessages.BreakpointsView_10, new String[] { titleState }), MessageFormat.format(DebugUIViewsMessages.BreakpointsView_11, new String[] { messageState }), e); // 
                            // If the breakpoint fails to update, reset its check state.
                            getCheckboxViewer().removeCheckStateListener(fCheckListener);
                            event.getCheckable().setChecked(breakpoint, !event.getChecked());
                            getCheckboxViewer().addCheckStateListener(fCheckListener);                            
                        }
                    });
                }
                return Status.CANCEL_STATUS;
            }
        }.schedule();
    }

	/**
	 * A group has been checked or unchecked. Enable/disable all of the
	 * breakpoints in that group to match.
	 */
	private void handleContainerChecked(CheckStateChangedEvent event, BreakpointContainer container) {
		final IBreakpoint[] breakpoints = container.getBreakpoints();
		final boolean enable= event.getChecked();
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {
                for (int i = 0; i < breakpoints.length; i++) {
                    IBreakpoint breakpoint = breakpoints[i];
                    breakpoint.setEnabled(enable);
                }
            }
        };
        try {
            // TODO: should use scheduling rule
            ResourcesPlugin.getWorkspace().run(runnable, null);
        } catch (CoreException e) {
            DebugUIPlugin.log(e);
        }
	}

	/**
	 * @see AbstractDebugView#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return IDebugHelpContextIds.BREAKPOINT_VIEW;
	}

	/**
	 * @see IWorkbenchPart#dispose()
	 */
	public void dispose() {
        disposeAction(IWorkbenchActionDefinitionIds.COPY);
        disposeAction(IWorkbenchActionDefinitionIds.PASTE);
        disposeAction(ACTION_REMOVE_FROM_GROUP); 
        
	    if (getCheckboxViewer() != null) {
	        getCheckboxViewer().removeCheckStateListener(fCheckListener);
	    }
		IAction action= getAction("ShowBreakpointsForModel"); //$NON-NLS-1$
		if (action != null) {
			((ShowSupportedBreakpointsAction)action).dispose(); 
		}
		getSite().getPage().removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointManagerListener(this);
		super.dispose();
		
		if (getEventHandler() != null) {
			getEventHandler().dispose();
		}
        
        if (fClipboard != null) {
            fClipboard.dispose();
        }
        
		getSite().getWorkbenchWindow().removePerspectiveListener(this);
	}

	/**
	 * @see AbstractDebugView#createActions()
	 */
	protected void createActions() {
		IAction action = new OpenBreakpointMarkerAction(getViewer());
		setAction("GotoMarker", action); //$NON-NLS-1$
		setAction(DOUBLE_CLICK_ACTION, action);
		setAction("ShowBreakpointsForModel", new ShowSupportedBreakpointsAction(getStructuredViewer(),this)); //$NON-NLS-1$
		setAction("SkipBreakpoints", new SkipAllBreakpointsAction()); //$NON-NLS-1$
        
        fClipboard= new Clipboard(getSite().getShell().getDisplay());
        
        PasteBreakpointsAction paste = new PasteBreakpointsAction(this);
        configure(paste, IWorkbenchActionDefinitionIds.PASTE, ActionFactory.PASTE.getId(),ISharedImages.IMG_TOOL_PASTE);
        SelectionListenerAction copy = new CopyBreakpointsAction(this, fClipboard, paste);
        configure(copy, IWorkbenchActionDefinitionIds.COPY, ActionFactory.COPY.getId(), ISharedImages.IMG_TOOL_COPY);
        
        SelectionListenerAction remove = new RemoveFromWorkingSetAction(this);
        setAction(ACTION_REMOVE_FROM_GROUP, remove); 
        getViewer().addSelectionChangedListener(remove);
	}

	/**
     * Configures the action to override the global action, registers
     * the action for selection change notification, and registers
     * the action with this view.
     * 
     * @param sla action
     * @param defId action definition id
     * @param globalId global action id
     * @param imgId image identifier
     */
    private void configure(SelectionListenerAction action, String defId, String globalId, String imgId) {
        setAction(defId, action);
        action.setActionDefinitionId(defId);
        getViewSite().getActionBars().setGlobalActionHandler(globalId, action);
        getViewer().addSelectionChangedListener(action);
        action.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(imgId));
    }
    
    /**
     * Cleans up selection listener action
     * 
     * @param id action id
     */
    private void disposeAction(String id) {
        IAction action = getAction(id);
        if (action instanceof SelectionListenerAction) {
            SelectionListenerAction sla = (SelectionListenerAction) action;
            if (getViewer() != null) {
                getViewer().removeSelectionChangedListener(sla);
            }
        }
    }

    /**
	 * Adds items to the context menu.
	 * 
	 * @param menu The menu to contribute to
	 */
	protected void fillContextMenu(IMenuManager menu) {
		updateObjects();
		menu.add(new Separator(IDebugUIConstants.EMPTY_NAVIGATION_GROUP));
		menu.add(new Separator(IDebugUIConstants.NAVIGATION_GROUP));
		menu.add(getAction("GotoMarker")); //$NON-NLS-1$
		menu.add(new Separator(IDebugUIConstants.EMPTY_BREAKPOINT_GROUP));
		menu.add(new Separator(IDebugUIConstants.BREAKPOINT_GROUP));
        menu.add(getAction(IWorkbenchActionDefinitionIds.COPY));
        menu.add(getAction(IWorkbenchActionDefinitionIds.PASTE));
        IAction action = getAction(ACTION_REMOVE_FROM_GROUP);
        if (action.isEnabled()) {
            menu.add(action);
        }
		menu.add(new Separator(IDebugUIConstants.EMPTY_RENDER_GROUP));
		menu.add(new Separator(IDebugUIConstants.BREAKPOINT_GROUP_GROUP));
		
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	/**
	 * @see AbstractDebugView#configureToolBar(IToolBarManager)
	 */
	protected void configureToolBar(IToolBarManager tbm) {
		tbm.add(new Separator(IDebugUIConstants.BREAKPOINT_GROUP));
		tbm.add(getAction("ShowBreakpointsForModel")); //$NON-NLS-1$
		tbm.add(getAction("GotoMarker")); //$NON-NLS-1$
		tbm.add(getAction("SkipBreakpoints")); //$NON-NLS-1$
		tbm.add(new Separator(IDebugUIConstants.RENDER_GROUP));
	}
	
	/**
	 * Returns this view's event handler
	 * 
	 * @return a breakpoint view event handler
	 */
	protected BreakpointsViewEventHandler getEventHandler() {
		return fEventHandler;
	}

	/**
	 * Sets this view's event handler.
	 * 
	 * @param eventHandler a breakpoint view event handler
	 */
	private void setEventHandler(BreakpointsViewEventHandler eventHandler) {
		fEventHandler = eventHandler;
	}
	/**
	 * @see org.eclipse.debug.ui.AbstractDebugView#becomesVisible()
	 */
	protected void becomesVisible() {
		super.becomesVisible();
        CheckboxTreeViewer viewer = getCheckboxViewer();
        ISelection selection = viewer.getSelection();
        viewer.getControl().setRedraw(false);
        ((BreakpointsContentProvider)viewer.getContentProvider()).reorganize();
        viewer.setSelection(new StructuredSelection(selection));
        viewer.getControl().setRedraw(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection sel) {
		if (sel.isEmpty() || !isTrackingSelection()) {
			return;
		}
		IStructuredSelection selection= (IStructuredSelection) sel;
		Iterator iter= selection.iterator();
		Object firstElement= iter.next();
		if (firstElement == null || iter.hasNext()) {
			return;
		}
		IThread thread= null;
		if (firstElement instanceof IStackFrame) {
			thread= ((IStackFrame) firstElement).getThread();
		} else if (firstElement instanceof IThread) {
			thread= (IThread) firstElement;
		} else {
			return;
		}
		IBreakpoint[] breakpoints= thread.getBreakpoints();
		getViewer().setSelection(new StructuredSelection(breakpoints), true);
	}
	
	/**
	 * Returns whether this view is currently tracking the
	 * selection from the debug view.
	 * 
	 * @return whether this view is currently tracking the
	 *   debug view's selection
	 */
	public boolean isTrackingSelection() {
		return fIsTrackingSelection;
	}
	
	/**
	 * Sets whether this view should track the selection from
	 * the debug view.
	 * 
	 * @param trackSelection whether or not this view should
	 *   track the debug view's selection.
	 */
	public void setTrackSelection(boolean trackSelection) {
		fIsTrackingSelection= trackSelection;
		if (trackSelection) {
			getSite().getPage().addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
		} else {
			getSite().getPage().removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewPart#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento) {
		super.saveState(memento);
		IMemento node= memento.createChild(KEY_IS_TRACKING_SELECTION);
		node.putString(KEY_VALUE, String.valueOf(fIsTrackingSelection));
		
		StringBuffer buffer= new StringBuffer();
		IBreakpointOrganizer[] organizers = getBreakpointOrganizers();
        if (organizers != null) {
            for (int i = 0; i < organizers.length; i++) {
                IBreakpointOrganizer organizer = organizers[i];
                buffer.append(organizer.getIdentifier());
                if (i < (organizers.length - 1)) {
                    buffer.append(',');
                }
            }
            node = memento.createChild(IDebugUIConstants.EXTENSION_POINT_BREAKPOINT_ORGANIZERS);
            node.putString(KEY_VALUE, buffer.toString());
        }
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointManagerListener#breakpointManagerEnablementChanged(boolean)
	 */
	public void breakpointManagerEnablementChanged(boolean enabled) {
		DebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				IAction action = getAction("SkipBreakpoints"); //$NON-NLS-1$
				if (action != null) {
					((SkipAllBreakpointsAction) action).updateActionCheckedState();
				}
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	public void doubleClick(DoubleClickEvent event) {
		  IStructuredSelection selection= (IStructuredSelection) event.getSelection();
          if (selection.size() == 1) {
              Object element = selection.getFirstElement();
              if (element instanceof BreakpointContainer) {
                  getCheckboxViewer().setExpandedState(element, !getCheckboxViewer().getExpandedState(element));
                  return;
              }
          }
		super.doubleClick(event);
	}

	/**
	 * @param selectedContainers
	 */
	public void setBreakpointOrganizers(IBreakpointOrganizer[] organizers) {
        Viewer viewer = getViewer();
        ISelection selection = viewer.getSelection();
		fContentProvider.setOrganizers(organizers);
		viewer.setSelection(selection);
	}
	
	public IBreakpointOrganizer[] getBreakpointOrganizers() {
		return fContentProvider.getOrganizers();
	}

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPerspectiveListener2#perspectiveChanged(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor, org.eclipse.ui.IWorkbenchPartReference, java.lang.String)
     */
    public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, IWorkbenchPartReference partRef, String changeId) {
		if (partRef instanceof IViewReference && changeId.equals(IWorkbenchPage.CHANGE_VIEW_HIDE)) {
			String id = ((IViewReference) partRef).getId();
			if (id.equals(getViewSite().getId())) {
				// BreakpointsView closed. Persist settings.
				fgMemento= XMLMemento.createWriteRoot("BreakpointsViewMemento"); //$NON-NLS-1$
				saveState(fgMemento);
			}
		}
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPerspectiveListener#perspectiveActivated(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor)
     */
    public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPerspectiveListener#perspectiveChanged(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor, java.lang.String)
     */
    public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IDebugView#getPresentation(java.lang.String)
     */
    public IDebugModelPresentation getPresentation(String id) {
        if (getViewer() instanceof StructuredViewer) {
            IBaseLabelProvider lp = ((StructuredViewer)getViewer()).getLabelProvider();
            if (lp instanceof BreakpointsLabelProvider) {
                BreakpointsLabelProvider blp = (BreakpointsLabelProvider) lp;
                lp = blp.getPresentation();
            }
            if (lp instanceof DelegatingModelPresentation) {
                return ((DelegatingModelPresentation)lp).getPresentation(id);
            }
            if (lp instanceof LazyModelPresentation) {
                if (((LazyModelPresentation)lp).getDebugModelIdentifier().equals(id)) {
                    return (IDebugModelPresentation)lp;
                }
            }
        }
        return null;
    }    
    
    /**
     * Checks if the elements contained in the given selection can
     * be moved.
     * 
     * @param selection containing the elements to be moved
     */
    public boolean canMove(ISelection selection) {
        if (selection.isEmpty() || !fContentProvider.isShowingGroups()) {
            return false;
        }
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            Object[] objects = ss.toArray();
            for (int i = 0; i < objects.length; i++) {
                Object object = objects[i];
                if (object instanceof IBreakpoint) {
                    IBreakpoint breakpoint = (IBreakpoint) object;
                    BreakpointContainer[] containers = getMovedFromContainers(breakpoint);
                    if (containers == null || containers.length == 0) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }   
    
    /**
     * Returns whether the given selection can be pasted into the given target.
     * 
     * @param target target of the paste
     * @param selection the selection to paste
     * @return whether the given selection can be pasted into the given target
     */
    public boolean canPaste(Object target, ISelection selection) {
        if (target instanceof BreakpointContainer) {
            BreakpointContainer container = (BreakpointContainer) target;
            if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
                Object[] objects = ((IStructuredSelection)selection).toArray();
                for (int i = 0; i < objects.length; i++) {
                    if (objects[i] instanceof IBreakpoint) {
                        IBreakpoint breakpoint = (IBreakpoint)objects[i];
                        if (container.contains(breakpoint) || !container.getOrganizer().canAdd(breakpoint, container.getCategory())) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }   
    
    public void performRemove(BreakpointContainer[] containers, ISelection ss) {
        if (ss instanceof IStructuredSelection) {
            // remove from source on move operation
            IStructuredSelection selection = (IStructuredSelection) ss;
            Object[] breakpoints = selection.toArray();
            for (int i = 0; i < breakpoints.length; i++) {
                IBreakpoint breakpoint = (IBreakpoint) breakpoints[i];
                for (int j = 0; j < containers.length; j++) {
                    BreakpointContainer container = containers[j];
                    container.getOrganizer().removeBreakpoint(breakpoint, container.getCategory());
                }
            }
        }
    }
    
    /** 
     * Pastes the selection into the given target
     * 
     * @param target breakpoint container
     * @param selection breakpoints
     * @return whehther successful
     */
    public boolean performPaste(Object target, ISelection selection) {
        if (target instanceof BreakpointContainer && selection instanceof IStructuredSelection) {
            BreakpointContainer container = (BreakpointContainer) target;
            Object[] objects = ((IStructuredSelection)selection).toArray();
            for (int i = 0; i < objects.length; i++) {
                container.getOrganizer().addBreakpoint((IBreakpoint)objects[i], container.getCategory());
            }
            return true;
        }
        return false;
    }      
    
    public BreakpointContainer[] getMovedFromContainers(ISelection selection) {
        List list = new ArrayList();
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            Object[] objects = ss.toArray();
            for (int i = 0; i < objects.length; i++) {
                if (objects[i] instanceof IBreakpoint) {
                    IBreakpoint breakpoint = (IBreakpoint) objects[i];
                    BreakpointContainer[] containers = getMovedFromContainers(breakpoint);
                    for (int j = 0; j < containers.length; j++) {
                        list.add(containers[j]);
                    }
                }
                
            }
        }
        return (BreakpointContainer[]) list.toArray(new BreakpointContainer[list.size()]);
    }
    /**
     * Returns the parent of the given breakpoint that allows the breakpoint
     * to be removed. Only parents of selected breakpoints are considered.
     * 
     * @param breakpoint candidate for removal
     * @return containers that the breakpoint should be removed from
     */
    public BreakpointContainer[] getMovedFromContainers(IBreakpoint breakpoint) {
        BreakpointsViewer viewer = (BreakpointsViewer) getViewer();
        Item[] items = viewer.getSelectedItems();    
        List list = new ArrayList();
        for (int i = 0; i < items.length; i++) {
            TreeItem item = (TreeItem) items[i];
            if (breakpoint.equals(item.getData())) {
                BreakpointContainer parent = getRemoveableParent(item, breakpoint);
                if (parent != null) {
                    list.add(parent);
                }
            }
        }
        return (BreakpointContainer[]) list.toArray(new BreakpointContainer[list.size()]);
    }
        
    private BreakpointContainer getRemoveableParent(TreeItem item, IBreakpoint breakpoint) {
        TreeItem parentItem = item.getParentItem();
        if (parentItem != null) {
            Object data = parentItem.getData();
            if (data instanceof BreakpointContainer) {
                BreakpointContainer container = (BreakpointContainer) data;
                if (container.getOrganizer().canRemove(breakpoint, container.getCategory())) {
                    return container;
                }
            }
            return getRemoveableParent(parentItem, breakpoint);
        }
        return null;
    }    
    
    public boolean isShowingGroups() {
        return fContentProvider.isShowingGroups();
    }
    
	/**
	 * Returns a list containing a point indicating the breakpoint to attempt to
	 * select a list of groups to select, or <code>null</code> if none.
	 * 
	 * @return
	 */
	public List getSelectionState() {
		Tree tree = ((BreakpointsViewer)getViewer()).getTree();
		TreeItem[] selection = tree.getSelection();
		if (selection.length > 0) {
			List list = new ArrayList();
			TreeItem[] roots = tree.getItems();
			TreeItem first = getFirstSelectedItem(roots, selection);
			if (first.getData() instanceof IBreakpoint) {
				TreeItem parentItem = first.getParentItem();
				if (parentItem == null) {
					list.add(new Point(0, indexOf(roots, first)));
				} else {
					int breakpointIndex = indexOf(parentItem.getItems(),first);
					while (parentItem.getParentItem() != null) {
						parentItem = parentItem.getParentItem();
					}
					int groupIndex = indexOf(roots, parentItem);
					list.add(new Point(groupIndex, breakpointIndex));
				}
			} else {
				for (int i = 0; i < selection.length; i++) {
					TreeItem item = selection[i];
					list.add(item.getData());
				}
			}
			return list;
		}
		return null;
	}
	
	private TreeItem getFirstSelectedItem(TreeItem[] items, TreeItem[] selection) {
		for (int i = 0; i < items.length; i++) {
			TreeItem item = items[i];
			if (indexOf(selection, item) >= 0) {
				return item;
			}
			TreeItem first = getFirstSelectedItem(item.getItems(), selection);
			if (first != null) {
				return first;
			}
		}
		return null;
	}
	
	private int indexOf(Object[] list, Object object) {
		for (int i = 0; i < list.length; i++) {
			if (object.equals(list[i])) {
				return i;
			}
		}
		return -1;
	}
    
	public void preserveSelectionState(List state) {
		if (state != null) {
			if (state.get(0) instanceof Point) {
				Point p = (Point) state.get(0);
				int groupIndex = p.x;
				int bpIndex = p.y;
				Tree tree = ((BreakpointsViewer)getViewer()).getTree();
				TreeItem[] roots = tree.getItems();
				TreeItem selection = null;
				if (roots.length > 0 && groupIndex < roots.length) {
					TreeItem group = roots[groupIndex];
					if (group.getData() instanceof IBreakpoint) {
						if (bpIndex < roots.length) {
							selection = roots[bpIndex];
						} else {
							selection = roots[roots.length -1];
						}
					} else {
						TreeItem[] bps = group.getItems();
						while (bps.length > 0 && !(bps[0].getData() instanceof IBreakpoint)) {
							group = bps[0];
							bps = group.getItems();
						}
						if (bpIndex < bps.length) {
							selection = bps[bpIndex];
						} else if (bps.length > 0) {
							selection = bps[bps.length - 1];
						} else {
							selection = group;
						}
					}
				}
				if (selection != null) {
					((BreakpointsViewer)getViewer()).setSelection(selection);
				}
			} else {
				getViewer().setSelection(new StructuredSelection(state));
			}
		}
	}
}
