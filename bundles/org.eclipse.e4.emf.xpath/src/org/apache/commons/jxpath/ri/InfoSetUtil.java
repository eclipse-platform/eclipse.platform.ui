/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jxpath.ri;

import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.VariablePointer;

/**
 * Type conversions, XPath style.
 */
public class InfoSetUtil {

    private static final Double ZERO = Double.valueOf(0);
    private static final Double ONE = Double.valueOf(1);
    private static final Double NOT_A_NUMBER = Double.valueOf(Double.NaN);

    /**
     * Converts the supplied object to String.
     * @param object to convert
     * @return String value
     */
    public static String stringValue(final Object object) {
        if (object instanceof String) {
            return (String) object;
        }
        if (object instanceof Number) {
            final double d = ((Number) object).doubleValue();
            final long l = ((Number) object).longValue();
            return d == l ? String.valueOf(l) : String.valueOf(d);
        }
        if (object instanceof Boolean) {
            return ((Boolean) object).booleanValue() ? "true" : "false";
        }
        if (object == null) {
            return "";
        }
        if (object instanceof NodePointer) {
            return stringValue(((NodePointer) object).getValue());
        }
        if (object instanceof EvalContext) {
            final EvalContext ctx = (EvalContext) object;
            final Pointer ptr = ctx.getSingleNodePointer();
            return ptr == null ? "" : stringValue(ptr);
        }
        return String.valueOf(object);
    }

    /**
     * Converts the supplied object to Number.
     * @param object to convert
     * @return Number result
     */
    public static Number number(final Object object) {
        if (object instanceof Number) {
            return (Number) object;
        }
        if (object instanceof Boolean) {
            return ((Boolean) object).booleanValue() ? ONE : ZERO;
        }
        if (object instanceof String) {
            try {
                return Double.valueOf((String) object);
            }
            catch (final NumberFormatException ex) {
                return NOT_A_NUMBER;
            }
        }
        if (object instanceof EvalContext) {
            final EvalContext ctx = (EvalContext) object;
            final Pointer ptr = ctx.getSingleNodePointer();
            return ptr == null ? NOT_A_NUMBER : number(ptr);
        }
        if (object instanceof NodePointer) {
            return number(((NodePointer) object).getValue());
        }
        return number(stringValue(object));
    }

    /**
     * Converts the supplied object to double.
     * @param object to convert
     * @return double
     */
    public static double doubleValue(final Object object) {
        if (object instanceof Number) {
            return ((Number) object).doubleValue();
        }
        if (object instanceof Boolean) {
            return ((Boolean) object).booleanValue() ? 0.0 : 1.0;
        }
        if (object instanceof String) {
            if (object.equals("")) {
                return 0.0;
            }
            try {
                return Double.parseDouble((String) object);
            }
            catch (final NumberFormatException ex) {
                return Double.NaN;
            }
        }
        if (object instanceof NodePointer) {
            return doubleValue(((NodePointer) object).getValue());
        }
        if (object instanceof EvalContext) {
            final EvalContext ctx = (EvalContext) object;
            final Pointer ptr = ctx.getSingleNodePointer();
            return ptr == null ? Double.NaN : doubleValue(ptr);
        }
        return doubleValue(stringValue(object));
    }

    /**
     * Converts the supplied object to boolean.
     * @param object to convert
     * @return boolean
     */
    public static boolean booleanValue(final Object object) {
        if (object instanceof Number) {
            final double value = ((Number) object).doubleValue();
            final int negZero = -0;
            return value != 0 && value != negZero && !Double.isNaN(value);
        }
        if (object instanceof Boolean) {
            return ((Boolean) object).booleanValue();
        }
        if (object instanceof EvalContext) {
            final EvalContext ctx = (EvalContext) object;
            final Pointer ptr = ctx.getSingleNodePointer();
            return ptr != null && booleanValue(ptr);
        }
        if (object instanceof String) {
            return ((String) object).length() != 0;
        }
        if (object instanceof NodePointer) {
            NodePointer pointer = (NodePointer) object;
            if (pointer instanceof VariablePointer) {
                return booleanValue(pointer.getNode());
            }
            pointer = pointer.getValuePointer();
            return pointer.isActual();
        }
        return object != null;
    }
}
