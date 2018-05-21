/*******************************************************************************
 * Copyright (c) 2018 Remain Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     wim.jongman@remainsoftware.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.tips.ide.internal.provider;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.tips.json.JsonTipProvider;
import org.osgi.framework.Bundle;

public class TwitterTipProvider extends JsonTipProvider {

	public TwitterTipProvider() throws MalformedURLException {
		URL resource = getClass().getResource("twittertips.json"); // $NON-NLS-0$
		if (resource != null) {
			setJsonUrl(resource.toString());
		}
	}

	@Override
	public synchronized IStatus loadNewTips(IProgressMonitor pMonitor) {
		Bundle bundle = Platform.getBundle("org.eclipse.jdt.ui"); // $NON-NLS-0$
		if (bundle != null) {
			return super.loadNewTips(pMonitor);
		}
		return Status.OK_STATUS;
	}

	@Override
	public String getID() {
		return getClass().getName();
	}
}
