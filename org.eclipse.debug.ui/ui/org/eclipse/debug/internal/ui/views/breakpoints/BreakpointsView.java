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
package org.eclipse.debug.internal.ui.views.breakpoints;

 
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointManagerListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.actions.LinkBreakpointsWithDebugViewAction;
import org.eclipse.debug.internal.ui.actions.OpenBreakpointMarkerAction;
import org.eclipse.debug.internal.ui.actions.ShowSupportedBreakpointsAction;
import org.eclipse.debug.internal.ui.actions.SkipAllBreakpointsAction;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;

/**
 * This view shows the breakpoints registered with the breakpoint manager
 */
public class BreakpointsView extends AbstractDebugView implements ISelectionListener, IBreakpointManagerListener {

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
	private String fAutoGroup= null;
	
	/**
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		if (getViewer() != null) {
			initializeCheckedState();
			updateViewerBackground();
			DebugPlugin.getDefault().getBreakpointManager().addBreakpointManagerListener(this);
		}
	}

	/**
	 * @see AbstractDebugView#createViewer(Composite)
	 */
	protected Viewer createViewer(Composite parent) {
		final CheckboxTreeViewer viewer = new CheckboxTreeViewer(new Tree(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CHECK));
		viewer.setContentProvider(new BreakpointsViewContentProvider());
		viewer.setLabelProvider(new DelegatingModelPresentation() {
			public Image getImage(Object item) {
				if (item instanceof String) {
					return DebugPluginImages.getImage(IDebugUIConstants.IMG_OBJS_BREAKPOINT_GROUP);
				}
				return super.getImage(item);
			}
		});
		viewer.setSorter(new BreakpointsSorter());
		viewer.setInput(DebugPlugin.getDefault().getBreakpointManager());
		viewer.addCheckStateListener(fCheckListener);
		viewer.addTreeListener(new ITreeViewerListener() {
			public void treeExpanded(TreeExpansionEvent event) {
				initializeCheckedState();
			}
			public void treeCollapsed(TreeExpansionEvent event) {
			}
		});
		
		// Necessary so that the PropertySheetView hears about selections in this view
		getSite().setSelectionProvider(viewer);
		initIsTrackingSelection();
		setEventHandler(new BreakpointsViewEventHandler(this));
		return viewer;
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

	/**
	 * Sets the initial checked state of the items in the viewer.
	 */
	public void initializeCheckedState() {
		IBreakpointManager manager= DebugPlugin.getDefault().getBreakpointManager();
		CheckboxTreeViewer viewer= getCheckboxViewer();
		ITreeContentProvider provider= getTreeContentProvider();
		Object[] elements= provider.getElements(manager);
		ArrayList breakpoints= new ArrayList(elements.length);
		for (int i = 0; i < elements.length; i++) {
			breakpoints.add(elements[i]);
		}
		ListIterator iterator= breakpoints.listIterator();
		while (iterator.hasNext()) {
			try {
				Object element= iterator.next();
				if (element instanceof IBreakpoint && !((IBreakpoint) element).isEnabled()) {
					iterator.remove();
				} else if (element instanceof String) {
					Object[] children = provider.getChildren(element);
					int enabledChildren= 0;
					for (int i = 0; i < children.length; i++) {
						IBreakpoint child = (IBreakpoint) children[i];
						if (child.isEnabled()) {
							iterator.add(child);
							enabledChildren++;
						}
					}
					if (enabledChildren != children.length && enabledChildren > 0) {
						// If some but not all children are enabled, gray the group node
						viewer.setGrayed(element, true);
					} else if (enabledChildren == 0) {
						// Uncheck the group node if no children are enabled
						iterator.remove();
						viewer.setGrayed(element, false);
					}
				}
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
			}
		}
		viewer.setCheckedElements(breakpoints.toArray());
	}
	
	/**
	 * Updates the checked state of the given group based on
	 * the state of its children.
	 */
	protected void updateGroupCheckedState(String group) {
	    ITreeContentProvider provider = getTreeContentProvider();
	    CheckboxTreeViewer viewer = getCheckboxViewer();
	    Object[] children = provider.getChildren(group);
	    int enabledChildren= 0;
	    for (int i = 0; i < children.length; i++) {
            try {
                if (((IBreakpoint) children[i]).isEnabled()) {
                    enabledChildren++;
                }
            } catch (CoreException e) {
                DebugUIPlugin.log(e);
            }
        }
	    if (enabledChildren == 0) {
	        viewer.setGrayChecked(group, false);
	    } else if (enabledChildren == children.length) {
	        viewer.setGrayed(group, false);
	        viewer.setChecked(group, true);
	    } else {
	        viewer.setGrayChecked(group, true);
	    }
	    if (!DebugPlugin.getDefault().getBreakpointManager().isEnabled()) {
	    	viewer.setGrayed(group, true);
	    }
	}
	
	/**
	 * Sets the group that new breakpoints will automatically be
	 * added to.
	 * @param group the group name
	 */
	public void setAutoGroup(String group) {
	    if (group != null && group.length() < 1) {
	        group= null;
	    }
		fAutoGroup= group;
	}
	
	/**
	 * Returns the group that new breakpoints will be automatically
	 * added to or <code>null</code> if none.
	 * @return the group that new breakpoints will be automatically
	 *  added to or <code>null</code> if none
	 */
	public String getAutoGroup() {
		return fAutoGroup;
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
	    return (ITreeContentProvider) getCheckboxViewer().getContentProvider();
	}

	/**
	 * Responds to the user checking and unchecking breakpoints by enabling
	 * and disabling them.
	 * 
	 * @param event the check state change event
	 */
	private void handleCheckStateChanged(CheckStateChangedEvent event) {
		Object source= event.getElement();
		if (source instanceof String) {
			handleGroupChecked(event, (String) source);
		} else if (source instanceof IBreakpoint) {
			handleBreakpointChecked(event, (IBreakpoint) source);
		}
	}
	/**
	 * A breakpoint has been checked/unchecked. Update the group
	 * element's checked/grayed state as appropriate.
	 */
	private void handleBreakpointChecked(CheckStateChangedEvent event, IBreakpoint breakpoint) {
		boolean enable= event.getChecked();
		CheckboxTreeViewer viewer= getCheckboxViewer();
		ITreeContentProvider contentProvider= getTreeContentProvider();
		try {
			breakpoint.setEnabled(enable);
			String group = (String) contentProvider.getParent(breakpoint);
			if (group != null) {
				// First, assume that all other breakpoints will match the group
				// (set ungrayed with appropriate check state)
				if (DebugPlugin.getDefault().getBreakpointManager().isEnabled()) {
					viewer.setGrayed(group, false);
				}
				viewer.setChecked(group, enable);
				Object[] children = contentProvider.getChildren(group);
				for (int i = 0; i < children.length; i++) {
					if (((IBreakpoint) children[i]).isEnabled() != enable) {
						// Then, if any other breakpoints don't match the
						// selected breakpoint, gray and check the group.
						viewer.setGrayChecked(group, true);
						break;
					}
				}
			}
			viewer.update(breakpoint, null);
		} catch (CoreException e) {
			String titleState= enable ? DebugUIViewsMessages.getString("BreakpointsView.6") : DebugUIViewsMessages.getString("BreakpointsView.7"); //$NON-NLS-1$ //$NON-NLS-2$
			String messageState= enable ? DebugUIViewsMessages.getString("BreakpointsView.8") : DebugUIViewsMessages.getString("BreakpointsView.9");  //$NON-NLS-1$ //$NON-NLS-2$
			DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(), MessageFormat.format(DebugUIViewsMessages.getString("BreakpointsView.10"), new String[] { titleState }), MessageFormat.format(DebugUIViewsMessages.getString("BreakpointsView.11"), new String[] { messageState }), e); //$NON-NLS-1$ //$NON-NLS-2$
			// If the breakpoint fails to update, reset its check state.
			getCheckboxViewer().removeCheckStateListener(fCheckListener);
			event.getCheckable().setChecked(breakpoint, !event.getChecked());
			getCheckboxViewer().addCheckStateListener(fCheckListener);
		}
	}

