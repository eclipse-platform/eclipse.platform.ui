package org.eclipse.debug.internal.ui.views;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionListener;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.Viewer;
 
/**
 * Updates the expression view
 */ 
public class ExpressionViewEventHandler extends VariablesViewEventHandler implements IExpressionListener {

	/**
	 * Constructs a new event handler on the given view and
	 * viewer
	 * 
	 * @param view variables view
	 * @param viewer tree viewer
	 */
	public ExpressionViewEventHandler(AbstractDebugView view, Viewer viewer) {
		super(view, viewer);
		DebugPlugin plugin= DebugPlugin.getDefault();
		plugin.getExpressionManager().addExpressionListener(this);		
	}
	
	/**
	 * @see BasicContentProvider#doHandleDebug(Event)
	 */
	protected void doHandleDebugEvent(DebugEvent event) {
		switch (event.getKind()) {
			case DebugEvent.SUSPEND:
			case DebugEvent.CHANGE:
				refresh();

				// We have to be careful NOT to populate the detail pane in the
				// variables view on any CHANGE DebugEvent, since the very act of 
				// populating the detail pane does an evaluation, which queues up
				// a CHANGE DebugEvent, which would lead to an infinite loop.  It's
				// probably safer to add invidual event details here as needed,
				// rather than try to exclude the ones we think are problematic.
				if (event.getDetail() == DebugEvent.STEP_END) {
					getVariablesView().populateDetailPane();
				}
				break;
		}
	}	
	
	/**
	 * De-registers this event handler from the debug model.
	 */
	public void dispose() {
		DebugPlugin plugin= DebugPlugin.getDefault();
		plugin.getExpressionManager().removeExpressionListener(this);
		super.dispose();
	}	
	
	/**
	 * @see IExpressionListener#expressionAdded(IExpression)
	 */
	public void expressionAdded(final IExpression expression) {
		Runnable r = new Runnable() {
			public void run() {
				insert(expression);
				selectAndReveal(expression);
				getTreeViewer().expandToLevel(expression, 1);
			}
		};
		getView().asyncExec(r);
	}

	/**
	 * @see IExpressionListener#expressionRemoved(IExpression)
	 */
	public void expressionRemoved(final IExpression expression) {
		Runnable r = new Runnable() {
			public void run() {
				remove(expression);
				IContentProvider provider= getTreeViewer().getContentProvider();
				if (provider instanceof ExpressionViewContentProvider) {
					ExpressionViewContentProvider expressionProvider= (ExpressionViewContentProvider) provider;
					List children= new ArrayList();
					expressionProvider.getDecendants(expression, children);
					// Remove the parent cache for the expression's children
					expressionProvider.removeCache(children.toArray());
					// Remove the parent cache for the expression itself
					expressionProvider.removeCache(new Object[] {expression});
				}
			}
		};
		getView().asyncExec(r);		
	}

	/**
	 * @see IExpressionListener#expressionChanged(IExpression)
	 */
	public void expressionChanged(final IExpression expression) {
		Runnable r = new Runnable() {
			public void run() {
				refresh(expression);
			}
		};
		getView().asyncExec(r);			
	}

}
