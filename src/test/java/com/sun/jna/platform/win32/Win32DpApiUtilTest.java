package com.sun.jna.platform.win32;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.geom.Dimension2D;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64.Encoder;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Stream;

import javax.swing.AbstractButton;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.apache.commons.codec.BinaryEncoder;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.function.FailableBiConsumer;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.google.common.reflect.Reflection;

import io.github.toolfactory.narcissus.Narcissus;

class Win32DpApiUtilTest {

	private static Method METHOD_GET_NAME_MEMBER, METHOD_GET_NAME_FILE, METHOD_GET_CLASS, METHOD_GET_BYTES,
			METHOD_TO_MAP, METHOD_COLLECT, METHOD_GET_ABSOLUTE_PATH, METHOD_ENDS_WITH, METHOD_CONTAINS_KEY,
			METHOD_GET_PARENT_FILE, METHOD_CRYPT_PROTECT_DATA, METHOD_CRYPT_UNPROTECT_DATA, METHOD_TEST_AND_ACCEPT,
			METHOD_PUT, METHOD_LOAD, METHOD_GET_CONTEXT_CLASS_LOADER, METHOD_TEST_AND_APPLY,
			METHOD_GET_TOP_LEVEL_CLASSES, METHOD_MATCHER, METHOD_TO_LOWER_CASE, METHOD_MATCHES, METHOD_FIND,
			METHOD_IS_ABSTRACT, METHOD_IS_ASSIGNABLE_FROM, METHOD_TEST_AND_GET, METHOD_TEST_AND_RUN, METHOD_CAST,
			METHOD_IS_STATIC, METHOD_GET_TEXT, METHOD_SET_TEXT, METHOD_AND, METHOD_SHOW_OPEN_DIALOG,
			METHOD_GET_NAME_CLASS, METHOD_GET_TYPE = null;

