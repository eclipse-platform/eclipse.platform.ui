/*******************************************************************************
 * Copyright (c) 2016 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.internal.databinding.observable.sideeffect;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffect;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffectFactory;

/**
 * A factory to create {@link ISideEffect} objects, which are applied to the
 * given {@link Consumer} in {@link ISideEffectFactory#createFactory(Consumer)}.
 *
 * @since 3.3
 */
public final class SideEffectFactory implements ISideEffectFactory {

	private Consumer<ISideEffect> sideEffectConsumer;

	/**
	 * Creates a new factory which will pass all created side-effects to the
	 * given {@link Consumer}.
	 *
	 * @param sideEffectConsumer
	 *            {@link Consumer}, where the {@link ISideEffect} objects, which
	 *            are created by this factory are passed to.
	 */
	public SideEffectFactory(Consumer<ISideEffect> sideEffectConsumer) {
		this.sideEffectConsumer = sideEffectConsumer;
	}

	@Override
	public ISideEffect createPaused(Runnable runnable) {
		ISideEffect sideEffect = ISideEffect.createPaused(runnable);
		sideEffectConsumer.accept(sideEffect);

		return sideEffect;
	}

	@Override
	public ISideEffect createPaused(Realm realm, Runnable runnable) {
		ISideEffect sideEffect = ISideEffect.createPaused(realm, runnable);
		sideEffectConsumer.accept(sideEffect);

		return sideEffect;
	}

	@Override
	public ISideEffect create(Runnable runnable) {
		ISideEffect sideEffect = ISideEffect.create(runnable);
		sideEffectConsumer.accept(sideEffect);

		return sideEffect;
	}

	@Override
	public <T> ISideEffect create(Supplier<T> supplier, Consumer<T> consumer) {
		ISideEffect sideEffect = ISideEffect.create(supplier, consumer);
		sideEffectConsumer.accept(sideEffect);

		return sideEffect;
	}

	@Override
	public <T> ISideEffect createResumed(Supplier<T> supplier, Consumer<T> consumer) {
		ISideEffect sideEffect = ISideEffect.createResumed(supplier, consumer);
		sideEffectConsumer.accept(sideEffect);

		return sideEffect;
	}

	@Override
	public <T> ISideEffect consumeOnceAsync(Supplier<T> supplier, Consumer<T> consumer) {
		ISideEffect sideEffect = ISideEffect.consumeOnceAsync(supplier, consumer);
		sideEffectConsumer.accept(sideEffect);

		return sideEffect;
	}
}
