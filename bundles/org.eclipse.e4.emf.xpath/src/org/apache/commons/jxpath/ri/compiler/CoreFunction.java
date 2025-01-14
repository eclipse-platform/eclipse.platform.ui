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
package org.apache.commons.jxpath.ri.compiler;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Locale;

import org.apache.commons.jxpath.BasicNodeSet;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.JXPathInvalidSyntaxException;
import org.apache.commons.jxpath.NodeSet;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.InfoSetUtil;
import org.apache.commons.jxpath.ri.axes.NodeSetContext;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * An element of the compile tree representing one of built-in functions
 * like "position()" or "number()".
 */
public class CoreFunction extends Operation {

    private static final Double ZERO = Double.valueOf(0);
    private final int functionCode;

    /**
     * Create a new CoreFunction.
     * @param functionCode int function code
     * @param args argument Expressions
     */
    public CoreFunction(final int functionCode, final Expression[] args) {
        super(args);
        this.functionCode = functionCode;
    }

    /**
     * Gets the function code.
     * @return int function code
     */
    public int getFunctionCode() {
        return functionCode;
    }

    /**
     * Gets the name of this function.
     * @return String function name
     */
    protected String getFunctionName() {
        switch (functionCode) {
            case Compiler.FUNCTION_LAST :
                return "last";
            case Compiler.FUNCTION_POSITION :
                return "position";
            case Compiler.FUNCTION_COUNT :
                return "count";
            case Compiler.FUNCTION_ID :
                return "id";
            case Compiler.FUNCTION_LOCAL_NAME :
                return "local-name";
            case Compiler.FUNCTION_NAMESPACE_URI :
                return "namespace-uri";
            case Compiler.FUNCTION_NAME :
                return "name";
            case Compiler.FUNCTION_STRING :
                return "string";
            case Compiler.FUNCTION_CONCAT :
                return "concat";
            case Compiler.FUNCTION_STARTS_WITH :
                return "starts-with";
            case Compiler.FUNCTION_ENDS_WITH :
                return "ends-with";
            case Compiler.FUNCTION_CONTAINS :
                return "contains";
            case Compiler.FUNCTION_SUBSTRING_BEFORE :
                return "substring-before";
            case Compiler.FUNCTION_SUBSTRING_AFTER :
                return "substring-after";
            case Compiler.FUNCTION_SUBSTRING :
                return "substring";
            case Compiler.FUNCTION_STRING_LENGTH :
                return "string-length";
            case Compiler.FUNCTION_NORMALIZE_SPACE :
                return "normalize-space";
            case Compiler.FUNCTION_TRANSLATE :
                return "translate";
            case Compiler.FUNCTION_BOOLEAN :
                return "boolean";
            case Compiler.FUNCTION_NOT :
                return "not";
            case Compiler.FUNCTION_TRUE :
                return "true";
            case Compiler.FUNCTION_FALSE :
                return "false";
            case Compiler.FUNCTION_LANG :
                return "lang";
            case Compiler.FUNCTION_NUMBER :
                return "number";
            case Compiler.FUNCTION_SUM :
                return "sum";
            case Compiler.FUNCTION_FLOOR :
                return "floor";
            case Compiler.FUNCTION_CEILING :
                return "ceiling";
            case Compiler.FUNCTION_ROUND :
                return "round";
            case Compiler.FUNCTION_KEY :
                return "key";
            case Compiler.FUNCTION_FORMAT_NUMBER:
                return "format-number";
            default:
                return "unknownFunction" + functionCode + "()";
        }
    }

    /**
     * Convenience method to return the first argument.
     * @return Expression
     */
    public Expression getArg1() {
        return args[0];
    }

    /**
     * Convenience method to return the second argument.
     * @return Expression
     */
    public Expression getArg2() {
        return args[1];
    }

    /**
     * Convenience method to return the third argument.
     * @return Expression
     */
    public Expression getArg3() {
        return args[2];
    }

    /**
     * Gets the number of argument Expressions.
     * @return int count
     */
    public int getArgumentCount() {
        if (args == null) {
            return 0;
        }
        return args.length;
    }

