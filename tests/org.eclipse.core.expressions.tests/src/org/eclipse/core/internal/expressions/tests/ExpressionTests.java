/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.expressions.tests;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.expressions.IVariableResolver;
import org.eclipse.core.internal.expressions.AdaptExpression;
import org.eclipse.core.internal.expressions.CountExpression;
import org.eclipse.core.internal.expressions.EqualsExpression;
import org.eclipse.core.internal.expressions.Expressions;
import org.eclipse.core.internal.expressions.InstanceofExpression;
import org.eclipse.core.internal.expressions.IterateExpression;
import org.eclipse.core.internal.expressions.SystemTestExpression;


public class ExpressionTests extends TestCase {

	public static Test suite() {
		return new TestSuite(ExpressionTests.class);
	}
	
	public void testEscape() throws Exception {
		assertEquals("Str'ing", Expressions.unEscapeString("Str''ing")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("'", Expressions.unEscapeString("''")); //$NON-NLS-1$ //$NON-NLS-2$
		boolean caught= false;
		try {
			Expressions.unEscapeString("'"); //$NON-NLS-1$
		} catch (CoreException e) {
			caught= true;
		}
		assertTrue(caught);
	}
	
	public void testArgumentConversion() throws Exception {
		assertNull(Expressions.convertArgument(null));
		assertEquals("", Expressions.convertArgument("")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("", Expressions.convertArgument("''")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("eclipse", Expressions.convertArgument("eclipse")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("e'clips'e", Expressions.convertArgument("e'clips'e")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("eclipse", Expressions.convertArgument("'eclipse'")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("'ecl'ipse'", Expressions.convertArgument("'''ecl''ipse'''")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("true", Expressions.convertArgument("'true'")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("1.7", Expressions.convertArgument("'1.7'")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("007", Expressions.convertArgument("'007'")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals(Boolean.TRUE, Expressions.convertArgument("true")); //$NON-NLS-1$
		assertEquals(Boolean.FALSE, Expressions.convertArgument("false")); //$NON-NLS-1$
		assertEquals(new Integer(100), Expressions.convertArgument("100")); //$NON-NLS-1$
		assertEquals(new Float(1.7f), Expressions.convertArgument("1.7")); //$NON-NLS-1$
	}
	
	public void testArgumentParsing() throws Exception {
		Object[] result= null;
		
		result= Expressions.parseArguments(""); //$NON-NLS-1$
		assertEquals("", result[0]); //$NON-NLS-1$
		
		result= Expressions.parseArguments("s1"); //$NON-NLS-1$
		assertEquals("s1", result[0]); //$NON-NLS-1$
		
		result= Expressions.parseArguments(" s1 "); //$NON-NLS-1$
		assertEquals("s1", result[0]); //$NON-NLS-1$
		
		result= Expressions.parseArguments("s1,s2"); //$NON-NLS-1$
		assertEquals("s1", result[0]); //$NON-NLS-1$
		assertEquals("s2", result[1]); //$NON-NLS-1$
		
		result= Expressions.parseArguments(" s1 , s2 "); //$NON-NLS-1$
		assertEquals("s1", result[0]); //$NON-NLS-1$
		assertEquals("s2", result[1]); //$NON-NLS-1$
		
		result= Expressions.parseArguments("' s1 ',' s2 '"); //$NON-NLS-1$
		assertEquals(" s1 ", result[0]); //$NON-NLS-1$
		assertEquals(" s2 ", result[1]); //$NON-NLS-1$
		
		result= Expressions.parseArguments(" s1 , ' s2 '"); //$NON-NLS-1$
		assertEquals("s1", result[0]); //$NON-NLS-1$
		assertEquals(" s2 ", result[1]); //$NON-NLS-1$
		
		result= Expressions.parseArguments("' s1 ', s2 "); //$NON-NLS-1$
		assertEquals(" s1 ", result[0]); //$NON-NLS-1$
		assertEquals("s2", result[1]); //$NON-NLS-1$
		
		result= Expressions.parseArguments("''''"); //$NON-NLS-1$
		assertEquals("'", result[0]); //$NON-NLS-1$
		
		result= Expressions.parseArguments("''',''',','"); //$NON-NLS-1$
		assertEquals("','", result[0]);		 //$NON-NLS-1$
		assertEquals(",", result[1]); //$NON-NLS-1$
		
		result= Expressions.parseArguments("' s1 ', true "); //$NON-NLS-1$
		assertEquals(" s1 ", result[0]); //$NON-NLS-1$
		assertEquals(Boolean.TRUE, result[1]);
		
		boolean caught= false;
		try {
			Expressions.parseArguments("' s1"); //$NON-NLS-1$
		} catch (CoreException e) {
			caught= true;
		}
		assertTrue(caught);
		caught= false;
		try {
			Expressions.parseArguments("'''s1"); //$NON-NLS-1$
		} catch (CoreException e) {
			caught= true;
		}
		assertTrue(caught);
	}
	
	public void testSystemProperty() throws Exception {
		SystemTestExpression expression= new SystemTestExpression("os.name", System.getProperty("os.name")); //$NON-NLS-1$ //$NON-NLS-2$
		EvaluationResult result= expression.evaluate(new EvaluationContext(null, new Object()));
		assertTrue(result == EvaluationResult.TRUE);
	}
	
	public void testAdaptExpression() throws Exception {
		AdaptExpression expression= new AdaptExpression("org.eclipse.core.internal.expressions.tests.Adapter"); //$NON-NLS-1$
		expression.add(new InstanceofExpression("org.eclipse.core.internal.expressions.tests.Adapter")); //$NON-NLS-1$
		EvaluationResult result= expression.evaluate(new EvaluationContext(null, new Adaptee()));
		assertTrue(result == EvaluationResult.TRUE);
	}
	
	public void testAdaptExpressionFail() throws Exception {
		AdaptExpression expression= new AdaptExpression("org.eclipse.core.internal.expressions.tests.NotExisting"); //$NON-NLS-1$
		EvaluationResult result= expression.evaluate(new EvaluationContext(null, new Adaptee()));
		assertTrue(result == EvaluationResult.FALSE);
	}
	
	public void testAdaptExpressionFail2() throws Exception {
		AdaptExpression expression= new AdaptExpression("org.eclipse.core.internal.expressions.tests.Adapter"); //$NON-NLS-1$
		expression.add(new InstanceofExpression("org.eclipse.core.internal.expressions.tests.NotExisting")); //$NON-NLS-1$
		EvaluationResult result= expression.evaluate(new EvaluationContext(null, new Adaptee()));
		assertTrue(result == EvaluationResult.FALSE);
	}
	
	public void testVariableResolver() throws Exception {
		final Object result= new Object();
		IVariableResolver resolver= new IVariableResolver() {
			public Object resolve(String name, Object[] args) throws CoreException {
				assertEquals("variable", name); //$NON-NLS-1$
				assertEquals("arg1", args[0]); //$NON-NLS-1$
				assertEquals(Boolean.TRUE, args[1]);
				return result;
			}
		};
		EvaluationContext context= new EvaluationContext(null, new Object(), new IVariableResolver[] { resolver });
		assertTrue(result == context.resolveVariable("variable", new Object[] {"arg1", Boolean.TRUE})); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void testEqualsExpression() throws Exception {
		EqualsExpression exp= new EqualsExpression("name"); //$NON-NLS-1$
		EvaluationContext context= new EvaluationContext(null, "name"); //$NON-NLS-1$
		assertTrue(EvaluationResult.TRUE == exp.evaluate(context));
		
		exp= new EqualsExpression(Boolean.TRUE);
		context= new EvaluationContext(null, Boolean.TRUE);
		assertTrue(EvaluationResult.TRUE == exp.evaluate(context));		
		
		exp= new EqualsExpression("name"); //$NON-NLS-1$
		context= new EvaluationContext(null, Boolean.TRUE);
		assertTrue(EvaluationResult.FALSE == exp.evaluate(context));		
	}
	
	public void testCountExpressionAnyNumber() throws Exception {
		CountExpression exp= new CountExpression("*"); //$NON-NLS-1$
		
		List list= new ArrayList();
		EvaluationContext context= new EvaluationContext(null, list);
		assertTrue(EvaluationResult.TRUE == exp.evaluate(context));
		
		list.clear();
		list.add("one"); //$NON-NLS-1$
		context= new EvaluationContext(null, list);
		assertTrue(EvaluationResult.TRUE == exp.evaluate(context));
		
		list.clear();
		list.add("one"); //$NON-NLS-1$
		list.add("two"); //$NON-NLS-1$
		list.add("three"); //$NON-NLS-1$
		context= new EvaluationContext(null, list);
		assertTrue(EvaluationResult.TRUE == exp.evaluate(context));
	}
	
	public void testCountExpressionExact() throws Exception {
		CountExpression exp= new CountExpression("2"); //$NON-NLS-1$
		
		List list= new ArrayList();
		list.add("one"); //$NON-NLS-1$
		EvaluationContext context= new EvaluationContext(null, list);
		assertTrue(EvaluationResult.FALSE == exp.evaluate(context));
		
		list.clear();
		list.add("one"); //$NON-NLS-1$
		list.add("two"); //$NON-NLS-1$
		context= new EvaluationContext(null, list);
		assertTrue(EvaluationResult.TRUE == exp.evaluate(context));
		
		list.clear();
		list.add("one"); //$NON-NLS-1$
		list.add("two"); //$NON-NLS-1$
		list.add("three"); //$NON-NLS-1$
		context= new EvaluationContext(null, list);
		assertTrue(EvaluationResult.FALSE == exp.evaluate(context));
	}
	
	public void testCountExpressionNoneOrOne() throws Exception {
		CountExpression exp= new CountExpression("?"); //$NON-NLS-1$
		
		List list= new ArrayList();
		EvaluationContext context= new EvaluationContext(null, list);
		assertTrue(EvaluationResult.TRUE == exp.evaluate(context));
		
		list.clear();
		list.add("one"); //$NON-NLS-1$
		context= new EvaluationContext(null, list);
		assertTrue(EvaluationResult.TRUE == exp.evaluate(context));
		
		list.clear();
		list.add("one"); //$NON-NLS-1$
		list.add("two"); //$NON-NLS-1$
		context= new EvaluationContext(null, list);
		assertTrue(EvaluationResult.FALSE == exp.evaluate(context));
	}
	
	public void testCountExpressionOneOrMore() throws Exception {
		CountExpression exp= new CountExpression("+"); //$NON-NLS-1$
		
		List list= new ArrayList();
		EvaluationContext context= new EvaluationContext(null, list);
		assertTrue(EvaluationResult.FALSE == exp.evaluate(context));
		
		list.clear();
		list.add("one"); //$NON-NLS-1$
		context= new EvaluationContext(null, list);
		assertTrue(EvaluationResult.TRUE == exp.evaluate(context));
		
		list.clear();
		list.add("one"); //$NON-NLS-1$
		list.add("two"); //$NON-NLS-1$
		context= new EvaluationContext(null, list);
		assertTrue(EvaluationResult.TRUE == exp.evaluate(context));
	}
	
	public void testCountExpressionNone() throws Exception {
		CountExpression exp= new CountExpression("!"); //$NON-NLS-1$
		
		List list= new ArrayList();
		EvaluationContext context= new EvaluationContext(null, list);
		assertTrue(EvaluationResult.TRUE == exp.evaluate(context));
		
		list.clear();
		list.add("one"); //$NON-NLS-1$
		context= new EvaluationContext(null, list);
		assertTrue(EvaluationResult.FALSE == exp.evaluate(context));
		
		list.clear();
		list.add("one"); //$NON-NLS-1$
		list.add("two"); //$NON-NLS-1$
		context= new EvaluationContext(null, list);
		assertTrue(EvaluationResult.FALSE == exp.evaluate(context));
	}
	
	public void testInstanceofTrue() throws Exception {
		B b= new B();
		EvaluationContext context= new EvaluationContext(null, b);
		
		InstanceofExpression exp= new InstanceofExpression("org.eclipse.core.internal.expressions.tests.B"); //$NON-NLS-1$
		assertTrue(EvaluationResult.TRUE == exp.evaluate(context));

		exp= new InstanceofExpression("org.eclipse.core.internal.expressions.tests.A"); //$NON-NLS-1$
		assertTrue(EvaluationResult.TRUE == exp.evaluate(context));
		
		exp= new InstanceofExpression("org.eclipse.core.internal.expressions.tests.I"); //$NON-NLS-1$
		assertTrue(EvaluationResult.TRUE == exp.evaluate(context));
	}
	
	public void testInstanceofFalse() throws Exception {
		A a= new A();
		EvaluationContext context= new EvaluationContext(null, a);
		
		InstanceofExpression exp= new InstanceofExpression("org.eclipse.core.internal.expressions.tests.B"); //$NON-NLS-1$
		assertTrue(EvaluationResult.FALSE == exp.evaluate(context));
	}
	
	public void testIterateExpressionAndTrue() throws Exception {
		final List result= new ArrayList();
		Expression myExpression= new Expression() {
			public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
				result.add(context.getDefaultVariable());
				return EvaluationResult.TRUE;
			}
		};
		IterateExpression exp= new IterateExpression("and"); //$NON-NLS-1$
		exp.add(myExpression);
		List input= new ArrayList();
		input.add("one"); //$NON-NLS-1$
		input.add("two"); //$NON-NLS-1$
		EvaluationContext context= new EvaluationContext(null, input);
		assertTrue(EvaluationResult.TRUE == exp.evaluate(context));
		assertTrue(result.equals(input));
	}
	
	public void testIterateExpressionAndFalse() throws Exception {
		final List result= new ArrayList();
		Expression myExpression= new Expression() {
			public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
				result.add(context.getDefaultVariable());
				return EvaluationResult.FALSE;
			}
		};
		IterateExpression exp= new IterateExpression("and"); //$NON-NLS-1$
		exp.add(myExpression);
		List input= new ArrayList();
		input.add("one"); //$NON-NLS-1$
		input.add("two"); //$NON-NLS-1$
		EvaluationContext context= new EvaluationContext(null, input);
		assertTrue(EvaluationResult.FALSE == exp.evaluate(context));
		assertTrue(result.size() == 1 && result.get(0).equals("one")); //$NON-NLS-1$
	}
	
	public void testIterateExpressionOrTrue() throws Exception {
		final List result= new ArrayList();
		Expression myExpression= new Expression() {
			public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
				result.add(context.getDefaultVariable());
				return EvaluationResult.TRUE;
			}
		};
		IterateExpression exp= new IterateExpression("or"); //$NON-NLS-1$
		exp.add(myExpression);
		List input= new ArrayList();
		input.add("one"); //$NON-NLS-1$
		input.add("two"); //$NON-NLS-1$
		EvaluationContext context= new EvaluationContext(null, input);
		assertTrue(EvaluationResult.TRUE == exp.evaluate(context));
		assertTrue(result.size() == 1 && result.get(0).equals("one")); //$NON-NLS-1$
	}
	
	public void testIterateExpressionOrFalse() throws Exception {
		final List result= new ArrayList();
		Expression myExpression= new Expression() {
			public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
				result.add(context.getDefaultVariable());
				return EvaluationResult.FALSE;
			}
		};
		IterateExpression exp= new IterateExpression("or"); //$NON-NLS-1$
		exp.add(myExpression);
		List input= new ArrayList();
		input.add("one"); //$NON-NLS-1$
		input.add("two"); //$NON-NLS-1$
		EvaluationContext context= new EvaluationContext(null, input);
		assertTrue(EvaluationResult.FALSE == exp.evaluate(context));
		assertTrue(result.equals(input));
	}
	
	public void testReadXMLExpression() throws Exception {
		IExtensionRegistry registry= Platform.getExtensionRegistry();
		IConfigurationElement[] ces= registry.getConfigurationElementsFor("org.eclipse.core.expressions.tests", "testParticipants"); //$NON-NLS-1$ //$NON-NLS-2$
		
		IConfigurationElement enable= findExtension(ces, "test1").getChildren("enablement")[0]; //$NON-NLS-1$ //$NON-NLS-2$
		Expression exp= ExpressionConverter.getDefault().perform(enable);
		ref(exp);
	}

	private IConfigurationElement findExtension(IConfigurationElement[] ces, String id) {
		for (int i= 0; i < ces.length; i++) {
			if (id.equals(ces[i].getAttribute("id"))) //$NON-NLS-1$
				return ces[i];
		}
		return null;
	}
	
	protected void ref(Expression exp) {
		
	}
}
