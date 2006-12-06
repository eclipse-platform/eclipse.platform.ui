/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.intro.universal.util;

import java.util.HashMap;
import java.util.Map;

/*
 * Accepts a set of Objects that represents each product's preference over some
 * matter (e.g. where an item should appear in welcome) and provides a final ruling
 * on which Object to use.
 */
public class PreferenceArbiter {

	private Map references;
	private Object leader;
	
	public void consider(Object obj) {
		if (obj != null) {
			if (references == null) {
				references = new HashMap();
			}
			int[] count = (int[])references.get(obj);
			if (count == null) {
				count = new int[] { 0 };
				references.put(obj, count);
			}
			++count[0];
			if (obj != leader) {
				if (leader == null) {
					leader = obj;
				}
				else if (count[0] > ((int[])references.get(leader))[0]) {
					leader = obj;
				}
			}
		}
	}
	
	public Object getWinner() {
		return leader;
	}
}
