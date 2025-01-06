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

import static java.lang.System.lineSeparator;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.function.Predicate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension3;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextViewer;

import org.eclipse.ui.internal.findandreplace.status.FindAllStatus;
import org.eclipse.ui.internal.findandreplace.status.FindStatus;
import org.eclipse.ui.internal.findandreplace.status.FindStatus.StatusCode;
import org.eclipse.ui.internal.findandreplace.status.InvalidRegExStatus;
import org.eclipse.ui.internal.findandreplace.status.NoStatus;
import org.eclipse.ui.internal.findandreplace.status.ReplaceAllStatus;

import org.eclipse.ui.texteditor.IFindReplaceTargetExtension2;


public class FindReplaceLogicTest {
	private static final String LINE_STRING= "line";

	private static final int LINE_STRING_LENGTH= LINE_STRING.length();

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

	private void setFindAndReplaceString(IFindReplaceLogic findReplaceLogic, String findString, String replaceString) {
		findReplaceLogic.setFindString(findString);
		findReplaceLogic.setReplaceString(replaceString);
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
		textViewer.setDocument(new Document("aaaa"));

		setFindAndReplaceString(findReplaceLogic, "a", "b");
		findReplaceLogic.performReplaceAll();
		assertThat(textViewer.getDocument().get(), equalTo("bbbb"));
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 4);

