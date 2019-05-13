/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
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
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 433603
 *******************************************************************************/
package org.eclipse.ui.tests.intro;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.tests.api.MockPart;

/**
 * @since 3.0
 */
public class MockIntroPart extends MockPart implements IIntroPart {

	private IIntroSite site;

	public MockIntroPart() {
		super();
	}

	@Override
	public IIntroSite getIntroSite() {
		return site;
	}

	@Override
	public void init(IIntroSite site, IMemento memento) {
		setSite(site);
		callTrace.add("init");
	}

	/**
	 * @param site
	 */
	private void setSite(IIntroSite site) {
		this.site = site;
	}

	@Override
	public void saveState(IMemento memento) {
	}

	@Override
	public void standbyStateChanged(boolean standby) {
		callTrace.add("standbyStateChanged");
	}

	@Override
	public String getTitle() {
		return "Mock intro title";
	}
}
