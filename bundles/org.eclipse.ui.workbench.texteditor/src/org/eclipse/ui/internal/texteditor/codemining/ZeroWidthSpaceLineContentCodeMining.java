package org.eclipse.ui.internal.texteditor.codemining;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.codemining.LineContentCodeMining;

class ZeroWidthSpaceLineContentCodeMining extends LineContentCodeMining {

	private static final String ZWSP_ANNOTATION = "ZWSP"; //$NON-NLS-1$

	ZeroWidthSpaceLineContentCodeMining(Position position, ICodeMiningProvider provider) {
		super(position, provider);
	}

	@Override
	public boolean isResolved() {
		return true;
	}

	@Override
	public String getLabel() {
		return ZWSP_ANNOTATION;
	}
}