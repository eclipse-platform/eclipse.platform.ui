package org.eclipse.jface.databinding.updatables;

import org.eclipse.jface.databinding.IUpdatableFunction;
import org.eclipse.jface.databinding.Updatable;
import org.eclipse.jface.databinding.UpdatableTracker;

/**
 * Abstract base class for implementations of IUpdatableFunction. Clients may subclass
 * in order to implement the IUpdatableFunction interface.
 * 
 * @since 3.2
 */
public abstract class UpdatableFunction extends Updatable implements IUpdatableFunction {
	public final Object computeResult(Object input) {
		UpdatableTracker.getterCalled(this);
		
		return doComputeResult(input);
	}

	protected abstract Object doComputeResult(Object input);	
	
}
