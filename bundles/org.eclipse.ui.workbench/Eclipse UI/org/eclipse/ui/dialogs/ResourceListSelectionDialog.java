/************************************************************************
Copyright (c) 2000, 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM - Initial implementation
	Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font 
		should be activated and used by other components.
*************************************************************************/
package org.eclipse.ui.dialogs;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.misc.StringMatcher;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Shows a list of resources to the user with a text entry field
 * for a string pattern used to filter the list of resources.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 2.1
 */
public class ResourceListSelectionDialog extends SelectionDialog {
	Text pattern;
	Table resourceNames;
	Table folderNames;
	String patternString;
	private static Collator collator = Collator.getInstance();
	
	StringMatcher stringMatcher;
	
	UpdateThread updateThread;
	ResourceDescriptor[] descriptors;
	int descriptorsSize;
	
	WorkbenchLabelProvider labelProvider = new WorkbenchLabelProvider();
	static class ResourceDescriptor implements Comparable {
		String label;
		ArrayList resources = new ArrayList(1);
		public int compareTo(Object o) {
			return collator.compare(label,((ResourceDescriptor)o).label);
		}
	}
	
	class UpdateThread extends Thread {
		boolean stop = false;
		int firstMatch = 0;
		int lastMatch = descriptorsSize - 1;
		
