/*******************************************************************************
 * Copyright (c) 2024 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vector Informatik GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.findandreplace.overlay;

import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;

import org.eclipse.ui.internal.findandreplace.FindReplaceMessages;
import org.eclipse.ui.internal.findandreplace.HistoryStore;

/**
 * Wrap a Text Bar and a ToolItem to add an input history. The text is only
 * stored to history when requested by the Client code, the history is stored in
 * a {@code HistoryStore} provided by the client. The history bar behaves like a
 * normal {@code Text}.
 */
public class HistoryTextWrapper extends Composite {
	final private Text textBar;
	final private AccessibleToolBar tools;
	final private ToolItem dropDown;
	final private HistoryStore history;
	private SearchHistoryMenu menu;

	public HistoryTextWrapper(HistoryStore history, Composite parent, int style) {
		super(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(this);

		this.history = history;

		textBar = new Text(this, style);
		GridDataFactory.fillDefaults().grab(true, true).align(GridData.FILL, GridData.CENTER).applyTo(textBar);
		tools = new AccessibleToolBar(this);
		dropDown = new AccessibleToolItemBuilder(tools).withStyleBits(SWT.PUSH)
				.withToolTipText(FindReplaceMessages.FindReplaceOverlay_searchHistory_toolTip)
				.withImage(FindReplaceOverlayImages.get(FindReplaceOverlayImages.KEY_OPEN_HISTORY))
				.withOperation(this::createHistoryMenuDropdown)
				.build();

		listenForKeyboardHistoryNavigation();
	}

	private void listenForKeyboardHistoryNavigation() {
		addKeyListener(KeyListener.keyPressedAdapter(e -> {
			if (e.keyCode == SWT.ARROW_UP || e.keyCode == SWT.ARROW_DOWN) {
				int stepDirection = e.keyCode == SWT.ARROW_UP ? -1 : 1;
				navigateInHistory(stepDirection);
			}
		}));
	}

	private void createHistoryMenuDropdown() {
		if (menu != null && okayToUse(menu.getShell()) || !dropDown.isEnabled()) {
			return;
		}

		dropDown.setEnabled(false);
		Consumer<String> textUpdaterOnHistoryEntrySelection = selectedHistoryItem -> {
			if (selectedHistoryItem != null) {
				textBar.setText(selectedHistoryItem);
			}
		};
		menu = new SearchHistoryMenu(getShell(), history, textUpdaterOnHistoryEntrySelection);

		Point barPosition = textBar.toDisplay(0, 0);
		Rectangle dropDownBounds = dropDown.getBounds();
		menu.setPosition(barPosition.x, barPosition.y + dropDownBounds.height,
				textBar.getSize().x + dropDownBounds.width);
		menu.open();

		menu.getShell().addDisposeListener(
				__ -> getShell().getDisplay().timerExec(100, HistoryTextWrapper.this::enableDropDown));
	}

	private void enableDropDown() {
		if (!dropDown.isDisposed()) {
			dropDown.setEnabled(true);
		}
	}

	private void navigateInHistory(int navigationOffset) {
		if (history.size() == 0) {
			return;
		}

		int offset = history.indexOf(textBar.getText());

		offset += navigationOffset;
		offset = offset % history.size();

		if (offset < 0) {
			offset = history.size() - 1;
		}

		textBar.setText(history.get(offset));
	}

	public void storeHistory() {
		String string = textBar.getText();
		history.remove(string); // ensure findString is now on the newest index of the history
		history.add(string);
	}

	private static boolean okayToUse(final Widget widget) {
		return widget != null && !widget.isDisposed();
	}

	public void selectAll() {
		textBar.selectAll();
	}

	public void addModifyListener(final ModifyListener listener) {
		textBar.addModifyListener(listener);
	}

	@Override
	public void addFocusListener(final FocusListener listener) {
		textBar.addFocusListener(listener);
	}

	@Override
	public void addKeyListener(final KeyListener listener) {
		textBar.addKeyListener(listener);
	}

	public void setMessage(final String message) {
		textBar.setMessage(message);
	}

	public void setSelection(int i, int j) {
		textBar.setSelection(i, j);
	}

	public String getSelectionText() {
		return textBar.getSelectionText();
	}

	@Override
	public boolean isFocusControl() {
		return textBar.isFocusControl();
	}

	public String getText() {
		return textBar.getText();
	}

	public void setText(String str) {
		textBar.setText(str);
	}

	@Override
	public void setBackground(Color color) {
		super.setBackground(color);

		textBar.setBackground(color);
		tools.setBackground(color);
	}

	@Override
	public void setForeground(Color color) {
		super.setForeground(color);

		textBar.setForeground(color);
		tools.setForeground(color);
	}

	@Override
	public boolean forceFocus() {
		if (!textBar.isDisposed()) {
			return textBar.forceFocus();
		}
		return false;
	}

	@Override
	public void notifyListeners(int eventType, Event event) {
		textBar.notifyListeners(eventType, event);
	}

	public Text getTextBar() {
		return textBar;
	}

	AccessibleToolBar getDropDownTool() {
		return tools;
	}

}
