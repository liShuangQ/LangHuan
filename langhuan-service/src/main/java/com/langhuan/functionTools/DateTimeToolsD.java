package com.langhuan.functionTools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;

@Slf4j
public class DateTimeToolsD {

    //    @Tool
//    name：工具的名称。如果未提供，则将使用方法名称。AI 模型在调用工具时使用此名称来识别工具。因此，不允许在同一个类中有两个同名的工具。该名称在模型可用于特定聊天请求的所有工具中必须是唯一的。
//    description：工具的描述，模型可以使用它来了解何时以及如何调用工具。如果未提供，则方法名称将用作工具描述。但是，强烈建议提供详细的描述，因为这对于模型了解工具的用途以及如何使用它至关重要。未能提供良好的描述可能会导致模型在应该使用该工具时未使用该工具或错误地使用该工具。
//    returnDirect：工具结果是应直接返回给客户端还是传递回模型。有关更多详细信息，请参阅直接返回。
//    resultConverter：用于将工具调用的结果转换为发送回 AI 模型的实现。有关更多详细信息，请参阅 Result Conversion。ToolCallResultConverterString object

    // 由于模型的不同 不设置returnDirect会报错 需要模型阅读建议手动
    @Tool(description = "获取用户时区中的当前日期和时间", returnDirect = true)
    String getCurrentDateTime() {
        log.info("调用工具：" + "getCurrentDateTime");
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }

    //    @ToolParam
//    description：参数的描述，模型可以使用该描述来更好地了解如何使用它。例如，参数应采用什么格式、允许哪些值等。
//    required：参数是必需的还是可选的。默认情况下，所有参数都被视为必需参数。
    @Tool(description = "为用户设置闹钟", returnDirect = true)
    String setAlarm(@ToolParam(description = "时间") String time) {
        log.info("调用工具：" + "setAlarm");
        return "设置成功：" + time;
    }


}
