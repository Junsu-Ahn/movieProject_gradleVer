package org.example;

import lombok.Getter;
import lombok.Setter;
import org.example.db.DBConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Member_DB extends Main{
    public int id;
    public String regDate;
    public String loginId;
    public String loginPw;
    public String name;
    
    public DBConnection dbConnection;
    List<Member_DB> member_db;
    public Member_DB(int id, String regDate, String loginId, String loginPw, String name){
        this.id = id;
        this.regDate = regDate;
        this.loginId = loginId;
        this.loginPw = loginPw;
        this.name = name;
        member_db = new ArrayList<>();
        dbConnection = Main.getDBConnection();
    }

    public Member_DB(Map<String, Object> row) {
        super();
    }

    public List<Member_DB> getMembers(){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT * FROM `member`"));
        
        List<Map<String, Object>> rows = dbConnection.selectRows((sb.toString()));
        for(Map<String,Object> row : rows){
            member_db.add(new Member_DB(row));
        }
        return member_db;
    }
    
}
