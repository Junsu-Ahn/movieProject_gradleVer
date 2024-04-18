package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Scanner;

public class myPage extends Main {
    private static Scanner scanner = new Scanner(System.in);

    public static void fix() {
        System.out.print("비밀번호를 입력하세요: ");
        String password = scanner.nextLine();
        Member currentMember = members.get(currentMemberIdx);

        if (!currentMember.getLoginPw().equals(password)) {
            System.out.println("비밀번호가 일치하지 않습니다.");
            return;
        }

        try {
            // Connect to the database
            dbConnection.connect();
            Connection connection = dbConnection.getConnection();

            System.out.println("변경할 정보를 선택하세요:");
            System.out.println("1. 비밀번호");
            System.out.println("2. 이름");
            String fixOption = scanner.nextLine();

            switch (fixOption) {
                case "1":
                case "비밀번호":
                    System.out.print("새로운 비밀번호 입력: ");
                    String newPassword = scanner.nextLine();
                    System.out.print("비밀번호 확인: ");
                    String newPasswordConfirm = scanner.nextLine();
                    if (!newPassword.equals(newPasswordConfirm)) {
                        System.out.println("비밀번호가 일치하지 않습니다.");
                        return;
                    }

                    // Prepare SQL statement
                    String updatePasswordSQL = "UPDATE member SET loginPw = ? WHERE id = ?";
                    try (PreparedStatement statement = connection.prepareStatement(updatePasswordSQL)) {
                        // Set parameters
                        statement.setString(1, newPassword);
                        statement.setInt(2, currentMember.getId());

                        // Execute SQL statement
                        int rowsAffected = statement.executeUpdate();
                        if (rowsAffected == 1) {
                            System.out.println("비밀번호 변경이 완료되었습니다. 다시 로그인하세요.");
                            logout();
                        } else {
                            System.out.println("비밀번호 변경에 실패하였습니다.");
                        }
                    } catch (SQLException e) {
                        System.err.println("SQL 예외 발생: " + e.getMessage());
                    }
                    break;
                case "2":
                case "이름":
                    System.out.print("새로운 이름을 입력하세요: ");
                    String newName = scanner.nextLine();

                    // Prepare SQL statement
                    String updateNameSQL = "UPDATE member SET name = ? WHERE id = ?";
                    try (PreparedStatement statement = connection.prepareStatement(updateNameSQL)) {
                        // Set parameters
                        statement.setString(1, newName);
                        statement.setInt(2, currentMember.getId());

                        // Execute SQL statement
                        int rowsAffected = statement.executeUpdate();
                        if (rowsAffected == 1) {
                            System.out.println("이름 변경이 완료되었습니다.");
                            // Update local member object
                            currentMember.setName(newName);
                        } else {
                            System.out.println("이름 변경에 실패하였습니다.");
                        }
                    } catch (SQLException e) {
                        System.err.println("SQL 예외 발생: " + e.getMessage());
                    }
                    break;
                default:
                    System.out.println("잘못된 옵션 선택");
                    break;
            }

            // Close database connection
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void bye() {
        System.out.print("비밀번호를 입력하세요: ");
        String password = scanner.nextLine();

        Member currentMember = members.get(currentMemberIdx);
        if (!currentMember.getLoginPw().equals(password)) {
            System.out.println("비밀번호가 일치하지 않습니다.");
            return;
        }

        try {
            // Connect to the database
            dbConnection.connect();
            Connection connection = dbConnection.getConnection();

            // Prepare SQL statement
            String sql = "DELETE FROM member WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                // Set parameter
                statement.setInt(1, currentMember.getId());

                // Execute SQL statement
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected == 1) {
                    System.out.println("탈퇴가 성공적으로 완료되었습니다.");
                } else {
                    System.out.println("탈퇴에 실패하였습니다.");
                }
            } catch (SQLException e) {
                System.err.println("SQL 예외 발생: " + e.getMessage());
            } finally {
                // Close database connection
                connection.close();
            }

            members.remove(currentMemberIdx);
            System.out.println("탈퇴가 완료되었습니다.");

            isLogin = false;
            currentMemberIdx = -1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void cancelReservation() {
        Member currentMember = members.get(currentMemberIdx);
        if (currentMember.getMyMovie().isEmpty()) {
            System.out.println("예매 중인 영화가 없습니다.");
            return;
        }

        System.out.println("예매 현황 : ");
        for (String movieTitle : currentMember.getMyMovie().keySet()) {
            System.out.println(movieTitle);
        }

        System.out.print("취소할 영화 제목: ");
        String movieTitle = scanner.nextLine();

        for (MovieInfo movie : movieList) {
            if (movie.getTitle().equals(movieTitle)) {
                Map<String, Integer> myMovie = currentMember.getMyMovie();
                if (!myMovie.containsKey(movieTitle)) {
                    System.out.println("해당 영화를 예매하지 않았습니다.");
                    return;
                }

                try {
                    // Connect to the database
                    dbConnection.connect();
                    Connection connection = dbConnection.getConnection();

                    // Delete reservation from the database
                    String deleteReservationSQL = "DELETE FROM member_reservation WHERE member_id = ? AND movie_id = ?";
                    try (PreparedStatement statement = connection.prepareStatement(deleteReservationSQL)) {
                        statement.setInt(1, currentMember.getId());
                        statement.setInt(2, movie.getId());
                        int rowsAffected = statement.executeUpdate();

                        if (rowsAffected == 1) {
                            System.out.println("예매가 취소되었습니다.");
                        } else {
                            System.out.println("예매 취소에 실패하였습니다.");
                        }
                    } catch (SQLException e) {
                        System.err.println("SQL 예외 발생: " + e.getMessage());
                    } finally {
                        // Close database connection
                        connection.close();
                    }

                    // Update remaining seats in memory
                    String[] remainingSeats = movie.getRemainingSeats();
                    for (int i = 0; i < remainingSeats.length; i++) {
                        if (remainingSeats[i].equals("X")) {
                            remainingSeats[i] = Integer.toString(i + 1);
                            break;
                        }
                    }
                    return;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("해당 영화를 찾을 수 없습니다.");
    }


    public static void cancelReview() {
        if (members.get(currentMemberIdx).getMyReview().size() == 0) {
            System.out.println("작성한 리뷰가 없습니다.");
            return;
        }

        try {
            // Connect to the database
            dbConnection.connect();
            Connection connection = dbConnection.getConnection();

            System.out.println("나의 리뷰 현황 : ");
            for (Map.Entry<String, Integer> entry : members.get(currentMemberIdx).getMyReview().entrySet()) {
                System.out.println(entry.getKey() + "  " + entry.getValue());
            }
            System.out.print("삭제할 리뷰의 영화제목 입력 : ");
            String movieTitle = scanner.nextLine();

            // Prepare SQL statement to delete review
            String deleteReviewSQL = "DELETE FROM my_review WHERE member_id = ? AND movie_title = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteReviewSQL)) {
                // Set parameters
                statement.setInt(1, members.get(currentMemberIdx).getId());
                statement.setString(2, movieTitle);

                // Execute SQL statement
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected == 0) {
                    System.out.println("해당 영화에 대한 리뷰를 찾을 수 없습니다.");
                    return;
                }

                // Update local data
                for (MovieInfo movie : movieList) {
                    if (movie.getTitle().equals(movieTitle)) {
                        int rating = members.get(currentMemberIdx).getMyReview().get(movieTitle);
                        movie.minusRating(rating);
                        members.get(currentMemberIdx).getMyReview().remove(movieTitle);
                        break;
                    }
                }

                System.out.println("리뷰 삭제가 완료되었습니다.");
                System.out.println("나의 리뷰 현황 : ");
                for (Map.Entry<String, Integer> entry : members.get(currentMemberIdx).getMyReview().entrySet()) {
                    System.out.println(entry.getKey() + "  " + entry.getValue());
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

                try {
                    // Connect to the database
                    dbConnection.connect();
                    Connection connection = dbConnection.getConnection();

                    // Prepare SQL statement to update rating and total_ratings
                    String updateRatingSQL = "UPDATE movie_info SET rating_"
                            + rating + " = rating_" + rating + " + 1, total_ratings " +
                            "= (rating_1 + 2 * rating_2 + 3 * rating_3 + 4 * rating_4 + 5 * rating_5) / 5 WHERE title = ?";
                    try (PreparedStatement statement = connection.prepareStatement(updateRatingSQL)) {
                        // Set parameter
                        statement.setString(1, movieTitle);

                        // Execute SQL statement
                        int rowsAffected = statement.executeUpdate();
                        if (rowsAffected == 1) {
                            System.out.println("리뷰가 작성되었습니다.");

                            // myReview에 추가
                            members.get(currentMemberIdx).getMyReview().put(movieTitle, rating);

                            // 예매 현황에서 삭제
                            members.get(currentMemberIdx).myMovie.remove(movieTitle);
                        } else {
                            System.out.println("리뷰 작성에 실패하였습니다.");
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
        }
        System.out.println("해당 영화를 찾을 수 없습니다.");
    }

}

