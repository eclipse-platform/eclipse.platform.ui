/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stefan Xenos, IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.layout;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Control;

/**
 * This class provides a convenient shorthand for creating and initialising
 * RowData. This offers several benefits over creating RowData the normal way:
 *
 * <ul>
 * <li>The same factory can be used many times to create several RowData
 * instances</li>
 * <li>The setters on RowDataFactory all return "this", allowing them to be
 * chained</li>
 * </ul>
 *
 * @since 3.5
 */
public final class RowDataFactory {
	private final RowData data;

	/**
	 * Creates a RowDataFactory that creates copies of the given RowData.
	 *
	 * @param data
	 *            object to be copied
	 */
	private RowDataFactory(RowData data) {
		this.data = data;
	}

	/**
	 * Creates a new RowDataFactory initialized with the SWT defaults.
	 *
	 * <p>
	 * Initial values are:
	 * </p>
	 *
	 * <ul>
	 * <li>exclude(false)</li>
	 * <li>hint(SWT.DEFAULT, SWT.DEFAULT)</li>
	 * </ul>
	 *
	 * @return a new GridDataFactory instance
	 */
	public static RowDataFactory swtDefaults() {
		return new RowDataFactory(new RowData());
	}

	/**
	 * Creates a new RowDataFactory that creates copies of the given RowData by
	 * default.
	 *
	 * @param data
	 *            RowData to copy
	 * @return a new RowDataFactory that creates copies of the argument by
	 *         default
	 */
	public static RowDataFactory createFrom(RowData data) {
		return new RowDataFactory(copyData(data));
	}

	/**
	 * Returns a copy of the given RowData
	 *
	 * @param data
	 *            RowData to copy
	 * @return a copy of the argument
	 */
	public static RowData copyData(RowData data) {
		RowData newData = new RowData(data.width, data.height);
		newData.exclude = data.exclude;

		return newData;
	}

	/**
	 * Instructs the GridLayout to ignore this control when performing layouts.
	 *
	 * @param shouldExclude
	 *            true iff the control should be excluded from layouts
	 * @return this
	 */
	public RowDataFactory exclude(boolean shouldExclude) {
		data.exclude = shouldExclude;
		return this;
	}

	/**
	 * Creates a new GridData instance. All attributes of the GridData instance
	 * will be initialised by the factory.
	 *
	 * @return a new GridData instance
	 */
	public RowData create() {
		return copyData(data);
	}

	/**
	 * Creates a copy of the receiver.
	 *
	 * @return a copy of the receiver
	 */
	public RowDataFactory copy() {
		return new RowDataFactory(create());
	}

	/**
	 * Sets the layout data on the given control. Creates a new RowData instance
	 * and assigns it to the control by calling control.setLayoutData.
	 *
	 * @param control
	 *            control whose layout data will be initialised
	 */
	public void applyTo(Control control) {
		control.setLayoutData(create());
	}

	/**
	 * Sets the width and height hints. The width and height hints override the
	 * control's preferred size. If either hint is set to SWT.DEFAULT, the
	 * control's preferred size is used.
	 *
	 * @param xHint
	 *            horizontal hint (pixels), or SWT.DEFAULT to use the control's
	 *            preferred size
	 * @param yHint
	 *            vertical hint (pixels), or SWT.DEFAULT to use the control's
	 *            preferred size
	 * @return this
	 */
	public RowDataFactory hint(int xHint, int yHint) {
		data.width = xHint;
		data.height = yHint;
		return this;
	}

	/**
	 * Sets the width and height hints. The width and height hints override the
	 * control's preferred size. If either hint is set to SWT.DEFAULT, the
	 * control's preferred size is used.
	 *
	 * @param hint
	 *            size (pixels) to be used instead of the control's preferred
	 *            size. If the x or y values are set to SWT.DEFAULT, the
	 *            control's computeSize() method will be used to obtain that
	 *            dimension of the preferred size.
	 * @return this
	 */
	public RowDataFactory hint(Point hint) {
		data.width = hint.x;
		data.height = hint.y;
		return this;
	}
}
