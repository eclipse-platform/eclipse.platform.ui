package org.eclipse.ui.views.navigator;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.core.resources.*;
import org.eclipse.ui.dialogs.*;
import java.util.*;
import org.eclipse.ui.model.*;
import org.eclipse.ui.help.*;

/**
 * Shows a list of resources to the user with a text entry field
 * for a string pattern used to filter the list of resources.
 *
 */
/*package*/ class GotoResourceDialog extends SelectionDialog {
	Text pattern;
	Table resourceNames;
	Table folderNames;

	String patternString;
	IResource selection;
	
	StringMatcher stringMatcher;
	
	UpdateThread updateThread;
	ResourceDescriptor descriptors[];
	int descriptorsSize;

	WorkbenchLabelProvider labelProvider = new WorkbenchLabelProvider();

	static class ResourceDescriptor implements Comparable {
		String label;
		ArrayList resources = new ArrayList(1);
		public int compareTo(Object o) {
			return label.compareToIgnoreCase(((ResourceDescriptor)o).label);
		}
	}
	
	class UpdateThread extends Thread {
		boolean stop = false;
		public void run() {
			Display display = resourceNames.getDisplay();
			final boolean first[] = {true};
			for (int i = 0; i < descriptorsSize;i++) {
				if(i % 50 == 0) {
					try { Thread.sleep(10); } catch(InterruptedException e){}
				}
				if(stop || resourceNames.isDisposed()) return;
				final int index = i;
				if(match(descriptors[index].label)) {
					display.syncExec(new Runnable(){
						public void run() {
							if(stop || resourceNames.isDisposed()) return;
							createItem(index,first[0]);
							first[0]=false;
						}
					});
				}
			}
		}
	};
/**
 * Creates a new instance of the class.
 */
protected GotoResourceDialog(Shell parentShell,IResource resources[]) {
	super(parentShell);
	setTitle(ResourceNavigatorMessages.getString("Goto.title")); //$NON-NLS-1$
	setShellStyle(getShellStyle() | SWT.RESIZE);

	initDescriptors(resources);
}
public boolean close() {
	boolean r = super.close();
	labelProvider.dispose();
	return r;
}
/* (non-Javadoc)
 * Method declared in Window.
 */
protected void configureShell(Shell shell) {
	super.configureShell(shell);
	WorkbenchHelp.setHelp(shell, new Object[] {INavigatorHelpContextIds.GOTO_RESOURCE_DIALOG});
}
public void create() {
	super.create();
	pattern.setFocus();
}
/**
 * Creates the contents of this dialog, initializes the
 * listener and the update thread.
 */
protected Control createDialogArea(Composite parent) {
	
	Composite dialogArea = (Composite)super.createDialogArea(parent);

	Label l = new Label(dialogArea,SWT.NONE);
	l.setText(ResourceNavigatorMessages.getString("Goto.label")); //$NON-NLS-1$
	GridData data = new GridData(GridData.FILL_HORIZONTAL);
	l.setLayoutData(data);
	
	l = new Label(dialogArea,SWT.NONE);
	l.setText(ResourceNavigatorMessages.getString("Goto.pattern")); //$NON-NLS-1$
	data = new GridData(GridData.FILL_HORIZONTAL);
	l.setLayoutData(data);

	pattern = new Text(dialogArea,SWT.SINGLE|SWT.BORDER);
	pattern.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	l = new Label(dialogArea,SWT.NONE);
	l.setText(ResourceNavigatorMessages.getString("Goto.matching")); //$NON-NLS-1$
	data = new GridData(GridData.FILL_HORIZONTAL);
	l.setLayoutData(data);

	resourceNames = new Table(dialogArea,SWT.SINGLE|SWT.BORDER|SWT.V_SCROLL);
	data = new GridData(GridData.FILL_BOTH);
	data.heightHint = 6 * resourceNames.getItemHeight();
	resourceNames.setLayoutData(data);
	
	l = new Label(dialogArea,SWT.NONE);
	l.setText(ResourceNavigatorMessages.getString("Goto.folders")); //$NON-NLS-1$
	data = new GridData(GridData.FILL_HORIZONTAL);
	l.setLayoutData(data);
	
	folderNames = new Table(dialogArea,SWT.SINGLE|SWT.BORDER|SWT.V_SCROLL|SWT.H_SCROLL);
	data = new GridData(GridData.FILL_BOTH);
	data.widthHint = 250;
	data.heightHint = 3 * folderNames.getItemHeight();
	folderNames.setLayoutData(data);

	startNewThread();
	
	pattern.addKeyListener(new KeyAdapter(){
		public void keyPressed(KeyEvent e) {
			textChanged();
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
 * Create a new table item.
 */
private void createItem(int index, boolean setSelection) {
	TableItem item = new TableItem(resourceNames, SWT.NONE);
	ResourceDescriptor desc = descriptors[index];
	item.setText(desc.label);
	item.setData(desc);
	item.setImage(getImage(desc));
	if (setSelection) {
		resourceNames.setSelection(0);
		updateFolders(desc);
	}
}
/**
 * Return a image for a resource.
 */
private Image getImage(ResourceDescriptor desc) {
	IResource r = (IResource)desc.resources.get(0);
	return labelProvider.getImage(r);
}
/**
 * Returns the IResource selected by the user or null if the
 * cancel was pressed.
 */
public IResource getSelection() {
	return selection;
}
/**
 * Creates a ResourceDescriptor for each IResource
 * sort them and remove the duplicated ones.
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
 * Return true if the label matchs the choosen pattern.
 */
private boolean match(String label) {
	if((patternString == null) || (patternString.equals("")) || (patternString.equals("*")))//$NON-NLS-2$//$NON-NLS-1$
		return true;
	return stringMatcher.match(label);
}
/**
 * The user has selected a resource and the dialog is closing.
 */
protected void okPressed() {
	TableItem items[] = folderNames.getSelection();
	if(items.length != 1)
		selection = null;
	else
		selection = (IResource)items[0].getData();
	super.okPressed();
}
/**
 * Start a new update thread.
 */
private void startNewThread() {
	updateThread = new UpdateThread();
	updateThread.start();
}
/**
 * The text in the pattern text entry has changed.
 * Create a new string matcher and start a new
 * update tread.
 */
private void textChanged() {
	String oldPattern = patternString;
	patternString = pattern.getText();
	if(!patternString.endsWith("*"))//$NON-NLS-1$
		patternString = patternString + "*";//$NON-NLS-1$
	if(patternString.equals(oldPattern))
		return;
	updateThread.stop = true;
	stringMatcher = new StringMatcher(patternString,true,false);
	resourceNames.removeAll();
	startNewThread();
}
/**
 * A new resource has been selected. Change the contents
 * of the folder names list.
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
}
