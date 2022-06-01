package com.green.team4.service.sw;

import com.green.team4.mapper.sb.ProductOptMapper;
import com.green.team4.mapper.sw.ExchangeFilesMapper;
import com.green.team4.mapper.sw.ExchangeMapper;
import com.green.team4.mapper.sw.MemberInfoMapper;
import com.green.team4.vo.JH.Product_optVO;
import com.green.team4.vo.sw.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Log4j2
@RequiredArgsConstructor
public class ExchangeServiceImpl implements ExchangeService{

    // 의존성 주입 (첨부파일 Mapper 여기서 한번에 작업)
    private final ExchangeMapper exchangeMapper;
    private final ExchangeFilesMapper exchangeFilesMapper;
    private final OrderService orderService;
    private final MemberInfoMapper memberInfoMapper;
    private final ProductOptMapper productOptMapper;



    // 내부 사용 메서드 -------------------------------------------------------------------------

    private String getNewOrderNum(){ // 주문날짜 신규생성

        // 날짜 문자열 생성
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        String dateStr = dateFormat.format(date);
        log.info("생성된 날짜 str: "+dateStr);

        // 난수 생성 (10자리)
        String randNum = "";
        for (int i = 0; i < 10; i++) {
            randNum += String.valueOf((int)Math.floor(Math.random()*8));
        }

        // 주문번호 조합 생성
        String orderNum = dateStr+randNum;
        log.info("새로 생성된 주문번호: "+orderNum);

        return orderNum;
    }

    private OrderVO getNewOrderVO(OrderVO oldVO, int pno){ // 신규 주문서 발행 (취소 주문 건(여러 상품 중 일부만 취소한 경우))

        // (1) 취소 주문상품을 제외한 '업데이트 주문상품 List' 생성
        List<OrderItemVO> newItemlist = oldVO.getOrderItemList().stream().filter(i->i.getPno()!=pno).collect(Collectors.toList());
        log.info("신규 생성된 주문상품 list: "+newItemlist);

        // (2) 주문서 결제정보 업데이트에 활용할 정보 준비
        int tShipFee = oldVO.getTShipFee(); // 배송비
        int tUsePoint = oldVO.getTUsePoint(); // 사용한 포인트
        int tProductPrice = 0; // 상품 총 금액
        for(OrderItemVO item : newItemlist){
            tProductPrice += item.getITotalPrice();
        }
        int tTotalPrice = tProductPrice + tShipFee - tUsePoint; // 총 결제금액
//        int tSavePoint = (int) (tProductPrice*0.05); // 적립포인트 다시 산출
        int tSavePoint = 0;
        for(OrderItemVO item : newItemlist){
            tSavePoint += item.getISavePoint();
        }

        // (3) 주문서 신규 생성
        OrderVO newOrderVO = new OrderVO();
        // -- 주문서 기본정보 업데이트 --
        newOrderVO.setMno(oldVO.getMno());
        String newOno = getNewOrderNum(); // 주문번호 신규 생성
        newOrderVO.setOno(newOno);
        newOrderVO.setOrderDate(LocalDateTime.now()); // 주문일자 업데이트
        newOrderVO.setReceiverName(oldVO.getReceiverName());
        newOrderVO.setReceiverPhone(oldVO.getReceiverPhone());
        newOrderVO.setPostcode(oldVO.getPostcode());
        newOrderVO.setAddress(oldVO.getAddress());
        newOrderVO.setDetailAddress(oldVO.getDetailAddress());
        newOrderVO.setMessage(oldVO.getMessage());
        // -- 주문서 결제정보 업데이트 --
        newOrderVO.setTProductPrice(tProductPrice);
        newOrderVO.setTShipFee(tShipFee);
        newOrderVO.setTUsePoint(tUsePoint);
        newOrderVO.setTTotalPrice(tTotalPrice);
        newOrderVO.setTSavePoint(tSavePoint); // 적립 포인트
        // -- 주문상품 List 업데이트 --
        for(OrderItemVO nItem : newItemlist){
            nItem.setOno(newOno);
        }
        newOrderVO.setOrderItemList(newItemlist);

        return newOrderVO;
    }

