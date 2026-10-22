package service;

import database.FeedbackVoteDAO;
import model.Feedback;
import model.FeedbackVote;
import model.FeedbackVote.VoteType;
import model.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeedbackVerificationService {
    public static final double DEFAULT_MINIMUM_CREDIBILITY_SCORE = 60.0;

    private final FeedbackManager feedbackManager;
    private final FeedbackVoteDAO feedbackVoteDAO;
    private final double minimumCredibilityScore;

    public FeedbackVerificationService() {
        this(DEFAULT_MINIMUM_CREDIBILITY_SCORE);
    }

    public FeedbackVerificationService(double minimumCredibilityScore) {
        if (Double.isNaN(minimumCredibilityScore) || Double.isInfinite(minimumCredibilityScore)
                || minimumCredibilityScore < 0.0 || minimumCredibilityScore > 100.0) {
            throw new IllegalArgumentException("Minimum credibility score must be between 0 and 100.");
        }
        this.feedbackManager = new FeedbackManager();
        this.feedbackVoteDAO = new FeedbackVoteDAO();
        this.minimumCredibilityScore = minimumCredibilityScore;
    }

    public double calculateCredibility(long helpfulVotes, long notHelpfulVotes) {
        if (helpfulVotes < 0 || notHelpfulVotes < 0) {
            throw new IllegalArgumentException("Vote counts cannot be negative.");
        }
        double totalVotes = (double) helpfulVotes + notHelpfulVotes;
        return totalVotes == 0.0 ? 0.0 : (helpfulVotes * 100.0) / totalVotes;
    }

    public boolean meetsMinimumCredibility(double score) {
        if (Double.isNaN(score) || Double.isInfinite(score) || score < 0.0 || score > 100.0) {
            throw new IllegalArgumentException("Credibility score must be between 0 and 100.");
        }
        return score >= minimumCredibilityScore;
    }

    public double getMinimumCredibilityScore() {
        return minimumCredibilityScore;
    }

    public CommunityFeedbackLists listCommunityFeedback(User user) throws SQLException {
        List<Feedback> verified = new ArrayList<>();
        List<Feedback> pending = new ArrayList<>();
        for (Feedback feedback : feedbackManager.listCommunityFeedback(user)) {
            double score = calculateCredibility(feedback.getHelpfulVotes(), feedback.getNotHelpfulVotes());
            (meetsMinimumCredibility(score) ? verified : pending).add(feedback);
        }
        return new CommunityFeedbackLists(verified, pending);
    }

    public void castVote(User user, long feedbackId, VoteType voteType) throws SQLException {
        AuthService.requireRegisteredUser(user);
        FeedbackVote vote = new FeedbackVote(feedbackId, user.getId(), voteType);
        feedbackVoteDAO.saveOrUpdate(vote);
    }

    public static class CommunityFeedbackLists {
        private final List<Feedback> verified;
        private final List<Feedback> pending;

        private CommunityFeedbackLists(List<Feedback> verified, List<Feedback> pending) {
            this.verified = Collections.unmodifiableList(new ArrayList<>(verified));
            this.pending = Collections.unmodifiableList(new ArrayList<>(pending));
        }

        public List<Feedback> getVerified() {
            return verified;
        }

        public List<Feedback> getPending() {
            return pending;
        }
    }
}
