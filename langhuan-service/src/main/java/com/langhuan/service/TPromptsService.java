package com.langhuan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.langhuan.model.domain.TPrompts;
import com.langhuan.model.mapper.TPromptsMapper;
import com.langhuan.common.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lishuangqi
 * @description 针对表【t_prompts】的数据库操作Service实现
 * @createDate 2025-03-05 09:40:43
 */
@Service
@Slf4j
public class TPromptsService extends ServiceImpl<TPromptsMapper, TPrompts> {
    // 使用线程安全的来存储缓存的数据
    private static final Map<String, String> cachedPrompts = new ConcurrentHashMap<>();

    @Scheduled(fixedRate = Constant.AIDEFAULTSYSTEMPROMPTRECTIME)
    public void cacheTPrompts() {
        List<TPrompts> prompts = super.list();
        Map<String, String> newCache = new ConcurrentHashMap<>();
        for (TPrompts prompt : prompts) {
            newCache.put(prompt.getMethodName(), prompt.getContent());
        }
        synchronized (cachedPrompts) {
            cachedPrompts.clear();
            cachedPrompts.putAll(newCache);
        }
        log.info("Cached t_prompts table data at " + new java.util.Date());
    }

    public static String getCachedTPromptsByMethodName(String method_name) {
        String content = cachedPrompts.get(method_name);
        if (content == null || content.isEmpty()) {
            log.error("No valid content found for method name {}", method_name);
            return Constant.AINULLDEFAULTSYSTEMPROMPT;
        }
        return content;
    }

    // 新增
    public boolean saveTPrompts(TPrompts tPrompts) {
        return super.save(tPrompts);
    }

    // 删除
    public boolean removeTPromptsById(Integer id) {
        return super.removeById(id);
    }

    // 修改
    public boolean updateTPrompts(TPrompts tPrompts) {
        return super.updateById(tPrompts);
    }


    // 分页查询并支持模糊搜索
    public IPage<TPrompts> getTPromptsByPage(int pageNum, int pageSize, String methodName, String category, String description) {

        return super.page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<TPrompts>()
                        .like(!methodName.isEmpty(), TPrompts::getMethodName, methodName)
                        .like(!category.isEmpty(), TPrompts::getCategory, category)
                        .like(!description.isEmpty(), TPrompts::getDescription, description)
        );
    }
}




