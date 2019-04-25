/*******************************************************************************
 * Copyright (c) 2018 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Fabian Pfaff <fabian.pfaff@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener2;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Adds content assist to a Combo, that is intended to be used to choose a
 * directory.
 */
public class DirectoryProposalContentAssist {

	private static class FileNameSubstringMatchContentProposalProvider implements IContentProposalProvider {

		private List<String> proposals = Collections.emptyList();

		/**
		 * Returns an array of valid proposals filtered by substring matching of the
		 * fileName.
		 *
		 * @param contents the current contents of the text field
		 * @param position the current position of the cursor in the contents
		 *
		 * @return the array of {@link IContentProposal} that represent valid proposals
		 *         for the field.
		 */
		@Override
		public IContentProposal[] getProposals(String contents, int position) {
			if (position == 0) {
				return null;
			}
			String substring = contents.substring(0, position);
			Pattern pattern = Pattern.compile(substring,
					Pattern.LITERAL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
			IContentProposal[] filteredProposals = proposals.stream()
					.filter(proposal -> proposal.length() >= substring.length() && pattern.matcher(proposal).find())
					.map(ContentProposal::new)
					.toArray(IContentProposal[]::new);
			return filteredProposals.length == 0 ? null : filteredProposals;
		}

		/**
		 * Set the Strings to be used as content proposals.
		 *
		 * @param proposals the Strings to be used as proposals.
		 */
		public void setProposals(List<String> proposals) {
			this.proposals = proposals;
		}

	}

	private static class OpenableContentProposalAdapter extends ContentProposalAdapter {

		private static final String CONTENT_ASSIST_DECORATION_ID = "org.eclipse.ui.internal.ide.DirectoryProposalContentAssist$ReopenableContentProposalAdapter"; //$NON-NLS-1$

		public OpenableContentProposalAdapter(Control control, IControlContentAdapter controlContentAdapter,
				IContentProposalProvider proposalProvider, KeyStroke keyStroke, char[] autoActivationCharacters) {
			super(control, controlContentAdapter, proposalProvider, keyStroke, autoActivationCharacters);
			installContentProposalFieldDecoration(control, keyStroke);
		}

		/**
		 * Installs a field decoration that shows the user that the control supports
		 * content assist.
		 *
		 * @param control   the control that supports content assist
		 * @param keyStroke the key stroke to be shown in the field decoration hover
		 *                  text
		 */
		private void installContentProposalFieldDecoration(Control control, KeyStroke keyStroke) {
			ControlDecoration decoration = new ControlDecoration(control, SWT.TOP | SWT.LEFT);
			decoration.setShowOnlyOnFocus(true);
			FieldDecoration dec = getContentAssistFieldDecoration(keyStroke);
			decoration.setImage(dec.getImage());
			decoration.setDescriptionText(dec.getDescription());
		}

		/**
		 * Return the field decoration that should be used to indicate that content
		 * assist is available for a field. Ensure that the decoration text includes the
		 * correct key binding.
		 *
		 * @param keyStroke the key stroke to be shown in the hover text
		 * @return the {@link FieldDecoration} that should be used to show content
		 *         assist.
		 **/
		private FieldDecoration getContentAssistFieldDecoration(KeyStroke keyStroke) {
			FieldDecorationRegistry registry = FieldDecorationRegistry.getDefault();
			String decId = CONTENT_ASSIST_DECORATION_ID + keyStroke;
			FieldDecoration dec = registry.getFieldDecoration(decId);

			// If there is not one, base ours on the standard JFace one.
			if (dec == null) {
				FieldDecoration originalDec = registry.getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);

				registry.registerFieldDecoration(decId, null, originalDec.getImage());
				dec = registry.getFieldDecoration(decId);
			}
			dec.setDescription(NLS.bind(WorkbenchMessages.ContentAssist_Cue_Description_Key, keyStroke));
			return dec;
		}

		@Override
		public void openProposalPopup() {
			super.openProposalPopup();
		}

	}

	private class DirectoryProposalAutoCompleteField {

		private FileNameSubstringMatchContentProposalProvider proposalProvider;
		private OpenableContentProposalAdapter adapter;

		public DirectoryProposalAutoCompleteField(Control control, IControlContentAdapter controlContentAdapter) {
			proposalProvider = new FileNameSubstringMatchContentProposalProvider();
			KeyStroke triggeringKeyStroke = safeKeyStroke("Ctrl+Space"); //$NON-NLS-1$
			adapter = new OpenableContentProposalAdapter(control, controlContentAdapter, proposalProvider,
					triggeringKeyStroke, null);
			adapter.setPropagateKeys(true);
			adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		}

		private KeyStroke safeKeyStroke(String keyStrokePattern) {
			try {
				return KeyStroke.getInstance(keyStrokePattern);
			} catch (ParseException e) {
				return null;
			}
		}

		/**
		 * Sets the given proposals on the content provider and forces the proposal
		 * pop-up to refresh its content.
		 *
		 * @param proposals         the proposals to set
		 * @param openProposalPopup if the proposal pop-up should be opened
		 */
		public void refreshProposals(List<String> proposals, boolean openProposalPopup) {
			proposalProvider.setProposals(proposals);
			adapter.refresh();
			if (openProposalPopup) {
				adapter.openProposalPopup();
			}
		}

	}

	private Path lastDir;
	private DirectoryProposalAutoCompleteField autoCompleteField;
	private Combo directoryCombo;
	/**
	 * The {@link ContentProposalAdapter} closes the proposal pop-up when the
	 * proposals are empty. This remembers the popup state and reopen the popup
	 * after the proposals have been updated by the asynchronous job.
	 */
	private boolean popupActivated = false;
	private List<CompletableFuture<Void>> proposalUpdateFutures = Collections.synchronizedList(new ArrayList<>());

