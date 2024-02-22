package travel.infra;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import travel.config.kafka.KafkaProcessor;
import travel.domain.*;

@Service
public class UserInfoViewHandler {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenSignedUp_then_CREATE_1(@Payload SignedUp signedUp) {
        try {
            if (!signedUp.validate()) return;

            UserInfo userInfo = new UserInfo();
            userInfo.setId(signedUp.getId());
            userInfo.setName(signedUp.getName());
            userInfo.setUsername(signedUp.getUsername());
            userInfoRepository.save(userInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
