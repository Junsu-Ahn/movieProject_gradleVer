package org.example;

import org.example.db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

class Member{
    public int id;
    public String loginId;
    public String loginPw;
    public String name;
    public Map<String, Integer> myMovie;
    public Map<String, Integer> myReview;

    public Member(int id, String loginId, String loginPw, String name) {
        this.id = id;
        this.loginId = loginId;
        this.loginPw = loginPw;
        this.name = name;
        this.myMovie = new HashMap<>();
        this.myReview = new HashMap<>();
    }

    public void saveToDatabase(DBConnection dbConnection) {
        String sql = "INSERT INTO member (loginId, loginPw, name) VALUES (?, ?, ?)";

        try {
            Connection connection = dbConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, this.loginId);
            statement.setString(2, this.loginPw);
            statement.setString(3, this.name);
            statement.executeUpdate();
            statement.close(); // Statement를 닫지 않고 재사용할 수 있도록 수정

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public String toString() {
        return "Member{" +
                "id=" + id +
                ", loginId='" + loginId + '\'' +
                ", loginPw='" + loginPw + '\'' +
                ", name='" + name + '\'' +
                '}';
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

    public void setName(String newName) {
        this.name = newName;
    }
}
