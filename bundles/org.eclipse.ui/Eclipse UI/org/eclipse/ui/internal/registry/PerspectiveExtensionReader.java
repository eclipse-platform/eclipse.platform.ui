package org.eclipse.ui.internal.registry;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
  Dan Rubel <dan_rubel@instantiations.com> 
    - Fix for bug 11490 - define hidden view (placeholder for view) in plugin.xml 
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
	// ATT_VISIBLE added by dan_rubel@instantiations.com  
	private static final String ATT_VISIBLE="visible";//$NON-NLS-1$
	private static final String VAL_LEFT="left";//$NON-NLS-1$
	private static final String VAL_RIGHT="right";//$NON-NLS-1$
	private static final String VAL_TOP="top";//$NON-NLS-1$
	private static final String VAL_BOTTOM="bottom";//$NON-NLS-1$
	private static final String VAL_STACK="stack";//$NON-NLS-1$
	private static final String VAL_FAST="fast";//$NON-NLS-1$
	// VAL_FALSE added by dan_rubel@instantiations.com  
	private static final String VAL_FALSE="false";//$NON-NLS-1$	
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
// processView(IConfigurationElement) modified by dan_rubel@instantiations.com
/**
 * Process a view
 */
private boolean processView(IConfigurationElement element) {
	// Get id, relative, and relationship.
	String id = element.getAttribute(ATT_ID);
	String relative = element.getAttribute(ATT_RELATIVE);
	String relationship = element.getAttribute(ATT_RELATIONSHIP);
	String ratioString = element.getAttribute(ATT_RATIO);
	float ratio;
	
	if (id == null || relative == null || relationship == null)
		return false;
	
	// Get the ratio.
	if (ratioString == null) {
		// The ratio has not been specified.
		ratio = IPageLayout.NULL_RATIO;
	} else {
		try {
			ratio = new Float(ratioString).floatValue();
		} catch (NumberFormatException e) {
			return false;
		}
		// If the ratio is outside the allowable range, mark it as invalid.
		if (ratio < IPageLayout.RATIO_MIN || ratio > IPageLayout.RATIO_MAX)
			ratio = IPageLayout.INVALID_RATIO;
	}

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
	boolean visible = !VAL_FALSE.equals(element.getAttribute(ATT_VISIBLE));

	// If stack ..
	if (stack) {
		if (visible)
			pageLayout.stackView(id, relative);
		else
			pageLayout.stackPlaceholder(id, relative);
		return true;
	}
	
	// If the view is a fast view...
	if (fast) {
		if (ratio == IPageLayout.NULL_RATIO) {
			// The ratio has not been specified.
			pageLayout.addFastView(id);
		} else {
			pageLayout.addFastView(id, ratio);
		}
		return true;
	}
	
	// The view is a regular view.
	// If the ratio is not specified or is invalid, use the default ratio.
	if (ratio == IPageLayout.NULL_RATIO || ratio == IPageLayout.INVALID_RATIO)
		ratio = IPageLayout.DEFAULT_VIEW_RATIO;

	if (visible)
		pageLayout.addView(id, intRelation, ratio, relative);
	else
		pageLayout.addPlaceholder(id, intRelation, ratio, relative);
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
