package org.eclipse.team.tests.ccvs.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.internal.ccvs.ui.IPromptCondition;

public class DummyPromptCondition implements IPromptCondition {
	public boolean needsPrompt(IResource resource) {
		return false;
	}
	public String promptMessage(IResource resource) {
		// this method should never be called
		return resource.getName();
	}
}
