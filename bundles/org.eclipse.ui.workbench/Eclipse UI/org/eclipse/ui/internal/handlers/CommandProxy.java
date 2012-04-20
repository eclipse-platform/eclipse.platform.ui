/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.handlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;

/**
 * @since 3.103
 * 
 */
public class CommandProxy {
	private static Method firePreExecute = null;

	private static Method getFirePreExecute() {
		if (firePreExecute == null) {
			try {
				firePreExecute = Command.class.getDeclaredMethod(
						"firePreExecute", ExecutionEvent.class); //$NON-NLS-1$
				firePreExecute.setAccessible(true);
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return firePreExecute;
	}

	public static void firePreExecute(Command command, ExecutionEvent event) {
		try {
			getFirePreExecute().invoke(command, event);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static Method fireNotHandled = null;

	private static Method getFireNotHandled() {
		if (fireNotHandled == null) {
			try {
				fireNotHandled = Command.class.getDeclaredMethod(
						"fireNotHandled", NotHandledException.class);//$NON-NLS-1$
				fireNotHandled.setAccessible(true);
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return fireNotHandled;
	}

	/**
	 * @param command
	 * @param e
	 */
	public static void fireNotHandled(Command command, NotHandledException e) {
		try {
			getFireNotHandled().invoke(command, e);
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private static Method fireNotEnabled = null;

	private static Method getFireNotEnabled() {
		if (fireNotEnabled == null) {
			try {
				fireNotEnabled = Command.class.getDeclaredMethod(
						"fireNotEnabled", NotEnabledException.class);//$NON-NLS-1$
				fireNotEnabled.setAccessible(true);
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return fireNotEnabled;
	}

	/**
	 * @param command
	 * @param exception
	 */
	public static void fireNotEnabled(Command command, NotEnabledException exception) {
		try {
			getFireNotEnabled().invoke(command, exception);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static Method firePostExecuteFailure = null;

	private static Method getFirePostExecuteFailure() {
		if (firePostExecuteFailure == null) {
			try {
				firePostExecuteFailure = Command.class.getDeclaredMethod(
						"firePostExecuteFailure", ExecutionException.class);//$NON-NLS-1$
				firePostExecuteFailure.setAccessible(true);
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return firePostExecuteFailure;
	}

	/**
	 * @param command
	 * @param exception
	 */
	public static void firePostExecuteFailure(Command command, ExecutionException exception) {
		try {
			getFirePostExecuteFailure().invoke(command, exception);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static Method firePostExecuteSuccess = null;

	private static Method getFirePostExecuteSuccess() {
		if (firePostExecuteSuccess == null) {
			try {
				firePostExecuteSuccess = Command.class.getDeclaredMethod(
						"firePostExecuteSuccess", Object.class);//$NON-NLS-1$
				firePostExecuteSuccess.setAccessible(true);
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return firePostExecuteSuccess;
	}

	/**
	 * @param command
	 * @param returnValue
	 */
	public static void firePostExecuteSuccess(Command command, Object returnValue) {
		try {
			getFirePostExecuteSuccess().invoke(command, returnValue);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
