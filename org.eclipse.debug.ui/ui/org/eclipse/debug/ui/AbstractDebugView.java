package org.eclipse.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.debug.internal.ui.LazyModelPresentation;
import org.eclipse.debug.internal.ui.views.DebugSelectionManager;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.help.ViewContextComputer;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
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
 * <li>Provides a mechanism for displaying an error message
 * 		in the view, via the <code>PageBookView</code> mechanism.
 * 		By default, a page book is created with a page showing
 * 		this view's viewer. A message page is also created
 * 		and shown when <code>showMessage(String)</code> is
 * 		called.</li>
 * </ul>
 * <p>
 * This class may be subclassed.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 */

public abstract class AbstractDebugView extends PageBookView implements IDebugViewAdapter, IDoubleClickListener {
	
	/**
	 * Underlying viewer that displays the contents of
	 * this view.
	 */
	private Viewer fViewer = null;
	
	/**
	 * This view's message page.
	 */
	private MessagePage fMessagePage = null;
	
	/**
	 * Map of actions. Keys are strings, values
	 * are <code>IAction</code>.
	 */
	private Map fActionMap = null;
	
	/**
	 * The memento that was used to persist the state of this view.
	 * May be <code>null</code>.
	 */
	private IMemento fMemento;
	
	/**
	 * Action id for a view's remove action. Any view
	 * with a remove action that should be invoked when
	 * the delete key is pressed should store their
	 * remove action with this key.
	 * 
	 * @see #setAction(String, IAction)
	 */
	public static final String REMOVE_ACTION = "Remove_ActionId"; //$NON-NLS-1$
	
	/**
	 * Action id for a view's double-click action. Any view
	 * with an action that should be invoked when
	 * the mouse is double-clicked should store their
	 * action with this key.
	 * 
	 * @see #setAction(String, IAction)
	 */
	public static final String DOUBLE_CLICK_ACTION = "Double_Click_ActionId";	 //$NON-NLS-1$
	
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
	 * A page in this view's page book that contains this
	 * view's viewer.
	 */
	class ViewerPage extends Page {
		/**
		 * @see IPage#createControl(Composite)
		 */
		public void createControl(Composite parent) {
			Viewer viewer = createViewer(parent);
			setViewer(viewer);			
		}

		/**
		 * @see IPage#getControl()
		 */
		public Control getControl() {
			return getDefaultControl();
		}

