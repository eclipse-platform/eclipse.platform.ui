/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.internal.webapp.servlet;

import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.help.webapp.IFilter;

public class PrioritizedFilter implements IFilter, Comparable<PrioritizedFilter> {

	private IFilter filter;
	private int priority;

	public PrioritizedFilter(IFilter filter, int priority) {
		this.priority = priority;
		this.filter = filter;
	}
	
	private int priority() {
		return priority;
	}

	/*
	 * smaller number ranks higher
	 */
	@Override
	public int compareTo(PrioritizedFilter o) {
		return priority() - o.priority();
	}
	
	@Override
	public OutputStream filter(HttpServletRequest req, OutputStream out) {
		return filter.filter(req, out);
	}
}
