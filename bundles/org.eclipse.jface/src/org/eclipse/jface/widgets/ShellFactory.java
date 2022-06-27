/*******************************************************************************
 * Copyright (c) 2019 Marcus Hoepfner and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marcus Hoepfner - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.widgets;

import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

/**
 * This class provides a convenient shorthand for creating and initializing
 * {@link Shell}. This offers several benefits over creating Shell normal way:
 *
 * <ul>
 * <li>The same factory can be used many times to create several Shell
 * instances</li>
 * <li>The setters on ShellFactory all return "this", allowing them to be
 * chained</li>
 * </ul>
 *
 * Example usage:
 *
 * <pre>
 * Shell shell = ShellFactory.newShell(SWT.BORDER) //
 * 		.text("My Shell") //
 * 		.maximized(true) //
 * 		.create(parent);
 * </pre>
 * <p>
 * The above example creates a maximized shell with a text and creates the shell
 * in "parent". Where "parent" has to be another shell.
 * </p>
 *
 * <pre>
 * GridDataFactory gridDataFactory = GridDataFactory.swtDefaults();
 * ShellFactory shellFactory = ShellFactory.newShell(SWT.PUSH).layout(gridDataFactory::create);
 * shellFactory.text("Shell 1").create(parent);
 * shellFactory.text("Shell 2").create(parent);
 * shellFactory.text("Shell 3").create(parent);
 * </pre>
 * <p>
 * The above example creates three shells using the same instance of
 * ShellFactory. Note the layout method. A Supplier is used to create unique
 * GridData for every single shell.
 * </p>
 *
 * @since 3.21
 */
public final class ShellFactory extends AbstractCompositeFactory<ShellFactory, Shell> {

	/**
	 * @param factoryClass
	 * @param controlCreator
	 */
	private ShellFactory(int style) {
		super(ShellFactory.class, (Composite parent) -> new Shell((Shell) parent, style));
	}

	/**
	 * Creates the shell in the given display.
	 *
	 * @param display
	 * @return the created shell
	 *
	 * @since 3.28
	 */
	public final Shell create(Display display) {
		Shell shell = new Shell(display);
		applyProperties(shell);
		return shell;
	}

	/**
	 * Creates a new ShellFactory with the given style. Refer to
	 * {@link Shell#Shell(Shell, int)} for possible styles.
	 *
	 * @param style
	 * @return a new ShellFactory instance
	 */
	public static ShellFactory newShell(int style) {
		return new ShellFactory(style);
	}

	/**
	 * Sets the receiver's text, which is the string that the window manager will
	 * typically display as the receiver's <em>title</em>, to the argument, which
	 * must not be null.
	 * <p>
	 * Note: If control characters like '\n', '\t' etc. are used in the string, then
	 * the behavior is platform dependent.
	 * </p>
	 *
	 * @param text
	 * @return this
	 *
	 * @see Shell#setText(String)
	 */
	public ShellFactory text(String text) {
		addProperty(shell -> shell.setText(text));
		return this;
	}

	/**
	 * Sets the minimized stated of the receiver. If the argument is
	 * <code>true</code> causes the receiver to switch to the minimized state, and
	 * if the argument is <code>false</code> and the receiver was previously
	 * minimized, causes the receiver to switch back to either the maximized or
	 * normal states.
	 * <p>
	 * Note: The result of intermixing calls to <code>setMaximized(true)</code> and
	 * <code>setMinimized(true)</code> will vary by platform. Typically, the
	 * behavior will match the platform user's expectations, but not always. This
	 * should be avoided if possible.
	 * </p>
	 *
	 * @param minimized the new minimized state
	 * @return this
	 *
	 * @see Shell#setMinimized(boolean)
	 * @see Shell#setMaximized(boolean)
	 */
	public ShellFactory minimized(boolean minimized) {
		addProperty(shell -> shell.setMinimized(minimized));
		return this;
	}

	/**
	 * Sets the maximized state of the receiver. If the argument is
	 * <code>true</code> causes the receiver to switch to the maximized state, and
	 * if the argument is <code>false</code> and the receiver was previously
	 * maximized, causes the receiver to switch back to either the minimized or
	 * normal states.
	 * <p>
	 * Note: The result of intermixing calls to <code>setMaximized(true)</code> and
	 * <code>setMinimized(true)</code> will vary by platform. Typically, the
	 * behavior will match the platform user's expectations, but not always. This
	 * should be avoided if possible.
	 * </p>
	 *
	 * @param maximized the new maximized state
	 * @return this
	 *
	 * @see Shell#setMinimized(boolean)
	 * @see Shell#setMaximized(boolean)
	 */
	public ShellFactory maximized(boolean maximized) {
		addProperty(shell -> shell.setMaximized(maximized));
		return this;
	}

