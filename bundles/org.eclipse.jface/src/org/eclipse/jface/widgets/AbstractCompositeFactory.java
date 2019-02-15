package org.eclipse.jface.widgets;

import java.util.function.Function;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

/**
 * Abstract factory for composites. Factories for widgets that inherit from
 * Composite should extend this factory to handle the properties of Composite
 * itself, like layout.
 *
 * @param <F>
 * @param <C>
 *
 */
public abstract class AbstractCompositeFactory<F extends AbstractCompositeFactory<?, ?>, C extends Composite>
		extends AbstractControlFactory<F, C> {

	/**
	 * @param factoryClass
	 * @param controlCreator
	 */
	protected AbstractCompositeFactory(Class<F> factoryClass, Function<Composite, C> controlCreator) {
		super(factoryClass, controlCreator);
	}

	/**
	 * Sets the layout.
	 *
	 * @param layout
	 * @return this
	 */
	public F layout(Layout layout) {
		addProperty(control -> control.setLayout(layout));
		return cast(this);
	}
}