/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.internal;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.util.Arrays;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

/**
 * A dynamic menu item which supports to switch to other Windows.
 */
public class ReopenEditorMenu extends ContributionItem {
	private IWorkbenchWindow window;

	private EditorHistory history;

	private boolean showSeparator;

	// the maximum length for a file name; must be >= 4
	private static final int MAX_TEXT_LENGTH = 40;

	// only assign mnemonic to the first nine items
	private static final int MAX_MNEMONIC_SIZE = 9;

	/**
	 * Create a new instance.
	 *
	 * @param window        the window on which the menu is to be created
	 * @param id            menu's id
	 * @param showSeparator whether or not to show a separator
	 */
	public ReopenEditorMenu(IWorkbenchWindow window, String id, boolean showSeparator) {
		super(id);
		this.window = window;
		this.showSeparator = showSeparator;
		IWorkbench workbench = window.getWorkbench();
		if (workbench instanceof Workbench) {
			history = ((Workbench) workbench).getEditorHistory();
		}
	}

	/**
	 * Returns the text for a history item. This may be truncated to fit within the
	 * MAX_TEXT_LENGTH.
	 */
	private String calcText(int index, EditorHistoryItem item) {
		// IMPORTANT: avoid accessing the item's input since
		// this can require activating plugins.
		// Instead, ask the item for the info, which can
		// consult its memento if it is not restored yet.
		return calcText(index, item.getName(), item.getToolTipText(),
				Window.getDefaultOrientation() == SWT.RIGHT_TO_LEFT);
	}

	/**
	 * Return a string suitable for a file MRU list. This should not be called
	 * outside the framework.
	 *
	 * @param index   the index in the MRU list
	 * @param name    the file name
	 * @param toolTip potentially the path
	 * @param rtl     should it be right-to-left
	 * @return a string suitable for an MRU file menu
	 */
	public static String calcText(int index, String name, String toolTip, boolean rtl) {
		StringBuilder sb = new StringBuilder();

		int mnemonic = index + 1;
		StringBuilder nm = new StringBuilder();
		nm.append(mnemonic);
		if (mnemonic <= MAX_MNEMONIC_SIZE) {
			nm.insert(nm.length() - (Integer.toString(mnemonic)).length(), '&');
		}
//        sb.append(" "); //$NON-NLS-1$

		String fileName = name;
		String pathName = toolTip;
		if (pathName.equals(fileName)) {
			// tool tip text isn't necessarily a path;
			// sometimes it's the same as name, so it shouldn't be treated as a path then
			pathName = ""; //$NON-NLS-1$
		}
		IPath path = IPath.fromOSString(pathName);
		// if last segment in path is the fileName, remove it
		if (path.segmentCount() > 1 && path.segment(path.segmentCount() - 1).equals(fileName)) {
			path = path.removeLastSegments(1);
			pathName = path.toString();
		}

		if ((fileName.length() + pathName.length()) <= (MAX_TEXT_LENGTH - 4)) {
			// entire item name fits within maximum length
			sb.append(fileName);
			if (pathName.length() > 0) {
				sb.append("  ["); //$NON-NLS-1$
				sb.append(pathName);
				sb.append("]"); //$NON-NLS-1$
			}
		} else {
			// need to shorten the item name
			int length = fileName.length();
			if (length > MAX_TEXT_LENGTH) {
				// file name does not fit within length, truncate it
				sb.append(fileName.substring(0, MAX_TEXT_LENGTH - 3));
				sb.append("..."); //$NON-NLS-1$
			} else if (length > MAX_TEXT_LENGTH - 7) {
				sb.append(fileName);
			} else {
				sb.append(fileName);
				int segmentCount = path.segmentCount();
				if (segmentCount > 0) {
					length += 7; // 7 chars are taken for " [...]"

					sb.append("  ["); //$NON-NLS-1$

					// Add first n segments that fit
					int i = 0;
					while (i < segmentCount && length < MAX_TEXT_LENGTH) {
						String segment = path.segment(i);
						if (length + segment.length() < MAX_TEXT_LENGTH) {
							sb.append(segment);
							sb.append(IPath.SEPARATOR);
							length += segment.length() + 1;
							i++;
						} else if (i == 0) {
							// append at least part of the first segment
							sb.append(segment.substring(0, MAX_TEXT_LENGTH - length));
							length = MAX_TEXT_LENGTH;
							break;
						} else {
							break;
						}
					}

					sb.append("..."); //$NON-NLS-1$

					i = segmentCount - 1;
					// Add last n segments that fit
					while (i > 0 && length < MAX_TEXT_LENGTH) {
						String segment = path.segment(i);
						if (length + segment.length() < MAX_TEXT_LENGTH) {
							sb.append(IPath.SEPARATOR);
							sb.append(segment);
							length += segment.length() + 1;
							i--;
						} else {
							break;
						}
					}

					sb.append("]"); //$NON-NLS-1$
				}
			}
		}
		final String process;
		if (rtl) {
			process = sb + " " + nm; //$NON-NLS-1$
		} else {
			process = nm + " " + sb; //$NON-NLS-1$
		}
		return TextProcessor.process(process, TextProcessor.getDefaultDelimiters() + "[]");//$NON-NLS-1$
	}

