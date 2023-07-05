package org.eclipse.ui.genericeditor.examples.dotproject;

import org.eclipse.ui.internal.genericeditor.IContentAssistantLookAndFeel;
import org.eclipse.ui.internal.genericeditor.IContentAssistantLookAndFeelProperties;

@SuppressWarnings("restriction")
public class ContentAssistantLookAndFeel implements IContentAssistantLookAndFeel {

	@Override
	public void applyTo(IContentAssistantLookAndFeelProperties assistant) {
		assistant.enableAutoActivation(true);
		assistant.enableAutoActivateCompletionOnType(true);
		assistant.setAutoActivationDelay(10);
	}

}
