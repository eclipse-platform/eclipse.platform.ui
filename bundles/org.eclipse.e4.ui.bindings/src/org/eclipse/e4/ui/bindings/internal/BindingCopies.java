/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.bindings.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;

public class BindingCopies {

	private static Collection<Binding> sysBindings;
	private static Collection<Binding> inactiveSystemBindings;
	private static Collection<Binding> userBindings;

	public static void init() {
		inactiveSystemBindings = new ArrayList<Binding>();
		userBindings = new ArrayList<Binding>();
	}

	public static void setDefaultSysBindings(Collection<Binding> newSysBindings) {
		sysBindings = newSysBindings;
	}

	public static Collection<Binding> getSystemBindings() {
		return sysBindings;
	}

	public static void addInactiveSysBinding(Binding binding) {
		inactiveSystemBindings.add(binding);
	}

	public static Binding[] getInactiveSysBindings() {
		return inactiveSystemBindings.toArray(new Binding[inactiveSystemBindings.size()]);
	}

	public static void removeInactiveSysBinding(Binding binding) {
		inactiveSystemBindings.remove(binding);
	}

	public static void addUserBinding(Binding b) {
		userBindings.add(b);
	}

	public static Binding[] getUserDefinedBindings() {
		return userBindings.toArray(new Binding[userBindings.size()]);
	}

	public static boolean isUserBinding(TriggerSequence sequence, ParameterizedCommand command,
			String schemeId, String contextId) {
		boolean isUserBinding = false;
		Binding currBinding;
		Iterator<Binding> iter = userBindings.iterator();
		while (iter.hasNext() && !isUserBinding) {
			currBinding = iter.next();
			if (currBinding.getTriggerSequence().equals(sequence)
					&& currBinding.getParameterizedCommand().equals(command)
					&& currBinding.getSchemeId().equals(schemeId)
					&& currBinding.getContextId().equals(contextId)) {
				isUserBinding = true;
			}
		}
		return isUserBinding;
	}
}
