package yonam2023.sfproject.employee;


import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import yonam2023.sfproject.employee.domain.Employee;

@Controller
@RequestMapping("/employee")
@Slf4j
public class EmployeeController {

    @GetMapping
    public String init(Model model, @AuthenticationPrincipal Employee employee) {

        model.addAttribute("employee", employee);
        return "employee/dashboard";
    }

    @PostMapping("/")
    public void checkIn() {

    }

    @PostMapping("/")
    public void checkOut() {

    }
}