    private OrderVO getNewOrderVOForChange(OrderVO oldVO, int pno, ExchangeVO exchangeVO){ // 신규 주문서 발행 (교환 건)

        // (1) 교환 내역 반영한 '신규 주문상품 List' 생성
        List<OrderItemVO> newItemlist = oldVO.getOrderItemList(); // 기존 주문상품 List
        newItemlist.forEach(item->{ // 기존 주문상품 List에서 하나씩 꺼내어
            if(item.getPno() == pno){ // 교환신청 대상 상품과 동일한 상품 찾아서
                item.setIOption(exchangeVO.getEOption()); // 옵션1 내용 교환 신청한 내용으로 최신화
                item.setIOption2(exchangeVO.getEOption2()); // 옵션2 내용 교환 신청한 내용으로 최신화
                item.setIColor(exchangeVO.getColor()); // 색상 옵션 신청한 내용으로 최신화
            }
        });
        log.info("신규 생성된 주문상품 list: "+newItemlist);

        // (2) 주문서 신규 생성
        OrderVO newOrderVO = new OrderVO();
        // -- 주문서 기본정보 업데이트 --
        newOrderVO.setMno(oldVO.getMno());
        String newOno = getNewOrderNum(); // 주문번호 신규 생성
        newOrderVO.setOno(newOno);
        newOrderVO.setOrderDate(LocalDateTime.now()); // 주문일자 업데이트
        newOrderVO.setReceiverName(oldVO.getReceiverName());
        newOrderVO.setReceiverPhone(oldVO.getReceiverPhone());
        newOrderVO.setPostcode(oldVO.getPostcode());
        newOrderVO.setAddress(oldVO.getAddress());
        newOrderVO.setDetailAddress(oldVO.getDetailAddress());
        newOrderVO.setMessage(oldVO.getMessage());
        // -- 주문서 결제정보 업데이트 --
        newOrderVO.setTProductPrice(oldVO.getTProductPrice());
        newOrderVO.setTShipFee(oldVO.getTShipFee());
        newOrderVO.setTUsePoint(oldVO.getTUsePoint());
        newOrderVO.setTTotalPrice(oldVO.getTTotalPrice());
        newOrderVO.setTSavePoint(oldVO.getTSavePoint()); // 적립 포인트
        // -- 주문상품 List 업데이트 --
        for(OrderItemVO nItem : newItemlist){
            nItem.setOno(newOno); // 주문상품 각 정보에 주문번호를 새 번호로 매핑
        }
        newOrderVO.setOrderItemList(newItemlist);

        return newOrderVO;
    }

    // Service -------------------------------------------------------------------------

    @Override
    public int register(ExchangeVO exchangeVO) { // 취소/반품/교환 신규 등록
        log.info("ExchangeService => register 실행 => 받은 exchangeVO: "+exchangeVO);

        // 신청서 등록
        int result = exchangeMapper.insert(exchangeVO);
        int key = exchangeVO.getEno();
        log.info("key: "+key);

        // 신청서 첨부파일 등록
        if(exchangeVO.getExchangeFilesList() != null){
            exchangeVO.getExchangeFilesList().forEach(file->{
                file.setEno(key); // 신청서 DB 등록된 PK 바로 가져와서 이미지 데이터에 저장
                log.info("등록 파일: "+file);
                exchangeFilesMapper.insert(file);
            });
        }
        return result;
    }

    @Override
    public List<ExchangeVO> readAll(int mno) { // 취소/반품/교환 내역 모두 가져오기(mno단위)
        log.info("ExchangeService => readAll 실행 => 받은 mno: "+mno);

        // 신청서 모두 가져오기
        List<ExchangeVO> exchangeList = exchangeMapper.getAll(mno);
        log.info("ExchangeService => readAll 실행 후 받은 exchangeList: "+exchangeList);

        // 신청서 첨부파일 모두 가져오기
        List<ExchangeVO> resultList = new ArrayList<>();

        exchangeList.forEach(exchangeVO -> {
            List<ExchangeFilesVO> fileList = exchangeFilesMapper.getAll(exchangeVO.getEno()); // 신청서 첨부파일 가져오기
            exchangeVO.setExchangeFilesList(fileList); // 신청서에 가져온 첨부파일 저장
            resultList.add(exchangeVO); // 신청서List에 저장
        });

        return resultList;
    }

