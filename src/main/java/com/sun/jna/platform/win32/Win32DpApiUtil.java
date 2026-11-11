package com.sun.jna.platform.win32;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Proxy;
import java.nio.file.FileSystems;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableBiConsumer;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.toolfactory.narcissus.Narcissus;

public class Win32DpApiUtil {

	private static final Logger LOG = LoggerFactory.getLogger(Win32DpApiUtil.class);

	private static final String VALUE = "value";

	private static final String BASE64 = "base64";

	private static final String INPUT = "input";

	public static void main(final String[] args) throws IOException, DecoderException {
		//
		if (!isWindows()) {
			//
			info(LOG, "Please run under Microsoft Windows");
			//
			return;
			//
		} // if
			//
		final Map<String, String> map = toMap(args);
		//
		final String action = get(map, "action");
		//
		if (Objects.equals(action, "cryptProtectData")) {
			//
			cryptProtectData(map);
			//
		} else if (Objects.equals(action, "cryptUnprotectData")) {
			//
			cryptUnprotectData(map);
			//
		} else {
			//
			throw new RuntimeException(action);
			//
		} // if
			//
	}

	private static boolean isWindows() {
		return Objects.equals(getName(getClass(FileSystems.getDefault())), "sun.nio.fs.WindowsFileSystem");
	}

	private static void cryptProtectData(final Map<String, String> map) throws IOException {
		//
		byte[] bs = null;
		//
		final String encoding = StringUtils.defaultIfEmpty(get(map, "encoding"), BASE64);
		//
		if (containsKey(map, INPUT)) {
			//
			bs = testAndApply(x -> x != null && isWindows(), getBytes(get(map, INPUT)), Crypt32Util::cryptProtectData,
					null);
			//
			if (Objects.equals(encoding, BASE64)) {
				//
				info(LOG, testAndApply(Objects::nonNull, bs, x -> encodeToString(Base64.getEncoder(), x), null));
				//
			} else if (Objects.equals(encoding, "hex")) {
				//
				info(LOG, testAndApply(Objects::nonNull, bs, Hex::encodeHexString, null));
				//
			} else {
				//
				throw new RuntimeException(encoding);
				//
			} // if
				//
		} else if (containsKey(map, "file")) {
			//
			final File f = testAndApply(Objects::nonNull, get(map, "file"), File::new, null);
			//
			info(LOG, "input ={}", getAbsolutePath(f));
			//
			final File fileOutput = new File(getParentFile(f), String.join(".", getName(f), "cryptProtectData"));
			//
			testAndAccept((a, b) -> b != null, fileOutput,
					testAndApply(Objects::nonNull,
							testAndApply(a -> a != null && a.exists() && a.isFile() && isWindows(), f,
									FileUtils::readFileToByteArray, null),
							Crypt32Util::cryptProtectData, null),
					FileUtils::writeByteArrayToFile);
			//
			info(LOG, "output={}", getAbsolutePath(fileOutput));
			//
		} else {
			//
			throw new RuntimeException("Please speicfy input or file");
			//
		} // if
			//
	}

	private static void cryptUnprotectData(final Map<String, String> map) throws IOException, DecoderException {
		//
		byte[] bs = null;
		//
		final String encoding = StringUtils.defaultIfEmpty(get(map, "encoding"), BASE64);
		//
		if (containsKey(map, INPUT)) {
			//
			final String input = get(map, INPUT);
			//
			if (Objects.equals(encoding, BASE64)) {
				//
				bs = testAndApply(Objects::nonNull, input, x -> decode(Base64.getDecoder(), x), null);
				//
			} else if (Objects.equals(encoding, "hex")) {
				//
				bs = testAndApply(Objects::nonNull, input, x -> Hex.decodeHex(x), null);
				//
			} else {
				//
				throw new RuntimeException(encoding);
				//
			} // if
				//
			info(LOG,
					testAndApply(Objects::nonNull,
							testAndApply(x -> x != null && isWindows(), bs, Crypt32Util::cryptUnprotectData, null),
							String::new, null));
			//
		} else if (containsKey(map, "file")) {
			//
			final String file = get(map, "file");
			//
			final File f = testAndApply(Objects::nonNull, file, File::new, null);
			//
			info(LOG, "input ={}", getAbsolutePath(f));
			//
			File fileOutput = null;
			//
			final String name = getName(f);
			//
			final File parentFile = getParentFile(f);
			//
			final String fileNameSuffix = ".cryptProtectData";
			//
			if (endsWith(name, fileNameSuffix)) {
				//
				fileOutput = new File(parentFile, StringUtils.substringBefore(file, fileNameSuffix));
				//
			} else {
				//
				fileOutput = new File(parentFile, name + ".cryptUnprotectData");
				//
			} // if
				//
			testAndAccept((a, b) -> b != null, fileOutput,
					testAndApply(Objects::nonNull,
							testAndApply(a -> a != null && a.exists() && a.isFile(),
									testAndApply(Objects::nonNull, file, File::new, null),
									FileUtils::readFileToByteArray, null),
							Crypt32Util::cryptUnprotectData, null)

					, FileUtils::writeByteArrayToFile);
			//
			info(LOG, "output={}", getAbsolutePath(fileOutput));
			//
		} else {
			//
			throw new RuntimeException("Please speicfy input or file");
			//
		} // if
			//
	}

	private static <T, U, E extends Exception> void testAndAccept(final BiPredicate<T, U> predicate, final T t,
			final U u, final FailableBiConsumer<T, U, E> consumer) throws E {
		if (predicate != null && predicate.test(t, u) && consumer != null) {
			consumer.accept(t, u);
		}
	}

	private static File getParentFile(final File instance) {
		return instance != null && instance.getPath() != null ? instance.getParentFile() : null;
	}

