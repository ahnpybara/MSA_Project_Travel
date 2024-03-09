package travel.domain;

import javax.persistence.*;
import lombok.Data;
import travel.LodgingApplication;
import travel.repository.LodgingIntroRepository;

@Entity
@Table(name = "LodgingIntro_table")
@Data
public class LodgingIntro {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long contentid;

    private Long contenttypeid;

    private String roomcount;

    private String roomtype;

    private String refundregulation;

    private String checkintime;

    private String checkouttime;

    private String chkcooking;

    private String parkinglodging;

    @PostPersist
    public void onPostPersist() {}

    public static LodgingIntroRepository repository() {
        LodgingIntroRepository lodgingIntroRepository = LodgingApplication.applicationContext.getBean(
            LodgingIntroRepository.class
        );
        return lodgingIntroRepository;
    }
}
