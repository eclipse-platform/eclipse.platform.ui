/*******************************************************************************
 * Copyright (c) 2019 Altran Netherlands B.V. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Niko Stotz (Altran Netherlands B.V.) - initial implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text.codemining.annotation;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import org.eclipse.core.resources.IMarker;

import org.eclipse.jface.text.quickassist.IQuickFixableAnnotation;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationAccessExtension;
import org.eclipse.jface.text.source.IAnnotationPresentation;

import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * Filters and arranges Annotations that are suitable as code minings. Takes user preferences into
 * account.
 *
 * @since 3.13
 */
@NonNullByDefault
public class AnnotationCodeMiningFilter {
	/**
	 * Callback to locate an Annotation inside the editor.
	 */
	public interface Locator {
		public @Nullable Integer getOffset(Annotation annotation);

		public @Nullable Integer getLine(Annotation annotation);
	}

	final private IAnnotationAccessExtension annotationAccess;

	final private AnnotationCodeMiningPreferences preferences= new AnnotationCodeMiningPreferences();

	final private Stream<Annotation> annotations;

	public AnnotationCodeMiningFilter(IAnnotationAccessExtension annotationAccess, Annotation[]... annotations) {
		this.annotationAccess= annotationAccess;
		this.annotations= Arrays.stream(annotations).flatMap(Arrays::stream);
	}

	public AnnotationCodeMiningFilter(IAnnotationAccessExtension annotationAccess, Iterator<Annotation> annotations) {
		this.annotationAccess= annotationAccess;
		this.annotations= StreamSupport.stream(Spliterators.spliteratorUnknownSize(annotations, Spliterator.ORDERED), false);
	}

	/**
	 * Checks if there are any suitable annotations.
	 */
	public boolean isEmpty() {
		return !filterReTrigger(annotations).findAny().isPresent();
	}

	/**
	 * Returns all suitable annotations.
	 */
	public Stream<Annotation> sortDistinctLimit(Locator locator) {
		return limit(distinct(locator, sort(locator, filterShown(filterReTrigger(annotations)))));
	}

	/**
	 * Filters suitable annotations to decide whether we need to re-trigger code minings.
	 */
	private Stream<Annotation> filterReTrigger(Stream<Annotation> anns) {
		return anns
				.filter(this::isTypeProcessable)
				.filter(this::isPaintable)
				.filter(this::isInScope);
	}

	private boolean isTypeProcessable(Annotation a) {
		return a instanceof MarkerAnnotation ||
				a instanceof IQuickFixableAnnotation ||
				a instanceof IAnnotationPresentation;
	}

	private boolean isPaintable(Annotation a) {
		return annotationAccess.isPaintable(a);
	}

	private boolean isInScope(Annotation a) {
		return isError(a) || isWarning(a) || isInfo(a);
	}

	/**
	 * Filters suitable annotations to show.
	 */
	private Stream<Annotation> filterShown(Stream<Annotation> anns) {
		return anns
				.filter(a -> !a.isMarkedDeleted())
				.filter(this::isEnabled);
	}

	private boolean isEnabled(Annotation a) {
		if (isError(a)) {
			return preferences.isErrorEnabled();
		} else if (isWarning(a)) {
			return preferences.isWarningEnabled();
		} else if (isInfo(a)) {
			return preferences.isInfoEnabled();
		} else {
			return false;
		}
	}

	/**
	 * Sorts annotations based on 1) position in text, 2) layer, 3) severity, 4) text.
	 */
	private Stream<Annotation> sort(Locator locator, Stream<Annotation> anns) {
		return anns.sorted((a, b) -> {
			int resultPosition= comparePosition(locator, a, b);
			if (resultPosition != 0) {
				return resultPosition;
			}

			final int resultLayer= compareLayer(a, b);
			if (resultLayer != 0) {
				return resultLayer;
			}

			final int resultSeverity= compareSeverity(a, b);
			if (resultSeverity != 0) {
				return resultSeverity;
			}

			return a.getText() == null ? (b.getText() == null ? 0 : -1) : (b.getText() == null ? 1 : a.getText().compareTo(a.getText()));
		});
	}

	private int comparePosition(Locator locator, Annotation a, Annotation b) {
		final Integer aOffset= locator.getOffset(a);
		final Integer bOffset= locator.getOffset(b);

		if (aOffset == null || bOffset == null) {
			return 0;
		}

		int resultPosition= Integer.compare(aOffset, bOffset);
		return resultPosition;
	}

	private int compareLayer(Annotation a, Annotation b) {
		final int resultPriority= Integer.compare(annotationAccess.getLayer(a), annotationAccess.getLayer(b));
		return resultPriority;
	}

	private int compareSeverity(Annotation a, Annotation b) {
		final int resultSeverity= Integer.compare(getSeverity(a), getSeverity(b));
		return resultSeverity;
	}

	private int getSeverity(Annotation a) {
		if (isError(a)) {
			return IMarker.SEVERITY_ERROR;
		} else if (isWarning(a)) {
			return IMarker.SEVERITY_WARNING;
		} else if (isInfo(a)) {
			return IMarker.SEVERITY_INFO;
		} else {
			return -1;
		}
	}

	/**
	 * Assures annotations are distinct by line and text. Required, as sometimes the same message is
	 * shown at the same place from different annotations.
	 */
	private Stream<Annotation> distinct(Locator locator, Stream<Annotation> anns) {
		return anns
				.filter(distinctByKey(a -> {
					final Integer line= locator.getLine(a);
					if (line == null) {
						return null;
					}
					String key= line + a.getText();

					return key;
				}))
				.filter(Objects::nonNull);
	}

	/**
	 * Limits annotations to user-defined amount.
	 */
	private Stream<Annotation> limit(Stream<Annotation> anns) {
		return anns.limit(preferences.getMaxMinings());
	}

	private boolean isInfo(Annotation a) {
		return annotationAccess.isSubtype(a.getType(), "org.eclipse.ui.workbench.texteditor.info"); //$NON-NLS-1$
	}

	private boolean isWarning(Annotation a) {
		return annotationAccess.isSubtype(a.getType(), "org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
	}

	private boolean isError(Annotation a) {
		return annotationAccess.isSubtype(a.getType(), "org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
	}

	public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
		Map<Object, Boolean> seen= new LinkedHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}
}
