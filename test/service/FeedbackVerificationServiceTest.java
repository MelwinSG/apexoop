package service;

public class FeedbackVerificationServiceTest {
    public static void main(String[] args) {
        FeedbackVerificationService verification = new FeedbackVerificationService();
        assertScore(90.0, verification.calculateCredibility(18, 2));
        assertScore(70.0, verification.calculateCredibility(7, 3));
        assertScore(30.0, verification.calculateCredibility(3, 7));
        assertScore(0.0, verification.calculateCredibility(0, 0));
        assertTrue(verification.meetsMinimumCredibility(60.0), "Exactly 60% should be verified.");
        assertTrue(!verification.meetsMinimumCredibility(59.9), "Scores below 60% should remain pending.");

        try {
            verification.calculateCredibility(-1, 1);
            throw new AssertionError("Negative vote counts must be rejected.");
        } catch (IllegalArgumentException expected) {
            System.out.println("PASS: negative vote counts are rejected");
        }

        try {
            new FeedbackVerificationService(100.1);
            throw new AssertionError("An invalid minimum score must be rejected.");
        } catch (IllegalArgumentException expected) {
            System.out.println("PASS: invalid minimum score is rejected");
        }

        System.out.println("All feedback verification calculation tests passed.");
    }

    private static void assertScore(double expected, double actual) {
        if (Math.abs(expected - actual) > 0.000001) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
        System.out.println("PASS: credibility score " + expected + "%");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
        System.out.println("PASS: " + message);
    }
}
