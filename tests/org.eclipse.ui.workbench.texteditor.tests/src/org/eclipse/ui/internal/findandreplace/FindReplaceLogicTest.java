/*******************************************************************************
 * Copyright (c) 2023 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vector Informatik GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.findandreplace;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextViewer;

import org.eclipse.ui.internal.findandreplace.status.FindAllStatus;
import org.eclipse.ui.internal.findandreplace.status.FindStatus;
import org.eclipse.ui.internal.findandreplace.status.InvalidRegExStatus;
import org.eclipse.ui.internal.findandreplace.status.NoStatus;
import org.eclipse.ui.internal.findandreplace.status.ReplaceAllStatus;


public class FindReplaceLogicTest {
	Shell parentShell;

	private IFindReplaceLogic setupFindReplaceLogicObject(TextViewer target) {
		IFindReplaceLogic findReplaceLogic= new FindReplaceLogic();
		if (target != null) {
			findReplaceLogic.updateTarget(target.getFindReplaceTarget(), true);
		}

		return findReplaceLogic;
	}

	private TextViewer setupTextViewer(String contentText) {
		TextViewer textViewer= new TextViewer(parentShell, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		textViewer.setDocument(new Document(contentText));
		textViewer.getControl().setFocus();
		return textViewer;
	}

	@After
	public void disposeShell() {
		if (parentShell != null) {
			parentShell.dispose();
		}
	}

	@Before
	public void setupShell() {
		parentShell= new Shell();
	}

	@Test
	@Ignore("https://github.com/eclipse-platform/eclipse.platform.ui/issues/1203")
	public void testPerformReplaceAllBackwards() {
		TextViewer textViewer= setupTextViewer("");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);

		performReplaceAllBaseTestcases(findReplaceLogic, textViewer);
	}

	@Test
	public void testPerformReplaceAllForwards() {
		TextViewer textViewer= setupTextViewer("");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);

		performReplaceAllBaseTestcases(findReplaceLogic, textViewer);
	}

	/**
	 * Expects the TextViewer to contain a document with "aaaa"
	 *
	 * @param findReplaceLogic logic-object that will be tested
	 * @param textViewer textviewer-object that contains the contents on which findReplaceLogic
	 *            operates
	 */
	private void performReplaceAllBaseTestcases(IFindReplaceLogic findReplaceLogic, TextViewer textViewer) {
		Display display= parentShell.getDisplay();
		textViewer.setDocument(new Document("aaaa"));

		findReplaceLogic.performReplaceAll("a", "b", display);
		assertThat(textViewer.getDocument().get(), equalTo("bbbb"));
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 4);

		findReplaceLogic.performReplaceAll("b", "aa", display);
		assertThat(textViewer.getDocument().get(), equalTo("aaaaaaaa"));
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 4);

		findReplaceLogic.performReplaceAll("b", "c", display);
		assertThat(textViewer.getDocument().get(), equalTo("aaaaaaaa"));
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);

		findReplaceLogic.performReplaceAll("aaaaaaaa", "d", display); // https://github.com/eclipse-platform/eclipse.platform.ui/issues/1203
		assertThat(textViewer.getDocument().get(), equalTo("d"));
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 1);

		findReplaceLogic.performReplaceAll("d", null, display);
		assertThat(textViewer.getDocument().get(), equalTo(""));
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 1);

		textViewer.getDocument().set("f");
		findReplaceLogic.performReplaceAll("f", "", display);
		assertThat(textViewer.getDocument().get(), equalTo(""));
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 1);


		IFindReplaceTarget mockFindReplaceTarget= Mockito.mock(IFindReplaceTarget.class);
		Mockito.when(mockFindReplaceTarget.isEditable()).thenReturn(false);

		findReplaceLogic.updateTarget(mockFindReplaceTarget, false);
		findReplaceLogic.performReplaceAll("a", "b", display);
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);
	}

	@Test
	public void testPerformReplaceAllForwardRegEx() {
		TextViewer textViewer= setupTextViewer("hello@eclipse.com looks.almost@like_an_email");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.REGEX);
		findReplaceLogic.activate(SearchOptions.FORWARD);

		findReplaceLogic.performReplaceAll(".+\\@.+\\.com", "", parentShell.getDisplay());
		assertThat(textViewer.getDocument().get(), equalTo(" looks.almost@like_an_email"));
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 1);

		findReplaceLogic.performReplaceAll("( looks.)|(like_)", "", parentShell.getDisplay());
		assertThat(textViewer.getDocument().get(), equalTo("almost@an_email"));
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 2);

		findReplaceLogic.performReplaceAll("[", "", parentShell.getDisplay());
		assertThat(textViewer.getDocument().get(), equalTo("almost@an_email"));
		expectStatusIsMessageWithString(findReplaceLogic, "Unclosed character class near index 0" + System.lineSeparator()
				+ "[" + System.lineSeparator()
				+ "^");

	}

	@Test
	public void testPerformReplaceAllForward() {
		TextViewer textViewer= setupTextViewer("hello@eclipse.com looks.almost@like_an_email");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.REGEX);
		findReplaceLogic.activate(SearchOptions.FORWARD);

		findReplaceLogic.performReplaceAll(".+\\@.+\\.com", "", parentShell.getDisplay());
		assertThat(textViewer.getDocument().get(), equalTo(" looks.almost@like_an_email"));
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 1);

		findReplaceLogic.performReplaceAll("( looks.)|(like_)", "", parentShell.getDisplay());
		assertThat(textViewer.getDocument().get(), equalTo("almost@an_email"));
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 2);

		findReplaceLogic.performReplaceAll("[", "", parentShell.getDisplay());
		assertThat(textViewer.getDocument().get(), equalTo("almost@an_email"));
		expectStatusIsMessageWithString(findReplaceLogic, "Unclosed character class near index 0" + System.lineSeparator()
				+ "[" + System.lineSeparator()
				+ "^");
	}

	@Test
	public void testPerformSelectAndReplace() {
		TextViewer textViewer= setupTextViewer("Hello<replace>World<replace>!");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);

		findReplaceLogic.performSearch("<replace>"); // select first, then replace. We don't need to perform a second search
		findReplaceLogic.performSelectAndReplace("<replace>", " ");
		assertThat(textViewer.getDocument().get(), equalTo("Hello World<replace>!"));
		expectStatusEmpty(findReplaceLogic);

		findReplaceLogic.performSelectAndReplace("<replace>", " "); // perform the search yourself and replace that automatically
		assertThat(textViewer.getDocument().get(), equalTo("Hello World !"));
		expectStatusEmpty(findReplaceLogic);
	}

	@Test
	public void testPerformSelectAndReplaceRegEx() {
		TextViewer textViewer= setupTextViewer("Hello<replace>World<replace>!");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.activate(SearchOptions.REGEX);

		findReplaceLogic.performSearch("<(\\w*)>");
		boolean status= findReplaceLogic.performSelectAndReplace("<(\\w*)>", " ");
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello World<replace>!"));
		expectStatusEmpty(findReplaceLogic);

		status= findReplaceLogic.performSelectAndReplace("<(\\w*)>", " ");
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello World !"));
		expectStatusEmpty(findReplaceLogic);

		status= findReplaceLogic.performSelectAndReplace("<(\\w*)>", " ");
		assertEquals("Status wasn't correctly returned", false, status);
		assertEquals("Text shouldn't have been changed", "Hello World !", textViewer.getDocument().get());
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);
	}

	@Test
	public void testPerformSelectAndReplaceRegExWithLinebreaks() {
		TextViewer textViewer= setupTextViewer("""
				Hello
				World
				!""");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.activate(SearchOptions.REGEX);
		findReplaceLogic.deactivate(SearchOptions.WRAP);

		assertTrue(findReplaceLogic.performSearch("o$"));
		boolean status= findReplaceLogic.performSelectAndReplace("o$", "o!");
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("""
				Hello!
				World
				!"""));
		expectStatusEmpty(findReplaceLogic);

		status= findReplaceLogic.performSelectAndReplace("""
				d
				!""", "d!");
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("""
				Hello!
				World!"""));
		expectStatusEmpty(findReplaceLogic);

		status= findReplaceLogic.performSelectAndReplace("""
				""", " ");
		assertEquals("Status wasn't correctly returned", false, status);
		assertEquals("Text shouldn't have been changed", """
				Hello!
				World!""", textViewer.getDocument().get());
	}

	@Test
	public void testPerformSelectAndReplaceWithConfigurationChanges() {
		TextViewer textViewer= setupTextViewer("Hello<replace>World<replace>!<replace>!");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.activate(SearchOptions.REGEX);

		findReplaceLogic.performSearch("<(\\w*)>");
		boolean status= findReplaceLogic.performSelectAndReplace("<(\\w*)>", " ");
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello World<replace>!<replace>!"));
		expectStatusEmpty(findReplaceLogic);

		findReplaceLogic.deactivate(SearchOptions.REGEX);
		status= findReplaceLogic.performSelectAndReplace("<replace>", " ");
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello World !<replace>!"));
		expectStatusEmpty(findReplaceLogic);

		findReplaceLogic.activate(SearchOptions.REGEX);
		status= findReplaceLogic.performSelectAndReplace("<(\\w*)>", " ");
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello World ! !"));
		expectStatusEmpty(findReplaceLogic);

		status= findReplaceLogic.performSelectAndReplace("<(\\w*)>", " ");
		assertEquals("Status wasn't correctly returned", false, status);
		assertEquals("Text shouldn't have been changed", "Hello World ! !", textViewer.getDocument().get());
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);
	}

	@Test
	public void testPerformSelectAndReplaceBackward() {
		TextViewer textViewer= setupTextViewer("Hello<replace>World<replace>!");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.deactivate(SearchOptions.FORWARD);
		findReplaceLogic.activate(SearchOptions.WRAP); // this only works if the search was wrapped

		findReplaceLogic.performSearch("<replace>"); // select first, then replace. We don't need to perform a second search
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.WRAPPED);
		findReplaceLogic.performSelectAndReplace("<replace>", " ");
		assertThat(textViewer.getDocument().get(), equalTo("Hello<replace>World !"));

		findReplaceLogic.performSelectAndReplace("<replace>", " "); // perform the search yourself and replace that automatically
		assertThat(textViewer.getDocument().get(), equalTo("Hello World !"));
		expectStatusEmpty(findReplaceLogic);
	}

	@Test
	public void testPerformReplaceAndFind() {
		TextViewer textViewer= setupTextViewer("Hello<replace>World<replace>!");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);

		boolean status = findReplaceLogic.performReplaceAndFind("<replace>", " ");
		assertThat(status, is(true));
		assertThat(textViewer.getDocument().get(), equalTo("Hello World<replace>!"));
		assertThat(findReplaceLogic.getTarget().getSelectionText(), equalTo("<replace>"));
		expectStatusEmpty(findReplaceLogic);

		status= findReplaceLogic.performReplaceAndFind("<replace>", " ");
		assertThat(status, is(true));
		assertThat(textViewer.getDocument().get(), equalTo("Hello World !"));
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);

		status= findReplaceLogic.performReplaceAndFind("<replace>", " ");
		assertEquals("Status wasn't correctly returned", false, status);
		assertEquals("Text shouldn't have been changed", "Hello World !", textViewer.getDocument().get());
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);
	}

	@Test
	public void testPerformReplaceAndFindRegEx() {
		TextViewer textViewer= setupTextViewer("Hello<replace>World<replace>!<r>!");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.activate(SearchOptions.REGEX);

		boolean status= findReplaceLogic.performReplaceAndFind("<(\\w*)>", " ");
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello World<replace>!<r>!"));
		assertThat(findReplaceLogic.getTarget().getSelectionText(), equalTo("<replace>"));
		expectStatusEmpty(findReplaceLogic);

		status= findReplaceLogic.performReplaceAndFind("<(\\w*)>", " ");
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello World !<r>!"));
		assertThat(findReplaceLogic.getTarget().getSelectionText(), equalTo("<r>"));
		expectStatusEmpty(findReplaceLogic);

		status= findReplaceLogic.performReplaceAndFind("<(\\w)>", " ");
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello World ! !"));
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);

		status= findReplaceLogic.performReplaceAndFind("<(\\w*)>", " ");
		assertEquals("Status wasn't correctly returned", false, status);
		assertEquals("Text shouldn't have been changed", "Hello World ! !", textViewer.getDocument().get());
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);
	}

	@Test
	public void testPerformSelectAllForward() {
		TextViewer textViewer= setupTextViewer("AbAbAbAb");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);

		findReplaceLogic.performSelectAll("c", parentShell.getDisplay());
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);

		findReplaceLogic.performSelectAll("b", parentShell.getDisplay());
		expectStatusIsFindAllWithCount(findReplaceLogic, 4);
		// I don't have access to getAllSelectionPoints or similar (not yet implemented), so I cannot really test for correct behavior
		// related to https://github.com/eclipse-platform/eclipse.platform.ui/issues/1047

		findReplaceLogic.performSelectAll("AbAbAbAb", parentShell.getDisplay());
		expectStatusIsFindAllWithCount(findReplaceLogic, 1);
	}

	@Test
	public void testPerformSelectAllRegEx() {
		TextViewer textViewer= setupTextViewer("AbAbAbAb");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.activate(SearchOptions.REGEX);

		findReplaceLogic.performSelectAll("c.*", parentShell.getDisplay());
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);

		findReplaceLogic.performSelectAll("(Ab)*", parentShell.getDisplay());
		expectStatusIsFindAllWithCount(findReplaceLogic, 1);

		findReplaceLogic.performSelectAll("Ab(Ab)+Ab(Ab)+(Ab)+", parentShell.getDisplay());
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);
	}


	@Test
	public void testPerformSelectAllBackward() {
		TextViewer textViewer= setupTextViewer("AbAbAbAb");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.deactivate(SearchOptions.FORWARD);

		findReplaceLogic.performSelectAll("b", parentShell.getDisplay());
		expectStatusIsFindAllWithCount(findReplaceLogic, 4);
		// I don't have access to getAllSelectionPoints or similar (not yet implemented), so I cannot really test for correct behavior
		// related to https://github.com/eclipse-platform/eclipse.platform.ui/issues/1047

		findReplaceLogic.performSelectAll("AbAbAbAb", parentShell.getDisplay());
		expectStatusIsFindAllWithCount(findReplaceLogic, 1);
	}

	@Test
	public void testPerformSelectAllOnReadonlyTarget() {
		TextViewer textViewer= setupTextViewer("Ab Ab");
		textViewer.setEditable(false);
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.performSelectAll("Ab", Display.getCurrent());
		expectStatusIsFindAllWithCount(findReplaceLogic, 2);
	}

	@Test
	public void testSelectWholeWords() {
		TextViewer textViewer= setupTextViewer("Hello World of get and getters, set and setters");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.activate(SearchOptions.WHOLE_WORD);
		findReplaceLogic.deactivate(SearchOptions.WRAP);

		findReplaceLogic.performSearch("get");
		findReplaceLogic.performSearch("get");
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);
	}

	@Test
	public void testSelectInSearchScope() {
		TextViewer textViewer= setupTextViewer("line1\nline2\nline3");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		textViewer.setSelection(new TextSelection(6, 11));
		findReplaceLogic.deactivate(SearchOptions.GLOBAL);
		findReplaceLogic.performReplaceAll("l", "", Display.getCurrent());
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 2);

		findReplaceLogic.activate(SearchOptions.GLOBAL);
		textViewer.setSelection(new TextSelection(0, 5));
		findReplaceLogic.deactivate(SearchOptions.GLOBAL);

		findReplaceLogic.performReplaceAll("n", "", Display.getCurrent());
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 1);

		assertThat(textViewer.getTextWidget().getText(), is("lie1\nine2\nine3"));
	}

	@Test
	public void testWholeWordSearchAvailable() {
		TextViewer textViewer= setupTextViewer("line1\nline2\nline3");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);

		assertThat(findReplaceLogic.isWholeWordSearchAvailable("oneword"), is(true));
		assertThat(findReplaceLogic.isWholeWordSearchAvailable("stilläoneäword"), is(true));
		assertThat(findReplaceLogic.isWholeWordSearchAvailable("two.words"), is(false));
		assertThat(findReplaceLogic.isWholeWordSearchAvailable("two words"), is(false));
		assertThat(findReplaceLogic.isWholeWordSearchAvailable("oneword"), is(true));
		assertThat(findReplaceLogic.isWholeWordSearchAvailable("twöwords"), is(true));

		findReplaceLogic.activate(SearchOptions.REGEX);

		assertThat(findReplaceLogic.isWholeWordSearchAvailable("oneword"), is(false));
		assertThat(findReplaceLogic.isWholeWordSearchAvailable("stilläoneöword"), is(false));
		assertThat(findReplaceLogic.isWholeWordSearchAvailable("two.words"), is(false));
		assertThat(findReplaceLogic.isWholeWordSearchAvailable("two words"), is(false));

		assertThat(findReplaceLogic.isWholeWordSearchAvailable(""), is(false));
	}

	private void expectStatusEmpty(IFindReplaceLogic findReplaceLogic) {
		assertThat(findReplaceLogic.getStatus(), instanceOf(NoStatus.class));
	}

	private void expectStatusIsCode(IFindReplaceLogic findReplaceLogic, FindStatus.StatusCode code) {
		assertThat(findReplaceLogic.getStatus(), instanceOf(FindStatus.class));
		assertThat(((FindStatus) findReplaceLogic.getStatus()).getMessageCode(), equalTo(code));
	}

	private void expectStatusIsReplaceAllWithCount(IFindReplaceLogic findReplaceLogic, int count) {
		assertThat(findReplaceLogic.getStatus(), instanceOf(ReplaceAllStatus.class));
		assertThat(((ReplaceAllStatus) findReplaceLogic.getStatus()).getReplaceCount(), equalTo(count));
	}

	private void expectStatusIsFindAllWithCount(IFindReplaceLogic findReplaceLogic, int count) {
		assertThat(findReplaceLogic.getStatus(), instanceOf(FindAllStatus.class));
		assertThat(((FindAllStatus) findReplaceLogic.getStatus()).getSelectCount(), equalTo(count));
	}

	private void expectStatusIsMessageWithString(IFindReplaceLogic findReplaceLogic, String message) {
		assertThat(findReplaceLogic.getStatus(), instanceOf(InvalidRegExStatus.class));
		assertThat(((InvalidRegExStatus) findReplaceLogic.getStatus()).getMessage(), equalTo(message));
	}

}
