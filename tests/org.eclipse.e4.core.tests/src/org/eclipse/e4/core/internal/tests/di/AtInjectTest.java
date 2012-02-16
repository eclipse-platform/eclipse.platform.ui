/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.core.internal.tests.di;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.atinject.tck.Tck;
import org.atinject.tck.auto.Car;
import org.atinject.tck.auto.Convertible;
import org.atinject.tck.auto.Drivers;
import org.atinject.tck.auto.DriversSeat;
import org.atinject.tck.auto.Engine;
import org.atinject.tck.auto.FuelTank;
import org.atinject.tck.auto.Seat;
import org.atinject.tck.auto.Tire;
import org.atinject.tck.auto.V8Engine;
import org.atinject.tck.auto.accessories.Cupholder;
import org.atinject.tck.auto.accessories.SpareTire;
import org.eclipse.e4.core.di.IInjector;
import org.eclipse.e4.core.di.InjectorFactory;

public class AtInjectTest extends TestSuite {

	public static Test suite() {
		IInjector injector = InjectorFactory.getDefault();
		
		injector.addBinding(SpareTire.class);
		injector.addBinding(Seat.class);
		injector.addBinding(DriversSeat.class);
		injector.addBinding(Cupholder.class);
		injector.addBinding(Tire.class);
		injector.addBinding(FuelTank.class);
		
		injector.addBinding(Car.class).implementedBy(Convertible.class);
		injector.addBinding(Seat.class).named(Drivers.class.getName()).implementedBy(DriversSeat.class);
		injector.addBinding(Engine.class).implementedBy(V8Engine.class);
		injector.addBinding(Tire.class).named("spare").implementedBy(SpareTire.class);

		Car car = injector.make(Car.class, null);
		return Tck.testsFor(car, true, true);
	}
}