    @Override
    public List<ExchangeVO> readAllAdmin() { // 취소/반품/교환 내역 모두 가져오기
        log.info("ExchangeService => readAllAdmin 실행");

        // 신청서 모두 가져오기
        List<ExchangeVO> exchangeList = exchangeMapper.getAllAdmin();
        log.info("ExchangeService => readAllAdmin 실행 후 받은 exList: "+exchangeList);

        // 신청서 첨부파일 모두 가져오기
        List<ExchangeVO> resultList = new ArrayList<>();

        exchangeList.forEach(exchangeVO -> {
            List<ExchangeFilesVO> fileList = exchangeFilesMapper.getAll(exchangeVO.getEno()); // 신청서 첨부파일 가져오기
            exchangeVO.setExchangeFilesList(fileList); // 신청서에 가져온 첨부파일 저장
            resultList.add(exchangeVO); // 신청서List에 저장
        });

        return resultList;
    }

    @Override
    public ExchangeVO readOne(int eno) { // 취소/반품/교환 내역 하나 가져오기

        // 신청서 하나 가져오기
        log.info("ExchangeService => readOne 실행 => 받은 eno: "+eno);
        ExchangeVO exchangeVO = exchangeMapper.getOne(eno);
        log.info("ExchangeService => readOne 실행 후 받은 exchangeVO: "+exchangeVO);

        // 신청서 첨부파일 가져오기
        List<ExchangeFilesVO> fileList = exchangeFilesMapper.getAll(eno);

        // 신청서에 가져온 첨부파일 저장
        exchangeVO.setExchangeFilesList(fileList);

        return exchangeVO;
    }

    @Override
    public int modify(ExchangeVO exchangeVO) { // 취소/반품/교환 내역 수정
        log.info("ExchangeService => modify 실행 => 받은 exchangeVO: "+exchangeVO);
        int result = exchangeMapper.update(exchangeVO);
        log.info("ExchangeService => modify 실행 후 수정된 데이터 개수: "+result);
        return result;
    }

    @Override
    public int remove(int eno) { // 취소/반품/교환 내역 삭제
        log.info("ExchangeService => remove 실행 => 받은 eno: "+eno);

        // 신청서 첨부파일 삭제
        exchangeFilesMapper.delete(eno);

        // 신청서 삭제
        int result = exchangeMapper.delete(eno);
        log.info("ExchangeService => remove 실행 후 삭제된 데이터 개수: "+result);

        return result;
    }

