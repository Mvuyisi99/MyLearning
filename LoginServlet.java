package za.ac.cput.mylearning.servlets;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

public class LoginServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter pw = response.getWriter();

        String email = request.getParameter("email").trim();
        String password = request.getParameter("password");

        if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
            pw.println("<h3>Please fill all fields!</h3>");
            return;
        }

        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            Connection con = DriverManager.getConnection(
                    "jdbc:derby://localhost:1527/MyLearningDataBase", "administration", "admin");

            String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, email);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    int userId = rs.getInt("id");  // assuming 'id' column in users table
                    String name = rs.getString("name");
                    String surname = rs.getString("surname");

                    HttpSession session = request.getSession();
                    session.setAttribute("userId", userId);         // ← Critical for saving courses!
                    session.setAttribute("email", email);
                    session.setAttribute("fullName", name + " " + surname);

                    System.out.println("Login successful for user ID: " + userId);

                    // This is the fix — exact filename match
                    response.sendRedirect("Dashboard.html");
                }
            }
            con.close();
        } catch (ClassNotFoundException e) {
            pw.println("<h3>Error: Derby Driver not found - " + e.getMessage() + "</h3>");
            e.printStackTrace();
        } catch (SQLException e) {
            pw.println("<h3>Database Error: " + e.getMessage() + "</h3>");
            e.printStackTrace();
        }
    }
}
