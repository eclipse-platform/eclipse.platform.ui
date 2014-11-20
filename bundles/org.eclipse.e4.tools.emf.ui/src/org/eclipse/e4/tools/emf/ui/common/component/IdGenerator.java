/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 437951
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.common.component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty.E;
import org.eclipse.emf.databinding.edit.IEMFEditValueProperty;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.widgets.Control;

/**
 * Auto generates an id based on another field's current value.
 *
 * @author Steven Spungin
 *
 */
public class IdGenerator {

	private IValueChangeListener listener;
	private IObservableValue observableValue;
	protected boolean ignore;
	private IValueChangeListener listener2;
	private IObservableValue observableValue2;
	static Pattern patternId = Pattern.compile("^(.*\\.)\\d+$"); //$NON-NLS-1$

	/**
	 * Bind must be called AFTER the master observable value is set in order to
	 * properly initialize.
	 *
	 * @param master
	 * @param ebpLabel
	 * @param evpId
	 * @param control
	 *            Optional control.
	 */
	public void bind(final IObservableValue master, final IEMFEditValueProperty ebpLabel,
		final IEMFEditValueProperty evpId, Control control) {

		// RULES
		// Only start generating if the label is initially empty and the id ends
		// with a '.'
		// followed by an integer
		// If the id is manually changed, stop generating
		// If the control loses focus, stop generating

		final String origLabel = (String) ebpLabel.getValue(master.getValue());
		if (E.notEmpty(origLabel)) {
			stopGenerating();
			return;
		}
		String origId = (String) evpId.getValue(master.getValue());
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

			control.addDisposeListener(new DisposeListener() {

				@Override
				public void widgetDisposed(DisposeEvent e) {
					stopGenerating();
				}
			});
		}

		observableValue2 = evpId.observe(master.getValue());
		observableValue2.addValueChangeListener(listener2 = new IValueChangeListener() {

			@Override
			public void handleValueChange(ValueChangeEvent event) {
				if (!ignore) {
					stopGenerating();
				}
			}
		});

		observableValue = ebpLabel.observe(master.getValue());
		observableValue.addValueChangeListener(listener = new IValueChangeListener() {

			@Override
			public void handleValueChange(ValueChangeEvent event) {
				String labelValue = (String) ebpLabel.getValue(master.getValue());
				if (labelValue == null) {
					labelValue = ""; //$NON-NLS-1$
				}
				final String camelCase = camelCase(labelValue);
				ignore = true;
				evpId.setValue(master.getValue(), baseId + camelCase);
				ignore = false;
			}
		});

	}

	/**
	 * Strips all illegal id characters, and camel cases each word.
	 *
	 * @param value
	 * @return
	 */
	protected static String camelCase(String value) {
		final String[] parts = value.split("\\s+"); //$NON-NLS-1$
		String ret = ""; //$NON-NLS-1$
		boolean first = true;
		for (String part : parts) {
			part = part.replaceAll("[^0-9a-zA-Z_-]", ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (first) {
				first = false;
				ret = part.toLowerCase();
			} else {
				part = part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase();
				ret += part;
			}
		}
		return ret;
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
