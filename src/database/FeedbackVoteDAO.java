package database;

import model.FeedbackVote;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FeedbackVoteDAO {
    public void saveOrUpdate(FeedbackVote vote) throws SQLException {
        String checkFeedbackSql = "SELECT feedback_id FROM feedback WHERE feedback_id = ?";
        String saveVoteSql = "INSERT INTO feedback_votes (feedback_id, voter_id, vote_type) "
                + "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE vote_type = VALUES(vote_type)";

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement check = connection.prepareStatement(checkFeedbackSql)) {
                    check.setLong(1, vote.getFeedbackId());
                    try (ResultSet result = check.executeQuery()) {
                        if (!result.next()) {
                            throw new IllegalArgumentException("Feedback not found.");
                        }
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(saveVoteSql)) {
                    statement.setLong(1, vote.getFeedbackId());
                    statement.setLong(2, vote.getVoterId());
                    statement.setString(3, vote.getVoteType().name());
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        }
    }
}
