/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tools.metadata;

import java.io.*;
import java.io.IOException;
import java.io.PushbackInputStream;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.resolver.*;

public class StateDumper extends AbstractDumper {

	protected void dumpContents(PushbackInputStream input, StringBuffer contents) throws IOException, Exception, DumpException {
		PlatformAdmin admin = Platform.getPlatformAdmin();
		State state = admin.getFactory().readState(new DataInputStream(input));
		contents.append("State resolved: ");
		contents.append(state.isResolved());
		contents.append("\n");
		BundleDescription[] allBundles = state.getBundles();
		admin.getStateHelper().sortBundles(allBundles);
		for (int i = 0; i < allBundles.length; i++)
			dumpBundle(allBundles[i], contents);
	}

	private void dumpBundle(BundleDescription bundle, StringBuffer contents) {
		contents.append("\n");
		contents.append("Bundle: ");
		contents.append(bundle.getSymbolicName());
		contents.append('_');
		contents.append(bundle.getVersion());
		contents.append(" (");
		contents.append(bundle.isResolved() ? "resolved" : "unresolved");
		if (bundle.isSingleton())
			contents.append(", singleton");
		contents.append(")\n");
		HostSpecification host = bundle.getHost();
		if (host != null)
			dumpHost(host, contents);
		BundleSpecification[] required = bundle.getRequiredBundles();
		for (int i = 0; i < required.length; i++)
			dumpRequired(required[i], contents);
	}

	private void dumpRequired(BundleSpecification required, StringBuffer contents) {
		contents.append("\tRequired: ");
		contents.append(required.getName());
		contents.append(" - Version: ");
		contents.append(required.getVersionRange());
		contents.append(" (");
		contents.append(required.isResolved() ? ("actual: "+ required.getActualVersion().toString()) : "unresolved");
		if (required.isOptional())
			contents.append(", optional");
		contents.append(')');		
		contents.append('\n');
	}

	private void dumpHost(HostSpecification host, StringBuffer contents) {
		contents.append("\tHost: ");
		contents.append(host.getName());
		contents.append(" - Version: ");
		contents.append(host.getVersionRange());
		contents.append(" (");
		contents.append(host.isResolved() ? ("actual: "+ host.getActualVersion().toString()) : "unresolved");		
		contents.append(')');
		contents.append('\n');
	}

	public String getFormatDescription() {
		return "Framework state";
	}
}
