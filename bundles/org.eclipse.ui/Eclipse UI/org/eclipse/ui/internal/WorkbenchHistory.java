package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.*;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.internal.dialogs.PerspLabelProvider;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import java.util.*;

/**
 * This class is used to record "open editor" and "open page" actions as they
 * happen.  The input and type of each edior and page are recorded so that
 * the user can reopen an item from the history list. 
 */
public class WorkbenchHistory {
	private int size;
	private ArrayList stack;
	private WorkbenchLabelProvider workbenchLabelProvider = 
		new WorkbenchLabelProvider();
	private PerspLabelProvider perspLabelProvider =
		new PerspLabelProvider();

	
	public abstract class WorkbenchHistoryItem {
		IAdaptable input;
		Object desc;
		public WorkbenchHistoryItem(IAdaptable input, Object desc) {
			this.input = input;
			this.desc = desc;
		}
		public abstract String getLabel();
		public abstract Image getImage();
		public abstract void open(IWorkbenchPage page);
	}
	public class EditorHistoryItem extends WorkbenchHistoryItem {
		public EditorHistoryItem(IEditorInput input, IEditorDescriptor desc) {
			super(input,desc);
		}
		public String getLabel() {
			return ((IEditorInput)input).getToolTipText();
		}
		public Image getImage() {
			return workbenchLabelProvider.getImage(input);
		}
		public void open(IWorkbenchPage page) {
			if (page != null) {
				try {
					// Descriptor is null if opened on OLE editor.  .
					if (desc == null) {
						// There's no openEditor(IEditorInput) call, and openEditor(IEditorInput, String)
						// doesn't allow null id.
						// However, if id is null, the editor input must be an IFileEditorInput,
						// so we can use openEditor(IFile).  
						// Do nothing if for some reason input was not an IFileEditorInput.
						if (input instanceof IFileEditorInput)
							page.openEditor(((IFileEditorInput) input).getFile());
					} else {
						page.openEditor((IEditorInput)input, ((IEditorDescriptor)desc).getId());
					}
				} catch (PartInitException e2) {
				}
			}
		}
	}
	public class PerspectiveHistoryItem extends WorkbenchHistoryItem {
		public PerspectiveHistoryItem(IAdaptable input, IPerspectiveDescriptor desc) {
			super(input,desc);
		}
		public String getLabel() {
			return workbenchLabelProvider.getText(input) + " :: " + perspLabelProvider.getText(desc);
			
		}
		public Image getImage() {
			return perspLabelProvider.getImage(desc);
		}
		public void open(IWorkbenchPage page) {
			try {
				IWorkbench wb = page.getWorkbenchWindow().getWorkbench();
				wb.openPage(((IPerspectiveDescriptor)desc).getId(),input);
			} catch (WorkbenchException e) {
				MessageDialog.openError(
					page.getWorkbenchWindow().getShell(),
					WorkbenchMessages.getString("WorkbenchHistory.dialogTitle"), //$NON-NLS-1$
					e.getMessage());
			}
		}
	}

/**
 * Constructs a new history.
 */
public WorkbenchHistory(int size) {
	this.size = size;
	stack = new ArrayList(size);
}
	
/**
 * Adds an item to the history.
 */
public void add(IEditorInput input, IEditorDescriptor desc) {
	add(new EditorHistoryItem(input, desc));
}
/**
 * Adds an item to the history.
 */
public void add(IAdaptable input, IPerspectiveDescriptor desc) {
	add(new PerspectiveHistoryItem(input, desc));
}

private void add(WorkbenchHistoryItem item) {
	// Remove old item.
	remove(item.input);
	// Add the new item.
	stack.add(item);
	while (stack.size() > size) {
		stack.remove(0);
	}
}


/**
 * Returns an array of editor history items.  The items are returned in order
 * of most recent first.
 */
public WorkbenchHistoryItem [] getItems() {
	refresh();
	WorkbenchHistoryItem[] array = new WorkbenchHistoryItem[stack.size()];
	int length = array.length;
	for (int nX = 0; nX < length; nX ++) {
		array[nX] = (WorkbenchHistoryItem)stack.get(length - 1 - nX);
	}
	return array;
}
/**
 * Returns the stack height.
 */
public int getSize() {
	return stack.size();
}
/**
 * Refresh the editor list.  Any stale items are removed.
 */
public void refresh() {
//	Iterator iter = stack.iterator();
//	while (iter.hasNext()) {
//		EditorHistoryItem item = (EditorHistoryItem)iter.next();
//		if (!item.input.exists())
//			iter.remove();
//	}
}
/**
 * Removes all traces of an editor input from the history.
 */
public void remove(IAdaptable input) {
	Iterator iter = stack.iterator();
	while (iter.hasNext()) {
		WorkbenchHistoryItem item = (WorkbenchHistoryItem)iter.next();
		if (input.equals(item.input))
			iter.remove();
	}
}

public void dispose() {
	perspLabelProvider.dispose();
	workbenchLabelProvider.dispose();
}
}

