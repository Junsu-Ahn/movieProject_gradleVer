package org.example;

import java.util.HashMap;
import java.util.Map;

class Member extends Main {
    public int id;
    public String regDate;
    public String loginId;
    public String loginPw;
    public String name;
    public Map<String, Integer> myMovie;
    public Map<String, Integer> myReview;

    public Member(int id, String regDate, String loginId, String loginPw, String name) {
        this.id = id;
        this.regDate = regDate;
        this.loginId = loginId;
        this.loginPw = loginPw;
        this.name = name;
        this.myMovie = new HashMap<>();
        this.myReview = new HashMap<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getLoginPw() {
        return loginPw;
    }

    public Map<String, Integer> getMyMovie() {
        return myMovie;
    }

    public Map<String, Integer> getMyReview() {
        return myReview;
    }
}
