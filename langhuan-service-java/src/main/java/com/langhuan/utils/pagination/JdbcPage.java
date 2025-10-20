package com.langhuan.utils.pagination;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;

/**
 * JdbcTemplate分页查询结果类
 * 与MyBatis Plus IPage接口兼容，保证前端无影响
 *
 * @param <T> 数据类型
 * @author system
 */
@Data
@Accessors(chain = true)
public class JdbcPage<T> implements IPage<T> {

    /**
     * 当前页码
     */
    private long current = 1;

    /**
     * 每页显示条数
     */
    private long size = 10;

    /**
     * 总记录数
     */
    private long total = 0;

    /**
     * 查询数据列表
     */
    private List<T> records = Collections.emptyList();

    /**
     * 排序字段信息
     */
    private List<OrderItem> orders = Collections.emptyList();

    /**
     * 是否进行 count 查询
     */
    private boolean searchCount = true;

    /**
     * 是否命中count缓存
     */
    private boolean hitCount = false;

    /**
     * 是否优化 COUNT SQL
     */
    private boolean optimizeCountSql = true;

    /**
     * 是否自动优化 COUNT SQL
     */
    private boolean isSearchCount = true;

    /**
     * 构造函数
     */
    public JdbcPage() {
    }

    /**
     * 构造函数
     *
     * @param current 当前页
     * @param size    每页显示条数
     */
    public JdbcPage(long current, long size) {
        this(current, size, 0);
    }

    /**
     * 构造函数
     *
     * @param current 当前页
     * @param size    每页显示条数
     * @param total   总记录数
     */
    public JdbcPage(long current, long size, long total) {
        this(current, size, total, true);
    }

    /**
     * 构造函数
     *
     * @param current     当前页
     * @param size        每页显示条数
     * @param searchCount 是否进行count查询
     */
    public JdbcPage(long current, long size, boolean searchCount) {
        this(current, size, 0, searchCount);
    }

    /**
     * 构造函数
     *
     * @param current     当前页
     * @param size        每页显示条数
     * @param total       总记录数
     * @param searchCount 是否进行count查询
     */
    public JdbcPage(long current, long size, long total, boolean searchCount) {
        if (current > 1) {
            this.current = current;
        }
        this.size = size;
        this.total = total;
        this.searchCount = searchCount;
    }

    /**
     * 获取总页数
     *
     * @return 总页数
     */
    @Override
    public long getPages() {
        if (getSize() == 0) {
            return 0L;
        }
        long pages = getTotal() / getSize();
        if (getTotal() % getSize() != 0) {
            pages++;
        }
        return pages;
    }

    /**
     * 是否存在上一页
     *
     * @return true / false
     */
    public boolean hasPrevious() {
        return this.current > 1;
    }

    /**
     * 是否存在下一页
     *
     * @return true / false
     */
    public boolean hasNext() {
        return this.current < this.getPages();
    }

    /**
     * 获取排序信息
     *
     * @return 排序信息列表
     */
    @Override
    public List<OrderItem> orders() {
        return this.orders;
    }

    /**
     * 获取偏移量
     *
     * @return 偏移量
     */
    public long offset() {
        return (getCurrent() - 1) * getSize();
    }

    /**
     * 获取限制数量
     *
     * @return 限制数量
     */
    public long limit() {
        return getSize();
    }

    /**
     * 设置是否命中count缓存
     *
     * @param hit 是否命中
     * @return 当前对象
     */
    public IPage<T> setHitCount(boolean hit) {
        this.hitCount = hit;
        return this;
    }

    /**
     * 设置是否优化COUNT SQL
     *
     * @param optimize 是否优化
     * @return 当前对象
     */
    public IPage<T> setOptimizeCountSql(boolean optimize) {
        this.optimizeCountSql = optimize;
        return this;
    }

    /**
     * 设置是否进行count查询
     *
     * @param isSearchCount 是否进行count查询
     * @return 当前对象
     */
    public IPage<T> setSearchCount(boolean isSearchCount) {
        this.isSearchCount = isSearchCount;
        return this;
    }

    /**
     * 获取计算的COUNT值
     *
     * @return count值
     */
    public long getCountId() {
        return this.total;
    }

    /**
     * 获取descs
     *
     * @return descs
     */
    public String[] descs() {
        return orders.stream()
                .filter(item -> !item.isAsc())
                .map(OrderItem::getColumn)
                .toArray(String[]::new);
    }

    /**
     * 获取ascs
     *
     * @return ascs
     */
    public String[] ascs() {
        return orders.stream()
                .filter(OrderItem::isAsc)
                .map(OrderItem::getColumn)
                .toArray(String[]::new);
    }
} 