	/**
	 * Sets the full screen state of the receiver. If the argument is
	 * <code>true</code> causes the receiver to switch to the full screen state, and
	 * if the argument is <code>false</code> and the receiver was previously
	 * switched into full screen state, causes the receiver to switch back to either
	 * the maximized or normal states.
	 * <p>
	 * Note: The result of intermixing calls to <code>setFullScreen(true)</code>,
	 * <code>setMaximized(true)</code> and <code>setMinimized(true)</code> will vary
	 * by platform. Typically, the behavior will match the platform user's
	 * expectations, but not always. This should be avoided if possible.
	 * </p>
	 *
	 * @param fullScreen the new fullscreen state
	 * @return this
	 *
	 * @see Shell#setFullScreen(boolean)
	 */
	public ShellFactory fullScreen(boolean fullScreen) {
		addProperty(shell -> shell.setFullScreen(fullScreen));
		return this;
	}

	/**
	 * Sets the shell's menu bar by a {@link Function}. Implemented as callback
	 * since the shell itself is needed to create the menu.
	 *
	 * <pre>
	 * Shell shell = ShellFactory.newShell(SWT.BORDER) //
	 * 		.text("My Shell") //
	 * 		.menuBar(shell -> new Menu(shell, SWT.BAR)) //
	 * 		.create(parent);
	 * </pre>
	 *
	 * Or with more logic to create the menu (e.g. add items):
	 *
	 * <pre>
	 * Shell shell = ShellFactory.newShell(SWT.BORDER) //
	 * 		.text("My Shell") //
	 * 		.menuBar(this::fillMenu) //
	 * 		.create(parent);
	 * </pre>
	 *
	 * @param menuFunction the function (or lambda) to create the menu for the given
	 *                     shell
	 * @return this
	 *
	 * @see Shell#setMenuBar(org.eclipse.swt.widgets.Menu)
	 */
	public ShellFactory menuBar(Function<Decorations, Menu> menuFunction) {
		addProperty(shell -> shell.setMenuBar(menuFunction.apply(shell)));
		return this;
	}

	/**
	 * Creates a {@link ShellListener} and registers it for the activated event. If
	 * event is raised it calls the given consumer. The {@link ShellEvent} is passed
	 * to the consumer.
	 *
	 * @param consumer the consumer whose accept method is called
	 * @return this
	 *
	 * @see Shell#addShellListener(ShellListener)
	 * @see ShellListener#shellActivatedAdapter(Consumer)
	 */
	public ShellFactory onActivate(Consumer<ShellEvent> consumer) {
		addProperty(shell -> shell.addShellListener(ShellListener.shellActivatedAdapter(consumer)));
		return this;
	}

	/**
	 * Creates a {@link ShellListener} and registers it for the deactivated event.
	 * If event is raised it calls the given consumer. The {@link ShellEvent} is
	 * passed to the consumer.
	 *
	 * @param consumer the consumer whose accept method is called
	 * @return this
	 *
	 * @see Shell#addShellListener(ShellListener)
	 * @see ShellListener#shellDeactivatedAdapter(Consumer)
	 */
	public ShellFactory onDeactivate(Consumer<ShellEvent> consumer) {
		addProperty(shell -> shell.addShellListener(ShellListener.shellDeactivatedAdapter(consumer)));
		return this;
	}

	/**
	 * Creates a {@link ShellListener} and registers it for the iconified
	 * (minimized) event. If event is raised it calls the given consumer. The
	 * {@link ShellEvent} is passed to the consumer.
	 *
	 * @param consumer the consumer whose accept method is called
	 * @return this
	 *
	 * @see Shell#addShellListener(ShellListener)
	 * @see ShellListener#shellIconifiedAdapter(Consumer)
	 */
	public ShellFactory onIconify(Consumer<ShellEvent> consumer) {
		addProperty(shell -> shell.addShellListener(ShellListener.shellIconifiedAdapter(consumer)));
		return this;
	}

	/**
	 * Creates a {@link ShellListener} and registers it for the deiconified
	 * (un-minimized) event. If event is raised it calls the given consumer. The
	 * {@link ShellEvent} is passed to the consumer.
	 *
	 * @param consumer the consumer whose accept method is called
	 * @return this
	 *
	 * @see Shell#addShellListener(ShellListener)
	 * @see ShellListener#shellDeiconifiedAdapter(Consumer)
	 */
	public ShellFactory onDeiconify(Consumer<ShellEvent> consumer) {
		addProperty(shell -> shell.addShellListener(ShellListener.shellDeiconifiedAdapter(consumer)));
		return this;
	}

	/**
	 * Creates a {@link ShellListener} and registers it for the closed event. If
	 * event is raised it calls the given consumer. The {@link ShellEvent} is passed
	 * to the consumer.
	 *
	 * @param consumer the consumer whose accept method is called
	 * @return this
	 *
	 * @see Shell#addShellListener(ShellListener)
	 * @see ShellListener#shellClosedAdapter(Consumer)
	 */
	public ShellFactory onClose(Consumer<ShellEvent> consumer) {
		addProperty(shell -> shell.addShellListener(ShellListener.shellClosedAdapter(consumer)));
		return this;
	}
}