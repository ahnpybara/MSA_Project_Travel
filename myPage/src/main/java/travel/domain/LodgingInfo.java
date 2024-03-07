package travel.domain;

import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "LodgingInfo_table")
@Data
public class LodgingInfo {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
}
