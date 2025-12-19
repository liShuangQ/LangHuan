package com.langhuan.config

import io.modelcontextprotocol.spec.McpSchema
import org.springframework.ai.mcp.McpConnectionInfo
import org.springframework.ai.mcp.McpToolNamePrefixGenerator
import org.springframework.stereotype.Component


@Component
class CustomToolNamePrefixGenerator : McpToolNamePrefixGenerator {
    public override fun prefixedToolName(connectionInfo: McpConnectionInfo, tool: McpSchema.Tool): String {
        val serverName = connectionInfo.initializeResult().serverInfo().name()
//        val serverVersion = connectionInfo.initializeResult().serverInfo().version()
        return serverName + "_" + tool.name()
    }
}