	@BeforeClass
	static void beforeClass() throws NoSuchMethodException {
		//
		final Class<?> clz = Win32DpApiUtil.class;
		//
		(METHOD_GET_NAME_MEMBER = clz.getDeclaredMethod("getName", Member.class)).setAccessible(true);
		//
		(METHOD_GET_NAME_FILE = clz.getDeclaredMethod("getName", File.class)).setAccessible(true);
		//
		(METHOD_GET_CLASS = clz.getDeclaredMethod("getClass", Object.class)).setAccessible(true);
		//
		(METHOD_GET_BYTES = clz.getDeclaredMethod("getBytes", String.class)).setAccessible(true);
		//
		(METHOD_TO_MAP = clz.getDeclaredMethod("toMap", String[].class)).setAccessible(true);
		//
		(METHOD_COLLECT = clz.getDeclaredMethod("collect", Stream.class, Collector.class)).setAccessible(true);
		//
		(METHOD_GET_ABSOLUTE_PATH = clz.getDeclaredMethod("getAbsolutePath", File.class)).setAccessible(true);
		//
		(METHOD_ENDS_WITH = clz.getDeclaredMethod("endsWith", String.class, String.class)).setAccessible(true);
		//
		(METHOD_CONTAINS_KEY = clz.getDeclaredMethod("containsKey", Map.class, Object.class)).setAccessible(true);
		//
		(METHOD_GET_PARENT_FILE = clz.getDeclaredMethod("getParentFile", File.class)).setAccessible(true);
		//
		(METHOD_CRYPT_PROTECT_DATA = clz.getDeclaredMethod("cryptProtectData", Map.class, Map.class))
				.setAccessible(true);
		//
		(METHOD_CRYPT_UNPROTECT_DATA = clz.getDeclaredMethod("cryptUnprotectData", Map.class, Map.class))
				.setAccessible(true);
		//
		(METHOD_TEST_AND_ACCEPT = clz.getDeclaredMethod("testAndAccept", BiPredicate.class, Object.class, Object.class,
				FailableBiConsumer.class)).setAccessible(true);
		//
		(METHOD_PUT = clz.getDeclaredMethod("put", Map.class, Object.class, Object.class)).setAccessible(true);
		//
		(METHOD_LOAD = clz.getDeclaredMethod("load", ClassInfo.class)).setAccessible(true);
		//
		(METHOD_GET_CONTEXT_CLASS_LOADER = clz.getDeclaredMethod("getContextClassLoader", Thread.class))
				.setAccessible(true);
		//
		(METHOD_TEST_AND_APPLY = clz.getDeclaredMethod("testAndApply", Predicate.class, Object.class,
				FailableFunction.class, FailableFunction.class)).setAccessible(true);
		//
		(METHOD_GET_TOP_LEVEL_CLASSES = clz.getDeclaredMethod("getTopLevelClasses", ClassPath.class, String.class))
				.setAccessible(true);
		//
		(METHOD_MATCHER = clz.getDeclaredMethod("matcher", Pattern.class, String.class)).setAccessible(true);
		//
		(METHOD_TO_LOWER_CASE = clz.getDeclaredMethod("toLowerCase", String.class)).setAccessible(true);
		//
		(METHOD_MATCHES = clz.getDeclaredMethod("matches", String.class, String.class)).setAccessible(true);
		//
		(METHOD_FIND = clz.getDeclaredMethod("find", Matcher.class)).setAccessible(true);
		//
		(METHOD_IS_ABSTRACT = clz.getDeclaredMethod("isAbstract", Class.class)).setAccessible(true);
		//
		(METHOD_IS_ASSIGNABLE_FROM = clz.getDeclaredMethod("isAssignableFrom", Class.class, Class.class))
				.setAccessible(true);
		//
		(METHOD_TEST_AND_GET = clz.getDeclaredMethod("testAndGet", Boolean.TYPE, Supplier.class)).setAccessible(true);
		//
		(METHOD_TEST_AND_RUN = clz.getDeclaredMethod("testAndRun", Boolean.TYPE, Runnable.class)).setAccessible(true);
		//
		(METHOD_CAST = clz.getDeclaredMethod("cast", Class.class, Object.class)).setAccessible(true);
		//
		(METHOD_IS_STATIC = clz.getDeclaredMethod("isStatic", Member.class)).setAccessible(true);
		//
		(METHOD_GET_TEXT = clz.getDeclaredMethod("getText", JTextComponent.class)).setAccessible(true);
		//
		(METHOD_SET_TEXT = clz.getDeclaredMethod("setText", JTextComponent.class, String.class)).setAccessible(true);
		//
		(METHOD_AND = clz.getDeclaredMethod("and", Boolean.TYPE, Boolean.TYPE, BooleanSupplier.class))
				.setAccessible(true);
		//
		(METHOD_SHOW_OPEN_DIALOG = clz.getDeclaredMethod("showOpenDialog", JFileChooser.class, Integer.TYPE))
				.setAccessible(true);
		//
		(METHOD_GET_NAME_CLASS = clz.getDeclaredMethod("getName", Class.class)).setAccessible(true);
		//
		(METHOD_GET_TYPE = clz.getDeclaredMethod("getType", Field.class)).setAccessible(true);
		//
	}

	private static class IH implements InvocationHandler {

		private Boolean test, containsKey, getAsBoolean, value, isAnnotationPresent = null;

