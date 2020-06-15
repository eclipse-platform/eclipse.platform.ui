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

import java.util.function.Consumer;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Sash;

/**
 * This class provides a convenient shorthand for creating and initializing
 * {@link Sash}. This offers several benefits over creating Sash normal way:
 *
 * <ul>
 * <li>The same factory can be used many times to create several Sash
 * instances</li>
 * <li>The setters on SashFactory all return "this", allowing them to be
 * chained</li>
 * <li>SashFactory accepts a Lambda for {@link SelectionEvent} (see
 * {@link #onSelect})</li>
 * </ul>
 *
 * Example usage:
 *
 * <pre>
 * Sash sash = SashFactory.newSash(SWT.HORIZONTAL) //
 * 		.onSelect(event -&gt; sashSelected(event)) //
 * 		.layoutData(gridData) //
 * 		.create(parent);
 * </pre>
 * <p>
 * The above example creates a horizontal sash, registers a SelectionListener
 * and finally creates the sash in "parent".
 * </p>
 *
 * <pre>
 * GridDataFactory gridDataFactory = GridDataFactory.swtDefaults();
 * SashFactory sashFactory = SashFactory.newSash(SWT.HORIZONTAL).onSelect(event -&gt; sashSelected(event))
 * 		.layout(gridDataFactory::create);
 * sashFactory.data("Sash 1").create(parent);
 * sashFactory.data("Sash 2").create(parent);
 * sashFactory.data("Sash 3").create(parent);
 * </pre>
 * <p>
 * The above example creates three sashs using the same instance of SashFactory.
 * Note the layout method. A Supplier is used to create unique GridData for
 * every single sash.
 * </p>
 *
 * @since 3.21
 */
public final class SashFactory extends AbstractControlFactory<SashFactory, Sash> {

	private SashFactory(int style) {
		super(SashFactory.class, composite -> new Sash(composite, style));
	}

	/**
	 * Creates a new SashFactory with the given style. Refer to
	 * {@link Sash#Sash(Composite, int)} for possible styles.
	 *
	 * @param style
	 * @return a new SashFactory instance
	 */
	public static SashFactory newSash(int style) {
		return new SashFactory(style);
	}

	/**
	 * Creates a {@link SelectionListener} and registers it for the widgetSelected
	 * event. If the receiver is selected by the user the given consumer is invoked.
	 * The {@link SelectionEvent} is passed to the consumer.
	 *
	 * @param consumer the consumer whose accept method is called
	 * @return this
	 *
	 * @see Sash#addSelectionListener(SelectionListener)
	 * @see SelectionListener#widgetSelectedAdapter(Consumer)
	 */
	public SashFactory onSelect(Consumer<SelectionEvent> consumer) {
		addProperty(c -> c.addSelectionListener(SelectionListener.widgetSelectedAdapter(consumer)));
		return this;
	}
}