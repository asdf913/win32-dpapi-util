package com.sun.jna.platform.win32;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Dimension2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.Collection;
import java.util.EventObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.AbstractButton;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.text.JTextComponent;

import org.apache.commons.codec.BinaryDecoder;
import org.apache.commons.codec.BinaryEncoder;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableBiConsumer;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import io.github.toolfactory.narcissus.Narcissus;
import net.miginfocom.swing.MigLayout;

public class Win32DpApiUtil extends JPanel implements ActionListener {

	private static final long serialVersionUID = 823115347964369539L;

	private static final Logger LOG = LoggerFactory.getLogger(Win32DpApiUtil.class);

	private static final String VALUE = "value";

	private static final String BASE64 = "base64";

	private static final String INPUT = "input";

	private static final String CRYPT_PROTECT_DATA = "cryptProtectData";

	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	private @interface URL {
		String value();
	}

	@URL("https://learn.microsoft.com/en-us/windows/win32/api/dpapi/nf-dpapi-cryptprotectdata")
	private static int DEFAULT_CRYPT_PROTECT_DATA_FLAGS;

	private static int DEFAULT_CRYPT_UNPROTECT_DATA_FLAGS;

	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	private @interface Note {
		String value();
	}

	@Note("Input")
	private JTextComponent jtcCryptProtectInput = null;

	@Note("Input")
	private JTextComponent jtcCryptUnprotectInput = null;

	@Note("Flags")
	private JTextComponent jtcCryptProtectFlags = null;

	@Note("Flags")
	private JTextComponent jtcCryptUnprotectFlags = null;

	@Note("Entropy")
	private JTextComponent jtcCryptProtectEntropy = null;

	@Note("Entropy")
	private JTextComponent jtcCryptUnprotectEntropy = null;

	private JTextComponent jtcCryptProtectDescription = null;

	@Note("CryptProtect")
	private transient ComboBoxModel<?> cbmCryptProtect = null;

	private transient ComboBoxModel<?> cbmCryptUnprotect = null;

	private AbstractButton btnCryptProtect, btnCryptUnprotect = null;

	static {
		//
		final int api = Opcodes.ASM9;
		//
		try (final InputStream is = getResourceAsStream(Win32DpApiUtil.class,
				StringUtils.join("/", replace(getName(Crypt32Util.class), '.', '/'), ".class"))) {
			//
			final ClassReader cr = testAndApply(Objects::nonNull, is, ClassReader::new, null);
			//
			if (cr != null) {
				//
				final MethodVisitorImpl mvi = new MethodVisitorImpl();
				//
				cr.accept(new ClassVisitor(api) {

					@Override
					public MethodVisitor visitMethod(final int access, final String name, final String descriptor,
							final String signature, final String[] exceptions) {
						//
						if (Boolean.logicalAnd(Objects.equals(name, CRYPT_PROTECT_DATA),
								Objects.equals(descriptor, "([B)[B"))) {
							//
							return mvi;
							//
						} // if
							//
						return super.visitMethod(access, name, descriptor, signature, exceptions);
						//
					}

				}, 0);
				//
				if (mvi.integer != null) {
					//
					DEFAULT_CRYPT_PROTECT_DATA_FLAGS = mvi.integer.intValue();
					//
				} // if
					//
			} // try
				//
		} catch (final IOException e) {
			//
			error(LOG, getMessage(e), e);
			//
		} // try
			//
		try (final InputStream is = getResourceAsStream(Win32DpApiUtil.class,
				StringUtils.join("/", replace(getName(Crypt32Util.class), '.', '/'), ".class"))) {
			//
			final ClassReader cr = testAndApply(Objects::nonNull, is, ClassReader::new, null);
			//
			if (cr != null) {
				//
				final MethodVisitorImpl mvi = new MethodVisitorImpl();
				//
				cr.accept(new ClassVisitor(api) {

					@Override
					public MethodVisitor visitMethod(final int access, final String name, final String descriptor,
							final String signature, final String[] exceptions) {
						//
						if (Boolean.logicalAnd(Objects.equals(name, "cryptUnprotectData"),
								Objects.equals(descriptor, "([B)[B"))) {
							//
							return mvi;
							//
						} // if
							//
						return super.visitMethod(access, name, descriptor, signature, exceptions);
						//
					}

				}, 0);
				//
				if (mvi.integer != null) {
					//
					DEFAULT_CRYPT_UNPROTECT_DATA_FLAGS = mvi.integer.intValue();
					//
				} // if
					//
			} // try
				//
		} catch (final IOException e) {
			//
			error(LOG, getMessage(e), e);
			//
		} // try
			//
	}

