package com.example.klb_pda;

import java.io.Serializable;

public class CheckDT_List implements Serializable  {
    private final String xqr230b_02;
    private final Integer xqr230b_03;
    private final String xqr230b_04;
    private final String xqr230b_05;
    private final String xqr230b_09;
    private String xqr230b_06;
    private final String xqr230b_07;
    private final String xqr230b_08;
    private final Integer xqr230b_10;
    private Integer xqr230b_01;

    public String getXqr230b_02() {
        return xqr230b_02;
    }

    public Integer getXqr230b_03() {
        return xqr230b_03;
    }

    public String getXqr230b_04() {
        return xqr230b_04;
    }

    public String getXqr230b_05() {
        return xqr230b_05;
    }

    public String getXqr230b_06() {
        return xqr230b_06;
    }

    public void setXqr230b_06(String xqr230b_06) {
        this.xqr230b_06 = xqr230b_06;
    }

    public String getXqr230b_07() {
        return xqr230b_07;
    }

    public String getXqr230b_08() {
        return xqr230b_08;
    }

    public Integer getXqr230b_10() {
        return xqr230b_10;
    }

    public Integer getXqr230b_01() {
        return xqr230b_01;
    }

    public void setXqr230b_01(Integer xqr230b_01) {
        this.xqr230b_01 = xqr230b_01;
    }

    public String getXqr230b_09() {
        return xqr230b_09;
    }

    public CheckDT_List(Integer xqr230b_01, String xqr230b_02, Integer xqr230b_03, String xqr230b_04,
                        String xqr230b_05, String xqr230b_06, String xqr230b_07, String xqr230b_08, String xqr230b_09, Integer xqr230b_10) {
        this.xqr230b_01=xqr230b_01;
        this.xqr230b_02=xqr230b_02;
        this.xqr230b_03=xqr230b_03;
        this.xqr230b_04=xqr230b_04;
        this.xqr230b_05 = xqr230b_05;
        this.xqr230b_06 = xqr230b_06;
        this.xqr230b_07 = xqr230b_07;
        this.xqr230b_08 = xqr230b_08;
        this.xqr230b_09 = xqr230b_09;
        this.xqr230b_10 = xqr230b_10;
    }

}
