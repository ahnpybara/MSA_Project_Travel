package travel.infra;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import travel.domain.FlightReservationRepository;
import travel.domain.Status;

public class ReservationTimeoutCheckJob implements Job{

    @Autowired
    private FlightReservationRepository flightReservationRepository;
    
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException{
        
        Object value = context.getJobDetail().getJobDataMap().get("flightReservationId");

        if (value instanceof Long){
            Long flightReservationId = (Long) value;
        
        flightReservationRepository.findById(flightReservationId).ifPresent(flightReservation ->{
            if(flightReservation.getStatus() == Status.결제대기){
                flightReservation.setStatus(Status.취소완료);
                flightReservationRepository.save(flightReservation);
            }
        
        });
        }else{
            throw new JobExecutionException("flightReservationId is not Long type");
        }

    }

}
