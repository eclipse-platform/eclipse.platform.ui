package org.eclipse.ui.internal.texteditor.codemining;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.AbstractCodeMiningProvider;
import org.eclipse.jface.text.codemining.ICodeMining;

public class ZeroWidthSpaceLineContentCodeMiningProvider extends AbstractCodeMiningProvider {

	private static final char ZWSP_SIGN = '\u200b';

	@Override
	public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer,
			IProgressMonitor monitor) {
		List<ICodeMining> list = new ArrayList<>();

		String content = viewer.getDocument().get();
		for (int i = 0; i < content.length(); i++) {
			if (content.charAt(i) == ZWSP_SIGN) {
				list.add(createCodeMining(i));
			}
		}
		return CompletableFuture.completedFuture(list);
	}

	@Override
	public void dispose() {
	}

	private ICodeMining createCodeMining(int offset) {
		return new ZeroWidthSpaceLineContentCodeMining(new Position(offset, 1), this);
	}
}
