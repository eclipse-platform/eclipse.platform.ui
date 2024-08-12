/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
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
 *     Wedia - Joel DRIGO (joel.drigo@wedia-group.com): Bug 470866
 *     Sebastian Thomschke - Adapted code of ScaleFieldEditor for SpinnerFieldEditor
 *******************************************************************************/
package org.eclipse.jface.preference;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;

/**
 * A field editor for an integer type preference. This class may be used as is,
 * or subclassed as required.
 *
 * @since 3.35
 */
public class SpinnerFieldEditor extends FieldEditor {

	/**
	 * Value that will feed {@link Spinner#setDigits(int)}.
	 */
	private int digits = 0;

	/**
	 * Value that will feed {@link Spinner#setIncrement(int)}.
	 */
	private int incrementValue = 1;

	/**
	 * Value that will feed {@link Spinner#setMaximum(int)}.
	 */
	private int maxValue = 10;

	/**
	 * Value that will feed {@link Spinner#setMinimum(int)}.
	 */
	private int minValue = 0;

	/**
	 * Old integer value.
	 */
	private int oldValue;

	/**
	 * Value that will feed {@link Spinner#setPageIncrement(int)}.
	 */
	private int pageIncrementValue = 1;

	/**
	 * Value that will feed {@link Spinner#setTextLimit(int)}.
	 */
	private int textLimit = Spinner.LIMIT;

	/**
	 * The spinner, or <code>null</code> if none.
	 */
	protected Spinner spinner;

	/**
	 * Creates a spinner field editor using default values.
	 *
	 * @param name      the name of the preference this field editor works on
	 * @param labelText the label text of the field editor
	 * @param parent    the parent of the field editor's control
	 */
	public SpinnerFieldEditor(final String name, final String labelText, final Composite parent) {
		super(name, labelText, parent);
		updateSpinner();
	}

	/**
	 * Creates a spinner field editor with particular spinner values.
	 *
	 * @param name      the name of the preference this field editor works on
	 * @param labelText the label text of the field editor
	 * @param parent    the parent of the field editor's control
	 * @param min       the value used for {@link Spinner#setMinimum(int)}.
	 * @param max       the value used for {@link Spinner#setMaximum(int)}.
	 */
	public SpinnerFieldEditor(final String name, final String labelText, final Composite parent, final int min,
			final int max) {
		this(name, labelText, parent);
		setMinimum(min);
		setMaximum(max);
	}

	@Override
	protected void adjustForNumColumns(final int numColumns) {
		((GridData) spinner.getLayoutData()).horizontalSpan = numColumns - 1;
	}

	@Override
	protected void doFillIntoGrid(final Composite parent, final int numColumns) {
		getLabelControl(parent).setLayoutData(new GridData());

		spinner = getSpinnerControl(parent);
		final var gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = GridData.FILL;
		gd.horizontalSpan = numColumns - 1;
		gd.grabExcessHorizontalSpace = true;
		spinner.setLayoutData(gd);
	}

	@Override
	protected void doLoad() {
		if (spinner != null) {
			final int value = getPreferenceStore().getInt(getPreferenceName());
			spinner.setSelection(value);
			oldValue = value;
		}
	}

	@Override
	protected void doLoadDefault() {
		if (spinner != null) {
			final int value = getPreferenceStore().getDefaultInt(getPreferenceName());
			spinner.setSelection(value);
		}
		valueChanged();
	}

	@Override
	protected void doStore() {
		getPreferenceStore().setValue(getPreferenceName(), spinner.getSelection());
	}

	/**
	 * Returns the value that will be used for {@link Spinner#setDigits(int)}.
	 *
	 * @return the value (default 0).
	 * @see Spinner#setDigits(int)
	 */
	public int getDigits() {
		return digits;
	}

	/**
	 * Returns the value that will be used for {@link Spinner#setIncrement(int)}.
	 *
	 * @return the value (default 1).
	 * @see Spinner#setIncrement(int)
	 */
	public int getIncrement() {
		return incrementValue;
	}

	/**
	 * Returns the value that will be used for {@link Spinner#setMaximum(int)}.
	 *
	 * @return the value (default 10).
	 * @see Spinner#setMaximum(int)
	 */
	public int getMaximum() {
		return maxValue;
	}

	/**
	 * Returns the value that will be used for {@link Spinner#setMinimum(int)}.
	 *
	 * @return the value (default 0).
	 * @see Spinner#setMinimum(int)
	 */
	public int getMinimum() {
		return minValue;
	}

	@Override
	public int getNumberOfControls() {
		return 2;
	}

	/**
	 * Returns the value that will be used for
	 * {@link Spinner#setPageIncrement(int)}.
	 *
	 * @return the value (default 1).
	 * @see Spinner#setPageIncrement(int)
	 */
	public int getPageIncrement() {
		return pageIncrementValue;
	}

