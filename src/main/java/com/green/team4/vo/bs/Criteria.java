package com.green.team4.vo.bs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@ToString
@Getter
public class Criteria {
    private Long page;
    private Long pageNum;

    public Criteria(){
        this.page = 1L;
        this.pageNum = 10L;
    }

    public Criteria(Long page, Long pageNum){
        if(page<0){
            page=1L;
        }
        if(pageNum<=0){
            pageNum=10L;
        }
        this.page = page;
        this.pageNum = pageNum;
    }

}
