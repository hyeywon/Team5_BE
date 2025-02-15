package ojosama.talkak.recommendation.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;
import ojosama.talkak.common.util.HashConverter;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record EventQueue(
    Long memberId,
    Long categoryId,
    String lastUpdatedAt
) {

    public static EventQueue of(Long memberId, Long categoryId, LocalDateTime lastUpdatedAt) {
        return new EventQueue(memberId, categoryId, HashConverter.fromDateTime(lastUpdatedAt));
    }
}