		private Integer modifiers = null;

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			//
			if (Objects.equals(getReturnType(method), Void.TYPE)) {
				//
				return null;
				//
			} // if
				//
			final String name = getName(method);
			//
			if (proxy instanceof Member) {
				//
				if (Objects.equals(name, "getName")) {
					//
					return null;
					//
				} else if (Objects.equals(name, "getModifiers")) {
					//
					return modifiers;
					//
				} // if
					//
			} else if (proxy instanceof Map) {
				//
				if (contains(Arrays.asList("get", "put", "keySet"), name)) {
					//
					return null;
					//
				} else if (Objects.equals(name, "containsKey")) {
					//
					return containsKey;
					//
				} // if
					//
			} else if (Boolean.logicalAnd(Boolean.logicalOr(proxy instanceof Predicate, proxy instanceof BiPredicate),
					Objects.equals(name, "test"))) {
				//
				return test;
				//
			} else if (proxy instanceof Entry && contains(Arrays.asList("getValue", "getKey"), name)) {
				//
				return null;
				//
			} else if (Boolean.logicalAnd(
					Boolean.logicalOr(proxy instanceof FailableFunction, proxy instanceof BiFunction),
					Objects.equals(name, "apply"))) {
				//
				return null;
				//
			} else if (proxy instanceof Stream) {
				//
				if (contains(Arrays.asList("collect", "map"), name)) {
					//
					return null;
					//
				} else if (Objects.equals(name, "filter")) {
					//
					return proxy;
					//
				} // if
					//
			} else if (proxy instanceof Collection && contains(Arrays.asList("stream", "toArray"), name)) {
				//
				return null;
				//
			} else if (proxy instanceof MatchResult && Objects.equals(name, "group")) {
				//
				return null;
				//
			} else if (proxy instanceof RuntimeMXBean && Objects.equals(name, "getName")) {
				//
				return null;
				//
			} else if (proxy instanceof ComboBoxModel && Objects.equals(name, "getSelectedItem")) {
				//
				return null;
				//
			} else if (proxy instanceof BooleanSupplier && Objects.equals(name, "getAsBoolean")) {
				//
				return getAsBoolean;
				//
			} else if (proxy instanceof BinaryEncoder && Objects.equals(name, "encode")) {
				//
				return null;
				//
			} else if (Objects.equals(getName(getDeclaringClass(method)),
					"com.sun.jna.platform.win32.Win32DpApiUtil$Editable") && Objects.equals(name, "value")) {
				//
				return value;
				//
			} else if (proxy instanceof AnnotatedElement) {
				//
				if (Objects.equals(name, "isAnnotationPresent")) {
					//
					return isAnnotationPresent;
					//
				} else if (Objects.equals(name, "getAnnotation")) {
					//
					return null;
					//
				} // if
					//
			} // if
				//
			throw new Throwable(name);
			//
		}

