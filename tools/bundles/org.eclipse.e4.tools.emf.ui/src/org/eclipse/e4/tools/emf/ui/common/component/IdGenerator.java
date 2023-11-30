/*******************************************************************************
 * Copyright (c) 2014, 2017 TwelveTone LLC and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 437951
 * Patrik Suzzi <psuzzi@gmail.com> - Bug 464464
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.common.component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty.E;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.widgets.Control;

/**
 * Auto generates an id based on another field's current value.
 *
 * @author Steven Spungin
 */
public class IdGenerator {

	private IValueChangeListener<String> listener;
	private IObservableValue<String> observableValue;
	protected boolean ignore;
	private IValueChangeListener<String> listener2;
	private IObservableValue<String> observableValue2;
	static Pattern patternId = Pattern.compile("^(.*\\.)\\d+$"); //$NON-NLS-1$

	/**
	 * Bind must be called AFTER the master observable value is set in order to
	 * properly initialize.
	 *
	 * @param control
	 *            Optional control.
	 */
	public <T> void bind(final IObservableValue<T> master, final IValueProperty<T, String> ebpLabel,
			final IValueProperty<T, String> evpId, Control control) {

		// RULES
		// Only start generating if the label is initially empty and the id ends
		// with a '.'
		// followed by an integer
		// If the id is manually changed, stop generating
		// If the control loses focus, stop generating

		final String origLabel = ebpLabel.getValue(master.getValue());
		if (E.notEmpty(origLabel)) {
			stopGenerating();
			return;
		}
		String origId = evpId.getValue(master.getValue());
		if (origId == null) {
			origId = "id.0"; //$NON-NLS-1$
		}
		final Matcher m = patternId.matcher(origId);
		if (!m.matches()) {
			stopGenerating();
			return;
		}
		final String baseId = m.group(1);

		if (control != null) {
			control.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(org.eclipse.swt.events.FocusEvent e) {
					stopGenerating();
				}
			});

			control.addDisposeListener(e -> stopGenerating());
		}

		observableValue2 = evpId.observe(master.getValue());
		observableValue2.addValueChangeListener(listener2 = event -> {
			if (!ignore) {
				stopGenerating();
			}
		});

		observableValue = ebpLabel.observe(master.getValue());
		observableValue.addValueChangeListener(listener = event -> {
			String labelValue = ebpLabel.getValue(master.getValue());
			if (labelValue == null) {
				labelValue = ""; //$NON-NLS-1$
			}
			final String trimmedIdEnding = trimToLowercase(labelValue);
			ignore = true;
			evpId.setValue(master.getValue(), baseId + trimmedIdEnding);
			ignore = false;
		});

	}

	/**
	 * Strips all illegal id characters, and lower cases each word.
	 */
	protected static String trimToLowercase(String value) {
		final String[] parts = value.split("\\s+"); //$NON-NLS-1$
		final StringBuilder sb = new StringBuilder();
		for (String part : parts) {
			part = part.replaceAll("[^0-9a-zA-Z_-]", ""); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append(part.toLowerCase());
		}
		return sb.toString();
	}

	/**
	 * Removes all listeners and prevents the id from changing
	 */
	public void stopGenerating() {
		if (observableValue != null) {
			observableValue.removeValueChangeListener(listener);
			listener = null;
			observableValue = null;
		}
		if (observableValue2 != null) {
			observableValue2.removeValueChangeListener(listener2);
			listener2 = null;
			observableValue2 = null;
		}
	}

}
