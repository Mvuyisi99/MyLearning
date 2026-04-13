package za.ac.cput.mylearning.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import org.json.JSONObject;
import java.sql.*;

@WebServlet("/delete-course")
public class DeleteCourseServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            out.print("{\"success\":false,\"message\":\"Not logged in\"}");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        // Read JSON body
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        JSONObject json;
        try {
            json = new JSONObject(sb.toString());
        } catch (Exception e) {
            out.print("{\"success\":false,\"message\":\"Invalid JSON\"}");
            return;
        }

        String title = json.optString("title", "");
        if (title.isEmpty()) {
            out.print("{\"success\":false,\"message\":\"Invalid course title\"}");
            return;
        }

        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            try (Connection con = DriverManager.getConnection(
                    "jdbc:derby://localhost:1527/MyLearningDataBase", "administration", "admin")) {
                // Delete by title AND user_id (safe)
                String sql = "DELETE FROM saved_courses WHERE title = ? AND user_id = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, title);
                    ps.setInt(2, userId);
                    int rows = ps.executeUpdate();

                    if (rows > 0) {
                        out.print("{\"success\":true,\"message\":\"Course deleted\"}");
                    } else {
                        out.print("{\"success\":false,\"message\":\"Course not found or not yours\"}");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"success\":false,\"message\":\"Database error\"}");
        }
    }
}
