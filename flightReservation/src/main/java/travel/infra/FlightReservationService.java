package travel.infra;


import org.quartz.DateBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import travel.domain.*;

@Service
public class FlightReservationService {
    
    @Autowired
    private FlightReservationRepository flightReservationRepository;

    @Autowired
    private Scheduler scheduler;


    public FlightReservation saveFlightReservation(FlightReservation flightReservation){
        return flightReservationRepository.save(flightReservation);
    }

    public void scheduleReservationTimeoutCheck(Long flightReservationId){
        JobDetail jobDetail = JobBuilder.newJob(ReservationTimeoutCheckJob.class)
        .withIdentity("trigger" + flightReservationId, "reservationTimeoutChecks")
        .usingJobData("flightReservationId",flightReservationId)
        .build();

        Trigger trigger = TriggerBuilder.newTrigger()
        .withIdentity("trigger" + flightReservationId," reservationTimeoutChecks")
        .startAt(DateBuilder.futureDate(1, DateBuilder.IntervalUnit.MINUTE))
        .build();
        
        try{
            scheduler.scheduleJob(jobDetail, trigger);
        }catch(SchedulerException e){
            e.printStackTrace();
        }

    }
    
 
}