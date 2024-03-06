package travel.domain;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import lombok.Data;
import travel.LodgingApplication;

@Entity
@Table(name = "LodgingDetail_table")
@Data
//<<< DDD / Aggregate Root
public class LodgingDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long contentId;

    private Long contentTypeId;

    private Long createdTime;

    private String title;

    private Long modifiedTime;

    private String phoneNumber;

    private String telName;

    private String homePage;

    private String image;

    private String image2;

    private String areaCode;

    private String sigunguCode;

    private String addr1;

    private String addr2;

    private String mapX;

    private String mapY;

    private String overView;

    private String roomCount;

    private String roomType;

    private String refundregulation;

    private String checkInTime;

    private String checkOutTime;

    private String chkCooking;

    private String seminar;

    private String sports;

    private String sauna;

    private String beauty;

    private String beverage;

    private String karaoke;

    private String barbeque;

    private String campfire;

    private String bicyde;

    private String fitness;

    private String publicPc;

    private String publicBath;

    private String subfadility;

    private String foodPlace;

    private String reservationUrl;

    private String pickup;

    private String infoCenterLodging;

    private String parkingLodging;

    private String reservationLodging;

    private String scaleLodging;

    private String accomCountLodging;

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
