package axon.internship.domain;

import java.time.LocalDateTime;

public class Applicant {
    private String name;
    private String email;
    private LocalDateTime deliveryDateTime;
    private double initialScore;
    private double adjustedScore;

    public Applicant(String name, String email, LocalDateTime deliveryDateTime, double initialScore) {
        this.name = name;
        this.email = email;
        this.deliveryDateTime = deliveryDateTime;
        this.initialScore = initialScore;
        this.adjustedScore = initialScore;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getDeliveryDateTime() {
        return deliveryDateTime;
    }

    public double getInitialScore() {
        return initialScore;
    }

    public double getAdjustedScore() {
        return adjustedScore;
    }

    public String getLastName() {
        String[] nameParts = name.split("\\s+");
        return nameParts[nameParts.length - 1];
    }

    public void adjustScore(double adjustment) {
        adjustedScore += adjustment;
        adjustedScore = Math.max(0, Math.min(10, adjustedScore));
    }
}
