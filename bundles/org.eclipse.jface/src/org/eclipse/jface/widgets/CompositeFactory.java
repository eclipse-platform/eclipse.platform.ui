/*******************************************************************************
 * Copyright (c) 2019 Marcus Hoepfner and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marcus Hoepfner - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.widgets;

import java.util.function.Supplier;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

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
 * @since 3.18
 */
public final class CompositeFactory extends AbstractCompositeFactory<CompositeFactory, Composite> {

	private CompositeFactory(int style) {
		super(CompositeFactory.class, (Composite parent) -> new Composite(parent, style));
	}

	/**
	 * Creates a new CompositeFactory with the given style. Refer to
	 * {@link Composite#Composite(Composite, int)} for possible styles.
	 *
	 * @return a new CompositeFactory instance
	 */
	public static CompositeFactory newComposite(int style) {
		return new CompositeFactory(style);
	}


	/**
	 * Sets a {@link Supplier} for the creation of layout associated with the
	 * receiver. The supplier should always create a new instance of the layout in
	 * order to make this factory reusable
	 *
	 * <pre>
	 * GridLayoutFactory gridFactory = GridLayoutFactory.fillDefaults();
	 * CompositeFactory.newComposite(SWT.BODER).supplyLayout(gridFactory::create);
	 * </pre>
	 *
	 * or without GridDataFactory:
	 *
	 * <pre>
	 * CompositeFactory.newComposite(SWT.BODER).supplyLayout(GridLayout::new);
	 * </pre>
	 *
	 * @param layoutSupplier {@link Supplier} creating a new layout instance on
	 *                       every call
	 * @return this
	 *
	 * @see Composite#setLayout(Layout)
	 * @since 3.22
	 */
	public CompositeFactory supplyLayout(Supplier<Layout> layoutSupplier) {
		addProperty(c -> c.setLayout(layoutSupplier.get()));
		return cast(this);
	}


}