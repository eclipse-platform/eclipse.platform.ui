/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.rulers;

import org.eclipse.osgi.util.NLS;

public final class RulerColumnMessages extends NLS {
	private static final String BUNDLE_NAME= RulerColumnMessages.class.getName();

	static {
		NLS.initializeMessages(BUNDLE_NAME, RulerColumnMessages.class);
	}

	private RulerColumnMessages() {
		// Do not instantiate
	}

	public static String ExtensionPointHelper_invalid_contribution_msg;
	public static String ExtensionPointHelper_missing_attribute_msg;
	public static String ExtensionPointHelper_invalid_number_attribute_msg;
	public static String RulerColumnDescriptor_invalid_placement_msg;
	public static String RulerColumnDescriptor_missing_target_msg;
	public static String RulerColumnPlacement_illegal_child_msg;
	public static String RulerColumnPlacement_illegal_gravity_msg;
	public static String RulerColumnRegistry_cyclic_placement_msg;
	public static String RulerColumnRegistry_duplicate_id_msg;
	public static String RulerColumnRegistry_invalid_msg;
	public static String RulerColumnRegistry_unresolved_placement_msg;

}
