package yonam2023.sfproject.logistics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yonam2023.sfproject.logistics.aop.LogisticsNotify;
import yonam2023.sfproject.logistics.controller.form.SendForm;
import yonam2023.sfproject.logistics.domain.SendRecord;
import yonam2023.sfproject.logistics.domain.StoredItem;
import yonam2023.sfproject.logistics.repository.SendRecordRepository;
import yonam2023.sfproject.logistics.repository.StoredItemRepository;

import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class SendService {
    private final SendRecordRepository sendRecordRepo;
    private final StoredItemRepository storedItemRepo;

    @LogisticsNotify
    @Transactional
    public Long saveSendRecord(SendForm.Request sendReqForm){
        SendRecord sendRecord = sendReqForm.toEntity();
        sendRecordRepo.save(sendRecord);

        StoredItem storedItem = storedItemRepo.findByName(sendRecord.getItemName());

        if(storedItem == null){
            return storedItemRepo.save(new StoredItem(sendRecord.getItemName(),-sendRecord.getAmount())).getId();
        }
        else{
            storedItem.subAmount(sendRecord.getAmount());
            return storedItem.getId();
        }
    }

    @LogisticsNotify
    @Transactional
    public Long editSendRecord(long id, SendForm.Request sendReqForm){
        SendRecord targetRecord = sendRecordRepo.findById(id).orElseThrow();
        StoredItem storedItem = storedItemRepo.findByName(sendReqForm.getItemName());

        int previousReservedAmount = targetRecord.getAmount();

        // 출고 예약 수정
        targetRecord.setAmount(sendReqForm.getAmount());
        targetRecord.setDateTime(sendReqForm.getDateTime());

        //재고 수정
        storedItem.addAmount(previousReservedAmount); //원상 복구
        storedItem.subAmount(sendReqForm.getAmount()); //수정된 출고 amount 반영
        return storedItem.getId();
    }

    @LogisticsNotify
    @Transactional
    public Long deleteSendRecord(long recordId){

        // 두 사람이 동시에 같은 예약을 삭제하려고 하면 이미 DB에서 제거되었기 때문에 NoSuchElementException이 발생한다.
        SendRecord findRecord = sendRecordRepo.findById(recordId).orElseThrow();
        StoredItem storedItem = storedItemRepo.findByName(findRecord.getItemName());

        // 예약을 삭제하려고 하는데 예약된 게 없는 경우 storedItem == null이 된다. (사실 발생할 수 없는 경우다.)
        // 이 경우 NoSuchElementException을 발생시킨다.
        if(storedItem == null){
            throw new NoSuchElementException("출고 예약을 삭제하려고 하는데 예약된 게 없는 경우 byNameItem == null이 된다. (사실 발생할 수 없는 경우다.)");
        }
        else{
            storedItem.addAmount(findRecord.getAmount());
        }
        sendRecordRepo.deleteById(recordId);
        return storedItem.getId();
    }

}

