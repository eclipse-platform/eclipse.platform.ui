/*******************************************************************************
 * Copyright (c) 2007, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;


/**
 * Tests the {@link org.eclipse.jface.text.source.IAnnotationModelExtension2}.
 *
 * @since 3.4
 */
public class AnnotationModelExtension2Test extends TestCase {

	public class OldAnnotationModel implements IAnnotationModel {

		private final HashMap fAnnotations= new HashMap();

		public void addAnnotation(Annotation annotation, Position position) {
			fAnnotations.put(annotation, position);
		}

		public void addAnnotationModelListener(IAnnotationModelListener listener) {
		}

		public void connect(IDocument document) {
		}

		public void disconnect(IDocument document) {
		}

		public Iterator getAnnotationIterator() {
			return fAnnotations.keySet().iterator();
		}

		public Position getPosition(Annotation annotation) {
			return (Position) fAnnotations.get(annotation);
		}

		public void removeAnnotation(Annotation annotation) {
			fAnnotations.remove(annotation);
		}

		public void removeAnnotationModelListener(IAnnotationModelListener listener) {
		}

		public void removeAllAnnotations() {
			fAnnotations.clear();
		}
	}

	private static final int MODEL_COUNT= 3;

	private Document fDocument;
	private AnnotationModel fAnnotationModel;
	private AnnotationModel fNewInnerModel;
	private OldAnnotationModel fOldInnerModel;

	private Annotation fInside;
	private Annotation fBefore;
	private Annotation fAfter;
	private Annotation fInsideIn;
	private Annotation fInsideOut;
	private Annotation fBeforeIn;
	private Annotation fBoforeOut;
	private Annotation fAfterIn;
	private Annotation fAfterOut;

