package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;

/**
 * A dynamic menu item which supports to switch to other Windows.
 */
public class ReopenEditorMenu extends ContributionItem {
	private WorkbenchWindow fWindow;
	private EditorHistory history;
	private boolean showSeparator;
	private boolean dirty = true;
	private IMenuListener menuListener = new IMenuListener() {
		public void menuAboutToShow(IMenuManager manager) {
			manager.markDirty();
			dirty = true;
		}
	};

	// the maximum length for a file name; must be >= 4
	private static final int MAX_TEXT_LENGTH = 40;
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

		// IMPORTANT: avoid accessing the item's input since
		// this can require activating plugins.
		// Instead, ask the item for the info, which can
		// consult its memento if it is not restored yet.
		String fileName = item.getName();
		String pathName = item.getToolTipText();
		if (pathName.equals(fileName)) {
			// tool tip text isn't necessarily a path;
			// sometimes it's the same as name, so it shouldn't be treated as a path then
			pathName = ""; //$NON-NLS-1$
		}
		IPath path = new Path(pathName);
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
					length += 7;  // 7 chars are taken for "  [...]"

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

					sb.append("...");	 //$NON-NLS-1$

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
		return sb.toString();
	}
	/**
	 * Fills the given menu with
	 * menu items for all windows.
	 */
	public void fill(final Menu menu, int index) {
		if (fWindow.getActivePage() == null
			|| fWindow.getActivePage().getPerspective() == null)
			return;

		if(getParent() instanceof MenuManager)
			((MenuManager)getParent()).addMenuListener(menuListener);
				
		if(!dirty)
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

		final int menuIndex[] = new int[]{index};
		// Add one item for each item.
		for (int i = 0; i < array.length; i++) {
			final EditorHistoryItem item = array[i];
			final int historyIndex = i;
			Platform.run(new SafeRunnable() {
				public void run() throws Exception {
					String text = calcText(historyIndex, item);
					MenuItem mi = new MenuItem(menu, SWT.PUSH, menuIndex[0]);
					++menuIndex[0];
					mi.setText(text);
					mi.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							open(item);
						}
					});
				}
				public void handleException(Throwable e) {
					// just skip the item if there's an error,
					// e.g. in the calculation of the shortened name
					WorkbenchPlugin.log("Error in ReopenEditorMenu.fill: " + e);  // $NON-NLS-1 //$NON-NLS-1$
				}
			});
		}
		dirty = false;
	}
	/**
	 * Overridden to always return true and force dynamic menu building.
	 */
	public boolean isDirty() {
		return dirty;
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
				// Descriptor is null if opened on OLE editor.
				String itemName = item.getName();
				if (!item.isRestored()) {
					item.restoreState();
				}
				IEditorInput input = item.getInput();
				IEditorDescriptor desc = item.getDescriptor();
				if (input == null) {
					String title = WorkbenchMessages.getString("OpenRecent.errorTitle"); //$NON-NLS-1$
					String msg = WorkbenchMessages.format("OpenRecent.unableToOpen",new String[]{itemName}); //$NON-NLS-1$
					MessageDialog.openWarning(fWindow.getShell(), title, msg);
					history.remove(item);
				} else {
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
				}
			} catch (PartInitException e2) {
				String title = WorkbenchMessages.getString("OpenRecent.errorTitle"); //$NON-NLS-1$
				MessageDialog.openWarning(fWindow.getShell(), title, e2.getMessage());
				history.remove(item);
			}
		}
	}
}