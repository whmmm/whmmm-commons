package org.whmmm.util.linq;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * LINQ 风格流式操作包装器，参考 C# LINQ API 设计。
 * 链式方法返回 {@link LinqStream}，终端方法返回具体值或集合。
 *
 * <h3>延迟排序机制</h3>
 * {@link #orderBy} / {@link #orderByDescending} 设置初始比较器，
 * {@link #thenBy} / {@link #thenByDescending} 追加比较器，
 * 真正的排序推迟到终端操作（如 {@link #toList}、{@link #first} 等）时才执行。
 * 这样可以支持在 orderBy 之后继续使用 where、skip 等中间操作，最终一次性排序。
 *
 * <pre>{@code
 * list.orderBy(Person::getName)       // 设置排序键
 *     .where(p -> p.getAge() > 18)    // 过滤（保留排序）
 *     .thenBy(Person::getAge)         // 追加次要排序键
 *     .toList();                      // 终端操作时真正排序
 * }</pre>
 *
 * @param <T> 元素类型
 */
public class LinqStream<T> implements Serializable {
    private final Stream<T> stream;

    /**
     * 比较器链，支持 orderBy / thenBy 等延迟排序。
     * 终端操作时（toList, first, last 等）才会实际执行排序。
     */
    @Nullable
    private final Comparator<? super T> comparator;

    // ---- 构造 ----

    public LinqStream(Stream<T> stream) {
        this(stream, null);
    }

    private LinqStream(Stream<T> stream, @Nullable Comparator<? super T> comparator) {
        this.stream = stream;
        this.comparator = comparator;
    }

    /**
     * 创建空的 LinqStream
     */
    public static <T> LinqStream<T> empty() {
        return new LinqStream<>(Stream.empty());
    }

    /**
     * 从可变参数创建 LinqStream
     */
    @SafeVarargs
    public static <T> LinqStream<T> of(T... values) {
        return new LinqStream<>(Stream.of(values));
    }

    /**
     * 获取底层 Java Stream（只读，未经过延迟排序处理）
     */
    public Stream<T> stream() {
        return stream;
    }

    // ==================== 延迟排序核心 ====================

    /**
     * 如果设置了比较器链，则返回排序后的流；否则返回原流。
     * 所有终端操作都应通过此方法获取流，以保证延迟排序生效。
     */
    private Stream<T> sortedStream() {
        if (comparator != null) {
            return stream.sorted(comparator);
        }
        return stream;
    }

    /**
     * 获取当前的比较器链，用于子类或调试
     */
    @Nullable
    public Comparator<? super T> comparator() {
        return comparator;
    }

    // ==================== 元素获取 ====================

    /**
     * 返回第一个元素，没有则返回 null
     */
    @Nullable
    public T firstOrDefault() {
        return this.sortedStream().findFirst().orElse(null);
    }

    /**
     * 返回第一个满足条件的元素，没有则返回 null
     */
    @Nullable
    public T firstOrDefault(@Nonnull Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        return this.sortedStream().filter(predicate).findFirst().orElse(null);
    }

    /**
     * 返回第一个元素，流为空则抛异常
     */
    @Nonnull
    public T first() {
        T t = this.firstOrDefault();
        if (t == null) {
            throw new NoSuchElementException("stream is empty");
        }
        return t;
    }

    /**
     * 返回第一个满足条件的元素，没有则抛异常
     */
    @Nonnull
    public T first(@Nonnull Predicate<? super T> predicate) {
        T t = this.firstOrDefault(predicate);
        if (t == null) {
            throw new NoSuchElementException("no element matching predicate");
        }
        return t;
    }

    /**
     * 返回最后一个元素，没有则返回 null
     */
    @Nullable
    public T lastOrDefault() {
        return this.sortedStream().reduce((a, b) -> b).orElse(null);
    }

    /**
     * 返回最后一个满足条件的元素，没有则返回 null
     */
    @Nullable
    public T lastOrDefault(@Nonnull Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        List<T> list = toList(); // toList 内部已使用 sortedStream
        for (int i = list.size() - 1; i >= 0; i--) {
            if (predicate.test(list.get(i))) {
                return list.get(i);
            }
        }
        return null;
    }

    /**
     * 返回最后一个元素，流为空则抛异常
     */
    @Nonnull
    public T last() {
        T t = this.lastOrDefault();
        if (t == null) {
            throw new NoSuchElementException("stream is empty");
        }
        return t;
    }

    /**
     * 返回最后一个满足条件的元素，没有则抛异常
     */
    @Nonnull
    public T last(@Nonnull Predicate<? super T> predicate) {
        T t = this.lastOrDefault(predicate);
        if (t == null) {
            throw new NoSuchElementException("no element matching predicate");
        }
        return t;
    }

    /**
     * 返回唯一的元素（0个或1个），多个则抛异常
     */
    @Nullable
    public T singleOrDefault() {
        Iterator<T> it = this.sortedStream().iterator();
        if (!it.hasNext()) return null;
        T result = it.next();
        if (it.hasNext()) {
            throw new IllegalStateException("stream contains more than one element");
        }
        return result;
    }

    /**
     * 返回唯一的元素（0个或1个），没有则返回默认值
     */
    @Nullable
    public T singleOrDefault(@Nullable T defaultValue) {
        T t = singleOrDefault();
        return t != null ? t : defaultValue;
    }

    /**
     * 返回唯一的元素，空或多于一个则抛异常
     */
    @Nonnull
    public T single() {
        T t = this.singleOrDefault();
        if (t == null) {
            throw new NoSuchElementException("stream is empty");
        }
        return t;
    }

    /**
     * 返回指定索引的元素，越界返回 null
     */
    @Nullable
    public T elementAtOrDefault(int index) {
        if (index < 0) return null;
        return this.sortedStream().skip(index).findFirst().orElse(null);
    }

    /**
     * 返回指定索引的元素，越界返回默认值
     */
    @Nullable
    public T elementAtOrDefault(int index, @Nullable T defaultValue) {
        T t = elementAtOrDefault(index);
        return t != null ? t : defaultValue;
    }

    /**
     * 返回指定索引的元素，越界抛异常
     */
    @Nonnull
    public T elementAt(int index) {
        T t = elementAtOrDefault(index);
        if (t == null) {
            throw new IndexOutOfBoundsException("index " + index + " out of bounds");
        }
        return t;
    }

    // ==================== 过滤 / 投影 ====================

    /**
     * 过滤，等价于 C# Where。保留已有的排序比较器链。
     */
    public LinqStream<T> where(@Nonnull Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        return new LinqStream<>(this.stream().filter(predicate), this.comparator);
    }

    /**
     * 投影/映射，等价于 C# Select。元素类型改变，丢弃排序比较器。
     */
    public <R> LinqStream<R> select(@Nonnull Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper);
        return new LinqStream<>(this.stream().map(mapper));
    }

    /**
     * 扁平映射，等价于 C# SelectMany。元素类型改变，丢弃排序比较器。
     */
    public <R> LinqStream<R> selectMany(@Nonnull Function<? super T, ? extends Stream<? extends R>> mapper) {
        Objects.requireNonNull(mapper);
        return new LinqStream<>(this.stream().flatMap(mapper));
    }

    /**
     * 扁平映射（LinqStream 版本）
     */
    public <R> LinqStream<R> selectManyLinq(@Nonnull Function<? super T, ? extends LinqStream<? extends R>> mapper) {
        Objects.requireNonNull(mapper);
        return new LinqStream<>(
                this.stream().flatMap(t -> mapper.apply(t).stream())
        );
    }

    /**
     * 去重，等价于 C# Distinct。保留已有的排序比较器链。
     */
    public LinqStream<T> distinct() {
        return new LinqStream<>(this.stream().distinct(), this.comparator);
    }

    /**
     * 按指定键去重，等价于 C# DistinctBy。保留已有的排序比较器链。
     */
    public <R> LinqStream<T> distinctBy(@Nonnull Function<? super T, ? extends R> keySelector) {
        Objects.requireNonNull(keySelector);
        Set<R> seen = new HashSet<>();
        return new LinqStream<>(
                this.stream().filter(t -> seen.add(keySelector.apply(t))),
                this.comparator
        );
    }

    /**
     * 按类型过滤并强转，等价于 C# OfType。元素类型改变，丢弃排序比较器。
     */
    @SuppressWarnings("unchecked")
    public <R> LinqStream<R> ofType(@Nonnull Class<R> type) {
        Objects.requireNonNull(type);
        return new LinqStream<>((Stream<R>) this.stream().filter(type::isInstance));
    }

    /**
     * 强制类型转换，等价于 C# Cast。元素类型改变，丢弃排序比较器。
     */
    @SuppressWarnings("unchecked")
    public <R> LinqStream<R> cast(@Nonnull Class<R> type) {
        Objects.requireNonNull(type);
        return new LinqStream<>((Stream<R>) this.stream().peek(t -> {
            if (t != null && !type.isInstance(t)) {
                throw new ClassCastException(
                        "Cannot cast " + t.getClass().getName() + " to " + type.getName());
            }
        }));
    }

    /**
     * 过滤 null 元素。保留已有的排序比较器链。
     */
    public LinqStream<T> nonNull() {
        return where(Objects::nonNull);
    }

    // ==================== 排序（延迟） ====================

    /**
     * 按比较器升序排序（延迟），等价于 C# OrderBy。
     * 后续可通过 {@link #thenBy} / {@link #thenByDescending} 追加排序。
     */
    public LinqStream<T> orderBy(@Nonnull Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return new LinqStream<>(this.stream, comparator);
    }

    /**
     * 按键选择器升序排序（延迟），等价于 C# OrderBy。
     * 后续可通过 {@link #thenBy} / {@link #thenByDescending} 追加排序。
     */
    public <U extends Comparable<? super U>> LinqStream<T> orderBy(
            @Nonnull Function<? super T, ? extends U> keySelector) {
        Objects.requireNonNull(keySelector);
        return orderBy(Comparator.comparing(keySelector));
    }

    /**
     * 按比较器降序排序（延迟），等价于 C# OrderByDescending。
     * 后续可通过 {@link #thenBy} / {@link #thenByDescending} 追加排序。
     */
    public LinqStream<T> orderByDescending(@Nonnull Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return new LinqStream<>(this.stream, comparator.reversed());
    }

    /**
     * 按键选择器降序排序（延迟），等价于 C# OrderByDescending。
     * 后续可通过 {@link #thenBy} / {@link #thenByDescending} 追加排序。
     */
    public <U extends Comparable<? super U>> LinqStream<T> orderByDescending(
            @Nonnull Function<? super T, ? extends U> keySelector) {
        Objects.requireNonNull(keySelector);
        return orderByDescending(Comparator.comparing(keySelector));
    }

    /**
     * 追加升序排序（延迟），必须在 {@link #orderBy} 或 {@link #orderByDescending} 之后调用。
     * 等价于 C# ThenBy。
     *
     * @throws IllegalStateException 如果之前没有调用 orderBy / orderByDescending
     */
    public LinqStream<T> thenBy(@Nonnull Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        if (this.comparator == null) {
            throw new IllegalStateException(
                    "thenBy must be called after orderBy or orderByDescending");
        }
        return new LinqStream<>(this.stream, this.comparator.thenComparing((Comparator) comparator));
    }

    /**
     * 按键选择器追加升序排序（延迟），等价于 C# ThenBy。
     *
     * @throws IllegalStateException 如果之前没有调用 orderBy / orderByDescending
     */
    public <U extends Comparable<? super U>> LinqStream<T> thenBy(
            @Nonnull Function<? super T, ? extends U> keySelector) {
        Objects.requireNonNull(keySelector);
        return thenBy(Comparator.comparing(keySelector));
    }

    /**
     * 追加降序排序（延迟），必须在 {@link #orderBy} 或 {@link #orderByDescending} 之后调用。
     * 等价于 C# ThenByDescending。
     *
     * @throws IllegalStateException 如果之前没有调用 orderBy / orderByDescending
     */
    public LinqStream<T> thenByDescending(@Nonnull Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return thenBy(comparator.reversed());
    }

    /**
     * 按键选择器追加降序排序（延迟），等价于 C# ThenByDescending。
     *
     * @throws IllegalStateException 如果之前没有调用 orderBy / orderByDescending
     */
    public <U extends Comparable<? super U>> LinqStream<T> thenByDescending(
            @Nonnull Function<? super T, ? extends U> keySelector) {
        Objects.requireNonNull(keySelector);
        return thenByDescending(Comparator.comparing(keySelector));
    }

    // ==================== 分页 ====================

    /**
     * 跳过前 n 个元素，等价于 C# Skip。保留已有的排序比较器链。
     */
    public LinqStream<T> skip(long n) {
        return new LinqStream<>(this.stream().skip(n), this.comparator);
    }

    /**
     * 取前 n 个元素，等价于 C# Take。保留已有的排序比较器链。
     */
    public LinqStream<T> take(long n) {
        return new LinqStream<>(this.stream().limit(n), this.comparator);
    }

    /**
     * 跳过最后 n 个元素，等价于 C# SkipLast。
     * 需要先收集为 List，此后排序比较器已应用，不再保留。
     */
    public LinqStream<T> skipLast(int n) {
        List<T> list = toList();
        if (list.size() <= n) {
            return empty();
        }
        return new LinqStream<>(list.subList(0, list.size() - n).stream());
    }

    /**
     * 取最后 n 个元素，等价于 C# TakeLast。
     * 需要先收集为 List，此后排序比较器已应用，不再保留。
     */
    public LinqStream<T> takeLast(int n) {
        List<T> list = toList();
        if (list.size() <= n) {
            return new LinqStream<>(list.stream());
        }
        return new LinqStream<>(list.subList(list.size() - n, list.size()).stream());
    }

    /**
     * 跳过元素直到条件不成立，等价于 C# SkipWhile。
     * 需要先收集为 List，此后排序比较器已应用，不再保留。
     */
    public LinqStream<T> skipWhile(@Nonnull Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        List<T> list = toList();
        int i = 0;
        while (i < list.size() && predicate.test(list.get(i))) {
            i++;
        }
        return i == 0 ? new LinqStream<>(list.stream())
                : new LinqStream<>(list.subList(i, list.size()).stream());
    }

    /**
     * 取元素直到条件不成立，等价于 C# TakeWhile。
     * 需要先收集为 List，此后排序比较器已应用，不再保留。
     */
    public LinqStream<T> takeWhile(@Nonnull Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        List<T> list = toList();
        int i = 0;
        while (i < list.size() && predicate.test(list.get(i))) {
            i++;
        }
        return i == list.size() ? new LinqStream<>(list.stream())
                : new LinqStream<>(list.subList(0, i).stream());
    }

    /**
     * 反转顺序，等价于 C# Reverse。
     * 需要先收集为 List（应用排序后再反转），不再保留之前的比较器。
     */
    public LinqStream<T> reverse() {
        List<T> list = toList();
        Collections.reverse(list);
        return new LinqStream<>(list.stream());
    }

    // ==================== 合并 / 连接 ====================

    /**
     * 追加一个元素，等价于 C# Append。保留已有的排序比较器链。
     */
    public LinqStream<T> append(@Nullable T item) {
        return new LinqStream<>(
                Stream.concat(this.stream(), Stream.of(item)),
                this.comparator
        );
    }

    /**
     * 前插一个元素，等价于 C# Prepend。保留已有的排序比较器链。
     */
    public LinqStream<T> prepend(@Nullable T item) {
        return new LinqStream<>(
                Stream.concat(Stream.of(item), this.stream()),
                this.comparator
        );
    }

    /**
     * 连接两个流，等价于 C# Concat。保留已有的排序比较器链。
     */
    public LinqStream<T> concat(@Nonnull Stream<? extends T> other) {
        Objects.requireNonNull(other);
        return new LinqStream<>(
                Stream.concat(this.stream(), other),
                this.comparator
        );
    }

    /**
     * 连接两个 LinqStream
     */
    public LinqStream<T> concat(@Nonnull LinqStream<? extends T> other) {
        Objects.requireNonNull(other);
        return concat(other.stream());
    }

    /**
     * 并集（去重合并），等价于 C# Union。
     * 语义上等价于 concat + distinct，保留已有的排序比较器链。
     */
    public LinqStream<T> union(@Nonnull Stream<? extends T> other) {
        return this.concat(other).distinct();
    }

    /**
     * 交集，等价于 C# Intersect。保留已有的排序比较器链。
     */
    public LinqStream<T> intersect(@Nonnull Stream<? extends T> other) {
        Objects.requireNonNull(other);
        Set<T> otherSet = other.collect(Collectors.toSet());
        return where(otherSet::contains);
    }

    /**
     * 差集（本流有而 other 没有），等价于 C# Except。保留已有的排序比较器链。
     */
    public LinqStream<T> except(@Nonnull Stream<? extends T> other) {
        Objects.requireNonNull(other);
        Set<T> otherSet = other.collect(Collectors.toSet());
        return where(t -> !otherSet.contains(t));
    }

    /**
     * Zip 合并，等价于 C# Zip。
     */
    public static <T1, T2, R> LinqStream<R> zip(
            @Nonnull Stream<? extends T1> first,
            @Nonnull Stream<? extends T2> second,
            @Nonnull BiFunction<? super T1, ? super T2, ? extends R> zipper) {
        Objects.requireNonNull(first);
        Objects.requireNonNull(second);
        Objects.requireNonNull(zipper);
        Iterator<? extends T1> it1 = first.iterator();
        Iterator<? extends T2> it2 = second.iterator();
        List<R> results = new ArrayList<>();
        while (it1.hasNext() && it2.hasNext()) {
            results.add(zipper.apply(it1.next(), it2.next()));
        }
        return new LinqStream<>(results.stream());
    }

    /**
     * 如果流为空则返回包含默认值的流，等价于 C# DefaultIfEmpty。
     * 保留已有的排序比较器链。
     */
    public LinqStream<T> defaultIfEmpty(@Nullable T defaultValue) {
        List<T> list = toList();
        if (list.isEmpty()) {
            return new LinqStream<>(Stream.of(defaultValue));
        }
        return new LinqStream<>(list.stream());
    }

    /**
     * 内连接，等价于 C# Join。
     */
    public <TInner, TKey, TResult> LinqStream<TResult> join(
            @Nonnull Stream<? extends TInner> inner,
            @Nonnull Function<? super T, ? extends TKey> outerKeySelector,
            @Nonnull Function<? super TInner, ? extends TKey> innerKeySelector,
            @Nonnull BiFunction<? super T, ? super TInner, ? extends TResult> resultSelector) {
        Objects.requireNonNull(inner);
        Objects.requireNonNull(outerKeySelector);
        Objects.requireNonNull(innerKeySelector);
        Objects.requireNonNull(resultSelector);
        // join 需要消费本流，同时应用延迟排序
        Map<TKey, List<TInner>> innerLookup = inner.collect(
                Collectors.groupingBy(innerKeySelector));
        List<TResult> results = new ArrayList<>();
        this.sortedStream().forEach(outerItem -> {
            TKey key = outerKeySelector.apply(outerItem);
            List<TInner> innerItems = innerLookup.get(key);
            if (innerItems != null) {
                for (TInner innerItem : innerItems) {
                    results.add(resultSelector.apply(outerItem, innerItem));
                }
            }
        });
        return new LinqStream<>(results.stream());
    }

    /**
     * 分组连接，等价于 C# GroupJoin。
     */
    public <TInner, TKey, TResult> LinqStream<TResult> groupJoin(
            @Nonnull Stream<? extends TInner> inner,
            @Nonnull Function<? super T, ? extends TKey> outerKeySelector,
            @Nonnull Function<? super TInner, ? extends TKey> innerKeySelector,
            @Nonnull BiFunction<? super T, ? super List<TInner>, ? extends TResult> resultSelector) {
        Objects.requireNonNull(inner);
        Objects.requireNonNull(outerKeySelector);
        Objects.requireNonNull(innerKeySelector);
        Objects.requireNonNull(resultSelector);
        Map<TKey, List<TInner>> innerLookup = inner.collect(
                Collectors.groupingBy(innerKeySelector));
        List<TResult> results = new ArrayList<>();
        this.sortedStream().forEach(outerItem -> {
            TKey key = outerKeySelector.apply(outerItem);
            results.add(resultSelector.apply(outerItem,
                    innerLookup.getOrDefault(key, Collections.emptyList())));
        });
        return new LinqStream<>(results.stream());
    }

    // ==================== 判断 / 聚合 ====================

    /**
     * 是否有元素，等价于 C# Any()
     */
    public boolean any() {
        return this.sortedStream().findAny().isPresent();
    }

    /**
     * 是否有满足条件的元素，等价于 C# Any(predicate)
     */
    public boolean any(@Nonnull Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        return this.sortedStream().anyMatch(predicate);
    }

    /**
     * 是否全部满足条件，等价于 C# All
     */
    public boolean all(@Nonnull Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        return this.sortedStream().allMatch(predicate);
    }

    /**
     * 是否全不满足条件（等价于 !Any）
     */
    public boolean none(@Nonnull Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        return this.sortedStream().noneMatch(predicate);
    }

    /**
     * 流是否为空
     */
    public boolean isEmpty() {
        return !any();
    }

    /**
     * 元素个数，等价于 C# Count()
     */
    public long count() {
        return this.sortedStream().count();
    }

    /**
     * 满足条件的元素个数，等价于 C# Count(predicate)
     */
    public long count(@Nonnull Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        return this.sortedStream().filter(predicate).count();
    }

    /**
     * 是否包含指定元素，等价于 C# Contains
     */
    public boolean contains(@Nullable T value) {
        return this.sortedStream().anyMatch(t -> Objects.equals(t, value));
    }

    /**
     * 按比较器判断是否包含指定元素
     */
    public boolean contains(@Nullable T value, @Nonnull Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return this.sortedStream().anyMatch(t -> comparator.compare(t, value) == 0);
    }

    /**
     * 序列是否与另一个序列相等（按元素顺序逐个比较），等价于 C# SequenceEqual
     */
    public boolean sequenceEqual(@Nonnull Stream<? extends T> other) {
        Objects.requireNonNull(other);
        Iterator<T> it1 = this.sortedStream().iterator();
        Iterator<? extends T> it2 = other.iterator();
        while (it1.hasNext() && it2.hasNext()) {
            if (!Objects.equals(it1.next(), it2.next())) {
                return false;
            }
        }
        return !it1.hasNext() && !it2.hasNext();
    }

    // ==================== 数值聚合 ====================

    /**
     * 求和（int），等价于 C# Sum
     */
    public int sumInt(@Nonnull ToIntFunction<? super T> mapper) {
        Objects.requireNonNull(mapper);
        return this.sortedStream().mapToInt(mapper).sum();
    }

    /**
     * 求和（long），等价于 C# Sum
     */
    public long sumLong(@Nonnull ToLongFunction<? super T> mapper) {
        Objects.requireNonNull(mapper);
        return this.sortedStream().mapToLong(mapper).sum();
    }

    /**
     * 求和（double），等价于 C# Sum
     */
    public double sumDouble(@Nonnull ToDoubleFunction<? super T> mapper) {
        Objects.requireNonNull(mapper);
        return this.sortedStream().mapToDouble(mapper).sum();
    }

    /**
     * 求平均值（int），等价于 C# Average
     */
    public double averageInt(@Nonnull ToIntFunction<? super T> mapper) {
        Objects.requireNonNull(mapper);
        return this.sortedStream().mapToInt(mapper).average().orElse(0);
    }

    /**
     * 求平均值（long），等价于 C# Average
     */
    public double averageLong(@Nonnull ToLongFunction<? super T> mapper) {
        Objects.requireNonNull(mapper);
        return this.sortedStream().mapToLong(mapper).average().orElse(0);
    }

    /**
     * 求平均值（double），等价于 C# Average
     */
    public double averageDouble(@Nonnull ToDoubleFunction<? super T> mapper) {
        Objects.requireNonNull(mapper);
        return this.sortedStream().mapToDouble(mapper).average().orElse(0);
    }

    /**
     * 求最小值（按比较器），等价于 C# Min
     */
    @Nullable
    public T min(@Nonnull Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return this.sortedStream().min(comparator).orElse(null);
    }

    /**
     * 求最大值（按比较器），等价于 C# Max
     */
    @Nullable
    public T max(@Nonnull Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return this.sortedStream().max(comparator).orElse(null);
    }

    /**
     * 求最小值（按 key），等价于 C# MinBy
     */
    @Nullable
    public <U extends Comparable<? super U>> T minBy(@Nonnull Function<? super T, ? extends U> keySelector) {
        Objects.requireNonNull(keySelector);
        return min(Comparator.comparing(keySelector));
    }

    /**
     * 求最大值（按 key），等价于 C# MaxBy
     */
    @Nullable
    public <U extends Comparable<? super U>> T maxBy(@Nonnull Function<? super T, ? extends U> keySelector) {
        Objects.requireNonNull(keySelector);
        return max(Comparator.comparing(keySelector));
    }

    // ==================== 聚合归约 ====================

    /**
     * 聚合（无初始值），等价于 C# Aggregate
     */
    @Nullable
    public T aggregate(@Nonnull BinaryOperator<T> accumulator) {
        Objects.requireNonNull(accumulator);
        return this.sortedStream().reduce(accumulator).orElse(null);
    }

    /**
     * 聚合（带初始值），等价于 C# Aggregate
     */
    public <R> R aggregate(@Nullable R seed, @Nonnull BiFunction<R, ? super T, R> accumulator) {
        Objects.requireNonNull(accumulator);
        R result = seed;
        for (T item : toList()) {
            result = accumulator.apply(result, item);
        }
        return result;
    }

    /**
     * 聚合（带初始值和结果转换），等价于 C# Aggregate
     */
    @Nonnull
    public <R, TResult> TResult aggregate(
            @Nullable R seed,
            @Nonnull BiFunction<R, ? super T, R> accumulator,
            @Nonnull Function<R, TResult> resultSelector) {
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(resultSelector);
        return resultSelector.apply(aggregate(seed, accumulator));
    }

    // ==================== 收集 ====================

    /**
     * 收集为 List，等价于 C# ToList。
     * 在此之前设置的 orderBy/thenBy 排序会在此刻执行。
     */
    @Nonnull
    public List<T> toList() {
        return this.sortedStream().collect(Collectors.toList());
    }

    /**
     * 收集为不可变 List
     */
    @Nonnull
    public List<T> toUnmodifiableList() {
        return this.sortedStream().collect(Collectors.toUnmodifiableList());
    }

    /**
     * 收集为 Set，等价于 C# ToHashSet
     */
    @Nonnull
    public Set<T> toSet() {
        return this.sortedStream().collect(Collectors.toSet());
    }

    /**
     * 收集为不可变 Set
     */
    @Nonnull
    public Set<T> toUnmodifiableSet() {
        return this.sortedStream().collect(Collectors.toUnmodifiableSet());
    }

    /**
     * 收集为数组，等价于 C# ToArray
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public T[] toArray() {
        return (T[]) this.sortedStream().toArray();
    }

    /**
     * 收集为指定类型数组
     */
    @Nonnull
    public T[] toArray(@Nonnull IntFunction<T[]> generator) {
        Objects.requireNonNull(generator);
        return this.sortedStream().toArray(generator);
    }

    /**
     * 收集为 Map（重复 key 保留后者），等价于 C# ToDictionary
     */
    @Nonnull
    public <K, V> Map<K, V> toMap(
            @Nonnull Function<? super T, ? extends K> keyMapper,
            @Nonnull Function<? super T, ? extends V> valueMapper) {
        Objects.requireNonNull(keyMapper);
        Objects.requireNonNull(valueMapper);
        return this.sortedStream().collect(Collectors.toMap(
                keyMapper, valueMapper, (v1, v2) -> v2));
    }

    /**
     * 收集为 Map（自定义合并策略），等价于 C# ToDictionary
     */
    @Nonnull
    public <K, V> Map<K, V> toMap(
            @Nonnull Function<? super T, ? extends K> keyMapper,
            @Nonnull Function<? super T, ? extends V> valueMapper,
            @Nonnull BinaryOperator<V> mergeFunction) {
        Objects.requireNonNull(keyMapper);
        Objects.requireNonNull(valueMapper);
        Objects.requireNonNull(mergeFunction);
        return this.sortedStream().collect(Collectors.toMap(
                keyMapper, valueMapper, mergeFunction));
    }

    /**
     * 分组，等价于 C# GroupBy
     */
    @Nonnull
    public <K> Map<K, List<T>> groupBy(@Nonnull Function<? super T, ? extends K> keyMapper) {
        Objects.requireNonNull(keyMapper);
        return this.sortedStream().collect(Collectors.groupingBy(keyMapper));
    }

    /**
     * 分组并转换值，等价于 C# GroupBy
     */
    @Nonnull
    public <K, V> Map<K, List<V>> groupBy(
            @Nonnull Function<? super T, ? extends K> keyMapper,
            @Nonnull Function<? super T, ? extends V> valueMapper) {
        Objects.requireNonNull(keyMapper);
        Objects.requireNonNull(valueMapper);
        return this.sortedStream().collect(Collectors.groupingBy(
                keyMapper, Collectors.mapping(valueMapper, Collectors.toList())));
    }

    /**
     * 遍历每个元素，等价于 C# ForEach（List 上的）
     */
    public void forEach(@Nonnull Consumer<? super T> action) {
        Objects.requireNonNull(action);
        this.sortedStream().forEach(action);
    }

    /**
     * 按顺序遍历每个元素
     */
    public void forEachOrdered(@Nonnull Consumer<? super T> action) {
        Objects.requireNonNull(action);
        this.sortedStream().forEachOrdered(action);
    }

    /**
     * 分页，等价于组合使用 Skip/Take。保留已有的排序比较器链。
     *
     * @param pageNum  页码（从1开始）
     * @param pageSize 每页大小
     */
    public LinqStream<T> page(int pageNum, int pageSize) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 1;
        return skip((long) (pageNum - 1) * pageSize).take(pageSize);
    }
}
