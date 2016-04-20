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

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.internal.databinding.observable.sideeffect.SideEffectFactory;

/**
 * <p>
 * A factory to create {@link ISideEffect} objects, which are applied to the
 * given {@link Consumer} in {@link ISideEffectFactory#createFactory(Consumer)}.
 * </p>
 * Callers who provide the Consumer<ISideEffect> for the creation of an
 * {@link ISideEffectFactory}, are supposed to manage the lifecycle of the
 * aggregated {@link ISideEffect} instances, which are created by this factory.
 *
 * @see SideEffectFactory
 * @see CompositeSideEffect
 *
 * @since 1.6
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ISideEffectFactory {

	/**
	 * Creates a new {@link ISideEffectFactory} which will notify the given
	 * {@link Consumer} of every {@link ISideEffect} that is constructed by the
	 * factory.
	 * <p>
	 * For example, a {@link Consumer} could be passed to this method which
	 * automatically inserts every {@link ISideEffect} into the same
	 * {@link CompositeSideEffect}, allowing their lifecycle to be managed
	 * automatically by the object which provides the factory.
	 * <p>
	 * Callers who invoke this {@link #createFactory(Consumer)} method are
	 * supposed to manage the lifecycle of the aggregated {@link ISideEffect}
	 * instances, which are created by this factory. They do so by passing in a
	 * consumer which collects the side-effects constructed by the factory,
	 * allowing {@link ISideEffect#dispose()} to be invoked on them at a later
	 * time.
	 *
	 * @param sideEffectConsumer
	 *            a consumer which will be notified about every
	 *            {@link ISideEffect} constructed by this factory. The consumer
	 *            must guarantee that {@link ISideEffect#dispose()} will be
	 *            called on every {@link ISideEffect} it receives at some point
	 *            in the future.
	 * @return a newly constructed {@link ISideEffectFactory}
	 * @see CompositeSideEffect
	 */
	static ISideEffectFactory createFactory(Consumer<ISideEffect> sideEffectConsumer) {
		return new SideEffectFactory(sideEffectConsumer);
	}

	/**
	 * Creates a new {@link ISideEffect} on the default {@link Realm} but does
	 * not run it immediately. The lifecycle of the returned {@link ISideEffect}
	 * will be managed by the factory.
	 * <p>
	 * Callers are not required to dispose the resulting {@link ISideEffect},
	 * but may do so if they wish to dispose it early.
	 * <p>
	 * This is similar to {@link ISideEffect#createPaused(Runnable)} except that
	 * the resulting {@link ISideEffect} will have its lifecycle managed by the
	 * factory.
	 *
	 * @param runnable
	 *            the runnable to execute. Must be idempotent.
	 * @return a newly-created {@link ISideEffect} which has not yet been
	 *         activated. Callers may call {@link ISideEffect#dispose()} on the
	 *         result if they wish to dispose it early, but they are not
	 *         required to do so since the lifecycle of the returned
	 *         {@link ISideEffect} is managed automatically.
	 */
	ISideEffect createPaused(Runnable runnable);

	/**
	 * Creates a new {@link ISideEffect} on the given Realm but does not
	 * activate it immediately. Callers are not required to dispose the
	 * resulting {@link ISideEffect}, but may do so if they wish to dispose it
	 * early.
	 * <p>
	 * This is similar to {@link ISideEffect#createPaused(Realm, Runnable)}
	 * except that the resulting {@link ISideEffect} will have its lifecycle
	 * managed by the factory.
	 *
	 * @param realm
	 *            the realm to execute
	 * @param runnable
	 *            the runnable to execute. Must be idempotent.
	 * @return a newly-created {@link ISideEffect} which has not yet been
	 *         activated. Callers may call {@link ISideEffect#dispose()} on the
	 *         result if they wish to dispose it early, but they are not
	 *         required to do so since the lifecycle of the returned
	 *         {@link ISideEffect} is managed automatically.
	 */
	ISideEffect createPaused(Realm realm, Runnable runnable);

	/**
	 * Runs the given runnable once synchronously. The runnable will then run
	 * again after any tracked getter invoked by the runnable changes. It will
	 * continue doing so until the returned {@link ISideEffect} is disposed. The
	 * returned {@link ISideEffect} is associated with the default realm.
	 * <p>
	 * Callers are not required to dispose the resulting {@link ISideEffect},
	 * but may do so if they wish to dispose it early.
	 * <p>
	 * This is similar to {@link ISideEffect#create(Runnable)} except that the
	 * resulting {@link ISideEffect} will have its lifecycle managed by the
	 * factory.
	 *
	 * @param runnable
	 *            an idempotent runnable which will be executed once
	 *            synchronously then additional times after any tracked getter
	 *            it uses changes state
	 * @return an {@link ISideEffect} interface that may be used to stop the
	 *         side-effect from running. The {@link Runnable} will not be
	 *         executed anymore after the dispose method is invoked. Callers may
	 *         call {@link ISideEffect#dispose()} on the result if they wish to
	 *         dispose it early, but they are not required to do so since the
	 *         lifecycle of the returned {@link ISideEffect} is managed
	 *         automatically.
	 */
	ISideEffect create(Runnable runnable);

	/**
	 * Runs the supplier and passes its result to the consumer. The same thing
	 * will happen again after any tracked getter invoked by the supplier
	 * changes. It will continue to do so until the given {@link ISideEffect} is
	 * disposed. The returned {@link ISideEffect} is associated with the default
	 * realm.
	 * <p>
	 * Callers are not required to dispose the resulting {@link ISideEffect},
	 * but may do so if they wish to dispose it early.
	 * <p>
	 * The ISideEffect will initially be in the resumed state.
	 * <p>
	 * The first invocation of this method will be synchronous. This version is
	 * slightly more efficient than {@link #createResumed(Supplier, Consumer)}
	 * and should be preferred if synchronous execution is acceptable.
	 * <p>
	 * This is similar to {@link ISideEffect#create(Supplier, Consumer)} except
	 * that the resulting {@link ISideEffect} will have its lifecycle managed by
	 * the factory.
	 *
	 * @param supplier
	 *            a supplier which will compute a value and be monitored for
	 *            changes in tracked getters. It should be side-effect-free.
	 * @param consumer
	 *            a consumer which will receive the value. It should be
	 *            idempotent. It will not be monitored for tracked getters.
	 *
	 * @return an {@link ISideEffect} interface that may be used to stop the
	 *         side-effect from running. The {@link Runnable} will not be
	 *         executed anymore after the dispose method is invoked. Callers may
	 *         call {@link ISideEffect#dispose()} on the result if they wish to
	 *         dispose it early, but they are not required to do so since the
	 *         lifecycle of the returned {@link ISideEffect} is managed
	 *         automatically.
	 */
	<T> ISideEffect create(Supplier<T> supplier, Consumer<T> consumer);

	/**
	 * Runs the supplier and passes its result to the consumer. The same thing
	 * will happen again after any tracked getter invoked by the supplier
	 * changes. It will continue to do so until the given {@link ISideEffect} is
	 * disposed. The returned {@link ISideEffect} is associated with the default
	 * realm.
	 * <p>
	 * Callers are not required to dispose the resulting {@link ISideEffect},
	 * but may do so if they wish to dispose it early.
	 * <p>
	 * The ISideEffect will initially be in the resumed state.
	 * <p>
	 * The first invocation of this method will be asynchronous. This is useful,
	 * for example, when constructing an {@link ISideEffect} in a constructor
	 * since it ensures that the constructor will run to completion before the
	 * first invocation of the {@link ISideEffect}. However, this extra safety
	 * comes with a small performance cost over
	 * {@link #create(Supplier, Consumer)}.
	 * <p>
	 * This is similar to {@link ISideEffect#createResumed(Supplier, Consumer)}
	 * except that the resulting {@link ISideEffect} will have its lifecycle
	 * managed by the factory.
	 *
	 * @param supplier
	 *            a supplier which will compute a value and be monitored for
	 *            changes in tracked getters. It should be side-effect-free.
	 * @param consumer
	 *            a consumer which will receive the value. It should be
	 *            idempotent. It will not be monitored for tracked getters.
	 *
	 * @return an {@link ISideEffect} interface that may be used to stop the
	 *         side-effect from running. The {@link Runnable} will not be
	 *         executed anymore after the dispose method is invoked. Callers may
	 *         call {@link ISideEffect#dispose()} on the result if they wish to
	 *         dispose it early, but they are not required to do so since the
	 *         lifecycle of the returned {@link ISideEffect} is managed
	 *         automatically.
	 */
	<T> ISideEffect createResumed(Supplier<T> supplier, Consumer<T> consumer);

	/**
	 * Runs the given supplier until it returns a non-null result. The first
	 * time it returns a non-null result, that result will be passed to the
	 * consumer and the ISideEffect will dispose itself. As long as the supplier
	 * returns null, any tracked getters it invokes will be monitored for
	 * changes. If they change, the supplier will be run again at some point in
	 * the future.
	 * <p>
	 * The resulting ISideEffect will be dirty and resumed, so the first
	 * invocation of the supplier will be asynchronous. If the caller needs it
	 * to be invoked synchronously, they can call
	 * {@link ISideEffect#runIfDirty()}
	 * <p>
	 * Unlike {@link #create(Supplier, Consumer)}, the consumer does not need to
	 * be idempotent.
	 * <p>
	 * This method is used for gathering asynchronous data before opening an
	 * editor, saving to disk, opening a dialog box, or doing some other
	 * operation which should only be performed once.
	 * <p>
	 * Consider the following example, which displays the content of a text file
	 * in a message box without doing any file I/O on the UI thread.
	 * <p>
	 *
	 * <pre>
	 * IObservableValue&lt;String&gt; loadFileAsString(String filename) {
	 *   // Uses another thread to load the given filename. The resulting observable returns
	 *   // null if the file is not yet loaded or contains the file contents if the file is
	 *   // fully loaded
	 *   // ...
	 * }
	 *
	 * void showFileContents(Shell parentShell, String filename) {
	 *   IObservableValue&lt;String&gt; webPageContent = loadFileAsString(filename);
	 *   ISideEffect.runOnce(webPageContent::getValue, (content) -&gt; {
	 *   	MessageDialog.openInformation(parentShell, "Your file contains", content);
	 *   })
	 * }
	 * </pre>
	 * This is similar to
	 * {@link ISideEffect#consumeOnceAsync(Supplier, Consumer)} except that the
	 * resulting {@link ISideEffect} will have its lifecycle managed by the
	 * factory.
	 *
	 * @param supplier
	 *            supplier which returns null if the side-effect should continue
	 *            to wait or returns a non-null value to be passed to the
	 *            consumer if it is time to invoke the consumer
	 * @param consumer
	 *            a (possibly non-idempotent) consumer which will receive the
	 *            first non-null result returned by the supplier.
	 * @return a side-effect which can be used to control this operation. If it
	 *         is disposed before the consumer is invoked, the consumer will
	 *         never be invoked. It will not invoke the supplier if it is
	 *         paused.
	 */
	<T> ISideEffect consumeOnceAsync(Supplier<T> supplier, Consumer<T> consumer);
}