		private static Class<?> getDeclaringClass(final Member instance) {
			return instance != null ? instance.getDeclaringClass() : null;
		}

	}

	private Win32DpApiUtil instance = null;

	private IH ih = null;

	@BeforeMethod
	void beforeMethod() throws Throwable {
		//
		instance = cast(Win32DpApiUtil.class, Narcissus.allocateInstance(Win32DpApiUtil.class));
		//
		ih = new IH();
		//
	}

	private static String getName(final Class<?> instance) throws Throwable {
		try {
			final Object obj = invoke(METHOD_GET_NAME_CLASS, null, instance);
			if (obj == null) {
				return null;
			} else if (obj instanceof String) {
				return (String) obj;
			}
			throw new Throwable(Objects.toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	private static <T> T cast(final Class<T> clz, final Object instance) throws Throwable {
		try {
			return (T) invoke(METHOD_CAST, null, clz, instance);
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	private static String getName(final Member instance) throws Throwable {
		try {
			final Object obj = invoke(METHOD_GET_NAME_MEMBER, null, instance);
			if (obj == null) {
				return null;
			} else if (obj instanceof String) {
				return (String) obj;
			}
			throw new Throwable(Objects.toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	private static Object invoke(final Method method, final Object instance, final Object... args)
			throws IllegalAccessException, InvocationTargetException {
		return method != null && method.getDeclaringClass() != null ? method.invoke(instance, args) : null;
	}

	@Test
	void testNull() throws Throwable {
		//
		final Method[] ms = Win32DpApiUtil.class.getDeclaredMethods();
		//
		Method m = null;
		//
		Object result = null;
		//
		String toString, name = null;
		//
		Object[] os = null;
		//
		Class<?>[] parameterTypes = null;
		//
		Class<?> parameterType = null;
		//
		Collection<Object> collection = null;
		//
		for (int i = 0; ms != null && i < ms.length; i++) {
			//
			if ((m = ArrayUtils.get(ms, i)) == null || m.isSynthetic()
					|| (parameterTypes = m.getParameterTypes()) == null) {
				//
				continue;
				//
			} // if
				//
			clear(collection = ObjectUtils.getIfNull(collection, ArrayList::new));
			//
			for (int j = 0; j < parameterTypes.length; j++) {
				//
				if (Objects.equals(parameterType = ArrayUtils.get(parameterTypes, j), Character.TYPE)) {
					//
					add(collection, Character.valueOf(' '));
					//
				} else if (Objects.equals(parameterType, Boolean.TYPE)) {
					//
					add(collection, Boolean.FALSE);
					//
				} else if (Objects.equals(parameterType, Integer.TYPE)) {
					//
					add(collection, Integer.valueOf(0));
					//
				} else {
					//
					add(collection, null);
					//
				} // if
					//
			} // for
				//
			os = toArray(collection);
			//
			toString = Objects.toString(m);
			//
			if (Boolean.logicalAnd(contains(Arrays.asList("cryptProtectData", "cryptUnprotectData"), name = getName(m)),
					Arrays.equals(parameterTypes = m.getParameterTypes(), new Class<?>[] { Map.class, Map.class }))
					|| Boolean.logicalAnd(Objects.equals(name, "toByteArray"),
							Arrays.equals(parameterTypes, new Class<?>[] { String.class, String.class }))
					|| Boolean.logicalAnd(Objects.equals(name, "actionPerformed"),
							Arrays.equals(parameterTypes, new Class<?>[] { ActionEvent.class }))) {
				//
				final Method m1 = m;
				//
				final Object[] os1 = os;
				//
				Assert.assertThrows(Throwable.class, () -> {
					//
					if (isStatic(m1)) {
						//
						Narcissus.invokeStaticMethod(m1, os1);
						//
					} else {
						//
						Narcissus.invokeMethod(instance, m1, os1);
						//
					} // if
						//
				});
				//
			} else {
				//
				result = isStatic(m) ? Narcissus.invokeStaticMethod(m, os) : Narcissus.invokeMethod(instance, m, os);
				//
				if (contains(Arrays.asList(Integer.TYPE, Boolean.TYPE), getReturnType(m))) {
					//
					Assert.assertNotNull(result, toString);
					//
				} else {
					//
					Assert.assertNull(result, toString);
					//
				} // if
					//
			} // if
				//
		} // for
			//
	}

	private static boolean isStatic(final Member instance) throws Throwable {
		try {
			final Object obj = invoke(METHOD_IS_STATIC, null, instance);
			if (obj instanceof Boolean) {
				return ((Boolean) obj).booleanValue();
			}
			throw new Throwable(Objects.toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	private static Class<?> getReturnType(final Method instance) {
		return instance != null ? instance.getReturnType() : null;
	}

	private static boolean contains(final Collection<?> instance, final Object item) {
		return instance != null && instance.contains(item);
	}

	private static Object[] toArray(final Collection<?> instance) {
		return instance != null ? instance.toArray() : null;
	}

	@Test
	void testNotNull() throws Throwable {
		//
		final Method[] ms = Win32DpApiUtil.class.getDeclaredMethods();
		//
		Method m = null;
		//
		Class<?>[] parameterTypes = null;
		//
		Class<?> parameterType = null;
		//
		Object result = null;
		//
		String toString, name = null;
		//
		Object[] os = null;
		//
		Collection<Object> collection = null;
		//
		for (int i = 0; ms != null && i < ms.length; i++) {
			//
			if ((m = ArrayUtils.get(ms, i)) == null || m.isSynthetic()
					|| (parameterTypes = m.getParameterTypes()) == null) {
				//
				continue;
				//
			} // if
				//
			clear(collection = ObjectUtils.getIfNull(collection, ArrayList::new));
			//
			for (int j = 0; j < parameterTypes.length; j++) {
				//
				if (Objects.equals(parameterType = ArrayUtils.get(parameterTypes, j), Class.class)) {
					//
					add(collection, Class.class);
					//
				} else if (Objects.equals(parameterType, Character.TYPE)) {
					//
					add(collection, Character.valueOf(' '));
					//
				} else if (Objects.equals(parameterType, Boolean.TYPE)) {
					//
					add(collection, Boolean.FALSE);
					//
				} else if (Objects.equals(parameterType, Integer.TYPE)) {
					//
					add(collection, Integer.valueOf(0));
					//
				} else if (Objects.equals(parameterType, ClassLoader.class)) {
					//
					add(collection, Narcissus.allocateInstance(getClass(ClassLoader.getSystemClassLoader())));
					//
				} else if (Objects.equals(parameterType, Executable.class)) {
					//
					add(collection, Narcissus.allocateInstance(Method.class));
					//
				} else if (Objects.equals(parameterType, Component.class)) {
					//
					add(collection, Narcissus.allocateInstance(JLabel.class));
					//
				} else if (Objects.equals(parameterType, JTextComponent.class)) {
					//
					add(collection, Narcissus.allocateInstance(JTextField.class));
					//
				} else if (Objects.equals(parameterType, Dimension2D.class)) {
					//
					add(collection, Narcissus.allocateInstance(Dimension.class));
					//
				} else if (Objects.equals(parameterType, AbstractButton.class)) {
					//
					add(collection, Narcissus.allocateInstance(JButton.class));
					//
				} else if (parameterType != null && parameterType.isInterface()) {
					//
					if ((ih = ObjectUtils.getIfNull(ih, IH::new)) != null) {
						//
						final List<Field> fs = FieldUtils.getAllFieldsList(getClass(ih));
						//
						Field f = null;
						//
						Class<?> type = null;
						//
						for (int k = 0; k < IterableUtils.size(fs); k++) {
							//
							if ((f = IterableUtils.get(fs, k)) == null) {
								//
								continue;
								//
							} // if
								//
							if (Objects.equals(type = getType(f), Boolean.class)) {
								//
								Narcissus.setField(ih, f, Boolean.TRUE);
								//
							} else if (Objects.equals(type, Integer.class)) {
								//
								Narcissus.setField(ih, f, Integer.valueOf(0));
								//
							} // if
								//
						} // for
							//
					} // if
						//
					add(collection, Reflection.newProxy(parameterType, ih = ObjectUtils.getIfNull(ih, IH::new)));
					//
				} else if (parameterType != null && parameterType.isArray()) {
					//
					add(collection, Array.newInstance(parameterType.getComponentType(), 0));
					//
				} else {
					//
					add(collection, Narcissus.allocateInstance(parameterType));
					//
				} // if
					//
			} // for
				//
			os = toArray(collection);
			//
			toString = Objects.toString(m);
			//
			if (Boolean.logicalAnd(Objects.equals(name = getName(m), "actionPerformed"),
					Arrays.equals(parameterTypes, new Class<?>[] { ActionEvent.class }))) {
				//
				final Method m1 = m;
				//
				final Object[] os1 = os;
				//
				Assert.assertThrows(Throwable.class, () -> Narcissus.invokeMethod(instance, m1, os1));
				//
			} else {
				//
				result = Narcissus.invokeStaticMethod(m, os);
				//
				if (contains(Arrays.asList(Integer.TYPE, Boolean.TYPE), getReturnType(m))
						|| Boolean.logicalAnd(Objects.equals(name, "getName"),
								Arrays.equals(parameterTypes, new Class<?>[] { Class.class }))
						|| Boolean.logicalAnd(Objects.equals(name, "getClass"),
								Arrays.equals(parameterTypes, new Class<?>[] { Object.class }))
						|| Boolean.logicalAnd(Objects.equals(name, "filter"),
								Arrays.equals(parameterTypes, new Class<?>[] { Stream.class, Predicate.class }))
						|| Boolean.logicalAnd(Objects.equals(name, "encodeToString"),
								Arrays.equals(parameterTypes, new Class<?>[] { Encoder.class, byte[].class }))
						|| Boolean.logicalAnd(Objects.equals(name, "getSimpleName"),
								Arrays.equals(parameterTypes, new Class<?>[] { Class.class }))
						|| Boolean.logicalAnd(Objects.equals(name, "getWidth"),
								Arrays.equals(parameterTypes, new Class<?>[] { Dimension2D.class }))
						|| Boolean.logicalAnd(Objects.equals(name, "value"),
								Arrays.equals(parameterTypes, new Class<?>[] {
										Class.forName("com.sun.jna.platform.win32.Win32DpApiUtil$Editable") }))) {
					//
					Assert.assertNotNull(result, toString);
					//
				} else {
					//
					Assert.assertNull(result, toString);
					//
				} // if
					//
			} // if
				//
		} // for
			//
	}

	private static Class<?> getType(final Field instance) throws Throwable {
		try {
			final Object obj = invoke(METHOD_GET_TYPE, null, instance);
			if (obj == null) {
				return null;
			} else if (obj instanceof Class) {
				return (Class<?>) obj;
			}
			throw new Throwable(Objects.toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	private static Class<?> getClass(final Object instance) throws Throwable {
		try {
			final Object obj = invoke(METHOD_GET_CLASS, null, instance);
			if (obj == null) {
				return null;
			} else if (obj instanceof Class) {
				return (Class<?>) obj;
			}
			throw new Throwable(Objects.toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	private static <E> void add(final Collection<E> instance, final E item) {
		if (instance != null) {
			instance.add(item);
		}
	}

	private static void clear(final Collection<?> instance) {
		if (instance != null) {
			instance.clear();
		}
	}

	@Test
	void testGetBytes() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertEquals(invoke(METHOD_GET_BYTES, null, ""), new byte[] {});
		//
	}

	@Test
	void testToMap() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertNull(invoke(METHOD_TO_MAP, null, (Object) new String[] { null }));
		//
		Assert.assertNull(invoke(METHOD_TO_MAP, null, (Object) new String[] { "" }));
		//
		Assert.assertEquals(invoke(METHOD_TO_MAP, null, (Object) new String[] { "=" }),
				Collections.singletonMap("", ""));
		//
		Assert.assertEquals(invoke(METHOD_TO_MAP, null, (Object) new String[] { "= " }),
				Collections.singletonMap("", " "));
		//
		Assert.assertEquals(invoke(METHOD_TO_MAP, null, (Object) new String[] { " =" }),
				Collections.singletonMap(" ", ""));
		//
		Assert.assertEquals(invoke(METHOD_TO_MAP, null, (Object) new String[] { " = " }),
				Collections.singletonMap(" ", " "));
		//
		Assert.assertEquals(invoke(METHOD_TO_MAP, null, (Object) new String[] { "== " }),
				Collections.singletonMap("", "= "));
		//
	}

	@Test
	void testCollect() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertNull(invoke(METHOD_COLLECT, null, Stream.empty(), null));
		//
		Assert.assertNull(invoke(METHOD_COLLECT, null,
				Reflection.newProxy(Stream.class, ObjectUtils.getIfNull(ih, IH::new)), null));
		//
	}

	@Test
	void testGetAbsolutePath() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertNotNull(invoke(METHOD_GET_ABSOLUTE_PATH, null, new File(".")));
		//
	}

	@Test
	void testEndsWith() throws IllegalAccessException, InvocationTargetException {
		//
		final String empty = "";
		//
		Assert.assertEquals(invoke(METHOD_ENDS_WITH, null, empty, null), Boolean.FALSE);
		//
		Assert.assertEquals(invoke(METHOD_ENDS_WITH, null, empty, empty), Boolean.TRUE);
		//
		Assert.assertEquals(invoke(METHOD_ENDS_WITH, null, empty, Integer.toString(1)), Boolean.FALSE);
		//
	}

	@Test
	void testContainsKey() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertEquals(invoke(METHOD_CONTAINS_KEY, null, Collections.emptyMap(), null), Boolean.FALSE);
		//
	}

	@Test
	void testGetName() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertNotNull(invoke(METHOD_GET_NAME_FILE, null, new File(".")));
		//
	}

	@Test
	void testGetParentFile() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertNull(invoke(METHOD_GET_PARENT_FILE, null, new File(".")));
		//
	}

	@Test
	void testCryptProtectData() throws Throwable {
		//
		Assert.assertNull(invoke(METHOD_CRYPT_PROTECT_DATA, null, Collections.singletonMap("file", null), null));
		//
		Assert.assertNull(invoke(METHOD_CRYPT_PROTECT_DATA, null, Collections.singletonMap("file", ""), null));
		//
		Assert.assertNull(invoke(METHOD_CRYPT_PROTECT_DATA, null, Collections.singletonMap("file", "pom.xml"), null));
		//
		final Map<String, String> map = new LinkedHashMap<>(Collections.singletonMap("input", ""));
		//
		put(map, "encoding", "hex");
		//
		final Map<String, Class<?>> classMap = Collections.singletonMap("hex", Hex.class);
		//
		Assert.assertNull(invoke(METHOD_CRYPT_PROTECT_DATA, null, map, classMap));
		//
		put(map, "flags", "0");
		//
		Assert.assertNull(invoke(METHOD_CRYPT_PROTECT_DATA, null, map, classMap));
		//
	}

	private static <K, V> void put(final Map<K, V> instance, final K key, final V value) throws Throwable {
		try {
			invoke(METHOD_PUT, null, instance, key, value);
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test
	void testCryptUnprotectData() throws Throwable {
		//
		Assert.assertNull(invoke(METHOD_CRYPT_UNPROTECT_DATA, null, Collections.singletonMap("file", null), null));
		//
		Assert.assertNull(invoke(METHOD_CRYPT_UNPROTECT_DATA, null, Collections.singletonMap("file", ""), null));
		//
		final Map<String, String> map = new LinkedHashMap<>(Collections.singletonMap("input", ""));
		//
		put(map, "encoding", "hex");
		//
		Assert.assertNull(invoke(METHOD_CRYPT_UNPROTECT_DATA, null, map, Collections.singletonMap("hex", Hex.class)));
		//
	}

	@Test
	void testTestAndAccept() throws IllegalAccessException, InvocationTargetException {
		//
		if ((ih = ObjectUtils.getIfNull(ih, IH::new)) != null) {
			//
			ih.test = Boolean.TRUE;
			//
		} // if
			//
		Assert.assertNull(
				invoke(METHOD_TEST_AND_ACCEPT, null, Reflection.newProxy(BiPredicate.class, ih), null, null, null));
		//
	}

	@Test
	void testLoad() throws Throwable {
		//
		final ClassPath cp = ClassPath.from(getContextClassLoader(Thread.currentThread()));
		//
		Assert.assertNotNull(invoke(METHOD_LOAD, null, (Object) testAndApply(CollectionUtils::isNotEmpty,
				cp != null ? cp.getAllClasses() : null, x -> IterableUtils.get(x, 0), null)));
		//
	}

	private static <T, R, E extends Throwable> R testAndApply(final Predicate<T> predicate, final T value,
			final FailableFunction<T, R, E> functionTrue, final FailableFunction<T, R, E> functionFalse)
			throws Throwable {
		try {
			return (R) invoke(METHOD_TEST_AND_APPLY, null, predicate, value, functionTrue, functionFalse);
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	private static ClassLoader getContextClassLoader(final Thread instance) throws Throwable {
		try {
			final Object obj = invoke(METHOD_GET_CONTEXT_CLASS_LOADER, null, instance);
			if (obj == null) {
				return null;
			} else if (obj instanceof ClassLoader) {
				return (ClassLoader) obj;
			}
			throw new Throwable(Objects.toString(getClass(obj)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test
	void testGetTopLevelClasses() throws Throwable {
		//
		final ClassPath cp = ClassPath.from(getContextClassLoader(Thread.currentThread()));
		//
		Assert.assertNull(invoke(METHOD_GET_TOP_LEVEL_CLASSES, null, cp, null));
		//
		Assert.assertNotNull(invoke(METHOD_GET_TOP_LEVEL_CLASSES, null, cp, ""));
		//
		Assert.assertNull(invoke(METHOD_GET_TOP_LEVEL_CLASSES, null, cp, Narcissus.allocateInstance(String.class)));
		//
	}

	@Test
	void testToLowerCase() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertEquals(invoke(METHOD_TO_LOWER_CASE, null, "A"), "a");
		//
	}

	@Test
	void testMatches() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertEquals(invoke(METHOD_MATCHES, null, "A", "A"), Boolean.TRUE);
		//
		Assert.assertEquals(invoke(METHOD_MATCHES, null, "A", "B"), Boolean.FALSE);
		//
		Assert.assertEquals(invoke(METHOD_MATCHES, null, "A", Narcissus.allocateInstance(String.class)), Boolean.FALSE);
		//
	}

	@Test
	void testFind() throws IllegalAccessException, InvocationTargetException {
		//
		final Pattern pattern = Pattern.compile("\\d+");
		//
		Assert.assertEquals(invoke(METHOD_FIND, null, invoke(METHOD_MATCHER, null, pattern, "")), Boolean.FALSE);
		//
		Assert.assertEquals(invoke(METHOD_FIND, null, invoke(METHOD_MATCHER, null, pattern, "1")), Boolean.TRUE);
		//
	}

	@Test
	void testMain()
			throws InstantiationException, IllegalAccessException, IOException, DecoderException, EncoderException {
		//
		Win32DpApiUtil.main(new String[] { "gui=true" });
		//
	}

	@Test
	void testIsAbstract() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertEquals(invoke(METHOD_IS_ABSTRACT, null, (Object) null), Boolean.FALSE);
		//
		Assert.assertEquals(invoke(METHOD_IS_ABSTRACT, null, String.class), Boolean.FALSE);
		//
		Assert.assertEquals(invoke(METHOD_IS_ABSTRACT, null, AbstractMap.class), Boolean.TRUE);
		//
	}

	@Test
	void testIsAssignableFrom() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertEquals(invoke(METHOD_IS_ASSIGNABLE_FROM, null, Object.class, null), Boolean.FALSE);
		//
		Assert.assertEquals(invoke(METHOD_IS_ASSIGNABLE_FROM, null, Object.class, Object.class), Boolean.TRUE);
		//
		Assert.assertEquals(invoke(METHOD_IS_ASSIGNABLE_FROM, null, String.class, Object.class), Boolean.FALSE);
		//
	}

	@Test
	void testTestAndGet() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertNull(invoke(METHOD_TEST_AND_GET, null, Boolean.TRUE, null));
		//
	}

	@Test
	void testTestAndRun() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertNull(invoke(METHOD_TEST_AND_RUN, null, Boolean.TRUE, null));
		//
		Assert.assertNull(invoke(METHOD_TEST_AND_RUN, null, Boolean.TRUE,
				Reflection.newProxy(Runnable.class, ObjectUtils.getIfNull(ih, IH::new))));
		//
	}

	@Test
	void testGetText() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertEquals(invoke(METHOD_GET_TEXT, null, new JTextField()), "");
		//
	}

	@Test
	void testSetText() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertNull(invoke(METHOD_SET_TEXT, null, new JTextField(), null));
		//
	}

	@Test
	void testAnd() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertEquals(invoke(METHOD_AND, null, Boolean.TRUE, Boolean.TRUE, null), Boolean.FALSE);
		//
		if ((ih = ObjectUtils.getIfNull(ih, IH::new)) != null) {
			//
			ih.getAsBoolean = Boolean.FALSE;
			//
		} // if
			//
		final BooleanSupplier booleanSupplier = Reflection.newProxy(BooleanSupplier.class, ih);
		//
		Assert.assertEquals(invoke(METHOD_AND, null, Boolean.TRUE, Boolean.TRUE, booleanSupplier), Boolean.FALSE);
		//
		if (ih != null) {
			//
			ih.getAsBoolean = Boolean.TRUE;
			//
		} // if
			//
		Assert.assertEquals(invoke(METHOD_AND, null, Boolean.TRUE, Boolean.TRUE, booleanSupplier), Boolean.TRUE);
		//
	}

	@Test
	void testShowOpenDialog() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertEquals(invoke(METHOD_SHOW_OPEN_DIALOG, null, new JFileChooser(), JFileChooser.APPROVE_OPTION),
				Boolean.FALSE);
		//
	}

	@Test
	void testActionPerformed() throws IllegalAccessException {
		//
		if (instance == null) {
			//
			return;
			//
		} // if
			//
			// btnCryptUnprotect
			//
		final AbstractButton btnCryptUnprotect = new JButton();
		//
		FieldUtils.writeDeclaredField(instance, "btnCryptUnprotect", btnCryptUnprotect, true);
		//
		Assert.assertThrows(Throwable.class,
				() -> instance.actionPerformed(new ActionEvent(btnCryptUnprotect, 0, null)));
		//
		// btnCryptProtectFile
		//
		final AbstractButton btnCryptProtectFile = new JButton();
		//
		FieldUtils.writeDeclaredField(instance, "btnCryptProtectFile", btnCryptProtectFile, true);
		//
		instance.actionPerformed(new ActionEvent(btnCryptProtectFile, 0, null));
		//
		// btnCryptUnprotectFile
		//
		final AbstractButton btnCryptUnprotectFile = new JButton();
		//
		FieldUtils.writeDeclaredField(instance, "btnCryptUnprotectFile", btnCryptUnprotectFile, true);
		//
		instance.actionPerformed(new ActionEvent(btnCryptUnprotectFile, 0, null));
		//
	}

}