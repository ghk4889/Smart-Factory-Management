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

import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class ReceiveService {
    private final ReceiveRecordRepository receiveRecordRepo;
    private final StoredItemRepository storedItemRepo;

    @LogisticsNotify
    @Transactional
    public Long saveReceiveRecord(ReceiveForm.Request receiveReqForm){
        ReceiveRecord receiveRecord = receiveReqForm.toEntity();
        receiveRecordRepo.save(receiveRecord);

        StoredItem storedItem = storedItemRepo.findByName(receiveRecord.getItemName());

        if(storedItem == null){
            return storedItemRepo.save(new StoredItem(receiveRecord.getItemName(),receiveRecord.getAmount())).getId();
        }
        else{
            storedItem.addAmount(receiveRecord.getAmount());
            return storedItem.getId();
        }

    }

    @LogisticsNotify
    @Transactional
    public Long editReceiveRecord(long id, ReceiveForm.Request receiveReqForm){
        ReceiveRecord targetRecord = receiveRecordRepo.findById(id).orElseThrow();
        StoredItem storedItem = storedItemRepo.findByName(receiveReqForm.getItemName());

        int previousReservedAmount = targetRecord.getAmount();

        //입고 예약 수정
        targetRecord.setAmount(receiveReqForm.getAmount());
        targetRecord.setDateTime(receiveReqForm.getDateTime());

        //재고 수정
        storedItem.subAmount(previousReservedAmount);       //원상 복구
        storedItem.addAmount(receiveReqForm.getAmount());   //수정된 입고량 반영

        return storedItem.getId();
    }

    @LogisticsNotify
    @Transactional
    public Long deleteReceiveRecord(long recordId){

        ReceiveRecord findRecord = receiveRecordRepo.findById(recordId).orElseThrow();
        StoredItem storedItem = storedItemRepo.findByName(findRecord.getItemName());
        if(storedItem == null){
            //todo: 입고 예약일과 재고 반영일을 실제 동작하도록 구현한다면 재고가 생기기 전에 입고 예약을 취소할 수 있다.
            // 지금은 입고 예약일과 무관하게 바로 재고에 생기므로 byNameItem이 null일 수 없다. 일단 예외를 발생시키는 것으로 처리하자.
            // 추후 예약일 동기화 기능이 도입되면 적절한 로직을 추가하자.
            throw new NoSuchElementException("[ReceiveService#delete] 날짜 동기화 기능을 도입했다면 로직을 새로 작성해야 한다.");
        }
        else{
            storedItem.subAmount(findRecord.getAmount());
        }
        receiveRecordRepo.deleteById(recordId);

        return storedItem.getId();
    }

}
