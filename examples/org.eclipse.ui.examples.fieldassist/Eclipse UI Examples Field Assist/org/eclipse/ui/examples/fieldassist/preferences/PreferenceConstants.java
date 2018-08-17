/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> - bug 132479 - [FieldAssist] Field assist example improvements
 *******************************************************************************/
package org.eclipse.ui.examples.fieldassist.preferences;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {
	public static final String PREF_DECORATOR_VERTICALLOCATION = "prefDecoratorVerticalLocation";
	public static final String PREF_DECORATOR_VERTICALLOCATION_TOP = "prefDecoratorVerticalTop";
	public static final String PREF_DECORATOR_VERTICALLOCATION_CENTER = "prefDecoratorVerticalCenter";
	public static final String PREF_DECORATOR_VERTICALLOCATION_BOTTOM = "prefDecoratorVerticalBottom";
	public static final String PREF_DECORATOR_HORIZONTALLOCATION = "prefDecoratorHorizontalLocation";
	public static final String PREF_DECORATOR_HORIZONTALLOCATION_LEFT = "prefDecoratorHorizontalLeft";
	public static final String PREF_DECORATOR_HORIZONTALLOCATION_RIGHT = "prefDecoratorHorizontalRight";

	public static final String PREF_DECORATOR_MARGINWIDTH = "prefDecoratorMarginWidth";

	public static final String PREF_SHOWERRORDECORATION = "prefShowErrorDecoration";
	public static final String PREF_SHOWERRORMESSAGE = "prefShowErrorMessage";

	public static final String PREF_SHOWWARNINGDECORATION = "prefShowWarningDecoration";

	public static final String PREF_SHOWREQUIREDFIELDDECORATION = "prefShowRequiredFieldDecoration";
	public static final String PREF_SHOWREQUIREDFIELDLABELINDICATOR = "prefShowRequiredFieldLabelIndicator";

	public static final String PREF_SHOWCONTENTPROPOSALCUE = "prefShowContentProposalCue";

	public static final String PREF_SHOWSECONDARYPOPUP = "prefShowSecondaryPopup";
	public static final String PREF_CONTENTASSISTKEY_PROPAGATE = "prefContentAssistKeyPropagate";
	public static final String PREF_CONTENTASSISTDELAY = "prefContentAssistDelay";

	public static final String PREF_CONTENTASSISTKEY = "prefContentAssistKey";
	public static final String PREF_CONTENTASSISTKEY1 = "Ctrl+Space";
	public static final String PREF_CONTENTASSISTKEY2 = "*";
	public static final String PREF_CONTENTASSISTKEYAUTO = "Alphanumeric key (on auto-activate delay)";
	public static final String PREF_CONTENTASSISTKEYAUTOSUBSET = "t, d (on auto-activate delay)";

	public static final String PREF_CONTENTASSISTRESULT = "prefContentResult";
	public static final String PREF_CONTENTASSISTRESULT_REPLACE = "replace";
	public static final String PREF_CONTENTASSISTRESULT_INSERT = "insert";
	public static final String PREF_CONTENTASSISTRESULT_NONE = "none";

	public static final String PREF_CONTENTASSISTFILTER = "prefContentAssistFilter";
	public static final String PREF_CONTENTASSISTFILTER_CUMULATIVE = "cumulative filter";
	public static final String PREF_CONTENTASSISTFILTER_CHAR = "character filter";
	public static final String PREF_CONTENTASSISTFILTER_NONE = "none";
}
