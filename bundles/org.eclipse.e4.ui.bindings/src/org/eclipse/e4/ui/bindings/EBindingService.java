package org.eclipse.e4.ui.bindings;

import java.util.Collection;
import org.eclipse.core.commands.ParameterizedCommand;

public interface EBindingService {
	void activateBinding(TriggerSequence sequence, ParameterizedCommand command);

	void deactivateBinding(TriggerSequence sequence, ParameterizedCommand command);

	TriggerSequence createSequence(String sequence);

	Collection getConflictsFor(TriggerSequence sequence);

	ParameterizedCommand getPerfectMatch(TriggerSequence trigger);

	boolean isPartialMatch(TriggerSequence keySequence);

	boolean isPerfectMatch(TriggerSequence sequence);
}
