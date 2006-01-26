package org.eclipse.jface.databinding.updatables;

import org.eclipse.jface.databinding.IChangeListener;
import org.eclipse.jface.databinding.IReadableSet;
import org.eclipse.jface.databinding.IUpdatableFunction;
import org.eclipse.jface.databinding.IUpdatableFunctionFactory;

public final class IdentityFunction implements IUpdatableFunction {

	private static IdentityFunction instance = null;
	private static IUpdatableFunctionFactory factory;

	private IdentityFunction() {	
	}
	
	public static IdentityFunction getInstance() {
		if (instance == null) {
			instance = new IdentityFunction();
		}

		return instance;
	}
	
	public static IUpdatableFunctionFactory getFactory() {
		if (factory == null) {
			factory = new IUpdatableFunctionFactory() {
				public IUpdatableFunction createFunction(IReadableSet domain) {
					return getInstance();
				}
			};
		}

		return factory;
	}
	
	public Object computeResult(Object input) {
		return input;
	}

	public void addChangeListener(IChangeListener changeListener) {
	}

	public void removeChangeListener(IChangeListener changeListener) {
	}

	public void dispose() {

	}

	public boolean isStale() {
		return false;
	}
	
	public boolean isDisposed() {
		return false;
	}

}
