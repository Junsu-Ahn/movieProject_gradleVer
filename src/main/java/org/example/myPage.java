package org.example;

import java.util.Map;
import java.util.Scanner;

public class myPage extends Main {
    private static Scanner scanner = new Scanner(System.in);

    public static void fix() {
        System.out.print("비밀번호를 입력하세요 : ");
        String fix_data = scanner.nextLine();
        if (members.get(currentMemberIdx).getLoginPw().equals(fix_data)) {
            System.out.println("변경할 정보를 선택하세요 : ");
            System.out.println("1. 비밀번호");
            System.out.println("2. 이름");
            fix_data = scanner.nextLine();
            switch (fix_data) {
                case "1":
                case "비밀번호":
                    System.out.print("새로운 비밀번호 입력 : ");
                    fix_data = scanner.nextLine();
                    System.out.print("비밀번호 확인 : ");
                    String fix_data2 = scanner.nextLine();
                    if (!fix_data.equals(fix_data2)) {
                        System.out.println("비밀번호가 같지 않습니다");
                        break;
                    }
                    members.get(currentMemberIdx).loginPw = fix_data;
                    System.out.println("변경 완료. 다시 로그인 하세요.");
                    logout();
                    break;
                case "2":
                case "이름":
                    System.out.print("새로운 이름을 입력하세요 : ");
                    fix_data = scanner.nextLine();
                    members.get(currentMemberIdx).name = fix_data;
                    System.out.println("변경 완료");
                    break;
                default:
                    System.out.println("잘못된 옵션 선택");
                    break;
            }
        } else
            System.out.println("비밀번호가 일치하지 않습니다");
    }

    public static void bye() {

        System.out.print("비밀번호를 입력하세요: ");
        String password = scanner.nextLine();

        Member currentMember = members.get(currentMemberIdx);
        if (!currentMember.getLoginPw().equals(password)) {
            System.out.println("비밀번호가 일치하지 않습니다.");
            return;
        }

        members.remove(currentMemberIdx);
        System.out.println("탈퇴가 완료되었습니다.");

        for(int i = currentMemberIdx; i < members.size(); i++)
        {
            members.get(i).id--;
        }

        isLogin = false;
        currentMemberIdx = -1;
    }

    public static void cancelReservation() {
        if (members.get(currentMemberIdx).myReview.size() == 0) {
            System.out.println("예매 중인 영화가 없습니다.");
            return;
        }
        System.out.println("예매 현황 : ");
        for (String x : members.get(currentMemberIdx).getMyMovie().keySet())
            System.out.println(x);
        System.out.print("취소할 영화 제목: ");
        String movieTitle = scanner.nextLine();
        for (MovieInfo movie : movieList) {
            if (movie.getTitle().equals(movieTitle)) {
                Map<String, Integer> myMovie = members.get(currentMemberIdx).getMyMovie();
                if (!myMovie.containsKey(movieTitle)) {
                    System.out.println("해당 영화를 예매하지 않았습니다.");
                    return;
                }
                myMovie.remove(movieTitle);
                String[] remainingSeats = movie.getRemainingSeats();
                for (int i = 0; i < remainingSeats.length; i++) {
                    if (remainingSeats[i].equals("X")) {
                        remainingSeats[i] = Integer.toString(i + 1);
                        break;
                    }
                }
                System.out.println("예매가 취소되었습니다.");
                return;
            }
        }
        System.out.println("해당 영화를 찾을 수 없습니다.");
    }

    public static void cancelReview() {
        if (members.get(currentMemberIdx).myReview.size() == 0) {
            System.out.println("작성한 리뷰가 없습니다.");
            return;
        }

        System.out.println("나의 리뷰 현황 : ");
        for (Map.Entry<String, Integer> entry : members.get(currentMemberIdx).getMyReview().entrySet()) {
            System.out.println(entry.getKey() + "  " + entry.getValue());
        }
        System.out.print("삭제할 리뷰의 영화제목 입력 : ");
        String x = scanner.nextLine();
        for (MovieInfo movie : movieList) {
            if (movie.getTitle().equals(x)) {
                movie.minusRating(members.get(currentMemberIdx).getMyReview().get(x));
                members.get(currentMemberIdx).getMyReview().remove(x);
            }
        }
        System.out.println("삭제 완료");
        System.out.println("나의 리뷰 현황 : ");
        for (Map.Entry<String, Integer> entry : members.get(currentMemberIdx).getMyReview().entrySet()) {
            System.out.println(entry.getKey() + "  " + entry.getValue());
        }
    }

    public static void writeReview() {

        System.out.println("예매 현황 : ");
        for (String x : members.get(currentMemberIdx).getMyMovie().keySet())
            System.out.println(x);

        System.out.print("리뷰를 작성할 영화 제목: ");
        String movieTitle = scanner.nextLine();
        for (MovieInfo movie : movieList) {
            if (movie.getTitle().equals(movieTitle)) {
                System.out.print("평점을 입력하세요 (1~5점): ");
                int rating = Integer.parseInt(scanner.nextLine());
                if (rating < 1 || rating > 5) {
                    System.out.println("1부터 5까지의 점수를 입력해주세요.");
                    return;
                }
                movie.addRating(rating);
                System.out.println("리뷰가 작성되었습니다.");

                // 리뷰 작성이 끝나면 영화좌석을 다시 인덱스로 변경
                Map<String, Integer> myMovie = members.get(currentMemberIdx).getMyMovie();
                int selectedSeat = myMovie.get(movieTitle);
                movie.getRemainingSeats()[selectedSeat - 1] = Integer.toString(selectedSeat);

                // myReview에 추가
                members.get(currentMemberIdx).getMyReview().put(movieTitle, rating);

                // 예매 현황에서 삭제
                members.get(currentMemberIdx).myMovie.remove(movieTitle);
            }
        }
        System.out.println("해당 영화를 찾을 수 없습니다.");
    }
}