	private static String getAbsolutePath(final File instance) {
		return instance != null && instance.getPath() != null ? instance.getAbsolutePath() : null;
	}

	private static boolean endsWith(final String instance, final String suffix) {
		//
		if (instance == null || suffix == null) {
			//
			return false;
			//
		} // if
			//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						x -> Objects.equals(getName(x), VALUE)), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		return (field == null || Boolean.logicalAnd(Narcissus.getField(instance, field) != null,
				Narcissus.getField(suffix, field) != null)) && instance.endsWith(suffix);
		//
	}

	private static boolean containsKey(final Map<?, ?> instance, final Object key) {
		return instance != null && instance.containsKey(key);
	}

	private static void info(final Logger instance, final String msg) {
		if (instance != null) {
			instance.info(msg);
		}
	}

	private static void info(final Logger instance, final String format, final Object arg) {
		if (instance != null) {
			instance.info(format, arg);
		}
	}

	private static byte[] decode(final Decoder instance, final String string) {
		//
		if (instance == null || string == null) {
			//
			return null;
			//
		} // if
			//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(string), FieldUtils::getAllFieldsList, null)),
						f -> Objects.equals(getName(f), VALUE)), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		return field == null || Narcissus.getField(string, field) != null ? instance.decode(string) : null;
		//
	}

	private static String encodeToString(final Encoder instance, final byte[] bs) {
		return instance != null && bs != null ? instance.encodeToString(bs) : null;
	}

	private static byte[] getBytes(final String instance) {
		//
		if (instance == null) {
			//
			return null;
			//
		} // if
			//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						f -> Objects.equals(getName(f), VALUE)), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		return field == null || Narcissus.getField(instance, field) != null ? instance.getBytes() : null;
		//
	}

	private static <V> V get(final Map<?, V> instance, final Object key) {
		return instance != null ? instance.get(key) : null;
	}

	private static Map<String, String> toMap(final String... ss) {
		//
		Map<String, String> map = null;
		//
		Entry<String, String> entry = null;
		//
		for (int i = 0; i < length(ss); i++) {
			//
			if ((entry = toEntry(ArrayUtils.get(ss, i))) == null) {
				//
				continue;
				//
			} // if
				//
			put(map = ObjectUtils.getIfNull(map, LinkedHashMap::new), getKey(entry), getValue(entry));
			//
		} // for
			//
		return map;
		//
	}

	private static <K, V> void put(final Map<K, V> instance, final K key, final V value) {
		if (instance != null) {
			instance.put(key, value);
		}
	}

	private static <K> K getKey(final Entry<K, ?> instance) {
		return instance != null ? instance.getKey() : null;
	}

	private static <V> V getValue(final Entry<?, V> instance) {
		return instance != null ? instance.getValue() : null;
	}

	private static Entry<String, String> toEntry(final String string) {
		//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(string), FieldUtils::getAllFieldsList, null)),
						f -> Objects.equals(getName(f), VALUE)), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		if (string != null && field != null && Narcissus.getField(string, field) == null) {
			//
			return null;
			//
		} // if
			//
		if (Objects.equals(string, "=")) {
			//
			return Pair.of("", "");
			//
		} else if (string != null && string.length() == 2 && string.charAt(0) == '=') {
			//
			return Pair.of("", string.substring(1, string.length()));
			//
		} else if (string != null && string.length() == 2 && string.charAt(string.length() - 1) == '=') {
			//
			return Pair.of(string.substring(0, string.length() - 1), "");
			//
		} else if (string != null && string.indexOf('=') >= 0 && string.indexOf('=') == string.lastIndexOf('=')) {
			//
			return Pair.of(StringUtils.substringBefore(string, '='), StringUtils.substringAfter(string, '='));
			//
		} else if (string != null && string.length() > 2 && string.indexOf('=') != string.lastIndexOf('=')) {
			//
			return Pair.of(StringUtils.substring(string, 0, string.indexOf('=')),
					StringUtils.substring(string, string.indexOf('=') + 1));
			//
		} // if
			//
		return null;
		//
	}

	private static <T, R, A> R collect(final Stream<T> instance, final Collector<? super T, A, R> collector) {
		//
		return instance != null && (collector != null || Proxy.isProxyClass(getClass(instance)))
				? instance.collect(collector)
				: null;
		//
	}

	private static <T> Stream<T> filter(final Stream<T> instance, final Predicate<? super T> predicate) {
		return instance != null ? instance.filter(predicate) : instance;
	}

	private static <T> Stream<T> stream(final Collection<T> instance) {
		return instance != null ? instance.stream() : null;
	}

	private static <T, R, E extends Throwable> R testAndApply(final Predicate<T> predicate, final T value,
			final FailableFunction<T, R, E> functionTrue, final FailableFunction<T, R, E> functionFalse) throws E {
		return test(predicate, value) ? apply(functionTrue, value) : apply(functionFalse, value);
	}

	private static <T> boolean test(final Predicate<T> instance, final T value) {
		return instance != null && instance.test(value);
	}

	private static <T, R, E extends Throwable> R apply(final FailableFunction<T, R, E> instance, final T value)
			throws E {
		return instance != null ? instance.apply(value) : null;
	}

	private static int length(final Object[] instance) {
		return instance != null ? instance.length : 0;
	}

	private static String getName(final Class<?> instance) {
		return instance != null ? instance.getName() : null;
	}

	private static String getName(final Member instance) {
		return instance != null ? instance.getName() : null;
	}

	private static String getName(final File instance) {
		return instance != null && instance.getPath() != null ? instance.getName() : null;
	}

	private static Class<?> getClass(final Object instance) {
		return instance != null ? instance.getClass() : null;
	}

}