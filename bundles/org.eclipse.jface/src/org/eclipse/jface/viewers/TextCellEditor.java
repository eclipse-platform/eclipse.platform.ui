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
 *     Tom Eicher <eclipse@tom.eicher.name> - fix minimum width
 *******************************************************************************/

package org.eclipse.jface.viewers;

import static org.eclipse.swt.events.SelectionListener.widgetDefaultSelectedAdapter;

import java.text.MessageFormat;	// Not using ICU to support standalone JFace scenario

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * A cell editor that manages a text entry field.
 * The cell editor's value is the text string itself.
 * <p>
 * This class may be instantiated or subclassed.
 * </p>
 */
public class TextCellEditor extends CellEditor {

	/**
	 * The text control; initially <code>null</code>.
	 */
	protected Text text;

	private ModifyListener modifyListener;

	/**
	 * State information for updating action enablement
	 */
	private boolean isSelection = false;

	private boolean isDeleteable = false;

	private boolean isSelectable = false;

	/**
	 * Default TextCellEditor style
	 * specify no borders on text widget as cell outline in table already
	 * provides the look of a border.
	 */
	private static final int defaultStyle = SWT.SINGLE;

	/**
	 * Creates a new text string cell editor with no control
	 * The cell editor value is the string itself, which is initially the empty
	 * string. Initially, the cell editor has no cell validator.
	 *
	 * @since 2.1
	 */
	public TextCellEditor() {
		setStyle(defaultStyle);
	}

	/**
	 * Creates a new text string cell editor parented under the given control.
	 * The cell editor value is the string itself, which is initially the empty string.
	 * Initially, the cell editor has no cell validator.
	 *
	 * @param parent the parent control
	 */
	public TextCellEditor(Composite parent) {
		this(parent, defaultStyle);
	}