	public static Test suite() {
		return new TestSuite(AnnotationModelExtension2Test.class);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void setUp() throws Exception {
		fDocument= new Document("How much wood\nwould a woodchuck chuck\nif a woodchuck\ncould chuck wood?\n42");

		fAnnotationModel= new AnnotationModel();

		fNewInnerModel= new AnnotationModel();
		fAnnotationModel.addAnnotationModel("model1", fNewInnerModel);

		fOldInnerModel= new OldAnnotationModel();
		fAnnotationModel.addAnnotationModel("model2", fOldInnerModel);

		fInside= new Annotation(false);
		fInsideIn= new Annotation(false);
		fInsideOut= new Annotation(false);

		fBefore= new Annotation(false);
		fBeforeIn= new Annotation(false);
		fBoforeOut= new Annotation(false);

		fAfter= new Annotation(false);
		fAfterIn= new Annotation(false);
		fAfterOut= new Annotation(false);

		fAnnotationModel.connect(fDocument);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void tearDown() throws Exception {
		fAnnotationModel.disconnect(fDocument);
	}

	private void assertEquals(Annotation[] expected, Annotation[] actual, IAnnotationModel insideModel, IAnnotationModel beforeModel, IAnnotationModel afterModel) {
		HashSet expectedSet= new HashSet(Arrays.asList(expected));
		for (int i= 0; i < actual.length; i++) {
			if (!expectedSet.contains(actual[i])) {
				String message= "Unexpected annotation " + getName(actual[i]) + " in result with models [" + getAnnotationModelNames(insideModel, beforeModel, afterModel) + "]";
				assertTrue(message, false);
			}
			expectedSet.remove(actual[i]);
		}

		if (!expectedSet.isEmpty()) {
			String message= "Missing annotations in result with models [" + getAnnotationModelNames(insideModel, beforeModel, afterModel) + "]";
			for (Iterator iterator= expectedSet.iterator(); iterator.hasNext();) {
				Annotation missing= (Annotation) iterator.next();
				message= message + "\n" + getName(missing);
			}
			assertTrue(message, false);
		}
	}

	private String getAnnotationModelNames(IAnnotationModel insideModel, IAnnotationModel beforeModel, IAnnotationModel afterModel) {
		return "inside: " + getAnnotationModelName(insideModel) + " before: " + getAnnotationModelName(beforeModel) + " after: " + getAnnotationModelName(afterModel);
	}

	private String getAnnotationModelName(IAnnotationModel model) {
		if (model == fAnnotationModel) {
			return "'Top'";
		} else if (model == fNewInnerModel) {
			return "'New inner'";
		} else if (model == fOldInnerModel) {
			return "'Old inner'";
		}

		return "'Unknown'";
	}

	private String getName(Annotation annotation) {
		Position position= fAnnotationModel.getPosition(annotation);
		return "[" + position.getOffset() + ", " + position.getLength() + "]";
	}

	/*
	 * The annotations are added to the annotations models
	 * as following:
	 *
	 * 0-beforeout-9               21-afterout--30
	 * 0--beforein---11          19---afterin---30
	 * 0--before----10            20--after-----30
	 *
	 *              10--region----20
	 *
	 *              10--inside----20
	 *               11-insidein-19
	 *             9---insideout---21
	 */
	private void addAnnotations(IAnnotationModel insideModel, IAnnotationModel beforeModel, IAnnotationModel afterModel) {
		insideModel.addAnnotation(fInside, new Position(10, 11));
		insideModel.addAnnotation(fInsideIn, new Position(11, 9));
		insideModel.addAnnotation(fInsideOut, new Position(9, 13));

		beforeModel.addAnnotation(fBefore, new Position(0, 11));
		beforeModel.addAnnotation(fBeforeIn, new Position(0, 12));
		beforeModel.addAnnotation(fBoforeOut, new Position(0, 10));

		afterModel.addAnnotation(fAfter, new Position(20, 11));
		afterModel.addAnnotation(fAfterIn, new Position(19, 12));
		afterModel.addAnnotation(fAfterOut, new Position(21, 10));
	}

	private void removeAnnotations() {
		fAnnotationModel.removeAllAnnotations();
		fNewInnerModel.removeAllAnnotations();
		fOldInnerModel.removeAllAnnotations();
	}

	private Annotation[] getAnnotations(boolean lookAhead, boolean lookBehind) {
		Iterator iterator= fAnnotationModel.getAnnotationIterator(10, 11, lookAhead, lookBehind);

		ArrayList result= new ArrayList();
		while (iterator.hasNext()) {
			result.add(iterator.next());
		}

		return (Annotation[]) result.toArray(new Annotation[result.size()]);
	}

	private void assertPermutations(boolean lookAhead, boolean lookBehind, Annotation[] expected) {
		for (int i= 0; i < MODEL_COUNT; i++) {
			for (int j= 0; j < MODEL_COUNT; j++) {
				for (int k= 0; k < MODEL_COUNT; k++) {
					IAnnotationModel insideModel= getModel(i);
					IAnnotationModel beforeModel= getModel(j);
					IAnnotationModel afterModel= getModel(k);

					addAnnotations(insideModel, beforeModel, afterModel);

					Annotation[] actual= getAnnotations(lookAhead, lookBehind);
					assertEquals(expected, actual, insideModel, beforeModel, afterModel);

					removeAnnotations();
				}
			}
		}
	}

	private IAnnotationModel getModel(int number) {
		switch (number) {
			case 0:
				return fAnnotationModel;
			case 1:
				return fNewInnerModel;
			case 2:
				return fOldInnerModel;
			default:
				break;
		}
		return null;
	}

	public void testInside() throws Exception {
		Annotation[] expected= new Annotation[] { fInside, fInsideIn };
		assertPermutations(false, false, expected);
	}

	public void testAhead() throws Exception {
		Annotation[] expected= new Annotation[] { fInside, fInsideIn, fBefore, fBeforeIn };
		assertPermutations(true, false, expected);
	}

	public void testBehind() throws Exception {
		Annotation[] expected= new Annotation[] { fInside, fInsideIn, fAfter, fAfterIn };
		assertPermutations(false, true, expected);
	}

	public void testAheadBehind() throws Exception {
		Annotation[] expected= new Annotation[] { fInside, fInsideIn, fInsideOut, fAfter, fAfterIn, fBefore, fBeforeIn };
		assertPermutations(true, true, expected);
	}

}
