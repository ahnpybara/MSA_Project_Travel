package travel.domain;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import lombok.Data;
import travel.LodgingApplication;
import travel.repository.RoomRepository;

@Entity
@Table(name = "Room_table")
@Data
//<<< DDD / Aggregate Root
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long contentId;

    private Long contentTypeId;

    private Long roomCode;

    private String roomTitle;

    private String roomSize;

    private String roomCount;

    private Integer roomBaseCount;

    private Integer roomMaxCount;

    private Long roomOffSeasonMinfee1;

    private Long roomOffSeasonMinfee2;

    private Long roomPeakSeasonMinfee1;

    private Long roomPeakSeasonMinfee2;

    private String roomIntro;

    private String roomBathFadility;

    private String roomBath;

    private String roomHomeTheater;

    private String roomAirCondition;

    private String roomTv;

    private String roomPc;

    private String roomCable;

    private String roomInternet;

    private String roomRefrigerator;

    private String roomToiletries;

    private String roomSofa;

    private String roomCook;

    private String roomTable;

    private String roomHairdryer;

    private String roomSize2;

    private String roomImg1;

    private String roomImg1Alt;

    private String roomImg2;

    private String roomImg2Alt;

    private String roomImg3;

    private String roomImg3Alt;

    private String roomImg4;

    private String roomImg4Alt;

    private String originImgUrl;

    private String imgName;

    private String smallImageUrl;

    @PostPersist
    public void onPostPersist() {}

    public static RoomRepository repository() {
        RoomRepository roomRepository = LodgingApplication.applicationContext.getBean(
            RoomRepository.class
        );
        return roomRepository;
    }
}
//>>> DDD / Aggregate Root