		setFindAndReplaceString(findReplaceLogic, "b", "aa");
		findReplaceLogic.performReplaceAll();
		assertThat(textViewer.getDocument().get(), equalTo("aaaaaaaa"));
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 4);

		setFindAndReplaceString(findReplaceLogic, "b", "c");
		findReplaceLogic.performReplaceAll();
		assertThat(textViewer.getDocument().get(), equalTo("aaaaaaaa"));
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);

		setFindAndReplaceString(findReplaceLogic, "aaaaaaaa", "d");
		findReplaceLogic.performReplaceAll(); // https://github.com/eclipse-platform/eclipse.platform.ui/issues/1203
		assertThat(textViewer.getDocument().get(), equalTo("d"));
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 1);

		setFindAndReplaceString(findReplaceLogic, "d", "");
		findReplaceLogic.performReplaceAll();
		assertThat(textViewer.getDocument().get(), equalTo(""));
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 1);

		textViewer.getDocument().set("f");
		setFindAndReplaceString(findReplaceLogic, "f", "");
		findReplaceLogic.performReplaceAll();
		assertThat(textViewer.getDocument().get(), equalTo(""));
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 1);

		IFindReplaceTarget mockFindReplaceTarget= Mockito.mock(IFindReplaceTarget.class);
		Mockito.when(mockFindReplaceTarget.isEditable()).thenReturn(false);

		findReplaceLogic.updateTarget(mockFindReplaceTarget, false);
		setFindAndReplaceString(findReplaceLogic, "a", "b");
		findReplaceLogic.performReplaceAll();
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);
	}

	@Test
	public void testPerformReplaceAllForwardRegEx() {
		TextViewer textViewer= setupTextViewer("hello@eclipse.com looks.almost@like_an_email");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.REGEX);
		findReplaceLogic.activate(SearchOptions.FORWARD);

		setFindAndReplaceString(findReplaceLogic, ".+\\@.+\\.com", "");
		findReplaceLogic.performReplaceAll();
		assertThat(textViewer.getDocument().get(), equalTo(" looks.almost@like_an_email"));
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 1);

		setFindAndReplaceString(findReplaceLogic, "( looks.)|(like_)", "");
		findReplaceLogic.performReplaceAll();
		assertThat(textViewer.getDocument().get(), equalTo("almost@an_email"));
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 2);

		setFindAndReplaceString(findReplaceLogic, "[", "");
		findReplaceLogic.performReplaceAll();
		assertThat(textViewer.getDocument().get(), equalTo("almost@an_email"));
		expectStatusIsMessageWithString(findReplaceLogic, "Unclosed character class near index 0" + lineSeparator()
				+ "[" + lineSeparator()
				+ "^");
	}

	@Test
	public void testPerformReplaceAllForward() {
		TextViewer textViewer= setupTextViewer("hello@eclipse.com looks.almost@like_an_email");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.REGEX);
		findReplaceLogic.activate(SearchOptions.FORWARD);

		setFindAndReplaceString(findReplaceLogic, ".+\\@.+\\.com", "");
		findReplaceLogic.performReplaceAll();
		assertThat(textViewer.getDocument().get(), equalTo(" looks.almost@like_an_email"));
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 1);

		setFindAndReplaceString(findReplaceLogic, "( looks.)|(like_)", "");
		findReplaceLogic.performReplaceAll();
		assertThat(textViewer.getDocument().get(), equalTo("almost@an_email"));
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 2);

		setFindAndReplaceString(findReplaceLogic, "[", "");
		findReplaceLogic.performReplaceAll();
		assertThat(textViewer.getDocument().get(), equalTo("almost@an_email"));
		expectStatusIsMessageWithString(findReplaceLogic, "Unclosed character class near index 0" + lineSeparator()
				+ "[" + lineSeparator()
				+ "^");
	}

	@Test
	public void testPerformSelectAndReplace() {
		TextViewer textViewer= setupTextViewer("Hello<replace>World<replace>!");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		setFindAndReplaceString(findReplaceLogic, "<replace>", " ");

		findReplaceLogic.performSearch(); // select first, then replace. We don't need to perform a second search
		findReplaceLogic.performSelectAndReplace();
		assertThat(textViewer.getDocument().get(), equalTo("Hello World<replace>!"));
		expectStatusEmpty(findReplaceLogic);

		findReplaceLogic.performSelectAndReplace(); // perform the search yourself and replace that automatically
		assertThat(textViewer.getDocument().get(), equalTo("Hello World !"));
		expectStatusEmpty(findReplaceLogic);
	}

	@Test
	public void testPerformSelectAndReplaceRegEx() {
		TextViewer textViewer= setupTextViewer("Hello<replace>World<replace>!");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.activate(SearchOptions.REGEX);
		setFindAndReplaceString(findReplaceLogic, "<(\\w*)>", " ");

		findReplaceLogic.performSearch();
		boolean status= findReplaceLogic.performSelectAndReplace();
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello World<replace>!"));
		expectStatusEmpty(findReplaceLogic);

		status= findReplaceLogic.performSelectAndReplace();
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello World !"));
		expectStatusEmpty(findReplaceLogic);

		status= findReplaceLogic.performSelectAndReplace();
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

		setFindAndReplaceString(findReplaceLogic, "o$", "o!");
		boolean status= findReplaceLogic.performSelectAndReplace();
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("""
				Hello!
				World
				!"""));
		expectStatusEmpty(findReplaceLogic);

		setFindAndReplaceString(findReplaceLogic, """
				d
				!""", "d!");
		status= findReplaceLogic.performSelectAndReplace();
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("""
				Hello!
				World!"""));
		expectStatusEmpty(findReplaceLogic);

		setFindAndReplaceString(findReplaceLogic, """
				""", " ");
		status= findReplaceLogic.performSelectAndReplace();
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

		setFindAndReplaceString(findReplaceLogic, "<(\\w*)>", " ");
		boolean status= findReplaceLogic.performSelectAndReplace();
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello World<replace>!<replace>!"));
		expectStatusEmpty(findReplaceLogic);

		setFindAndReplaceString(findReplaceLogic, "<replace>", " ");
		findReplaceLogic.deactivate(SearchOptions.REGEX);
		status= findReplaceLogic.performSelectAndReplace();
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello World !<replace>!"));
		expectStatusEmpty(findReplaceLogic);

		setFindAndReplaceString(findReplaceLogic, "<(\\w*)>", " ");
		findReplaceLogic.activate(SearchOptions.REGEX);
		status= findReplaceLogic.performSelectAndReplace();
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello World ! !"));
		expectStatusEmpty(findReplaceLogic);

		status= findReplaceLogic.performSelectAndReplace();
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
		setFindAndReplaceString(findReplaceLogic, "<replace>", " ");

		findReplaceLogic.performSearch(); // select first, then replace. We don't need to perform a second search
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.WRAPPED);
		findReplaceLogic.performSelectAndReplace();
		assertThat(textViewer.getDocument().get(), equalTo("Hello<replace>World !"));

		findReplaceLogic.performSelectAndReplace(); // perform the search yourself and replace that automatically
		assertThat(textViewer.getDocument().get(), equalTo("Hello World !"));
		expectStatusEmpty(findReplaceLogic);
	}

	@Test
	public void testPerformReplaceAndFind_caseInsensitive() {
		TextViewer textViewer= setupTextViewer("Hello<replace>World<replace>!");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		setFindAndReplaceString(findReplaceLogic, "<Replace>", " ");

		boolean status= findReplaceLogic.performReplaceAndFind();
		assertTrue("replace should have been performed", status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello World<replace>!"));
		assertThat(findReplaceLogic.getTarget().getSelectionText(), equalTo("<replace>"));
		expectStatusEmpty(findReplaceLogic);

		setFindAndReplaceString(findReplaceLogic, "<replace>", " ");
		status= findReplaceLogic.performReplaceAndFind();
		assertTrue("replace should have been performed", status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello World !"));
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);

		status= findReplaceLogic.performReplaceAndFind();
		assertFalse("replace should not have been performed", status);
		assertEquals("Text shouldn't have been changed", "Hello World !", textViewer.getDocument().get());
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);
	}

	@Test
	public void testPerformReplaceAndFind_caseSensitive() {
		TextViewer textViewer= setupTextViewer("Hello<Replace>World<replace>!");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.activate(SearchOptions.CASE_SENSITIVE);
		setFindAndReplaceString(findReplaceLogic, "<replace>", " ");

		boolean status= findReplaceLogic.performReplaceAndFind();
		assertTrue("replace should have been performed", status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello<Replace>World !"));
		assertThat(findReplaceLogic.getTarget().getSelectionText(), equalTo(" "));

		status= findReplaceLogic.performReplaceAndFind();
		assertFalse("replace should not have been performed", status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello<Replace>World !"));
		assertThat(findReplaceLogic.getTarget().getSelectionText(), equalTo(" "));
	}

	@Test
	public void testPerformReplaceAndFind_caseSensitiveAndIncremental() {
		TextViewer textViewer= setupTextViewer("Hello<replace>World<replace>!");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.activate(SearchOptions.INCREMENTAL);
		setFindAndReplaceString(findReplaceLogic, "<Replace>", " ");

		boolean status= findReplaceLogic.performReplaceAndFind();
		assertTrue("replace should have been performed", status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello World<replace>!"));
		assertThat(findReplaceLogic.getTarget().getSelectionText(), equalTo("<replace>"));
		expectStatusEmpty(findReplaceLogic);

		setFindAndReplaceString(findReplaceLogic, "<replace>", " ");
		status= findReplaceLogic.performReplaceAndFind();
		assertTrue("replace should have been performed", status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello World !"));
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);

		status= findReplaceLogic.performReplaceAndFind();
		assertFalse("replace should not have been performed", status);
		assertEquals("Text shouldn't have been changed", "Hello World !", textViewer.getDocument().get());
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);
	}

	@Test
	public void testPerformReplaceAndFindRegEx() {
		TextViewer textViewer= setupTextViewer("Hello<replace>World<replace>!<r>!");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.activate(SearchOptions.REGEX);

		executeReplaceAndFindRegExTest(textViewer, findReplaceLogic);
	}

	@Test
	public void testPerformReplaceAndFindRegEx_incrementalActive() {
		TextViewer textViewer= setupTextViewer("Hello<replace>World<replace>!<r>!");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.activate(SearchOptions.INCREMENTAL);
		findReplaceLogic.activate(SearchOptions.REGEX);

		executeReplaceAndFindRegExTest(textViewer, findReplaceLogic);
	}

	@Test
	public void testPerformReplaceAndFindRegEx_withInvalidEscapeInReplace() {
		TextViewer textViewer= setupTextViewer("Hello");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.activate(SearchOptions.REGEX);

		setFindAndReplaceString(findReplaceLogic, "Hello", "Hello\\");
		boolean status= findReplaceLogic.performReplaceAndFind();
		assertFalse(status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello"));
		assertThat(findReplaceLogic.getTarget().getSelectionText(), equalTo("Hello"));
		assertThat(findReplaceLogic.getStatus(), instanceOf(InvalidRegExStatus.class));

		setFindAndReplaceString(findReplaceLogic, "Hello", "Hello" + System.lineSeparator());

		status= findReplaceLogic.performReplaceAndFind();
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello" + System.lineSeparator()));
		assertThat(findReplaceLogic.getTarget().getSelectionText(), equalTo("Hello" + System.lineSeparator()));
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);
	}

	private void executeReplaceAndFindRegExTest(TextViewer textViewer, IFindReplaceLogic findReplaceLogic) {
		setFindAndReplaceString(findReplaceLogic, "<(\\w*)>", " ");

		boolean status= findReplaceLogic.performReplaceAndFind();
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello World<replace>!<r>!"));
		assertThat(findReplaceLogic.getTarget().getSelectionText(), equalTo("<replace>"));
		expectStatusEmpty(findReplaceLogic);

		status= findReplaceLogic.performReplaceAndFind();
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello World !<r>!"));
		assertThat(findReplaceLogic.getTarget().getSelectionText(), equalTo("<r>"));
		expectStatusEmpty(findReplaceLogic);

		setFindAndReplaceString(findReplaceLogic, "<(\\w)>", " ");
		status= findReplaceLogic.performReplaceAndFind();
		assertTrue(status);
		assertThat(textViewer.getDocument().get(), equalTo("Hello World ! !"));
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);

		setFindAndReplaceString(findReplaceLogic, "<(\\w*)>", " ");
		status= findReplaceLogic.performReplaceAndFind();
		assertEquals("Status wasn't correctly returned", false, status);
		assertEquals("Text shouldn't have been changed", "Hello World ! !", textViewer.getDocument().get());
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);
	}

	@Test
	public void testPerformSearchAndReplaceRegEx_incrementalActive() {
		TextViewer textViewer= setupTextViewer("some text");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.activate(SearchOptions.INCREMENTAL);
		findReplaceLogic.activate(SearchOptions.REGEX);

		findReplaceLogic.setFindString("text");
		textViewer.setSelectedRange(0, 0);

		findReplaceLogic.setReplaceString("");
		findReplaceLogic.performSelectAndReplace();

		assertEquals("some ", textViewer.getDocument().get());
	}

	@Test
	public void testPerformSelectAllForward() {
		TextViewer textViewer= setupTextViewer("AbAbAbAb");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);

		findReplaceLogic.setFindString("c");
		findReplaceLogic.performSelectAll();
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);

		findReplaceLogic.setFindString("b");
		findReplaceLogic.performSelectAll();
		expectStatusIsFindAllWithCount(findReplaceLogic, 4);
		// I don't have access to getAllSelectionPoints or similar (not yet implemented), so I cannot really test for correct behavior
		// related to https://github.com/eclipse-platform/eclipse.platform.ui/issues/1047

		findReplaceLogic.setFindString("AbAbAbAb");
		findReplaceLogic.performSelectAll();
		expectStatusIsFindAllWithCount(findReplaceLogic, 1);
	}

	@Test
	public void testPerformSelectAllRegEx() {
		TextViewer textViewer= setupTextViewer("AbAbAbAb");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.activate(SearchOptions.REGEX);

		findReplaceLogic.setFindString("c.*");
		findReplaceLogic.performSelectAll();
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);

		findReplaceLogic.setFindString("(Ab)*");
		findReplaceLogic.performSelectAll();
		expectStatusIsFindAllWithCount(findReplaceLogic, 1);

		findReplaceLogic.setFindString("Ab(Ab)+Ab(Ab)+(Ab)+");
		findReplaceLogic.performSelectAll();
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);
	}


	@Test
	public void testPerformSelectAllBackward() {
		TextViewer textViewer= setupTextViewer("AbAbAbAb");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.deactivate(SearchOptions.FORWARD);

		findReplaceLogic.setFindString("b");
		findReplaceLogic.performSelectAll();
		expectStatusIsFindAllWithCount(findReplaceLogic, 4);
		// I don't have access to getAllSelectionPoints or similar (not yet implemented), so I cannot really test for correct behavior
		// related to https://github.com/eclipse-platform/eclipse.platform.ui/issues/1047

		findReplaceLogic.setFindString("AbAbAbAb");
		findReplaceLogic.performSelectAll();
		expectStatusIsFindAllWithCount(findReplaceLogic, 1);
	}

	@Test
	public void testPerformSelectAllOnReadonlyTarget() {
		TextViewer textViewer= setupTextViewer("Ab Ab");
		textViewer.setEditable(false);
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.setFindString("Ab");
		findReplaceLogic.performSelectAll();
		expectStatusIsFindAllWithCount(findReplaceLogic, 2);
	}

	@Test
	public void testSelectWholeWords() {
		TextViewer textViewer= setupTextViewer("Hello World of get and getters, set and setters");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.activate(SearchOptions.WHOLE_WORD);
		findReplaceLogic.deactivate(SearchOptions.WRAP);

		findReplaceLogic.setFindString("get");
		findReplaceLogic.performSearch();
		findReplaceLogic.performSearch();
		expectStatusIsCode(findReplaceLogic, FindStatus.StatusCode.NO_MATCH);
	}

	@Test
	public void testSelectInSearchScope_withZeroLengthSelection() {
		String originalContents= "line1" + lineSeparator() + "line2" + lineSeparator() + "line3";
		TextViewer textViewer= setupTextViewer(originalContents);
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);

		int lineLength= ("line1" + lineSeparator()).length();
		textViewer.setSelection(new TextSelection(lineLength + 1, 0));
		findReplaceLogic.deactivate(SearchOptions.GLOBAL);
		setFindAndReplaceString(findReplaceLogic, "l", "");
		findReplaceLogic.performReplaceAll();

		expectStatusIsReplaceAllWithCount(findReplaceLogic, 1);
		assertThat(textViewer.getTextWidget().getText(), is("line1" + lineSeparator() + "ine2" + lineSeparator() + "line3"));
	}

	@Test
	public void testSelectInSearchScope_withZeroLengthSelectionAtBeginningOfLine() {
		String originalContents= "line1" + lineSeparator() + "line2" + lineSeparator() + "line3";
		TextViewer textViewer= setupTextViewer(originalContents);
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);

		int lineLength= ("line1" + lineSeparator()).length();
		textViewer.setSelection(new TextSelection(lineLength, 0));
		findReplaceLogic.deactivate(SearchOptions.GLOBAL);
		setFindAndReplaceString(findReplaceLogic, "l", "");
		findReplaceLogic.performReplaceAll();

		expectStatusIsReplaceAllWithCount(findReplaceLogic, 1);
		assertThat(textViewer.getTextWidget().getText(), is("line1" + lineSeparator() + "ine2" + lineSeparator() + "line3"));
	}

	@Test
	public void testSelectInSearchScope_withSingleLineelection() {
		String originalContents= "line1" + lineSeparator() + "line2" + lineSeparator() + "line3";
		TextViewer textViewer= setupTextViewer(originalContents);
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);

		int lineLength= ("line1" + lineSeparator()).length();
		textViewer.setSelection(new TextSelection(lineLength + 1, 3));
		findReplaceLogic.deactivate(SearchOptions.GLOBAL);
		setFindAndReplaceString(findReplaceLogic, "l", "");
		findReplaceLogic.performReplaceAll();

		expectStatusIsReplaceAllWithCount(findReplaceLogic, 1);
		assertThat(textViewer.getTextWidget().getText(), is("line1" + lineSeparator() + "ine2" + lineSeparator() + "line3"));
	}

	@Test
	public void testSelectInSearchScope_withMultiLineSelection() {
		String originalContents= "line1" + lineSeparator() + "line2" + lineSeparator() + "line3";
		TextViewer textViewer= setupTextViewer(originalContents);
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);

		int beginningOfSecondLine= originalContents.indexOf("l", 1);
		textViewer.setSelection(new TextSelection(beginningOfSecondLine, originalContents.substring(beginningOfSecondLine).length()));
		findReplaceLogic.deactivate(SearchOptions.GLOBAL);
		setFindAndReplaceString(findReplaceLogic, "l", "");
		findReplaceLogic.performReplaceAll();

		expectStatusIsReplaceAllWithCount(findReplaceLogic, 2);
		assertThat(textViewer.getTextWidget().getText(), is("line1" + lineSeparator() + "ine2" + lineSeparator() + "ine3"));
	}

	@Test
	public void testSelectInSearchScope_withSelectionEndingAtBeginningOfLine() {
		String originalContents= "line1" + lineSeparator() + "line2" + lineSeparator() + "line3";
		TextViewer textViewer= setupTextViewer(originalContents);
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);

		int beginningOfSecondLine= originalContents.indexOf("l", 1);
		int lineLength= ("line1" + lineSeparator()).length();
		textViewer.setSelection(new TextSelection(beginningOfSecondLine, lineLength));
		findReplaceLogic.deactivate(SearchOptions.GLOBAL);
		setFindAndReplaceString(findReplaceLogic, "l", "");
		findReplaceLogic.performReplaceAll();

		// Selection ending at beginning of new line should not include that line in search scope
		expectStatusIsReplaceAllWithCount(findReplaceLogic, 1);
		assertThat(textViewer.getTextWidget().getText(), is("line1" + lineSeparator() + "ine2" + lineSeparator() + "line3"));
	}

	@Test
	public void testSelectInSearchScope_changeScope() {
		String originalContents= "line1" + lineSeparator() + "line2" + lineSeparator() + "line3";
		TextViewer textViewer= setupTextViewer(originalContents);
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);

		textViewer.setSelection(new TextSelection(8, 10));
		findReplaceLogic.deactivate(SearchOptions.GLOBAL);
		findReplaceLogic.activate(SearchOptions.GLOBAL);
		textViewer.setSelection(new TextSelection(0, 2));
		findReplaceLogic.deactivate(SearchOptions.GLOBAL);
		setFindAndReplaceString(findReplaceLogic, "l", "");
		findReplaceLogic.performReplaceAll();

		expectStatusIsReplaceAllWithCount(findReplaceLogic, 1);
		assertThat(textViewer.getTextWidget().getText(), is("ine1" + lineSeparator() + "line2" + lineSeparator() + "line3"));
	}

	@Test
	public void testWholeWordSearchAvailable() {
		String originalContents= "line1" + lineSeparator() + "line2" + lineSeparator() + "line3";
		TextViewer textViewer= setupTextViewer(originalContents);
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);

		Predicate<String> isConsideredWholeWord= string -> {
			findReplaceLogic.setFindString(string);
			return findReplaceLogic.isAvailable(SearchOptions.WHOLE_WORD);
		};

		assertTrue(isConsideredWholeWord.test("oneword"));
		assertTrue(isConsideredWholeWord.test("stilläoneäword"));
		assertFalse(isConsideredWholeWord.test("two.words"));
		assertFalse(isConsideredWholeWord.test("two words"));
		assertTrue(isConsideredWholeWord.test("oneword"));
		assertTrue(isConsideredWholeWord.test("twöwords"));

		findReplaceLogic.activate(SearchOptions.REGEX);

		assertFalse(isConsideredWholeWord.test("oneword"));
		assertFalse(isConsideredWholeWord.test("stilläoneäword"));
		assertFalse(isConsideredWholeWord.test("two.words"));
		assertFalse(isConsideredWholeWord.test("two words"));

		assertFalse(isConsideredWholeWord.test(""));
	}

	@Test
	public void testReplaceInScopeStaysInScope() {
		TextViewer textViewer= setupTextViewer(LINE_STRING + lineSeparator() + LINE_STRING + lineSeparator() + LINE_STRING);
		int lineSeparatorLength= lineSeparator().length();
		textViewer.setSelectedRange(LINE_STRING_LENGTH + lineSeparatorLength, 2 * LINE_STRING_LENGTH + lineSeparatorLength);
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.deactivate(SearchOptions.GLOBAL);
		findReplaceLogic.activate(SearchOptions.WRAP);
		setFindAndReplaceString(findReplaceLogic, LINE_STRING, "");
		findReplaceLogic.performSelectAndReplace();
		assertThat(textViewer.getTextWidget().getText(), is(LINE_STRING + lineSeparator() + lineSeparator() + LINE_STRING));
		expectStatusEmpty(findReplaceLogic);

		findReplaceLogic.performSelectAndReplace();
		assertThat(textViewer.getTextWidget().getText(), is(LINE_STRING + lineSeparator() + lineSeparator()));
		expectStatusEmpty(findReplaceLogic);

		findReplaceLogic.performSelectAndReplace();
		assertThat(textViewer.getTextWidget().getText(), is(LINE_STRING + lineSeparator() + lineSeparator()));
		expectStatusIsCode(findReplaceLogic, StatusCode.NO_MATCH);
	}

	@Test
	public void testSearchInScopeBeginsSearchInScope() {
		int lineSeparatorLength= lineSeparator().length();
		TextViewer textViewer= setupTextViewer(LINE_STRING + lineSeparator() + LINE_STRING + lineSeparator() + LINE_STRING);
		textViewer.setSelectedRange(LINE_STRING_LENGTH + lineSeparatorLength, 2 * LINE_STRING_LENGTH + lineSeparatorLength);
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.deactivate(SearchOptions.GLOBAL);
		findReplaceLogic.setFindString(LINE_STRING);
		findReplaceLogic.performSearch();
		expectStatusEmpty(findReplaceLogic);
		assertThat(findReplaceLogic.getTarget().getSelection().x, not(is(0)));
		assertThat(findReplaceLogic.getTarget().getSelection().x, not(is(textViewer.getDocument().get().length() - LINE_STRING_LENGTH)));
	}

	/**
	 * The TextViewer implementation of IFindReplaceLogic is misleading and not adhering to the
	 * Interface: IFindReplaceTargetExtension3#replaceSelection will NOT replace the selection if
	 * nothing was previously found. This does not generally have to be the case and thus we mock a
	 * target that is setup like this:
	 *
	 * The text contained is {@code ~SELECTEDTEXT~abcd} with ~ marking the boundaries of the
	 * selection. We perform a search for "NOTFOUND" first - the selection stays put since the
	 * string was not found. At this point, we do not want to perform
	 * {@code replaceSelection("NOTFOUND")} since an implementation adhering to the specification of
	 * the function would just overwrite the current selection.
	 */
	@Test
	public void onlySelectAndReplacesIfFindSuccessfulOnCustomTarget() {
		IFindReplaceTarget mockedTarget= Mockito.mock(IFindReplaceTarget.class, withSettings().extraInterfaces(IFindReplaceTargetExtension3.class, IFindReplaceTargetExtension2.class));

		when(mockedTarget.getSelectionText()).thenReturn("SELECTEDTEXT");
		when(mockedTarget.getSelection()).thenReturn(new Point(0, "SELECTEDTEXT".length()));
		when(mockedTarget.isEditable()).thenReturn(true);
		when(mockedTarget.findAndSelect(anyInt(), anyString(), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(-1);
		when(((IFindReplaceTargetExtension2) mockedTarget).validateTargetState()).thenReturn(true);

		IFindReplaceLogic findReplaceLogic= new FindReplaceLogic();
		findReplaceLogic.updateTarget(mockedTarget, true);
		setFindAndReplaceString(findReplaceLogic, "NOTFOUND", "");
		findReplaceLogic.performSelectAndReplace();

		verify((IFindReplaceTargetExtension3) mockedTarget, never()).replaceSelection(anyString(), anyBoolean());
	}

	@Test
	public void testCanReplaceAfterWrap() {
		TextViewer textViewer= setupTextViewer(LINE_STRING + lineSeparator() + LINE_STRING);
		textViewer.setSelectedRange(LINE_STRING_LENGTH + lineSeparator().length(), 0);
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.activate(SearchOptions.WRAP);
		setFindAndReplaceString(findReplaceLogic, LINE_STRING, "");

		findReplaceLogic.performSelectAndReplace();
		assertThat(textViewer.getTextWidget().getText(), is(LINE_STRING + lineSeparator()));
		findReplaceLogic.performSelectAndReplace();
		assertThat(textViewer.getTextWidget().getText(), is(lineSeparator()));
	}

	@Test
	public void testDontSelectAndReplaceIfFindNotSuccessful() {
		String setupString= "ABCD" + lineSeparator() + LINE_STRING;
		TextViewer textViewer= setupTextViewer(setupString);
		textViewer.setSelectedRange(0, 4);
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.activate(SearchOptions.WRAP);
		setFindAndReplaceString(findReplaceLogic, "NOTFOUND", "");
		findReplaceLogic.performSelectAndReplace();
		// ensure nothing was replaced
		assertThat(textViewer.getTextWidget().getText(), is(setupString));
		// ensure the selection was not overridden
		assertThat(findReplaceLogic.getTarget().getSelection().x, is(0));
		assertThat(findReplaceLogic.getTarget().getSelection().y, is(4));
	}

	@Test
	public void testResetIncrementalBaseLocation() {
		String setupString= "test\ntest\ntest";
		TextViewer textViewer= setupTextViewer(setupString);
		textViewer.setSelectedRange(0, 0);
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		findReplaceLogic.activate(SearchOptions.WRAP);
		findReplaceLogic.activate(SearchOptions.INCREMENTAL);

		findReplaceLogic.setFindString("test");
		assertThat(textViewer.getSelectedRange(), is(new Point(0, 4)));
		textViewer.setSelectedRange(5, 0);
		findReplaceLogic.resetIncrementalBaseLocation();
		findReplaceLogic.performSearch();
		assertThat(textViewer.getSelectedRange(), is(new Point(5, 4)));
	}

	@Test
	public void testSetFindString_incrementalInactive() {
		TextViewer textViewer= setupTextViewer("Test Test Test Test");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);

		assertEquals(new Point(0, 0), findReplaceLogic.getTarget().getSelection());
		findReplaceLogic.setFindString("Test");
		assertEquals(new Point(0, 0), findReplaceLogic.getTarget().getSelection());
	}

	@Test
	public void testSetFindString_incrementalActive() {
		TextViewer textViewer= setupTextViewer("Test Test Test Test");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.INCREMENTAL);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		assertEquals(new Point(0, 0), findReplaceLogic.getTarget().getSelection());

		findReplaceLogic.setFindString("Test");
		assertEquals(new Point(0, 4), findReplaceLogic.getTarget().getSelection());

		findReplaceLogic.setFindString("Test"); // incremental search is idempotent
		assertEquals(new Point(0, 4), findReplaceLogic.getTarget().getSelection());

		findReplaceLogic.setFindString("T");
		assertEquals(new Point(0, 1), findReplaceLogic.getTarget().getSelection());

		findReplaceLogic.setFindString("Te");
		assertEquals(new Point(0, 2), findReplaceLogic.getTarget().getSelection());

		findReplaceLogic.setFindString(""); // this clears the incremental search, but the "old search" still remains active
		assertEquals(new Point(0, 2), findReplaceLogic.getTarget().getSelection());
	}

	@Test
	public void testIncrementBaseLocationWithRegEx() {
		TextViewer textViewer= setupTextViewer("Test Test Test Test Test");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.INCREMENTAL);
		findReplaceLogic.activate(SearchOptions.FORWARD);

		findReplaceLogic.setFindString("Test");
		assertThat(findReplaceLogic.getTarget().getSelection(), is(new Point(0, 4)));

		findReplaceLogic.activate(SearchOptions.REGEX);
		findReplaceLogic.deactivate(SearchOptions.INCREMENTAL);
		findReplaceLogic.performSearch();
		findReplaceLogic.activate(SearchOptions.INCREMENTAL);
		assertThat(findReplaceLogic.getTarget().getSelection(), is(new Point(5, 4)));
		findReplaceLogic.deactivate(SearchOptions.INCREMENTAL);
		findReplaceLogic.performSearch();
		findReplaceLogic.activate(SearchOptions.INCREMENTAL);
		assertThat(findReplaceLogic.getTarget().getSelection(), is(new Point(10, 4)));
		findReplaceLogic.deactivate(SearchOptions.REGEX);

		findReplaceLogic.setFindString("Test");
		assertThat(findReplaceLogic.getTarget().getSelection(), is(new Point(10, 4)));
		findReplaceLogic.performSearch();
		assertThat(findReplaceLogic.getTarget().getSelection(), is(new Point(15, 4)));
	}

	@Test
	public void testIncrementalSearchNoUpdateIfAlreadyOnWord() {
		TextViewer textViewer= setupTextViewer("hellohello");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.activate(SearchOptions.FORWARD);
		textViewer.setSelectedRange(0, 4);
		findReplaceLogic.activate(SearchOptions.INCREMENTAL);
		textViewer.setSelectedRange(0, 0);
		findReplaceLogic.setFindString("hello");
		assertThat(findReplaceLogic.getTarget().getSelection(), is(new Point(0, 5)));
	}

	@Test
	public void testIncrementalSearchBackwardNoUpdateIfAlreadyOnWord() {
		TextViewer textViewer= setupTextViewer("hellohello");
		IFindReplaceLogic findReplaceLogic= setupFindReplaceLogicObject(textViewer);
		findReplaceLogic.deactivate(SearchOptions.FORWARD);
		textViewer.setSelectedRange(5, 5);
		findReplaceLogic.activate(SearchOptions.INCREMENTAL);
		textViewer.setSelectedRange(5, 0);
		findReplaceLogic.setFindString("hello");
		assertThat(findReplaceLogic.getTarget().getSelection(), is(new Point(5, 5)));
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
