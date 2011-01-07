/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.remote;

import java.util.HashSet;
import junit.framework.TestCase;

import org.eclipse.help.AbstractTocProvider;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.base.remote.RemoteTocProvider;
import org.eclipse.help.internal.toc.TocContribution;
import org.eclipse.help.internal.toc.TocFileProvider;

public class TocManagerTest extends TestCase {
	
	private int mode;

	protected void setUp() throws Exception {
		BaseHelpSystem.ensureWebappRunning();
		mode = BaseHelpSystem.getMode();
		RemotePreferenceStore.savePreferences();
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
	}
	
	protected void tearDown() throws Exception {
		BaseHelpSystem.setMode(mode);
		RemotePreferenceStore.restorePreferences();
	}

	public void testDuplicatesOneRemote() throws Exception {
		
		RemotePreferenceStore.setMockRemoteServer();
		HelpPlugin.getTocManager().clearCache();
		boolean hasDuplicates=hasDuplicateContributions(HelpPlugin.getTocManager().getTocContributions("en"));
		assertFalse(hasDuplicates);
	}
	
	public void testDuplicatesTwoRemote() throws Exception {
		
		RemotePreferenceStore.setTwoMockRemoteServers();
		HelpPlugin.getTocManager().clearCache();
		boolean hasDuplicates=hasDuplicateContributions(HelpPlugin.getTocManager().getTocContributions("en"));
		assertFalse(hasDuplicates);
	}
	
	public void testLocalProviderPriority() throws Exception {
		
		int localPriority=0,remotePriority=0;
		RemotePreferenceStore.setMockRemoteServer();
		RemotePreferenceStore.setMockLocalPriority();
		HelpPlugin.getTocManager().clearCache();
		AbstractTocProvider [] tocProviders = HelpPlugin.getTocManager().getTocProviders();
		for(int i=0;i<tocProviders.length;i++)
		{
			if(tocProviders[i] instanceof TocFileProvider)
				localPriority = tocProviders[i].getPriority();
			
			if(tocProviders[i] instanceof RemoteTocProvider)
				remotePriority = tocProviders[i].getPriority();
		}
		
		assertTrue(localPriority<remotePriority);
	}
	
	public void testRemoteProviderPriority() throws Exception {
		
		RemotePreferenceStore.setMockRemoteServer();
		RemotePreferenceStore.setMockRemotePriority();
		HelpPlugin.getTocManager().clearCache();
		int localPriority=0,remotePriority=0;
		
		AbstractTocProvider [] tocProviders = HelpPlugin.getTocManager().getTocProviders();
		for(int i=0;i<tocProviders.length;i++)
		{
			if(tocProviders[i] instanceof TocFileProvider)
				localPriority = tocProviders[i].getPriority();
			
			if(tocProviders[i] instanceof RemoteTocProvider)
				remotePriority = tocProviders[i].getPriority();
		}
		
		assertTrue(remotePriority<localPriority);
	}
	
	public static boolean hasDuplicateContributions(TocContribution[] tocContributions)
	{
		HashSet<String> contributionsFound = new HashSet<String>();
		
		for(int i=0;i<tocContributions.length;i++)
		{
			if(contributionsFound.contains(tocContributions[i].getId()))
				return true;
			else
				contributionsFound.add(tocContributions[i].getId());
		}
		
		return false;
	}
}
