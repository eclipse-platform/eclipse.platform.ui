/*******************************************************************************
 * Copyright (c) 2018 Google, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stefan Xenos (Google) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.tests.forms.widgets;

final class ScrollTestData {
	boolean useLayoutExtension;
	boolean expandHorizontal;
	boolean expandVertical;
	int minWidth;
	int maxWidth;
	int maxHeight;
	int formMinX;
	int formMinY;
	boolean hScroll;
	boolean vScroll;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("new ScrollTestData()");
		builder.append(".setMax(" + maxWidth + ", " + maxHeight + ")");
		if (formMinX != 0 || formMinY != 0) {
			builder.append(".setFormMin(" + formMinX + ", " + formMinY + ")");
		}
		if (expandHorizontal || expandVertical) {
			builder.append(".expand(" + expandHorizontal + ", " + expandVertical + ")");
		}
		if (useLayoutExtension) {
			builder.append(".setMinWidth(" + minWidth + ")");
		}
		if (hScroll || vScroll) {
			builder.append(".expectScroll(" + hScroll + ", " + vScroll + ")");
		}
		return builder.toString();
	}

	public boolean expectsHScroll() {
		return hScroll;
	}

	public boolean expectsVScroll() {
		return vScroll;
	}

	public boolean useLayoutExtension() {
		return useLayoutExtension;
	}

	public boolean expandHorizontal() {
		return expandHorizontal;
	}

	public boolean expandVertical() {
		return expandVertical;
	}

	public int getMinWidth() {
		return minWidth;
	}

	public int getMaxWidth() {
		return maxWidth;
	}

	public int getMaxHeight() {
		return maxHeight;
	}

	public int getFormMinX() {
		return formMinX;
	}

	public int getFormMinY() {
		return formMinY;
	}

	public ScrollTestData expectScroll(boolean h, boolean v) {
		this.hScroll = h;
		this.vScroll = v;
		return this;
	}

	public ScrollTestData expand(boolean expandHorizontal, boolean expandVertical) {
		this.expandHorizontal = expandHorizontal;
		this.expandVertical = expandVertical;
		return this;
	}

	public ScrollTestData setMinWidth(int minWidth) {
		useLayoutExtension = (minWidth != -1);
		this.minWidth = minWidth;
		return this;
	}

	public ScrollTestData setMax(int maxWidth, int maxHeight) {
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
		return this;
	}

	public ScrollTestData setFormMin(int formMinX, int formMinY) {
		this.formMinX = formMinX;
		this.formMinY = formMinY;
		return this;
	}
}