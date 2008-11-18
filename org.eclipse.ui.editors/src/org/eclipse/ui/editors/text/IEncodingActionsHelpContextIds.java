/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.editors.text;

import org.eclipse.ui.texteditor.IAbstractTextEditorHelpContextIds;


/**
 * Help context IDs for the encoding actions.
 * <p>
 * This interface contains constants only; it is not intended to be implemented.</p>
 *
 * @since 2.0
 * @deprecated As of 3.1, encoding needs to be changed via properties dialog
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IEncodingActionsHelpContextIds {

	/**
	 * Help id of the action to change the encoding into default.
	 * Default value: <code>"org.eclipse.ui.default_action_context"</code>
	 * @since 3.0
	 */
	public static final String DEFAULT= IAbstractTextEditorHelpContextIds.PREFIX + IEncodingActionsConstants.DEFAULT + IAbstractTextEditorHelpContextIds.ACTION_POSTFIX;

	/**
	 * Help id of the action to change the encoding into US ASCII.
	 * Default value: <code>"org.eclipse.ui.US-ASCII_action_context"</code>
	 */
	public static final String US_ASCII= IAbstractTextEditorHelpContextIds.PREFIX + IEncodingActionsConstants.US_ASCII + IAbstractTextEditorHelpContextIds.ACTION_POSTFIX;

	/**
	 * Help id of the action to change the encoding into ISO-8859-1.
	 * Default value: <code>"org.eclipse.ui.ISO-8859-1_action_context"</code>
	 */
	public static final String ISO_8859_1=  IAbstractTextEditorHelpContextIds.PREFIX + IEncodingActionsConstants.ISO_8859_1 + IAbstractTextEditorHelpContextIds.ACTION_POSTFIX;

	/**
	 * Help id of the action to change the encoding into UTF-8
	 * Default value: <code>"org.eclipse.ui.UTF-8_action_context"</code>
	 */
	public static final String UTF_8=  IAbstractTextEditorHelpContextIds.PREFIX + IEncodingActionsConstants.UTF_8 + IAbstractTextEditorHelpContextIds.ACTION_POSTFIX;

	/**
	 * Help id of the action to change the encoding into UTF-16BE.
	 * Default value: <code>"org.eclipse.ui.UTF-16BE_action_context"</code>
	 */
	public static final String UTF_16BE=  IAbstractTextEditorHelpContextIds.PREFIX + IEncodingActionsConstants.UTF_16BE + IAbstractTextEditorHelpContextIds.ACTION_POSTFIX;

	/**
	 * Help id of the action to change the encoding into UTF-16LE.
	 * Default value: <code>"org.eclipse.ui.UTF-16LE_action_context"</code>
	 */
	public static final String UTF_16LE=  IAbstractTextEditorHelpContextIds.PREFIX + IEncodingActionsConstants.UTF_16LE + IAbstractTextEditorHelpContextIds.ACTION_POSTFIX;

	/**
	 * Help id of the action to change the encoding into UTF-16.
	 * Default value: <code>"org.eclipse.ui.UTF-16_action_context"</code>
	 */
	public static final String UTF_16=  IAbstractTextEditorHelpContextIds.PREFIX + IEncodingActionsConstants.UTF_16 + IAbstractTextEditorHelpContextIds.ACTION_POSTFIX;

	/**
	 * Help id of the action to change the encoding into the system encoding.
	 * Default value: <code>"org.eclipse.ui.System_action_context"</code>
	 */
	public static final String SYSTEM=  IAbstractTextEditorHelpContextIds.PREFIX + IEncodingActionsConstants.SYSTEM + IAbstractTextEditorHelpContextIds.ACTION_POSTFIX;

	/**
	 * Help id of the action to change the encoding into a custom encoding.
	 * Default value: <code>"org.eclipse.ui.Custom_action_context"</code>
	 */
	public static final String CUSTOM=  IAbstractTextEditorHelpContextIds.PREFIX + IEncodingActionsConstants.CUSTOM + IAbstractTextEditorHelpContextIds.ACTION_POSTFIX;
}
