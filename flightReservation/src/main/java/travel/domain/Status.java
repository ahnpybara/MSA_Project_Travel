package travel.domain;

public enum Status {
    결제대기,   // create method
    결제완료,   //Paid
    결제실패,   //PaymentFail
    결제취소,   //PaymentCancel
    예약완료,   //complete payment 
    예약취소,   //cancel 메서드
    환불완료,   //PaymentRefunded
    환불실패    //PaymentRefundedFailed
}
