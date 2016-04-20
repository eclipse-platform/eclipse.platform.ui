/*******************************************************************************
 * Copyright (c) 2015 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefan Xenos (Google) - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.databinding.observable.sideeffect;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.internal.databinding.observable.sideeffect.SideEffect;

/**
 * An {@link ISideEffect} allows you to run code whenever one or more
 * observables change. An {@link ISideEffect} is a lot like a listener except
 * that it doesn't need to be attached to anything. Instead, it reacts
 * automatically to changes in tracked getters that are invoked by the listener.
 * <p>
 * Observables form a directed graph of dependencies. Classes like
 * {@link WritableValue} form the inputs to the graph (nodes which have only
 * outputs), classes like {@link ComputedValue} form the interior nodes (they
 * receive inputs from observables and produce an output which is used by other
 * observables), and {@link ISideEffect} is used for the leaf nodes (nodes which
 * receive inputs but produce no output).
 * <p>
 * Side-effects have a life-cycle which passes through a number of states:
 * <ul>
 * <li>Paused: The side-effect will listen for changes but will not react to
 * them. If any change occurs while the side-effect is paused, it will react
 * when and if the side-effect is resumed. Some side-effects are paused
 * immediately on construction. This is useful, for example, for creating a
 * side-effect in an object's constructor which should not begin running until a
 * later time. When using a side-effect to update a control or a view, it is
 * common to pause the side-effect when the view is hidden and resume the
 * side-effect when the view becomes visible.</li>
 * <li>Resumed: The side-effect will listen for changes and react to them
 * asynchronously. Side-effects may be paused and resumed any number of times.
 * </li>
 * <li>Disposed: The side-effect will not listen to or react to changes. It will
 * also remove any strong references to its dependencies. Once a side-effect
 * enters the disposed state it remains in that state until it is garbage
 * collected.</li>
 * </ul>
 *
 * Example usage:
 *
 * <pre>
 * IObservableValue&lt;String&gt; firstName = ...
 * IObservableValue&lt;String&gt; lastName = ...
 * IObservableValue&lt;Boolean&gt; showFullNamePreference = ...
 * Label userName = ...
 *
 * ISideEffect sideEffect = ISideEffect.create(() -&gt; {
 *     String name = showFullNamePreference.get()
 *         ? (firstName.get() + " " + lastName.get())
 *         : firstName.get();
 *     userName.setText("Your name is " + name);
 * });
 * </pre>
 * <p>
 * The above example uses an {@link ISideEffect} to fill in a label with a
 * user's name. It will react automatically to changes in the username and the
 * showFullNamePreference.
 * <p>
 * The same thing could be accomplished by attaching listeners to all three
 * observables, but there are several advantages to using {@link ISideEffect}
 * over listeners.
 * <ul>
 * <li>The {@link ISideEffect} can self-optimize based on branches in the run
 * method. It will remove listeners from any {@link IObservable} which wasn't
 * used on the most recent run. In the above example, there is no need to listen
 * to the lastName field when showFullNamePreference is false.
 * <li>The {@link ISideEffect} will batch changes together and run
 * asynchronously. If firstName and lastName change at the same time, the
 * {@link ISideEffect} will only run once.
 * <li>Since the {@link ISideEffect} doesn't need to be explicitly attached to
 * the observables it affects, it is impossible for it to get out of sync with
 * the underlying data.
 * </ul>
 * <p>
 * Please be aware of a common anti-pattern. Don't create new observables inside
 * an {@link ISideEffect} unless you remember them for future runs. Creating new
 * observables inside an {@link ISideEffect} can easily create infinite loops.
 *
 * <pre>
 * // Bad: May create an infinite loop, since each AvatarObservable instance may
 * // fire an asynchronous event after creation
 * void createControls() {
 * 	ISideEffect sideEffect = ISideEffect.create(() -&gt; {
 * 		IObservableValue&lt;Image&gt; myAvatar = new AvatarObservable();
 *
 * 		myIcon.setImage(myAvatar.getValue());
 * 	});
 * }
 *
 * // Good: The AvatarObservable instance is remembered between invocations of
 * // the side-effect.
 * void createControls() {
 * 	final IObservableValue&lt;Image&gt; myAvatar = new AvatarObservable();
 * 	ISideEffect sideEffect = ISideEffect.create(() -&gt; {
 * 		myIcon.setImage(myAvatar.getValue());
 * 	});
 * }
 * </pre>
 *
 * @since 1.6
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ISideEffect {

	/**
	 * Disposes the side-effect, detaching all listeners and deallocating all
	 * memory used by the side-effect. The side-effect will not execute again
	 * after this method is invoked.
	 * <p>
	 * This method may be invoked more than once.
	 */
	void dispose();

	/**
	 * Returns true if this side-effect has been disposed. A disposed
	 * side-effect will never execute again or retain any strong references to
	 * the observables it uses. A side-effect which has not been disposed has
	 * some possibility of executing again in the future and of retaining strong
	 * references to observables.
	 *
	 * @return true if this side-effect has been disposed.
	 */
	boolean isDisposed();

	/**
	 * Increments the count of the number of times the {@link ISideEffect} has
	 * been paused. If the side-effect has been paused a greater number of times
	 * than it has been resumed, it enters the paused state.
	 * <p>
	 * When a {@link ISideEffect} is paused, this prevents it from running again
	 * until it is resumed. Note that the side-effect will continue listening to
	 * its dependencies while it is paused. If a dependency changes while the
	 * {@link ISideEffect} is paused, the {@link ISideEffect} will run again
	 * after it is resumed.
	 * <p>
	 * A side-effect may be paused and resumed any number of times. You should
	 * use pause instead of dispose if there is a chance you may want to resume
	 * the SideEffect later.
	 */
	void pause();

	/**
	 * Increments the count of the number of times the {@link ISideEffect} has
	 * been resumed. If the side-effect has been resumed an equal number of
	 * times than it has been paused, it leaves the paused state and enters the
	 * resumed state. It is an error to resume {@link ISideEffect} more often
	 * than it has been paused.
	 * <p>
	 * When a {@link ISideEffect} is resumed, it starts reacting to changes in
	 * tracked getters invoked by its runnable. It will continue to react to
	 * changes until it is either paused or disposed. If the {@link ISideEffect}
	 * is dirty, it will be run at the earliest opportunity after this method
	 * returns.
	 */
	void resume();

	/**
	 * Increments the count of the number of times the {@link ISideEffect} has
	 * been resumed. If the side-effect has been resumed an equal or greater
	 * number of times than it has been paused, it leaves the paused state and
	 * enters the resumed state.
	 * <p>
	 * When a {@link ISideEffect} is resumed, it starts reacting to changes in
	 * TrackedGetters invoked by its runnable. It will continue to react to
	 * changes until it is either paused or disposed. If the {@link ISideEffect}
	 * is dirty, it will be run synchronously.
	 * <p>
	 * This is a convenience method which is fully equivalent to calling
	 * {@link #resume} followed by {@link #runIfDirty}, but slightly faster.
	 */
	void resumeAndRunIfDirty();

	/**
	 * Causes the side effect to run synchronously if and only if it is
	 * currently dirty (that is, if one of its dependencies has changed since
	 * the last time it ran). Does nothing if the {@link ISideEffect} is
	 * currently paused.
	 */
	void runIfDirty();

	/**
	 * Adds a listener that will be invoked when this {@link ISideEffect}
	 * instance is disposed. The listener will not be invoked if the receiver
	 * has already been disposed at the time when the listener is attached.
	 *
	 * @param disposalConsumer
	 *            a consumer which will be notified once this
	 *            {@link ISideEffect} is disposed.
	 */
	void addDisposeListener(Consumer<ISideEffect> disposalConsumer);

	/**
	 * Removes a dispose listener from this {@link ISideEffect} instance. Has no
	 * effect if no such listener was previously attached.
	 *
	 * @param disposalConsumer
	 *            a consumer which is supposed to be removed from the dispose
	 *            listener list.
	 */
	void removeDisposeListener(Consumer<ISideEffect> disposalConsumer);

	/**
	 * Creates a new {@link ISideEffect} on the default {@link Realm} but does
	 * not run it immediately. Callers are responsible for invoking
	 * {@link #resume()} or {@link #resumeAndRunIfDirty()} when they want the
	 * runnable to begin executing.
	 *
	 * @param runnable
	 *            the runnable to execute. Must be idempotent.
	 * @return a newly-created {@link ISideEffect} which has not yet been
	 *         activated. Callers are responsible for calling {@link #dispose()}
	 *         on the result when it is no longer needed.
	 */
	static ISideEffect createPaused(Runnable runnable) {
		return new SideEffect(runnable);
	}

	/**
	 * Creates a new {@link ISideEffect} on the given Realm but does not
	 * activate it immediately. Callers are responsible for invoking
	 * {@link #resume()} when they want the runnable to begin executing.
	 *
	 * @param realm
	 *            the realm to execute
	 * @param runnable
	 *            the runnable to execute. Must be idempotent.
	 * @return a newly-created {@link ISideEffect} which has not yet been
	 *         activated. Callers are responsible for calling {@link #dispose()}
	 *         on the result when it is no longer needed.
	 */
	static ISideEffect createPaused(Realm realm, Runnable runnable) {
		return new SideEffect(realm, runnable);
	}

	/**
	 * Runs the given runnable once synchronously. The runnable will then run
	 * again after any tracked getter invoked by the runnable changes. It will
	 * continue doing so until the returned {@link ISideEffect} is disposed. The
	 * returned {@link ISideEffect} is associated with the default realm. The
	 * caller must dispose the returned {@link ISideEffect} when they are done
	 * with it.
	 *
	 * @param runnable
	 *            an idempotent runnable which will be executed once
	 *            synchronously then additional times after any tracked getter
	 *            it uses changes state
	 * @return an {@link ISideEffect} interface that may be used to stop the
	 *         side-effect from running. The {@link Runnable} will not be
	 *         executed anymore after the dispose method is invoked.
	 */
	static ISideEffect create(Runnable runnable) {
		IObservable[] dependencies = ObservableTracker.runAndMonitor(runnable, null, null);

		if (dependencies.length == 0) {
			return SideEffect.NULL_SIDE_EFFECT;
		}

		return new SideEffect(runnable, dependencies);
	}

	/**
	 * Runs the supplier and passes its result to the consumer. The same thing
	 * will happen again after any tracked getter invoked by the supplier
	 * changes. It will continue to do so until the given {@link ISideEffect} is
	 * disposed. The returned {@link ISideEffect} is associated with the default
	 * realm. The caller must dispose the returned {@link ISideEffect} when they
	 * are done with it.
	 * <p>
	 * The ISideEffect will initially be in the resumed state.
	 * <p>
	 * The first invocation of this method will be synchronous. This version is
	 * slightly more efficient than {@link #createResumed(Supplier, Consumer)} and
	 * should be preferred if synchronous execution is acceptable.
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
	 *         executed anymore after the dispose method is invoked.
	 */
	static <T> ISideEffect create(Supplier<T> supplier, Consumer<T> consumer) {
		return ISideEffect.create(SideEffect.makeRunnable(supplier, consumer));
	}

	/**
	 * Runs the supplier and passes its result to the consumer. The same thing
	 * will happen again after any tracked getter invoked by the supplier
	 * changes. It will continue to do so until the given {@link ISideEffect} is
	 * disposed. The returned {@link ISideEffect} is associated with the default
	 * realm. The caller must dispose the returned {@link ISideEffect} when they
	 * are done with it.
	 * <p>
	 * The ISideEffect will initially be in the resumed state.
	 * <p>
	 * The first invocation of this method will be asynchronous. This is useful,
	 * for example, when constructing an {@link ISideEffect} in a constructor
	 * since it ensures that the constructor will run to completion before the
	 * first invocation of the {@link ISideEffect}. However, this extra safety
	 * comes with a small performance cost over
	 * {@link #create(Supplier, Consumer)}.
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
	 *         executed anymore after the dispose method is invoked.
	 */
	static <T> ISideEffect createResumed(Supplier<T> supplier, Consumer<T> consumer) {
		ISideEffect result = ISideEffect.createPaused(SideEffect.makeRunnable(supplier, consumer));
		result.resume();
		return result;
	}

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
	 * to be invoked synchronously, they can call {@link #runIfDirty()}
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
	static <T> ISideEffect consumeOnceAsync(Supplier<T> supplier, Consumer<T> consumer) {
		final ISideEffect[] result = new ISideEffect[1];

		Runnable theRunnable = () -> {
			T value = supplier.get();

			if (value != null) {
				ObservableTracker.setIgnore(true);
				try {
					consumer.accept(value);
				} finally {
					ObservableTracker.setIgnore(false);
				}

				result[0].dispose();
			}
		};

		result[0] = ISideEffect.createPaused(theRunnable);
		result[0].resume();

		return result[0];
	}
}
