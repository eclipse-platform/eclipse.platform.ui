/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.events;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Classes that implement this interface will be notified before and after the
 * expandable control's expansion state changes.
 *
 * @since 3.0
 */
public interface IExpansionListener {
	/**
	 * Notifies the listener that the expandable control is about to change its
	 * expansion state. The state provided by the event is the new state.
	 *
	 * @param e
	 *            the expansion event
	 */
	void expansionStateChanging(ExpansionEvent e);
	/**
	 * Notifies the listener after the expandable control has changed its
	 * expansion state. The state provided by the event is the new state.
	 *
	 * @param e
	 *            the expansion event
	 */
	void expansionStateChanged(ExpansionEvent e);

	/**
	 * Static helper method to create a <code>IExpansionListener</code> for the
	 * {@link #expansionStateChanging(ExpansionEvent)} method, given a lambda
	 * expression or a method reference.
	 *
	 * @param consumer the consumer of the event
	 * @return IExpansionListener
	 * @since 3.9
	 */
	static IExpansionListener expansionStateChangingAdapter(Consumer<ExpansionEvent> consumer) {
		Objects.requireNonNull(consumer);
		return new ExpansionAdapter() {
			@Override
			public void expansionStateChanging(ExpansionEvent e) {
				consumer.accept(e);
			}
		};
	}

	/**
	 * Static helper method to create a <code>IExpansionListener</code> for the
	 * {@link #expansionStateChanged(ExpansionEvent)} method, given a lambda
	 * expression or a method reference.
	 *
	 * @param consumer the consumer of the event
	 * @return IExpansionListener
	 * @since 3.9
	 */
	static IExpansionListener expansionStateChangedAdapter(Consumer<ExpansionEvent> consumer) {
		Objects.requireNonNull(consumer);
		return new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				consumer.accept(e);
			}
		};
	}
}