/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.bindings;

import java.util.Collection;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;

public interface EBindingService {

	public static final String DIALOG_CONTEXT_ID = "org.eclipse.ui.contexts.dialog"; //$NON-NLS-1$

	// TODO perhaps use a map of attributes for things
	// that aren't important to the model
	Binding createBinding(TriggerSequence sequence, ParameterizedCommand command, String schemeId,
			String contextId, String locale, String platform, int bindingType);

	void activateBinding(Binding binding);

	void deactivateBinding(Binding binding);

	TriggerSequence createSequence(String sequence);

	Collection<Binding> getConflictsFor(TriggerSequence sequence);

	Binding getPerfectMatch(TriggerSequence trigger);

	boolean isPartialMatch(TriggerSequence keySequence);

	boolean isPerfectMatch(TriggerSequence sequence);

	TriggerSequence getBestSequenceFor(ParameterizedCommand command);

	Collection<TriggerSequence> getSequencesFor(ParameterizedCommand command);

	Collection<Binding> getPartialMatches(TriggerSequence sequence);
}
