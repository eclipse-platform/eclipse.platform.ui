/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.TextStyle;

import org.eclipse.jface.viewers.StyledStringBuilder;
import org.eclipse.jface.viewers.StyledStringBuilder.Styler;

public class StyledStringBuilderTest extends TestCase {
	
	public static class TestStyler extends Styler {
		
		public final int borderStyle;

		public TestStyler(int borderStyle) {
			this.borderStyle= borderStyle;
		}
		
		public void applyStyles(TextStyle textStyle) {
			textStyle.borderStyle= borderStyle;
		}
	}
	
	public static final TestStyler STYLER1= new TestStyler(SWT.BORDER_DOT);
	public static final TestStyler STYLER2= new TestStyler(SWT.BORDER_DASH);
	
	
	public static Test allTests() {
		return new TestSuite(StyledStringBuilderTest.class);
	}

	public static Test suite() {
		return allTests();
	}
	
	public void testEmpty() {
		StyledStringBuilder styledString= new StyledStringBuilder();
		
		String str= "";
		
		assertEquals(str.length(), styledString.length());
		assertEquals(str, styledString.toString());
		assertEquals(styledString.toStyleRanges().length, 0);
	}
	
	public void testAppendString1() {
		StyledStringBuilder styledString= new StyledStringBuilder();
		
		String str= "Hello";
		
		styledString.append(str, STYLER1);
		
		assertEquals(str.length(), styledString.length());
		assertEquals(str, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(1, styleRanges.length);
		
		assertEquals(styleRanges[0], STYLER1, 0, str.length());
	}
	
	public void testAppendString2() {
		StyledStringBuilder styledString= new StyledStringBuilder();
		
		String str1= "Hello";
		String str2= "You";
		styledString.append(str1);
		styledString.append(str2, STYLER1);
		
		String res= str1 + str2;
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(1, styleRanges.length);
		assertEquals(styleRanges[0], STYLER1, str1.length(), str2.length());
	}
	
	public void testAppendString3() {
		StyledStringBuilder styledString= new StyledStringBuilder();
		
		String str1= "Hello";
		String str2= "You";
		styledString.append(str1, STYLER1);
		styledString.append(str2);
		
		String res= str1 + str2;
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(1, styleRanges.length);
		assertEquals(styleRanges[0], STYLER1, 0, str1.length());
	}
	
	public void testAppendString4() {
		StyledStringBuilder styledString= new StyledStringBuilder();
		
		String str1= "Hello";
		String str2= "You";
		styledString.append(str1);
		styledString.append(str2, STYLER1);
		styledString.append(str2, STYLER1);
		
		String res= str1 + str2 + str2;
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(1, styleRanges.length);
		assertEquals(styleRanges[0], STYLER1, str1.length(), str2.length() * 2);
	}
	
	public void testAppendString5() {
		StyledStringBuilder styledString= new StyledStringBuilder();
		
		String str1= "Hello";
		String str2= "You";
		String str3= "Me";
		styledString.append(str1);
		styledString.append(str2, STYLER1);
		styledString.append(str3, STYLER2);
		
		String res= str1 + str2 + str3;
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(2, styleRanges.length);
		assertEquals(styleRanges[0], STYLER1, str1.length(), str2.length());
		assertEquals(styleRanges[1], STYLER2, str1.length() + str2.length(), str3.length());
	}
	
	public void testAppendString6() {
		StyledStringBuilder styledString= new StyledStringBuilder();
		
		String str1= "Hello";
		String str2= "You";
		String str3= "Me";
		styledString.append(str1, STYLER1);
		styledString.append(str2);
		styledString.append(str3, STYLER2);
		
		String res= str1 + str2 + str3;
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(2, styleRanges.length);
		assertEquals(styleRanges[0], STYLER1, 0, str1.length());
		assertEquals(styleRanges[1], STYLER2, str1.length() + str2.length(), str3.length());
	}
	
	public void testAppendString7() {
		StyledStringBuilder styledString= new StyledStringBuilder();
		
		String str1= "Hello";
		String str2= "";
		String str3= "Me";
		styledString.append(str1, STYLER1);
		styledString.append(str2, STYLER2);
		styledString.append(str3, STYLER1);
		
		String res= str1 + str2 + str3;
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(1, styleRanges.length);
		assertEquals(styleRanges[0], STYLER1, 0, res.length());
	}
	
	public void testAppendChar1() {
		StyledStringBuilder styledString= new StyledStringBuilder();
		
		styledString.append('H', STYLER1);
		styledString.append('2', STYLER2);
		styledString.append('O', STYLER1);
		
		String res= "H2O";
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(3, styleRanges.length);
		assertEquals(styleRanges[0], STYLER1, 0, 1);
		assertEquals(styleRanges[1], STYLER2, 1, 1);
		assertEquals(styleRanges[2], STYLER1, 2, 1);
	}
	
	public void testAppendChar2() {
		StyledStringBuilder styledString= new StyledStringBuilder();
		
		styledString.append('H', STYLER1);
		styledString.append('2');
		styledString.append('O', STYLER2);
		
		String res= "H2O";
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(2, styleRanges.length);
		assertEquals(styleRanges[0], STYLER1, 0, 1);
		assertEquals(styleRanges[1], STYLER2, 2, 1);
	}
	
	public void testAppendStyledString1() {
		StyledStringBuilder other= new StyledStringBuilder();
		
		String str2= "You";
		String str3= "Me";
		other.append(str2, STYLER1);
		other.append(str3, STYLER2);
		
		String str1= "We";
		
		StyledStringBuilder styledString= new StyledStringBuilder(str1);
		styledString.append(other);
		
		String res= str1 + str2 + str3;
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(2, styleRanges.length);
		assertEquals(styleRanges[0], STYLER1, str1.length(), str2.length());
		assertEquals(styleRanges[1], STYLER2, str1.length() + str2.length(), str3.length());
	}
	
	public void testAppendStyledString2() {
		StyledStringBuilder other= new StyledStringBuilder();
		
		String str2= "You";
		String str3= "Me";
		other.append(str2, STYLER1);
		other.append(str3, STYLER2);
		
		String str1= "We";
		
		StyledStringBuilder styledString= new StyledStringBuilder(str1, STYLER1);
		styledString.append(other);
		
		String res= str1 + str2 + str3;
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(2, styleRanges.length);
		assertEquals(styleRanges[0], STYLER1, 0, str1.length() + str2.length());
		assertEquals(styleRanges[1], STYLER2, str1.length() + str2.length(), str3.length());
	}
	
	public void testAppendStyledString3() {
		
		StyledStringBuilder other= new StyledStringBuilder();
		
		String str2= "You";
		String str3= "Me";
		other.append(str2);
		other.append(str3, STYLER2);
		
		String str1= "We";
		
		StyledStringBuilder styledString= new StyledStringBuilder(str1, STYLER1);
		styledString.append(other);
		
		String res= str1 + str2 + str3;
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(2, styleRanges.length);
		assertEquals(styleRanges[0], STYLER1, 0, str1.length());
		assertEquals(styleRanges[1], STYLER2, str1.length() + str2.length(), str3.length());
	}
	
	public void testAppendStyledString4() {
		
		StyledStringBuilder other= new StyledStringBuilder();
		
		String str2= "You";
		String str3= "Me";
		other.append(str2, STYLER2);
		other.append(str3);
		
		String str1= "We";
		
		StyledStringBuilder styledString= new StyledStringBuilder(str1, STYLER1);
		styledString.append(other);
		
		String res= str1 + str2 + str3;
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(2, styleRanges.length);
		assertEquals(styleRanges[0], STYLER1, 0, str1.length());
		assertEquals(styleRanges[1], STYLER2, str1.length(), str2.length());
	}
	
	public void testAppendStyledString5() {
		StyledStringBuilder other= new StyledStringBuilder();
		
		String str2= "You";
		String str3= "Me";
		other.append(str2);
		other.append(str3, STYLER1);
		
		String str1= "We";
		
		StyledStringBuilder styledString= new StyledStringBuilder(str1);
		styledString.append(other);
		
		String res= str1 + str2 + str3;
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(1, styleRanges.length);
		assertEquals(styleRanges[0], STYLER1, str1.length() +  str2.length(), str3.length());
	}
	
	public void testSetStyle1() {
		String str1= "One";
		String str2= "Two";
		String str3= "Three";
		
		String res= str1 + str2 + str3;
		
		StyledStringBuilder styledString= new StyledStringBuilder();
		styledString.append(res);
		
		styledString.setStyle(0, str1.length(), STYLER1);
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(1, styleRanges.length);
		assertEquals(styleRanges[0], STYLER1, 0, str1.length());
	}
	
	public void testSetStyle2() {
		
		String str1= "One";
		String str2= "Two";
		String str3= "Three";
		
		String res= str1 + str2 + str3;
		
		StyledStringBuilder styledString= new StyledStringBuilder();
		styledString.append(res);
		
		styledString.setStyle(str1.length(), str2.length(), STYLER1);
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(1, styleRanges.length);
		assertEquals(styleRanges[0], STYLER1, str1.length(), str2.length());
	}
	
	public void testSetStyle3() {
		
		String str1= "One";
		String str2= "Two";
		String str3= "Three";
		
		String res= str1 + str2 + str3;
		
		StyledStringBuilder styledString= new StyledStringBuilder();
		styledString.append(res);
		
		styledString.setStyle(str1.length(), res.length() - str1.length(), STYLER1);
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(1, styleRanges.length);
		assertEquals(styleRanges[0], STYLER1, str1.length(), res.length() - str1.length());
	}
	
	public void testSetStyle4() {
		
		String str1= "One";
		String str2= "Two";
		String str3= "Three";
		
		String res= str1 + str2 + str3;
		
		StyledStringBuilder styledString= new StyledStringBuilder();
		styledString.append(res);
		
		styledString.setStyle(0, res.length(), STYLER1);
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(1, styleRanges.length);
		assertEquals(styleRanges[0], STYLER1, 0, res.length());
	}
	
	public void testSetStyle5() {
		
		String str1= "One";
		String str2= "Two";
		String str3= "Three";
		
		String res= str1 + str2 + str3;
		
		StyledStringBuilder styledString= new StyledStringBuilder();
		styledString.append(res);
		
		styledString.setStyle(0, res.length(), null);
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(0, styleRanges.length);
	}
	
	public void testSetStyle6() {
		
		String str1= "One";
		String str2= "Two";
		
		String res= str1 + str2;
		
		StyledStringBuilder styledString= new StyledStringBuilder(str1, STYLER1);
		styledString.append(str2);
		
		styledString.setStyle(str1.length(), str2.length(), STYLER2);
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(2, styleRanges.length);
		assertEquals(styleRanges[0], STYLER1, 0, str1.length());
		assertEquals(styleRanges[1], STYLER2, str1.length(), str2.length());
	}
	
	public void testSetStyle7() {
		
		String str1= "One";
		String str2= "Two";
		
		String res= str1 + str2;
		
		StyledStringBuilder styledString= new StyledStringBuilder(str1);
		styledString.append(str2, STYLER1);
		
		styledString.setStyle(0, str1.length(), STYLER2);
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(2, styleRanges.length);
		assertEquals(styleRanges[0], STYLER2, 0, str1.length());
		assertEquals(styleRanges[1], STYLER1, str1.length(), str2.length());
	}
	
	public void testSetStyle8() {
		
		String str1= "One";
		String str2= "Two";
		
		String res= str1 + str2;
		
		StyledStringBuilder styledString= new StyledStringBuilder();
		styledString.append(str1, STYLER1);
		styledString.append(str2, STYLER2);
		
		styledString.setStyle(0, str1.length(), STYLER2);
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(1, styleRanges.length);
		assertEquals(styleRanges[0], STYLER2, 0, res.length());
	}
	
	public void testSetStyle9() {
		
		String str1= "One";
		String str2= "Two";
		
		String res= str1 + str2;
		
		StyledStringBuilder styledString= new StyledStringBuilder();
		styledString.append(str1, STYLER1);
		styledString.append(str2, STYLER2);
		
		styledString.setStyle(0, res.length(), null);
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(0, styleRanges.length);
	}
	
	public void testSetStyle10() {
		
		String str1= "One";
		String str2= "Two";
		
		String res= str1 + str2;
		
		StyledStringBuilder styledString= new StyledStringBuilder();
		styledString.append(str1, STYLER1);
		styledString.append(str2, STYLER2);
		
		styledString.setStyle(1, res.length() - 2, null);
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(2, styleRanges.length);
		
		assertEquals(styleRanges[0], STYLER1, 0, 1);
		assertEquals(styleRanges[1], STYLER2, res.length() - 1, 1);
	}
	
	public void testSetStyle11() {
		
		String str1= "One";
		String str2= "Two";
		
		String res= str1 + str2;
		
		StyledStringBuilder styledString= new StyledStringBuilder();
		styledString.append(str1, STYLER1);
		styledString.append(str2, STYLER2);
		
		styledString.setStyle(1, res.length() - 1, STYLER1);
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(1, styleRanges.length);
		
		assertEquals(styleRanges[0], STYLER1, 0, res.length());
	}
	
	public void testSetStyle12() {
		
		String str1= "One";
		String str2= "Two";
		
		String res= str1 + str2;
		
		StyledStringBuilder styledString= new StyledStringBuilder();
		styledString.append(str1, STYLER1);
		styledString.append(str2, STYLER2);
		
		styledString.setStyle(0, res.length() - 1, STYLER2);
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(1, styleRanges.length);
		
		assertEquals(styleRanges[0], STYLER2, 0, res.length());
	}
	
	public void testSetStyle13() {
		
		String str1= "One";
		String str2= "Two";
		
		String res= str1 + str2;
		
		StyledStringBuilder styledString= new StyledStringBuilder();
		styledString.append(str1, STYLER1);
		styledString.append(str2, STYLER2);
		
		styledString.setStyle(1, res.length() - 2, STYLER1);
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(2, styleRanges.length);
		
		assertEquals(styleRanges[0], STYLER1, 0, res.length() - 1);
		assertEquals(styleRanges[1], STYLER2, res.length() - 1, 1);
	}
	
	public void testSetStyle14() {
		
		String str1= "One";
		String str2= "Two";
		
		String res= str1 + str2;
		
		StyledStringBuilder styledString= new StyledStringBuilder();
		styledString.append(str1, STYLER1);
		styledString.append(str2, STYLER2);
		
		styledString.setStyle(1, res.length() - 2, STYLER2);
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(2, styleRanges.length);
		
		assertEquals(styleRanges[0], STYLER1, 0, 1);
		assertEquals(styleRanges[1], STYLER2, 1, res.length() - 1);
	}
	
	public void testSetStyle15() {
		
		String str1= "One";
		String str2= "Two";
		
		String res= str1 + str2;
		
		StyledStringBuilder styledString= new StyledStringBuilder();
		styledString.append(str1, null);
		styledString.append(str2, STYLER2);
		
		styledString.setStyle(0, 1, STYLER1);
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(2, styleRanges.length);
		
		assertEquals(styleRanges[0], STYLER1, 0, 1);
		assertEquals(styleRanges[1], STYLER2, str1.length(), str2.length());
	}
	
	public void testSetStyle16() {
				
		String res= "H2O";
		
		StyledStringBuilder styledString= new StyledStringBuilder();
		styledString.append('H', null);
		styledString.append('2', STYLER1);
		styledString.append('O', STYLER2);
		
		styledString.setStyle(0, res.length(), STYLER1);
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(1, styleRanges.length);
		
		assertEquals(styleRanges[0], STYLER1, 0, res.length());
	}
	
	public void testSetStyle17() {
		
		String res= "H2O";
		
		StyledStringBuilder styledString= new StyledStringBuilder();
		styledString.append('H', null);
		styledString.append('2', STYLER1);
		styledString.append('O', STYLER2);
		
		styledString.setStyle(0, res.length(), null);
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(0, styleRanges.length);
	}
	
	public void testSetStyle18() {
		String res= "H2OH2O";
		
		StyledStringBuilder styledString= new StyledStringBuilder();
		styledString.append('H', null);
		styledString.append('2', STYLER1);
		styledString.append('O', STYLER2);
		styledString.append('H', null);
		styledString.append('2', STYLER2);
		styledString.append('O', STYLER1);
		
		styledString.setStyle(1, res.length() - 2, STYLER1);
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(1, styleRanges.length);
		
		assertEquals(styleRanges[0], STYLER1, 1, res.length() - 1);
	}
	
	public void testSetStyle19() {
		String res= "O2O2O2O2O2O2";
		
		StyledStringBuilder styledString= new StyledStringBuilder();
		styledString.append("O2", null);
		styledString.append("O2", STYLER1);
		styledString.append("O2", STYLER2);
		styledString.append("O2", STYLER1);
		styledString.append("O2", STYLER2);
		styledString.append("O2", null);
		
		styledString.setStyle(1, res.length() - 2, STYLER1);
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(1, styleRanges.length);
		
		assertEquals(styleRanges[0], STYLER1, 1, res.length() - 2);
	}
	
	public void testSetStyle20() {
		String res= "O2O2O2O2O2O2";
		
		StyledStringBuilder styledString= new StyledStringBuilder();
		styledString.append("O2", null);
		styledString.append("O2", STYLER1);
		styledString.append("O2", STYLER2);
		styledString.append("O2", STYLER1);
		styledString.append("O2", STYLER2);
		styledString.append("O2", null);
		
		styledString.setStyle(3, 6, null);
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(2, styleRanges.length);
		
		assertEquals(styleRanges[0], STYLER1, 2, 1);
		assertEquals(styleRanges[1], STYLER2, 9, 1);
	}
	
	public void testSetStyle21() {
		String res= "O2O2O2O2O2O2";
		
		StyledStringBuilder styledString= new StyledStringBuilder();
		styledString.append("O2", null);
		styledString.append("O2", STYLER1);
		styledString.append("O2", STYLER2);
		styledString.append("O2", STYLER1);
		styledString.append("O2", STYLER2);
		styledString.append("O2", null);
		
		styledString.setStyle(3, 6, STYLER1);
		styledString.setStyle(3, 6, null);
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(2, styleRanges.length);
		
		assertEquals(styleRanges[0], STYLER1, 2, 1);
		assertEquals(styleRanges[1], STYLER2, 9, 1);
	}
	
	public void testCombination1() {
		String str1= "One";
		String str2= "Two";
		
		String res= str1 + str2 + str1;
		
		StyledStringBuilder styledString= new StyledStringBuilder();
		styledString.append(str1, null);
		styledString.append(str2, STYLER2);
		
		styledString.setStyle(str1.length(), str2.length(), STYLER1);
		
		styledString.append(str1, STYLER1);
		
		assertEquals(res.length(), styledString.length());
		assertEquals(res, styledString.toString());
		StyleRange[] styleRanges= styledString.toStyleRanges();
		assertEquals(1, styleRanges.length);
		
		assertEquals(styleRanges[0], STYLER1, str1.length(), str2.length() + str1.length());
	}
	
	
	private void assertEquals(StyleRange range, TestStyler style, int offset, int length) {
		assertEquals(offset, range.start);
		assertEquals(length, range.length);
		assertEquals(style.borderStyle, range.borderStyle);
	}
	
	
}
