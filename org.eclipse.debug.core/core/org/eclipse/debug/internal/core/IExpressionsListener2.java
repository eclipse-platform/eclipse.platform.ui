package org.eclipse.debug.internal.core;

import org.eclipse.debug.core.IExpressionsListener;
import org.eclipse.debug.core.model.IExpression;

public interface IExpressionsListener2 extends IExpressionsListener {

	/**
	 * Fires the model delta necessary to update the viewer after one or more
	 * expressions have been moved to a different index in the tree.  The
	 * expression array must be in the same order as they were added.  The given index
	 * <strong>must</strong> take into account the removal of the expressions to be removed.
	 * Therefore, for each of the expressions being moved with indices lower than the expect
	 * insertion index, the passed insertion index must be reduced by one.
	 * 
	 * @param expressions array of expressions to be moved
	 * @param index the index the expressions will be added to, adjusted for moved expressions
	 */
	public void expressionsMoved(IExpression[] expressions, int index);
	
	/**
	 * Fires the model delta necessary to update the viewer after one or more
	 * expressions have been inserted into a specific index in the tree.  The 
	 * expression array must be in the same order as they were added.
	 * 
	 * @param expressions array of expressions to be moved
	 * @param index the index the expressions will be added to
	 */
	public void expressionsInserted(IExpression[] expressions, int index);
	
}