		/*
		 * @see IPage#setFocus()
		 */
		public void setFocus() {
			Viewer viewer= getViewer();
			if (viewer != null) {
				Control c = viewer.getControl();
				if (!c.isFocusControl()) {
					c.setFocus();
				}
			}
		}

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
		super.createPartControl(parent);
		createActions();
		initializeToolBar();
		createContextMenu(getViewer().getControl());
		WorkbenchHelp.setHelp(
			parent,
			new ViewContextComputer(this, getHelpContextId()));
		getViewer().getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				handleKeyPressed(e);
			}
		});
		if (getViewer() instanceof StructuredViewer) {
			((StructuredViewer)getViewer()).addDoubleClickListener(this);	
		}
		// notify the selection manager that a debug view has been
		// created/realized.
		DebugSelectionManager.getDefault().registerView(this);
		// create the message page
		setMessagePage(new MessagePage());
		getMessagePage().createControl(getPageBook());
		initPage(getMessagePage());
	}	
	
	/**
	 * The default page for a debug view is its viewer.
	 * 
	 * @see PageBookView#createDefaultPage(PageBook)
	 */
	protected final IPage createDefaultPage(PageBook book) {
		ViewerPage page = new ViewerPage();
		page.createControl(book);
		initPage(page);
		return page;
	}	
	/**
	 * Creates and returns this view's underlying viewer.
	 * The viewer's control will automatically be hooked
	 * to display a pop-up menu that other plug-ins may
	 * contribute to. Subclasses must override this method.
	 * 
	 * @param parent the parent control
	 */
	protected abstract Viewer createViewer(Composite parent);
	
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
		if (getViewer() instanceof StructuredViewer) {
			((StructuredViewer)getViewer()).removeDoubleClickListener(this);
		}
		setViewer(null);
		fActionMap.clear();
		super.dispose();
	}
	
	/**
	 * @see IDebugViewAdapter#getViewer()
	 */
	public Viewer getViewer() {
		return fViewer;
	}
	
	/**
	 * Returns this view's viewer as a structured viewer,
	 * or <code>null</code> if none.
	 * 
	 * @return this view's viewer as a structured viewer
	 * 	or <code>null</code>
	 */
	protected StructuredViewer getStructuredViewer() {
		if (getViewer() instanceof StructuredViewer) {
			return (StructuredViewer)getViewer();
		}
		return null;
	}
	
	/**
	 * Returns this view's viewer as a text viewer,
	 * or <code>null</code> if none.
	 * 
	 * @return this view's viewer as a text viewer
	 * 	or <code>null</code>
	 */
	protected TextViewer getTextViewer() {
		if (getViewer() instanceof TextViewer) {
			return (TextViewer)getViewer();
		}
		return null;
	}	
	
	/**
	 * @see IDebugViewAdapter#getPresentation(String)
	 */
	public IDebugModelPresentation getPresentation(String id) {
		if (getViewer() instanceof StructuredViewer) {
			IBaseLabelProvider lp = ((StructuredViewer)getViewer()).getLabelProvider();
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
	 * to this view, state is restored for toggle actions that have
	 * a persisted state in the view's memento.  As well, any toggle
	 * actions that have an initial state of 'checked' are invoked.
	 * The actions' states are restored and the actions are invoked 
	 * in a runnable, after the view is created.
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
							if (action.getStyle() == IAction.AS_CHECK_BOX && getMemento() != null) {
								initActionState(getMemento(), action);	
							}
							if (action.isChecked()) {
								action.run();
							}
						}
					}
				}
				setMemento(null);
			}
		};
		if (getViewer().getControl().isDisposed()) {
			return;
		}
		asyncExec(r);
	}
	
	/**
	 * Sets the viewer for this view.
	 * 
	 * @param viewer viewer
	 */
	private void setViewer(Viewer viewer) {
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
	 * Handles key events in viewer. Invokes the 
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
		if (action != null && action.isEnabled()) {
			action.run();
		}
	}	
	
	/**
	 * Registers the given runnable with the display
	 * associated with this view's control, if any.
	 * 
	 * @see Display.asyncExec(Runnable)
	 */
	public void asyncExec(Runnable r) {
		if (getViewer() != null) {
			Control ctrl= getViewer().getControl();
			if (ctrl != null && !ctrl.isDisposed()) {
				ctrl.getDisplay().asyncExec(r);
			}
		}
	}
	
	/**
	 * Registers the given runnable with the display
	 * associated with this view's control, if any.
 	 *
	 * @see Display.syncExec(Runnable)
	 */
	public void syncExec(Runnable r) {
		if (getViewer() != null) {
			Control ctrl= getViewer().getControl();
			if (ctrl != null && !ctrl.isDisposed()) {
				ctrl.getDisplay().syncExec(r);
			}
		}
	}	
	
	/**
	 * @see IViewPart#init(IViewSite, IMemento)
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		//store the memento to be used when this view is created.
		setMemento(memento);
	}
	
	/**
	 * Returns the memento that contains the persisted state of
	 * the view.  May be <code>null</code>.
	 */
	protected IMemento getMemento() {
		return fMemento;
	}

	/** 
	 * Sets the memento that contains the persisted state of the 
	 * view.
	 */
	protected void setMemento(IMemento memento) {
		fMemento = memento;
	}
	
	/**
	 * Persists the state of the check box actions contributed
	 * to this view.
	 * 
	 * @see IViewPart#saveState(IMemento)
	 */
	public void saveState(IMemento memento) {
		if (getMemento() != null) {
			//this view was never fully created
			//persist the old values.
			memento.putMemento(getMemento());
			return;
		}
		IToolBarManager tbm= getViewSite().getActionBars().getToolBarManager();
		IContributionItem[] items= tbm.getItems();
		for (int i = 0; i < items.length; i++) {
			IContributionItem iContributionItem = items[i];
			if (iContributionItem instanceof ActionContributionItem) {
				ActionContributionItem item= (ActionContributionItem)iContributionItem;
				IAction action= item.getAction();
				if (action.getStyle() == IAction.AS_CHECK_BOX) {
					saveActionState(memento, action);			
				}
			}		
		}
	}
	
	/**
	 * Persists the checked state of the action in the memento.
	 * The state is persisted as an <code>Integer</code>: <code>1</code>
	 * meaning the action is checked; <code>0</code> representing unchecked.
	 */
	protected void saveActionState(IMemento memento, IAction action) {
		String id= action.getId();
		if (id != null) {
			int state= action.isChecked() ? 1 : 0;
			memento.putInteger(id, state);
		}
	}
	
	/**
	 * Restores the persisted checked state of the action as stored
	 * in the memento.
	 * <p>
	 * The state was persisted as an <code>Integer</code>: <code>1</code>
	 * meaning the action is checked; <code>0</code> representing unchecked.
	 * 
	 * @param memento the memento used to persist the actions state
	 * @param action the action that needs its state restored.
	 */
	protected void initActionState(IMemento memento, IAction action) {
		String id= action.getId();
		if (id != null) {
			Integer state= memento.getInteger(id);
			if (state != null) {
				action.setChecked(state.intValue() == 1);
			}
		}
	}
	
	/**
	 * Returns the specified view in this view's page
	 * or <code>null</code> if none.
	 * 
	 * @param id view identifier
	 * @return view part
	 */
	protected IViewPart findView(String id) {
		IWorkbenchPage page = getSite().getPage();
		IViewPart view = null;
		if (page != null) {
			view = page.findView(id);
		}
		return view;	
	}
	
	/**
	 * @see PageBookView#isImportant(IWorkbenchPart)
	 */
	protected boolean isImportant(IWorkbenchPart part) {
		return false;
	}

	/**
	 * @see PageBookView#doCreatePage(IWorkbenchPart)
	 */
	protected PageRec doCreatePage(IWorkbenchPart part) {
		return null;
	}

	/**
	 * @see PageBookView#doDestroyPage(IWorkbenchPart, PageRec)
	 */
	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
	}

	/**
	 * @see PageBookView#getBootstrapPart()
	 */
	protected IWorkbenchPart getBootstrapPart() {
		return null;
	}

	/**
	 * Returns the default control for this view. By defuault,
	 * this view's viewer's control is returned. Subclasses
	 * should override if required - for example, if this
	 * view has its viewer nested inside other controls.
	 * 
	 * @return this view's default control.
	 */ 
	protected Control getDefaultControl() {
		Viewer viewer = getViewer();
		if (viewer != null) {
			return viewer.getControl();
		} 
		return null;
	}
	
	/**
	 * Sets this view's message page
	 * 
	 * @param page message page
	 */
	private void setMessagePage(MessagePage page) {
		fMessagePage = page;
	}
	
	/**
	 * Returns this view's message page
	 * 
	 * @return message page
	 */
	protected MessagePage getMessagePage() {
		return fMessagePage;
	}	
	
	/**
	 * Shows the given message in this view's message'
	 * page. Makes the message page the visible page.
	 * 
	 * @param message the message to display
	 */
	public void showMessage(String message) {
		if (getPageBook().isDisposed()) {
			return;
		}
		getMessagePage().setMessage(message);
		getPageBook().showPage(getMessagePage().getControl());
	}
	
	/**
	 * Shows this view's viewer page.
	 */
	public void showViewer() {
		if (getPageBook().isDisposed()) {
			return;
		}
		getPageBook().showPage(getDefaultPage().getControl());
	}
	
	/**
	 * Returns whether this view's viewer is
	 * currently available.
	 * 
	 * @return whether this view's viewer is
	 * currently available
	 */
	public boolean isAvailable() {
		return !(getViewer() == null || getViewer().getControl() == null || getViewer().getControl().isDisposed());
	}	
}	