	private static void error(final Logger instance, final String msg, final Throwable t) {
		if (instance != null) {
			instance.error(msg, t);
		}
	}

	private static String getMessage(final Throwable instance) {
		return instance != null ? instance.getMessage() : null;
	}

	private static InputStream getResourceAsStream(final Class<?> clz, final String name) {
		//
		if (clz == null) {
			//
			return null;
			//
		} // if
			//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(name), FieldUtils::getAllFieldsList, null)),
						x -> Objects.equals(getName(x), VALUE)), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		return field == null || Narcissus.getField(name, field) != null ? clz.getResourceAsStream(name) : null;
		//
	}

	private static class MethodVisitorImpl extends MethodVisitor {

		private Integer integer = null;

		private MethodVisitorImpl() {
			//
			super(Opcodes.ASM9);
			//
		}

		@Override
		public void visitInsn(final int opcode) {
			//
			if (opcode == Opcodes.ICONST_0) {
				//
				if (integer != null) {
					//
					throw new RuntimeException();
					//
				} // if
					//
				integer = Integer.valueOf(0);
				//
				return;
				//
			} else if (opcode == Opcodes.ARETURN) {
				//
				return;
				//
			} // if
				//
			throw new RuntimeException();
			//
		}

	}

	private static String replace(final String instance, final char oldChar, final char newChar) {
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
						x -> Objects.equals(getName(x), VALUE)), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		return field == null || Narcissus.getField(instance, field) != null ? instance.replace(oldChar, newChar) : null;
		//
	}

	public static void main(final String[] args)
			throws IOException, DecoderException, InstantiationException, IllegalAccessException, EncoderException {
		//
		final Map<String, String> map = toMap(args);
		//
		final boolean isWindows = isWindows();
		//
		final String message = "Please run under Microsoft Windows";
		//
		if (Boolean.logicalOr(isGui(), Objects.equals(get(map, "gui"), "true"))) {
			//
			final Win32DpApiUtil instance = new Win32DpApiUtil();
			//
			instance.setLayout(new MigLayout());
			//
			if (isWindows || true// TODO
			) {
				//
				final JTabbedPane jtp = new JTabbedPane();
				//
				final JPanel jPanel1 = new JPanel();
				//
				jPanel1.setLayout(new MigLayout());
				//
				jPanel1.add(new JLabel("Encoding"));
				//
				final Map<String, Class<?>> encodingMap = getEncodingMap(getContextClassLoader(Thread.currentThread()));
				//
				final String wrap = "wrap";
				//
				final String[] encodings = toArray(keySet(encodingMap), new String[] {});
				//
				Arrays.sort(encodings, Win32DpApiUtil::compare);
				//
				jPanel1.add(new JComboBox<>(instance.cbmCryptProtect = new DefaultComboBoxModel<>(encodings)),
						String.format("wmin %1$s,%2$s", 160, wrap));
				//
				jPanel1.add(new JLabel("Input"));
				//
				final String growx = "growx";
				//
				jPanel1.add(instance.jtcCryptProtectInput = new JTextField(), String.format("%1$s,%2$s", growx, wrap));
				//
				jPanel1.add(new JLabel("Flags"));
				//
				jPanel1.add(instance.jtcCryptProtectFlags = new JTextField(), String.format("%1$s,%2$s", growx, wrap));
				//
				jPanel1.add(new JLabel("Entropy"));
				//
				jPanel1.add(instance.jtcCryptProtectEntropy = new JTextField(),
						String.format("%1$s,%2$s", growx, wrap));
				//
				final JLabel jLabel = new JLabel("Description");
				//
				jPanel1.add(jLabel);
				//
				jPanel1.add(instance.jtcCryptProtectDescription = new JTextField(),
						String.format("%1$s,%2$s", growx, wrap));
				//
				jPanel1.add(new JLabel());
				//
				jPanel1.add(instance.btnCryptProtect = new JButton("Execute"), String.format("%1$s,%2$s", growx, wrap));
				//
				instance.btnCryptProtect.addActionListener(instance);
				//
				jtp.add("CryptProtect", jPanel1);
				//
				final JPanel jPanel2 = new JPanel();
				//
				jPanel2.setLayout(new MigLayout());
				//
				jPanel2.add(new JLabel("Encoding"));
				//
				jPanel2.add(new JComboBox<>(instance.cbmCryptUnprotect = new DefaultComboBoxModel<>(encodings)),
						String.format("wmin %1$s,%2$s", 160, wrap));
				//
				jPanel2.add(new JLabel("Input"), String.format("wmin %1$s", getWidth(jLabel.getPreferredSize())));
				//
				jPanel2.add(instance.jtcCryptUnprotectInput = new JTextField(),
						String.format("wmin %1$s,%2$s", 160, wrap));
				//
				jPanel2.add(new JLabel("Flags"));
				//
				jPanel2.add(instance.jtcCryptUnprotectFlags = new JTextField(),
						String.format("%1$s,%2$s", growx, wrap));
				//
				jPanel2.add(new JLabel("Entropy"));
				//
				jPanel2.add(instance.jtcCryptUnprotectEntropy = new JTextField(),
						String.format("%1$s,%2$s", growx, wrap));
				//
				jPanel2.add(new JLabel());
				//
				jPanel2.add(instance.btnCryptUnprotect = new JButton("Execute"),
						String.format("%1$s,%2$s", growx, wrap));
				//
				instance.btnCryptUnprotect.addActionListener(instance);
				//
				jtp.add("CryptUnprotect", jPanel2);
				//
				instance.add(jtp, String.format("wmin %1$s", 219));
				//
			} else {
				//
				instance.add(new JLabel(message));
				//
			} // if
				//
			final JFrame jFrame = testAndGet(!GraphicsEnvironment.isHeadless(), JFrame::new);
			//
			if (jFrame != null) {
				//
				jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				//
				jFrame.add(instance);
				//
				jFrame.pack();
				//
			} // if
				//
			testAndRun(!isTestMode(), () -> setVisible(jFrame, true));
			//
			return;
			//
		} // if
			//
		if (!isWindows) {
			//
			info(LOG, message);
			//
			return;
			//
		} // if
			//
		final String action = get(map, "action");
		//
		final Map<String, Class<?>> encodingMap = getEncodingMap(getContextClassLoader(Thread.currentThread()));
		//
		if (Objects.equals(action, CRYPT_PROTECT_DATA)) {
			//
			cryptProtectData(map, encodingMap);
			//
		} else if (Objects.equals(action, "cryptUnprotectData")) {
			//
			cryptUnprotectData(map, encodingMap);
			//
		} else {
			//
			throw new RuntimeException(action);
			//
		} // if
			//
	}

	private static int compare(final String a, final String b) {
		//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(stream(testAndApply(Objects::nonNull, getClass(a), FieldUtils::getAllFieldsList, null)),
						x -> Objects.equals(getName(x), VALUE)), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		if (field != null
				&& Boolean.logicalOr(Narcissus.getField(a, field) == null, Narcissus.getField(b, field) == null)) {
			//
			return 0;
			//
		} // if
			//
		final int compare = Integer.compare(StringUtils.length(a), StringUtils.length(b));
		//
		if (compare < 0) {
			//
			return compare;
			//
		} // if
			//
		final Pattern pattern = Pattern.compile("\\d+");
		//
		Matcher matcher = matcher(pattern, a);
		//
		String s = null;
		//
		Integer ia = null;
		//
		if (find(matcher) && NumberUtils.isDigits(s = group(matcher))) {
			//
			ia = Integer.valueOf(s);
			//
		} // if
			//
		Integer ib = null;
		//
		if (find(matcher = matcher(pattern, b)) && NumberUtils.isDigits(s = group(matcher))) {
			//
			ib = Integer.valueOf(s);
			//
		} // if
			//
		return ObjectUtils.compare(ia, ib);
		//
	}

	private static <K> Set<K> keySet(final Map<K, ?> instance) {
		return instance != null ? instance.keySet() : null;
	}

	private static <T> T[] toArray(final Collection<T> instance, final T[] array) {
		return instance != null ? instance.toArray(array) : null;
	}

	private static Map<String, Class<?>> getEncodingMap(final ClassLoader classLoader) {
		//
		return collect(filter(
				map(stream(getTopLevelClasses(from(classLoader), "org.apache.commons.codec.binary")),
						Win32DpApiUtil::load),
				x -> Boolean.logicalAnd(Boolean.logicalAnd(isAssignableFrom(BinaryEncoder.class, x),
						isAssignableFrom(BinaryDecoder.class, x)), !isAbstract(x))),
				Collectors.toMap(x -> {
					//
					final String simpleName = getSimpleName(x);
					//
					if (matches(simpleName, "^\\w+\\d{2}$")) {
						//
						return toLowerCase(simpleName);
						//
					} // if
						//
					final Matcher matcher = matcher(Pattern.compile("([A-Z][a-z]+)"), simpleName);
					//
					if (find(matcher)) {
						//
						return toLowerCase(group(matcher));
						//
					} // if
						//
					return simpleName;
					//
				}, Function.identity()));
		//
	}

	private static Double getWidth(final Dimension2D instance) {
		return instance != null ? instance.getWidth() : null;
	}

	private static void setVisible(final Component instnace, final boolean visible) {
		if (instnace != null) {
			instnace.setVisible(visible);
		}
	}

	private static void testAndRun(final boolean condition, final Runnable runnable) {
		if (condition && runnable != null) {
			runnable.run();
		}
	}

	private static <T> T testAndGet(final boolean condition, final Supplier<T> supplier) {
		return condition && supplier != null ? supplier.get() : null;
	}

	private static boolean isAbstract(final Class<?> instance) {
		return instance != null && Modifier.isAbstract(instance.getModifiers());
	}

	private static boolean isAssignableFrom(final Class<?> a, final Class<?> b) {
		return a != null && b != null && a.isAssignableFrom(b);
	}

	private static boolean isTestMode() {
		try {
			return Class.forName("org.testng.annotations.Test") != null;
		} catch (final ClassNotFoundException e) {
			return false;
		}
	}

	private static boolean isGui() {
		//
		if (Objects.equals(getName(getClass(FileSystems.getDefault())), "sun.nio.fs.MacOSXFileSystem")) {
			//
			return System.console() == null;
			//
		} // if
			//
		if (isWindows()) {
			//
			final Matcher matcher = matcher(Pattern.compile("\\d+"), getName(ManagementFactory.getRuntimeMXBean()));
			//
			if (find(matcher)) {
				//
				return BooleanUtils.toBooleanDefaultIfNull(testAndApply(NumberUtils::isDigits, group(matcher),
						x -> endsWith(Kernel32Util.QueryFullProcessImageName(NumberUtils.toInt(x), 0), "javaw.exe"),
						null), false);
				//
			} // if
				//
		} // if
			//
		return false;
		//
	}

	private static String getName(final RuntimeMXBean instance) {
		return instance != null ? instance.getName() : null;
	}

	private static ClassPath from(final ClassLoader classloader) {
		//
		final List<Method> ms = collect(
				filter(testAndApply(Objects::nonNull, ClassPath.class.getDeclaredMethods(), Arrays::stream, null),
						x -> Boolean.logicalAnd(Objects.equals(getName(x), "from"),
								Arrays.equals(getParameterTypes(x), new Class<?>[] { ClassLoader.class }))),
				Collectors.toList());
		//
		return cast(ClassPath.class,
				testAndApply((a, b) -> isStatic(a) && b != null && b.getParent() != null,
						testAndApply(x -> IterableUtils.size(x) == 1, ms, x -> IterableUtils.get(x, 0), null),
						classloader, Narcissus::invokeStaticMethod, null));
		//
	}

	private static Class<?>[] getParameterTypes(final Executable instance) {
		//
		if (instance == null) {
			//
			return null;
			//
		} // if
			//
		final Stream<Field> stream = testAndApply(Objects::nonNull,
				testAndApply(Objects::nonNull, getClass(instance), Narcissus::getDeclaredFields, null), Arrays::stream,
				null);
		//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(stream, x -> Objects.equals(getName(x), "parameterTypes")), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		return field == null || Narcissus.getField(instance, field) != null ? instance.getParameterTypes() : null;
		//
	}

	private static <T, U, R> R testAndApply(final BiPredicate<T, U> predicate, final T t, final U u,
			final BiFunction<T, U, R> functionTrue, final BiFunction<T, U, R> functionFalse) {
		return (predicate != null && predicate.test(t, u)) ? apply(functionTrue, t, u) : apply(functionFalse, t, u);
	}

	private static <T, U, R> R apply(final BiFunction<T, U, R> instance, final T t, final U u) {
		return instance != null ? instance.apply(t, u) : null;
	}

	private static boolean isStatic(final Member instance) {
		return instance != null && Modifier.isStatic(instance.getModifiers());
	}

	private static String group(final MatchResult instance) {
		return instance != null ? instance.group() : null;
	}

	private static boolean find(final Matcher instance) {
		//
		if (instance == null) {
			//
			return false;
			//
		} // if
			//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						x -> Objects.equals(getName(x), "groups")), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		return (field == null || Narcissus.getField(instance, field) != null) && instance.find();
		//
	}

	private static Matcher matcher(final Pattern instance, final String input) {
		//
		if (Boolean.logicalOr(instance == null, input == null)) {
			//
			return null;
			//
		} // if
			//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						x -> Objects.equals(getName(x), "normalizedPattern")), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		return (field == null || Narcissus.getField(instance, field) != null) ? instance.matcher(input) : null;
		//
	}

	private static String toLowerCase(final String instance) {
		//
		if (instance == null) {
			//
			return instance;
			//
		} // if
			//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						x -> Objects.equals(getName(x), VALUE)), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		return (field == null || Narcissus.getField(instance, field) != null) ? instance.toLowerCase() : null;
		//
	}

	private static boolean matches(final String instance, final String regex) {
		//
		if (instance == null) {
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
		if (field != null && Narcissus.getField(regex, field) == null) {
			//
			return false;
			//
		} // if
			//
		return (field == null || Narcissus.getField(instance, field) != null) && instance.matches(regex);
		//
	}

	private static String getSimpleName(final Class<?> instance) {
		return instance != null ? instance.getSimpleName() : null;
	}

	private static Class<?> load(final ClassInfo instance) {
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
						x -> Objects.equals(getName(x), "loader")), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		return field == null || Narcissus.getField(instance, field) != null ? instance.load() : null;
		//
	}

	private static <T, R> Stream<R> map(final Stream<T> instance, final Function<? super T, ? extends R> mapper) {
		return instance != null ? instance.map(mapper) : null;
	}

	private static ImmutableSet<ClassInfo> getTopLevelClasses(final ClassPath instance, final String packageName) {
		//
		if (instance == null || packageName == null) {
			//
			return null;
			//
		} // if
			//
		Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(stream(
						testAndApply(Objects::nonNull, getClass(packageName), FieldUtils::getAllFieldsList, null)),
						x -> Objects.equals(getName(x), VALUE)), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		if (field != null && Narcissus.getField(packageName, field) == null) {
			//
			return null;
			//
		} // if
			//
		return (field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						x -> Objects.equals(getName(x), "resources")), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null)) == null || Narcissus.getField(instance, field) != null
						? instance.getTopLevelClasses(packageName)
						: null;
		//
	}

	private static ClassLoader getContextClassLoader(final Thread instance) {
		return instance != null ? instance.getContextClassLoader() : null;
	}

	private static boolean isWindows() {
		return Objects.equals(getName(getClass(FileSystems.getDefault())), "sun.nio.fs.WindowsFileSystem");
	}

	private static void cryptProtectData(final Map<String, String> map, final Map<String, Class<?>> classMap)
			throws IOException, InstantiationException, IllegalAccessException, EncoderException {
		//
		final Integer flags = testAndApply(x -> containsKey(map, x), "flags",
				x -> testAndApply(NumberUtils::isDigits, get(map, x), Integer::valueOf, null), null);
		//
		final byte[] entropy = getBytes(get(map, "entropy"));
		//
		final String description = get(map, "description");
		//
		if (containsKey(map, INPUT)) {
			//
			final byte[] bs = cryptProtectData(getBytes(get(map, INPUT)), entropy, flags, description);
			//
			final String encoding = StringUtils.defaultIfEmpty(get(map, "encoding"), BASE64);
			//
			if (!containsKey(classMap, encoding)) {
				//
				throw new RuntimeException(String
						.format("Please specify the encoding from the following encoding (%1$s). ", keySet(classMap)));
				//
			} // if
				//
			final Class<?> clz = get(classMap, encoding);
			//
			final BinaryEncoder binaryEncoder = cast(BinaryEncoder.class, clz != null ? clz.newInstance() : null);
			//
			info(LOG, testAndApply(Objects::nonNull,
					binaryEncoder != null && bs != null ? binaryEncoder.encode(bs) : null, String::new, null));
			//
		} else if (containsKey(map, "file")) {
			//
			final File f = testAndApply(Objects::nonNull, get(map, "file"), File::new, null);
			//
			info(LOG, "input ={}", getAbsolutePath(f));
			//
			final File fileOutput = new File(getParentFile(f), String.join(".", getName(f), CRYPT_PROTECT_DATA));
			//
			testAndAccept((a, b) -> b != null, fileOutput,
					cryptProtectData(
							testAndApply(a -> and(a, Win32DpApiUtil::exists, Win32DpApiUtil::isFile) && isWindows(), f,
									FileUtils::readFileToByteArray, null),
							entropy, flags, description),
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

	private static byte[] cryptProtectData(final byte[] data, final byte[] entropy, final Integer flags,
			final String description) {
		//
		if (data == null || !isWindows()) {
			//
			return null;
			//
		} // if
			//
		if (entropy != null || StringUtils.isNotEmpty(description)) {
			//
			return flags != null ? Crypt32Util.cryptProtectData(data, entropy, flags.intValue(), description, null)
					: Crypt32Util.cryptProtectData(data, entropy, DEFAULT_CRYPT_PROTECT_DATA_FLAGS, description, null);
			//
		} // if
			//
		return flags != null ? Crypt32Util.cryptProtectData(data, flags.intValue())
				: Crypt32Util.cryptProtectData(data);
		//
	}

	private static void cryptUnprotectData(final Map<String, String> map, final Map<String, Class<?>> classMap)
			throws IOException, DecoderException, InstantiationException, IllegalAccessException {
		//
		final Integer flags = testAndApply(x -> containsKey(map, x), "flags",
				x -> testAndApply(NumberUtils::isDigits, get(map, x), Integer::valueOf, null), null);
		//
		final byte[] entropy = getBytes(get(map, "entropy"));
		//
		if (containsKey(map, INPUT)) {
			//
			final String encoding = StringUtils.defaultIfEmpty(get(map, "encoding"), BASE64);
			//
			if (!containsKey(classMap, encoding)) {
				//
				throw new RuntimeException(String
						.format("Please specify the encoding from the following encoding (%1$s). ", keySet(classMap)));
				//
			} // if
				//
			final Class<?> clz = get(classMap, encoding);
			//
			info(LOG, testAndApply(Objects::nonNull,
					cryptUnprotectData(toByteArray(get(map, INPUT),
							cast(BinaryDecoder.class, clz != null ? clz.newInstance() : null)), entropy, flags),
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
			final byte[] bs = cryptUnprotectData(
					testAndApply(a -> and(a, Win32DpApiUtil::exists, Win32DpApiUtil::isFile),
							testAndApply(Objects::nonNull, file, File::new, null), FileUtils::readFileToByteArray,
							null),
					entropy, flags);
			//
			if (!Arrays.equals(testAndApply(x -> and(x, Win32DpApiUtil::exists, Win32DpApiUtil::isFile), fileOutput,
					FileUtils::readFileToByteArray, null), bs)) {
				//
				FileUtils.writeByteArrayToFile(fileOutput, bs);
				//
			} // if
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

	private static byte[] cryptUnprotectData(final byte[] data, final byte[] entropy, final Integer flags) {
		//
		if (data == null || !isWindows()) {
			//
			return null;
			//
		} // if
			//
		if (entropy != null) {
			//
			return flags != null ? Crypt32Util.cryptUnprotectData(data, entropy, flags.intValue(), null)
					: Crypt32Util.cryptUnprotectData(data, entropy, DEFAULT_CRYPT_UNPROTECT_DATA_FLAGS, null);
			//
		} // if
			//
		return flags != null ? Crypt32Util.cryptUnprotectData(data, flags.intValue())
				: Crypt32Util.cryptUnprotectData(data);
		//
	}

	private static <T> T cast(final Class<T> clz, final Object instance) {
		return clz != null && clz.isInstance(instance) ? clz.cast(instance) : null;
	}

	private static byte[] toByteArray(final String instance, final BinaryDecoder binaryDecoder)
			throws DecoderException {
		//
		final Field field = testAndApply(x -> IterableUtils.size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						x -> Objects.equals(getName(x), VALUE)), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		if (instance != null && field != null && Narcissus.getField(instance, field) == null) {
			//
			return null;
			//
		} // if
			//
		return binaryDecoder != null ? binaryDecoder.decode(getBytes(instance)) : null;
		//
	}

	private static <T> boolean and(final T value, final Predicate<T> a, final Predicate<T> b) {
		return test(a, value) && test(b, value);
	}

	private static boolean isFile(final File instance) {
		return instance != null && instance.getPath() != null && instance.isFile();
	}

	private static boolean exists(final File instance) {
		return instance != null && instance.getPath() != null && instance.exists();
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

	@Override
	public void actionPerformed(final ActionEvent evt) {
		//
		final Object source = getSource(evt) ;
		//
		if (Objects.equals(source, btnCryptProtect)) {
			//
			final byte[] bs = cryptProtectData(getBytes(getText(jtcCryptProtectInput)),
					getBytes(getText(jtcCryptProtectEntropy)),
					testAndApply(NumberUtils::isDigits, getText(jtcCryptProtectFlags), Integer::valueOf, null),
					getText(jtcCryptProtectDescription));
			//
			final Map<String, Class<?>> encodingMap = getEncodingMap(getContextClassLoader(Thread.currentThread()));
			//
			final String encoding = StringUtils.defaultIfEmpty(Objects.toString(getSelectedItem(cbmCryptProtect)),
					BASE64);
			//
			if (!containsKey(encodingMap, encoding)) {
				//
				throw new RuntimeException(String.format(
						"Please specify the encoding from the following encoding (%1$s). ", keySet(encodingMap)));
				//
			} // if
				//
			final Class<?> clz = get(encodingMap, encoding);
			//
			BinaryEncoder binaryEncoder = null;
			//
			try {
				//
				binaryEncoder = cast(BinaryEncoder.class, clz != null ? clz.newInstance() : null);
				//
				info(LOG, testAndApply(Objects::nonNull,
						binaryEncoder != null && bs != null ? binaryEncoder.encode(bs) : null, String::new, null));
				//
			} catch (final InstantiationException | IllegalAccessException | EncoderException e) {
				//
				throw new RuntimeException(e);
				//
			} // try
				//
		} else if (Objects.equals(source, btnCryptUnprotect)) {
			//
			final Map<String, Class<?>> encodingMap = getEncodingMap(getContextClassLoader(Thread.currentThread()));
			//
			final String encoding = StringUtils.defaultIfEmpty(Objects.toString(getSelectedItem(cbmCryptUnprotect)),
					BASE64);
			//
			if (!containsKey(encodingMap, encoding)) {
				//
				throw new RuntimeException(String.format(
						"Please specify the encoding from the following encoding (%1$s). ", keySet(encodingMap)));
				//
			} // if
				//
			final Class<?> clz = get(encodingMap, encoding);
			//
			try {
				//
				info(LOG, testAndApply(Objects::nonNull, cryptUnprotectData(
						toByteArray(getText(jtcCryptUnprotectInput),
								cast(BinaryDecoder.class, clz != null ? clz.newInstance() : null)),
						getBytes(getText(jtcCryptUnprotectEntropy)),
						testAndApply(NumberUtils::isDigits, getText(jtcCryptUnprotectFlags), Integer::valueOf, null)),
						String::new, null));
				//
			} catch (final InstantiationException | IllegalAccessException | DecoderException e) {
				//
				throw new RuntimeException(e);
				//
			} // try
				//
		} // if
			//
	}

	private static Object getSource(final EventObject instance) {
		return instance != null ? instance.getSource() : null;
	}

	private static Object getSelectedItem(final ComboBoxModel<?> instance) {
		return instance != null ? instance.getSelectedItem() : null;
	}

	private static String getText(final JTextComponent instance) {
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
						x -> Objects.equals(getName(x), "model")), Collectors.toList()),
				x -> IterableUtils.get(x, 0), null);
		//
		return field == null || Narcissus.getField(instance, field) != null ? instance.getText() : null;
		//
	}

}