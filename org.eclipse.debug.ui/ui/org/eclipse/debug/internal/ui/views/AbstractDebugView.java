package org.eclipse.debug.internal.ui.views;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugViewAdapter;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.help.ViewContextComputer;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Common functionality for views in the debug UI:<ul>
 * <li>Debug view adpater implementation - <code>IDebugViewAdapter</code></li>
 * <li>Action registry - actions can be stored in this view
 * 		with a key. Actions that implement <code>IUpdate</code>
 *      are updated when <code>updateActions()</code> is
 *		called.</li>
 * <li>Hooks the context menu associated with this view's
 * 		underlying viewer and registers the menu with this
 * 		view's site, such that other plug-ins may contribute.</li>
 * <li>Hooks a key press listener, and invokes the
 * 		<code>REMOVE_ACTION</code> when the delete key 
 * 		is pressed.</li>
 * <li>Hooks a double-click listener, and invokes the
 * 		<code>DOUBLE_CLICK_ACTION</code> when the mouse 
 * 		is double-clicked.</li>
 * </ul>
 */

public abstract class AbstractDebugView extends ViewPart implements IDebugViewAdapter, IDoubleClickListener {
	
	/**
	 * Underlying viewer that displays the contents of
	 * this view.
	 */
	private StructuredViewer fViewer = null;
	
	/**
	 * Map of actions. Keys are strings, values
	 * are <code>IAction</code>.
	 */
	private Map fActionMap = null;
	
	/**
	 * Action id for a view's remove action. Any view
	 * with a remove action that should be invoked when
	 * the delete key is pressed should store their
	 * remove action with this key.
	 * 
	 * @see #setAction(String, IAction)
	 */
	protected static final String REMOVE_ACTION = "Remove_ActionId";
	
	/**
	 * Action id for a view's double-click action. Any view
	 * with an action that should be invoked when
	 * the mouse is double-clicked should store their
	 * action with this key.
	 * 
	 * @see #setAction(String, IAction)
	 */
	protected static final String DOUBLE_CLICK_ACTION = "Double_Click_ActionId";	
	
	/**
	 * Constructs a new debug view.
	 */
	public AbstractDebugView() {
		fActionMap = new HashMap(5);
	}
	
