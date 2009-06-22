/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.core.services.internal.context;

import java.text.NumberFormat;
import org.eclipse.e4.core.services.annotations.In;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IContextFunction;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextFunction;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.core.tests.services.TestActivator;
import org.osgi.framework.ServiceRegistration;

/**
 *
 */
public class ContextExample {
	class Crayon {
		@In
		IPaletteService pallete;

		public void draw() {
			if (pallete == null)
				System.out.println("No palette");
			else
				System.out.println("My pen is:  " + pallete.getColor());
		}
	}

	static enum Color {
		RED, BLUE, YELLOW, GREEN, ORANGE, PURPLE;
	}

	interface IPaletteService {
		public Color getColor();
	}

	class PaletteImpl implements IPaletteService {
		private final Color color;

		PaletteImpl(Color color) {
			this.color = color;
		}

		public Color getColor() {
			return color;
		}
	}

	static class ComplementaryColor extends ContextFunction {
		public Object compute(IEclipseContext context, Object[] arguments) {
			switch ((Color) context.get("color")) {
			case RED:
				return Color.GREEN;
			case GREEN:
				return Color.RED;
			case BLUE:
				return Color.ORANGE;
			case ORANGE:
				return Color.BLUE;
			case YELLOW:
				return Color.PURPLE;
			case PURPLE:
				return Color.YELLOW;
			default:
				return null;
			}
		}
	}

	static class ResourceSelection implements IContextFunction {
		public Object compute(IEclipseContext context, Object[] arguments) {
			return null;
		}
	}

	public static void main(String[] arguments) {
		new ContextExample().price();
	}

	/**
	 * 
	 */
	public void run() {
		IEclipseContext parent = EclipseContextFactory.create();
		parent.set("complement", new ComplementaryColor());
		IEclipseContext context = EclipseContextFactory.create(parent, null);
		context.set("color", Color.YELLOW);
		Crayon crayon = new Crayon();
		ContextInjectionFactory.inject(crayon, context);
		crayon.draw();
	}

	public void runWithService() {
		ServiceRegistration reg = TestActivator.bundleContext.registerService(IPaletteService.class
				.getName(), new PaletteImpl(Color.BLUE), null);
		IEclipseContext context = EclipseContextFactory
				.createServiceContext(TestActivator.bundleContext);
		Crayon crayon = new Crayon();
		ContextInjectionFactory.inject(crayon, context);
		crayon.draw();
		reg.unregister();
		crayon.draw();

	}

	public void run2() {
		IEclipseContext parent = EclipseContextFactory.create();
		parent.set("complement", new ComplementaryColor());
		IEclipseContext child = EclipseContextFactory.create(parent, null);
		child.set("color", Color.RED);
		System.out.println(child.get("color"));
		System.out.println(child.get("complement"));

	}

	public void run3() {
		// IEclipseContext context = EclipseContextFactory.create();
		// Object[] args = new Object[] {IResource.class};
		// IResource[] resources = context.get("Selection", args);
	}

	double total = 0;

	public void price() {
		final IEclipseContext context = EclipseContextFactory.create();
		context.set("price", 19.99);
		context.set("tax", 0.05);
		context.runAndTrack(new Runnable() {
			public void run() {
				total = (Double) context.get("price") * (1.0 + (Double) context.get("tax"));
			}

			@Override
			public String toString() {
				return "calculator";
			}
		});
		print(total);
		context.set("tax", 0.07);
		print(total);
	}

	private void print(double price) {
		System.out.println(NumberFormat.getCurrencyInstance().format(price));
	}
}
