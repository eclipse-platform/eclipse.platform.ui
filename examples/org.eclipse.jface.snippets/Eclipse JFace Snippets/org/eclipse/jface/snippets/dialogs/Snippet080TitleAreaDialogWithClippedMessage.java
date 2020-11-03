package org.eclipse.jface.snippets.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;

public class Snippet080TitleAreaDialogWithClippedMessage {
	public static void main(String[] args) {

		TitleAreaDialog dialog = new TitleAreaDialog(null) {
			@Override
			protected boolean isResizable() {
				return true;
			}
		};
		dialog.create();
		dialog.setTitle("Test Dialog");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 20; i++) {
			sb.append("Hello World ! ");
		}
		dialog.setMessage(sb.toString());

		dialog.open();
	}
}