	/**
	 * Creates a new text string cell editor parented under the given control.
	 * The cell editor value is the string itself, which is initially the empty string.
	 * Initially, the cell editor has no cell validator.
	 *
	 * @param parent the parent control
	 * @param style the style bits
	 * @since 2.1
	 */
	public TextCellEditor(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * Checks to see if the "deletable" state (can delete/
	 * nothing to delete) has changed and if so fire an
	 * enablement changed notification.
	 */
	private void checkDeleteable() {
		boolean oldIsDeleteable = isDeleteable;
		isDeleteable = isDeleteEnabled();
		if (oldIsDeleteable != isDeleteable) {
			fireEnablementChanged(DELETE);
		}
	}

	/**
	 * Checks to see if the "selectable" state (can select)
	 * has changed and if so fire an enablement changed notification.
	 */
	private void checkSelectable() {
		boolean oldIsSelectable = isSelectable;
		isSelectable = isSelectAllEnabled();
		if (oldIsSelectable != isSelectable) {
			fireEnablementChanged(SELECT_ALL);
		}
	}

	/**
	 * Checks to see if the selection state (selection /
	 * no selection) has changed and if so fire an
	 * enablement changed notification.
	 */
	private void checkSelection() {
		boolean oldIsSelection = isSelection;
		isSelection = text.getSelectionCount() > 0;
		if (oldIsSelection != isSelection) {
			fireEnablementChanged(COPY);
			fireEnablementChanged(CUT);
		}
	}

	@Override
	protected Control createControl(Composite parent) {
		text = new Text(parent, getStyle());
		text.addSelectionListener(widgetDefaultSelectedAdapter(this::handleDefaultSelection));
		text.addKeyListener(new KeyAdapter() {
			// hook key pressed - see PR 14201
			@Override
			public void keyPressed(KeyEvent e) {
				keyReleaseOccurred(e);

				// as a result of processing the above call, clients may have
				// disposed this cell editor
				if ((getControl() == null) || getControl().isDisposed()) {
					return;
				}
				checkSelection(); // see explanation below
				checkDeleteable();
				checkSelectable();
			}
		});
		text.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_ESCAPE
					|| e.detail == SWT.TRAVERSE_RETURN) {
				e.doit = false;
			}
		});
		// We really want a selection listener but it is not supported so we
		// use a key listener and a mouse listener to know when selection changes
		// may have occurred
		text.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				checkSelection();
				checkDeleteable();
				checkSelectable();
			}
		});
		text.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				TextCellEditor.this.focusLost();
			}
		});
		text.setFont(parent.getFont());
		text.setBackground(parent.getBackground());
		text.setText("");//$NON-NLS-1$
		text.addModifyListener(getModifyListener());
		return text;
	}

	/**
	 * The <code>TextCellEditor</code> implementation of
	 * this <code>CellEditor</code> framework method returns
	 * the text string.
	 *
	 * @return the text string
	 */
	@Override
	protected Object doGetValue() {
		return text.getText();
	}

	@Override
	protected void doSetFocus() {
		if (text != null) {
			text.selectAll();
			text.setFocus();
			checkSelection();
			checkDeleteable();
			checkSelectable();
		}
	}

	/**
	 * The <code>TextCellEditor</code> implementation of
	 * this <code>CellEditor</code> framework method accepts
	 * a text string (type <code>String</code>).
	 *
	 * @param value a text string (type <code>String</code>)
	 */
	@Override
	protected void doSetValue(Object value) {
		Assert.isTrue(text != null && (value instanceof String));
		text.removeModifyListener(getModifyListener());
		text.setText((String) value);
		text.addModifyListener(getModifyListener());
	}

	/**
	 * Processes a modify event that occurred in this text cell editor.
	 * This framework method performs validation and sets the error message
	 * accordingly, and then reports a change via <code>fireEditorValueChanged</code>.
	 * Subclasses should call this method at appropriate times. Subclasses
	 * may extend or reimplement.
	 *
	 * @param e the SWT modify event
	 */
	protected void editOccurred(ModifyEvent e) {
		String value = text.getText();
		if (value == null) {
			value = "";//$NON-NLS-1$
		}
		Object typedValue = value;
		boolean oldValidState = isValueValid();
		boolean newValidState = isCorrect(typedValue);
		if (!newValidState) {
			// try to insert the current value into the error message.
			setErrorMessage(MessageFormat.format(getErrorMessage(), value));
		}
		valueChanged(oldValidState, newValidState);
	}

	/**
	 * Since a text editor field is scrollable we don't
	 * set a minimumSize.
	 */
	@Override
	public LayoutData getLayoutData() {
		LayoutData data = new LayoutData();
		data.minimumWidth= 0;
		return data;
	}

	/**
	 * Return the modify listener.
	 */
	private ModifyListener getModifyListener() {
		if (modifyListener == null) {
			modifyListener = this::editOccurred;
		}
		return modifyListener;
	}

	/**
	 * Handles a default selection event from the text control by applying the editor
	 * value and deactivating this cell editor.
	 *
	 * @param event the selection event
	 *
	 * @since 3.0
	 */
	protected void handleDefaultSelection(SelectionEvent event) {
		// same with enter-key handling code in keyReleaseOccurred(e);
		fireApplyEditorValue();
		deactivate();
	}

	/**
	 * The <code>TextCellEditor</code>  implementation of this
	 * <code>CellEditor</code> method returns <code>true</code> if
	 * the current selection is not empty.
	 */
	@Override
	public boolean isCopyEnabled() {
		if (text == null || text.isDisposed()) {
			return false;
		}
		return text.getSelectionCount() > 0;
	}

	/**
	 * The <code>TextCellEditor</code>  implementation of this
	 * <code>CellEditor</code> method returns <code>true</code> if
	 * the current selection is not empty.
	 */
	@Override
	public boolean isCutEnabled() {
		if (text == null || text.isDisposed()) {
			return false;
		}
		return text.getSelectionCount() > 0;
	}

	/**
	 * The <code>TextCellEditor</code>  implementation of this
	 * <code>CellEditor</code> method returns <code>true</code>
	 * if there is a selection or if the caret is not positioned
	 * at the end of the text.
	 */
	@Override
	public boolean isDeleteEnabled() {
		if (text == null || text.isDisposed()) {
			return false;
		}
		return text.getSelectionCount() > 0
				|| text.getCaretPosition() < text.getCharCount();
	}

	/**
	 * The <code>TextCellEditor</code>  implementation of this
	 * <code>CellEditor</code> method always returns <code>true</code>.
	 */
	@Override
	public boolean isPasteEnabled() {
		if (text == null || text.isDisposed()) {
			return false;
		}
		return true;
	}

	/**
	 * Check if save all is enabled
	 * @return true if it is
	 */
	public boolean isSaveAllEnabled() {
		if (text == null || text.isDisposed()) {
			return false;
		}
		return true;
	}

	/**
	 * Returns <code>true</code> if this cell editor is
	 * able to perform the select all action.
	 * <p>
	 * This default implementation always returns
	 * <code>false</code>.
	 * </p>
	 * <p>
	 * Subclasses may override
	 * </p>
	 * @return <code>true</code> if select all is possible,
	 *  <code>false</code> otherwise
	 */
	@Override
	public boolean isSelectAllEnabled() {
		if (text == null || text.isDisposed()) {
			return false;
		}
		return text.getCharCount() > 0;
	}

	/**
	 * Processes a key release event that occurred in this cell editor.
	 * <p>
	 * The <code>TextCellEditor</code> implementation of this framework method
	 * ignores when the RETURN key is pressed since this is handled in
	 * <code>handleDefaultSelection</code>.
	 * An exception is made for Ctrl+Enter for multi-line texts, since
	 * a default selection event is not sent in this case.
	 * </p>
	 *
	 * @param keyEvent the key event
	 */
	@Override
	protected void keyReleaseOccurred(KeyEvent keyEvent) {
		if (keyEvent.character == '\r') { // Return key
			// Enter is handled in handleDefaultSelection.
			// Do not apply the editor value in response to an Enter key event
			// since this can be received from the IME when the intent is -not-
			// to apply the value.
			// See bug 39074 [CellEditors] [DBCS] canna input mode fires bogus event from Text Control
			//
			// An exception is made for Ctrl+Enter for multi-line texts, since
			// a default selection event is not sent in this case.
			if (text != null && !text.isDisposed()
					&& (text.getStyle() & SWT.MULTI) != 0) {
				if ((keyEvent.stateMask & SWT.CTRL) != 0) {
					super.keyReleaseOccurred(keyEvent);
				}
			}
			return;
		}
		super.keyReleaseOccurred(keyEvent);
	}

	/**
	 * The <code>TextCellEditor</code> implementation of this
	 * <code>CellEditor</code> method copies the
	 * current selection to the clipboard.
	 */
	@Override
	public void performCopy() {
		text.copy();
	}

	/**
	 * The <code>TextCellEditor</code> implementation of this
	 * <code>CellEditor</code> method cuts the
	 * current selection to the clipboard.
	 */
	@Override
	public void performCut() {
		text.cut();
		checkSelection();
		checkDeleteable();
		checkSelectable();
	}

	/**
	 * The <code>TextCellEditor</code> implementation of this
	 * <code>CellEditor</code> method deletes the
	 * current selection or, if there is no selection,
	 * the character next character from the current position.
	 */
	@Override
	public void performDelete() {
		if (text.getSelectionCount() > 0) {
			// remove the contents of the current selection
			text.insert(""); //$NON-NLS-1$
		} else {
			// remove the next character
			int pos = text.getCaretPosition();
			if (pos < text.getCharCount()) {
				text.setSelection(pos, pos + 1);
				text.insert(""); //$NON-NLS-1$
			}
		}
		checkSelection();
		checkDeleteable();
		checkSelectable();
	}

	/**
	 * The <code>TextCellEditor</code> implementation of this
	 * <code>CellEditor</code> method pastes the
	 * the clipboard contents over the current selection.
	 */
	@Override
	public void performPaste() {
		text.paste();
		checkSelection();
		checkDeleteable();
		checkSelectable();
	}

	/**
	 * The <code>TextCellEditor</code> implementation of this
	 * <code>CellEditor</code> method selects all of the
	 * current text.
	 */
	@Override
	public void performSelectAll() {
		text.selectAll();
		checkSelection();
		checkDeleteable();
	}

	/**
	 * This implementation of
	 * {@link CellEditor#dependsOnExternalFocusListener()} returns false if the
	 * current instance's class is TextCellEditor, and true otherwise.
	 * Subclasses that hook their own focus listener should override this method
	 * and return false. See also bug 58777.
	 *
	 * @since 3.4
	 */
	@Override
	protected boolean dependsOnExternalFocusListener() {
		return getClass() != TextCellEditor.class;
	}
}
