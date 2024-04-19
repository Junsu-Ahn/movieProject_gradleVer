package org.example;

import lombok.Getter;
import lombok.Setter;
import org.example.db.DBConnection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.example.db.DBConnection;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map;
import java.util.Scanner;
@Getter
@Setter
public class Main {
    public static DBConnection dbConnection;
    public static List<MovieInfo> movieList = new ArrayList<>();
    public static List<Member> members = new ArrayList<>();
    public static List<Member> memberDB = new ArrayList();
    public static Scanner scanner = new Scanner(System.in);
    public static int currentMemberIdx = -1;
    public static boolean isLogin = false;
    public static void main(String[] args) {

        DBConnection.DB_NAME = "sbs_proj";
        DBConnection.DB_USER = "sbsst";
        DBConnection.DB_PASSWORD = "sbs123414";
        DBConnection.DB_PORT = 3306;

        dbConnection = new DBConnection();
        dbConnection.connect();
        makeTestData();
        loadMovies();
        for(int i = 0 ; i < memberDB.size(); i++)
            System.out.println(memberDB.get(i));

        while (true) {
            System.out.print("명령어) ");
            String cmd = scanner.nextLine();

            switch (cmd) {
                case "exit":
                    return;
                case "member join":
                    memberJoin();
                    break;
                case "member list":
                    listMembers();
                    break;
                case "purchase":
                    purchase();
                    break;
                case "show movies":
                    showMovies();
                    break;
                case "login":
                    login();
                    break;
                case "logout":
                    logout();
                    break;
                case "mypage":
                    myPage();
                    break;
                default:
                    System.out.println("올바르지 않은 명령어입니다.");
            }
        }
    }

    public static List<Member> getMemberList() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT * FROM `member`"));

        List<Member> member_db = new ArrayList();
        List<Map<String, Object>> rows = dbConnection.selectRows(sb.toString());

        for(Map<String,Object> row : rows) {
            int id = (int) row.get("id");
            String loginId = (String) row.get("loginId");
            String loginPw = (String) row.get("loginPw");
            String name = (String) row.get("name");

            member_db.add(new Member(id, loginId, loginPw, name));
        }

