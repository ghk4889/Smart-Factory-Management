package yonam2023.sfproject.logistics.controller.form;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import yonam2023.sfproject.logistics.domain.ReceiveRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReceiveForm {


    @NoArgsConstructor
    @ToString
    @Getter
    @Setter
    public static class Request {
        private String itemName;
        private int amount;
        @DateTimeFormat(pattern = "yyyy-MM-dd") // html에서 넘어온 Model의 String 타입을 알맞게 변환해줌.
        private LocalDate date;
        private String supplier;

        /* Dto -> Entity */
        public ReceiveRecord toEntity() {
            return new ReceiveRecord(itemName, amount, supplier,
                    // Math.random() * 24 ==> 0 ~ 23  // atTime(hour, minute)에서 hour는 0~23만 올 수 있음.
                    // Math.random() * 60 ==> 0 ~ 59  // atTime(hour, minute)에서 minute는 0~59만 올 수 있음.
                    date.atTime((int) (Math.random() * 24), (int) (Math.random() * 60)));

        }

        public LocalDateTime getDateTime(){
            return date.atTime((int) (Math.random() * 24), (int) (Math.random() * 59));
        }


    }

}