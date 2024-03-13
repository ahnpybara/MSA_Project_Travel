package travel.domain;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PostPersist;
import javax.persistence.Table;

import lombok.Data;
import travel.LodgingApplication;
import travel.repository.RoomRepository;

@Entity
@Table(name = "Room_table")
@Data
// <<< DDD / Aggregate Root
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long contentid;

    private Long contenttypeid;

    private Long roomcode;

    private String roomtitle;

    // private String roomsize;

    // private String roomcount;

    // private Integer roombasecount;

    // private Integer roommaxcount;

    // private Long roomoffseasonminfee1;

    // private Long roomoffseasonminfee2;

    // private Long roompeakSeasonMinfee1;

    // private Long roompeakseasonminfee2;

    // private String roomintro;

    // private String roombathfadility;

    // private String roombath;

    // private String roomhometheater;

    // private String roomaircondition;

    // private String roomtv;

    // private String roompc;

    // private String roomcable;

    // private String roominternet;

    // private String roomrefrigerator;

    // private String roomtoiletries;

    // private String roomsofa;

    // private String roomcook;

    // private String roomtable;

    // private String roomhairdryer;

    // private String roomsize2;

    // private String roomImg1;

    // private String roomImg1Alt;

    // private String roomImg2;

    // private String roomImg2Alt;

    // private String roomImg3;

    // private String roomImg3Alt;

    // private String roomImg4;

    // private String roomImg4Alt;

    // private String originImgurl;

    // private String imgname;

    // private String smallimageurl;

    @ElementCollection
    private Map<LocalDate,Long> roomCapacity = new HashMap<>();

    @PostPersist
    public void onPostPersist() {
    }

    public static RoomRepository repository() {
        RoomRepository roomRepository = LodgingApplication.applicationContext.getBean(
                RoomRepository.class);
        return roomRepository;
    }
}
// >>> DDD / Aggregate Root