    @Transactional
    @Override
    public void cancelAndReturn(String ono, int pno, int eno) { // 취소/반품 처리 진행
        log.info("ExchangeService => cancelAndReturn 실행 => 받은 ono: "+ono);
        log.info("ExchangeService => cancelAndReturn 실행 => 받은 pno: "+pno);
        log.info("ExchangeService => cancelAndReturn 실행 => 받은 eno: "+eno);

        // 해당 주문서 상태 변경 및 새로운 주문서 발행
        // (1) 기존 주문서 취소처리
        OrderVO orderVO = orderService.readOne(ono); // 해당 주문서 가져오기
        orderVO.setTPayStatus("주문취소완료");
        orderService.modifyStatus(orderVO);

        // (2) 해당 고객 기존 적립포인트 회수
        MemberInfoVO memberInfoVO = memberInfoMapper.getOne(orderVO.getMno());
        int modPoint = memberInfoVO.getPoint() - orderVO.getTSavePoint(); // 회원 총 포인트 - 해당주문 적립 포인트
        memberInfoVO.setPoint(modPoint); // 회원 총 보유포인트 업데이트
        memberInfoMapper.updateByAdmin(memberInfoVO); // DB에 수정된 회원정보 저장

        // (3) 취소 상품 재고 현황 원상복구
        List<Product_optVO> optList = productOptMapper.getProductOption(pno); // 취소상품 옵션 모두가져오기
        List<OrderItemVO> cItemList = orderVO.getOrderItemList().stream().filter(i->i.getPno()==pno).collect(Collectors.toList()); // 취소 주문상품 가져오기
        log.info("optList: "+optList);
        log.info("cItemList: "+cItemList);
        List<Product_optVO> trgOptList = optList.stream().filter( // 취소 주문상품 옵션에 해당하는 옵션 테이블 데이터 필터
                opt->(String.valueOf(opt.getPOption())).equals(String.valueOf(cItemList.get(0).getIOption()))
                    && (String.valueOf(opt.getPOption2())).equals(String.valueOf(cItemList.get(0).getIOption2()))
                    && (String.valueOf(opt.getPColor())).equals(String.valueOf(cItemList.get(0).getIColor()))
        ).collect(Collectors.toList());
        log.info("trgOptList: "+trgOptList);

        int cancelCnt = cItemList.get(0).getICount(); // 취소할 수량
        int originalCnt = trgOptList.get(0).getPAmount(); // 기존 재고

        trgOptList.get(0).setPAmount(originalCnt + cancelCnt); // 원복수량 = 기존 수량 + 취소 수량
        productOptMapper.updateAmt(trgOptList.get(0)); // DB에 재고 업데이트하여 저장


        if(orderVO.getOrderItemList().size()>1){ // 1주문에 2개 상품이상이 있는 건이면 아래 실행
            // (4) 새로운 주문서 발행 *취소 상품 제외한 나머지 상품 포함한 주문서로 재발행
            OrderVO newOrderVO = getNewOrderVO(orderVO,pno); // 신규 발행한 주문서
            orderService.register(newOrderVO); // DB에 새 주문서 등록

            ExchangeVO exVO = exchangeMapper.getOne(eno); // 기존 취소/반품 신청서 가져오기
            exVO.setNewOno(newOrderVO.getOno()); // 신규발행 주문번호 반영
            exchangeMapper.update(exVO); // DB에 반영

            // (5) 새로 산출한 적립포인트 회원정보에 업데이트
            MemberInfoVO newMemberInfoVO = memberInfoMapper.getOne(orderVO.getMno()); // 해당 회원정보 가져오기
            int oriPoint = newMemberInfoVO.getPoint(); // 기존 회원보유 포인트(취소에 따른 차감 반영 후)
            int newAddPoint = newOrderVO.getTSavePoint(); // 새로 다시 추가해야할 적립포인트
            int updatePoint = oriPoint + newAddPoint; // 새로 다시 적립후 최종 보유 포인트
            newMemberInfoVO.setPoint(updatePoint); // 해당 회원 포인트 현황 업데이트
            memberInfoMapper.updateByAdmin(newMemberInfoVO); // DB에 포인트 현황 업데이트한 회원정보 저장


        }

        // (6) 취소 신청 테이블에서 해당 취소신청 건 진행상태 변경
        ExchangeVO exchangeVO = exchangeMapper.getOne(eno);
        exchangeVO.setExStatus("취소완료");
        exchangeVO.setExEndDate(LocalDateTime.now());
        exchangeMapper.update(exchangeVO);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public OrderVO change(String ono, int pno, int eno) { // 교환 처리 진행
        log.info("ExchangeService => change 실행 => 받은 ono: "+ono);
        log.info("ExchangeService => change 실행 => 받은 pno: "+pno);
        log.info("ExchangeService => change 실행 => 받은 eno: "+eno);

        // 해당 주문서 상태 변경 및 새로운 주문서 발행
        // (1) 기존 주문서 취소처리
        OrderVO orderVO = orderService.readOne(ono); // 해당 주문서 가져오기
        orderVO.setTPayStatus("주문교환완료");
        orderService.modifyStatus(orderVO);

        // (2) 해당 고객 기존 적립포인트는 변동X ** 동일 가격 상품에서 옵션만 변경되므로 적립포인트는 변동X

        // (3) 교환(기존) 상품 재고 현황 원상복구
        List<Product_optVO> optList = productOptMapper.getProductOption(pno); // 교환상품 옵션 모두가져오기
        List<OrderItemVO> cItemList = orderVO.getOrderItemList().stream().filter(i->i.getPno()==pno).collect(Collectors.toList()); // 교환 주문상품 가져오기
        log.info("optList: "+optList);
        log.info("cItemList: "+cItemList);
        log.info("교환신청 옵션"+cItemList.get(0));

        List<Product_optVO> trgOptList = optList.stream().filter( // 교환(기존) 주문상품 옵션에 해당하는 옵션 테이블 데이터 필터
                opt->(String.valueOf(opt.getPOption())).equals(String.valueOf(cItemList.get(0).getIOption()))
                    && (String.valueOf(opt.getPOption2())).equals(String.valueOf(cItemList.get(0).getIOption2()))
                    && (String.valueOf(opt.getPColor())).equals(String.valueOf(cItemList.get(0).getIColor()))
        ).collect(Collectors.toList());
        log.info("넘어오나아");
        log.info("trgOptList: "+trgOptList);

        int cancelCnt = cItemList.get(0).getICount(); // 원상복구해야할 수량(교환(기존) 수량)
        int originalCnt = trgOptList.get(0).getPAmount(); // 기존 재고
        log.info("원복예상 수량: "+cancelCnt);
        log.info("기존 총 수량: "+originalCnt);

        trgOptList.get(0).setPAmount(originalCnt + cancelCnt); // 원복수량 = 기존 수량 + 교환(기존) 수량
        productOptMapper.updateAmt(trgOptList.get(0)); // DB에 재고 업데이트하여 저장

        // (4) 교환(변경) 상품 재고 현황 업데이트(차감)
        log.info("교환(변경) 상품 재고 업데이트 프로세스 진행");
        ExchangeVO trgExVO = exchangeMapper.getOne(eno); // 교환신청서 가져오기(안에 교환신청 상품 옵션 활용)
        List<Product_optVO> newTrgOptList = optList.stream().filter(
                opt->(String.valueOf(opt.getPOption())).equals(String.valueOf(trgExVO.getEOption()))
                    && (String.valueOf(opt.getPOption2())).equals(String.valueOf(trgExVO.getEOption2()))
                    && (String.valueOf(opt.getPColor())).equals(String.valueOf(trgExVO.getColor()))
        ).collect(Collectors.toList());

        int exItemCnt = cancelCnt; // 교환을 위해 반품받은 물건의 수량과 새로 출고해야할 물건 수량은 동일
        int exOriginalCnt = newTrgOptList.get(0).getPAmount(); // 출고 될 상품의 기존 재고

        log.info("차감예상 수량: "+exItemCnt);
        log.info("기존 총 수량: "+exOriginalCnt);

        newTrgOptList.get(0).setPAmount(exOriginalCnt - exItemCnt); // 교환(변경) 옵션 차감한 재고로 업데이트
        productOptMapper.updateAmt(newTrgOptList.get(0)); // DB에 재고 업데이트하여 저장

        // (5) 새로운 주문서 발행 *교환 내역 반영한 최신 주문서로 재발행
        OrderVO newOrderVO = getNewOrderVOForChange(orderVO,pno,trgExVO); // 신규 발행한 주문서
        orderService.register(newOrderVO); // DB에 새 주문서 등록

        ExchangeVO exVO = exchangeMapper.getOne(eno); // 기존 교환 신청서 가져오기
        exVO.setNewOno(newOrderVO.getOno()); // 신규발행 주문번호 반영
        exchangeMapper.update(exVO); // DB에 반영

        // (6) 교환 신청 테이블에서 해당 교환신청 건 진행상태 변경
        ExchangeVO exchangeVO = exchangeMapper.getOne(eno);
        exchangeVO.setExStatus("교환완료");
        exchangeVO.setExEndDate(LocalDateTime.now());
        exchangeMapper.update(exchangeVO);

        // (7) 새로 발행한 주문서 반환(교환배송 등록에 사용)
        return newOrderVO;
    }


}