	/**
	 * Applies auto-completion to a Combo that is intended to be used to choose a
	 * directory. Proposals are triggered automatically, but can also be triggered
	 * by pressing Ctrl+Space.
	 *
	 * @param combo the Combo that gets the auto-completion applied
	 */
	public void apply(Combo combo) {
		directoryCombo = combo;
		autoCompleteField = new DirectoryProposalAutoCompleteField(directoryCombo, new ComboContentAdapter());

		getContentProposalAdapter().addContentProposalListener(e -> {
			popupActivated = false;
			updateProposals(directoryCombo.getText(), popupActivated);
		});
		getContentProposalAdapter().addContentProposalListener(new IContentProposalListener2() {

			@Override
			public void proposalPopupOpened(ContentProposalAdapter adapter) {
				popupActivated = true;
			}

			@Override
			public void proposalPopupClosed(ContentProposalAdapter adapter) {
				// do nothing on purpose
			}
		});

		directoryCombo.addModifyListener(e -> {
			updateProposals(directoryCombo.getText().substring(0, directoryCombo.getCaretPosition()), false);
		});

		// use key release because otherwise the caret position is not yet updated
		directoryCombo.addKeyListener(KeyListener.keyReleasedAdapter(e -> {
			if (e.keyCode == SWT.ESC) {
				popupActivated = false;
			}
			if (isTraverse(e)) {
				int caretPosition = directoryCombo.getCaretPosition();
				updateProposals(directoryCombo.getText().substring(0, caretPosition), popupActivated);
			}
		}));

		directoryCombo.addMouseListener(MouseListener.mouseUpAdapter(e -> {
			popupActivated = false;
			int caretPosition = ((Combo) e.getSource()).getCaretPosition();
			updateProposals(directoryCombo.getText().substring(0, caretPosition), popupActivated);
		}));
	}

	private boolean isTraverse(KeyEvent e) {
		return e.keyCode == SWT.ARROW_LEFT || e.keyCode == SWT.ARROW_RIGHT
				 || e.keyCode == SWT.HOME || e.keyCode == SWT.END;
	}

	/**
	 * Checks if the directory in the combo has changed. If the directory has
	 * changed the proposals get asynchronously updated with the subfolders of the
	 * new directory. If the current content of the combo is not a valid directory
	 * the proposals get cleared.
	 */
	private void updateProposals(String textFromCombo, boolean openProposalPopup) {
		Path dir = pathWithoutFileName(textFromCombo);
		if (dir != null && dir.equals(lastDir)) {
			if (openProposalPopup) {
				autoCompleteField.adapter.openProposalPopup();
			}
			return;
		}
		if (dir == null || !safeIsDirectory(dir)) {
			updateProposals(Collections.emptyList(), false);
			lastDir = null;
			return;
		}

		lastDir = dir;

		CompletableFuture<Void> completableFuture = CompletableFuture
				.runAsync(() -> updateProposals(retrieveDirectoriesIn(dir), openProposalPopup));

		proposalUpdateFutures.add(completableFuture);
		proposalUpdateFutures.removeIf(CompletableFuture::isDone);
	}

	private List<String> retrieveDirectoriesIn(Path dir) {
		try (Stream<Path> files = Files.list(dir)) {
			return filterPaths(files.sorted()).collect(toList());
		} catch (IOException ex) {
			return new ArrayList<>();
		}
	}

	private void updateProposals(List<String> proposals, boolean openProposalPopup) {
		directoryCombo.getDisplay().syncExec(() -> autoCompleteField.refreshProposals(proposals, openProposalPopup));
	}

	private Path pathWithoutFileName(String inputPath) {
		int lastIndex = inputPath.lastIndexOf(File.separatorChar);
		if (separatorNotFound(lastIndex)) {
			return null;
		}
		return safeGetPath(removeFileName(inputPath, lastIndex));
	}

	private boolean separatorNotFound(int lastIndex) {
		return lastIndex < 0;
	}

	private String removeFileName(String text, int lastIndex) {
		if (lastIndex == 0) {
			return File.separator;
		}
		return text.substring(0, lastIndex + 1);
	}

	private Path safeGetPath(String text) {
		try {
			return Paths.get(text);
		} catch (InvalidPathException ex) {
			return null;
		}
	}

	private boolean safeIsDirectory(Path dir) {
		try {
			return dir.toFile().isDirectory();
		} catch (SecurityException ex) {
			return false;
		}
	}

	/**
	 * Filters out files and hidden directories.
	 *
	 * @param paths the Paths to filter
	 * @return the filtered paths
	 */
	private Stream<String> filterPaths(Stream<Path> paths) {
		return paths.filter(path -> {
			try {
				return safeIsDirectory(path) && !Files.isHidden(path);
			} catch (IOException e) {
				return false;
			}
		}).map(path -> path.toString() + File.separator);
	}

	public DirectoryProposalAutoCompleteField getAutoCompleteField() {
		return autoCompleteField;
	}

	public ContentProposalAdapter getContentProposalAdapter() {
		return autoCompleteField.adapter;
	}

	/**
	 * Wait until the asynchronous proposal refresh is finished. This method is
	 * intended to by used by tests.
	 *
	 * @param timeout timeout in milliseconds
	 * @throws TimeoutException     if the wait timed out
	 * @throws ExecutionException   if the future completed exceptionally
	 * @throws InterruptedException if the current thread was interrupted while
	 *                              waiting
	 */
	protected void wait(int timeout) throws InterruptedException, ExecutionException, TimeoutException {
		CompletableFuture.allOf(proposalUpdateFutures.toArray(new CompletableFuture[proposalUpdateFutures.size()]))
				.get(timeout, TimeUnit.MILLISECONDS);
	}

}
