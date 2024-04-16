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
import java.util.*;
@Getter
@Setter
public class Main {
    public static DBConnection dbConnection;
    Member_DB member_db;
    public static List<MovieInfo> movieList = new ArrayList<>();
    public static List<Member> members = new ArrayList<>();
    public static Scanner scanner = new Scanner(System.in);
    public static int currentMemberIdx = -1;
    public static boolean isLogin = false;
    public static void main(String[] args) {
        loadMovies();
        makeTestData();

        DBConnection.DB_NAME = "sbs_proj";
        DBConnection.DB_USER = "sbsst";
        DBConnection.DB_PASSWORD = "sbs123414";
        DBConnection.DB_PORT = 3306;



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
    public static DBConnection getDBConnection() {
        if ( dbConnection == null ) {
            dbConnection = new DBConnection();
        }

        return dbConnection;
    }
    private static void loadMovies() {
        String url = "https://search.naver.com/search.naver?where=nexearch&sm=tab_etc&qvt=0&query=%ED%98%84%EC%9E%AC%EC%83%81%EC%98%81%EC%98%81%ED%99%94";
        try {
            Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0").get();
            Elements elements = doc.select(".area_text_box");

            for (Element element : elements) {
                Element link = element.select("a").first();
                if (link != null) {
                    String title = link.text();
                    movieList.add(new MovieInfo(title));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void memberJoin() {
        if(isLogin)
        {
            System.out.print("로그아웃 하시겠습니까? Y/N ");
            String b = scanner.nextLine();
            if(b.toLowerCase().equals("n"))
                return;
        }
        int id = members.size() + 1;
        String regDate = Util.getNowDateStr();
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

        Member member = new Member(id, regDate, loginId, loginPw, name);
        members.add(member);
        System.out.printf("%d번 회원이 생성되었습니다. 환영합니다!\n", id);
        currentMemberIdx = id - 1;
        isLogin = true;
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

        for (MovieInfo movie : movieList) {
            if (movie.getTitle().equals(movieTitle)) {
                System.out.println("예매 가능한 좌석:");
                String[] remainingSeats = movie.getRemainingSeats();
                for (int i = 0; i < remainingSeats.length; i++) {
                    System.out.print(remainingSeats[i] + " ");
                }
                System.out.println();

                System.out.print("좌석 선택: ");
                int selectedSeat = Integer.parseInt(scanner.nextLine());

                if (selectedSeat < 1 || selectedSeat > 10){
                    System.out.println("잘못된 좌석 선택입니다.");
                    return;
                }
                remainingSeats[selectedSeat - 1] = "X";
                members.get(currentMemberIdx).getMyMovie().put(movieTitle, selectedSeat);
                System.out.println("예매가 완료되었습니다.");
                return;
            }
        }
        System.out.println("해당 영화를 찾을 수 없습니다.");
    }

    private static void showMovies() {
        Collections.sort(movieList);
        for (MovieInfo movie : movieList) {
            System.out.printf("%s (%.2f) - ", movie.getTitle(), movie.getRating());
            for (String seat : movie.getRemainingSeats()) {
                System.out.print(seat + " ");
            }
            System.out.println();
        }
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
        Member member = members.get(currentMemberIdx);
        System.out.println(member.getName() + "님의 예매 현황:");
        Map<String, Integer> myMovie = member.getMyMovie();
        for (String movieTitle : myMovie.keySet()) {
            System.out.println(movieTitle + " - 좌석 " + myMovie.get(movieTitle));
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
            int id = members.size() + 1;
            String regDate = Util.getNowDateStr();
            String loginId = "user" + id;
            String loginPw = "password" + id;
            String name = "User" + id;
            members.add(new Member(id, regDate, loginId, loginPw, name));
        }
        currentMemberIdx = members.size()-1;
        isLogin = true;
    }

}



