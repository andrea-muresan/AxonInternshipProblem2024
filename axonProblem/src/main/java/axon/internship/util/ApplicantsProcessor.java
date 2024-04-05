package axon.internship.util;

import axon.internship.domain.Applicant;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;

public class ApplicantsProcessor {
    /**
     *
     * @param csvStream input stream allowing to read the CSV input file
     * @return the processing output, in JSON format
     */
    public String processApplicants(InputStream csvStream) {
        Map<String, Applicant> uniqueApplicantsMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvStream))) {
            String line;

            while ((line = reader.readLine()) != null) {
                Applicant applicant = parseApplicant(line);
                if (applicant != null) {
                    uniqueApplicantsMap.put(applicant.getEmail(), applicant);
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        List<Applicant> applicants = new ArrayList<>(uniqueApplicantsMap.values());

        applyScoreAdjustments(applicants);

        List<Applicant> topApplicants = computeTopApplicants(applicants);

        double averageScore = computeAverageScore(topApplicants);

        // Prepare JSON output
        JSONObject outputJson = new JSONObject();
        outputJson.put("uniqueApplicants", uniqueApplicantsMap.size());

        JSONArray topApplicantsArray = new JSONArray();
        for (Applicant applicant : topApplicants) {
            topApplicantsArray.put(applicant.getLastName());
        }
        outputJson.put("topApplicants", topApplicantsArray);

        outputJson.put("averageScore", averageScore);

        return outputJson.toString();

    }

    // Parse a line of CSV data into an Applicant object
    private Applicant parseApplicant(String line) {
        String[] parts = line.split(",");
        if (parts.length != 4) {
            return null;
        }
        try {
            String name = parts[0].trim();
            String email = parts[1].trim();
            LocalDateTime deliveryDateTime = LocalDateTime.parse(parts[2].trim());
            double score = Double.parseDouble(parts[3].trim());

            String[] nameParts = name.split("\\s");
            if (nameParts.length < 2) return null;

            if (!isValidEmail(email)) return null;

            if (score < 0 || score > 10) return null;


            return new Applicant(name, email, deliveryDateTime, score);
        } catch (Exception e) {
            return null;
        }
    }

    // Check if an email is valid
    private boolean isValidEmail(String email) {
        return email.matches("[\\w.-_]+@[\\w.-_]+\\.[a-zA-z]{1,}");
    }

    // Apply adjustments to scores based on delivery date and time
    private void applyScoreAdjustments(List<Applicant> applicants) {
        if (applicants.isEmpty()) return;

        LocalDateTime earliestDateTime = applicants.getFirst().getDeliveryDateTime();
        LocalDateTime latestDateTime = applicants.getFirst().getDeliveryDateTime();

        if (!earliestDateTime.toLocalDate().isEqual(latestDateTime.toLocalDate())) {
            for (Applicant applicant : applicants) {
                LocalDateTime deliveryDateTime = applicant.getDeliveryDateTime();
                if (deliveryDateTime.isBefore(earliestDateTime)) {
                    earliestDateTime = deliveryDateTime;
                }
                if (deliveryDateTime.isAfter(latestDateTime)) {
                    latestDateTime = deliveryDateTime;
                }
            }
        }

        // Apply adjustments
        for (Applicant applicant :applicants) {
            if (applicant.getDeliveryDateTime().toLocalDate().isEqual(earliestDateTime.toLocalDate())) {
                applicant.adjustScore(1);
            } else if (applicant.getDeliveryDateTime().isAfter(latestDateTime.withHour(12))) {
                applicant.adjustScore(-1);
            }
        }
    }

    // Compute top applicants based on adjusted scores
    private List<Applicant> computeTopApplicants(List<Applicant> applicants) {
        if (applicants.isEmpty()) return new ArrayList<>();

        applicants.sort(Comparator.comparing(Applicant::getAdjustedScore, Comparator.reverseOrder())
                .thenComparing(Applicant::getInitialScore, Comparator.reverseOrder())
                .thenComparing(Applicant::getDeliveryDateTime)
                .thenComparing(Applicant::getEmail));

        int size = Math.min(3, applicants.size());
        return applicants.subList(0, size);
    }

    private double computeAverageScore(List<Applicant> topApplicants) {
        if (topApplicants.isEmpty()) return 0;

        topApplicants.sort(Comparator.comparing(Applicant::getAdjustedScore).reversed());

        // Get the top half
        List<Applicant> topHalf = topApplicants.subList(0, (topApplicants.size() + 1) / 2);

        double sum = topHalf.stream().mapToDouble(Applicant::getInitialScore).sum();

        double averageScore =  sum / topHalf.size();

        // Format average score to have only two decimals
        DecimalFormat df = new DecimalFormat("#.##");
        return Double.parseDouble(df.format(averageScore));
    }
}
