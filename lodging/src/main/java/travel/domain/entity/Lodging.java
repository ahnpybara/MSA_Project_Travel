package travel.domain.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PostPersist;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import travel.LodgingApplication;
import travel.domain.repository.LodgingRepository;

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

    @JsonProperty("contentid")
    private Long contentId;

    @JsonProperty("contenttypeid")
    private Long contentTypeId;

    @JsonProperty("createtime")
    private Long createdTime;

    @JsonProperty("firstimage")
    private String image;

    @JsonProperty("firstimage2")
    private String thumbNail;
    @JsonProperty("tel")

    private String phoneNumber;

    private String title;

    @JsonProperty("sigungucode")
    private Long sigunguCode;

    @JsonProperty("areacode")
    private Long areaCode;

    @JsonProperty("mapx")
    private Float mapX;

    @JsonProperty("mapy")
    private Float mapY;

    @JsonProperty("mlevel")
    private Long mLevel;

    @JsonProperty("modifiedtime")
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
