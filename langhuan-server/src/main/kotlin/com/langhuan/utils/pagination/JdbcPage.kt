package com.langhuan.utils.pagination

import com.baomidou.mybatisplus.core.metadata.IPage
import com.baomidou.mybatisplus.core.metadata.OrderItem
import kotlin.jvm.JvmName

/**
 * JdbcTemplate分页查询结果类
 * 与MyBatis Plus IPage接口兼容，保证前端无影响
 *
 * @param <T> 数据类型
 * @author system
 */
class JdbcPage<T> : IPage<T> {

    /**
     * 当前页码
     */
    @get:JvmName("getCurrentPage")
    var current: Long = 1

    /**
     * 每页显示条数
     */
    @get:JvmName("getPageSize")
    var size: Long = 10

    /**
     * 总记录数
     */
    @get:JvmName("getTotalCount")
    var total: Long = 0

    /**
     * 查询数据列表
     */
    @get:JvmName("getPageRecords")
    var records: List<T> = emptyList()

    /**
     * 排序字段信息
     */
    var orders: List<OrderItem> = emptyList()

    /**
     * 是否进行 count 查询
     */
    @get:JvmName("getSearchCountFlag")
    var searchCount: Boolean = true

    /**
     * 是否命中count缓存
     */
    var hitCount: Boolean = false

    /**
     * 是否优化 COUNT SQL
     */
    var optimizeCountSql: Boolean = true

    /**
     * 是否自动优化 COUNT SQL
     */
    @get:JvmName("getIsSearchCountFlag")
    var autoSearchCount: Boolean = true

    /**
     * 构造函数
     */
    constructor()

    /**
     * 构造函数
     *
     * @param current 当前页
     * @param size    每页显示条数
     */
    constructor(current: Long, size: Long) : this(current, size, 0)

    /**
     * 构造函数
     *
     * @param current 当前页
     * @param size    每页显示条数
     * @param total   总记录数
     */
    constructor(current: Long, size: Long, total: Long) : this(current, size, total, true)

    /**
     * 构造函数
     *
     * @param current     当前页
     * @param size        每页显示条数
     * @param searchCount 是否进行count查询
     */
    constructor(current: Long, size: Long, searchCount: Boolean) : this(current, size, 0, searchCount)

    /**
     * 构造函数
     *
     * @param current     当前页
     * @param size        每页显示条数
     * @param total       总记录数
     * @param searchCount 是否进行count查询
     */
    constructor(current: Long, size: Long, total: Long, searchCount: Boolean) {
        if (current > 1) {
            this.current = current
        }
        this.size = size
        this.total = total
        this.searchCount = searchCount
    }

    /**
     * 获取总页数
     *
     * @return 总页数
     */
    override fun getPages(): Long {
        if (size == 0L) {
            return 0L
        }
        var pages = total / size
        if (total % size != 0L) {
            pages++
        }
        return pages
    }

    /**
     * 获取查询数据列表
     *
     * @return 查询数据列表
     */
    override fun getRecords(): List<T> = records

    /**
     * 获取总记录数
     *
     * @return 总记录数
     */
    override fun getTotal(): Long = total

    /**
     * 获取每页显示条数
     *
     * @return 每页显示条数
     */
    override fun getSize(): Long = size

    /**
     * 获取当前页码
     *
     * @return 当前页码
     */
    override fun getCurrent(): Long = current

    /**
     * 设置查询数据列表
     *
     * @param records 查询数据列表
     * @return 当前对象
     */
    override fun setRecords(records: List<T>): IPage<T> {
        this.records = records
        return this
    }

    /**
     * 设置总记录数
     *
     * @param total 总记录数
     * @return 当前对象
     */
    override fun setTotal(total: Long): IPage<T> {
        this.total = total
        return this
    }

    /**
     * 设置每页显示条数
     *
     * @param size 每页显示条数
     * @return 当前对象
     */
    override fun setSize(size: Long): IPage<T> {
        this.size = size
        return this
    }

    /**
     * 设置当前页码
     *
     * @param current 当前页码
     * @return 当前对象
     */
    override fun setCurrent(current: Long): IPage<T> {
        this.current = current
        return this
    }

    /**
     * 是否存在上一页
     *
     * @return true / false
     */
    fun hasPrevious(): Boolean {
        return current > 1
    }

    /**
     * 是否存在下一页
     *
     * @return true / false
     */
    fun hasNext(): Boolean {
        return current < getPages()
    }

    /**
     * 获取排序信息
     *
     * @return 排序信息列表
     */
    override fun orders(): List<OrderItem> {
        return orders
    }

    /**
     * 获取偏移量
     *
     * @return 偏移量
     */
    override fun offset(): Long {
        return (current - 1) * size
    }

    /**
     * 获取限制数量
     *
     * @return 限制数量
     */
    fun limit(): Long {
        return size
    }

    /**
     * 设置是否命中count缓存
     *
     * @param hit 是否命中
     * @return 当前对象
     */
    fun setHitCount(hit: Boolean): IPage<T> {
        this.hitCount = hit
        return this
    }

    /**
     * 设置是否优化COUNT SQL
     *
     * @param optimize 是否优化
     * @return 当前对象
     */
    fun setOptimizeCountSql(optimize: Boolean): IPage<T> {
        this.optimizeCountSql = optimize
        return this
    }

    /**
     * 设置是否进行count查询
     *
     * @param isSearchCount 是否进行count查询
     * @return 当前对象
     */
    fun setSearchCount(isSearchCount: Boolean): IPage<T> {
        this.autoSearchCount = isSearchCount
        return this
    }

    /**
     * 获取计算的COUNT值
     *
     * @return count值
     */
    fun getCountId(): Long {
        return this.total
    }

    /**
     * 获取descs
     *
     * @return descs
     */
    fun descs(): Array<String> {
        return orders
                .filter { !it.isAsc }
                .map { it.column }
                .toTypedArray()
    }

    /**
     * 获取ascs
     *
     * @return ascs
     */
    fun ascs(): Array<String> {
        return orders
                .filter { it.isAsc }
                .map { it.column }
                .toTypedArray()
    }
}
