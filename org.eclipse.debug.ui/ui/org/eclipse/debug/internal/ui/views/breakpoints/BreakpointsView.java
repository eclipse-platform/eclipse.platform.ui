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
import java.util.List;
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
	private static String KEY_BREAKPOINT_CONTAINER_FACTORIES= "breakpointContainerFactories"; //$NON-NLS-1$
	private static String KEY_VALUE="value"; //$NON-NLS-1$
	private String fAutoGroup= null;
	private BreakpointsViewContentProvider fContentProvider;
	
	/**
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		if (getViewer() != null) {
			updateViewerBackground();
			DebugPlugin.getDefault().getBreakpointManager().addBreakpointManagerListener(this);
		}
	}

	/**
	 * @see AbstractDebugView#createViewer(Composite)
	 */
	protected Viewer createViewer(Composite parent) {
		fContentProvider= new BreakpointsViewContentProvider();
		final CheckboxTreeViewer viewer = new CheckboxTreeViewer(new Tree(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CHECK)) {
		    public void refresh() {
		    	BreakpointsViewContentProvider provider = (BreakpointsViewContentProvider) getContentProvider();
		    	List expanded= new ArrayList();
		    	IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
		    	for (int i = 0; i < breakpoints.length; i++) {
					IBreakpoint breakpoint= breakpoints[i];
					Object parent = provider.getParent(breakpoint);
					if (parent instanceof IBreakpointContainer && getExpandedState(parent)) {
						expanded.add(breakpoint);
					}
				}
		    	provider.clearParentCache();
                super.refresh();
                Iterator iter = expanded.iterator();
                while (iter.hasNext()) {
                	expandToLevel(iter.next(), ALL_LEVELS);
                }
                initializeCheckedState(this, fContentProvider);
            }
		};
		viewer.setContentProvider(fContentProvider);
		viewer.setSorter(new BreakpointsSorter());
		viewer.setInput(DebugPlugin.getDefault().getBreakpointManager());
		viewer.addCheckStateListener(fCheckListener);
		viewer.addTreeListener(new ITreeViewerListener() {
			public void treeExpanded(TreeExpansionEvent event) {
				initializeCheckedState(viewer, fContentProvider);
			}
			public void treeCollapsed(TreeExpansionEvent event) {
			}
		});
	    viewer.setLabelProvider(new DelegatingModelPresentation() {
			public Image getImage(Object item) {
				if (item instanceof IBreakpointContainer) {
					Image image = ((IBreakpointContainer) item).getContainerImage();
					if (image == null) {
						image= DebugPluginImages.getImage(IDebugUIConstants.IMG_OBJS_BREAKPOINT_GROUP);
					}
					return image;
				}
				return super.getImage(item);
			}
			
			public String getText(Object item) {
				if (item instanceof IBreakpointContainer) {
					IBreakpointContainer container= (IBreakpointContainer) item;
					return container.getName();
				}
				return super.getText(item);
			}
		});
		
		// Necessary so that the PropertySheetView hears about selections in this view
		getSite().setSelectionProvider(viewer);
		initIsTrackingSelection();
		initBreakpointContainerFactories();
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
	
	private void initBreakpointContainerFactories() {
		IMemento memento = getMemento();
		if (memento != null) {
			IMemento node = memento.getChild(KEY_BREAKPOINT_CONTAINER_FACTORIES);
			if (node != null) {
				String factoryIds = node.getString(KEY_VALUE);
				BreakpointContainerFactoryManager manager = BreakpointContainerFactoryManager.getDefault();
				List factories= new ArrayList();
				int start= 0;
				int index= factoryIds.indexOf(',');
				while (index != -1 && start < factoryIds.length() - 1) {
					String factoryId= factoryIds.substring(start, index);
					if (factoryId.length() > 0) {
						IBreakpointContainerFactory factory = manager.getFactory(factoryId);
						if (factory != null) {
							factories.add(factory);
						}
					}
					start= index + 1;
					index= factoryIds.indexOf(',', start);
				}
				fContentProvider.setBreakpointContainerFactories(factories);
			}
		}
	}

	/**
	 * Sets the initial checked state of the items in the viewer.
	 */
	public void initializeCheckedState(CheckboxTreeViewer viewer, ITreeContentProvider provider) {
		IBreakpointManager manager= DebugPlugin.getDefault().getBreakpointManager();
		Object[] elements= provider.getElements(manager);
		ArrayList elementsToCheck= new ArrayList(elements.length);
		for (int i = 0; i < elements.length; i++) {
			elementsToCheck.add(elements[i]);
		}
		ListIterator iterator= elementsToCheck.listIterator();
		while (iterator.hasNext()) {
			updateCheckedState(iterator.next(), viewer, provider);
		}
	}
	
	public void updateCheckedState(Object element, CheckboxTreeViewer viewer, ITreeContentProvider provider) {
		if (element instanceof IBreakpoint) {
			try {
				viewer.setChecked(element, ((IBreakpoint) element).isEnabled());
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
			}
		} else if (element instanceof IBreakpointContainer) {
			IBreakpoint[] breakpoints = ((IBreakpointContainer) element).getBreakpoints();
			int enabledChildren= 0;
			for (int i = 0; i < breakpoints.length; i++) {
				IBreakpoint breakpoint = breakpoints[i];
				try {
					if (breakpoint.isEnabled()) {
						enabledChildren++;
					}
				} catch (CoreException e) {
					DebugUIPlugin.log(e);
				}
			}
			if (enabledChildren == 0) {
				// Uncheck the container node if no children are enabled
				viewer.setGrayChecked(element, false);
			} else if (enabledChildren == breakpoints.length) {
				// Check the container if all children are enabled
				viewer.setGrayed(element, false);
				viewer.setChecked(element, true);
			} else {
				// If some but not all children are enabled, gray the container node
				viewer.setGrayChecked(element, true);
			}
			// Update any children (breakpoints and containers)
			Object[] children = provider.getChildren(element);
			for (int i = 0; i < children.length; i++) {
				updateCheckedState(children[i], viewer, provider);
			}
		}
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
		if (source instanceof IBreakpointContainer) {
			handleContainerChecked(event, (IBreakpointContainer) source);
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
		try {
			breakpoint.setEnabled(enable);
			viewer.update(breakpoint, null);
			// updateParents will also be called from the breakpointChanged callback,
			// but we include it here so that the feedback is immediate when the user
			// toggles a breakpoint within the view.
			updateParents(breakpoint, enable);
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
	 * Updates the checked state of the given object's container assuming
	 * that the child element has changed to the given enabled state.
	 * @param object
	 * @param enable
	 */
	public void updateParents(Object object, boolean enable) {
		Object parent= getTreeContentProvider().getParent(object);
		if (!(parent instanceof IBreakpointContainer)) {
			return;
		}
		CheckboxTreeViewer viewer= getCheckboxViewer();
		IBreakpointContainer container= (IBreakpointContainer) parent;
		// First, assume that all other breakpoints will match the group
		// (set ungrayed with appropriate check state)
		if (DebugPlugin.getDefault().getBreakpointManager().isEnabled()) {
			viewer.setGrayed(container, false);
		}
		viewer.setChecked(container, enable);
		IBreakpoint[] breakpoints = container.getBreakpoints();
		for (int i = 0; i < breakpoints.length; i++) {
			try {
				if (breakpoints[i].isEnabled() != enable) {
					// Then, if any other breakpoints don't match the
					// selected breakpoint, gray and check the group.
					viewer.setGrayChecked(container, true);
					break;
				}
			} catch (CoreException e) {
			}
		}
		updateParents(parent, enable);
	}

	/**
	 * A group has been checked or unchecked. Enable/disable all of the
	 * breakpoints in that group to match.
	 */
	private void handleContainerChecked(CheckStateChangedEvent event, IBreakpointContainer container) {
		CheckboxTreeViewer viewer= getCheckboxViewer();
		IBreakpoint[] breakpoints = container.getBreakpoints();
		boolean enable= event.getChecked();
		viewer.setGrayed(container, false);
		
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(getEventHandler());
		for (int i = 0; i < breakpoints.length; i++) {
			IBreakpoint breakpoint= breakpoints[i];
			try {
				viewer.setChecked(breakpoint, enable);
				breakpoint.setEnabled(enable);
				viewer.update(breakpoint, null);
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
			}
		}
		// updateParents will also be called from the breakpointChanged callback,
		// but we include it here so that the feedback is immediate when the user
		// toggles a breakpoint within the view.
		updateParents(container, enable);
		if (!DebugPlugin.getDefault().getBreakpointManager().isEnabled()) {
			viewer.setGrayed(container, true);
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
		
		StringBuffer buffer= new StringBuffer();
		List breakpointContainerFactories = getBreakpointContainerFactories();
		Iterator iter = breakpointContainerFactories.iterator();
		while (iter.hasNext()) {
			IBreakpointContainerFactory factory= (IBreakpointContainerFactory) iter.next();
			buffer.append(factory.getIdentifier()).append(',');
		}
		node = memento.createChild(KEY_BREAKPOINT_CONTAINER_FACTORIES);
		node.putString(KEY_VALUE, buffer.toString());
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
              if (element instanceof IBreakpointContainer) {
                  getCheckboxViewer().setExpandedState(element, !getCheckboxViewer().getExpandedState(element));
                  return;
              }
          }
		super.doubleClick(event);
	}

	/**
	 * @param selectedContainers
	 */
	public void setBreakpointContainerFactories(List selectedContainers) {
		fContentProvider.setBreakpointContainerFactories(selectedContainers);
		getViewer().refresh();
		getCheckboxViewer().expandAll();
	}
	
	public List getBreakpointContainerFactories() {
		return fContentProvider.getBreakpointContainerFactories();
	}
}
