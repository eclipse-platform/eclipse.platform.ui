package org.eclipse.ui.internal.registry;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.*;

/**
 * A strategy to read perspective extension from the registry.
 * A pespective extension is one of a view, viewAction, perspAction,
 * newWizardAction, or actionSet.
 */
public class PerspectiveExtensionReader extends RegistryReader {
	private String targetID;
	private PageLayout pageLayout;
	private static final String TAG_EXTENSION="perspectiveExtension";//$NON-NLS-1$
	private static final String TAG_ACTION_SET="actionSet";//$NON-NLS-1$
	private static final String TAG_WIZARD_SHORTCUT="newWizardShortcut";//$NON-NLS-1$
	private static final String TAG_VIEW_SHORTCUT="viewShortcut";//$NON-NLS-1$
	private static final String TAG_PERSP_SHORTCUT="perspectiveShortcut";//$NON-NLS-1$
	private static final String TAG_VIEW="view";//$NON-NLS-1$
	private static final String ATT_ID="id";//$NON-NLS-1$
	private static final String ATT_TARGET_ID="targetID";//$NON-NLS-1$
	private static final String ATT_RELATIVE="relative";//$NON-NLS-1$
	private static final String ATT_RELATIONSHIP="relationship";//$NON-NLS-1$
	private static final String ATT_RATIO="ratio";//$NON-NLS-1$
	private static final String VAL_LEFT="left";//$NON-NLS-1$
	private static final String VAL_RIGHT="right";//$NON-NLS-1$
	private static final String VAL_TOP="top";//$NON-NLS-1$
	private static final String VAL_BOTTOM="bottom";//$NON-NLS-1$
	private static final String VAL_STACK="stack";//$NON-NLS-1$
	private static final String VAL_FAST="fast";//$NON-NLS-1$
/**
 * RegistryViewReader constructor comment.
 */
public PerspectiveExtensionReader() {
	super();
}
/**
 * Read the view extensions within a registry.
 */
public void extendLayout(String id, PageLayout out)
{
	targetID = id;
	pageLayout = out;
	readRegistry(Platform.getPluginRegistry(), 
		IWorkbenchConstants.PLUGIN_ID, 
		IWorkbenchConstants.PL_PERSPECTIVE_EXTENSIONS);
}
/**
 * Process an action set.
 */
private boolean processActionSet(IConfigurationElement element) {
	String id = element.getAttribute(ATT_ID);
	if (id != null)
		pageLayout.addActionSet(id);
	return true;
}
/**
 * Process an extension.
 * Assumption: Extension is for current perspective.
 */
private boolean processExtension(IConfigurationElement element) {
	IConfigurationElement [] children = element.getChildren();
	for (int nX = 0; nX < children.length; nX ++) {
		IConfigurationElement child = children[nX];
		String type = child.getName();
		boolean result = false;
		if (type.equals(TAG_ACTION_SET))
			result = processActionSet(child);
		else if (type.equals(TAG_VIEW))
			result = processView(child);
		else if (type.equals(TAG_VIEW_SHORTCUT))
			result = processViewShortcut(child);
		else if (type.equals(TAG_WIZARD_SHORTCUT))
			result = processWizardShortcut(child);
		else if (type.equals(TAG_PERSP_SHORTCUT))
			result = processPerspectiveShortcut(child);
		if (!result) {
			WorkbenchPlugin.log("Unable to process element: " +//$NON-NLS-1$
				type +
				" in perspective extension: " +//$NON-NLS-1$
				element.getDeclaringExtension().getUniqueIdentifier());
		}
	}
	return true;
}
/**
 * Process a perspective shortcut
 */
private boolean processPerspectiveShortcut(IConfigurationElement element) {
	String id = element.getAttribute(ATT_ID);
	if (id != null)
		pageLayout.addPerspectiveShortcut(id);
	return true;
}
/**
 * Process a view
 */
private boolean processView(IConfigurationElement element) {
	// Get id, relative, and relationship.
	String id = element.getAttribute(ATT_ID);
	String relative = element.getAttribute(ATT_RELATIVE);
	String relationship = element.getAttribute(ATT_RELATIONSHIP);
	if (id == null || relative == null || relationship == null)
		return false;

	// Get relationship details.
	boolean stack = false;
	boolean fast = false;
	int intRelation = 0;
	if (relationship.equals(VAL_LEFT))
		intRelation = IPageLayout.LEFT;
	else if (relationship.equals(VAL_RIGHT))
		intRelation = IPageLayout.RIGHT;
	else if (relationship.equals(VAL_TOP))
		intRelation = IPageLayout.TOP;
	else if (relationship.equals(VAL_BOTTOM))
		intRelation = IPageLayout.BOTTOM;
	else if (relationship.equals(VAL_STACK)) 
		stack = true;
	else if (relationship.equals(VAL_FAST))
		fast = true;
	else
		return false;

	// If stack ..
	if (stack) {
		pageLayout.stackView(id, relative);
		return true;
	}
	
	// If fast ..
	if (fast) {
		pageLayout.addFastView(id);
		return true;
	}

	// Otherwise, get ratio.
	float ratio = 0.5f;
	String test = element.getAttribute(ATT_RATIO);
	if (test != null) {
		try {
			ratio = Float.parseFloat(test);
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	// Add view.
	pageLayout.addView(id, intRelation, ratio, relative);
	return true;
}
/**
 * Process a view shortcut
 */
private boolean processViewShortcut(IConfigurationElement element) {
	String id = element.getAttribute(ATT_ID);
	if (id != null)
		pageLayout.addShowViewShortcut(id);
	return true;
}
/**
 * Process a wizard shortcut
 */
private boolean processWizardShortcut(IConfigurationElement element) {
	String id = element.getAttribute(ATT_ID);
	if (id != null)
		pageLayout.addNewWizardShortcut(id);
	return true;
}
/**
 * readElement method comment.
 */
protected boolean readElement(IConfigurationElement element) {
	String type = element.getName();
	if (type.equals(TAG_EXTENSION)) {
		String id = element.getAttribute(ATT_TARGET_ID);
		if (targetID.equals(id))
			return processExtension(element);
		return true;
	}
	return false;
}
}