	/**
	 * Debug views implement the debug view adapter which
	 * provides access to a view's underlying viewer and
	 * debug model presentation for a specific debug model.
	 * 
	 * @see IAdaptable#getAdapter(java.lang.Class)
	 * @see IDebugViewAdapter
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IDebugViewAdapter.class) {
			return this;
		}
		return super.getAdapter(adapter);
	}
	
	/**
	 * Creates this view's underlying viewer and actions.
	 * Hooks a pop-up menu to the underlying viewer's control,
	 * as well as a key listener. When the delete key is pressed,
	 * the <code>REMOVE_ACTION</code> is invoked. Hooks help to
	 * this view. Subclasses must implement the following methods
	 * which are called in the following order when a view is
	 * created:<ul>
	 * <li><code>createViewer(Composite)</code> - the context
	 *   menu is hooked to the viewer's control.</li>
	 * <li><code>createActions()</code></li>
	 * <li><code>configureToolBar(IToolBarManager)</code></li>
	 * <li><code>getHelpContextId()</code></li>
	 * </ul>
	 * @see IWorkbenchPart#createPartControl(Composite)
	 * @see #createViewer(Composite)
	 * @see #createActions()
	 * @see #configureToolbar(IToolBarManager)
	 * @see #getHelpContextId()
	 * @see #fillContextMenu(IMenuManager)
	 */
	public final void createPartControl(Composite parent) {
		StructuredViewer viewer = createViewer(parent);
		setViewer(viewer);
		createActions();
		initializeToolBar();
		createContextMenu(getViewer().getControl());
		WorkbenchHelp.setHelp(
			parent,
			new ViewContextComputer(this, getHelpContextId()));
		viewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				handleKeyPressed(e);
			}
		});
		viewer.addDoubleClickListener(this);	
	}	
	/**
	 * Creates and returns this view's underlying viewer.
	 * The viewer's control will automatically be hooked
	 * to display a pop-up menu that other plug-ins may
	 * contribute to. Subclasses must override this method.
	 * 
	 * @param parent the parent control
	 */
	protected abstract StructuredViewer createViewer(Composite parent);
	
	/**
	 * Creates this view's actions. Subclasses must
	 * override this method, which is called after
	 * <code>createViewer(Composite)</code>
	 */
	protected abstract void createActions();	
	
	/**
	 * Returns this view's help context id, which is hooked
	 * to this view on creation.
	 * 
	 * @return help context id
	 */
	protected abstract String getHelpContextId();
	
	/**
	 * IWorkbenchPart#dispose()
	 */
	public void dispose() {
		if (getViewer() != null) {
			getViewer().removeDoubleClickListener(this);
		}
		setViewer(null);
		super.dispose();
	}
	
	/**
	 * @see IDebugViewAdapter#getViewer()
	 */
	public StructuredViewer getViewer() {
		return fViewer;
	}
	
	/**
	 * @see IDebugViewAdapter#getPresentation(String)
	 */
	public IDebugModelPresentation getPresentation(String id) {
		return ((DelegatingModelPresentation)getViewer().getLabelProvider()).getPresentation(id);
	}
	
	/**
	 * Creates a pop-up menu on the given control. The menu
	 * is registered with this view's site, such that other
	 * plug-ins may contribute to the menu. Subclasses should
	 * call this method, specifying the menu control as the
	 * control used in their viewer (for example, tree viewer).
	 * Subclasses must implement the method
	 * <code>#fillContextMenu(IMenuManager)</code> which will
	 * be called each time the context menu is realized.
	 * 
	 * @param menuControl the control with which the pop-up
	 *  menu will be associated with.
	 */
	protected void createContextMenu(Control menuControl) {
		MenuManager menuMgr= new MenuManager("#PopUp"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillContextMenu(mgr);
			}
		});
		Menu menu= menuMgr.createContextMenu(menuControl);
		menuControl.setMenu(menu);

		// register the context menu such that other plugins may contribute to it
		getSite().registerContextMenu(menuMgr, getViewer());
	}
	
	/**
	 * Subclasses must override this method to fill the context
	 * menu each time it is realized.
	 * 
	 * @param menu the context menu
	 */
	protected abstract void fillContextMenu(IMenuManager menu);	
	/**
	 * Configures this view's toolbar. Subclasses implement
	 * <code>#configureToolBar(IToolBarManager)</code> to
	 * contribute actions to the toolbar.
	 * <p>
	 * To properly initialize toggle actions that are contributed
	 * to this view, toggle actions that have an initial state
	 * of 'checked' are invoked. The actions are invoked in a
	 * runnable, after the view is created.
	 * </p>
	 */
	protected void initializeToolBar() {
		final IToolBarManager tbm= getViewSite().getActionBars().getToolBarManager();
		configureToolBar(tbm);
		getViewSite().getActionBars().updateActionBars();
		
		// this is in a runnable to be run after this view's pane
		// is created
		Runnable r = new Runnable() {
			public void run() {
				if (getViewer().getControl().isDisposed()) {
					return;
				}
				IContributionItem[] items = tbm.getItems();
				if (items != null) {
					for (int i = 0; i < items.length; i++) {
						if (items[i] instanceof ActionContributionItem) {
							IAction action = ((ActionContributionItem)items[i]).getAction();
							if (action.isChecked()) {
								action.run();
							}
						}
					}
				}
			}
		};
		if (getViewer().getControl().isDisposed()) {
			return;
		}
		getViewer().getControl().getDisplay().asyncExec(r);
	}
	
	/**
	 * @see IWorkbenchPart
	 */
	public void setFocus() {
		StructuredViewer viewer= getViewer();
		if (viewer != null) {
			Control c = viewer.getControl();
			if (!c.isFocusControl()) {
				c.setFocus();
			}
		}
	}	
	
	/**
	 * Sets the viewer for this view.
	 * 
	 * @param viewer structured viewer
	 */
	private void setViewer(StructuredViewer viewer) {
		fViewer = viewer;
	}
	
	/**
	 * Subclasses implement this menu to contribute actions
	 * to the toolbar. This method is called after 
	 * <code>createActions()</code>.
	 * 
	 * @param tbm the tool bar manager for this view's site
	 * @see #createViewer(Composite)
	 */
	protected abstract void configureToolBar(IToolBarManager tbm);	
	
	/**
	 * Installs the given action under the given action id.
	 *
	 * @param actionId the action id
	 * @param action the action, or <code>null</code> to clear it
	 * @see #getAction
	 */
	public void setAction(String actionID, IAction action) {
		if (action == null)
			fActionMap.remove(actionID);
		else
			fActionMap.put(actionID, action);
	}	
	
	/**
	 * Returns the action installed under the given action id.
	 *
	 * @param actionId the action id
	 * @return the action, or <code>null</code> if none
	 * @see #setAction
	 */
	public IAction getAction(String actionID) {
		return (IAction) fActionMap.get(actionID);
	}
	
	/**
	 * Updates all actions that are instances of 
	 * <code>IUpdate</code>.
	 */
	public void updateActions() {
		Iterator actions = fActionMap.values().iterator();
		while (actions.hasNext()) {
			Object object = actions.next();
			if (object instanceof IUpdate) {
				((IUpdate)object).update();
			}
		}
	}
	
	/**
	 * Updates all actions that are instances of 
	 * <code>ISelectionChangedListener</code>.
	 */
	public void updateSelectionActions() {
		ISelection selection = getViewer().getSelection();
		SelectionChangedEvent event = new SelectionChangedEvent(getViewer(), selection);
		Iterator actions = fActionMap.values().iterator();
		while (actions.hasNext()) {
			Object object = actions.next();
			if (object instanceof ISelectionChangedListener) {
				((ISelectionChangedListener)object).selectionChanged(event);
			}
		}
	}
	
			
	/**
	 * Handles key events in viewer. Imvokes the 
	 * <code>REMOVE_ACTION</code> when the delete
	 * key is pressed.
	 */
	protected void handleKeyPressed(KeyEvent event) {
		if (event.character == SWT.DEL && event.stateMask == 0) {
			IAction action = getAction(REMOVE_ACTION);
			if (action != null && action.isEnabled()) {
				action.run();
			}
		}
	}	
	
	/**
	 * Delegate to the <code>DOUBLE_CLICK_ACTION</code>,
	 * if any.
	 *  
	 * @see IDoubleClickListener#doubleClick(DoubleClickEvent)
	 */
	public void doubleClick(DoubleClickEvent event) {
		IAction action = getAction(DOUBLE_CLICK_ACTION);
		if (action != null) {
			action.run();
		}
	}	
	
	/**
	 * Registers the given runnable with the display
	 * assocaited with this view's control, if any.
	 * 
	 * @see Display.asyncExec(Runnable)
	 */
	protected void asyncExec(Runnable r) {
		if (getViewer() != null) {
			Control ctrl= getViewer().getControl();
			if (ctrl != null && !ctrl.isDisposed()) {
				ctrl.getDisplay().asyncExec(r);
			}
		}
	}
	
	/**
	 * Registers the given runnable with the display
	 * assocaited with this view's control, if any.
 	 *
	 * @see Display.syncExec(Runnable)
	 */
	protected void syncExec(Runnable r) {
		if (getViewer() != null) {
			Control ctrl= getViewer().getControl();
			if (ctrl != null && !ctrl.isDisposed()) {
				ctrl.getDisplay().syncExec(r);
			}
		}
	}		
}	