        return member_db;
    }

    public static DBConnection getDBConnection() {
        if ( dbConnection == null ) {
            dbConnection = new DBConnection();
        }
        return dbConnection;
    }
    private static void loadMovies() {
        String url = "https://search.naver.com/search.naver?where=nexearch&sm=tab_etc&qvt=0&query=%ED%98%84%EC%9E%AC%EC%83%81%EC%98%81%EC%98%81%ED%99%94";
        try {
            // 데이터베이스 연결 열기
            dbConnection.connect();
            Connection connection = dbConnection.getConnection();

            Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0").get();
            Elements elements = doc.select(".area_text_box");

            for (Element element : elements) {
                Element link = element.select("a").first();
                if (link != null) {
                    String title = link.text();
                    MovieInfo movie = new MovieInfo(title);
                    movieList.add(movie);
                    // 데이터베이스에 영화 정보 삽입
                    insertMovieInfoToDB(connection, movie);
                }
            }
            // 데이터베이스 연결 닫기
            connection.close();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertMovieInfoToDB(Connection connection, MovieInfo movie) throws SQLException {
        String sql = "INSERT INTO movie_info (id, title) VALUES (?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            // 파라미터 설정
            statement.setInt(1, movie.getId()); // 직접 id를 지정해줍니다.
            statement.setString(2, movie.getTitle());

            // 쿼리 실행
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 1) {
                System.out.println("영화 정보가 데이터베이스에 성공적으로 삽입되었습니다.");
            } else {
                System.out.println("영화 정보를 데이터베이스에 삽입하는데 실패하였습니다.");
            }
        } catch (SQLException e) {
            System.err.println("SQL 예외 발생: " + e.getMessage());
            throw e; // 예외를 다시 던져서 호출한 곳에서 처리할 수 있도록 함
        }
    }

    private static void memberJoin() {
        if (isLogin) {
            System.out.print("로그아웃 하시겠습니까? Y/N ");
            String b = scanner.nextLine();
            if (b.toLowerCase().equals("n"))
                return;
        }
        // Prompt for member details
        String loginId;
        while (true) {
            System.out.print("로그인 아이디: ");
            loginId = scanner.nextLine();
            if (!isJoinableLoginId(loginId)) {
                System.out.println("이미 사용 중인 아이디입니다.");
            } else {
                break;
            }
        }
        System.out.print("로그인 비밀번호: ");
        String loginPw = scanner.nextLine();
        System.out.print("이름: ");
        String name = scanner.nextLine();

        try {
            // Connect to the database
            dbConnection.connect();
            Connection connection = dbConnection.getConnection();

            // Prepare SQL statement
            String sql = "INSERT INTO member (loginId, loginPw, name) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                // Set parameters
                statement.setString(1, loginId);
                statement.setString(2, loginPw);
                statement.setString(3, name);

                // Execute SQL statement
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected == 1) {
                    System.out.println("회원가입이 성공적으로 완료되었습니다.");
                } else {
                    System.out.println("회원가입에 실패하였습니다.");
                }
            } catch (SQLException e) {
                System.err.println("SQL 예외 발생: " + e.getMessage());
            } finally {
                // Close database connection
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void listMembers() {
        for (Member member : members) {
            System.out.println(member.getId() + "번 회원 - " + member.getName());
        }
    }

    private static void purchase() {
        if (!isLogin) {
            System.out.println("로그인 후 이용해주세요.");
            return;
        }

        System.out.print("예매할 영화 제목: ");
        String movieTitle = scanner.nextLine();

        try {
            // Connect to the database
            dbConnection.connect();
            Connection connection = dbConnection.getConnection();

            // Check if the movie exists
            String findMovieSQL = "SELECT id FROM movie_info WHERE title = ?";
            try (PreparedStatement statement = connection.prepareStatement(findMovieSQL)) {
                statement.setString(1, movieTitle);
                ResultSet resultSet = statement.executeQuery();

                if (!resultSet.next()) {
                    System.out.println("해당 영화를 찾을 수 없습니다.");
                    return;
                }

                int movieId = resultSet.getInt("id");

                // Check remaining seats
                String getRemainingSeatsSQL = "SELECT * FROM movie_info WHERE id = ?";
                try (PreparedStatement seatStatement = connection.prepareStatement(getRemainingSeatsSQL)) {
                    seatStatement.setInt(1, movieId);
                    ResultSet seatResult = seatStatement.executeQuery();

                    if (!seatResult.next()) {
                        System.out.println("예매 정보를 가져오는 데 실패하였습니다.");
                        return;
                    }

                    String[] remainingSeats = new String[10];
                    for (int i = 0; i < 10; i++) {
                        remainingSeats[i] = seatResult.getString("seat_" + (i + 1));
                    }

                    System.out.println("예매 가능한 좌석:");
                    for (int i = 0; i < remainingSeats.length; i++) {
                        if (remainingSeats[i].equals("X")) {
                            System.out.print("X ");
                        } else {
                            System.out.print((i + 1) + " ");
                        }
                    }
                    System.out.println();

                    System.out.print("좌석 선택: ");
                    int selectedSeat = Integer.parseInt(scanner.nextLine());

                    if (selectedSeat < 1 || selectedSeat > 10 || remainingSeats[selectedSeat - 1].equals("X")) {
                        System.out.println("잘못된 좌석 선택입니다.");
                        return;
                    }

                    // Update remaining seats in database
                    String updateSeatsSQL = "UPDATE movie_info SET seat_" + selectedSeat + " = 'X' WHERE id = ?";
                    try (PreparedStatement updateStatement = connection.prepareStatement(updateSeatsSQL)) {
                        updateStatement.setInt(1, movieId);
                        updateStatement.executeUpdate();
                    }

                    // Insert reservation into the database
                    String insertReservationSQL = "UPDATE member SET myMovie = CONCAT(IFNULL(myMovie, ''), ?) WHERE id = ?";
                    try (PreparedStatement insertStatement = connection.prepareStatement(insertReservationSQL)) {
                        // Create a string representation of the movie reservation
                        String movieReservation = movieTitle + ": Seat Number - " + selectedSeat + ";";

                        // Update the myMovie column for the current member
                        insertStatement.setString(1, movieReservation);
                        insertStatement.setInt(2, members.get(currentMemberIdx).getId());

                        int rowsAffected = insertStatement.executeUpdate();
                        if (rowsAffected == 1) {
                            System.out.println("예매가 완료되었습니다.");
                        } else {
                            System.out.println("예매에 실패하였습니다.");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL 예외 발생: " + e.getMessage());
        }
    }


    private static void showMovies() {
        try {
            // Connect to the database
            dbConnection.connect();
            Connection connection = dbConnection.getConnection();

            // Fetch movie information from the database
            String getMoviesSQL = "SELECT title, total_ratings, seat_1, seat_2, seat_3, seat_4, seat_5, seat_6, seat_7, seat_8, seat_9, seat_10 FROM movie_info";
            try (PreparedStatement statement = connection.prepareStatement(getMoviesSQL)) {
                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    String title = resultSet.getString("title");
                    double totalRatings = resultSet.getDouble("total_ratings");
                    String[] remainingSeats = new String[10];
                    for (int i = 0; i < 10; i++) {
                        remainingSeats[i] = resultSet.getString("seat_" + (i + 1));
                    }

                    System.out.print(title + " (" + totalRatings + ") ");
                    for (String seat : remainingSeats) {
                        if (seat.equals("X")) {
                            System.out.print("X ");
                        } else {
                            System.out.print((seat.equals("0") ? 0 : Integer.parseInt(seat)) + " ");
                        }
                    }
                    System.out.println();
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL 예외 발생: " + e.getMessage());
        }
    }


    private static double calculateRating(int rating1, int rating2, int rating3, int rating4, int rating5, int totalRatings) {
        // Calculate total ratings
        int total = rating1 + rating2 + rating3 + rating4 + rating5;

        // Calculate weighted average rating
        double weightedRating = (1 * rating1 + 2 * rating2 + 3 * rating3 + 4 * rating4 + 5 * rating5) / (double)total;

        return weightedRating;
    }


    private static void login() {
        System.out.print("로그인 아이디: ");
        String loginId = scanner.nextLine();
        System.out.print("로그인 비밀번호: ");
        String loginPw = scanner.nextLine();

        for (int i = 0; i < members.size(); i++) {
            Member member = members.get(i);
            if (member.getLoginId().equals(loginId) && member.getLoginPw().equals(loginPw)) {
                currentMemberIdx = i;
                isLogin = true;
                System.out.println("로그인 되었습니다.");
                return;
            }
        }
        System.out.println("로그인 실패. 아이디 또는 비밀번호를 확인해주세요.");
    }
    public static void logout(){
        currentMemberIdx = -1;
        isLogin = false;
    }
    public static void myPage() {
        if (!isLogin) {
            System.out.println("로그인 후 이용해주세요.");
            return;
        }
        Member currentMember = members.get(currentMemberIdx);
        try {
            // Connect to the database
            dbConnection.connect();
            Connection connection = dbConnection.getConnection();

            // Query to retrieve the current member's reservations
            String getReservationsSQL = "SELECT myMovie FROM member WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(getReservationsSQL)) {
                statement.setInt(1, currentMember.getId());
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    // Get the value of myMovie column from the result set
                    String myMovie = resultSet.getString("myMovie");

                    // Check if myMovie is empty or null
                    if (myMovie == null || myMovie.isEmpty()) {
                        System.out.println("예매 중인 영화가 없습니다.");
                        return;
                    }

                    // Split the myMovie string to get individual movie reservations
                    String[] reservations = myMovie.split(";");
                    System.out.println("예매 현황 : ");
                    System.out.println(myMovie);
                }
            }
        }
        catch (SQLException e) {
            System.err.println("SQL 예외 발생: " + e.getMessage());
        }

        while(true)
        {
            System.out.println("메뉴를 선택하세요 : ");
            System.out.println("1. 예매취소");
            System.out.println("2. 리뷰쓰기");
            System.out.println("3. 회원탈퇴");
            System.out.println("4. 리뷰삭제");
            System.out.println("5. 회원정보수정");
            System.out.println("6. 이전으로");
            String m_cmd = scanner.nextLine();

            switch(m_cmd)
            {
                case "1":
                case "예매취소":
                    myPage.cancelReservation();
                    break;
                case "2":
                case "리뷰쓰기":
                    myPage.writeReview();
                    break;
                case "3":
                case "회원탈퇴":
                    myPage.bye();
                    return;
                case "4":
                case "리뷰삭제":
                    myPage.cancelReview();
                    break;
                case "5":
                case "회원정보수정":
                    myPage.fix();
                    break;
                case "6":
                case "이전으로":
                    return;
                default:
                    System.out.println("올바르지 않은 명령어 입니다");
            }
        }
        }


    private static boolean isJoinableLoginId(String loginId) {
        for (Member member : members) {
            if (member.getLoginId().equals(loginId)) {
                return false;
            }
        }
        return true;
    }

    private static void makeTestData() {
        // 회원 가입 테스트 데이터 생성
        for (int i = 0; i < 3; i++) {
            String loginId = "user" + (i + 1);
            String loginPw = "password" + (i + 1);
            String name = "User" + (i + 1);

            // 중복을 체크하지 않고 새로운 회원 정보 생성
            Member member = new Member(i+1, loginId, loginPw, name);
            members.add(member);
            member.saveToDatabase(dbConnection);
            memberDB.add(member);
        }
        currentMemberIdx = members.size() - 1;
        isLogin = true;
    }
}



