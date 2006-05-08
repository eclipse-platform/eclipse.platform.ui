/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.internal.navigator.extensions.INavigatorSiteEditor;
import org.eclipse.ui.navigator.CommonViewer;


/**
 * A NavigatorSiteEditor is used to edit (i.e., rename) elements in a Navigator view. It displays a
 * text editor box overlay on the Navigator tree widget.
 *  
 * @since 3.2
 */
public class NavigatorSiteEditor implements INavigatorSiteEditor {

	private Tree navigatorTree;
	private TreeEditor treeEditor;
	private Text textEditor;
	private Composite textEditorParent;
	private TextActionHandler textActionHandler;
	private String text; // the text being edited
	private CommonViewer commonViewer;


	/**
	 * Creates an instance of a NavigatorSiteEditor.
	 * 
	 * @param aCommonViewer
	 *            the viewer this editor applies to
	 * @param navigatorTree
	 *            the tree that is being edited
	 */
	public NavigatorSiteEditor(CommonViewer aCommonViewer, Tree navigatorTree) {
		commonViewer = aCommonViewer;
		this.navigatorTree = navigatorTree;
		treeEditor = new TreeEditor(navigatorTree);
	}

	/**
	 * Creates the parent composite for the editor overlay.
	 * 
	 * @return the parent composite for the editor overlay
	 */
	Composite createParent() {
		Composite result = new Composite(navigatorTree, SWT.NONE);
		TreeItem[] selectedItems = navigatorTree.getSelection();
		treeEditor.horizontalAlignment = SWT.LEFT;
		treeEditor.grabHorizontal = true;
		treeEditor.setEditor(result, selectedItems[0]);
		return result;
	}

	/**
	 * Creates the text editor widget.
	 * 
	 * @param runnable
	 *            the Runnable to execute when editing ends by the user pressing enter or clicking
	 *            outside the text editor box.
	 */
	void createTextEditor(final Runnable runnable) {
		// Create text editor parent. This draws a nice bounding rect.
		textEditorParent = createParent();
		textEditorParent.setVisible(false);
		textEditorParent.addListener(SWT.Paint, new Listener() {
			public void handleEvent(Event e) {
				Point textSize = textEditor.getSize();
				Point parentSize = textEditorParent.getSize();
				e.gc.drawRectangle(0, 0, Math.min(textSize.x + 4, parentSize.x - 1), parentSize.y - 1);
			}
		});

		// Create inner text editor.
		textEditor = new Text(textEditorParent, SWT.NONE);
		textEditorParent.setBackground(textEditor.getBackground());
		textEditor.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event e) {
				Point textSize = textEditor.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				textSize.x += textSize.y; // Add extra space for new characters.
				Point parentSize = textEditorParent.getSize();
				textEditor.setBounds(2, 1, Math.min(textSize.x, parentSize.x - 4), parentSize.y - 2);
				textEditorParent.redraw();
			}
		});
		textEditor.addListener(SWT.Traverse, new Listener() {
			public void handleEvent(Event event) {
				//Workaround for Bug 20214 due to extra
				//traverse events
				switch (event.detail) {
					case SWT.TRAVERSE_ESCAPE :
						//Do nothing in this case
						disposeTextWidget();
						event.doit = true;
						event.detail = SWT.TRAVERSE_NONE;
						break;
					case SWT.TRAVERSE_RETURN :
						saveChangesAndDispose(runnable);
						event.doit = true;
						event.detail = SWT.TRAVERSE_NONE;
						break;
				}
			}
		});
		textEditor.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent fe) {
				saveChangesAndDispose(runnable);
			}
		});

		if (textActionHandler != null) {
			textActionHandler.addText(textEditor);
		}
	}

	/**
	 * Closes the text editor widget.
	 */
	void disposeTextWidget() {
		if (textActionHandler != null) {
			textActionHandler.removeText(textEditor);
		}
		if (textEditorParent != null) {
			textEditorParent.dispose();
			textEditorParent = null;
			textEditor = null;
			treeEditor.setEditor(null, null);
		}
	}

	/**
	 * Displays a text editor overlay on the tree widget.
	 * 
	 * @param runnable
	 *            Runnable to execute when editing ends either by the user pressing enter or
	 *            clicking outside the editor box.
	 */
	public void edit(Runnable runnable) {
		IStructuredSelection selection = (IStructuredSelection) commonViewer.getSelection();

		if (selection.size() != 1) {
			return;
		}
		text = getLabel(selection.getFirstElement());
		if (text == null) {
			return;
		}
		// Make sure text editor is created only once. Simply reset text
		// editor when action is executed more than once. Fixes bug 22269.
		if (textEditorParent == null) {
			createTextEditor(runnable);
		}
		textEditor.setText(text);
		// Open text editor with initial size.
		textEditorParent.setVisible(true);
		Point textSize = textEditor.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		textSize.x += textSize.y; // Add extra space for new characters.
		Point parentSize = textEditorParent.getSize();
		textEditor.setBounds(2, 1, Math.min(textSize.x, parentSize.x - 4), parentSize.y - 2);
		textEditorParent.redraw();
		textEditor.selectAll();
		textEditor.setFocus();
	}

	/**
	 * Returns the displayed label of the given element.
	 * 
	 * @param element
	 *            the element that is displayed in the navigator
	 * @return the displayed label of the given element.
	 */
	String getLabel(Object element) {
		return ((ILabelProvider) commonViewer.getLabelProvider()).getText(element);
	}

 
	public String getText() {
		return text;
	}

	/**
	 * Saves the changes and disposes of the text widget.
	 * 
	 * @param runnable
	 *            Runnable to execute
	 */
	void saveChangesAndDispose(final Runnable runnable) {
		final String newText = textEditor.getText();
		// Run this in an async to make sure that the operation that triggered
		// this action is completed. Otherwise this leads to problems when the
		// icon of the item being renamed is clicked (i.e., which causes the rename
		// text widget to lose focus and trigger this method).
		Runnable editRunnable = new Runnable() {
			public void run() {
				disposeTextWidget();
				if (newText.length() > 0 && newText.equals(text) == false) {
					text = newText;
					runnable.run();
				}
				text = null;
			}
		};
		navigatorTree.getShell().getDisplay().asyncExec(editRunnable);
	}

 
	public void setTextActionHandler(TextActionHandler actionHandler) {
		textActionHandler = actionHandler;
	}

}
