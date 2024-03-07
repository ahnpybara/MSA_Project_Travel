package travel.domain;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import travel.LodgingApplication;
import travel.repository.LodgingDetailRepository;

@Entity
@Table(name = "LodgingDetail_table")
@Data
// <<< DDD / Aggregate Root
public class LodgingDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @JsonProperty("contentid")
    private Long contentId;

    @JsonProperty("contenttypeid")
    private Long contentTypeId;

    @JsonProperty("createdtime")
    private Long createdTime;

    private String title;

    @JsonProperty("modifiedtime")
    private Long modifiedTime;

    @JsonProperty("tel")
    private String phoneNumber;

    @JsonProperty("telname")
    private String telName;

    @JsonProperty("homepage")
    private String homePage;

    private String image;

    private String image2;

    @JsonProperty("areacode")
    private String areaCode;

    @JsonProperty("sigungucode")
    private String sigunguCode;

    private String addr1;

    private String addr2;

    @JsonProperty("mapx")
    private String mapX;

    @JsonProperty("mapy")
    private String mapY;

    @JsonProperty("overview")
    private String overView;

    @JsonProperty("roomcount")
    private String roomCount;

    @JsonProperty("roomtype")
    private String roomType;

    private String refundregulation;

    @JsonProperty("checkintime")
    private String checkInTime;

    @JsonProperty("checkouttime")
    private String checkOutTime;

    @JsonProperty("chkcooking")
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

    private String mlevel;

    @JsonProperty("publicpc")
    private String publicPc;

    @JsonProperty("publicbath")
    private String publicBath;

    private String subfadility;

    @JsonProperty("foodplace")
    private String foodPlace;

    @JsonProperty("reservationurl")
    private String reservationUrl;

    private String pickup;

    @JsonProperty("infocenterlodging")
    private String infoCenterLodging;

    @JsonProperty("parkinglodging")
    private String parkingLodging;

    @JsonProperty("reservationlodging")
    private String reservationLodging;

    @JsonProperty("scalelodging")
    private String scaleLodging;

    @JsonProperty("accomcountlodging")
    private String accomCountLodging;

    @PostPersist
    public void onPostPersist() {
    }

    public static LodgingDetailRepository repository() {
        LodgingDetailRepository lodgingDetailRepository = LodgingApplication.applicationContext.getBean(
                LodgingDetailRepository.class);
        return lodgingDetailRepository;
    }
}
// >>> DDD / Aggregate Root
