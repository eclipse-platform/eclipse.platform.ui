/*******************************************************************************
 * Copyright (c) 2024 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.internal.findandreplace;

import org.eclipse.swt.widgets.Widget;

/**
 * Wraps UI access for different find/replace UIs
 */
public interface IFindReplaceUIAccess {

	void closeAndRestore();

	void close();

	boolean isShown();

	void unselect(SearchOptions option);

	void select(SearchOptions option);

	void simulateKeyboardInteractionInFindInputField(int keyCode, boolean shiftPressed);

	String getFindText();

	String getSelectedFindText();

	String getReplaceText();

	void setFindText(String text);

	void setReplaceText(String text);

	boolean hasFocus();

	Widget getButtonForSearchOption(SearchOptions option);

	void performReplaceAll();

	void performReplace();

	void performReplaceAndFind();

	void assertInitialConfiguration();

	void assertUnselected(SearchOptions option);

	void assertSelected(SearchOptions option);

	void assertDisabled(SearchOptions option);

	void assertEnabled(SearchOptions option);

}
