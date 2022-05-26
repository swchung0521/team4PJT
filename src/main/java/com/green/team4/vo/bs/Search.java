package com.green.team4.vo.bs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter

public class Search extends Criteria{

    private String searchType;
    private String keyword;

}
