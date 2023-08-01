package yonam2023.sfproject.logistics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yonam2023.sfproject.logistics.aop.LogisticsNotify;
import yonam2023.sfproject.logistics.controller.form.ReceiveForm;
import yonam2023.sfproject.logistics.domain.ReceiveRecord;
import yonam2023.sfproject.logistics.domain.StoredItem;
import yonam2023.sfproject.logistics.repository.ReceiveRecordRepository;
import yonam2023.sfproject.logistics.repository.StoredItemRepository;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class ReceiveService {
    private final ReceiveRecordRepository receiveRecordRepo;
    private final StoredItemRepository storedItemRepo;

    @LogisticsNotify
    @Transactional
    public String saveReceiveRecord(ReceiveForm.Request receiveReqForm){
        ReceiveRecord receiveRecord = receiveReqForm.toEntity();
        receiveRecordRepo.save(receiveRecord);

        StoredItem storedItem = storedItemRepo.findByName(receiveRecord.getItemName());

        // 처음 입고 예약 받은 물건은 재고DB에 저장된 기록이 없으므로, (storedItem == null) 이다.
        if(storedItem == null){
            // 재고DB에 물건의 존재만 저장하는 것이므로, amount를 0으로 둔다.
            return storedItemRepo.save( new StoredItem(receiveRecord.getItemName(),0) ).getName();
        }

        return storedItem.getName();

    }

    @LogisticsNotify
    @Transactional
    public String confirmReceiveRecord(long receiveId){
        ReceiveRecord receiveRecord = receiveRecordRepo.findById(receiveId).orElseThrow();
        StoredItem storedItem = storedItemRepo.findByName(receiveRecord.getItemName());

        // 처음 입고 받는 물건이어도 null일 수 없다. 입고 예약할 때(ReceiveService#saveReceiveRecord) amount = 0으로 재고에 올려놨기 때문.
        if(storedItem == null){
            //있을 수 없는 상황이므로 예외 발생
            throw new NoSuchElementException("발생할 수 없는 상황.");
        }
        //입고일을 현재 날짜로 변경
        receiveRecord.setDateTime(LocalDateTime.now());

        //현재 예약이 confirm 되었으므로 갱신.
        receiveRecord.setConfirmed(true);

        //재고에 반영
        storedItem.addAmount(receiveRecord.getAmount());
        return storedItem.getName();

    }

    @LogisticsNotify
    @Transactional
    public String editReceiveRecord(long receiveId, ReceiveForm.Request receiveReqForm){
        ReceiveRecord targetRecord = receiveRecordRepo.findById(receiveId).orElseThrow();
        StoredItem storedItem = storedItemRepo.findByName(receiveReqForm.getItemName());

        int previousReservedAmount = targetRecord.getAmount();

        //입고 예약 수정
        targetRecord.setAmount(receiveReqForm.getAmount());
        targetRecord.setDateTime(receiveReqForm.getDateTime());

        //재고 수정
        storedItem.subAmount(previousReservedAmount);       //원상 복구
        storedItem.addAmount(receiveReqForm.getAmount());   //수정된 입고량 반영

        return storedItem.getName();
    }

    @LogisticsNotify
    @Transactional
    public String deleteReceiveRecord(long recordId){

        ReceiveRecord findRecord = receiveRecordRepo.findById(recordId).orElseThrow();
        StoredItem storedItem = storedItemRepo.findByName(findRecord.getItemName());

        //입고 예약 목록에 delete 버튼이 활성화 된 상태
        // == 입고 예약이 되어 있는 상태
        // == 재고DB에도 예약된 Item이 항상 존재함. (ReceiveService#saveReceiveRecord)
        // == storedItem은 절대 null이 될 수 없음.
        if(storedItem == null){
            throw new NoSuchElementException("[ReceiveService#delete] 발생할 수 없는 상황.");
        }
        else{
            storedItem.subAmount(findRecord.getAmount());
        }
        receiveRecordRepo.deleteById(recordId);

        return storedItem.getName();
    }

}
