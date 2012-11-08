/*
 * Copyright (c) XIAOWEI CHEN, 2009.
 * All Rights Reserved. Reproduction in whole or in part is prohibited
 * without the written consent of the copyright owner.
 * 
 * XIAOWEI CHEN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. XIAOWEI CHEN SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 * 
 * All rights reserved.
 */
package com.candybon.memory;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Stack;

/**
 * @author XiaoweiChen
 */
/**
 * A Simple observer that allows runtime examination of java object sizes. <br>
 * {@link Instrumentation} need to be initialized from the JVM. Provide the <code>-javaagent</code> to the JVM when startup. <br>
 * More details on : <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/lang/instrument/package-summary.html">java.lang.instrument</a> <br>
 * Command Line : <code>-javaagent:jarpath</code> <br>
 * jarpath is the Observer JAR file path. <br>
 */
public class MemoryObserver {

    private static Instrumentation instrumentation = null;
    
    // Debug mode
    private static boolean debug = false;

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        MemoryObserver.instrumentation = instrumentation;
    }

    public static void setPrintouts(boolean on) {
        debug = on;
    }

    /**
     * Get the shallow size of the provided object. <br>
     * A shared <i>flyweight</i> object it is considered to have a size of 0 <br>
     * Objects of the following type are eligible to be flyweight:
     * <ul>
     * <li>{@link Enum}</li>
     * <li>{@link Boolean#TRUE} and {@link Boolean#FALSE}</li>
     * <li>{@link Integer}</li>
     * <li>{@link Short}</li>
     * <li>{@link Byte}</li>
     * <li>{@link Long}</li>
     * <li>{@link Character}</li>
     * </ul>
     * The method will use {@link Instrumentation#getObjectSize(Object)} to estimate the size.
     * 
     * @param object
     *            The object to get the size of
     * @return The size (bytes) of the provided object
     * @throws IllegalStateException
     *             If the instrumentation environment is not initialized
     */
    public static long shallowSizeOf(Object object) {
        if (instrumentation == null) {
            throw new IllegalStateException("Instrumentation environment not initialised.");
        }

        long size = 0;
        if (!isSharedFlyweight(object)) {
            size = instrumentation.getObjectSize(object);
        }
        if (debug) {
            System.out.println("size [" + object.getClass().getSimpleName() + "][" + size + "]");
        }

        return size;
    }

    /**
     * Returns the total size of the object. <br>
     * The method will iterate through all fields in both the provided class and any superclasses. <br>
     * An internal map of all visited objects is kept in order not to count the same object twice.
     * 
     * @param object
     *            The object to get the size of
     * @return The total size (bytes) of the provided object
     * @throws IllegalStateException
     *             If the instrumentation environment is not initialized
     */
    public static long deepSizeOf(Object object) {
        if (instrumentation == null) {
            throw new IllegalStateException("Instrumentation environment not initialised.");
        }

        Map<Object, Object> visited = new IdentityHashMap<Object, Object>();

        /*
         * A stack is used instead of running a recursive algorithm where the same method is called for each found field/object in the examined object.
         * Recursiveness may cause issues with objects that contains a lot of other objects as it could result in a stack overflow. Instead we keep
         * pushing/popping objects to the stack as they are encountered/examined. Each found field/object is pushed on the stack.
         */
        Stack<Object> stack = new Stack<Object>();
        stack.push(object);

        long result = 0;
        Object currentObject;
        while (!stack.isEmpty()) {
            currentObject = stack.pop();
            if (currentObject != null) {
                getObjects(currentObject, stack, visited);
                result += shallowSizeOf(currentObject);
            }
        }
        return result;
    }

    /**
     * Verify if the provided object is a so called shared flyweight.
     * 
     * @param object
     * @return true if the object is a flyweight
     */
    private static boolean isSharedFlyweight(Object object) {
        boolean result = false;
        if (object instanceof Comparable<?>) {
            if (object instanceof Enum<?>) {
                result = true;
            } else if (object instanceof String) {
                result = (object == ((String) object).intern());
            } else if (object instanceof Boolean) {
                result = (object == Boolean.TRUE || object == Boolean.FALSE);
            } else if (object instanceof Integer) {
                result = (object == Integer.valueOf((Integer) object));
            } else if (object instanceof Short) {
                result = (object == Short.valueOf((Short) object));
            } else if (object instanceof Byte) {
                result = (object == Byte.valueOf((Byte) object));
            } else if (object instanceof Long) {
                result = (object == Long.valueOf((Long) object));
            } else if (object instanceof Character) {
                result = (object == Character.valueOf((Character) object));
            }
        }

        if (debug) {
            System.out.println("isflyweight [" + object.getClass().getSimpleName() + "][" + result + "]");
        }

        return result;
    }

    /**
     * The method will push any and all fields that the object may have on to the provided stack. <br>
     * In addition the method recursively walk through any super classes and store their fields.
     * 
     * @param object
     *            The object to get the size of
     * @param stack
     *            Contains all found fields
     * @param visited
     *            Contains all fields that have been processed
     */
    private static void getObjects(Object object, Stack<Object> stack, Map<Object, Object> visited) {
        // should the object be ignored?
        if (object == null || visited.containsKey(object) || isSharedFlyweight(object)) {
            return;
        }

        @SuppressWarnings("unchecked")
        Class clazz = object.getClass();

        /*
         * array fields are handled somewhat differently from ordinary fields
         */
        if (clazz.isArray()) {
            if (!clazz.getComponentType().isPrimitive()) {
                int length = Array.getLength(object);
                for (int i = 0; i < length; i++) {
                    stack.add(Array.get(object, i));
                }
            }
        } /*
         * add all non-primitive fields to the stack
         */ else {
            // iterate through a super classes
            while (clazz != null) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if (!Modifier.isStatic(field.getModifiers()) && !field.getType().isPrimitive()) {
                        field.setAccessible(true);
                        try {
                            if (debug) {
                                System.out.println("adding field [" + clazz.getSimpleName() + "].[" + field.getName() + "]");
                            }
                            stack.add(field.get(object));
                        } catch (IllegalAccessException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
                clazz = clazz.getSuperclass();
            }
        }

        // remember that the object has been processed.
        visited.put(object, null);
    }
}
