package org.eclipse.ui.internal.dialogs;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.swt.layout.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.preference.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.UIHackFinder;

/*
 * A page used as a filler for nodes in the preference tree
 * for which no page is suppplied.
 */
public class EmptyPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
protected Control createContents(Composite parent) {
	return new Composite(parent, SWT.NULL);
}
/**
 * Hook method to get a page specific preference store. Reimplement this
 * method if a page don't want to use its parent's preference store.
 */
protected IPreferenceStore doGetPreferenceStore() {
	return WorkbenchPlugin.getDefault().getPreferenceStore();
}
/**
 * @see IWorkbenchPreferencePage
 */
public void init(IWorkbench workbench){
}
}
