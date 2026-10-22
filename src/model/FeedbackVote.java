package model;

public class FeedbackVote {
    public enum VoteType {
        HELPFUL,
        NOT_HELPFUL
    }

    private final long feedbackId;
    private final long voterId;
    private final VoteType voteType;

    public FeedbackVote(long feedbackId, long voterId, VoteType voteType) {
        if (feedbackId <= 0) {
            throw new IllegalArgumentException("A valid feedback ID is required.");
        }
        if (voterId <= 0) {
            throw new IllegalArgumentException("A valid customer identity is required.");
        }
        if (voteType == null) {
            throw new IllegalArgumentException("Choose Helpful or Not Helpful.");
        }
        this.feedbackId = feedbackId;
        this.voterId = voterId;
        this.voteType = voteType;
    }

    public long getFeedbackId() {
        return feedbackId;
    }

    public long getVoterId() {
        return voterId;
    }

    public VoteType getVoteType() {
        return voteType;
    }
}
