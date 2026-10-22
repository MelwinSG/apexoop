USE feedback_db;

CREATE TABLE IF NOT EXISTS feedback_votes (
    vote_id BIGINT NOT NULL AUTO_INCREMENT,
    feedback_id INT NOT NULL,
    voter_id INT NOT NULL,
    vote_type ENUM('HELPFUL', 'NOT_HELPFUL') NOT NULL,
    PRIMARY KEY (vote_id),
    UNIQUE KEY uq_feedback_votes_feedback_voter (feedback_id, voter_id),
    CONSTRAINT fk_feedback_votes_feedback FOREIGN KEY (feedback_id)
        REFERENCES feedback (feedback_id) ON DELETE CASCADE,
    CONSTRAINT fk_feedback_votes_voter FOREIGN KEY (voter_id)
        REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;
