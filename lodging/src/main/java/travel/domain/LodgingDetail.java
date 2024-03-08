package travel.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PostPersist;
import javax.persistence.Table;

import lombok.Data;
import travel.LodgingApplication;
import travel.repository.LodgingDetailRepository;

@Entity
@Table(name = "LodgingDetail_table")
@Data
//<<< DDD / Aggregate Root
public class LodgingDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long contentid;

    private Long contenttypeid;

    private Long createdtime;

    private String title;

    private Long modifiedtime;

    private String tel;

    private String telname;

    private String homePage;

    private String firstimage;

    private String firstimage2;

    private String areacode;

    private String sigungucode;

    private String addr1;

    private String addr2;

    private String mapx;

    private String mapy;

    private String overview;

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
