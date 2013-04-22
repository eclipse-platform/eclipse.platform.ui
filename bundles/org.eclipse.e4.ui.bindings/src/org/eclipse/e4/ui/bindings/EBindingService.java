/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
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
import java.util.Map;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;

/**
 */
public interface EBindingService {

	public static final String DIALOG_CONTEXT_ID = "org.eclipse.ui.contexts.dialog"; //$NON-NLS-1$
	public static final String DEFAULT_SCHEME_ID = "org.eclipse.ui.defaultAcceleratorConfiguration"; //$NON-NLS-1$
	public static final String MODEL_TO_BINDING_KEY = "binding"; //$NON-NLS-1$
	public static final String ACTIVE_SCHEME_TAG = "activeSchemeId"; //$NON-NLS-1$
	public static final String SCHEME_ID_ATTR_TAG = "schemeId"; //$NON-NLS-1$
	public static final String LOCALE_ATTR_TAG = "locale"; //$NON-NLS-1$
	public static final String PLATFORM_ATTR_TAG = "platform"; //$NON-NLS-1$
	public static final String TYPE_ATTR_TAG = "type"; //$NON-NLS-1$
	public static final String DELETED_BINDING_TAG = "deleted"; //$NON-NLS-1$

	Binding createBinding(TriggerSequence sequence, ParameterizedCommand command, String contextId,
			Map<String, String> attributes);

	void activateBinding(Binding binding);

	void deactivateBinding(Binding binding);

	TriggerSequence createSequence(String sequence);

	Collection<Binding> getConflictsFor(TriggerSequence sequence);

	Collection<Binding> getAllConflicts();

	Binding getPerfectMatch(TriggerSequence trigger);

	boolean isPartialMatch(TriggerSequence keySequence);

	boolean isPerfectMatch(TriggerSequence sequence);

	TriggerSequence getBestSequenceFor(ParameterizedCommand command);

	Collection<TriggerSequence> getSequencesFor(ParameterizedCommand command);

	Collection<Binding> getPartialMatches(TriggerSequence sequence);

	Collection<Binding> getActiveBindings();

	Collection<Binding> getBindingsFor(ParameterizedCommand cmd);
}
