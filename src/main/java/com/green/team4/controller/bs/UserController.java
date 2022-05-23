package com.green.team4.controller.bs;

import com.green.team4.service.bs.UserService;
import com.green.team4.vo.bs.Criteria;
import com.green.team4.vo.bs.PageMaker;
import com.green.team4.vo.bs.UserVO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/bs/board/*")
@Log4j2
public class UserController {
    @Autowired
    UserService userService;
    @GetMapping("/list")
    public void list(Model model,Criteria criteria) {
        PageMaker pageMaker = new PageMaker(criteria, 500);
        log.info("list로 이동....");
        log.info("criteria : " + criteria.getPage());
        log.info("올림값 : " + (int)(Math.ceil((double)(criteria.getPage()+1)/(double)10)));
        log.info("pageMaker : " + pageMaker.isNext() + pageMaker.isPrev());
        model.addAttribute("items",userService.getPageList(criteria));
        model.addAttribute("pageMaker",pageMaker);

    }

    @GetMapping("/register")
    public void register() {
        log.info("register로 이동....");
    }

    @PostMapping("/register")
    public String register(UserVO vo) {
        userService.insert(vo);
        log.info("게시글 등록 성공");
        return "redirect:list";
    }

    @GetMapping({"/read","modify"})
    public void read(Model model, Long uNo) {
        log.info("read로 이동");
        model.addAttribute("item",userService.getOne(uNo));
    }


    @PostMapping("/modify")
    public String modify(UserVO vo) {
        userService.modify(vo);
        log.info("수정 완료");
        return "redirect:list";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam("uNo") Long uNo){
        userService.delete(uNo);
        log.info("삭제(수정) 완료");
        return "redirect:list";
    }
}
