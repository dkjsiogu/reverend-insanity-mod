package com.reverendinsanity.core.deduction;

import com.reverendinsanity.core.combat.KillerMove;
import javax.annotation.Nullable;

// 推演结果
public record DeductionResult(
    Outcome outcome,
    @Nullable KillerMove resultMove,
    int improvementLevel,
    float experienceGained,
    String message
) {
    public enum Outcome {
        GREAT_SUCCESS("大成功"),
        SUCCESS("成功"),
        PARTIAL("部分成功"),
        FAILURE("失败"),
        DISCOVERY("意外发现");

        private final String displayName;
        Outcome(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }
}
