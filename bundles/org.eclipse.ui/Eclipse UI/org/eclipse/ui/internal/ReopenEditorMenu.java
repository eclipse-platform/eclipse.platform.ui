package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.ui.*;

/**
 * A dynamic menu item which supports to switch to other Windows.
 */
public class ReopenEditorMenu extends ContributionItem {
	private WorkbenchWindow fWindow;
	private EditorHistory history;
	private boolean showSeparator;

	private static final int MAX_TEXT_LENGTH = 50;
	// only assign mnemonic to the first nine items 
	private static final int MAX_MNEMONIC_SIZE = 9;
	/**
	 * Create a new instance.
	 */
	public ReopenEditorMenu(WorkbenchWindow window, EditorHistory history, boolean showSeparator) {
		super("Reopen Editor"); //$NON-NLS-1$
		fWindow = window;
		this.history = history;
		this.showSeparator = showSeparator;
	}
	/**
	 * Returns the text for a history item.  This may be truncated to fit
	 * within the MAX_TEXT_LENGTH.
	 */
	private String calcText(int index, EditorHistoryItem item) {
		StringBuffer sb = new StringBuffer();

		int mnemonic = index + 1;
		sb.append(mnemonic);
		if (mnemonic <= MAX_MNEMONIC_SIZE) {
			sb.insert(sb.length() - (mnemonic + "").length(), '&'); //$NON-NLS-1$
		}
		sb.append(" "); //$NON-NLS-1$

		// If the input text is a path, get the filename from the
		// path's last segment; otherwise, use the input name.
		String fileName;
		String pathName = item.getInput().getToolTipText();
		IPath path = new Path(pathName);
		if (path.segmentCount() > 1) {
			fileName = path.lastSegment();
			path = path.removeLastSegments(1);
			pathName = path.toString();
		} else {
			fileName = item.getInput().getName();
		}
		
		if ((fileName.length() + pathName.length()) <= MAX_TEXT_LENGTH) {
			// entire item name fits within maximum length
			sb.append(fileName);
			if (path.segmentCount() != 0) {
				sb.append("  ["); //$NON-NLS-1$
				sb.append(pathName);
				sb.append("]"); //$NON-NLS-1$
			}
		} else {
			// need to shorten the item name
			int length = fileName.length();
			if (length > MAX_TEXT_LENGTH) {
				// file name does not fit within length, truncate it
				sb.append(fileName.substring(0, MAX_TEXT_LENGTH));
				sb.append("..."); //$NON-NLS-1$
			} else {				
				sb.append(fileName);
				int segmentCount = path.segmentCount();
				if (segmentCount > 0) {
					sb.append("  ["); //$NON-NLS-1$
				}
				// Add first n segments that fit
				int i = 0;
				while (i < segmentCount) {
					String segment = path.segment(i);
					length += segment.length();
					if (length < MAX_TEXT_LENGTH) {
						sb.append(segment);
						sb.append(path.SEPARATOR);
						i++;
					} else if (i == 0) {
						// append at least part of the first segment
						sb.append(segment.substring(0, MAX_TEXT_LENGTH - (length - segment.length())));
						i = segmentCount;
						length = MAX_TEXT_LENGTH;
					} else {
						i = segmentCount;
						length -= segment.length();
					}
				}
				if (segmentCount > 0) {
					sb.append("...");	 //$NON-NLS-1$
				}			
				i = segmentCount - 1;
				if (i > 0) {
					// Add last n segments that fit
					while (i > 0) {			
						String segment = path.segment(i);
						length += segment.length();
						if (length < MAX_TEXT_LENGTH) {
							sb.append(path.SEPARATOR);
							sb.append(segment);
							i--;
						} else {
							i = 0;
						}
					}
				}
				if (segmentCount > 0) {
					sb.append("]"); //$NON-NLS-1$
				}
			}
		}
		return sb.toString();
	}
	/**
	 * Fills the given menu with
	 * menu items for all windows.
	 */
	public void fill(Menu menu, int index) {
		if (fWindow.getActivePage() == null
			|| fWindow.getActivePage().getPerspective() == null)
			return;

		// Get items.
		EditorHistoryItem[] array = history.getItems();

		// If no items return.
		if (array.length <= 0) {
			return;
		}

		// Add separator.
		if (showSeparator) {
			new MenuItem(menu, SWT.SEPARATOR, index);
			++index;
		}

		// Add one item for each item.
		for (int i = 0; i < array.length; i++) {
			final EditorHistoryItem item = array[i];
			MenuItem mi = new MenuItem(menu, SWT.PUSH, index);
			++index;
			mi.setText(calcText(i, item));
			mi.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					open(item);
				}
			});
		}
	}
	/**
	 * Overridden to always return true and force dynamic menu building.
	 */
	public boolean isDynamic() {
		return true;
	}
	/**
	 * Reopens the editor for the given history item.
	 */
	void open(EditorHistoryItem item) {
		IWorkbenchPage page = fWindow.getActivePage();
		if (page != null) {
			try {
				// Fix for 1GF6HQ1: ITPUI:WIN2000 - NullPointerException: opening a .ppt file
				// Descriptor is null if opened on OLE editor.  .
				IEditorInput input = item.getInput();
				IEditorDescriptor desc = item.getDescriptor();
				if (desc == null) {
					// There's no openEditor(IEditorInput) call, and openEditor(IEditorInput, String)
					// doesn't allow null id.
					// However, if id is null, the editor input must be an IFileEditorInput,
					// so we can use openEditor(IFile).  
					// Do nothing if for some reason input was not an IFileEditorInput.
					if (input instanceof IFileEditorInput) {
						page.openEditor(((IFileEditorInput) input).getFile());
					}
				} else {
					page.openEditor(input, desc.getId());
				}
			} catch (PartInitException e2) {
				String title = WorkbenchMessages.getString("OpenRecent.errorTitle"); //$NON-NLS-1$
				MessageDialog.openWarning(fWindow.getShell(), title, e2.getMessage());
				history.remove(item.getInput());
			}
		}
	}
}