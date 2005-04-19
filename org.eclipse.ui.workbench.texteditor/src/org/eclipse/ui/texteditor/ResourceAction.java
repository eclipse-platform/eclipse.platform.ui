/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;



import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.PlatformUI;


/**
 * An action which configures its label, image, tooltip, and description from
 * a resource bundle using known keys.
 * <p>
 * Clients may subclass this abstract class to define new kinds of actions. As
 * with <code>Action</code>, subclasses must implement the
 * <code>IAction.run</code> method to carry out the action's semantics.
 * </p>
 */
public abstract class ResourceAction extends Action {

	/**
	 * Retrieves and returns the value with the given key from the given resource
	 * bundle, or returns the given default value if there is no such resource.
	 * Convenience method for dealing gracefully with missing resources.
	 *
	 * @param bundle the resource bundle
	 * @param key the resource key
	 * @param defaultValue the default value, or <code>null</code>
	 * @return the resource value, or the given default value (which may be
	 *   <code>null</code>)
	 */
	protected static String getString(ResourceBundle bundle, String key, String defaultValue) {

		String value= defaultValue;
		try {
			value= bundle.getString(key);
		} catch (MissingResourceException x) {
		}

		return value;
	}

	/**
	 * Creates a new action that configures itself from the given resource
	 * bundle.
	 * <p>
	 * The following keys, prepended by the given option prefix,
	 * are used for retrieving resources from the given bundle:
	 * <ul>
	 *   <li><code>"label"</code> - <code>setText</code></li>
	 *   <li><code>"tooltip"</code> - <code>setToolTipText</code></li>
	 *   <li><code>"image"</code> - <code>setImageDescriptor</code></li>
	 *   <li><code>"description"</code> - <code>setDescription</code></li>
	 * </ul>
	 * </p>
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys, or
	 *   <code>null</code> if none
 	 * @param	style one of <code>IAction.AS_PUSH_BUTTON</code>, <code>IAction.AS_CHECK_BOX</code>,
 	 *			and <code>IAction.AS_RADIO_BUTTON</code>.
	 *
	 * @see ResourceAction#ResourceAction(ResourceBundle, String)
	 * @see org.eclipse.jface.action.IAction#AS_CHECK_BOX
	 * @see org.eclipse.jface.action.IAction#AS_DROP_DOWN_MENU
	 * @see org.eclipse.jface.action.IAction#AS_PUSH_BUTTON
	 * @see org.eclipse.jface.action.IAction#AS_RADIO_BUTTON
	 * @since 2.1
	 */
	public ResourceAction(ResourceBundle bundle, String prefix, int style) {
		super(null, style);
		initialize(bundle, prefix);
	}

	/**
	 * Creates a new action that configures itself from the given resource
	 * bundle.
	 * <p>
	 * The following keys, prepended by the given option prefix,
	 * are used for retrieving resources from the given bundle:
	 * <ul>
	 *   <li><code>"label"</code> - <code>setText</code></li>
	 *   <li><code>"tooltip"</code> - <code>setToolTipText</code></li>
	 *   <li><code>"image"</code> - <code>setImageDescriptor</code></li>
	 *   <li><code>"description"</code> - <code>setDescription</code></li>
	 * </ul>
	 * </p>
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys, or
	 *   <code>null</code> if none
	 */
	public ResourceAction(ResourceBundle bundle, String prefix) {
		super();
		initialize(bundle, prefix);
	}

	/**
	 * Sets the action's help context id.
	 *
	 * @param contextId the help context id
	 */
	public final void setHelpContextId(String contextId) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, contextId);
	}

	/**
	 * Initializes this action using the given bundle and prefix.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys, or <code>null</code> if none
	 * @since 2.1
	 */
	protected void initialize(ResourceBundle bundle, String prefix) {
		String labelKey= "label"; //$NON-NLS-1$
		String tooltipKey= "tooltip"; //$NON-NLS-1$
		String imageKey= "image"; //$NON-NLS-1$
		String descriptionKey= "description"; //$NON-NLS-1$

		if (prefix != null && prefix.length() > 0) {
			labelKey= prefix + labelKey;
			tooltipKey= prefix + tooltipKey;
			imageKey= prefix + imageKey;
			descriptionKey= prefix + descriptionKey;
		}

		setText(getString(bundle, labelKey, labelKey));
		setToolTipText(getString(bundle, tooltipKey, null));
		setDescription(getString(bundle, descriptionKey, null));

		String file= getString(bundle, imageKey, null);
		if (file != null && file.trim().length() > 0)
			setImageDescriptor(ImageDescriptor.createFromFile(getClass(), file));
	}
}
