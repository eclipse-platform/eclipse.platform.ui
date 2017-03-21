package org.eclipse.ui.genericeditor.examples.dotproject;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;

public class ProjectDocumentParticipant implements IDocumentSetupParticipant {

	@Override
	public void setup(IDocument document) {
		document.addDocumentListener(new SpellCheckDocumentListener());
	}

}