		public void run() {
			Display display = resourceNames.getDisplay();
			final int itemIndex[] = {0};
			final int itemCount[] = {0};
			//Keep track of if the widget got disposed 
			//so that we can abort if required
			final boolean[] disposed = {false};
			display.syncExec(new Runnable(){
				public void run() {
					//Be sure the widget still exists
					if(resourceNames.isDisposed()){
						disposed[0] = true;
						return;
					}
			 		itemCount[0] = resourceNames.getItemCount();
				}
			});
			
			if(disposed[0])
				return;
				 
			int last;
			if ((patternString.indexOf('?') == -1) && (patternString.endsWith("*")) && 
				(patternString.indexOf('*') == patternString.length() - 1)) {
				// Use a binary search to get first and last match when the pattern
				// string ends with "*" and has no other embedded special characters.  
				// For this case, we can be smarter about getting the first and last 
				// match since the items are in sorted order.
				firstMatch = getFirstMatch();
				if (firstMatch == -1) {
					firstMatch = 0;
					lastMatch = -1;
				} else {
					lastMatch = getLastMatch();
				}
				last = lastMatch;
				for (int i = firstMatch; i <= lastMatch;i++) {
					if(i % 50 == 0) {
						try { Thread.sleep(10); } catch(InterruptedException e){}
					}
					if(stop || resourceNames.isDisposed()){
						disposed[0] = true;
						 return;
					}
					final int index = i;
					display.syncExec(new Runnable() {
						public void run() {
							if(stop || resourceNames.isDisposed()) return;
							updateItem(index,itemIndex[0],itemCount[0]);
							itemIndex[0]++;
						}
					});
				}
			} else {
				last = lastMatch;
				boolean setFirstMatch = true;
				for (int i = firstMatch; i <= lastMatch;i++) {
					if(i % 50 == 0) {
						try { Thread.sleep(10); } catch(InterruptedException e){}
					}
					if(stop || resourceNames.isDisposed()){
						disposed[0] = true;
						 return;
					}
					final int index = i;
					if(match(descriptors[index].label)) {
						if(setFirstMatch) {
							setFirstMatch = false;
							firstMatch = index;
						}
						last = index;
						display.syncExec(new Runnable() {
							public void run() {
								if(stop || resourceNames.isDisposed()) return;
								updateItem(index,itemIndex[0],itemCount[0]);
								itemIndex[0]++;
							}
						});
					}
				}
			}
			
			if(disposed[0])
				return;
				
			lastMatch = last;
			display.syncExec(new Runnable() {
				public void run() {
					if(resourceNames.isDisposed())
						return;
			 		itemCount[0] = resourceNames.getItemCount();
			 		if(itemIndex[0] < itemCount[0]) {
			 			resourceNames.setRedraw(false);
				 		resourceNames.remove(itemIndex[0],itemCount[0] -1);
			 			resourceNames.setRedraw(true);
			 		}
			 		// If no resources, remove remaining folder entries
			 		if(resourceNames.getItemCount() == 0) {
			 			folderNames.removeAll();
			 		}
				}
			});
		}
	};
/**
 * Creates a new instance of the class.
 * 
 * @param parentShell shell to parent the dialog on
 * @param resources resources to display in the dialog
 */
public ResourceListSelectionDialog(Shell parentShell, IResource[] resources) {
	super(parentShell);
	setShellStyle(getShellStyle() | SWT.RESIZE);
	initDescriptors(resources);
}
/**
 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
 */
protected void cancelPressed() {
	setResult(null);
	super.cancelPressed();
}
/**
 * @see org.eclipse.jface.window.Window#close()
 */
public boolean close() {
	boolean result = super.close();
	labelProvider.dispose();
	return result;
}
/**
 * @see org.eclipse.jface.window.Window#create()
 */
public void create() {
	super.create();
	pattern.setFocus();
}
/**
 * Creates the contents of this dialog, initializes the
 * listener and the update thread.
 * 
 * @param parent parent to create the dialog widgets in
 */
protected Control createDialogArea(Composite parent) {
	Composite dialogArea = (Composite)super.createDialogArea(parent);
	Label l = new Label(dialogArea,SWT.NONE);
	l.setText(WorkbenchMessages.getString("ResourceSelectionDialog.label")); //$NON-NLS-1$
	GridData data = new GridData(GridData.FILL_HORIZONTAL);
	l.setLayoutData(data);
	
	l = new Label(dialogArea,SWT.NONE);
	l.setText(WorkbenchMessages.getString("ResourceSelectionDialog.pattern")); //$NON-NLS-1$
	data = new GridData(GridData.FILL_HORIZONTAL);
	l.setLayoutData(data);
	pattern = new Text(dialogArea,SWT.SINGLE|SWT.BORDER);
	pattern.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	l = new Label(dialogArea,SWT.NONE);
	l.setText(WorkbenchMessages.getString("ResourceSelectionDialog.matching")); //$NON-NLS-1$
	data = new GridData(GridData.FILL_HORIZONTAL);
	l.setLayoutData(data);
	resourceNames = new Table(dialogArea,SWT.SINGLE|SWT.BORDER|SWT.V_SCROLL);
	data = new GridData(GridData.FILL_BOTH);
	data.heightHint = 12 * resourceNames.getItemHeight();
	resourceNames.setLayoutData(data);
	
	l = new Label(dialogArea,SWT.NONE);
	l.setText(WorkbenchMessages.getString("ResourceSelectionDialog.folders")); //$NON-NLS-1$
	data = new GridData(GridData.FILL_HORIZONTAL);
	l.setLayoutData(data);
	
	folderNames = new Table(dialogArea,SWT.SINGLE|SWT.BORDER|SWT.V_SCROLL|SWT.H_SCROLL);
	data = new GridData(GridData.FILL_BOTH);
	data.widthHint = 300;
	data.heightHint = 4 * folderNames.getItemHeight();
	folderNames.setLayoutData(data);
	
	updateThread = new UpdateThread();
	
	pattern.addKeyListener(new KeyAdapter(){
		public void keyPressed(KeyEvent e) {
			if (e.keyCode == SWT.ARROW_DOWN) resourceNames.setFocus();
			else textChanged();
		}
	});
			
	resourceNames.addSelectionListener(new SelectionAdapter(){
		public void widgetSelected(SelectionEvent e) {
			updateFolders((ResourceDescriptor)e.item.getData());
		}
		public void widgetDefaultSelected(SelectionEvent e) {
			okPressed();
		}
	});
	
	folderNames.addSelectionListener(new SelectionAdapter(){
		public void widgetDefaultSelected(SelectionEvent e) {
			okPressed();
		}
	});
	
	return dialogArea;
}
/**
 * Use a binary search to get the first match for the patternString.
 * This method assumes the patternString does not contain any '?' 
 * characters and that it contains only one '*' character at the end
 * of the string.
 */
private int getFirstMatch() {
	int high = descriptorsSize;
	int low = -1;
	boolean match = false;
	ResourceDescriptor desc = new ResourceDescriptor();
	desc.label = patternString.substring(0, patternString.length() - 1);
	while (high - low > 1) {
		int index = (high + low) / 2;
		String label = descriptors[index].label;
		if (match(label)) {
			high = index;
			match = true;
		} else {
			int compare = descriptors[index].compareTo(desc);
			if (compare == -1) {
				low = index;
			} else {
				high = index;
			}
		}
	}
	if (match) return high;
	else return -1;
}
/**
 * Return an image for a resource descriptor.
 * 
 * @param desc resource descriptor to return image for
 * @return an image for a resource descriptor.
 */
private Image getImage(ResourceDescriptor desc) {
	IResource r = (IResource)desc.resources.get(0);
	return labelProvider.getImage(r);
}
/**
 * Use a binary search to get the last match for the patternString.
 * This method assumes the patternString does not contain any '?' 
 * characters and that it contains only one '*' character at the end
 * of the string.
 */
private int getLastMatch() {
	int high = descriptorsSize;
	int low = -1;
	boolean match = false;
	ResourceDescriptor desc = new ResourceDescriptor();
	desc.label = patternString.substring(0, patternString.length() - 1);
	while (high - low > 1) {
		int index = (high + low) / 2;
		String label = descriptors[index].label;
		if (match(label)) {
			low = index;
			match = true;
		} else {
			int compare = descriptors[index].compareTo(desc);
			if (compare == -1) {
				low = index;
			} else {
				high = index;
			}
		}
	}
	if (match) return low;
	else return -1;
}
/**
 * Creates a ResourceDescriptor for each IResource,
 * sorts them and removes the duplicated ones.
 * 
 * @param resources resources to create resource descriptors for
 */
private void initDescriptors(IResource resources[]) {
	descriptors = new ResourceDescriptor[resources.length];
	for (int i = 0; i < resources.length; i++){
		IResource r = resources[i];
		ResourceDescriptor d = new ResourceDescriptor();
		//TDB: Should use the label provider and compare performance.
		d.label = r.getName();
		d.resources.add(r);
		descriptors[i] = d;
	}
	Arrays.sort(descriptors);
	descriptorsSize = descriptors.length;

	//Merge the resource descriptor with the same label and type.
	int index = 0;
	if(descriptorsSize < 2)
		return;
	ResourceDescriptor current = descriptors[index];
	IResource currentResource = (IResource)current.resources.get(0);
	for (int i = 1; i < descriptorsSize; i++){
		ResourceDescriptor next = descriptors[i];
		IResource nextResource = (IResource)next.resources.get(0);
		if((next.label.equals(current.label)) && (nextResource.getType() == currentResource.getType())) {
			current.resources.add(next.resources.get(0));
		} else {
			descriptors[index + 1] = descriptors[i];
			index++;
			current = descriptors[index];
			currentResource = (IResource)current.resources.get(0);
		}
	}
	descriptorsSize = index + 1;
}
/**
 * Returns true if the label matches the chosen pattern.
 * 
 * @param label label to match with the current pattern
 * @return true if the label matches the chosen pattern. 
 * 	false otherwise.
 */
private boolean match(String label) {
	if((patternString == null) || (patternString.equals("")) || (patternString.equals("*")))//$NON-NLS-2$//$NON-NLS-1$
		return true;
	return stringMatcher.match(label);
}
/**
 * The user has selected a resource and the dialog is closing.
 * Set the selected resource as the dialog result.
 */
protected void okPressed() {
	TableItem items[] = folderNames.getSelection();
	if(items.length == 1) {
		ArrayList result = new ArrayList();
		result.add(items[0].getData());
		setResult(result);
	}
	super.okPressed();
}
/**
 * The text in the pattern text entry has changed.
 * Create a new string matcher and start a new update tread.
 */
private void textChanged() {
	String oldPattern = patternString;
	patternString = pattern.getText().trim();
	if (!patternString.equals("")) {
		if(patternString.indexOf('*') == -1 && patternString.indexOf('?') == -1)	//$NON-NLS-1$ //$NON-NLS-2$
			patternString = patternString + "*";	//$NON-NLS-1$
	}
	if(patternString.equals(oldPattern))
		return;
	
	updateThread.stop = true;
	stringMatcher = new StringMatcher(patternString,true,false);
	UpdateThread oldThread = updateThread;
	updateThread = new UpdateThread();
	if (patternString.equals("")) {
		updateThread.firstMatch = 0;
		updateThread.lastMatch = -1;
	} else if(oldPattern == null || 
	  (oldPattern.length() == 0) || 
	  (!patternString.regionMatches(0,oldPattern,0,oldPattern.length())) ||
	  (patternString.endsWith("?")) ||
	  (patternString.endsWith("*"))) {
		updateThread.firstMatch = 0;
		updateThread.lastMatch = descriptorsSize - 1;
	} else {
		updateThread.firstMatch = oldThread.firstMatch;
		updateThread.lastMatch = oldThread.lastMatch;
	}
	updateThread.start();
}
/**
 * A new resource has been selected. Change the contents
 * of the folder names list.
 * 
 * @desc resource descriptor of the selected resource
 */
private void updateFolders(ResourceDescriptor desc) {
	folderNames.removeAll();
	for (int i = 0; i < desc.resources.size(); i++){
		TableItem newItem = new TableItem(folderNames,SWT.NONE);
		IResource r = (IResource) desc.resources.get(i);
		IResource parent = r.getParent();
		String text;
		if (parent.getType() == IResource.ROOT) {
			// XXX: Get readable name for workspace root ("Workspace"), without duplicating language-specific string here.
			text = labelProvider.getText(parent);
		}
		else {
			text = parent.getFullPath().makeRelative().toString();
		}
		newItem.setText(text);
		newItem.setImage(labelProvider.getImage(r.getParent()));
		newItem.setData(r);
	}
	folderNames.setSelection(0);
}
/**
 * Update the specified item with the new info from the resource 
 * descriptor.
 * Create a new table item if there is no item. 
 * 
 * @param index index of the resource descriptor
 * @param itemPos position of the existing item to update
 * @param itemCount number of items in the resources table widget
 */
private void updateItem(int index, int itemPos,int itemCount) {
	ResourceDescriptor desc = descriptors[index];
	TableItem item;
	if(itemPos < itemCount) {
		item = resourceNames.getItem(itemPos);
		if(item.getData() != desc) {
			item.setText(desc.label);
			item.setData(desc);
			item.setImage(getImage(desc));
			if (itemPos == 0) {
				resourceNames.setSelection(0);
				updateFolders(desc);
			}
		}
	} else {
		item = new TableItem(resourceNames, SWT.NONE);
		item.setText(desc.label);
		item.setData(desc);
		item.setImage(getImage(desc));
		if (itemPos == 0) {
			resourceNames.setSelection(0);
			updateFolders(desc);
		}
	}
}
}
