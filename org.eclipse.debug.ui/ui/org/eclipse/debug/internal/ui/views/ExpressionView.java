package org.eclipse.debug.internal.ui.views;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
 
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
		return null;
	}		
	
	/**
	 * Initializes the viewer input on creation
	 */
	protected void setInitialContent() {
		getViewer().setInput(DebugPlugin.getDefault().getExpressionManager());
	}	
}
