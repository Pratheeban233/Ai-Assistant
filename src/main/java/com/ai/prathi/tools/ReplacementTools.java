package com.ai.prathi.tools;

import com.ai.prathi.service.ReplacementService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReplacementTools {

    private final ReplacementService replacementService;

    @Tool("Record a replacement request for an eligible paid and delivered order. This does not create or ship a replacement.")
    public String requestReplacement(
            @P("Verified order number") String orderNumber,
            @P("Customer's reason for requesting a replacement") String reason) {
        return replacementService.requestReplacement(orderNumber, reason);
    }
}
