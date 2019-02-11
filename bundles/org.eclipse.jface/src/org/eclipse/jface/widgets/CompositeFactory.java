package org.eclipse.jface.widgets;

import org.eclipse.swt.widgets.Composite;

/**
 * This class provides a convenient shorthand for creating and initializing
 * {@link Composite}. This offers several benefits over creating Composite
 * normal way:
 *
 * <ul>
 * <li>The same factory can be used many times to create several Composite
 * instances</li>
 * <li>The setters on CompositeFactory all return "this", allowing them to be
 * chained</li>
 * </ul>
 *
 * @since 3.4
 *
 */
public class CompositeFactory extends AbstractCompositeFactory<CompositeFactory, Composite> {

	private CompositeFactory(int style) {
		super(CompositeFactory.class, (Composite parent) -> new Composite(parent, style));
	}

	/**
	 * Creates a new CompositeFactory with the given style. Refer to
	 * {@link Composite#Composite(Composite, int)} for possible styles.
	 *
	 * @param style
	 * @return a new CompositeFactory instance
	 */
	public static CompositeFactory newComposite(int style) {
		return new CompositeFactory(style);
	}
}