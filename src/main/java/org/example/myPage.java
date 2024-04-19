package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
            String deleteSQL = "DELETE FROM member WHERE id = ?";
            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSQL)) {
                // Set parameter
                deleteStatement.setInt(1, currentMember.getId());

                // Execute SQL statement to delete the member
                int rowsAffected = deleteStatement.executeUpdate();
                if (rowsAffected == 1) {
                    System.out.println("탈퇴가 성공적으로 완료되었습니다.");
                } else {
                    System.out.println("탈퇴에 실패하였습니다.");
                    return;
                }
            } catch (SQLException e) {
                System.err.println("SQL 예외 발생: " + e.getMessage());
                return;
            }

            // Shift member IDs for members after the deleted member
            for (int i = currentMemberIdx + 1; i < members.size(); i++) {
                Member member = members.get(i);
                int newId = member.getId() - 1;
                member.id = newId;

                // Update member ID in the database
                String updateSQL = "UPDATE member SET id = ? WHERE id = ?";
                try (PreparedStatement updateStatement = connection.prepareStatement(updateSQL)) {
                    // Set parameters
                    updateStatement.setInt(1, newId);
                    updateStatement.setInt(2, member.getId() + 1);

                    // Execute SQL statement to update member ID
                    updateStatement.executeUpdate();
                } catch (SQLException e) {
                    System.err.println("SQL 예외 발생: " + e.getMessage());
                }
            }

            // Close database connection
            connection.close();

            // Remove the member from the list and reset login status
            members.remove(currentMemberIdx);
            isLogin = false;
            currentMemberIdx = -1;
        } catch (SQLException e) {
            System.err.println("SQL 예외 발생: " + e.getMessage());
        }
    }


    public static void cancelReservation() {
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
                    boolean foundReservation = false;
                    StringBuilder updatedMyMovie = new StringBuilder();

                    System.out.print("취소 할 영화 제목 : ");
                    String movieTitleToCancel = scanner.nextLine();
                    for (String reservation : reservations) {
                        // Extract movie title and seat number from each reservation
                        String[] parts = reservation.split(":");
                        String movieTitle = parts[0];

                        if (!foundReservation && movieTitle.equals(movieTitleToCancel)) {
                            foundReservation = true;
                            continue; // Skip this reservation
                        }

                        updatedMyMovie.append(reservation).append(";"); // Append other reservations
                    }

                    if (!foundReservation) {
                        System.out.println("해당 영화를 예매하지 않았습니다.");
                        return;
                    }

                    // Remove last ';' character from updatedMyMovie
                    String finalMyMovie = updatedMyMovie.toString().replaceAll(";$", "");

                    // Update the reservation in the database
                    String updateReservationSQL = "UPDATE member SET myMovie = ? WHERE id = ?";
                    try (PreparedStatement updateStatement = connection.prepareStatement(updateReservationSQL)) {
                        updateStatement.setString(1, finalMyMovie);
                        updateStatement.setInt(2, currentMember.getId());
                        updateStatement.executeUpdate();
                    }

                    System.out.println("예매가 취소되었습니다.");
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL 예외 발생: " + e.getMessage());
        }
    }

    public static void cancelReview() {
        Member currentMember = members.get(currentMemberIdx);

        try {
            // Connect to the database
            dbConnection.connect();
            Connection connection = dbConnection.getConnection();

            // Query to retrieve the member's reviews
            String getReviewsSQL = "SELECT movie_title FROM my_review WHERE member_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(getReviewsSQL)) {
                statement.setInt(1, currentMember.getId());
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    // Get the value of movie_title column from the result set
                    String movieTitle = resultSet.getString("movie_title");

                    // Check if movieTitle is empty or null
                    if (movieTitle == null || movieTitle.isEmpty()) {
                        System.out.println("작성한 리뷰가 없습니다.");
                        return;
                    }

                    // Output the member's reviews
                    System.out.println("나의 리뷰 현황 : ");
                    do {
                        System.out.println(movieTitle);
                    } while (resultSet.next());
                } else {
                    System.out.println("작성한 리뷰가 없습니다.");
                    return;
                }
            }

            // Prompt for the movie title to delete review
            System.out.print("삭제할 리뷰의 영화제목 입력 : ");
            String movieTitle = scanner.nextLine();

            // Prepare SQL statement to delete review
            String deleteReviewSQL = "DELETE FROM my_review WHERE member_id = ? AND movie_title = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteReviewSQL)) {
                // Set parameters
                statement.setInt(1, currentMember.getId());
                statement.setString(2, movieTitle);

                // Execute SQL statement
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected == 0) {
                    System.out.println("해당 영화에 대한 리뷰를 찾을 수 없습니다.");
                    return;
                }

                System.out.println("리뷰 삭제가 완료되었습니다.");
            } catch (SQLException e) {
                System.err.println("SQL 예외 발생: " + e.getMessage());
            } finally {
                // Close database connection
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("SQL 예외 발생: " + e.getMessage());
        }
    }


    public static void writeReview() {
        Member currentMember = members.get(currentMemberIdx);
        try {
            // Connect to the database
            dbConnection.connect();
            Connection connection = dbConnection.getConnection();

            // Query to retrieve the member's reservations
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

                    // Split the myMovie string to get individual movie titles
                    String[] movies = myMovie.split(";");

                    System.out.println("예매 현황 : ");
                    for (String movie : movies) {
                        String[] parts = movie.split(":");
                        System.out.println(parts[0]);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL 예외 발생: " + e.getMessage());
        }

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

