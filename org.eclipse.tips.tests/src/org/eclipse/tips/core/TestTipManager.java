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
package org.eclipse.tips.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;

public class TestTipManager extends TipManager {

	private boolean fShouldRun = true;
	private List<Integer> fReadList = new ArrayList<>();

	@Override
	public TipManager setRunAtStartup(boolean shouldRun) {
		fShouldRun = shouldRun;
		return this;
	}

	@Override
	public boolean isRunAtStartup() {
		return fShouldRun;
	}

	@Override
	public ITipManager register(TipProvider provider) {
		super.register(provider);
		load(provider);
		return this;
	}

	private void load(TipProvider provider) {
		provider.loadNewTips(new NullProgressMonitor());
	}

	@Override
	public boolean isRead(Tip tip) {
		return fReadList.contains(tip.hashCode());
	}

	@Override
	public TipManager setAsRead(Tip tip) {
		fReadList.remove((Integer) tip.hashCode());
		fReadList.add(tip.hashCode());
		return this;
	}

	@Override
	public ITipManager log(IStatus status) {
		System.out.println(status.toString());
		return this;
	}

	@Override
	public TipManager open(boolean startUp) {
		return this;
	}

	@Override
	public int getPriority(TipProvider provider) {
		return 20;
	}
}