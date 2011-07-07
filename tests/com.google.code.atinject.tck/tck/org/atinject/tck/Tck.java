/*
 * Copyright (C) 2009 The JSR-330 Expert Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.atinject.tck;

import org.atinject.tck.auto.Car;
import org.atinject.tck.auto.Convertible;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Maufactures the compatibility test suite. Call {@link #testsFor testsFor}
 * from a JUnit static {@code suite} method:
 *
 * <pre>
 * import junit.framework.Test;
 * import org.atinject.tck.Tck;
 *
 * public class MyTck {
 *   public static Test suite() {
 *     Car car = new MyInjector().getInstance(Car.class);
 *     return Tck.testsFor(car, true, true);
 *   }
 * }
 * </pre>
 *
 * <p>Run the tests using JUnit. For example:
 *
 * <pre>
 * java junit.textui.TestRunner MyTck
 * </pre>
 */
public class Tck {

    private Tck() {}

    /**
     * Constructs a test suite for the given {@link Car} instance. Create the
     * {@code Car} instance using an injector with the following configuration:
     *
     * <ul>
     *   <li>{@link org.atinject.tck.auto.Car} is implemented by
     *       {@link org.atinject.tck.auto.Convertible Convertible}.
     *   <li>{@link org.atinject.tck.auto.Drivers @Drivers} {@link org.atinject.tck.auto.Seat Seat} is
     *       implemented by {@link org.atinject.tck.auto.DriversSeat DriversSeat}.
     *   <li>{@link org.atinject.tck.auto.Engine Engine} is implemented by
     *       {@link org.atinject.tck.auto.V8Engine V8Engine}.
     *   <li>{@link javax.inject.Named @Named("spare")} {@link org.atinject.tck.auto.Tire Tire} is implemented by
     *       {@link org.atinject.tck.auto.accessories.SpareTire SpareTire}.
     *   <li>The following concrete classes may also be injected: {@link org.atinject.tck.auto.accessories.Cupholder
     *       Cupholder}, {@link org.atinject.tck.auto.Tire Tire} and {@link org.atinject.tck.auto.FuelTank
     *       FuelTank}.
     * </ul>
     *
     * <p>If static member injection is supported, the static members of the
     * following types shall also be injected:
     * {@link org.atinject.tck.auto.Convertible Convertible},
     * {@link org.atinject.tck.auto.Tire Tire}, and {@link
     * org.atinject.tck.auto.accessories.SpareTire SpareTire}.
     *
     * @param car to test
     * @param supportsStatic if the injector supports static member injection
     * @param supportsPrivate if the injector supports private member injection
     *
     * @throws NullPointerException if car is null
     * @throws ClassCastException if car doesn't extend Convertible
     */
    public static Test testsFor(Car car, boolean supportsStatic,
            boolean supportsPrivate) {
        if (car == null) {
            throw new NullPointerException("car");
        }

        if (!(car instanceof Convertible)) {
            throw new ClassCastException("car doesn't implement Convertible");
        }

        Convertible.localConvertible.set((Convertible) car);
        try {
            TestSuite suite = new TestSuite(Convertible.Tests.class);
            if (supportsStatic) {
                suite.addTestSuite(Convertible.StaticTests.class);
            }
            if (supportsPrivate) {
                suite.addTestSuite(Convertible.PrivateTests.class);
            }
            return suite;
        } finally {
            Convertible.localConvertible.remove();
        }
    }
}
