/*******************************************************************************
 * Copyright (c) 2026 Vector Informatik GmbH.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jface.internal.text.codemining;

import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.codemining.IPlaceholderCodeMining;

final class CodeMiningDrawHelper {

	private CodeMiningDrawHelper() {
		// Utility class
	}

	/**
	 * Returns the codemining to draw. Is either {@code null} or a
	 * {@linkplain #isDrawableMining(ICodeMining) drawable} mining.
	 * <p>
	 * If the mining has a {@code null} label and is not resolved, the {@code redrawRunnable} is
	 * invoked. If the mining is resolved without error, the resolved minings array is updated.
	 * <p>
	 * The returned mining can be an unresolved mining if it is a placeholder code mining and has a
	 * non-empty {@linkplain IPlaceholderCodeMining#getPlaceholderLabel() placeholder label}.
	 *
	 * @param mining The mining at {@code index}
	 * @param index The index of the mining
	 * @param resolvedMinings The array of resolved minings
	 * @param redrawRunnable The runnable to invoke for redrawing, should be a runnable obtained via
	 *            {@link #once(Runnable)}
	 * @return The {@linkplain #isDrawableMining(ICodeMining) drawable} code mining to draw, or
	 *         {@code null} if no mining should be drawn.
	 */
	static ICodeMining chooseMiningToDraw(ICodeMining mining, int index, ICodeMining[] resolvedMinings, Runnable redrawRunnable) {
		ICodeMining lastResolvedMining= (resolvedMinings != null && resolvedMinings.length > index) ? resolvedMinings[index] : null;
		ICodeMining toDraw= switch (getResolveState(mining)) {
			case UNRESOLVED -> {
				redrawRunnable.run();
				yield lastResolvedMining;
			}
			case UNRESOLVED_WITH_PLACEHOLDER -> {
				redrawRunnable.run();

				if (CodeMiningManager.isValidMining(lastResolvedMining)) {
					yield lastResolvedMining;
				}

				yield mining;
			}
			case RESOLVED_ERROR -> lastResolvedMining;
			case RESOLVED -> {
				if (resolvedMinings != null && resolvedMinings.length > index) {
					resolvedMinings[index]= mining;
				}
				yield mining;
			}
		};

		if (isDrawableMining(toDraw)) {
			return toDraw;
		}

		return null;
	}

	/**
	 * Wraps the given Runnable, so that it is invoked at most once.
	 * <p>
	 * This implementation is not thread-safe. If the runnable is shared by multiple threads, the
	 * delegated-to runnable can still be invoked multiple times.
	 *
	 * @param r The runnable to wrap
	 * @return The runnable wrapper, delegates to the given Runnable at most once.
	 */
	static Runnable once(Runnable r) {
		return new Runnable() {

			boolean ran= false;

			@Override
			public void run() {
				if (!ran) {
					ran= true;
					r.run();
				}
			}
		};
	}

	/**
	 * Returns {@code true} if the given mining has a non-empty label or is not resolved and has a
	 * non-empty placeholder label. Returns {@code false} otherwise.
	 *
	 * @param mining the mining to check.
	 * @return {@code true} if the given mining has a non-empty label or is not resolved and has a
	 *         non-empty placeholder label. {@code false} otherwise
	 */
	static boolean isDrawableMining(ICodeMining mining) {
		if (mining == null) {
			return false;
		}

		String label= CodeMiningManager.getLabelOrPlaceholder(mining);
		return label != null && !label.isEmpty();
	}

	private static ResolveState getResolveState(ICodeMining mining) {
		String label= mining.getLabel();

		if (label == null && mining.isResolved()) {
			return ResolveState.RESOLVED_ERROR;
		} else if (label != null) {
			return ResolveState.RESOLVED;
		}

		if (mining instanceof IPlaceholderCodeMining placeholderMining) {
			String placeholderLabel= placeholderMining.getPlaceholderLabel();

			if (placeholderLabel != null && !placeholderLabel.isEmpty()) {
				return ResolveState.UNRESOLVED_WITH_PLACEHOLDER;
			}
		}

		return ResolveState.UNRESOLVED;
	}

	private enum ResolveState {

		UNRESOLVED, UNRESOLVED_WITH_PLACEHOLDER, RESOLVED_ERROR, RESOLVED;

	}

}
