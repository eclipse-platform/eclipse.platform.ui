package org.eclipse.ant.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for the tree viewer of the Ant Console.
 */

public class AntTreeLabelProvider implements ILabelProvider {
	
	private AntConsole console;
	
public AntTreeLabelProvider(AntConsole console) {
	this.console = console;
}


/**
 * @see ILabelProvider#getImage(Object)
 */
public Image getImage(Object element) {
	return null;
}

/**
 * @see ILabelProvider#getText(Object)
 */
public String getText(Object element) {
	String text = ((OutputStructureElement) element).getName();
	if ( text == null)
		// this can happen if the userwrites a task name that doesn't exist => the #taskStarted will be triggered
		// but null will be given as a name to the current task, and then only, the exception will be raised
		return Policy.bind("antTreeLabelProvider.invalidTaskName");
	return text;
}

/**
 * @see IBaseLabelProvider#addListener(ILabelProviderListener)
 */
public void addListener(ILabelProviderListener listener) {
}

/**
 * @see IBaseLabelProvider#dispose()
 */
public void dispose() {
}

/**
 * @see IBaseLabelProvider#isLabelProperty(Object, String)
 */
public boolean isLabelProperty(Object arg0, String arg1) {
	return false;
}

/**
 * @see IBaseLabelProvider#removeListener(ILabelProviderListener)
 */
public void removeListener(ILabelProviderListener listener) {
}

}

