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

import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;

/**
 * This class provides a convenient shorthand for creating and initializing
 * {@link SashForm}. This offers several benefits over creating SashForm normal
 * way:
 *
 * <ul>
 * <li>The same factory can be used many times to create several SashForm
 * instances</li>
 * <li>The setters on SashFormFactory all return "this", allowing them to be
 * chained</li>
 * </ul>
 *
 * Example usage:
 *
 * <pre>
 * SashForm sashForm = SashFormFactory.newFormSash(SWT.HORIZONTAL).create(parent);
 * Composite c1 = CompositeFactory.newComposite(SWT.NONE).create(sashForm);
 * Composite c2 = CompositeFactory.newComposite(SWT.NONE).create(sashForm);
 * sashForm.setWeights(new int[] { 30, 70 });
 * </pre>
 * <p>
 * The above example creates a horizontal sash. Note that
 * {@link SashForm#setWeights(int[])} can only be called after children have
 * been added to the SashForm. Hence this method is not part of this factory.
 * </p>
 *
 * <pre>
 * GridDataFactory gridDataFactory = GridDataFactory.swtDefaults();
 * SashFactory sashFactory = SashFactory.newSash(SWT.HORIZONTAL).layout(gridDataFactory::create);
 * sashFactory.data("Sash 1").create(parent);
 * sashFactory.data("Sash 2").create(parent);
 * sashFactory.data("Sash 3").create(parent);
 * </pre>
 * <p>
 * The above example creates three sashForms using the same instance of
 * SashFormFactory. Note the layout method. A Supplier is used to create unique
 * GridData for every single sash.
 * </p>
 *
 * @since 3.21
 */
public final class SashFormFactory extends AbstractControlFactory<SashFormFactory, SashForm> {

	private SashFormFactory(int style) {
		super(SashFormFactory.class, parent -> new SashForm(parent, style));
	}

	/**
	 * Creates a new SashFormFactory with the given style. Refer to
	 * {@link SashForm#SashForm(Composite, int)} for possible styles.
	 *
	 * @return a new SashFormFactory instance
	 */
	public static SashFormFactory newSashForm(int style) {
		return new SashFormFactory(style);
	}

	/**
	 * Specify the width of the sashes when the controls in the SashForm are laid
	 * out.
	 *
	 * @param width the width of the sashes
	 * @return this
	 *
	 * @see SashForm#setSashWidth
	 */
	public SashFormFactory sashWidth(int width) {
		addProperty(sash -> sash.setSashWidth(width));
		return this;
	}
}