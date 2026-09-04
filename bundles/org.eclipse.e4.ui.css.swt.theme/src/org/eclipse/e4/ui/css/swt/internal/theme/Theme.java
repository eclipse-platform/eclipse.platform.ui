/*******************************************************************************
 * Copyright (c) 2010, 2015 Tom Schindl and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.internal.theme;

import org.eclipse.e4.ui.css.swt.theme.ITheme;

public class Theme implements ITheme {
	private final String id;
	private final String label;
	private final boolean dark;
	private String osVersion;

	public Theme(String id, String label) {
		this(id, label, id != null && id.contains("dark")); //$NON-NLS-1$
	}

	public Theme(String id, String label, boolean dark) {
		this.id = id;
		this.label = label;
		this.dark = dark;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public boolean isDark() {
		return dark;
	}

	public void setOsVersion(String version) {
		this.osVersion = version;
	}

	public String getOsVersion() {
		return this.osVersion;
	}

	@Override
	public String toString() {
		return "Theme [id=" + id + ", label='" + label + "', dark=" + dark
				+ ", osVersion=" + osVersion + "]";
	}


}