package org.eclipse.debug.internal.ui.views;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
 
/**
 * Provides contents for the expression view
 */
public class ExpressionViewContentProvider extends VariablesViewContentProvider {

	/**
	 * @see ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object parent) {
		Object[] children= null;
		try {
			if (parent instanceof IExpressionManager) {
				// do not cache parents
				return ((IExpressionManager)parent).getExpressions();
			} else if (parent instanceof IExpression) {
				children = ((IExpression)parent).getValue().getVariables();
			} else if (parent instanceof IVariable) {
				children = ((IVariable)parent).getValue().getVariables();
			}
			if (children != null) {
				cache(parent, children);
				return children;
			}
		} catch (DebugException e) {
			DebugUIPlugin.logError(e);
		}
		return new Object[0];
	}

	/**
	 * Get all decendants of the given parent recursively.
	 * 
	 * @param parent the element whose decendants are to be calculated
	 * @param decendants the list of decendants calculated by
	 *        this method
	 */
	protected List getDecendants(Object parent, List decendants) {
		List children= getChildrenList(parent);
		decendants.addAll(children);
		Iterator iter= children.iterator();
		while(iter.hasNext()) {
			getDecendants(iter.next(), decendants);
		}
		return decendants;
	}

	/**
	 * Returns the child elements of the given parent
	 * element (@see ITreeContentProvider#getChildren(Object)) as a list.
	 * 
	 */
	private List getChildrenList(Object parent) {
		Object[] children= getChildren(parent);
		int numChildren= children.length;
		List list= new ArrayList(numChildren);
		for (int i= 0; i < numChildren; i++) {
			list.add(children[i]);
		}
		return list;
	}
	
	/**
	 * @see ITreeContentProvider#getParent(Object)
	 */
	public Object getParent(Object item) {
		if (item instanceof IExpression) {
			return DebugPlugin.getDefault().getExpressionManager();
		}
		return super.getParent(item);
	}
}
