/*******************************************************************************
 * Copyright (c) 2012 Tensilica Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Abeer Bagul (Tensilica Inc) - initial API and implementation (Bug 372181)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.expression;

import org.eclipse.osgi.util.NLS;

public class ExpressionWorkingSetMessages extends NLS
{
	static
	{
		initializeMessages("org.eclipse.debug.internal.ui.expression.workingset.ExpressionWorkingSetMessages", //$NON-NLS-1$
				ExpressionWorkingSetMessages.class);
	}
	
	public static String Page_Title;
	public static String Page_Description;
	
	public static String WorkingSetName_label;
	public static String Expressions_label;
	public static String SelectAll;
	public static String DeselectAll;
	
	public static String Error_whitespace;
	public static String Error_emptyName;
	public static String Error_nameExists;
}
