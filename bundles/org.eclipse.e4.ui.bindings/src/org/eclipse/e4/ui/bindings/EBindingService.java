package org.eclipse.e4.ui.bindings;

import java.util.Collection;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;

public interface EBindingService {
	Binding createBinding(TriggerSequence sequence, ParameterizedCommand command, String schemeId,
			String contextId);

	Binding activateBinding(TriggerSequence sequence, ParameterizedCommand command);

	void activateBinding(Binding binding);

	Binding deactivateBinding(TriggerSequence sequence, ParameterizedCommand command);

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
