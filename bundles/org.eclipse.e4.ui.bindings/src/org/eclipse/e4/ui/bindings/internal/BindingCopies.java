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
	}

	public static void setDefaultSysBindings(Collection<Binding> newSysBindings) {
		sysBindings = newSysBindings;
	}

	public static Collection<Binding> getSystemBindings() {
		if (sysBindings == null) {
			sysBindings = new ArrayList<Binding>();
		}
		return sysBindings;
	}

	static Collection<Binding> getUserBindings() {
		if (userBindings == null) {
			userBindings = new ArrayList<Binding>();
		}
		return userBindings;
	}

	static Collection<Binding> getInactiveBindings() {
		if (inactiveSystemBindings == null) {
			inactiveSystemBindings = new ArrayList<Binding>();
		}
		return inactiveSystemBindings;
	}

	public static void addInactiveSysBinding(Binding binding) {
		getInactiveBindings().add(binding);
	}

	public static Binding[] getInactiveSysBindings() {
		return getInactiveBindings().toArray(new Binding[getInactiveBindings().size()]);
	}

	public static void removeInactiveSysBinding(Binding binding) {
		getInactiveBindings().remove(binding);
	}

	public static void addUserBinding(Binding b) {
		getUserBindings().add(b);
	}

	public static Binding[] getUserDefinedBindings() {
		return getUserBindings().toArray(new Binding[getUserBindings().size()]);
	}

	public static boolean isUserBinding(TriggerSequence sequence, ParameterizedCommand command,
			String schemeId, String contextId) {
		boolean isUserBinding = false;
		Binding currBinding;
		Iterator<Binding> iter = getUserBindings().iterator();
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
