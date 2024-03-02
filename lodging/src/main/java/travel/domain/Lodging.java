package travel.domain;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import travel.LodgingApplication;

@Entity
@Table(name = "Lodging_table")
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Lodging {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String errorStatus;

    private String addr1;

    private String addr2;

    @JsonProperty("contentid")
    private Long contentId;

    @JsonProperty("contenttypeid")
    private Long contentTypeId;

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

    @JsonProperty("modifiedtime")
    private Long modifiedTime;

    public static LodgingRepository repository() {
        LodgingRepository lodgingRepository = LodgingApplication.applicationContext.getBean(
            LodgingRepository.class
        );
        return lodgingRepository;
    }
}
