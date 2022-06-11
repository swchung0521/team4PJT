package com.green.team4.controller.admin;

import com.green.team4.mapper.admin.StaticMapper;
import com.green.team4.service.admin.MailService;
import com.green.team4.service.community.BoardService;
import com.green.team4.vo.admin.StaticVO;
import com.green.team4.vo.community.BoardVO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@Log4j2
public class AdminPageController {
    @Autowired
    MailService mailService;
    @Autowired
    BoardService boardService;
    @Autowired
    private StaticMapper staticMapper;
    @GetMapping("/admin/adpage")
    public void adPage(Model model){
        log.info("AdminPageController => adPage(GET) 실행");
        StaticVO profit = staticMapper.getTotalProfit();
        StaticVO memberCnt = staticMapper.getTotalMemberCnt();
        StaticVO delMemCnt = staticMapper.getTotalDeleteMemCnt();
        StaticVO productCnt = staticMapper.getTotalProductCnt();
        List<BoardVO> boardCnt = boardService.getList();

        log.info("staticVo profit 출력: "+profit.getTotalProfit());
        log.info("staticVo memberCnt 출력: "+memberCnt.getTotalMemberCnt());
        log.info("staticVo delMemCnt 출력: "+delMemCnt.getTotalDeleteMemCnt());
        log.info("staticVo productCnt 출력: "+productCnt.getTotalProductCnt());
        log.info("게시글개수: " + boardCnt);

        model.addAttribute("profit",profit.getTotalProfit());
        model.addAttribute("memberCnt",memberCnt.getTotalMemberCnt());
        model.addAttribute("delMemCnt",delMemCnt.getTotalDeleteMemCnt());
        model.addAttribute("productCnt",productCnt.getTotalProductCnt());
        model.addAttribute("boardCnt",boardCnt.size());
    }

}
