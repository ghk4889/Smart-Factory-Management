package yonam2023.sfproject.logistics.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class ReceiveRecord {
    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String itemName;

    private int amount;

    private LocalDateTime dateTime;

    public ReceiveRecord(String itemName, int amount, LocalDateTime dateTime) {
        this.itemName = itemName;
        this.amount = amount;
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return "ReceiveRecord{" +
                "id=" + id +
                ", itemName='" + itemName + '\'' +
                ", amount=" + amount +
                ", receiveDateTime=" + dateTime +
                '}';
    }
}
