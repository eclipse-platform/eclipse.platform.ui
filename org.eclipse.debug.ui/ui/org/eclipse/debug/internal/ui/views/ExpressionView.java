package org.eclipse.debug.internal.ui.views;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.actions.RemoveAllExpressionsAction;
import org.eclipse.debug.internal.ui.actions.RemoveExpressionAction;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchActionConstants;
 
/**
 * Displays expressions and their values with a detail
 * area.
 */
public class ExpressionView extends VariablesView {
	
	/**
	 * @see AbstractDebugView#createViewer(Composite)
	 */
	public Viewer createViewer(Composite parent) {
		Viewer v = super.createViewer(parent);
		// do not listen for selection changes in the debug view
		DebugSelectionManager.getDefault().removeSelectionChangedListener(this, getSite().getPage(), IDebugUIConstants.ID_DEBUG_VIEW);
		return v;
	}
	

	/**
	 * Creates this view's content provider.
	 * 
	 * @return a content provider
	 */
	protected IContentProvider createContentProvider() {
		return new ExpressionViewContentProvider();
	}
	
	/**
	 * Creates this view's event handler.
	 * 
	 * @param viewer the viewer associated with this view
	 * @return an event handler
	 */
	protected AbstractDebugEventHandler createEventHandler(Viewer viewer) {
		return new ExpressionViewEventHandler(this);
	}		
	
	/**
	 * @see AbstractDebugView#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return IDebugHelpContextIds.EXPRESSION_VIEW;		
	}	
	
	/**
	 * Initializes the viewer input on creation
	 */
	protected void setInitialContent() {
		getViewer().setInput(DebugPlugin.getDefault().getExpressionManager());
	}	
	
	/**
	 * @see AbstractDebugView#createActions()
	 */
	protected void createActions() {
		super.createActions();
		
		IAction action = new RemoveExpressionAction(getViewer());
		setAction(REMOVE_ACTION,action); 
		setAction("RemoveAll",new RemoveAllExpressionsAction()); 		 //$NON-NLS-1$
	}
	
	/**
	 * Configures the toolBar.
	 * 
	 * @param tbm The toolbar that will be configured
	 */
	protected void configureToolBar(IToolBarManager tbm) {
		tbm.add(new Separator(this.getClass().getName()));
		tbm.add(new Separator(IDebugUIConstants.RENDER_GROUP));
		tbm.add(getAction("ShowTypeNames")); //$NON-NLS-1$
		tbm.add(new Separator(IDebugUIConstants.EMPTY_EXPRESSION_GROUP));		
		tbm.add(new Separator(IDebugUIConstants.EXPRESSION_GROUP));
		tbm.add(getAction(REMOVE_ACTION));
		tbm.add(getAction("RemoveAll")); //$NON-NLS-1$
		tbm.add(new Separator("TOGGLE_VIEW")); //$NON-NLS-1$
		tbm.add(getAction("ShowDetailPane")); //$NON-NLS-1$
	}	
	
   /**
	* Adds items to the tree viewer's context menu including any extension defined
	* actions.
	* 
	* @param menu The menu to add the item to.
	*/
	protected void fillContextMenu(IMenuManager menu) {

		menu.add(new Separator(IDebugUIConstants.EMPTY_EXPRESSION_GROUP));
		menu.add(new Separator(IDebugUIConstants.EXPRESSION_GROUP));
		menu.add(getAction("ChangeVariableValue")); //$NON-NLS-1$
		menu.add(getAction(REMOVE_ACTION));
		menu.add(getAction("RemoveAll")); //$NON-NLS-1$
		menu.add(new Separator(IDebugUIConstants.EMPTY_RENDER_GROUP));
		menu.add(new Separator(IDebugUIConstants.RENDER_GROUP));
		menu.add(getAction("ShowTypeNames")); //$NON-NLS-1$
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}	
}
