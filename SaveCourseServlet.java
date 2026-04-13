package za.ac.cput.mylearning.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

@WebServlet("/save-course")
public class SaveCourseServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Read raw JSON from frontend fetch body
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line.trim());
            }
        }
        String jsonString = sb.toString();

        if (jsonString.isEmpty()) {
            out.print("{\"success\": false, \"message\": \"No data received\"}");
            return;
        }

        try {
            JSONObject courseJson = new JSONObject(jsonString);

            // Check if user is logged in
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                out.print("{\"success\": false, \"message\": \"User not logged in\"}");
                return;
            }
            int userId = (Integer) session.getAttribute("userId");

            // Connect to Derby and save
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            try (Connection con = DriverManager.getConnection("jdbc:derby://localhost:1527/MyLearningDataBase", "administration", "admin")) {
                String sql = "INSERT INTO saved_courses "
                        + "(user_id, title, category, lessons, hours, image_url, course_data) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, userId);
                ps.setString(2, courseJson.optString("title", "Untitled Course"));
                ps.setString(3, courseJson.optString("category", "General"));
                ps.setInt(4, courseJson.optInt("lessons", 0));
                ps.setDouble(5, courseJson.optDouble("hours", 0.0));
                ps.setString(6, courseJson.optString("image", ""));
                ps.setString(7, courseJson.toString()); // Full course JSON saved as string

                int rows = ps.executeUpdate();

                if (rows > 0) {
                    out.print("{\"success\": true, \"message\": \"Course saved successfully!\"}");
                } else {
                    out.print("{\"success\": false, \"message\": \"Failed to save course\"}");
                }
            }
        } catch (JSONException e) {
            out.print("{\"success\": false, \"message\": \"Invalid JSON format\"}");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            out.print("{\"success\": false, \"message\": \"Derby driver not found\"}");
            e.printStackTrace();
        } catch (Exception e) {
            out.print("{\"success\": false, \"message\": \"Database error\"}");
            e.printStackTrace();
        }
    }
}