	/**
	 * Fills the given menu with menu items for all windows.
	 */
	@Override
	public void fill(final Menu menu, int index) {
		if (window.getActivePage() == null || window.getActivePage().getPerspective() == null) {
			return;
		}

		int itemsToShow = WorkbenchPlugin.getDefault().getPreferenceStore().getInt(IPreferenceConstants.RECENT_FILES);
		if (itemsToShow == 0 || history == null) {
			return;
		}

		// Get items.
		EditorHistoryItem[] historyItems = history.getItems();

		int n = Math.min(itemsToShow, historyItems.length);
		if (n <= 0) {
			addClearHistory(menu, n);
			return;
		}

		if (showSeparator) {
			new MenuItem(menu, SWT.SEPARATOR, index);
			++index;
		}

		final int menuIndex[] = new int[] { index };

		for (int i = 0; i < n; i++) {
			final EditorHistoryItem item = historyItems[i];
			final int historyIndex = i;
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					String text = calcText(historyIndex, item);
					MenuItem mi = new MenuItem(menu, SWT.PUSH, menuIndex[0]);
					++menuIndex[0];
					mi.setText(text);
					mi.addSelectionListener(widgetSelectedAdapter(e -> open(item)));
				}

				@Override
				public void handleException(Throwable e) {
					// just skip the item if there's an error,
					// e.g. in the calculation of the shortened name
					WorkbenchPlugin.log(getClass(), "fill", e); //$NON-NLS-1$
				}
			});
		}

		addClearHistory(menu, n);
	}

	private void addClearHistory(Menu menu, int nItems) {
		new MenuItem(menu, SWT.SEPARATOR);
		MenuItem miClear = new MenuItem(menu, SWT.PUSH);
		miClear.setText(WorkbenchMessages.OpenRecentDocumentsClear_text);
		miClear.addSelectionListener(widgetSelectedAdapter(e -> Arrays.stream(history.getItems()).forEach(history::remove)));
		miClear.setEnabled(nItems > 0);
	}

	/**
	 * Overridden to always return true and force dynamic menu building.
	 */
	@Override
	public boolean isDynamic() {
		return true;
	}

	/**
	 * Reopens the editor for the given history item.
	 */
	private void open(EditorHistoryItem item) {
		IWorkbenchPage page = window.getActivePage();
		if (page != null) {
			try {
				String itemName = item.getName();
				if (!item.isRestored()) {
					item.restoreState();
				}
				IEditorInput input = item.getInput();
				IEditorDescriptor desc = item.getDescriptor();
				if (input == null || !input.exists() || desc == null) {
					String title = WorkbenchMessages.OpenRecent_errorTitle;
					String msg = NLS.bind(WorkbenchMessages.OpenRecent_unableToOpen, itemName);
					MessageDialog.openWarning(window.getShell(), title, msg);
					history.remove(item);
				} else {
					page.openEditor(input, desc.getId());
				}
			} catch (PartInitException e2) {
				String title = WorkbenchMessages.OpenRecent_errorTitle;
				MessageDialog.openWarning(window.getShell(), title, e2.getMessage());
				history.remove(item);
			}
		}
	}

	@Override
	public void dispose() {
		window = null;
		super.dispose();
	}

}
