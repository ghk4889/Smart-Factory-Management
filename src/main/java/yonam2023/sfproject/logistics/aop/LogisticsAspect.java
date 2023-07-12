package yonam2023.sfproject.logistics.aop;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import yonam2023.sfproject.employee.domain.DepartmentType;
import yonam2023.sfproject.logistics.repository.StoredItemRepository;
import yonam2023.sfproject.notification.fcm.NotifyService;

public class LogisticsAspect {

    @Aspect
    @RequiredArgsConstructor
    @Component
    public static class NotifyAspect{

        private final NotifyService notifyService;
        private final StoredItemRepository storedItemRepo;

        @AfterReturning(pointcut = "@annotation(yonam2023.sfproject.logistics.aop.LogisticsNotify)", returning = "storedId")
        public void notify(JoinPoint joinPoint, Long storedId){
            notifyService.departmentNotify(DepartmentType.LOGISTICS,"["+joinPoint.getSignature().getName()+"]",
                    "Item: "+storedItemRepo.findById(storedId).orElseThrow().getName());
        }
    }


}
