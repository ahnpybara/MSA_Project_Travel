package travel.domain;

public enum Status {
    결제대기, //맨처음
    결제완료, //PyamentComplete
    결제실패, //PaymentFailed
    결제취소, //PaymenCancelled
    예약완료, //Flightreservation
    예약취소, 
    취소요청,
    취소실패,
    취소완료
}
