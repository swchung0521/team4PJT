package com.green.team4.service.admin;

import com.github.pagehelper.PageHelper;
import com.green.team4.mapper.admin.MailMapper;
import com.green.team4.mapper.mypage.MemberInfoMapper;
import com.green.team4.vo.admin.MailVO;
import com.green.team4.vo.mypage.MemberInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class MailService {
    @Autowired
    private JavaMailSender javaMailSender;

    private final MemberInfoMapper memberInfoMapper;
    private final MailMapper mailMapper;

    public int sendMail(MailVO vo){
        ArrayList<String> toUserList = new ArrayList<>();
        toUserList.add(vo.getEmail());
        int toUserSize = toUserList.size();
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo((String[])
                toUserList.toArray(new String[toUserSize]));
        simpleMailMessage.setSubject(vo.getSubject());
        simpleMailMessage.setText(vo.getText());
        javaMailSender.send(simpleMailMessage);
        return mailMapper.insert(vo);
    }

    public List<MailVO> page(int pageNum) {
        PageHelper.startPage(pageNum,10);
        List<MailVO> mailList = mailMapper.getAll();
        mailList.forEach(System.out::println);
        return mailList;
    }
}
