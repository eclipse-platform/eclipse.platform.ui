/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.update.internal.core;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.update.configuration.*;
import org.eclipse.update.configurator.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.operations.*;


/**
 * Parses the installation log and creates installation configuration objects
 */
public class InstallLogParser {
	private IPath fLogPath;
	private BufferedReader buffRead;
	private InstallConfiguration currentConfiguration;
	private HashMap installConfigMap;
	private Comparator comparator;
	
	private static final String FEATURE_INSTALL = "feature-install"; //$NON-NLS-1$
	private static final String FEATURE_REMOVE = "feature-remove"; //$NON-NLS-1$
	private static final String SITE_INSTALL = "site-install"; //$NON-NLS-1$
	private static final String SITE_REMOVE = "site-remove"; //$NON-NLS-1$
	private static final String UNCONFIGURE = "feature-disable"; //$NON-NLS-1$
	private static final String CONFIGURE = "feature-enable"; //$NON-NLS-1$
	private static final String REVERT = "revert"; //$NON-NLS-1$
	private static final String RECONCILIATION = "reconciliation"; //$NON-NLS-1$
	private static final String PRESERVED = "preserve-configuration"; //$NON-NLS-1$	
	
	private static final String CONFIGURATION = "!CONFIGURATION"; //$NON-NLS-1$
	private static final String ACTIVITY = "!ACTIVITY"; //$NON-NLS-1$
	
	public static final String SUCCESS = "success"; //$NON-NLS-1$
	public static final String FAILURE = "failure"; //$NON-NLS-1$

	
	public InstallLogParser(){
		String loc = ConfiguratorUtils.getCurrentPlatformConfiguration().getConfigurationLocation().getFile();
		fLogPath = new Path(loc).removeLastSegments(1).append(".install-log"); 
		installConfigMap = new HashMap();
		try {
			IInstallConfiguration[] configs = SiteManager.getLocalSite().getConfigurationHistory();
			for (int i=0;i <configs.length; i++)
				installConfigMap.put(configs[i].getCreationDate(), configs[i]);
		} catch (CoreException e) {
			UpdateCore.log(e);
		}
	}
	
	public void parseInstallationLog(){
		try {
			openLog();
			parseLog();
		} catch (CoreException e) {
			UpdateUtils.logException(e);
		} finally {
			closeLog();
		}
	}
	
	private void openLog() throws CoreException {
		try {
			buffRead = new BufferedReader(new FileReader(fLogPath.toOSString()));
		} catch (FileNotFoundException e) {
			throwCoreException(e);
		}
	}
	
	private void throwCoreException(Throwable e) throws CoreException {
		throw new CoreException(
			new Status(
				IStatus.ERROR,
				UpdateUtils.getPluginId(),
				IStatus.ERROR,
				UpdateUtils.getString("InstallLogParser.errors"), //$NON-NLS-1$
				e));
	}
	
	private void parseLog() throws CoreException {
		// 		.install-log template
		//		!CONFIGURATION <configuration-date>
		//		!ACTIVITY <date> <target> <action> <status>

		try {
			String type, status, action;
			StringTokenizer htmlCode;

			while (buffRead.ready()) {

				htmlCode = new StringTokenizer(buffRead.readLine());
				while (!(htmlCode.hasMoreElements())) {
					if (!buffRead.ready())
						return;
					htmlCode = new StringTokenizer(buffRead.readLine());
				}

				type = htmlCode.nextToken().trim();

				if (type.equals(ACTIVITY)) {
					String date;
					StringBuffer target = new StringBuffer();
					date = htmlCode.nextToken("."); 
					htmlCode.nextToken(" "); 
					while (htmlCode.countTokens() > 2){
						target.append(" ");
						target.append(htmlCode.nextToken());
					}
					
					action = htmlCode.nextToken();
					status = htmlCode.nextToken();

					createActivity(action, date, status, target.toString(), currentConfiguration);
				}  else {
					StringBuffer date;
					date = new StringBuffer();
					while (htmlCode.countTokens() > 0){
						date.append(" ");
						date.append(htmlCode.nextToken());
					}
					currentConfiguration = (InstallConfiguration)installConfigMap.get(new Date(date.toString()));
				}
			}
		} catch (Exception e) {
			throwCoreException(e);
		}
	}
	
	private void closeLog() {
		try {
			if (buffRead != null)
				buffRead.close();
		} catch (IOException e) {
		} finally {
			buffRead = null;
		}
	}
	private IActivity createActivity(String action, String date, String status, String target, InstallConfiguration config){
		ConfigurationActivity a = new ConfigurationActivity();

		int code = 0;
		if (FEATURE_INSTALL.equals(action))
			code = IActivity.ACTION_FEATURE_INSTALL;
		else if (FEATURE_REMOVE.equals(action))
			code = IActivity.ACTION_FEATURE_REMOVE;
		else if (SITE_INSTALL.equals(action))
			code = IActivity.ACTION_SITE_INSTALL;
		else if (SITE_REMOVE.equals(action))
			code = IActivity.ACTION_SITE_REMOVE;
		else if (UNCONFIGURE.equals(action))
			code = IActivity.ACTION_UNCONFIGURE;
		else if (CONFIGURE.equals(action))
			code = IActivity.ACTION_CONFIGURE;
		else if (REVERT.equals(action))
			code = IActivity.ACTION_REVERT;
		else if (RECONCILIATION.equals(action))
			code = IActivity.ACTION_RECONCILIATION;
		else if (PRESERVED.equals(action))
			code = IActivity.ACTION_ADD_PRESERVED;
		
		a.setAction(code);
		a.setDate(new Date(date));
		a.setStatus(SUCCESS.equals(status) ? IActivity.STATUS_OK : IActivity.STATUS_NOK);
		a.setLabel(target);
		
		config.addActivity(a);
		return a;
	}
	private InstallConfiguration createConfiguration(String date){
		Date d = new Date(date);
		InstallConfiguration config = new InstallConfiguration();
		config.setCreationDate(d);
		config.setLabel(date);
		return config;
	}
}
