/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.launchConfigurations;

import org.eclipse.ant.internal.ui.IAntUIHelpContextIds;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.RefreshTab;
import org.eclipse.ui.externaltools.internal.launchConfigurations.ExternalToolsBuilderTab;

public class AntBuilderTabGroup extends AbstractLaunchConfigurationTabGroup {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#createTabs(org.eclipse.debug.ui.ILaunchConfigurationDialog, java.lang.String)
	 */
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		RefreshTab refresh = new RefreshTab();
		refresh.setHelpContextId(IAntUIHelpContextIds.ANT_REFRESH_TAB);
		AntClasspathTab classpath = new AntClasspathTab();
		classpath.setHelpContextId(IAntUIHelpContextIds.ANT_CLASSPATH_TAB);
		AntJRETab jre = new AntJRETab();
		jre.setHelpContextId(IAntUIHelpContextIds.ANT_JRE_TAB);
		AntEnvironmentTab env = new AntEnvironmentTab();
		env.setHelpContextId(IAntUIHelpContextIds.ANT_ENVIRONMENT_TAB);
		ExternalToolsBuilderTab builder = new ExternalToolsBuilderTab(false);
		builder.setHelpContextId(IAntUIHelpContextIds.ANT_BUILD_OPTIONS_TAB);
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
			new AntMainTab(),
			refresh,	
            new AntBuilderTargetsTab(),
			classpath,
			new AntPropertiesTab(),
			jre,
			env,
			builder
		};
		setTabs(tabs);
	}
}
