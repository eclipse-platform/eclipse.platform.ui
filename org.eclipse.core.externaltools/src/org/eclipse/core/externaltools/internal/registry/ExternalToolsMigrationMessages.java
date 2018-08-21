/**********************************************************************
.
. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License 2.0 which accompanies this distribution, and is
t https://www.eclipse.org/legal/epl-2.0/
t
t SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.externaltools.internal.registry;

import org.eclipse.osgi.util.NLS;

public class ExternalToolsMigrationMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.core.externaltools.internal.registry.launchConfigurations.ExternalToolsUIMessages";//$NON-NLS-1$

	public static String ExternalToolMigration_37;
	public static String ExternalToolMigration_38;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME,
				ExternalToolsMigrationMessages.class);
	}
}