    /**
     * Returns true if any argument is context dependent or if
     * the function is last(), position(), boolean(), local-name(),
     * name(), string(), lang(), number().
     * @return boolean
     */
    @Override
    public boolean computeContextDependent() {
        if (super.computeContextDependent()) {
            return true;
        }

        switch (functionCode) {
            case Compiler.FUNCTION_LAST:
            case Compiler.FUNCTION_POSITION:
                return true;

            case Compiler.FUNCTION_BOOLEAN:
            case Compiler.FUNCTION_LOCAL_NAME:
            case Compiler.FUNCTION_NAME:
            case Compiler.FUNCTION_NAMESPACE_URI:
            case Compiler.FUNCTION_STRING:
            case Compiler.FUNCTION_LANG:
            case Compiler.FUNCTION_NUMBER:
                return args == null || args.length == 0;

            case Compiler.FUNCTION_FORMAT_NUMBER:
                return args != null && args.length == 2;

            case Compiler.FUNCTION_COUNT:
            case Compiler.FUNCTION_ID:
            case Compiler.FUNCTION_CONCAT:
            case Compiler.FUNCTION_STARTS_WITH:
            case Compiler.FUNCTION_ENDS_WITH:
            case Compiler.FUNCTION_CONTAINS:
            case Compiler.FUNCTION_SUBSTRING_BEFORE:
            case Compiler.FUNCTION_SUBSTRING_AFTER:
            case Compiler.FUNCTION_SUBSTRING:
            case Compiler.FUNCTION_STRING_LENGTH:
            case Compiler.FUNCTION_NORMALIZE_SPACE:
            case Compiler.FUNCTION_TRANSLATE:
            case Compiler.FUNCTION_NOT:
            case Compiler.FUNCTION_TRUE:
            case Compiler.FUNCTION_FALSE:
            case Compiler.FUNCTION_SUM:
            case Compiler.FUNCTION_FLOOR:
            case Compiler.FUNCTION_CEILING:
            case Compiler.FUNCTION_ROUND:
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(getFunctionName());
        buffer.append('(');
        final Expression[] args = getArguments();
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if (i > 0) {
                    buffer.append(", ");
                }
                buffer.append(args[i]);
            }
        }
        buffer.append(')');
        return buffer.toString();
    }

    @Override
    public Object compute(final EvalContext context) {
        return computeValue(context);
    }

    @Override
    public Object computeValue(final EvalContext context) {
        switch (functionCode) {
            case Compiler.FUNCTION_LAST :
                return functionLast(context);
            case Compiler.FUNCTION_POSITION :
                return functionPosition(context);
            case Compiler.FUNCTION_COUNT :
                return functionCount(context);
            case Compiler.FUNCTION_LANG :
                return functionLang(context);
            case Compiler.FUNCTION_ID :
                return functionID(context);
            case Compiler.FUNCTION_LOCAL_NAME :
                return functionLocalName(context);
            case Compiler.FUNCTION_NAMESPACE_URI :
                return functionNamespaceURI(context);
            case Compiler.FUNCTION_NAME :
                return functionName(context);
            case Compiler.FUNCTION_STRING :
                return functionString(context);
            case Compiler.FUNCTION_CONCAT :
                return functionConcat(context);
            case Compiler.FUNCTION_STARTS_WITH :
                return functionStartsWith(context);
            case Compiler.FUNCTION_ENDS_WITH :
                return functionEndsWith(context);
            case Compiler.FUNCTION_CONTAINS :
                return functionContains(context);
            case Compiler.FUNCTION_SUBSTRING_BEFORE :
                return functionSubstringBefore(context);
            case Compiler.FUNCTION_SUBSTRING_AFTER :
                return functionSubstringAfter(context);
            case Compiler.FUNCTION_SUBSTRING :
                return functionSubstring(context);
            case Compiler.FUNCTION_STRING_LENGTH :
                return functionStringLength(context);
            case Compiler.FUNCTION_NORMALIZE_SPACE :
                return functionNormalizeSpace(context);
            case Compiler.FUNCTION_TRANSLATE :
                return functionTranslate(context);
            case Compiler.FUNCTION_BOOLEAN :
                return functionBoolean(context);
            case Compiler.FUNCTION_NOT :
                return functionNot(context);
            case Compiler.FUNCTION_TRUE :
                return functionTrue(context);
            case Compiler.FUNCTION_FALSE :
                return functionFalse(context);
            case Compiler.FUNCTION_NULL :
                return functionNull(context);
            case Compiler.FUNCTION_NUMBER :
                return functionNumber(context);
            case Compiler.FUNCTION_SUM :
                return functionSum(context);
            case Compiler.FUNCTION_FLOOR :
                return functionFloor(context);
            case Compiler.FUNCTION_CEILING :
                return functionCeiling(context);
            case Compiler.FUNCTION_ROUND :
                return functionRound(context);
            case Compiler.FUNCTION_KEY :
                return functionKey(context);
            case Compiler.FUNCTION_FORMAT_NUMBER :
                return functionFormatNumber(context);
            default:
                return null;
        }
    }

    /**
     * last() implementation.
     * @param context evaluation context
     * @return Number
     */
    protected Object functionLast(final EvalContext context) {
        assertArgCount(0);
        // Move the position to the beginning and iterate through
        // the context to count nodes.
        final int old = context.getCurrentPosition();
        context.reset();
        int count = 0;
        while (context.nextNode()) {
            count++;
        }

        // Restore the current position.
        if (old != 0) {
            context.setPosition(old);
        }
        return Double.valueOf(count);
    }

    /**
     * position() implementation.
     * @param context evaluation context
     * @return Number
     */
    protected Object functionPosition(final EvalContext context) {
        assertArgCount(0);
        return Integer.valueOf(context.getCurrentPosition());
    }

    /**
     * count() implementation.
     * @param context evaluation context
     * @return Number
     */
    protected Object functionCount(final EvalContext context) {
        assertArgCount(1);
        final Expression arg1 = getArg1();
        int count = 0;
        Object value = arg1.compute(context);
        if (value instanceof NodePointer) {
            value = ((NodePointer) value).getValue();
        }
        if (value instanceof EvalContext) {
            final EvalContext ctx = (EvalContext) value;
            while (ctx.hasNext()) {
                ctx.next();
                count++;
            }
        }
        else if (value instanceof Collection) {
            count = ((Collection) value).size();
        }
        else if (value == null) {
            count = 0;
        }
        else {
            count = 1;
        }
        return Double.valueOf(count);
    }

    /**
     * lang() implementation.
     * @param context evaluation context
     * @return Boolean
     */
    protected Object functionLang(final EvalContext context) {
        assertArgCount(1);
        final String lang = InfoSetUtil.stringValue(getArg1().computeValue(context));
        final NodePointer pointer = (NodePointer) context.getSingleNodePointer();
        if (pointer == null) {
            return Boolean.FALSE;
        }
        return pointer.isLanguage(lang) ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * id() implementation.
     * @param context evaluation context
     * @return Pointer
     */
    protected Object functionID(final EvalContext context) {
        assertArgCount(1);
        final String id = InfoSetUtil.stringValue(getArg1().computeValue(context));
        final JXPathContext jxpathContext = context.getJXPathContext();
        final NodePointer pointer = (NodePointer) jxpathContext.getContextPointer();
        return pointer.getPointerByID(jxpathContext, id);
    }

    /**
     * key() implementation.
     * @param context evaluation context
     * @return various Object
     */
    protected Object functionKey(final EvalContext context) {
        assertArgCount(2);
        final String key = InfoSetUtil.stringValue(getArg1().computeValue(context));
        Object value = getArg2().compute(context);
        EvalContext ec = null;
        if (value instanceof EvalContext) {
            ec = (EvalContext) value;
            if (ec.hasNext()) {
                value = ((NodePointer) ec.next()).getValue();
            }
            else { // empty context -> empty results
                return new NodeSetContext(context, new BasicNodeSet());
            }
        }
        final JXPathContext jxpathContext = context.getJXPathContext();
        NodeSet nodeSet = jxpathContext.getNodeSetByKey(key, value);
        if (ec != null && ec.hasNext()) {
            final BasicNodeSet accum = new BasicNodeSet();
            accum.add(nodeSet);
            while (ec.hasNext()) {
                value = ((NodePointer) ec.next()).getValue();
                accum.add(jxpathContext.getNodeSetByKey(key, value));
            }
            nodeSet = accum;
        }
        return new NodeSetContext(context, nodeSet);
    }

    /**
     * namespace-uri() implementation.
     * @param context evaluation context
     * @return String
     */
    protected Object functionNamespaceURI(final EvalContext context) {
        if (getArgumentCount() == 0) {
            final NodePointer ptr = context.getCurrentNodePointer();
            final String str = ptr.getNamespaceURI();
            return str == null ? "" : str;
        }
        assertArgCount(1);
        final Object set = getArg1().compute(context);
        if (set instanceof EvalContext) {
            final EvalContext ctx = (EvalContext) set;
            if (ctx.hasNext()) {
                final NodePointer ptr = (NodePointer) ctx.next();
                final String str = ptr.getNamespaceURI();
                return str == null ? "" : str;
            }
        }
        return "";
    }

    /**
     * local-name() implementation.
     * @param context evaluation context
     * @return String
     */
    protected Object functionLocalName(final EvalContext context) {
        if (getArgumentCount() == 0) {
            final NodePointer ptr = context.getCurrentNodePointer();
            return ptr.getName().getName();
        }
        assertArgCount(1);
        final Object set = getArg1().compute(context);
        if (set instanceof EvalContext) {
            final EvalContext ctx = (EvalContext) set;
            if (ctx.hasNext()) {
                final NodePointer ptr = (NodePointer) ctx.next();
                return ptr.getName().getName();
            }
        }
        return "";
    }

    /**
     * name() implementation.
     * @param context evaluation context
     * @return String
     */
    protected Object functionName(final EvalContext context) {
        if (getArgumentCount() == 0) {
            final NodePointer ptr = context.getCurrentNodePointer();
            return ptr.getName().toString();
        }
        assertArgCount(1);
        final Object set = getArg1().compute(context);
        if (set instanceof EvalContext) {
            final EvalContext ctx = (EvalContext) set;
            if (ctx.hasNext()) {
                final NodePointer ptr = (NodePointer) ctx.next();
                return ptr.getName().toString();
            }
        }
        return "";
    }

    /**
     * string() implementation.
     * @param context evaluation context
     * @return String
     */
    protected Object functionString(final EvalContext context) {
        if (getArgumentCount() == 0) {
            return InfoSetUtil.stringValue(context.getCurrentNodePointer());
        }
        assertArgCount(1);
        return InfoSetUtil.stringValue(getArg1().computeValue(context));
    }

    /**
     * concat() implementation.
     * @param context evaluation context
     * @return String
     */
    protected Object functionConcat(final EvalContext context) {
        if (getArgumentCount() < 2) {
            assertArgCount(2);
        }
        final StringBuilder buffer = new StringBuilder();
        final Expression[] args = getArguments();
        for (final Expression arg : args) {
            buffer.append(InfoSetUtil.stringValue(arg.compute(context)));
        }
        return buffer.toString();
    }

    /**
     * starts-with() implementation.
     * @param context evaluation context
     * @return Boolean
     */
    protected Object functionStartsWith(final EvalContext context) {
        assertArgCount(2);
        final String s1 = InfoSetUtil.stringValue(getArg1().computeValue(context));
        final String s2 = InfoSetUtil.stringValue(getArg2().computeValue(context));
        return s1.startsWith(s2) ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * ends-with() implementation.
     * @param context evaluation context
     * @return Boolean
     * @since 1.4
     */
    protected Object functionEndsWith(final EvalContext context) {
        assertArgCount(2);
        final String s1 = InfoSetUtil.stringValue(getArg1().computeValue(context));
        final String s2 = InfoSetUtil.stringValue(getArg2().computeValue(context));
        return s1.endsWith(s2) ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * contains() implementation.
     * @param context evaluation context
     * @return Boolean
     */
    protected Object functionContains(final EvalContext context) {
        assertArgCount(2);
        final String s1 = InfoSetUtil.stringValue(getArg1().computeValue(context));
        final String s2 = InfoSetUtil.stringValue(getArg2().computeValue(context));
        return Boolean.valueOf(s1.contains(s2));
    }

    /**
     * substring-before() implementation.
     * @param context evaluation context
     * @return String
     */
    protected Object functionSubstringBefore(final EvalContext context) {
        assertArgCount(2);
        final String s1 = InfoSetUtil.stringValue(getArg1().computeValue(context));
        final String s2 = InfoSetUtil.stringValue(getArg2().computeValue(context));
        final int index = s1.indexOf(s2);
        if (index == -1) {
            return "";
        }
        return s1.substring(0, index);
    }

    /**
     * substring-after() implementation.
     * @param context evaluation context
     * @return String
     */
    protected Object functionSubstringAfter(final EvalContext context) {
        assertArgCount(2);
        final String s1 = InfoSetUtil.stringValue(getArg1().computeValue(context));
        final String s2 = InfoSetUtil.stringValue(getArg2().computeValue(context));
        final int index = s1.indexOf(s2);
        if (index == -1) {
            return "";
        }
        return s1.substring(index + s2.length());
    }

    /**
     * substring() implementation.
     * @param context evaluation context
     * @return String
     */
    protected Object functionSubstring(final EvalContext context) {
        final int minArgs = 2;
        final int maxArgs = 3;
        assertArgRange(minArgs, maxArgs);
        final int ac = getArgumentCount();

        final String s1 = InfoSetUtil.stringValue(getArg1().computeValue(context));
        double from = InfoSetUtil.doubleValue(getArg2().computeValue(context));
        if (Double.isNaN(from)) {
            return "";
        }

        from = Math.round(from);
        if (from > s1.length() + 1) {
            return "";
        }
        if (ac == 2) {
            if (from < 1) {
                from = 1;
            }
            return s1.substring((int) from - 1);
        }
        double length =
            InfoSetUtil.doubleValue(getArg3().computeValue(context));
        length = Math.round(length);
        if (length < 0) {
            return "";
        }

        final double to = from + length;
        if (to < 1) {
            return "";
        }

        if (to > s1.length() + 1) {
            if (from < 1) {
                from = 1;
            }
            return s1.substring((int) from - 1);
        }

        if (from < 1) {
            from = 1;
        }
        return s1.substring((int) from - 1, (int) (to - 1));
    }

    /**
     * string-length() implementation.
     * @param context evaluation context
     * @return Number
     */
    protected Object functionStringLength(final EvalContext context) {
        String s;
        if (getArgumentCount() == 0) {
            s = InfoSetUtil.stringValue(context.getCurrentNodePointer());
        }
        else {
            assertArgCount(1);
            s = InfoSetUtil.stringValue(getArg1().computeValue(context));
        }
        return Double.valueOf(s.length());
    }

    /**
     * normalize-space() implementation.
     * @param context evaluation context
     * @return String
     */
    protected Object functionNormalizeSpace(final EvalContext context) {
        assertArgCount(1);
        final String s = InfoSetUtil.stringValue(getArg1().computeValue(context));
        final char[] chars = s.toCharArray();
        int out = 0;
        int phase = 0;
        for (int in = 0; in < chars.length; in++) {
            switch (chars[in]) {
                case ' ':
                case '\t':
                case '\r':
                case '\n':
                    if (phase == 1) { // non-space
                        phase = 2;
                        chars[out++] = ' ';
                    }
                    break;
                default:
                    chars[out++] = chars[in];
                    phase = 1;
            }
        }
        if (phase == 2) { // trailing-space
            out--;
        }
        return new String(chars, 0, out);
    }

    /**
     * translate() implementation.
     * @param context evaluation context
     * @return String
     */
    protected Object functionTranslate(final EvalContext context) {
        final int argCount = 3;
        assertArgCount(argCount);
        final String s1 = InfoSetUtil.stringValue(getArg1().computeValue(context));
        final String s2 = InfoSetUtil.stringValue(getArg2().computeValue(context));
        final String s3 = InfoSetUtil.stringValue(getArg3().computeValue(context));
        final char[] chars = s1.toCharArray();
        int out = 0;
        for (int in = 0; in < chars.length; in++) {
            final char c = chars[in];
            final int inx = s2.indexOf(c);
            if (inx != -1) {
                if (inx < s3.length()) {
                    chars[out++] = s3.charAt(inx);
                }
            }
            else {
                chars[out++] = c;
            }
        }
        return new String(chars, 0, out);
    }

    /**
     * boolean() implementation.
     * @param context evaluation context
     * @return Boolean
     */
    protected Object functionBoolean(final EvalContext context) {
        assertArgCount(1);
        return InfoSetUtil.booleanValue(getArg1().computeValue(context))
            ? Boolean.TRUE
            : Boolean.FALSE;
    }

    /**
     * not() implementation.
     * @param context evaluation context
     * @return Boolean
     */
    protected Object functionNot(final EvalContext context) {
        assertArgCount(1);
        return InfoSetUtil.booleanValue(getArg1().computeValue(context))
            ? Boolean.FALSE
            : Boolean.TRUE;
    }

    /**
     * true() implementation.
     * @param context evaluation context
     * @return Boolean.TRUE
     */
    protected Object functionTrue(final EvalContext context) {
        assertArgCount(0);
        return Boolean.TRUE;
    }

    /**
     * false() implementation.
     * @param context evaluation context
     * @return Boolean.FALSE
     */
    protected Object functionFalse(final EvalContext context) {
        assertArgCount(0);
        return Boolean.FALSE;
    }

    /**
     * null() implementation.
     * @param context evaluation context
     * @return null
     */
    protected Object functionNull(final EvalContext context) {
        assertArgCount(0);
        return null;
    }

    /**
     * number() implementation.
     * @param context evaluation context
     * @return Number
     */
    protected Object functionNumber(final EvalContext context) {
        if (getArgumentCount() == 0) {
            return InfoSetUtil.number(context.getCurrentNodePointer());
        }
        assertArgCount(1);
        return InfoSetUtil.number(getArg1().computeValue(context));
    }

    /**
     * sum() implementation.
     * @param context evaluation context
     * @return Number
     */
    protected Object functionSum(final EvalContext context) {
        assertArgCount(1);
        final Object v = getArg1().compute(context);
        if (v == null) {
            return ZERO;
        }
        if (v instanceof EvalContext) {
            double sum = 0.0;
            final EvalContext ctx = (EvalContext) v;
            while (ctx.hasNext()) {
                final NodePointer ptr = (NodePointer) ctx.next();
                sum += InfoSetUtil.doubleValue(ptr);
            }
            return Double.valueOf(sum);
        }
        throw new JXPathException(
            "Invalid argument type for 'sum': " + v.getClass().getName());
    }

    /**
     * floor() implementation.
     * @param context evaluation context
     * @return Number
     */
    protected Object functionFloor(final EvalContext context) {
        assertArgCount(1);
        final double v = InfoSetUtil.doubleValue(getArg1().computeValue(context));
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            return Double.valueOf(v);
        }
        return Double.valueOf(Math.floor(v));
    }

    /**
     * ceiling() implementation.
     * @param context evaluation context
     * @return Number
     */
    protected Object functionCeiling(final EvalContext context) {
        assertArgCount(1);
        final double v = InfoSetUtil.doubleValue(getArg1().computeValue(context));
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            return Double.valueOf(v);
        }
        return Double.valueOf(Math.ceil(v));
    }

    /**
     * round() implementation.
     * @param context evaluation context
     * @return Number
     */
    protected Object functionRound(final EvalContext context) {
        assertArgCount(1);
        final double v = InfoSetUtil.doubleValue(getArg1().computeValue(context));
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            return Double.valueOf(v);
        }
        return Double.valueOf(Math.round(v));
    }

    /**
     * format-number() implementation.
     * @param context evaluation context
     * @return String
     */
    private Object functionFormatNumber(final EvalContext context) {
        final int minArgs = 2;
        final int maxArgs = 3;
        assertArgRange(minArgs, maxArgs);

        final double number =
            InfoSetUtil.doubleValue(getArg1().computeValue(context));
        final String pattern =
            InfoSetUtil.stringValue(getArg2().computeValue(context));

        DecimalFormatSymbols symbols;
        if (getArgumentCount() == maxArgs) {
            final String symbolsName =
                InfoSetUtil.stringValue(getArg3().computeValue(context));
            symbols =
                context.getJXPathContext().getDecimalFormatSymbols(symbolsName);
        }
        else {
            final NodePointer pointer = context.getCurrentNodePointer();
            Locale locale;
            if (pointer != null) {
                locale = pointer.getLocale();
            }
            else {
                locale = context.getJXPathContext().getLocale();
            }
            symbols = new DecimalFormatSymbols(locale);
        }

        final DecimalFormat format = (DecimalFormat) NumberFormat.getInstance();
        format.setDecimalFormatSymbols(symbols);
        format.applyLocalizedPattern(pattern);
        return format.format(number);
    }

    /**
     * Assert {@code count} args.
     * @param count int
     */
    private void assertArgCount(final int count) {
        assertArgRange(count, count);
    }

    /**
     * Assert at least {@code min}/at most {@code max} args.
     * @param min int
     * @param max int
     */
    private void assertArgRange(final int min, final int max) {
        final int ct = getArgumentCount();
        if (ct < min || ct > max) {
            throw new JXPathInvalidSyntaxException(
                    "Incorrect number of arguments: " + this);
        }
    }
}
