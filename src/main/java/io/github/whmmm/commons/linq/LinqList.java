package io.github.whmmm.commons.linq;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * LINQ 风格集合包装器，参考 C# LINQ API 设计。
 * 包装 {@link Collection}，提供流式查询和常用操作方法。
 *
 * @param <T> 元素类型
 */
public class LinqList<T> implements Serializable {
    private final Collection<T> collection;

    private LinqList(Collection<T> collection) {
        Collection<T> coll = collection;
        if (coll == null) {
            coll = new ArrayList<>();
        }
        this.collection = coll;
    }

    /**
     * 将 Collection 包装为 LinqList
     */
    public static <T> LinqList<T> toLinq(Collection<T> collection) {
        return new LinqList<>(collection);
    }

    /**
     * 从可变参数创建 LinqList
     */
    @SafeVarargs
    public static <T> LinqList<T> of(T... values) {
        return new LinqList<>(Arrays.asList(values));
    }

    /**
     * 创建空的 LinqList
     */
    public static <T> LinqList<T> empty() {
        return new LinqList<>(Collections.emptyList());
    }

    /**
     * 获取底层 Collection
     */
    public Collection<T> collection() {
        return collection;
    }

    // ==================== Stream 转换 ====================

    /**
     * 转为 LinqStream
     */
    public LinqStream<T> stream() {
        return new LinqStream<>(collection.stream());
    }

    /**
     * 转为过滤 null 后的 LinqStream
     */
    public LinqStream<T> nonnullStream() {
        return new LinqStream<>(collection.stream().filter(Objects::nonNull));
    }

    // ==================== 元素获取 ====================

    /**
     * 返回第一个元素，没有则返回 null
     */
    @Nullable
    public T firstOrDefault() {
        return this.nonnullStream().firstOrDefault();
    }

    /**
     * 返回第一个满足条件的元素，没有则返回 null
     */
    @Nullable
    public T firstOrDefault(@Nonnull Predicate<? super T> predicate) {
        return this.nonnullStream().firstOrDefault(predicate);
    }

    /**
     * 返回第一个元素，为空则抛异常
     */
    @Nonnull
    public T first() {
        return this.nonnullStream().first();
    }

    /**
     * 返回第一个满足条件的元素，没有则抛异常
     */
    @Nonnull
    public T first(@Nonnull Predicate<? super T> predicate) {
        return this.nonnullStream().first(predicate);
    }

    /**
     * 返回最后一个元素，没有则返回 null
     */
    @Nullable
    public T lastOrDefault() {
        return this.nonnullStream().lastOrDefault();
    }

    /**
     * 返回最后一个满足条件的元素，没有则返回 null
     */
    @Nullable
    public T lastOrDefault(@Nonnull Predicate<? super T> predicate) {
        return this.nonnullStream().lastOrDefault(predicate);
    }

    /**
     * 返回最后一个元素，为空则抛异常
     */
    @Nonnull
    public T last() {
        return this.nonnullStream().last();
    }

    /**
     * 返回唯一的元素（0个或1个）
     */
    @Nullable
    public T singleOrDefault() {
        return this.nonnullStream().singleOrDefault();
    }

    /**
     * 返回唯一的元素，没有则返回默认值
     */
    @Nullable
    public T singleOrDefault(@Nullable T defaultValue) {
        return this.nonnullStream().singleOrDefault(defaultValue);
    }

    /**
     * 返回唯一的元素
     */
    @Nonnull
    public T single() {
        return this.nonnullStream().single();
    }

    /**
     * 返回指定索引的元素，越界返回 null
     */
    @Nullable
    public T elementAtOrDefault(int index) {
        return this.nonnullStream().elementAtOrDefault(index);
    }

    /**
     * 返回指定索引的元素，越界返回默认值
     */
    @Nullable
    public T elementAtOrDefault(int index, @Nullable T defaultValue) {
        return this.nonnullStream().elementAtOrDefault(index, defaultValue);
    }

    /**
     * 返回指定索引的元素，越界抛异常
     */
    @Nonnull
    public T elementAt(int index) {
        return this.nonnullStream().elementAt(index);
    }

    // ==================== 过滤 / 投影（返回 LinqStream） ====================

    /**
     * 过滤
     */
    public LinqStream<T> where(@Nonnull Predicate<? super T> predicate) {
        return this.nonnullStream().where(predicate);
    }

    /**
     * 投影
     */
    public <R> LinqStream<R> select(@Nonnull Function<? super T, ? extends R> mapper) {
        return this.nonnullStream().select(mapper);
    }

    /**
     * 扁平映射
     */
    public <R> LinqStream<R> selectMany(@Nonnull Function<? super T, ? extends Stream<? extends R>> mapper) {
        return this.nonnullStream().selectMany(mapper);
    }

    /**
     * 按类型过滤并强转
     */
    public <R> LinqStream<R> ofType(@Nonnull Class<R> type) {
        return this.nonnullStream().ofType(type);
    }

    // ==================== 判断 / 聚合 ====================

    /**
     * 是否有元素
     */
    public boolean any() {
        return this.nonnullStream().any();
    }

    /**
     * 是否有满足条件的元素
     */
    public boolean any(@Nonnull Predicate<? super T> predicate) {
        return this.nonnullStream().any(predicate);
    }

    /**
     * 是否全部满足条件
     */
    public boolean all(@Nonnull Predicate<? super T> predicate) {
        return this.nonnullStream().all(predicate);
    }