	/**
	 * A group has been checked or unchecked. Enable/disable all of the
	 * breakpoints in that group to match.
	 */
	private void handleGroupChecked(CheckStateChangedEvent event, String group) {
		CheckboxTreeViewer viewer= getCheckboxViewer();
		Object[] children = getTreeContentProvider().getChildren(group);
		boolean enable= event.getChecked();
		viewer.setGrayed(group, false);
		
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(getEventHandler());
		for (int i = 0; i < children.length; i++) {
			IBreakpoint breakpoint= (IBreakpoint) children[i];
			try {
				viewer.setChecked(breakpoint, enable);
				breakpoint.setEnabled(enable);
				viewer.update(breakpoint, null);
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
			}
		}
		if (!DebugPlugin.getDefault().getBreakpointManager().isEnabled()) {
			viewer.setGrayed(group, true);
		}
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(getEventHandler());
		return;
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
	}

	/**
	 * @see AbstractDebugView#createActions()
	 */
	protected void createActions() {
		IAction action = new OpenBreakpointMarkerAction(getViewer());
		setAction("GotoMarker", action); //$NON-NLS-1$
		setAction(DOUBLE_CLICK_ACTION, action);
		setAction("ShowBreakpointsForModel", new ShowSupportedBreakpointsAction(getStructuredViewer(),this)); //$NON-NLS-1$
		setAction("LinkWithDebugView", new LinkBreakpointsWithDebugViewAction(this)); //$NON-NLS-1$
		setAction("SkipBreakpoints", new SkipAllBreakpointsAction()); //$NON-NLS-1$
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
		menu.add(new Separator(IDebugUIConstants.EMPTY_RENDER_GROUP));

		menu.add(new Separator(IDebugUIConstants.SELECT_GROUP));
		menu.add(new Separator(IDebugUIConstants.BREAKPOINT_GROUP_GROUP));
		menu.add(new Separator(IDebugUIConstants.REMOVE_GROUP));
		
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	/**
	 * @see AbstractDebugView#configureToolBar(IToolBarManager)
	 */
	protected void configureToolBar(IToolBarManager tbm) {
		tbm.add(new Separator(IDebugUIConstants.BREAKPOINT_GROUP));
		tbm.add(getAction("ShowBreakpointsForModel")); //$NON-NLS-1$
		tbm.add(getAction("GotoMarker")); //$NON-NLS-1$
		tbm.add(getAction("LinkWithDebugView")); //$NON-NLS-1$
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
		getViewer().refresh();
		initializeCheckedState();
		updateViewerBackground();
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
				updateViewerBackground();
			}
		});
	}

	/**
	 * Updates the background color of the viewer based
	 * on the breakpoint manager enablement.
	 */
	protected void updateViewerBackground() {
		Color color= null;
		boolean enabled = true;
		if (!DebugPlugin.getDefault().getBreakpointManager().isEnabled()) {
			color= DebugUIPlugin.getStandardDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
			enabled = false;
		}
		Tree tree = getCheckboxViewer().getTree();
		updateTreeItems(tree.getItems(), color, !enabled);
		tree.setBackground(color);
		if (enabled) {
			setContentDescription(""); //$NON-NLS-1$
		} else {
			setContentDescription(DebugUIViewsMessages.getString("BreakpointsView.19")); //$NON-NLS-1$
		}
	}
	
	/**
	 * Recursively sets the color and grayed state of the given tree items
	 */
	private void updateTreeItems(TreeItem[] items, Color color, boolean gray) {
	    for (int i = 0; i < items.length; i++) {
            TreeItem item = items[i];
            item.setBackground(color);
            item.setGrayed(gray);
            updateTreeItems(item.getItems(), color, gray);
        }
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	public void doubleClick(DoubleClickEvent event) {
		  IStructuredSelection selection= (IStructuredSelection) event.getSelection();
          if (selection.size() == 1) {
              Object element = selection.getFirstElement();
              if (element instanceof String) {
                  getCheckboxViewer().setExpandedState(element, !getCheckboxViewer().getExpandedState(element));
                  return;
              }
          }
		super.doubleClick(event);
	}
}
