package travel.domain;

import javax.persistence.*;
import lombok.Data;
import travel.LodgingApplication;

@Entity
@Table(name = "LodgingDetail_table")
@Data
public class LodgingDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long contentId;

    private Long contentTypeId;

    private Long roomCount;

    private String checkinTime;

    private String checkoutTime;

    private String foodplace;

    private String phoneNumber;

    private String reservationLodging;

    private String capacityLodging;

    private String parkingLodging;

    private String pickUp;

    private String reservationUrl;

    private String scaleLodging;

    @PostPersist
    public void onPostPersist() {}

    public static LodgingDetailRepository repository() {
        LodgingDetailRepository lodgingDetailRepository = LodgingApplication.applicationContext.getBean(
            LodgingDetailRepository.class
        );
        return lodgingDetailRepository;
    }
}
//>>> DDD / Aggregate Root
