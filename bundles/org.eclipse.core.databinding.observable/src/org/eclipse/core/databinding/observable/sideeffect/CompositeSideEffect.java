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

package org.eclipse.core.databinding.observable.sideeffect;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.Assert;

/**
 * Represents an {@link ISideEffect} that is composed of a bunch of component
 * {@link ISideEffect}s. It has the following properties:
 *
 * <ul>
 * <li>Disposing the composite will dispose all of the children.</li>
 * <li>If the composite is paused, all of the children will be paused as well.
 * </li>
 * </ul>
 *
 * Note that resuming a composite does not guarantee that all children will
 * resume. Children may also be paused externally, in which case the child must
 * be resumed both by the composite and by the external source(s) before it will
 * execute.
 * <p>
 * Children may belong to multiple composites. When this occurs, all of its
 * parent composites must be resumed in order for the child to execute and the
 * child will be disposed the first time any of its parents are disposed.
 * <p>
 * Children may be removed from a composite. When this occurs, the child may be
 * resumed immediately if the composite was paused and disposing the composite
 * will no longer have any effect on the removed child.
 * <p>
 * Disposing a child will automatically remove it from its parent composite(s).
 * <p>
 * The main use of this class is to manage a group of side-effects that share
 * the same life-cycle. For example, all side-effects used to populate widgets
 * within a workbench part would likely be paused and resumed when the part is
 * made visible or invisible, and would all be disposed together when the part
 * is closed.
 *
 * @since 1.6
 */
public final class CompositeSideEffect implements ISideEffect {

	private final List<ISideEffect> sideEffects;
	private int pauseDepth;
	private boolean isDisposed;
	private final Realm realm;

	/**
	 * List of dispose listeners. Null if empty.
	 */
	private List<Consumer<ISideEffect>> disposeListeners;

	private Consumer<ISideEffect> removalConsumer = this::remove;

	/**
	 * Default constructor of an CompositeSideEffect.
	 */
	public CompositeSideEffect() {
		realm = Realm.getDefault();
		sideEffects = new ArrayList<>();
	}

	private void checkRealm() {
		Assert.isTrue(realm.isCurrent(), "This operation must be run within the observable's realm"); //$NON-NLS-1$
	}

	@Override
	public void dispose() {
		checkRealm();
		if (isDisposed) {
			return;
		}
		sideEffects.forEach(s -> s.removeDisposeListener(removalConsumer));
		sideEffects.forEach(s -> s.dispose());
		sideEffects.clear();
		isDisposed = true;
		if (disposeListeners != null) {
			List<Consumer<ISideEffect>> listeners = disposeListeners;
			disposeListeners = null;
			listeners.forEach(dc -> dc.accept(CompositeSideEffect.this));
		}
	}

	@Override
	public boolean isDisposed() {
		checkRealm();
		return this.isDisposed;
	}

	@Override
	public void addDisposeListener(Consumer<ISideEffect> disposalConsumer) {
		checkRealm();
		if (isDisposed()) {
			return;
		}
		if (this.disposeListeners == null) {
			this.disposeListeners = new ArrayList<>();
		}
		this.disposeListeners.add(disposalConsumer);
	}

	@Override
	public void removeDisposeListener(Consumer<ISideEffect> disposalConsumer) {
		checkRealm();
		if (this.disposeListeners == null) {
			return;
		}
		this.disposeListeners.remove(disposalConsumer);
	}

	@Override
	public void pause() {
		checkRealm();
		pauseDepth++;
		if (pauseDepth == 1) {
			sideEffects.forEach(s -> s.pause());
		}
	}

	@Override
	public void resume() {
		checkRealm();
		pauseDepth--;
		if (pauseDepth < 0) {
			throw new IllegalStateException(
					"The resume() method was called more times than pause()."); //$NON-NLS-1$
		} else if (pauseDepth == 0) {
			sideEffects.forEach(s -> s.resume());
		}
	}

	@Override
	public void resumeAndRunIfDirty() {
		checkRealm();
		pauseDepth--;
		if (pauseDepth == 0) {
			sideEffects.forEach(s -> s.resumeAndRunIfDirty());
		}
	}

	@Override
	public void runIfDirty() {
		checkRealm();
		if (pauseDepth <= 0) {
			sideEffects.forEach(s -> s.runIfDirty());
		}
	}

	/**
	 * Adds the given {@link ISideEffect} instance from the composite.
	 *
	 * @param sideEffect
	 *            {@link ISideEffect}
	 */
	public void add(ISideEffect sideEffect) {
		checkRealm();
		if (!sideEffect.isDisposed()) {
			sideEffects.add(sideEffect);
			if (pauseDepth > 0) {
				sideEffect.pause();
			}
			sideEffect.addDisposeListener(removalConsumer);
		}
	}

	/**
	 * Removes the given {@link ISideEffect} instance from the composite. This
	 * has no effect if the given side-effect is not part of the composite.
	 *
	 * @param sideEffect
	 *            {@link ISideEffect}
	 */
	public void remove(ISideEffect sideEffect) {
		checkRealm();
		sideEffects.remove(sideEffect);
		if (!sideEffect.isDisposed()) {
			if (pauseDepth > 0) {
				sideEffect.resume();
			}
			sideEffect.removeDisposeListener(removalConsumer);
		}
	}
}