	/**
	 * Returns the value that will be used for {@link Spinner#setTextLimit(int)}.
	 *
	 * @return the value (default {@link Spinner#LIMIT}).
	 * @see Spinner#setTextLimit(int)
	 */
	public int getTextLimit() {
		return textLimit;
	}

	/**
	 * Returns this field editor's spinner control.
	 *
	 * @return the spinner control, or <code>null</code> if no spinner field is
	 *         created yet
	 */
	public Spinner getSpinnerControl() {
		return spinner;
	}

	/**
	 * Returns this field editor's spinner control. The control is created if it
	 * does not yet exist.
	 *
	 * @param parent the parent
	 * @return the spinner control
	 */
	private Spinner getSpinnerControl(final Composite parent) {
		if (spinner == null) {
			spinner = new Spinner(parent, SWT.BORDER);
			spinner.setFont(parent.getFont());
			spinner.addSelectionListener(widgetSelectedAdapter(event -> valueChanged()));
			spinner.addDisposeListener(event -> spinner = null);
		} else {
			checkParent(spinner, parent);
		}
		return spinner;
	}

	/**
	 * Set the value to be used for Spinner.setDigits(int) and update the spinner.
	 *
	 * @param limit a value greater than 0.
	 * @return <code>this</code>
	 * @see Spinner#setDigits(int)
	 */
	public SpinnerFieldEditor setDigits(final int limit) {
		digits = limit;
		updateSpinner();
		return this;
	}

	@Override
	public void setFocus() {
		if (spinner != null && !spinner.isDisposed()) {
			spinner.setFocus();
		}
	}

	/**
	 * Set the value to be used for Spinner.setIncrement(int) and update the
	 * spinner.
	 *
	 * @param increment a value greater than 0.
	 * @return <code>this</code>
	 * @see Spinner#setIncrement(int)
	 */
	public SpinnerFieldEditor setIncrement(final int increment) {
		incrementValue = increment;
		updateSpinner();
		return this;
	}

	/**
	 * Set the value to be used for Spinner.setMaximum(int) and update the spinner.
	 *
	 * @param max a value greater than 0.
	 * @return <code>this</code>
	 * @see Spinner#setMaximum(int)
	 */
	public SpinnerFieldEditor setMaximum(final int max) {
		maxValue = max;
		updateSpinner();
		return this;
	}

	/**
	 * Set the value to be used for Spinner.setMinumum(int) and update the spinner.
	 *
	 * @param min a value greater than 0.
	 * @return <code>this</code>
	 * @see Spinner#setMinimum(int)
	 */
	public SpinnerFieldEditor setMinimum(final int min) {
		minValue = min;
		updateSpinner();
		return this;
	}

	/**
	 * Set the value to be used for Spinner.setPageIncrement(int) and update the
	 * spinner.
	 *
	 * @param pageIncrement a value greater than 0.
	 * @return <code>this</code>
	 * @see Spinner#setPageIncrement(int)
	 */
	public SpinnerFieldEditor setPageIncrement(final int pageIncrement) {
		pageIncrementValue = pageIncrement;
		updateSpinner();
		return this;
	}

	/**
	 * Set the value to be used for Spinner.setTextLimit(int) and update the
	 * spinner.
	 *
	 * @param limit a value greater than 0.
	 * @return <code>this</code>
	 * @see Spinner#setTextLimit(int)
	 */
	public SpinnerFieldEditor setTextLimit(final int limit) {
		textLimit = limit;
		updateSpinner();
		return this;
	}

	/**
	 * Update the spinner particulars with set values.
	 */
	private void updateSpinner() {
		if (spinner != null && !spinner.isDisposed()) {
			spinner.setDigits(getDigits());

			spinner.setMinimum(getMinimum());
			spinner.setMaximum(getMaximum());
			// Reapplying the minimum to ensure that the spinner's value is correctly
			// adjusted in scenarios where the new minimum exceeds the previous maximum.
			spinner.setMinimum(getMinimum());

			spinner.setIncrement(getIncrement());
			spinner.setPageIncrement(getPageIncrement());
			spinner.setTextLimit(getTextLimit());
		}
	}

	/**
	 * Informs this field editor's listener, if it has one, about a change to the
	 * value (<code>VALUE</code> property) provided that the old and new values are
	 * different.
	 * <p>
	 * This hook is <em>not</em> called when the spinner is initialized (or reset to
	 * the default value) from the preference store.
	 * </p>
	 */
	protected void valueChanged() {
		setPresentsDefaultValue(false);

		final int newValue = spinner.getSelection();
		if (newValue != oldValue) {
			fireStateChanged(IS_VALID, false, true);
			fireValueChanged(VALUE, Integer.valueOf(oldValue), Integer.valueOf(newValue));
			oldValue = newValue;
		}
	}

	@Override
	public void setEnabled(final boolean enabled, final Composite parent) {
		super.setEnabled(enabled, parent);
		if (spinner != null) {
			spinner.setEnabled(enabled);
		}
	}
}
