package axon.internship;


import axon.internship.util.ApplicantsProcessor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Main {
    public static void main(String[] args) {

        // Process applicants
        ApplicantsProcessor processor = new ApplicantsProcessor();
        try (InputStream inputStream = new FileInputStream("mock_applicants.csv")) {
            String output = processor.processApplicants(inputStream);
            System.out.println(output);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}