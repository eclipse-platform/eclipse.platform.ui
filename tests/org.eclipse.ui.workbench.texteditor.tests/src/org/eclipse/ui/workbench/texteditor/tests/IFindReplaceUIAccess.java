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
package org.eclipse.ui.workbench.texteditor.tests;

import java.util.Set;

import org.eclipse.swt.widgets.Widget;

import org.eclipse.jface.text.IFindReplaceTarget;

import org.eclipse.ui.internal.findandreplace.IFindReplaceLogic;
import org.eclipse.ui.internal.findandreplace.SearchOptions;


/**
 * Wraps UI-Access for different Find/Replace-UIs
 */
interface IFindReplaceUIAccess {

	IFindReplaceTarget getTarget();

	void closeAndRestore();

	void close();

	void ensureHasFocusOnGTK();

	void unselect(SearchOptions option);

	void select(SearchOptions option);

	void simulateEnterInFindInputField(boolean shiftPressed);

	void simulateKeyPressInFindInputField(int keyCode, boolean shiftPressed);

	String getFindText();

	String getReplaceText();

	void setFindText(String text);

	void setReplaceText(String text);

	Widget getButtonForSearchOption(SearchOptions option);

	Set<SearchOptions> getEnabledOptions();

	Set<SearchOptions> getSelectedOptions();

	IFindReplaceLogic getFindReplaceLogic();

	void performReplaceAll();

	void performReplace();

	void performReplaceAndFind();

	abstract void assertInitialConfiguration();

	void assertUnselected(SearchOptions option);

	void assertSelected(SearchOptions option);

	void assertDisabled(SearchOptions option);

	void assertEnabled(SearchOptions option);

}
