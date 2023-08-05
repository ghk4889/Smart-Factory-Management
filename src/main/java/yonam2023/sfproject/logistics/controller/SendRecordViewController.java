package yonam2023.sfproject.logistics.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import yonam2023.sfproject.logistics.controller.form.SendForm;
import yonam2023.sfproject.logistics.domain.SendRecord;
import yonam2023.sfproject.logistics.repository.SendRecordRepository;
import yonam2023.sfproject.logistics.service.SendService;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@RequestMapping("/sendRecords")
@Controller
@RequiredArgsConstructor
@Slf4j
public class SendRecordViewController {
    private final SendRecordRepository sendRecordRepo;
    private final SendService sendService;

    //더미 데이터 생성
    @PostConstruct
    public void init(){
        List<SendRecord> sendRecords = new ArrayList<>();
        LocalDate date = Year.of(2023).atMonth(3).atDay(20);
        for(int i = 0; i<20; i++){
            SendRecord newRecord = new SendRecord("item " + i, 11, "부산", date.atTime(15, i + 1));
            newRecord.setConfirmed(true);
            sendRecords.add(newRecord);
        }
        sendRecordRepo.saveAll(sendRecords);
    }

    @GetMapping
    public String sendRecordsHome(Model model, @PageableDefault(sort = "dateTime", direction = Sort.Direction.DESC) Pageable pageable){
        Page<SendRecord> all = sendRecordRepo.findAll(pageable);
        model.addAttribute("pageObj", all);
        return "logistics/sendRecord/sendRecords";
    }

    @GetMapping("/reserve")
    public String reserveForm(Model model){
        return "logistics/sendRecord/sendReserveForm";
    }

    @PostMapping("/reserve")
    public String reserveItem(@ModelAttribute SendForm.Request sendReqForm){
        sendService.saveSendRecord(sendReqForm);
        // todo: 값 검증 로직 추가하기. ex) 재고량을 초과하는 출고량이 입력되면 경고 문구.
        return "redirect:/sendRecords";
    }

    @PatchMapping("/confirm/{recordId}")
    @ResponseBody
    public ResponseEntity confirmReserve(@PathVariable long recordId){
        String itemName = sendService.confirmSendRecord(recordId);
        return ResponseEntity.ok(itemName);
    }

    // edit form page
    @GetMapping("/{recordId}/edit")
    public String editForm(@PathVariable long recordId, Model model){
        SendRecord targetRecord = sendRecordRepo.findById(recordId).orElse(null);
        model.addAttribute("record", targetRecord);
        return "logistics/sendRecord/sendEditForm";
    }

    @PatchMapping("/{recordId}")
    public String editReserveRecord(@PathVariable long recordId, @ModelAttribute SendForm.Request sendReqForm){
        sendService.editSendRecord(recordId, sendReqForm);
        return "redirect:/sendRecords";
    }


    // 공부하는 차원에서 delete 연산만 ajax로 처리함.
    @DeleteMapping("/{recordId}")
    @ResponseBody
    public ResponseEntity deleteReserve(@PathVariable long recordId){
        //NoSuchElementException 발생 시 LogisticsExControllerAdvice의 @ExceptionHandler가 처리
        sendService.deleteSendRecord(recordId);
        return ResponseEntity.ok(recordId);
    }

}
