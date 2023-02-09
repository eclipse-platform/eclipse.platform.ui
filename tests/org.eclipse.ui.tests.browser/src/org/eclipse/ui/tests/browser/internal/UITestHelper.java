/*******************************************************************************
 * Copyright (c) 2004, 2023 IBM Corporation and others.
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
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 489250
 *******************************************************************************/
package org.eclipse.ui.tests.browser.internal;

import static org.junit.Assert.fail;

import java.text.MessageFormat;
import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.PropertyDialog;
import org.eclipse.ui.internal.dialogs.PropertyPageContributorManager;
import org.eclipse.ui.internal.dialogs.PropertyPageManager;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.junit.Assert;

public class UITestHelper {
	private static class PreferenceDialogWrapper extends PreferenceDialog {
		public PreferenceDialogWrapper(Shell parentShell, PreferenceManager manager) {
			super(parentShell, manager);
		}
		@Override
		protected boolean showPage(IPreferenceNode node) {
			return super.showPage(node);
		}
	}

	private static class PropertyDialogWrapper extends PropertyDialog {
		public PropertyDialogWrapper(Shell parentShell, PreferenceManager manager, ISelection selection) {
			super(parentShell, manager, selection);
		}

		@Override
		protected boolean showPage(IPreferenceNode node) {
			return super.showPage(node);
		}
	}

	protected static Shell getShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}

	public static PreferenceDialog getPreferenceDialog(String id) {
		PreferenceDialogWrapper dialog = null;
		PreferenceManager manager = WorkbenchPlugin.getDefault().getPreferenceManager();
		if (manager != null) {
			dialog = new PreferenceDialogWrapper(getShell(), manager);
			dialog.create();

			for (IPreferenceNode node : manager.getElements(PreferenceManager.PRE_ORDER)) {
				if ( node.getId().equals(id) ) {
					dialog.showPage(node);
					break;
				}
			}
		}
		return dialog;
	}

	public static PropertyDialog getPropertyDialog(String id, IAdaptable element) {
		PropertyDialogWrapper dialog = null;

		PropertyPageManager manager = new PropertyPageManager();
		String title = "";
		String name  = "";

		// load pages for the selection
		// fill the manager with contributions from the matching contributors
		PropertyPageContributorManager.getManager().contribute(manager, element);

		IWorkbenchAdapter adapter = element.getAdapter(IWorkbenchAdapter.class);
		if (adapter != null) {
			name = adapter.getLabel(element);
		}

		// testing if there are pages in the manager
		Iterator<IPreferenceNode> pages = manager.getElements(PreferenceManager.PRE_ORDER).iterator();
		if (!pages.hasNext())
			return null;

		title = MessageFormat.format("PropertyDialog.propertyMessage", name);
		dialog = new PropertyDialogWrapper(getShell(), manager, new StructuredSelection(element));
		dialog.create();
		dialog.getShell().setText(title);
		for (IPreferenceNode node : manager.getElements(PreferenceManager.PRE_ORDER)) {
			if (node.getId().equals(id)) {
				dialog.showPage(node);
				break;
			}
		}
		return dialog;
	}

	/**
	 * Automated test that checks all the labels and buttons of a dialog
	 * to make sure there is enough room to display all the text.  Any
	 * text that wraps is only approximated and is currently not accurate.
	 *
	 * @param dialog the test dialog to be verified.
	 */
	public static void assertDialog(Dialog dialog) {
		Assert.assertNotNull(dialog);
		dialog.setBlockOnOpen(false);
		dialog.open();
		Shell shell = dialog.getShell();
		verifyCompositeText(shell);
		dialog.close();
	}

	/*
	 * Looks at all the child widgets of a given composite and
	 * verifies the text on all labels and widgets.
	 * @param composite The composite to look through
	 */
	private static void verifyCompositeText(Composite composite) {
		Control children[] = composite.getChildren();
		for (Control element : children) {
			try {
				//verify the text if the child is a button
				verifyButtonText((Button) element);
			} catch (ClassCastException exNotButton) {
				try {
					//child is not a button, maybe a label
					verifyLabelText((Label) element);
				} catch (ClassCastException exNotLabel) {
					try {
						//child is not a label, make a recursive call if it is a composite
						verifyCompositeText((Composite) element);
					} catch (ClassCastException exNotComposite) {
						//the child is not a button, label, or composite - ignore it.
					}
				}
			}
		}
	}

	/*
	 * Verifies that a given button is large enough to display its text.
	 * @param button The button to verify,
	 */
	private static void verifyButtonText(Button button) {
		String widget = button.toString();
		Point size = button.getSize();

		// compute the size with no line wrapping
		Point preferred = button.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		//if (size.y/preferred.y) == X, then label spans X lines, so divide
		//the calculated value of preferred.x by X
		if (preferred.y * size.y > 0) {
			preferred.y /= countLines(button.getText()); //check for '\n\'
			if (size.y / preferred.y > 1) {
				preferred.x /= (size.y / preferred.y);
			}
		}

		String message =
			new StringBuilder("Warning: ")
				.append(widget)
				.append("\n\tActual Width -> ")
				.append(size.x)
				.append("\n\tRecommended Width -> ")
				.append(preferred.x)
				.toString();
		if (preferred.x > size.x) {
			//close the dialog
			button.getShell().dispose();
			fail(message);
		}
	}

	/*
	 * Verifies that a given label is large enough to display its text.
	 * @param label The label to verify,
	 */
	private static void verifyLabelText(Label label) {
		String widget = label.toString();
		Point size = label.getSize();

		// compute the size with no line wrapping
		Point preferred = label.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		//if (size.y/preferred.y) == X, then label spans X lines, so divide
		//the calculated value of preferred.x by X
		if (preferred.y * size.y > 0) {
			preferred.y /= countLines(label.getText());
			if (size.y / preferred.y > 1) {
				preferred.x /= (size.y / preferred.y);
			}
		}
		String message = new StringBuilder("Warning: ").append(widget)
			.append("\n\tActual Width -> ").append(size.x)
			.append("\n\tRecommended Width -> ").append(preferred.x).toString();
		if (preferred.x > size.x) {
			//close the dialog
			label.getShell().dispose();
			fail(message);
		}
	}

	/*
	 * Counts the number of lines in a given String.
	 * For example, if a string contains one (1) newline character,
	 * a value of two (2) would be returned.
	 * @param text The string to look through.
	 * @return int the number of lines in text.
	 */
	private static int countLines(String text) {
		int newLines = 1;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '\n') {
				newLines++;
			}
		}
		return newLines;
	}
}