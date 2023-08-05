package yonam2023.sfproject.logistics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yonam2023.sfproject.logistics.aop.LogisticsNotify;
import yonam2023.sfproject.logistics.controller.form.SendForm;
import yonam2023.sfproject.logistics.domain.SendRecord;
import yonam2023.sfproject.logistics.domain.StoredItem;
import yonam2023.sfproject.logistics.repository.SendRecordRepository;
import yonam2023.sfproject.logistics.repository.StoredItemRepository;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Slf4j
@RequiredArgsConstructor
@Service
public class SendService {
    private final SendRecordRepository sendRecordRepo;
    private final StoredItemRepository storedItemRepo;

    @LogisticsNotify
    @Transactional
    public String saveSendRecord(SendForm.Request sendReqForm){
        SendRecord sendRecord = sendReqForm.toEntity();
        sendRecordRepo.save(sendRecord);

        log.info("sendReqForm.getItemName: {}", sendReqForm.getItemName());
        log.info("sendRecord.getItemName : {}", sendRecord.getItemName());

        StoredItem storedItem = storedItemRepo.findByName(sendRecord.getItemName());

        // 상품을 출고 예약할 때 재고에 해당 상품이 없는 경우이다. 이 경우 두 가지 정책이 있다.
        // 1) 예외를 발생시킨다.  
        // 2) 재고에 수량이 0인 상품을 넣어 재고 목록을 만든다.(= 출고 예약일뿐이므로 재고가 없어도 예약은 가능하다고 보는 정책)
        // 서비스의 융통성을 위해 2번 정책으로 구현한다.
        if(storedItem == null){
            // 재고DB에 물건의 존재만 저장하는 것이므로, amount를 0으로 둔다.
            return storedItemRepo.save( new StoredItem(sendRecord.getItemName(),0) ).getName();
        }
        return storedItem.getName();
    }

    @LogisticsNotify
    @Transactional
    public String confirmSendRecord(long sendId){
        SendRecord sendRecord = sendRecordRepo.findById(sendId).orElseThrow();
        StoredItem storedItem = storedItemRepo.findByName(sendRecord.getItemName());

        // 출고하려는 물건은 항상 재고 목록에 있어야 된다. (수량이 0이하 일 수는 있다.)
        if(storedItem == null){
            //있을 수 없는 상황이므로 예외 발생
            throw new NoSuchElementException("발생할 수 없는 상황.");
        }
        //출고일을 현재 날짜로 변경
        sendRecord.setDateTime(LocalDateTime.now());

        //현재 예약이 confirm 되었으므로 갱신.
        sendRecord.setConfirmed(true);

        //재고에 반영
        storedItem.subAmount(sendRecord.getAmount());

        return storedItem.getName();
    }

    @LogisticsNotify
    @Transactional
    public String editSendRecord(long id, SendForm.Request sendReqForm){
        SendRecord targetRecord = sendRecordRepo.findById(id).orElseThrow();
        StoredItem storedItem = storedItemRepo.findByName(sendReqForm.getItemName());

        int previousReservedAmount = targetRecord.getAmount();

        // 출고 예약 수정
        targetRecord.setAmount(sendReqForm.getAmount());
        targetRecord.setDateTime(sendReqForm.getDateTime());

        //재고 수정
        storedItem.addAmount(previousReservedAmount); //원상 복구
        storedItem.subAmount(sendReqForm.getAmount()); //수정된 출고 amount 반영
        return storedItem.getName();
    }

    @LogisticsNotify
    @Transactional
    public String deleteSendRecord(long recordId){

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
        return storedItem.getName();
    }

}

