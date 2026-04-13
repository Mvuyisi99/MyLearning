package za.ac.cput.mylearning.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import org.json.JSONObject;
import org.json.JSONArray;

@WebServlet("/load-courses")  // ← Fixed: lowercase, matches JS fetch
public class LoadCoursesServlet extends HttpServlet {

    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");  // ← Fixed: correct MIME type
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {  // ← Fixed: "userId" (lowercase u)
            out.print("[]");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        JSONArray courses = new JSONArray();

        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            try (Connection con = DriverManager.getConnection("jdbc:derby://localhost:1527/MyLearningDataBase", "administration", "admin")) {
                String sql = "SELECT course_data FROM saved_courses WHERE user_id = ? ORDER BY created_at DESC";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, userId);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        String courseJsonStr = rs.getString("course_data");
                        JSONObject course = new JSONObject(courseJsonStr);
                        courses.put(course);
                    }
                }
            }
            out.print(courses.toString());  // ← Critical: send the JSON array!
        } catch (Exception e) {
            e.printStackTrace();
            out.print("[]");  // Return empty on error
        }
    }
}