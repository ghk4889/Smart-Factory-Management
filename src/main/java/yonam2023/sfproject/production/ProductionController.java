package yonam2023.sfproject.production;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import yonam2023.sfproject.production.domain.Production;
import yonam2023.sfproject.production.rpository.ProductionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/production")
public class ProductionController {

    @Autowired
    ProductionRepository pr;

    @GetMapping
    public String initGet(Model model, @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Production> all = pr.findAll(pageable);
        model.addAttribute("list", all);
        return "production/init";
    }

    @GetMapping("/getAll")
    public @ResponseBody List<Production> productionGetAll(){
        return pr.findAll();
    }

    @GetMapping("/{id}")
    public @ResponseBody Production productionGetAll(@PathVariable long id){
        Optional<Production> oProduction =  pr.findById(id);
        if(oProduction.isEmpty()){
            return Production.builder().stype("logNotExist").build();
        }else{
            return oProduction.get();
        }
    }

    @PostMapping("/update")
    public @ResponseBody String productionUpdate(@RequestBody Map<String, String> param){

        Long id = Long.parseLong(param.get("ID"));
        String stype = param.get("STYPE");
        int svalue = Integer.parseInt(param.get("SVALUE"));

        Optional<Production> oProduction =  pr.findById(id);
        if(oProduction.isEmpty()){
            return "그런 값은 존재하지 않음.";
        }else{
            Production production = oProduction.get();
            production.setStype(stype);
            production.setSvalue(svalue);

            pr.save(production);

            return "ID가 "+id.toString()+"인 튜플이 stype : " + stype + " / svalue : " + svalue + "로 수정됨.";
        }
    }

    @PostMapping("/insert")
    public @ResponseBody List<Production> productionInsert(@RequestBody Map<String, String> param){
        String stype = param.get("STYPE");
        int svalue = Integer.parseInt(param.get("SVALUE"));
        Production production= Production.builder().stype(stype).svalue(svalue).build();
        pr.save(production);

        return pr.findAll();
    }

    @PostMapping("/delete")
    public @ResponseBody List<Production> productionDelete(@RequestBody Map<String, String> param){
        Long id = Long.parseLong(param.get("ID"));
        List<Production> lProduction = new ArrayList<>();

        Optional<Production> oProduction =  pr.findById(id);
        if(oProduction.isEmpty()){
            Production rp = Production.builder().stype("logNotExist").build();
            lProduction.add(rp);
            return lProduction;
        }else{
            pr.deleteById(id);

            return pr.findAll();
        }
    }

    @GetMapping("/deleteAll")
    public @ResponseBody List<Production> productionDeleteAll(){
        pr.deleteAll();
        return pr.findAll();
    }
}