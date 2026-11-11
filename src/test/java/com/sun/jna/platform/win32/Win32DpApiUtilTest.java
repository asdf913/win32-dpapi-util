package com.sun.jna.platform.win32;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

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

import com.google.common.reflect.Reflection;

import io.github.toolfactory.narcissus.Narcissus;

class Win32DpApiUtilTest {

	private static Method METHOD_GET_NAME_MEMBER, METHOD_GET_NAME_FILE, METHOD_GET_CLASS, METHOD_DECODE,
			METHOD_ENCODE_TO_STRING, METHOD_GET_BYTES, METHOD_TO_MAP, METHOD_COLLECT, METHOD_GET_ABSOLUTE_PATH,
			METHOD_ENDS_WITH, METHOD_CONTAINS_KEY, METHOD_GET_PARENT_FILE, METHOD_CRYPT_PROTECT_DATA,
			METHOD_TEST_AND_ACCEPT = null;

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
		(METHOD_DECODE = clz.getDeclaredMethod("decode", Decoder.class, String.class)).setAccessible(true);
		//
		(METHOD_ENCODE_TO_STRING = clz.getDeclaredMethod("encodeToString", Encoder.class, byte[].class))
				.setAccessible(true);
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
		(METHOD_CRYPT_PROTECT_DATA = clz.getDeclaredMethod("cryptProtectData", Map.class)).setAccessible(true);
		//
		(METHOD_TEST_AND_ACCEPT = clz.getDeclaredMethod("testAndAccept", BiPredicate.class, Object.class, Object.class,
				FailableBiConsumer.class)).setAccessible(true);
		//
	}

	private static class IH implements InvocationHandler {

		private Boolean test, containsKey = null;

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
			if (proxy instanceof Member && Objects.equals(name, "getName")) {
				//
				return null;
				//
			} else if (proxy instanceof Map) {
				//
				if (contains(Arrays.asList("get", "put"), name)) {
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
			} else if (proxy instanceof FailableFunction && Objects.equals(name, "apply")) {
				//
				return null;
				//
			} else if (proxy instanceof Stream) {
				//
				if (Objects.equals(name, "collect")) {
					//
					return null;
					//
				} else if (Objects.equals(name, "filter")) {
					//
					return proxy;
					//
				} // if
					//
			} else if (proxy instanceof Collection && Objects.equals(name, "stream")) {
				//
				return null;
				//
			} // if
				//
			throw new Throwable(name);
			//
		}

	}

	private IH ih = null;

	@BeforeMethod
	void beforeMethod() {
		//
		ih = new IH();
		//
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
		String toString = null;
		//
		Object[] os = null;
		//
		for (int i = 0; ms != null && i < ms.length; i++) {
			//
			if ((m = ArrayUtils.get(ms, i)) == null || m.isSynthetic()) {
				//
				continue;
				//
			} // if
				//
			os = toArray(Collections.nCopies(m.getParameterCount(), null));
			//
			toString = Objects.toString(m);
			//
			if (Boolean.logicalAnd(Objects.equals(getName(m), "cryptProtectData"),
					Arrays.equals(m.getParameterTypes(), new Class<?>[] { Map.class }))) {
				//
				final Method m1 = m;
				//
				final Object[] os1 = os;
				//
				Assert.assertThrows(Throwable.class, () -> Narcissus.invokeStaticMethod(m1, os1));
				//
			} else {
				//
				result = Narcissus.invokeStaticMethod(m, os);
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
				} else if (parameterType != null && parameterType.isInterface()) {
					//
					if ((ih = ObjectUtils.getIfNull(ih, IH::new)) != null) {
						//
						final List<Field> fs = FieldUtils.getAllFieldsList(getClass(ih));
						//
						Field f = null;
						//
						for (int k = 0; k < IterableUtils.size(fs); k++) {
							//
							if ((f = IterableUtils.get(fs, k)) == null) {
								//
								continue;
								//
							} // if
								//
							if (Objects.equals(f.getType(), Boolean.class)) {
								//
								Narcissus.setField(ih, f, Boolean.TRUE);
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
			result = Narcissus.invokeStaticMethod(m, os);
			//
			if (contains(Arrays.asList(Integer.TYPE, Boolean.TYPE), getReturnType(m))
					|| Boolean.logicalAnd(Objects.equals(name = getName(m), "getName"),
							Arrays.equals(parameterTypes, new Class<?>[] { Class.class }))
					|| Boolean.logicalAnd(Objects.equals(name, "getClass"),
							Arrays.equals(parameterTypes, new Class<?>[] { Object.class }))
					|| Boolean.logicalAnd(Objects.equals(name, "filter"),
							Arrays.equals(parameterTypes, new Class<?>[] { Stream.class, Predicate.class }))
					|| Boolean.logicalAnd(Objects.equals(name, "encodeToString"),
							Arrays.equals(parameterTypes, new Class<?>[] { Encoder.class, byte[].class }))) {
				//
				Assert.assertNotNull(result, toString);
				//
			} else {
				//
				Assert.assertNull(result, toString);
				//
			} // if
				//
		} // for
			//
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
	void testDecode() throws IllegalAccessException, InvocationTargetException {
		//
		final Decoder decoder = Base64.getDecoder();
		//
		Assert.assertNull(invoke(METHOD_DECODE, null, decoder, null));
		//
		Assert.assertEquals(invoke(METHOD_DECODE, null, decoder, ""), new byte[] {});
		//
	}

	@Test
	void testEncodeToString() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertNull(invoke(METHOD_ENCODE_TO_STRING, null, Base64.getEncoder(), null));
		//
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
	void testCryptProtectData() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertNull(invoke(METHOD_CRYPT_PROTECT_DATA, null, Collections.singletonMap("file", null)));
		//
		Assert.assertNull(invoke(METHOD_CRYPT_PROTECT_DATA, null, Collections.singletonMap("file", "")));
		//
		Assert.assertNull(invoke(METHOD_CRYPT_PROTECT_DATA, null, Collections.singletonMap("file", "pom.xml")));
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

}