/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.TextStyle;
import org.junit.Test;

public class StyledStringBuilderTest {

	public static class TestStyler extends Styler {

		public final int borderStyle;

		public TestStyler(int borderStyle) {
			this.borderStyle = borderStyle;
		}

		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.borderStyle = borderStyle;
		}
	}

	public static final TestStyler STYLER1 = new TestStyler(SWT.BORDER_DOT);
	public static final TestStyler STYLER2 = new TestStyler(SWT.BORDER_DASH);

	@Test
	public void testEmpty() {
		StyledString styledString = new StyledString();

		String str = "";

		assertEquals(str.length(), styledString.length());
		assertEquals(str, styledString.getString());
		assertEquals(styledString.getStyleRanges().length, 0);
	}

	@Test
	public void testAppendString1() {
		StyledString styledString = new StyledString();

		String str = "Hello";

		styledString.append(str, STYLER1);

		assertEquals(str.length(), styledString.length());
		assertEquals(str, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(1, styleRanges.length);

		assertRangeEquals(styleRanges[0], STYLER1, 0, str.length());
	}

	@Test
	public void testAppendString2() {
		StyledString styledString = new StyledString();

		String str1 = "Hello";
		String str2 = "You";
		styledString.append(str1);
		styledString.append(str2, STYLER1);

		String res = str1 + str2;

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(1, styleRanges.length);
		assertRangeEquals(styleRanges[0], STYLER1, str1.length(), str2.length());
	}

	@Test
	public void testAppendString3() {
		StyledString styledString = new StyledString();

		String str1 = "Hello";
		String str2 = "You";
		styledString.append(str1, STYLER1);
		styledString.append(str2);

		String res = str1 + str2;

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(1, styleRanges.length);
		assertRangeEquals(styleRanges[0], STYLER1, 0, str1.length());
	}

	@Test
	public void testAppendString4() {
		StyledString styledString = new StyledString();

		String str1 = "Hello";
		String str2 = "You";
		styledString.append(str1);
		styledString.append(str2, STYLER1);
		styledString.append(str2, STYLER1);

		String res = str1 + str2 + str2;

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(1, styleRanges.length);
		assertRangeEquals(styleRanges[0], STYLER1, str1.length(), str2.length() * 2);
	}

	@Test
	public void testAppendString5() {
		StyledString styledString = new StyledString();

		String str1 = "Hello";
		String str2 = "You";
		String str3 = "Me";
		styledString.append(str1);
		styledString.append(str2, STYLER1);
		styledString.append(str3, STYLER2);

		String res = str1 + str2 + str3;

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(2, styleRanges.length);
		assertRangeEquals(styleRanges[0], STYLER1, str1.length(), str2.length());
		assertRangeEquals(styleRanges[1], STYLER2, str1.length() + str2.length(), str3.length());
	}

	@Test
	public void testAppendString6() {
		StyledString styledString = new StyledString();

		String str1 = "Hello";
		String str2 = "You";
		String str3 = "Me";
		styledString.append(str1, STYLER1);
		styledString.append(str2);
		styledString.append(str3, STYLER2);

		String res = str1 + str2 + str3;

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(2, styleRanges.length);
		assertRangeEquals(styleRanges[0], STYLER1, 0, str1.length());
		assertRangeEquals(styleRanges[1], STYLER2, str1.length() + str2.length(), str3.length());
	}

	@Test
	public void testAppendString7() {
		StyledString styledString = new StyledString();

		String str1 = "Hello";
		String str2 = "";
		String str3 = "Me";
		styledString.append(str1, STYLER1);
		styledString.append(str2, STYLER2);
		styledString.append(str3, STYLER1);

		String res = str1 + str2 + str3;

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(1, styleRanges.length);
		assertRangeEquals(styleRanges[0], STYLER1, 0, res.length());
	}

	@Test
	public void testAppendChar1() {
		StyledString styledString = new StyledString();

		styledString.append('H', STYLER1);
		styledString.append('2', STYLER2);
		styledString.append('O', STYLER1);

		String res = "H2O";

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(3, styleRanges.length);
		assertRangeEquals(styleRanges[0], STYLER1, 0, 1);
		assertRangeEquals(styleRanges[1], STYLER2, 1, 1);
		assertRangeEquals(styleRanges[2], STYLER1, 2, 1);
	}

	@Test
	public void testAppendChar2() {
		StyledString styledString = new StyledString();

		styledString.append('H', STYLER1);
		styledString.append('2');
		styledString.append('O', STYLER2);

		String res = "H2O";

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(2, styleRanges.length);
		assertRangeEquals(styleRanges[0], STYLER1, 0, 1);
		assertRangeEquals(styleRanges[1], STYLER2, 2, 1);
	}

	@Test
	public void testAppendStyledString1() {
		StyledString other = new StyledString();

		String str2 = "You";
		String str3 = "Me";
		other.append(str2, STYLER1);
		other.append(str3, STYLER2);

		String str1 = "We";

		StyledString styledString = new StyledString(str1);
		styledString.append(other);

		String res = str1 + str2 + str3;

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(2, styleRanges.length);
		assertRangeEquals(styleRanges[0], STYLER1, str1.length(), str2.length());
		assertRangeEquals(styleRanges[1], STYLER2, str1.length() + str2.length(), str3.length());
	}

	@Test
	public void testAppendStyledString2() {
		StyledString other = new StyledString();

		String str2 = "You";
		String str3 = "Me";
		other.append(str2, STYLER1);
		other.append(str3, STYLER2);

		String str1 = "We";

		StyledString styledString = new StyledString(str1, STYLER1);
		styledString.append(other);

		String res = str1 + str2 + str3;

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(2, styleRanges.length);
		assertRangeEquals(styleRanges[0], STYLER1, 0, str1.length() + str2.length());
		assertRangeEquals(styleRanges[1], STYLER2, str1.length() + str2.length(), str3.length());
	}

	@Test
	public void testAppendStyledString3() {

		StyledString other = new StyledString();

		String str2 = "You";
		String str3 = "Me";
		other.append(str2);
		other.append(str3, STYLER2);

		String str1 = "We";

		StyledString styledString = new StyledString(str1, STYLER1);
		styledString.append(other);

		String res = str1 + str2 + str3;

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(2, styleRanges.length);
		assertRangeEquals(styleRanges[0], STYLER1, 0, str1.length());
		assertRangeEquals(styleRanges[1], STYLER2, str1.length() + str2.length(), str3.length());
	}

	@Test
	public void testAppendStyledString4() {

		StyledString other = new StyledString();

		String str2 = "You";
		String str3 = "Me";
		other.append(str2, STYLER2);
		other.append(str3);

		String str1 = "We";

		StyledString styledString = new StyledString(str1, STYLER1);
		styledString.append(other);

		String res = str1 + str2 + str3;

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(2, styleRanges.length);
		assertRangeEquals(styleRanges[0], STYLER1, 0, str1.length());
		assertRangeEquals(styleRanges[1], STYLER2, str1.length(), str2.length());
	}

	@Test
	public void testAppendStyledString5() {
		StyledString other = new StyledString();

		String str2 = "You";
		String str3 = "Me";
		other.append(str2);
		other.append(str3, STYLER1);

		String str1 = "We";

		StyledString styledString = new StyledString(str1);
		styledString.append(other);

		String res = str1 + str2 + str3;

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(1, styleRanges.length);
		assertRangeEquals(styleRanges[0], STYLER1, str1.length() + str2.length(), str3.length());
	}

	@Test
	public void testSetStyle1() {
		String str1 = "One";
		String str2 = "Two";
		String str3 = "Three";

		String res = str1 + str2 + str3;

		StyledString styledString = new StyledString();
		styledString.append(res);

		styledString.setStyle(0, str1.length(), STYLER1);

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(1, styleRanges.length);
		assertRangeEquals(styleRanges[0], STYLER1, 0, str1.length());
	}

	@Test
	public void testSetStyle2() {

		String str1 = "One";
		String str2 = "Two";
		String str3 = "Three";

		String res = str1 + str2 + str3;

		StyledString styledString = new StyledString();
		styledString.append(res);

		styledString.setStyle(str1.length(), str2.length(), STYLER1);

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(1, styleRanges.length);
		assertRangeEquals(styleRanges[0], STYLER1, str1.length(), str2.length());
	}

	@Test
	public void testSetStyle3() {

		String str1 = "One";
		String str2 = "Two";
		String str3 = "Three";

		String res = str1 + str2 + str3;

		StyledString styledString = new StyledString();
		styledString.append(res);

		styledString.setStyle(str1.length(), res.length() - str1.length(), STYLER1);

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(1, styleRanges.length);
		assertRangeEquals(styleRanges[0], STYLER1, str1.length(), res.length() - str1.length());
	}

	@Test
	public void testSetStyle4() {

		String str1 = "One";
		String str2 = "Two";
		String str3 = "Three";

		String res = str1 + str2 + str3;

		StyledString styledString = new StyledString();
		styledString.append(res);

		styledString.setStyle(0, res.length(), STYLER1);

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(1, styleRanges.length);
		assertRangeEquals(styleRanges[0], STYLER1, 0, res.length());
	}

	@Test
	public void testSetStyle5() {

		String str1 = "One";
		String str2 = "Two";
		String str3 = "Three";

		String res = str1 + str2 + str3;

		StyledString styledString = new StyledString();
		styledString.append(res);

		styledString.setStyle(0, res.length(), null);

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(0, styleRanges.length);
	}

	@Test
	public void testSetStyle6() {

		String str1 = "One";
		String str2 = "Two";

		String res = str1 + str2;

		StyledString styledString = new StyledString(str1, STYLER1);
		styledString.append(str2);

		styledString.setStyle(str1.length(), str2.length(), STYLER2);

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(2, styleRanges.length);
		assertRangeEquals(styleRanges[0], STYLER1, 0, str1.length());
		assertRangeEquals(styleRanges[1], STYLER2, str1.length(), str2.length());
	}

	@Test
	public void testSetStyleInsert() {

		String str1 = "One";
		String str2 = "Two";
		String str3 = "Three";

		String res = str1 + str2 + str3;

		StyledString styledString = new StyledString(str1);
		styledString.append(str2 + str3, STYLER1);

		styledString.setStyle(str1.length() + 1, str2.length(), STYLER2);

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());

		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(3, styleRanges.length);

		assertRangeEquals(styleRanges[0], STYLER1, str1.length(), 1);
		assertRangeEquals(styleRanges[1], STYLER2, str1.length() + 1, str2.length());
		assertRangeEquals(styleRanges[2], STYLER1, str1.length() + str2.length() + 1, str3.length() - 1);
	}

	@Test
	public void testSetStyleInsert2() {

		String str1 = "one";
		String str2 = "two";

		String res = str1 + str2;

		StyledString styledString = new StyledString(res);

		styledString.setStyle(0, str1.length(), STYLER1);
		styledString.setStyle(str1.length(), 1, STYLER1);

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());

		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(1, styleRanges.length);

		assertRangeEquals(styleRanges[0], STYLER1, 0, str1.length() + 1);
	}

	@Test
	public void testSetStyle7() {

		String str1 = "One";
		String str2 = "Two";

		String res = str1 + str2;

		StyledString styledString = new StyledString(str1);
		styledString.append(str2, STYLER1);

		styledString.setStyle(0, str1.length(), STYLER2);

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(2, styleRanges.length);
		assertRangeEquals(styleRanges[0], STYLER2, 0, str1.length());
		assertRangeEquals(styleRanges[1], STYLER1, str1.length(), str2.length());
	}

	@Test
	public void testSetStyle8() {

		String str1 = "One";
		String str2 = "Two";

		String res = str1 + str2;

		StyledString styledString = new StyledString();
		styledString.append(str1, STYLER1);
		styledString.append(str2, STYLER2);

		styledString.setStyle(0, str1.length(), STYLER2);

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(1, styleRanges.length);
		assertRangeEquals(styleRanges[0], STYLER2, 0, res.length());
	}

	@Test
	public void testSetStyle9() {

		String str1 = "One";
		String str2 = "Two";

		String res = str1 + str2;

		StyledString styledString = new StyledString();
		styledString.append(str1, STYLER1);
		styledString.append(str2, STYLER2);

		styledString.setStyle(0, res.length(), null);

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(0, styleRanges.length);
	}

	@Test
	public void testSetStyle10() {

		String str1 = "One";
		String str2 = "Two";

		String res = str1 + str2;

		StyledString styledString = new StyledString();
		styledString.append(str1, STYLER1);
		styledString.append(str2, STYLER2);

		styledString.setStyle(1, res.length() - 2, null);

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(2, styleRanges.length);

		assertRangeEquals(styleRanges[0], STYLER1, 0, 1);
		assertRangeEquals(styleRanges[1], STYLER2, res.length() - 1, 1);
	}

	@Test
	public void testSetStyle11() {

		String str1 = "One";
		String str2 = "Two";

		String res = str1 + str2;

		StyledString styledString = new StyledString();
		styledString.append(str1, STYLER1);
		styledString.append(str2, STYLER2);

		styledString.setStyle(1, res.length() - 1, STYLER1);

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(1, styleRanges.length);

		assertRangeEquals(styleRanges[0], STYLER1, 0, res.length());
	}

	@Test
	public void testSetStyle12() {

		String str1 = "One";
		String str2 = "Two";

		String res = str1 + str2;

		StyledString styledString = new StyledString();
		styledString.append(str1, STYLER1);
		styledString.append(str2, STYLER2);

		styledString.setStyle(0, res.length() - 1, STYLER2);

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(1, styleRanges.length);

		assertRangeEquals(styleRanges[0], STYLER2, 0, res.length());
	}

	@Test
	public void testSetStyle13() {

		String str1 = "One";
		String str2 = "Two";

		String res = str1 + str2;

		StyledString styledString = new StyledString();
		styledString.append(str1, STYLER1);
		styledString.append(str2, STYLER2);

		styledString.setStyle(1, res.length() - 2, STYLER1);

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(2, styleRanges.length);

		assertRangeEquals(styleRanges[0], STYLER1, 0, res.length() - 1);
		assertRangeEquals(styleRanges[1], STYLER2, res.length() - 1, 1);
	}

	@Test
	public void testSetStyle14() {

		String str1 = "One";
		String str2 = "Two";

		String res = str1 + str2;

		StyledString styledString = new StyledString();
		styledString.append(str1, STYLER1);
		styledString.append(str2, STYLER2);

		styledString.setStyle(1, res.length() - 2, STYLER2);

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(2, styleRanges.length);

		assertRangeEquals(styleRanges[0], STYLER1, 0, 1);
		assertRangeEquals(styleRanges[1], STYLER2, 1, res.length() - 1);
	}

	@Test
	public void testSetStyle15() {

		String str1 = "One";
		String str2 = "Two";

		String res = str1 + str2;

		StyledString styledString = new StyledString();
		styledString.append(str1, null);
		styledString.append(str2, STYLER2);

		styledString.setStyle(0, 1, STYLER1);

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(2, styleRanges.length);

		assertRangeEquals(styleRanges[0], STYLER1, 0, 1);
		assertRangeEquals(styleRanges[1], STYLER2, str1.length(), str2.length());
	}

	@Test
	public void testSetStyle16() {

		String res = "H2O";

		StyledString styledString = new StyledString();
		styledString.append('H', null);
		styledString.append('2', STYLER1);
		styledString.append('O', STYLER2);

		styledString.setStyle(0, res.length(), STYLER1);

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(1, styleRanges.length);

		assertRangeEquals(styleRanges[0], STYLER1, 0, res.length());
	}

	@Test
	public void testSetStyle17() {

		String res = "H2O";

		StyledString styledString = new StyledString();
		styledString.append('H', null);
		styledString.append('2', STYLER1);
		styledString.append('O', STYLER2);

		styledString.setStyle(0, res.length(), null);

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(0, styleRanges.length);
	}

	@Test
	public void testSetStyle18() {
		String res = "H2OH2O";

		StyledString styledString = new StyledString();
		styledString.append('H', null);
		styledString.append('2', STYLER1);
		styledString.append('O', STYLER2);
		styledString.append('H', null);
		styledString.append('2', STYLER2);
		styledString.append('O', STYLER1);

		styledString.setStyle(1, res.length() - 2, STYLER1);

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(1, styleRanges.length);

		assertRangeEquals(styleRanges[0], STYLER1, 1, res.length() - 1);
	}

	@Test
	public void testSetStyle19() {
		String res = "O2O2O2O2O2O2";

		StyledString styledString = new StyledString();
		styledString.append("O2", null);
		styledString.append("O2", STYLER1);
		styledString.append("O2", STYLER2);
		styledString.append("O2", STYLER1);
		styledString.append("O2", STYLER2);
		styledString.append("O2", null);

		styledString.setStyle(1, res.length() - 2, STYLER1);

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(1, styleRanges.length);

		assertRangeEquals(styleRanges[0], STYLER1, 1, res.length() - 2);
	}

	@Test
	public void testSetStyle20() {
		String res = "O2O2O2O2O2O2";

		StyledString styledString = new StyledString();
		styledString.append("O2", null);
		styledString.append("O2", STYLER1);
		styledString.append("O2", STYLER2);
		styledString.append("O2", STYLER1);
		styledString.append("O2", STYLER2);
		styledString.append("O2", null);

		styledString.setStyle(3, 6, null);

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(2, styleRanges.length);

		assertRangeEquals(styleRanges[0], STYLER1, 2, 1);
		assertRangeEquals(styleRanges[1], STYLER2, 9, 1);
	}

	@Test
	public void testSetStyle21() {
		String res = "O2O2O2O2O2O2";

		StyledString styledString = new StyledString();
		styledString.append("O2", null);
		styledString.append("O2", STYLER1);
		styledString.append("O2", STYLER2);
		styledString.append("O2", STYLER1);
		styledString.append("O2", STYLER2);
		styledString.append("O2", null);

		styledString.setStyle(3, 6, STYLER1);
		styledString.setStyle(3, 6, null);

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(2, styleRanges.length);

		assertRangeEquals(styleRanges[0], STYLER1, 2, 1);
		assertRangeEquals(styleRanges[1], STYLER2, 9, 1);
	}

	@Test
	public void testCombination1() {
		String str1 = "One";
		String str2 = "Two";

		String res = str1 + str2 + str1;

		StyledString styledString = new StyledString();
		styledString.append(str1, null);
		styledString.append(str2, STYLER2);

		styledString.setStyle(str1.length(), str2.length(), STYLER1);

		styledString.append(str1, STYLER1);

		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.getString());
		StyleRange[] styleRanges = styledString.getStyleRanges();
		assertEquals(1, styleRanges.length);

		assertRangeEquals(styleRanges[0], STYLER1, str1.length(), str2.length() + str1.length());
	}

	private static void assertRangeEquals(StyleRange range, TestStyler style, int offset, int length) {
		assertEquals(offset, range.start);
		assertEquals(length, range.length);
		assertEquals(style.borderStyle, range.borderStyle);
	}

}
