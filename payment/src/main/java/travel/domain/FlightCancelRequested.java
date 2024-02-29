package travel.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import travel.infra.AbstractEvent;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public class FlightCancelRequested extends AbstractEvent {
    private Long id;
}
