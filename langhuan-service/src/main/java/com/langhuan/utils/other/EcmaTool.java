package com.langhuan.utils.other;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EcmaTool {
    /**
     * 使用给定的占位符映射替换模板字符串中的所有占位符。
     *
     * @param template     模板字符串，其中包含占位符（形如"${placeholder}"）。
     * @param placeholders 占位符和其对应值的映射。
     * @return 替换占位符后的字符串。
     * @throws IllegalArgumentException 如果模板或占位符映射为null，或占位符格式不正确。
     */
    public static String JtemplateString(String template, Map<String, Object> placeholders) {
        // 检查模板和占位符映射是否为null
        if (template == null || placeholders == null) {
            throw new IllegalArgumentException("Template and placeholders cannot be null");
        }

        // 使用正则表达式匹配模板中的所有占位符
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(template);

        StringBuilder result = new StringBuilder();
        int lastIndex = 0;

        // 遍历所有匹配的占位符，并用对应的值替换它们
        while (matcher.find()) {
            // 将模板中匹配占位符之前的文本添加到结果中
            if (lastIndex < matcher.start()) {
                result.append(template.substring(lastIndex, matcher.start()));
            }

            // 获取占位符名称，并检查其是否为空
            String placeholderName = matcher.group(1);
            if (placeholderName == null || placeholderName.isEmpty()) {
                throw new IllegalArgumentException("Invalid placeholder format: ${}");
            }

            // 获取占位符的值，如果不存在则默认为空字符串
            Object placeholderValue = placeholders.get(placeholderName);
            if (placeholderValue == null) {
                placeholderValue = "";
            }
            // 将占位符的值添加到结果中
            result.append(placeholderValue);
            lastIndex = matcher.end();
        }

        // 将模板中最后一个占位符之后的文本添加到结果中
        if (lastIndex < template.length()) {
            result.append(template.substring(lastIndex));
        }

        return result.toString();
    }

    /**
     * 使用给定的模板字符串和占位符映射生成带占位符的SQL字符串。
     *
     * @param template     包含占位符的模板字符串，占位符格式为"${placeholderName}"。
     * @param placeholders 占位符和其对应值的映射。
     * @return 包含两个元素的映射，其中一个元素是生成的SQL字符串，另一个是占位符值的列表。sql,val
     * SQL字符串中的占位符被替换为"?"，对应的值通过列表维护。
     * @throws IllegalArgumentException 如果模板或占位符映射为null。
     */
    public static Map<String, Object> JtemplateStringSql(String template, Map<String, Object> placeholders) {
        // 校验模板和占位符不为null
        if (template == null || placeholders == null) {
            throw new IllegalArgumentException("Template and placeholders cannot be null");
        }

        // 编译模板匹配模式
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(template);
        StringBuilder sqlString = new StringBuilder();
        List<Object> values = new ArrayList<>();

        int lastIndex = 0;
        // 查找并替换所有占位符
        while (matcher.find()) {
            // 将模板中非占位符部分添加到SQL字符串
            sqlString.append(template.substring(lastIndex, matcher.start()));
            String placeholderName = matcher.group(1);
            Object placeholderValue = placeholders.get(placeholderName);
            if (placeholderValue == null) {
                placeholderValue = "";
                // 如果占位符没有对应的值，则默认为空字符串。可根据需要在此处记录警告或抛出具体异常
            }

            // 将占位符替换为"?"，并将值添加到列表中
            sqlString.append("?");
            values.add(placeholderValue);

            lastIndex = matcher.end();
        }
        // 将模板中剩余部分添加到SQL字符串
        if (lastIndex < template.length()) {
            sqlString.append(template.substring(lastIndex));
        }
        System.out.println(values);
        // 返回包含SQL字符串和值列表的映射
        return new HashMap<String, Object>() {
            {
                put("sql", sqlString.toString());
                put("val", values.toArray());
            }
        };
    }

    /**
     * 将原始列表分割成指定大小的批次。
     *
     * @param originalList 原始列表，不可为null。
     * @param batchSize    批次大小，必须大于0。
     * @return 分割后的批次列表，每个批次的大小不超过指定的批次大小。
     * @throws IllegalArgumentException 如果原始列表为null或批次大小不大于0，则抛出此异常。
     */
    public static <T> List<List<T>> splitIntoBatches(List<T> originalList, int batchSize) {
        if (originalList == null) {
            throw new IllegalArgumentException("原始列表不能为null");
        }

        if (batchSize <= 0) {
            throw new IllegalArgumentException("批次大小必须大于0");
        }

        List<List<T>> batches = new ArrayList<>();
        List<T> currentBatch = new ArrayList<>();

        Iterator<T> iterator = originalList.iterator(); // 使用迭代器遍历原始列表以优化性能

        // 循环遍历列表，将元素添加到当前批次中，当当前批次达到指定大小或遍历完毕时，将当前批次添加到结果批次列表中
        while (iterator.hasNext()) {
            currentBatch.add(iterator.next());
            if (currentBatch.size() == batchSize || !iterator.hasNext()) {
                batches.add(currentBatch);
                currentBatch = new ArrayList<>();
            }
        }
        return batches;
    }

    /**
     * 模拟js的includes方法，检查给定数组是否包含指定的元素。
     *
     * @param array 用于检查的数组，其元素类型为泛型 T。
     * @param val   需要查找的元素，其类型与数组元素类型相同。
     * @return 如果数组中包含指定元素，则返回 true；否则返回 false。
     */
    public static <T> boolean Jincludes(T[] array, T val) {
        // 使用 HashSet 存储数组元素，以实现快速查找
        Set<T> set = new HashSet<>();
        // 将数组元素添加到 HashSet 中
        Collections.addAll(set, array);
        // 检查 HashSet 是否包含指定元素
        return set.contains(val);
    }

    /**
     * 模拟js的some方法，判断给定的列表中是否存在至少一个元素满足指定的条件。
     *
     * @param list 要进行判断的列表，不能为空。
     * @param fun  用于判断列表元素是否满足条件的谓词函数，不能为空。
     * @return 如果列表中存在至少一个元素满足条件，则返回true；否则返回false。
     * @throws IllegalArgumentException 如果列表或谓词函数为null，抛出此异常。
     */
    public static <T> boolean Jsome(List<T> list, Predicate<T> fun) {
        // 检查列表和函数参数是否为null
        if (list == null || fun == null) {
            throw new IllegalArgumentException("List and Function must not be null");
        }
        // 使用流对列表进行遍历，并判断是否有元素满足给定的谓词条件
        return list.stream().anyMatch(fun);
    }

    /**
     * 模拟js的every方法，对给定的列表中的所有元素应用指定的谓词函数，并检查是否所有元素都满足该谓词。
     *
     * @param list 要检查的列表，不能为空。
     * @param fun  要应用的谓词函数，不能为空。
     * @return 如果列表中的所有元素都满足谓词函数，则返回true；否则返回false。
     * @throws IllegalArgumentException 如果列表或谓词函数为null，抛出此异常。
     */
    public static <T> boolean Jevery(List<T> list, Predicate<T> fun) {
        // 检查输入的列表和谓词函数是否为null
        if (list == null || fun == null) {
            throw new IllegalArgumentException("List and Function must not be null");
        }
        // 使用流的allMatch方法检查所有元素是否都满足谓词函数
        return list.stream().allMatch(fun);
    }

    /**
     * 模拟js的find方法，在给定的列表中，使用提供的断言函数查找第一个匹配的元素。
     *
     * @param list 要搜索的列表，不能为空。
     * @param fun  用于断言元素是否匹配的函数，不能为空。
     * @return 找到的第一个匹配元素，如果没有找到则返回null。
     * @throws IllegalArgumentException 如果列表或断言函数为null，抛出此异常。
     */
    public static <T> T Jfind(List<T> list, Predicate<T> fun) {
        // 检查列表和断言函数是否为null
        if (list == null || fun == null) {
            throw new IllegalArgumentException("List and Function must not be null");
        }

        try {
            // 使用提供的断言函数过滤列表，并尝试获取第一个匹配的元素
            return list.stream().filter(fun).findFirst().orElse(null);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // 捕获并处理可能的异常，返回null
            return null;
        }
    }


    /**
     * 模拟js的Object.entries方法，将Map中的键值对转换为包含键和值的List集合的列表。
     *
     * @param map 要转换的Map对象。如果map为null，则返回一个空的List列表。
     * @return 返回一个List列表，其中每个元素都是一个包含键和值的List。
     */
    public static <K, V> List<List<Object>> Jentries(Map<K, V> map) {
        // 当输入的map为null时，直接返回一个空的ArrayList
        if (map == null) {
            return new ArrayList<>();
        }
        // 通过stream方式处理map的entrySet，将每个entry转换为包含key和value的ArrayList
        return map.entrySet().stream()
                .map(entry -> new ArrayList<Object>() {{
                    add(entry.getKey());
                    add(entry.getValue());
                }})
                // 将转换后的ArrayList收集到一个新的List中并返回
                .collect(Collectors.toList());
    }


    public static void main(String[] args) {
//        Map<String, Object> map1 = new HashMap<>();
//        map1.put("name", "Alice");
//        map1.put("age", 25);
//        Map<String, Object> result = JtemplateStringSql("select td.* from T_REPORT_CASEBASE_PINNEDDIRECTORIES d\n" +
//                "join T_REPORT_CASEBASE_DIRECTORIES td\n" +
//                "on d.DIRECTORYID = td.DIRECTORYID where d.USERID = ${user}", new HashMap<String, Object>() {{
//            put("user", "12");
//        }});
//        System.out.println(result.get("sql"));
//        System.out.println(result.get("val"));

        String path = "/home/tcoamp-fj-yd/apache-tomcat-8.0.53/webapps/tcoamp-web-res/StandardizeCaseBase/";
        System.out.println(path.substring(path.lastIndexOf("/tcoamp-web-res")));

    }

}
