package travel.domain;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import lombok.Data;
import travel.LodgingApplication;

@Entity
@Table(name = "Lodging_table")
@Data
//<<< DDD / Aggregate Root
public class Lodging {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String addr1;

    private String addr2;

    private Long contentId;

    private Long contentTypeId;

    private Long createdTime;

    private String image;

    private String thumbNail;

    private String phoneNumber;

    private String title;

    private String sigunguCode;

    private String areaCode;

    private Float mapX;

    private Float mapY;

    private Long mLevel;

    private Long modifiedTime;

    @PostPersist
    public void onPostPersist() {}

    public static LodgingRepository repository() {
        LodgingRepository lodgingRepository = LodgingApplication.applicationContext.getBean(
            LodgingRepository.class
        );
        return lodgingRepository;
    }
}
//>>> DDD / Aggregate Root