    /**
     * 是否全不满足条件
     */
    public boolean none(@Nonnull Predicate<? super T> predicate) {
        return this.nonnullStream().none(predicate);
    }

    /**
     * 是否为空
     */
    public boolean isEmpty() {
        return collection.isEmpty();
    }

    /**
     * 非 null 元素数量
     */
    public int count() {
        return (int) this.nonnullStream().count();
    }

    /**
     * 满足条件的非 null 元素数量
     */
    public int count(@Nonnull Predicate<? super T> predicate) {
        return (int) this.nonnullStream().count(predicate);
    }

    /**
     * 原始集合大小（含 null）
     */
    public int size() {
        return collection.size();
    }

    /**
     * 是否包含指定元素
     */
    public boolean contains(@Nullable T value) {
        return this.nonnullStream().contains(value);
    }

    // ==================== 聚合归约 ====================

    /**
     * 聚合（无初始值）
     */
    @Nullable
    public T aggregate(@Nonnull BinaryOperator<T> accumulator) {
        return this.nonnullStream().aggregate(accumulator);
    }

    /**
     * 聚合（带初始值）
     */
    public <R> R aggregate(@Nullable R seed, @Nonnull BiFunction<R, ? super T, R> accumulator) {
        return this.nonnullStream().aggregate(seed, accumulator);
    }

    // ==================== 排序 ====================

    /**
     * 升序排序后返回 LinqStream
     */
    public <U extends Comparable<? super U>> LinqStream<T> orderBy(
            @Nonnull Function<? super T, ? extends U> keySelector) {
        return this.nonnullStream().orderBy(keySelector);
    }

    /**
     * 降序排序后返回 LinqStream
     */
    public <U extends Comparable<? super U>> LinqStream<T> orderByDescending(
            @Nonnull Function<? super T, ? extends U> keySelector) {
        return this.nonnullStream().orderByDescending(keySelector);
    }

    // ==================== 收集 ====================

    /**
     * 收集非 null 元素为 List
     */
    @Nonnull
    public List<T> toList() {
        return this.nonnullStream().toList();
    }

    /**
     * 收集非 null 元素为 Set
     */
    @Nonnull
    public Set<T> toSet() {
        return this.nonnullStream().toSet();
    }

    /**
     * 收集非 null 元素为数组
     */
    @Nonnull
    public T[] toArray() {
        return this.nonnullStream().toArray();
    }

    /**
     * 收集为指定类型数组
     */
    @Nonnull
    public T[] toArray(@Nonnull IntFunction<T[]> generator) {
        return this.nonnullStream().toArray(generator);
    }

    /**
     * 收集为 Map（重复 key 保留后者）
     */
    @Nonnull
    public <K, V> Map<K, V> toMap(
            @Nonnull Function<? super T, ? extends K> keyMapper,
            @Nonnull Function<? super T, ? extends V> valueMapper) {
        return this.nonnullStream().toMap(keyMapper, valueMapper);
    }

    /**
     * 分组
     */
    @Nonnull
    public <K> Map<K, List<T>> groupBy(@Nonnull Function<? super T, ? extends K> keyMapper) {
        return this.nonnullStream().groupBy(keyMapper);
    }

    /**
     * 遍历非 null 元素
     */
    public void forEach(@Nonnull Consumer<? super T> action) {
        this.nonnullStream().forEach(action);
    }

    // ==================== 数值聚合 ====================

    /**
     * 求和（int）
     */
    public int sumInt(@Nonnull ToIntFunction<? super T> mapper) {
        return this.nonnullStream().sumInt(mapper);
    }

    /**
     * 求和（long）
     */
    public long sumLong(@Nonnull ToLongFunction<? super T> mapper) {
        return this.nonnullStream().sumLong(mapper);
    }

    /**
     * 求和（double）
     */
    public double sumDouble(@Nonnull ToDoubleFunction<? super T> mapper) {
        return this.nonnullStream().sumDouble(mapper);
    }

    /**
     * 求平均值（int）
     */
    public double averageInt(@Nonnull ToIntFunction<? super T> mapper) {
        return this.nonnullStream().averageInt(mapper);
    }

    /**
     * 求平均值（long）
     */
    public double averageLong(@Nonnull ToLongFunction<? super T> mapper) {
        return this.nonnullStream().averageLong(mapper);
    }

    /**
     * 求平均值（double）
     */
    public double averageDouble(@Nonnull ToDoubleFunction<? super T> mapper) {
        return this.nonnullStream().averageDouble(mapper);
    }

    /**
     * 求最小值（按比较器）
     */
    @Nullable
    public T min(@Nonnull Comparator<? super T> comparator) {
        return this.nonnullStream().min(comparator);
    }

    /**
     * 求最大值（按比较器）
     */
    @Nullable
    public T max(@Nonnull Comparator<? super T> comparator) {
        return this.nonnullStream().max(comparator);
    }

    /**
     * 求最小值（按 key）
     */
    @Nullable
    public <U extends Comparable<? super U>> T minBy(@Nonnull Function<? super T, ? extends U> keySelector) {
        return this.nonnullStream().minBy(keySelector);
    }

    /**
     * 求最大值（按 key）
     */
    @Nullable
    public <U extends Comparable<? super U>> T maxBy(@Nonnull Function<? super T, ? extends U> keySelector) {
        return this.nonnullStream().maxBy(keySelector);
    }
}
